package com.colora.world.storage

import com.colora.world.engine.content.ContentJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Serializable
data class BackupEntry(val path: String, val size: Long, val sha256: String)

@Serializable
data class BackupManifest(
    val format: String = FORMAT,
    val schema: Int = SCHEMA,
    val createdAt: Long,
    val appVersion: String,
    val contentVersion: Int,
    val profiles: List<String>,
    val entries: List<BackupEntry>,
) {
    companion object { const val FORMAT = "colora-backup"; const val SCHEMA = 1 }
}

class BackupException(val reason: Reason, detail: String) : Exception("$reason: $detail") {
    enum class Reason {
        NOT_A_BACKUP, UNSUPPORTED_SCHEMA, TOO_MANY_ENTRIES, TOO_LARGE, SUSPICIOUS_RATIO,
        UNSAFE_PATH, SYMLINK, DUPLICATE_ENTRY, FORBIDDEN_CONTENT, DIGEST_MISMATCH,
        MISSING_ENTRY, UNLISTED_ENTRY, CORRUPT_DATA,
    }
}

data class BackupLimits(
    val maxEntries: Int = 20_000,
    val maxEntryBytes: Long = 64L * 1024 * 1024,
    val maxTotalBytes: Long = 1024L * 1024 * 1024,
    val maxCompressedBytes: Long = 512L * 1024 * 1024,
    val maxRatio: Double = 120.0,
)

data class ImportResult(val profileIdMap: Map<String, String>, val documents: Int, val revisions: Int, val remappedIds: Int)

/**
 * `.colora.zip` export/import. Only child-made documents and profiles are
 * written; parent settings, PIN/gate state, entitlements, purchase tokens and
 * logs are not part of the store and the importer refuses any path outside
 * the allowed layout. Import is staged and validated in full before a single
 * atomic move into the library; failures leave the library untouched.
 */
class BackupService(private val store: DocumentStore, private val appVersion: String, private val contentVersion: Int) {

    // ------------------------------------------------------------------ export

    /** Writes a consistent snapshot: for each document only the revisions that are valid at export time. */
    fun export(profileIds: List<String>, out: OutputStream, now: Long = System.currentTimeMillis()) {
        val entries = mutableListOf<Pair<String, ByteArray>>()
        for (pid in profileIds) {
            val pf = File(store.profileDir(pid), "profile.json")
            require(pf.isFile) { "unknown profile $pid" }
            entries += "profiles/$pid/profile.json" to pf.readBytes()
            for (ptr in store.documents(pid)) {
                val docId = ptr.documentId
                var any = false
                for (m in store.revisions(pid, docId)) {
                    val (meta, bytes) = store.readRevision(pid, docId, m.revisionId) ?: continue
                    entries += "profiles/$pid/docs/$docId/rev/${meta.revisionId}.bin" to bytes
                    entries += "profiles/$pid/docs/$docId/rev/${meta.revisionId}.meta.json" to ContentJson.encodeToString(RevisionMeta.serializer(), meta).toByteArray()
                    any = true
                }
                if (!any) continue
                val current = store.readCurrent(pid, docId)!!.first
                val p = ptr.copy(currentRevisionId = current.revisionId, sequence = current.sequence)
                entries += "profiles/$pid/docs/$docId/current.json" to ContentJson.encodeToString(DocPointer.serializer(), p).toByteArray()
            }
        }
        val manifest = BackupManifest(createdAt = now, appVersion = appVersion, contentVersion = contentVersion, profiles = profileIds,
            entries = entries.map { (p, b) -> BackupEntry(p, b.size.toLong(), Files2.sha256(b)) })
        ZipOutputStream(out).use { z ->
            z.putNextEntry(ZipEntry(MANIFEST)); z.write(ContentJson.encodeToString(BackupManifest.serializer(), manifest).toByteArray()); z.closeEntry()
            for ((p, b) in entries) { z.putNextEntry(ZipEntry(p)); z.write(b); z.closeEntry() }
        }
    }

    // ------------------------------------------------------------------ import

