package com.colora.world.data

import com.colora.world.content.ContentRepo
import com.colora.world.engine.character.CharacterRecipe
import com.colora.world.engine.content.ContentJson
import com.colora.world.engine.paint.PaintCommand
import com.colora.world.engine.story.StoryProgress
import com.colora.world.engine.world.SceneDocument
import com.colora.world.storage.BackupService
import com.colora.world.storage.ChildProfile
import com.colora.world.storage.DocKind
import com.colora.world.storage.DocPointer
import com.colora.world.storage.DocumentStore
import com.colora.world.storage.RecoveryReport
import com.colora.world.storage.RevisionMeta
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import java.io.File
import java.util.UUID

@Serializable
data class ArtworkDoc(
    val schemaVersion: Int = 1,
    val templateId: String,
    val templateVersion: Int = 1,
    val commands: List<PaintCommand> = emptyList(),
    /** Revision this copy started from (duplicate / "colour again"). */
    val copiedFrom: String? = null,
)

@Serializable
data class CharacterDoc(val schemaVersion: Int = 1, val recipe: CharacterRecipe)

@Serializable
data class BookItem(val ref: String, val x: Float = 0.5f, val y: Float = 0.5f, val scale: Float = 1f)

@Serializable
data class BookPage(val layout: String = "one", val items: List<BookItem> = emptyList(), val captionKey: String? = null, val stickers: List<String> = emptyList())

@Serializable
data class BookDoc(val schemaVersion: Int = 1, val titleKey: String = "book.title.my_book", val customTitle: String? = null, val coverColor: Int = 0xFFFF786D.toInt(), val pages: List<BookPage> = emptyList())

@Serializable
data class LabDoc(val schemaVersion: Int = 1, val swatches: List<Int> = emptyList())

@Serializable
data class RewardsDoc(val schemaVersion: Int = 1, val stickers: List<String> = emptyList(), val bookPages: List<String> = emptyList())

/** Summary row for galleries. */
data class DocSummary(val pointer: DocPointer, val sourceId: String?) {
    val id get() = pointer.documentId
    val revisionId get() = pointer.currentRevisionId
}

/**
 * Typed façade over [DocumentStore]. Every write is an immutable revision; the
 * pointer moves last (ADR-006). All calls block: use Dispatchers.IO.
 */
class Library(filesDir: File, private val content: ContentRepo) {
    /** noBackup-style location: never part of Auto Backup (see data_extraction_rules.xml). */
    val root = File(filesDir, "library")
    val store = DocumentStore(root)
    val backup = BackupService(store, contentVersion = 1, appVersion = "1.0.0")

    fun recover(): RecoveryReport = store.recover()

    // --------------------------------------------------------------- profiles
    fun profiles(): List<ChildProfile> = store.profiles()

    fun createProfile(avatarId: String, locale: String, alias: String? = null): ChildProfile {
        val p = ChildProfile(id = "p" + uid(), avatarId = avatarId, localAlias = alias, locale = locale, createdAt = System.currentTimeMillis())
        store.saveProfile(p); return p
    }

    fun updateProfile(p: ChildProfile) = store.saveProfile(p)
    fun deleteProfile(pid: String) = store.deleteProfile(pid)

    // ------------------------------------------------------------- documents
    private fun <T> encode(s: KSerializer<T>, v: T) = ContentJson.encodeToString(s, v).toByteArray()
    private fun <T> decode(s: KSerializer<T>, b: ByteArray): T = ContentJson.decodeFromString(s, String(b))

    fun list(pid: String, kind: DocKind): List<DocSummary> = store.documents(pid, kind)
        .map { p -> DocSummary(p, runCatching { store.revisions(pid, p.documentId).lastOrNull { it.revisionId == p.currentRevisionId }?.sourceId }.getOrNull()) }
        .sortedByDescending { it.pointer.updatedAt }

    fun <T> read(pid: String, docId: String, s: KSerializer<T>): Pair<RevisionMeta, T>? =
        store.readCurrent(pid, docId)?.let { (m, b) -> runCatching { m to decode(s, b) }.getOrNull() }

    fun <T> readRevision(pid: String, docId: String, revId: String, s: KSerializer<T>): T? =
        store.readRevision(pid, docId, revId)?.let { (_, b) -> runCatching { decode(s, b) }.getOrNull() }

    fun <T> write(pid: String, docId: String, kind: DocKind, s: KSerializer<T>, v: T, sourceId: String? = null): RevisionMeta =
        store.commit(pid, docId, kind, encode(s, v), sourceId = sourceId, sourceVersion = 1)

