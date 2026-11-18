package com.colora.world.storage

import com.colora.world.engine.content.ContentJson
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * File-backed store for child-made documents with immutable revisions.
 *
 * Layout under [root]:
 *   profiles/<pid>/profile.json
 *   profiles/<pid>/docs/<docId>/rev/<revId>.bin        payload (immutable)
 *   profiles/<pid>/docs/<docId>/rev/<revId>.meta.json  header with sha256; written after the payload
 *   profiles/<pid>/docs/<docId>/current.json           pointer; written last (the commit point)
 *   trash/<pid>/<docId>/...                            user deletions kept 7 days
 *
 * The pointer file is the single commit point, so there is no gap between a
 * "file commit" and a separate metadata commit: Room/indices in the app are
 * derived caches rebuilt from pointers (see docs/DECISIONS.md, ADR-006).
 */
class DocumentStore(val root: File, private val clock: () -> Long = System::currentTimeMillis) {
    private val locks = ConcurrentHashMap<String, Any>()
    private fun lock(docId: String) = locks.computeIfAbsent(docId) { Any() }

    private val profilesDir get() = File(root, "profiles")
    private val trashDir get() = File(root, "trash")
    internal fun profileDir(pid: String) = File(profilesDir, pid)
    internal fun docDir(pid: String, docId: String) = File(profileDir(pid), "docs/$docId")
    private fun revDir(pid: String, docId: String) = File(docDir(pid, docId), "rev")

    // ---------------------------------------------------------------- profiles

    fun saveProfile(p: ChildProfile) {
        Files2.requireId(p.id)
        Files2.writeAtomic(File(profileDir(p.id), "profile.json"), ContentJson.encodeToString(ChildProfile.serializer(), p).toByteArray())
    }

    fun profiles(): List<ChildProfile> = (profilesDir.listFiles() ?: emptyArray())
        .mapNotNull { d -> File(d, "profile.json").takeIf { it.isFile }?.let { runCatching { ContentJson.decodeFromString(ChildProfile.serializer(), it.readText()) }.getOrNull() } }
        .sortedBy { it.createdAt }

    /** Deletes a profile and all its documents (parent-confirmed; the UI offers a backup first). */
    fun deleteProfile(pid: String) { Files2.requireId(pid); Files2.deleteTree(profileDir(pid)); Files2.deleteTree(File(trashDir, pid)) }

    // --------------------------------------------------------------- documents

    /**
     * Commits a new immutable revision and moves the pointer. Calls for the same
     * document are serialised; every call yields a strictly larger sequence so
     * autosave and manual save can never lose each other's update.
     */
    fun commit(
        pid: String, docId: String, kind: DocKind, payload: ByteArray,
        sourceId: String? = null, sourceVersion: Int? = null,
    ): RevisionMeta {
        Files2.requireId(pid); Files2.requireId(docId)
        synchronized(lock(docId)) {
            val prev = pointer(pid, docId)
            val revId = "r" + UUID.randomUUID().toString().replace("-", "")
            val now = clock()
            val meta = RevisionMeta(
                revisionId = revId, documentId = docId, profileId = pid, kind = kind,
                sequence = (prev?.sequence ?: 0) + 1, parentRevisionId = prev?.currentRevisionId,
                sourceId = sourceId, sourceVersion = sourceVersion, createdAt = now,
                sizeBytes = payload.size.toLong(), sha256 = Files2.sha256(payload),
            )
            val rd = revDir(pid, docId)
            Files2.writeAtomic(File(rd, "$revId.bin"), payload)
            Files2.writeAtomic(File(rd, "$revId.meta.json"), ContentJson.encodeToString(RevisionMeta.serializer(), meta).toByteArray())
            val ptr = DocPointer(docId, pid, kind, revId, meta.sequence, prev?.favorite ?: false, prev?.createdAt ?: now, now)
            writePointer(ptr)
            return meta
        }
    }

    private fun writePointer(p: DocPointer) =
        Files2.writeAtomic(File(docDir(p.profileId, p.documentId), "current.json"), ContentJson.encodeToString(DocPointer.serializer(), p).toByteArray())

    fun pointer(pid: String, docId: String): DocPointer? {
        val f = File(docDir(pid, docId), "current.json")
        if (!f.isFile) return null
        return runCatching { ContentJson.decodeFromString(DocPointer.serializer(), f.readText()) }.getOrNull()
    }

    fun setFavorite(pid: String, docId: String, favorite: Boolean) = synchronized(lock(docId)) {
        val p = pointer(pid, docId) ?: return@synchronized
        writePointer(p.copy(favorite = favorite))
    }

    fun documents(pid: String, kind: DocKind? = null): List<DocPointer> =
        (File(profileDir(pid), "docs").listFiles() ?: emptyArray())
            .mapNotNull { pointer(pid, it.name) }
            .filter { kind == null || it.kind == kind }
            .sortedByDescending { it.updatedAt }

    fun revisions(pid: String, docId: String): List<RevisionMeta> =
        (revDir(pid, docId).listFiles { f -> f.name.endsWith(".meta.json") } ?: emptyArray())
            .mapNotNull { readMeta(it) }.sortedBy { it.sequence }

    private fun readMeta(f: File) = runCatching { ContentJson.decodeFromString(RevisionMeta.serializer(), f.readText()) }.getOrNull()

