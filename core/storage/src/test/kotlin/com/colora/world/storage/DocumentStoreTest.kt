package com.colora.world.storage

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DocumentStoreTest {
    @get:Rule val tmp = TemporaryFolder()
    private var now = 1_000_000L
    private fun store() = DocumentStore(tmp.root) { now }
    private fun DocumentStore.withProfile(id: String = "p1") = also { saveProfile(ChildProfile(id, "avatar.fox", createdAt = now)) }

    @Test fun committedRevisionReadsBackAndIsImmutable() {
        val s = store().withProfile()
        val r1 = s.commit("p1", "art1", DocKind.ARTWORK, "v1".toByteArray(), "animals.cat", 1)
        val r2 = s.commit("p1", "art1", DocKind.ARTWORK, "v2".toByteArray())
        assertEquals(r1.revisionId, r2.parentRevisionId)
        assertEquals("v2", String(s.readCurrent("p1", "art1")!!.second))
        assertEquals("v1", String(s.readRevision("p1", "art1", r1.revisionId)!!.second))   // old revision still intact
        assertEquals(listOf(1L, 2L), s.revisions("p1", "art1").map { it.sequence })
    }

    @Test fun corruptCurrentRevisionFallsBackToNewestValid() {
        val s = store().withProfile()
        s.commit("p1", "a", DocKind.ARTWORK, "good".toByteArray())
        val bad = s.commit("p1", "a", DocKind.ARTWORK, "newer".toByteArray())
        File(s.docDir("p1", "a"), "rev/${bad.revisionId}.bin").writeText("bit rot")
        assertEquals("good", String(s.readCurrent("p1", "a")!!.second))
        val rep = s.recover()
        assertEquals(listOf(bad.revisionId), rep.invalidRevisions)
        assertEquals(listOf("a"), rep.pointersRepaired)
        assertEquals("good", String(s.readCurrent("p1", "a")!!.second))
    }

    @Test fun crashBeforePointerWriteIsRecoveredToNewestRevision() {
        val s = store().withProfile()
        s.commit("p1", "a", DocKind.SCENE, "one".toByteArray())
        val ptr = File(s.docDir("p1", "a"), "current.json").readBytes()
        s.commit("p1", "a", DocKind.SCENE, "two".toByteArray())
        File(s.docDir("p1", "a"), "current.json").writeBytes(ptr)          // simulate crash: pointer not yet moved
        File(s.docDir("p1", "a"), "rev/.tmp-x.bin-123").writeText("half") // orphan temp
        File(s.docDir("p1", "a"), "rev/rorphan.bin").writeText("no meta") // payload whose meta never landed
        val rep = s.recover()
        assertEquals(1, rep.tempFilesRemoved)
        assertEquals("two", String(s.readCurrent("p1", "a")!!.second))
        assertFalse(File(s.docDir("p1", "a"), "rev/rorphan.bin").exists())
    }

    @Test fun missingPointerIsRebuilt() {
        val s = store().withProfile()
        s.commit("p1", "a", DocKind.BOOK, "book".toByteArray())
        File(s.docDir("p1", "a"), "current.json").delete()
        assertNull(s.pointer("p1", "a"))
        s.recover()
        assertNotNull(s.pointer("p1", "a"))
        assertEquals(DocKind.BOOK, s.pointer("p1", "a")!!.kind)
    }

    @Test fun concurrentAutosaveAndManualSaveNeverLoseUpdates() {
        val s = store().withProfile()
        val pool = Executors.newFixedThreadPool(8)
        val go = CountDownLatch(1)
        repeat(200) { i -> pool.submit { go.await(); s.commit("p1", "doc", DocKind.ARTWORK, "v$i".toByteArray()) } }
        go.countDown(); pool.shutdown(); assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS))
        val revs = s.revisions("p1", "doc")
        assertEquals((1L..200L).toList(), revs.map { it.sequence })
        assertEquals(revs.last().revisionId, s.pointer("p1", "doc")!!.currentRevisionId)
        // parent chain is linear: every revision's parent is the previous one
        revs.zipWithNext().forEach { (a, b) -> assertEquals(a.revisionId, b.parentRevisionId) }
    }

    @Test fun garbageCollectionKeepsReferencedAndCurrent() {
        val s = store().withProfile()
        val revs = (1..10).map { s.commit("p1", "a", DocKind.ARTWORK, "v$it".toByteArray()) }
        val removed = s.collectGarbage("p1", "a", referenced = setOf(revs[1].revisionId), keepLatest = 3)
        assertEquals(6, removed)
        val left = s.revisions("p1", "a").map { it.revisionId }.toSet()
        assertEquals(setOf(revs[1], revs[7], revs[8], revs[9]).map { it.revisionId }.toSet(), left)
    }

    @Test fun trashRestoresWithinSevenDaysAndPurgesAfter() {
        val s = store().withProfile()
        s.commit("p1", "a", DocKind.ARTWORK, "x".toByteArray())
        s.commit("p1", "b", DocKind.ARTWORK, "y".toByteArray())
        s.moveToTrash("p1", "a"); s.moveToTrash("p1", "b")
        assertTrue(s.documents("p1").isEmpty())
        assertEquals(2, s.trash("p1").size)
        assertTrue(s.restoreFromTrash("p1", "a"))
        assertEquals("x", String(s.readCurrent("p1", "a")!!.second))
        now += 6L * 24 * 3600_000; assertEquals(0, s.purgeTrash())
        now += 2L * 24 * 3600_000; assertEquals(1, s.purgeTrash())
        assertTrue(s.trash("p1").isEmpty())
    }

    @Test fun favoriteSurvivesNewRevisions() {
        val s = store().withProfile()
        s.commit("p1", "a", DocKind.ARTWORK, "1".toByteArray())
        s.setFavorite("p1", "a", true)
        s.commit("p1", "a", DocKind.ARTWORK, "2".toByteArray())
        assertTrue(s.pointer("p1", "a")!!.favorite)
    }

    @Test(expected = IllegalArgumentException::class)
    fun idsCannotEscapeTheStore() { store().commit("p1", "../evil", DocKind.ARTWORK, ByteArray(0)) }

    @Test fun largePayloadRoundTrips() {
        val s = store().withProfile()
        val big = ByteArray(3_000_000) { (it * 31).toByte() }
        s.commit("p1", "big", DocKind.ARTWORK, big)
        assertArrayEquals(big, s.readCurrent("p1", "big")!!.second)
    }
}