    /** Validates without changing anything; used for the parent preview. */
    fun inspect(zip: File, limits: BackupLimits = BackupLimits()): BackupManifest {
        val staging = newStaging()
        try { return stageAndValidate(zip, staging, limits) } finally { Files2.deleteTree(staging) }
    }

    /** Imports every profile in the backup as a NEW profile (the default; never overwrites). */
    fun importAsNewProfiles(zip: File, limits: BackupLimits = BackupLimits()): ImportResult {
        val staging = newStaging()
        val moved = mutableListOf<File>()
        try {
            val manifest = stageAndValidate(zip, staging, limits)
            // ---- remap ids: profile ids always; document/revision ids when they collide with the library
            val idMap = HashMap<String, String>()
            for (pid in manifest.profiles) idMap[pid] = newId("p")
            val existingDocs = HashSet<String>()
            store.profiles().forEach { p -> store.documents(p.id).forEach { existingDocs += it.documentId; store.revisions(p.id, it.documentId).forEach { r -> existingDocs += r.revisionId } } }
            var docs = 0; var revs = 0
            for (pid in manifest.profiles) {
                for (dd in File(staging, "profiles/$pid/docs").listFiles() ?: emptyArray()) {
                    if (dd.name in existingDocs) idMap[dd.name] = newId("d")
                    for (m in File(dd, "rev").listFiles { f -> f.name.endsWith(".meta.json") } ?: emptyArray()) {
                        val rid = m.name.removeSuffix(".meta.json")
                        if (rid in existingDocs) idMap[rid] = "r" + UUID.randomUUID().toString().replace("-", "")
                    }
                }
            }
            // ---- build the remapped library tree inside staging/out
            val outRoot = File(staging, "out")
            for (pid in manifest.profiles) {
                val npid = idMap.getValue(pid)
                val src = File(staging, "profiles/$pid")
                val prof = ContentJson.decodeFromString(ChildProfile.serializer(), File(src, "profile.json").readText())
                writePlain(File(outRoot, "$npid/profile.json"), ContentJson.encodeToString(ChildProfile.serializer(), prof.copy(id = npid)).toByteArray())
                for (dd in File(src, "docs").listFiles() ?: emptyArray()) {
                    val ndoc = idMap[dd.name] ?: dd.name
                    val ptr = ContentJson.decodeFromString(DocPointer.serializer(), File(dd, "current.json").readText())
                    for (m in File(dd, "rev").listFiles { f -> f.name.endsWith(".meta.json") } ?: emptyArray()) {
                        val meta = ContentJson.decodeFromString(RevisionMeta.serializer(), m.readText())
                        val payload = File(dd, "rev/${meta.revisionId}.bin").readBytes()
                        val newPayload = remapPayload(payload, idMap)
                        val nrid = idMap[meta.revisionId] ?: meta.revisionId
                        val nmeta = meta.copy(revisionId = nrid, documentId = ndoc, profileId = npid,
                            parentRevisionId = meta.parentRevisionId?.let { idMap[it] ?: it },
                            sizeBytes = newPayload.size.toLong(), sha256 = Files2.sha256(newPayload))
                        writePlain(File(outRoot, "$npid/docs/$ndoc/rev/$nrid.bin"), newPayload)
                        writePlain(File(outRoot, "$npid/docs/$ndoc/rev/$nrid.meta.json"), ContentJson.encodeToString(RevisionMeta.serializer(), nmeta).toByteArray())
                        revs++
                    }
                    val nptr = ptr.copy(documentId = ndoc, profileId = npid, currentRevisionId = idMap[ptr.currentRevisionId] ?: ptr.currentRevisionId)
                    writePlain(File(outRoot, "$npid/docs/$ndoc/current.json"), ContentJson.encodeToString(DocPointer.serializer(), nptr).toByteArray())
                    docs++
                }
            }
            // ---- commit: one atomic directory move per profile; roll back on any failure
            for (pid in manifest.profiles) {
                val npid = idMap.getValue(pid)
                val dst = store.profileDir(npid)
                dst.parentFile.mkdirs()
                java.nio.file.Files.move(File(outRoot, npid).toPath(), dst.toPath(), java.nio.file.StandardCopyOption.ATOMIC_MOVE)
                moved += dst
            }
            Files2.syncDir(File(store.root, "profiles"))
            val remapped = idMap.size - manifest.profiles.size
            return ImportResult(manifest.profiles.associateWith { idMap.getValue(it) }, docs, revs, remapped)
        } catch (e: Exception) {
            moved.forEach { Files2.deleteTree(it) }
            throw if (e is BackupException) e else BackupException(BackupException.Reason.CORRUPT_DATA, e.message ?: e.toString())
        } finally {
            Files2.deleteTree(staging)
        }
    }