    /** Reads and verifies an immutable revision. Returns null if missing or corrupt. */
    fun readRevision(pid: String, docId: String, revId: String): Pair<RevisionMeta, ByteArray>? {
        val meta = File(revDir(pid, docId), "$revId.meta.json").takeIf { it.isFile }?.let { readMeta(it) } ?: return null
        val bin = File(revDir(pid, docId), "$revId.bin").takeIf { it.isFile } ?: return null
        val bytes = bin.readBytes()
        if (bytes.size.toLong() != meta.sizeBytes || Files2.sha256(bytes) != meta.sha256) return null
        return meta to bytes
    }

    /** Current revision, falling back to the newest valid revision if the pointer target is broken. */
    fun readCurrent(pid: String, docId: String): Pair<RevisionMeta, ByteArray>? {
        pointer(pid, docId)?.let { p -> readRevision(pid, docId, p.currentRevisionId)?.let { return it } }
        return revisions(pid, docId).asReversed().firstNotNullOfOrNull { readRevision(pid, docId, it.revisionId) }
    }

    // ---------------------------------------------------------------- recovery

    /**
     * Startup recovery: removes orphan temp files, re-points documents whose
     * pointer is missing/broken or older than a newer valid revision (a crash
     * between revision write and pointer write), and purges trash > 7 days.
     */
    fun recover(): RecoveryReport {
        var tmp = 0
        root.walkTopDown().filter { it.isFile && it.name.startsWith(Files2.TMP_PREFIX) }.toList().forEach { if (it.delete()) tmp++ }
        val repaired = mutableListOf<String>()
        val invalid = mutableListOf<String>()
        for (pd in profilesDir.listFiles() ?: emptyArray()) {
            for (dd in File(pd, "docs").listFiles() ?: emptyArray()) {
                val pid = pd.name; val docId = dd.name
                synchronized(lock(docId)) {
                    val valid = revisions(pid, docId).filter { m ->
                        (readRevision(pid, docId, m.revisionId) != null).also { ok -> if (!ok) invalid += m.revisionId }
                    }
                    // payload without meta = revision never completed; remove it
                    File(dd, "rev").listFiles { f -> f.name.endsWith(".bin") }?.forEach { b ->
                        if (!File(b.parentFile, b.name.removeSuffix(".bin") + ".meta.json").exists()) b.delete()
                    }
                    val newest = valid.maxByOrNull { it.sequence } ?: return@synchronized
                    val p = pointer(pid, docId)
                    val pointsToValid = p != null && valid.any { it.revisionId == p.currentRevisionId }
                    if (p == null || !pointsToValid || newest.sequence > p.sequence) {
                        val now = clock()
                        writePointer(DocPointer(docId, pid, newest.kind, newest.revisionId, newest.sequence, p?.favorite ?: false, p?.createdAt ?: valid.first().createdAt, now))
                        repaired += docId
                    }
                }
            }
        }
        return RecoveryReport(tmp, repaired, invalid, purgeTrash())
    }

    /**
     * Removes old revisions but never the current one, never one in [referenced]
     * (scenes, books, toys, story progress) and always keeps the newest [keepLatest].
     */
    fun collectGarbage(pid: String, docId: String, referenced: Set<String>, keepLatest: Int = 5): Int = synchronized(lock(docId)) {
        val p = pointer(pid, docId) ?: return@synchronized 0
        val revs = revisions(pid, docId)
        val keep = revs.takeLast(keepLatest).map { it.revisionId }.toSet() + p.currentRevisionId + referenced
        var n = 0
        for (m in revs) if (m.revisionId !in keep) {
            File(revDir(pid, docId), "${m.revisionId}.meta.json").delete()
            File(revDir(pid, docId), "${m.revisionId}.bin").delete()
            n++
        }
        n
    }

    // -------------------------------------------------------------------- trash

    fun moveToTrash(pid: String, docId: String) = synchronized(lock(docId)) {
        val p = pointer(pid, docId) ?: return@synchronized
        val dst = File(trashDir, "$pid/$docId")
        Files2.deleteTree(dst); dst.parentFile.mkdirs()
        Files2.writeAtomic(File(docDir(pid, docId), "trash.json"), ContentJson.encodeToString(TrashRecord.serializer(), TrashRecord(docId, pid, clock(), p.kind)).toByteArray())
        java.nio.file.Files.move(docDir(pid, docId).toPath(), dst.toPath(), java.nio.file.StandardCopyOption.ATOMIC_MOVE)
        Files2.syncDir(dst.parentFile)
    }

    fun trash(pid: String): List<TrashRecord> = (File(trashDir, pid).listFiles() ?: emptyArray())
        .mapNotNull { d -> runCatching { ContentJson.decodeFromString(TrashRecord.serializer(), File(d, "trash.json").readText()) }.getOrNull() }

    fun restoreFromTrash(pid: String, docId: String): Boolean = synchronized(lock(docId)) {
        val src = File(trashDir, "$pid/$docId")
        val dst = docDir(pid, docId)
        if (!src.isDirectory || dst.exists()) return@synchronized false
        dst.parentFile.mkdirs()
        File(src, "trash.json").delete()
        java.nio.file.Files.move(src.toPath(), dst.toPath(), java.nio.file.StandardCopyOption.ATOMIC_MOVE)
        true
    }

    fun deleteForever(pid: String, docId: String) = Files2.deleteTree(File(trashDir, "$pid/$docId"))

    fun purgeTrash(): Int {
        val cutoff = clock() - TRASH_DAYS * 24L * 3600_000L
        var n = 0
        for (pd in trashDir.listFiles() ?: emptyArray()) for (r in trash(pd.name)) if (r.deletedAt < cutoff) { deleteForever(pd.name, r.documentId); n++ }
        return n
    }

    companion object { const val TRASH_DAYS = 7 }
}
