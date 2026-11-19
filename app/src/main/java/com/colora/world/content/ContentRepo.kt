package com.colora.world.content

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import com.colora.world.engine.content.ArtDef
import com.colora.world.engine.content.Catalog
import com.colora.world.engine.content.CatalogEntry
import com.colora.world.engine.content.CharacterPack
import com.colora.world.engine.content.ContentIndex
import com.colora.world.engine.content.ContentJson
import com.colora.world.engine.content.Extras
import com.colora.world.engine.content.PalettePack
import com.colora.world.engine.content.PromptPack
import com.colora.world.engine.content.StoryPack
import com.colora.world.engine.content.TemplateDef
import com.colora.world.engine.content.WorldDef
import com.colora.world.engine.world.ToyAnchor
import com.colora.world.engine.world.ToyCatalog
import com.colora.world.render.ArtModel
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * Read-only access to the bundled content pack (assets/content). Parsed models
 * and Android paths are cached; nothing is parsed per frame.
 */
class ContentRepo(private val ctx: Context) {
    private fun text(path: String): String = ctx.assets.open("content/$path").bufferedReader().use { it.readText() }

    val catalog: Catalog by lazy { ContentJson.decodeFromString(Catalog.serializer(), text("catalog.json")) }
    val extras: Extras by lazy { ContentJson.decodeFromString(Extras.serializer(), text("extras.json")) }
    val characters: CharacterPack by lazy { ContentJson.decodeFromString(CharacterPack.serializer(), text("characters.json")) }
    val stories: StoryPack by lazy { ContentJson.decodeFromString(StoryPack.serializer(), text("stories.json")) }
    val prompts: PromptPack by lazy { ContentJson.decodeFromString(PromptPack.serializer(), text("prompts.json")) }
    val palettes: PalettePack by lazy { ContentJson.decodeFromString(PalettePack.serializer(), text("palettes.json")) }

    private val locales = ConcurrentHashMap<String, Map<String, String>>()
    fun locale(lang: String): Map<String, String> = locales.getOrPut(lang) {
        runCatching {
            val obj = ContentJson.parseToJsonElement(text("locales/$lang.json")) as JsonObject
            obj.mapValues { it.value.jsonPrimitive.content }
        }.getOrElse { emptyMap() }
    }

    /** Content text in [lang] with English fallback, never a raw key for known content. */
    fun label(key: String, lang: String): String = locale(lang)[key] ?: locale("en")[key] ?: key.substringAfterLast('.').replace('_', ' ')

    val templatesById: Map<String, CatalogEntry> by lazy { catalog.templates.associateBy { it.id } }
    fun template(id: String): CatalogEntry? = templatesById[id]
    fun topicOf(templateId: String): String? = templatesById[templateId]?.topicId

    // ------------------------------------------------------------------ art
    private val arts = ConcurrentHashMap<String, ArtModel>()

    fun artPath(id: String): String = when {
        id == FREE_PAGE_ID -> ""
        id.startsWith("prop.") -> "props/$id.json"
        id.startsWith("sticker.") -> "stickers/$id.json"
        id.startsWith("char.") -> "characters/$id.json"
        id.startsWith("milo.") -> "mascot/$id.json"
        else -> "templates/$id.json"
    }

    /** Loads any layered artwork (template, prop, sticker, character part, mascot). Null if missing/corrupt. */
    fun art(id: String): ArtModel? {
        arts[id]?.let { return it }
        val model = runCatching {
            if (id == FREE_PAGE_ID) ArtModel.freePage()
            else {
                val raw = text(artPath(id))
                val def: TemplateDef = if (id.contains('.') && templatesById.containsKey(id)) TemplateDef.parse(raw) else ArtDef.parse(raw).asTemplate()
                val preColored = runCatching { ArtDef.parse(raw).preColored }.getOrDefault(false)
                ArtModel(def, preColored)
            }
        }.onFailure { Log.w("Colora", "missing or invalid art $id", it) }.getOrNull() ?: return null
        arts[id] = model
        return model
    }

    private val worlds = ConcurrentHashMap<String, WorldDef>()
    fun world(id: String): WorldDef? = worlds[id] ?: runCatching { WorldDef.parse(text("worlds/$id.json")) }.onFailure { Log.e("Colora", "world $id invalid", it) }.getOrNull()?.also { worlds[id] = it }

    // ----------------------------------------------------------- thumbnails
    private val thumbs = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }

    fun thumb(path: String): Bitmap? {
        thumbs.get(path)?.let { return it }
        val bmp = runCatching { ctx.assets.open("content/$path").use { BitmapFactory.decodeStream(it) } }.getOrNull() ?: return null
        thumbs.put(path, bmp)
        return bmp
    }

    fun illustration(path: String): Bitmap? = thumb(path)

    // ------------------------------------------------------- capabilities
    fun capabilities(thingId: String): Set<String> = when {
        thingId.startsWith("prop.") -> extras.props.firstOrNull { it.id == thingId }?.capabilities?.toSet() ?: emptySet()
        thingId.startsWith("char.body.") -> setOf("passenger", "seatable", "carryable")
        thingId == FREE_PAGE_ID -> setOf("decor")
        else -> templatesById[thingId]?.capabilities?.toSet() ?: emptySet()
    }

    /** Anchors of a placed thing, in toy-local scene units (see [ToyGeometry]). */
    val toyCatalog = object : ToyCatalog {
        override fun capabilities(templateId: String) = this@ContentRepo.capabilities(templateId)
        override fun anchors(templateId: String): List<ToyAnchor> {
            val art = art(templateId) ?: return emptyList()
            return art.def.anchors.map { a ->
                val (dx, dy) = ToyGeometry.anchorOffset(art, a.x, a.y)
                ToyAnchor(a.id, a.type, dx, dy)
            }
        }
    }

    /** Verifies sizes and SHA-256 of every indexed content file. Returns the paths that failed. */
    fun verifyIndex(): List<String> {
        val idx = runCatching { ContentJson.decodeFromString(ContentIndex.serializer(), text("index.json")) }.getOrElse { return listOf("index.json") }
        val bad = ArrayList<String>()
        val buf = ByteArray(64 * 1024)
        for (e in idx.files) {
            try {
                val md = MessageDigest.getInstance("SHA-256")
                var n = 0L
                ctx.assets.open("content/${e.path}").use { inp -> while (true) { val r = inp.read(buf); if (r < 0) break; md.update(buf, 0, r); n += r } }
                val hex = md.digest().joinToString("") { "%02x".format(it) }
                if (n != e.size || hex != e.sha256) bad += e.path
            } catch (_: Exception) { bad += e.path }
        }
        return bad
    }

    companion object { const val FREE_PAGE_ID = "free.page" }
}

/**
 * Shared mapping between toy design space (1024 units) and normalised scene
 * space. A toy's (x, y) is its *foot point*: bottom-centre of its content box.
 */
object ToyGeometry {
    /** Height of a 1024-unit toy at scale 1, as a fraction of the world's height. */
    const val TOY_HEIGHT = 0.30f
    /** World aspect (2048 × 1024). */
    const val WORLD_ASPECT = 2f

    fun footY(art: ArtModel) = art.def.contentBounds.getOrElse(3) { 1000f }

    fun anchorOffset(art: ArtModel, ax: Float, ay: Float): Pair<Float, Float> {
        val k = TOY_HEIGHT / 1024f
        return Pair((ax - 512f) * k / WORLD_ASPECT, (ay - footY(art)) * k)
    }
}
