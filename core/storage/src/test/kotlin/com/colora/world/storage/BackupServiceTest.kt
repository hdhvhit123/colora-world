package com.colora.world.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class BackupServiceTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun library(name: String): Pair<DocumentStore, BackupService> {
        val s = DocumentStore(tmp.newFolder(name))
        return s to BackupService(s, "1.0.0-test", 1)
    }

    private fun seed(s: DocumentStore, pid: String = "kid1"): Map<String, String> {
        s.saveProfile(ChildProfile(pid, "avatar.fox", "Mai", createdAt = 1))
        val art = s.commit(pid, "art1", DocKind.ARTWORK, """{"cmds":[1,2,3]}""".toByteArray(), "animals.cat", 1)
        val art2 = s.commit(pid, "art1", DocKind.ARTWORK, """{"cmds":[1,2,3,4]}""".toByteArray(), "animals.cat", 1)
        val scene = s.commit(pid, "scene1", DocKind.SCENE, """{"objects":[{"id":"t1","artworkRevisionId":"${art.revisionId}"}]}""".toByteArray())
        return mapOf("art" to art.revisionId, "art2" to art2.revisionId, "scene" to scene.revisionId)
    }

    private fun exportTo(svc: BackupService, pids: List<String>): File {
        val f = File(tmp.root, "b-${System.nanoTime()}.colora.zip")
        f.outputStream().use { svc.export(pids, it) }
        return f
    }

    @Test fun exportImportRoundTripIntoCleanApp() {
        val (a, sa) = library("a"); seed(a)
        val zip = exportTo(sa, listOf("kid1"))
        val (b, sb) = library("b")
        val res = sb.importAsNewProfiles(zip)
        val npid = res.profileIdMap.getValue("kid1")
        assertNotEquals("kid1", npid)                         // imported as a new profile, never overwrite
        assertEquals("Mai", b.profiles().single().localAlias)
        assertEquals(2, res.documents); assertEquals(3, res.revisions)
        assertEquals("""{"cmds":[1,2,3,4]}""", String(b.readCurrent(npid, "art1")!!.second))
        assertEquals(2, b.revisions(npid, "art1").size)
        // the archive never contains anything but the allowed layout
        ZipFile(zip).use { z -> z.entries().toList().forEach { e -> BackupService.checkPath(e.name) } }
    }

    @Test fun importingTwiceRemapsCollidingIdsAndRewritesReferences() {
        val (a, sa) = library("a"); val ids = seed(a)
        val zip = exportTo(sa, listOf("kid1"))
        val first = sa.importAsNewProfiles(zip)   // into the SAME library: every doc/rev id collides
        val npid = first.profileIdMap.getValue("kid1")
        assertTrue(first.remappedIds >= 5)
        val docs = a.documents(npid).map { it.documentId }.toSet()
        assertTrue("art1" !in docs && "scene1" !in docs)
        val newScene = a.documents(npid, DocKind.SCENE).single()
        val text = String(a.readCurrent(npid, newScene.documentId)!!.second)
        assertTrue(ids.getValue("art") !in text)                   // reference rewritten to the remapped revision
        val newArt = a.documents(npid, DocKind.ARTWORK).single()
        val revIds = a.revisions(npid, newArt.documentId).map { it.revisionId }
        assertTrue(revIds.any { it in text })
        // original library untouched
        assertEquals("""{"cmds":[1,2,3,4]}""", String(a.readCurrent("kid1", "art1")!!.second))
    }

    // ---- hostile archives -------------------------------------------------

    private fun craft(entries: List<Pair<String, ByteArray>>, manifestFor: List<Pair<String, ByteArray>> = entries, profiles: List<String> = listOf("kid1")): File {
        val m = BackupManifest(createdAt = 1, appVersion = "x", contentVersion = 1, profiles = profiles,
            entries = manifestFor.map { (p, b) -> BackupEntry(p, b.size.toLong(), Files2.sha256(b)) })
        val f = File(tmp.root, "evil-${System.nanoTime()}.zip")
        ZipOutputStream(f.outputStream()).use { z ->
            z.putNextEntry(ZipEntry("manifest.json")); z.write(com.colora.world.engine.content.ContentJson.encodeToString(BackupManifest.serializer(), m).toByteArray()); z.closeEntry()
            for ((p, b) in entries) { z.putNextEntry(ZipEntry(p)); z.write(b); z.closeEntry() }
        }
        return f
    }

    private fun expect(reason: BackupException.Reason, zip: File, limits: BackupLimits = BackupLimits()) {
        val (lib, svc) = library("t${System.nanoTime()}")
        seed(lib, "existing")
        val before = lib.root.walkTopDown().filter { it.isFile }.map { it.relativeTo(lib.root).path to it.readBytes().contentHashCode() }.toSet()
        try { svc.importAsNewProfiles(zip, limits); fail("expected $reason") } catch (e: BackupException) { assertEquals(reason, e.reason) }
        val after = lib.root.walkTopDown().filter { it.isFile }.map { it.relativeTo(lib.root).path to it.readBytes().contentHashCode() }.toSet()
        assertEquals("library must be untouched after a failed import", before, after)
    }

    private val profile = "profiles/kid1/profile.json" to """{"id":"kid1","avatarId":"a","createdAt":1}""".toByteArray()

    @Test fun zipSlipIsBlocked() = expect(BackupException.Reason.UNSAFE_PATH, craft(listOf(profile, "profiles/kid1/../../../etc/x.json" to "x".toByteArray())))
    @Test fun absolutePathIsBlocked() = expect(BackupException.Reason.UNSAFE_PATH, craft(listOf(profile, "/data/x.json" to "x".toByteArray())))
    @Test fun backslashPathIsBlocked() = expect(BackupException.Reason.UNSAFE_PATH, craft(listOf(profile, "profiles\\kid1\\x.json" to "x".toByteArray())))
    @Test fun entitlementFilesAreRefused() = expect(BackupException.Reason.FORBIDDEN_CONTENT, craft(listOf(profile, "entitlements/full.json" to """{"granted":true}""".toByteArray())))
    @Test fun scriptsAreRefused() = expect(BackupException.Reason.FORBIDDEN_CONTENT, craft(listOf(profile, "profiles/kid1/run.sh" to "rm -rf /".toByteArray())))
    @Test fun digestMismatchIsRefused() = expect(BackupException.Reason.DIGEST_MISMATCH,
        craft(listOf(profile), manifestFor = listOf(profile.first to "different".toByteArray())))
    @Test fun missingEntryIsRefused() = expect(BackupException.Reason.MISSING_ENTRY,
        craft(listOf(profile), manifestFor = listOf(profile, "profiles/kid1/docs/d/current.json" to "{}".toByteArray())))
    @Test fun unlistedEntryIsRefused() = expect(BackupException.Reason.UNLISTED_ENTRY,
        craft(listOf(profile, "profiles/kid1/docs/d/current.json" to "{}".toByteArray()), manifestFor = listOf(profile)))
    @Test fun entryForUnlistedProfileIsRefused() = expect(BackupException.Reason.FORBIDDEN_CONTENT,
        craft(listOf(profile, "profiles/other/profile.json" to profile.second)))
    @Test fun tooManyEntries() = expect(BackupException.Reason.TOO_MANY_ENTRIES,
        craft(listOf(profile) + (1..30).map { "profiles/kid1/docs/d$it/current.json" to "{}".toByteArray() }), BackupLimits(maxEntries = 10))
    @Test fun zipBombRatioIsRefused() = expect(BackupException.Reason.SUSPICIOUS_RATIO,
        craft(listOf(profile, "profiles/kid1/docs/d/rev/r1.bin" to ByteArray(20_000_000))))
    @Test fun oversizedEntryIsRefused() = expect(BackupException.Reason.TOO_LARGE,
        craft(listOf(profile, "profiles/kid1/docs/d/rev/r1.bin" to ByteArray(2_000_000) { it.toByte() })), BackupLimits(maxEntryBytes = 1_000_000))
    @Test fun corruptRevisionMetaIsRefused() {
        val bin = "payload".toByteArray()
        val meta = """{"revisionId":"r1","documentId":"d","profileId":"kid1","kind":"ARTWORK","sequence":1,"createdAt":1,"sizeBytes":7,"sha256":"00"}""".toByteArray()
        val ptr = """{"documentId":"d","profileId":"kid1","kind":"ARTWORK","currentRevisionId":"r1","sequence":1,"createdAt":1,"updatedAt":1}""".toByteArray()
        expect(BackupException.Reason.DIGEST_MISMATCH, craft(listOf(profile, "profiles/kid1/docs/d/rev/r1.bin" to bin,
            "profiles/kid1/docs/d/rev/r1.meta.json" to meta, "profiles/kid1/docs/d/current.json" to ptr)))
    }
    @Test fun notAZip() { val f = File(tmp.root, "x.zip").apply { writeText("hello, not a zip at all........") }; expect(BackupException.Reason.NOT_A_BACKUP, f) }

    @Test fun symlinkEntryIsRefused() {
        // Hand-patch a central-directory record to "made by Unix" with S_IFLNK mode.
        val f = craft(listOf(profile))
        val bytes = f.readBytes()
        var i = bytes.size - 22
        while (i >= 0 && !(bytes[i] == 0x50.toByte() && bytes[i + 1] == 0x4b.toByte() && bytes[i + 2] == 5.toByte() && bytes[i + 3] == 6.toByte())) i--
        val cdOff = (bytes[i + 16].toInt() and 0xFF) or ((bytes[i + 17].toInt() and 0xFF) shl 8) or ((bytes[i + 18].toInt() and 0xFF) shl 16)
        // second record = profile.json; skip first (manifest)
        var p = cdOff
        val n1 = (bytes[p + 28].toInt() and 0xFF) or ((bytes[p + 29].toInt() and 0xFF) shl 8)
        val e1 = (bytes[p + 30].toInt() and 0xFF) or ((bytes[p + 31].toInt() and 0xFF) shl 8)
        val c1 = (bytes[p + 32].toInt() and 0xFF) or ((bytes[p + 33].toInt() and 0xFF) shl 8)
        p += 46 + n1 + e1 + c1
        bytes[p + 5] = 3                                     // version made by: Unix
        bytes[p + 40] = 0xFF.toByte(); bytes[p + 41] = 0xA1.toByte()   // mode 0120777 (symlink)
        f.writeBytes(bytes)
        expect(BackupException.Reason.SYMLINK, f)
    }
}
