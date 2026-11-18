package com.colora.world.engine.content

import com.colora.world.engine.prompts.CreativePrompt
import com.colora.world.engine.story.StoryDef
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Generic layered artwork: props, stickers, character parts, mascot (same renderer as templates). */
@Serializable
data class ArtDef(
    val id: String,
    val kind: String = "template",
    val designBounds: DesignBounds,
    val contentBounds: List<Float>,
    val parts: List<PartDef>,
    val regions: List<RegionDef>,
    val details: List<DetailDef>,
    val anchors: List<AnchorDef> = emptyList(),
    val capabilities: List<String> = emptyList(),
    val labelKey: String? = null,
    val preColored: Boolean = false,
    val group: String? = null,
    val partKind: String? = null,
    val sockets: JsonElement? = null,
    val preset: String? = null,
) {
    /** Lets the template geometry/hit-test code work on any artwork. */
    fun asTemplate(): TemplateDef = TemplateDef(
        schemaVersion = 1, id = id, contentVersion = 1, topicId = kind, labelKey = labelKey ?: id, difficulty = "simple",
        access = Access("base", "base"), interactionPreset = preset ?: "none", capabilities = capabilities, audioSetId = "none",
        compatibleWorlds = emptyList(), designBounds = designBounds, contentBounds = contentBounds,
        parts = parts, regions = regions, details = details, anchors = anchors,
    )

    companion object { fun parse(json: String): ArtDef = ContentJson.decodeFromString(serializer(), json) }
}

@Serializable
data class CatalogEntry(
    val id: String, val topicId: String, val index: Int, val tier: String, val packId: String, val difficulty: String,
    val regions: Int, val preset: String, val capabilities: List<String>, val file: String, val thumb: String,
)

@Serializable
data class TopicEntry(val id: String, val labelKey: String, val templates: List<String>, val coverTemplate: String, val pack: String)

@Serializable
data class Catalog(val schemaVersion: Int, val contentVersion: Int, val topics: List<TopicEntry>, val templates: List<CatalogEntry>)

@Serializable
data class PropEntry(val id: String, val file: String, val thumb: String, val capabilities: List<String>, val anchors: List<AnchorDef>, val labelKey: String)

@Serializable
data class StickerEntry(val id: String, val group: String, val file: String, val thumb: String)

@Serializable
data class SimpleEntry(val id: String, val file: String, val thumb: String)

@Serializable
data class WorldEntry(val id: String, val file: String, val thumb: String, val labelKey: String, val anchors: Int, val layers: List<String>, val tier: String)

@Serializable
data class Extras(val schemaVersion: Int, val props: List<PropEntry>, val stickers: List<StickerEntry>, val mascot: List<SimpleEntry>, val worlds: List<WorldEntry>)

@Serializable
data class WorldShape(val d: String, val fill: String, val outline: Float)

@Serializable
data class WorldLayer(val id: String, val parallax: Float, val shapes: List<WorldShape>)

@Serializable
data class WorldZone(val id: String, val type: String, val d: String, val bounds: List<Float>)

@Serializable
data class WorldDef(
    val schemaVersion: Int, val id: String, val contentVersion: Int, val labelKey: String, val designSize: List<Int>,
    val sky: List<String>, val groundY: Float, val layers: List<WorldLayer>, val zones: List<WorldZone>, val anchors: List<AnchorDef>,
) { companion object { fun parse(json: String): WorldDef = ContentJson.decodeFromString(serializer(), json) } }

@Serializable
data class StoryPack(val schemaVersion: Int, val stories: List<StoryDef>)

@Serializable
data class PromptEntry(
    val id: String, val textKey: String, val suggestedTemplates: List<String> = emptyList(), val altTextKey: String? = null,
    val altTemplates: List<String> = emptyList(), val paletteId: String? = null, val destination: String = "studio", val illustration: String,
) {
    fun toPrompt() = CreativePrompt(id, textKey, suggestedTemplates, altTextKey, altTemplates, paletteId, destination)
}

@Serializable
data class PromptPack(val schemaVersion: Int, val prompts: List<PromptEntry>)

@Serializable
data class Palette(val id: String, val labelKey: String, val colors: List<String>)

@Serializable
data class PalettePack(val schemaVersion: Int, val palettes: List<Palette>, val extended: List<String>)

@Serializable
data class CharBody(val id: String, val file: String, val thumb: String, val preset: String, val sockets: Map<String, SocketDef>, val regions: Int)

@Serializable
data class SocketDef(val x: Float, val y: Float, val scale: Float, val z: String, val rot: Float = 0f, val mirror: Boolean = false)

@Serializable
data class CharPart(val id: String, val kind: String, val sockets: List<String>, val file: String, val thumb: String, val minScale: Float, val maxScale: Float, val regions: Int)

@Serializable
data class CharacterPack(val schemaVersion: Int, val stageScale: Float, val bodies: List<CharBody>, val parts: List<CharPart>, val scaleLimits: Map<String, Float> = emptyMap())

@Serializable
data class IndexEntry(val path: String, val size: Long, val sha256: String)

@Serializable
data class ContentIndex(val schemaVersion: Int, val contentVersion: Int, val files: List<IndexEntry>)