    private fun writePlain(f: File, b: ByteArray) { f.parentFile.mkdirs(); f.writeBytes(b) }

    /** Rewrites id references (e.g. artworkRevisionId inside scenes and books) when ids were remapped. */
    private fun remapPayload(payload: ByteArray, idMap: Map<String, String>): ByteArray {
        if (idMap.isEmpty()) return payload
        val text = runCatching { payload.toString(Charsets.UTF_8) }.getOrNull() ?: return payload
        val tree = runCatching { ContentJson.parseToJsonElement(text) }.getOrNull() ?: return payload
        var changed = false
        fun walk(e: JsonElement): JsonElement = when (e) {
            is JsonObject -> JsonObject(e.mapValues { walk(it.value) })
            is JsonArray -> JsonArray(e.map { walk(it) })
            is JsonPrimitive -> if (e.isString && e.content in idMap) { changed = true; JsonPrimitive(idMap.getValue(e.content)) } else e
        }
        val out = walk(tree)
        return if (changed) ContentJson.encodeToString(JsonElement.serializer(), out).toByteArray() else payload
    }

    private fun newStaging(): File = File(store.root, ".staging/" + UUID.randomUUID()).also { it.mkdirs() }
    private fun newId(prefix: String) = prefix + UUID.randomUUID().toString().replace("-", "").take(24)

    private fun fail(r: BackupException.Reason, d: String): Nothing = throw BackupException(r, d)