    fun saveArtwork(pid: String, docId: String, doc: ArtworkDoc) = write(pid, docId, DocKind.ARTWORK, ArtworkDoc.serializer(), doc, doc.templateId)
    fun loadArtwork(pid: String, docId: String) = read(pid, docId, ArtworkDoc.serializer())
    fun artworkRevision(pid: String, docId: String, revId: String) = readRevision(pid, docId, revId, ArtworkDoc.serializer())

    fun saveScene(pid: String, doc: SceneDocument) = write(pid, doc.id, DocKind.SCENE, SceneDocument.serializer(), doc, doc.worldId)
    fun loadScene(pid: String, docId: String) = read(pid, docId, SceneDocument.serializer())?.second

    fun saveCharacter(pid: String, docId: String, d: CharacterDoc) = write(pid, docId, DocKind.CHARACTER, CharacterDoc.serializer(), d, d.recipe.bodyId)
    fun loadCharacter(pid: String, docId: String) = read(pid, docId, CharacterDoc.serializer())
    fun characterRevision(pid: String, docId: String, revId: String) = readRevision(pid, docId, revId, CharacterDoc.serializer())

    fun saveBook(pid: String, docId: String, d: BookDoc) = write(pid, docId, DocKind.BOOK, BookDoc.serializer(), d)
    fun loadBook(pid: String, docId: String) = read(pid, docId, BookDoc.serializer())?.second

    fun loadLab(pid: String) = read(pid, LAB_ID, LabDoc.serializer())?.second ?: LabDoc()
    fun saveLab(pid: String, d: LabDoc) = write(pid, LAB_ID, DocKind.LAB, LabDoc.serializer(), d.copy(swatches = d.swatches.takeLast(24)))

    fun loadRewards(pid: String) = read(pid, REWARDS_ID, RewardsDoc.serializer())?.second ?: RewardsDoc()
    fun saveRewards(pid: String, d: RewardsDoc) = write(pid, REWARDS_ID, DocKind.STORY_PROGRESS, RewardsDoc.serializer(), d)

    fun storyProgress(pid: String, storyId: String) = read(pid, storyDoc(storyId), StoryProgress.serializer())?.second
    fun saveStoryProgress(pid: String, p: StoryProgress) = write(pid, storyDoc(p.storyId), DocKind.STORY_PROGRESS, StoryProgress.serializer(), p, p.storyId)
    private fun storyDoc(storyId: String) = "story-" + storyId.replace(Regex("[^A-Za-z0-9_-]"), "_")

    fun trash(pid: String, docId: String) = store.moveToTrash(pid, docId)
    fun restore(pid: String, docId: String) = store.restoreFromTrash(pid, docId)
    fun trashList(pid: String) = store.trash(pid)
    fun deleteForever(pid: String, docId: String) = store.deleteForever(pid, docId)
    fun setFavorite(pid: String, docId: String, fav: Boolean) = store.setFavorite(pid, docId, fav)

    /** Revisions referenced by scenes and books are never garbage-collected. */
    fun referencedRevisions(pid: String): Set<String> {
        val out = HashSet<String>()
        for (s in list(pid, DocKind.SCENE)) loadScene(pid, s.id)?.objects?.forEach { o -> ToyRef.parse(o.artworkRevisionId)?.revId?.let(out::add) }
        for (b in list(pid, DocKind.BOOK)) loadBook(pid, b.id)?.pages?.forEach { pg -> pg.items.forEach { i -> ToyRef.parse(i.ref)?.revId?.let(out::add) } }
        return out
    }

    fun collectGarbage(pid: String): Int {
        val refs = referencedRevisions(pid)
        return store.documents(pid).sumOf { store.collectGarbage(pid, it.documentId, refs, keepLatest = 8) }
    }

    fun storageBytes(): Long = root.walkTopDown().filter { it.isFile }.sumOf { it.length() }

    companion object {
        const val LAB_ID = "lab"
        const val REWARDS_ID = "rewards"
        fun uid(): String = UUID.randomUUID().toString().replace("-", "").take(20)
        fun newDocId(prefix: String) = prefix + uid()
    }
}

/**
 * Reference from a scene/book to immutable content:
 * `art:<docId>:<revId>`, `chr:<docId>:<revId>`, `scene:<docId>:<revId>` or `prop:<propId>`.
 */
data class ToyRef(val kind: String, val docId: String, val revId: String?) {
    override fun toString() = if (revId == null) "$kind:$docId" else "$kind:$docId:$revId"
    companion object {
        fun parse(s: String): ToyRef? {
            val p = s.split(':')
            return when {
                p.size == 3 -> ToyRef(p[0], p[1], p[2])
                p.size == 2 -> ToyRef(p[0], p[1], null)
                else -> null
            }
        }
        fun art(docId: String, revId: String) = ToyRef("art", docId, revId)
        fun character(docId: String, revId: String) = ToyRef("chr", docId, revId)
        fun prop(id: String) = ToyRef("prop", id, null)
    }
}
