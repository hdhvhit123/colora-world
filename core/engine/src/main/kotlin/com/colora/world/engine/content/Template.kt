package com.colora.world.engine.content

import com.colora.world.engine.geometry.Bounds
import com.colora.world.engine.geometry.PathData
import com.colora.world.engine.geometry.PathOp
import com.colora.world.engine.geometry.Polygon
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val ContentJson = Json { ignoreUnknownKeys = true; explicitNulls = false }

@Serializable
data class DesignBounds(val width: Int, val height: Int)

@Serializable
data class Access(val tier: String, val packId: String)

@Serializable
data class PartDef(
    val id: String,
    val parent: String? = null,
    val pivot: List<Float>,
    val motion: String? = null,
    val amp: Float = 0f,
    val silhouette: String,
)

@Serializable
data class RegionDef(
    val id: String,
    val part: String,
    val order: Int,
    val d: String,
    val bounds: List<Float>,
    val sample: String,
)

@Serializable
data class DetailDef(val part: String, val kind: String, val w: Float = 7f, val d: String)

@Serializable
data class AnchorDef(val id: String, val type: String, val x: Float, val y: Float)

/** Contract of `assets/content/templates/<id>.json` (see docs/CONTENT_PIPELINE.md). */
@Serializable
data class TemplateDef(
    val schemaVersion: Int,
    val id: String,
    val contentVersion: Int,
    val topicId: String,
    val labelKey: String,
    val difficulty: String,
    val access: Access,
    val interactionPreset: String,
    val capabilities: List<String>,
    val audioSetId: String,
    val compatibleWorlds: List<String>,
    val designBounds: DesignBounds,
    val contentBounds: List<Float>,
    val parts: List<PartDef>,
    val regions: List<RegionDef>,
    val details: List<DetailDef>,
    val anchors: List<AnchorDef> = emptyList(),
) {
    companion object {
        fun parse(json: String): TemplateDef = ContentJson.decodeFromString(serializer(), json)
    }
}

/** Precomputed geometry for hit-testing and clipping, built once per template load. */
class TemplateGeometry(val def: TemplateDef) {
    class RegionGeo(val def: RegionDef, val partIndex: Int, val ops: List<PathOp>, val contours: List<FloatArray>, val bounds: Bounds) {
        val area: Float by lazy { Polygon.area(contours) }
    }

    val partOrder: Map<String, Int> = def.parts.withIndex().associate { it.value.id to it.index }
    val regions: List<RegionGeo> = def.regions.map { r ->
        val ops = PathData.parse(r.d)
        val contours = PathData.flatten(ops)
        RegionGeo(r, partOrder[r.part] ?: error("region ${r.id} has unknown part ${r.part}"), ops, contours, PathData.bounds(contours))
    }
    private val byId = regions.associateBy { it.def.id }

    /** Topmost first: later parts draw above earlier parts; within a part, later regions above earlier. */
    val hitOrder: List<RegionGeo> = regions.sortedWith(compareByDescending<RegionGeo> { it.partIndex }.thenByDescending { it.def.order })

    fun region(id: String): RegionGeo? = byId[id]

    /**
     * Region under (x, y) in template units. If no region contains the point,
     * the nearest region edge within [tolerance] wins (helps small fingers on
     * thin parts) – but never a region farther than the tolerance.
     */
    fun hitTest(x: Float, y: Float, tolerance: Float = 0f): String? {
        for (r in hitOrder) {
            if (!r.bounds.contains(x, y)) continue
            if (Polygon.contains(r.contours, x, y)) return r.def.id
        }
        if (tolerance <= 0f) return null
        var best: RegionGeo? = null
        var bestD = tolerance
        for (r in hitOrder) {
            if (!r.bounds.contains(x, y, tolerance)) continue
            val d = Polygon.distanceToEdge(r.contours, x, y)
            if (d < bestD) { bestD = d; best = r }
        }
        return best?.def?.id
    }
}