    private fun stageAndValidate(zip: File, staging: File, limits: BackupLimits): BackupManifest {
        if (zip.length() > limits.maxCompressedBytes) fail(BackupException.Reason.TOO_LARGE, "archive ${zip.length()} bytes")
        // 1. central directory: entry count, symlinks, declared names
        val central = readCentralDirectory(zip)
        if (central.size > limits.maxEntries) fail(BackupException.Reason.TOO_MANY_ENTRIES, "${central.size}")
        for (c in central) {
            if (c.isSymlink) fail(BackupException.Reason.SYMLINK, c.name)
            checkPath(c.name)
        }
        if (central.map { it.name }.toSet().size != central.size) fail(BackupException.Reason.DUPLICATE_ENTRY, "duplicate names in directory")
        // 2. stream entries with real byte counting (declared sizes are not trusted)
        var total = 0L
        val seen = HashSet<String>()
        val digests = HashMap<String, Pair<Long, String>>()
        var manifestBytes: ByteArray? = null
        ZipInputStream(zip.inputStream().buffered()).use { zin ->
            while (true) {
                val e = zin.nextEntry ?: break
                val name = e.name
                checkPath(name)
                if (e.isDirectory) continue
                if (!seen.add(name)) fail(BackupException.Reason.DUPLICATE_ENTRY, name)
                if (seen.size > limits.maxEntries) fail(BackupException.Reason.TOO_MANY_ENTRIES, "${seen.size}")
                val bytes = readLimited(zin, limits.maxEntryBytes, name)
                total += bytes.size
                if (total > limits.maxTotalBytes) fail(BackupException.Reason.TOO_LARGE, "total $total")
                if (zip.length() > 0 && total.toDouble() / zip.length() > limits.maxRatio && total > 1_000_000)
                    fail(BackupException.Reason.SUSPICIOUS_RATIO, "ratio ${total / zip.length()}")
                if (name == MANIFEST) { manifestBytes = bytes; continue }
                val f = File(staging, name)
                f.parentFile.mkdirs(); f.writeBytes(bytes)
                digests[name] = bytes.size.toLong() to Files2.sha256(bytes)
            }
        }
        // 3. manifest and integrity index
        val mb = manifestBytes ?: fail(BackupException.Reason.NOT_A_BACKUP, "no manifest")
        val manifest = runCatching { ContentJson.decodeFromString(BackupManifest.serializer(), mb.toString(Charsets.UTF_8)) }
            .getOrElse { fail(BackupException.Reason.NOT_A_BACKUP, "manifest unreadable") }
        if (manifest.format != BackupManifest.FORMAT) fail(BackupException.Reason.NOT_A_BACKUP, manifest.format)
        if (manifest.schema > BackupManifest.SCHEMA) fail(BackupException.Reason.UNSUPPORTED_SCHEMA, "${manifest.schema}")
        if (manifest.profiles.isEmpty()) fail(BackupException.Reason.CORRUPT_DATA, "no profiles")
        manifest.profiles.forEach { runCatching { Files2.requireId(it) }.getOrElse { _ -> fail(BackupException.Reason.UNSAFE_PATH, it) } }
        val listed = manifest.entries.associateBy { it.path }
        if (listed.size != manifest.entries.size) fail(BackupException.Reason.DUPLICATE_ENTRY, "manifest")
        for ((p, e) in listed) {
            val got = digests[p] ?: fail(BackupException.Reason.MISSING_ENTRY, p)
            if (got.first != e.size || got.second != e.sha256) fail(BackupException.Reason.DIGEST_MISMATCH, p)
        }
        for (p in digests.keys) if (p !in listed) fail(BackupException.Reason.UNLISTED_ENTRY, p)
        // 4. structure: every entry belongs to a listed profile; every document parses and is self-consistent
        for (p in digests.keys) {
            val pid = p.split('/')[1]
            if (pid !in manifest.profiles) fail(BackupException.Reason.FORBIDDEN_CONTENT, p)
        }
        for (pid in manifest.profiles) {
            val pf = File(staging, "profiles/$pid/profile.json")
            if (!pf.isFile) fail(BackupException.Reason.MISSING_ENTRY, "profiles/$pid/profile.json")
            runCatching { ContentJson.decodeFromString(ChildProfile.serializer(), pf.readText()) }.getOrElse { fail(BackupException.Reason.CORRUPT_DATA, pf.name) }
            for (dd in File(staging, "profiles/$pid/docs").listFiles() ?: emptyArray()) {
                val ptr = runCatching { ContentJson.decodeFromString(DocPointer.serializer(), File(dd, "current.json").readText()) }
                    .getOrElse { fail(BackupException.Reason.CORRUPT_DATA, "${dd.name}/current.json") }
                if (ptr.documentId != dd.name || ptr.profileId != pid) fail(BackupException.Reason.CORRUPT_DATA, "pointer ids ${dd.name}")
                var hasCurrent = false
                for (m in File(dd, "rev").listFiles { f -> f.name.endsWith(".meta.json") } ?: emptyArray()) {
                    val meta = runCatching { ContentJson.decodeFromString(RevisionMeta.serializer(), m.readText()) }
                        .getOrElse { fail(BackupException.Reason.CORRUPT_DATA, m.name) }
                    val bin = File(dd, "rev/${meta.revisionId}.bin")
                    if (m.name != "${meta.revisionId}.meta.json" || meta.documentId != dd.name || !bin.isFile) fail(BackupException.Reason.CORRUPT_DATA, m.name)
                    if (Files2.sha256(bin) != meta.sha256) fail(BackupException.Reason.DIGEST_MISMATCH, bin.name)
                    if (meta.revisionId == ptr.currentRevisionId) hasCurrent = true
                }
                if (!hasCurrent) fail(BackupException.Reason.MISSING_ENTRY, "current revision of ${dd.name}")
            }
        }
        return manifest
    }

    private fun readLimited(inp: InputStream, max: Long, name: String): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buf = ByteArray(32 * 1024)
        var n = 0L
        while (true) {
            val r = inp.read(buf); if (r < 0) break
            n += r
            if (n > max) fail(BackupException.Reason.TOO_LARGE, "entry $name")
            out.write(buf, 0, r)
        }
        return out.toByteArray()
    }

    companion object {
        const val MANIFEST = "manifest.json"
        private val ALLOWED = listOf(
            Regex("^profiles/[A-Za-z0-9_-]{1,64}/profile\\.json$"),
            Regex("^profiles/[A-Za-z0-9_-]{1,64}/docs/[A-Za-z0-9_-]{1,64}/current\\.json$"),
            Regex("^profiles/[A-Za-z0-9_-]{1,64}/docs/[A-Za-z0-9_-]{1,64}/rev/[A-Za-z0-9_-]{1,64}\\.(bin|meta\\.json)$"),
        )

        /** Only the exact library layout is accepted: no traversal, absolute paths, backslashes or foreign files. */
        internal fun checkPath(name: String) {
            if (name == MANIFEST) return
            val bad = name.isEmpty() || name.startsWith("/") || name.contains('\\') || name.contains("..") ||
                name.contains(':') || name.contains('\u0000') || name.split('/').any { it == "." || it.isEmpty() }
            if (bad) throw BackupException(BackupException.Reason.UNSAFE_PATH, name)
            if (ALLOWED.none { it.matches(name) }) throw BackupException(BackupException.Reason.FORBIDDEN_CONTENT, name)
        }

        data class CentralEntry(val name: String, val isSymlink: Boolean)

        /** Minimal ZIP central-directory reader (java.util.zip hides external attributes, needed for symlink detection). */
        internal fun readCentralDirectory(zip: File): List<CentralEntry> {
            RandomAccessFile(zip, "r").use { raf ->
                val len = raf.length()
                if (len < 22) throw BackupException(BackupException.Reason.NOT_A_BACKUP, "too small")
                val tailLen = minOf(len, 22L + 65535L).toInt()
                val tail = ByteArray(tailLen)
                raf.seek(len - tailLen); raf.readFully(tail)
                var eocd = -1
                for (i in tailLen - 22 downTo 0) if (le32(tail, i) == 0x06054b50L) { eocd = i; break }
                if (eocd < 0) throw BackupException(BackupException.Reason.NOT_A_BACKUP, "no end of central directory")
                val count = le16(tail, eocd + 10)
                val cdSize = le32(tail, eocd + 12)
                val cdOffset = le32(tail, eocd + 16)
                if (cdOffset + cdSize > len || cdSize > 64L * 1024 * 1024) throw BackupException(BackupException.Reason.NOT_A_BACKUP, "bad central directory")
                val cd = ByteArray(cdSize.toInt())
                raf.seek(cdOffset); raf.readFully(cd)
                val out = ArrayList<CentralEntry>()
                var p = 0
                repeat(count) {
                    if (p + 46 > cd.size || le32(cd, p) != 0x02014b50L) throw BackupException(BackupException.Reason.NOT_A_BACKUP, "bad entry header")
                    val madeBy = le16(cd, p + 4) ushr 8
                    val nameLen = le16(cd, p + 28); val extraLen = le16(cd, p + 30); val commentLen = le16(cd, p + 32)
                    val ext = le32(cd, p + 38)
                    val name = String(cd, p + 46, nameLen, Charsets.UTF_8)
                    val unixMode = (ext ushr 16) and 0xFFFF
                    out += CentralEntry(name, madeBy == 3 && (unixMode and 0xF000) == 0xA000L)
                    p += 46 + nameLen + extraLen + commentLen
                }
                return out
            }
        }
        private fun le16(b: ByteArray, i: Int) = (b[i].toInt() and 0xFF) or ((b[i + 1].toInt() and 0xFF) shl 8)
        private fun le32(b: ByteArray, i: Int) = (le16(b, i).toLong()) or (le16(b, i + 2).toLong() shl 16)
    }
}

