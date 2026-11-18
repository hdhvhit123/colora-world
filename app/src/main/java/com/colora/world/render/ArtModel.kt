package com.colora.world.render

import android.graphics.Path
import android.graphics.RectF
import com.colora.world.engine.content.Access
import com.colora.world.engine.content.DesignBounds
import com.colora.world.engine.content.PartDef
import com.colora.world.engine.content.RegionDef
import com.colora.world.engine.content.TemplateDef
import com.colora.world.engine.content.TemplateGeometry
import com.colora.world.engine.geometry.PathData
import com.colora.world.engine.geometry.PathOp

/** Converts pipeline path data (absolute M/L/Q/C/Z) to an Android [Path] once. */
fun androidPath(d: String): Path = androidPath(PathData.parse(d))

fun androidPath(ops: List<PathOp>): Path {
    val p = Path()
    p.fillType = Path.FillType.EVEN_ODD
    for (op in ops) when (op) {
        is PathOp.MoveTo -> p.moveTo(op.x, op.y)
        is PathOp.LineTo -> p.lineTo(op.x, op.y)
        is PathOp.QuadTo -> p.quadTo(op.x1, op.y1, op.x, op.y)
        is PathOp.CubicTo -> p.cubicTo(op.x1, op.y1, op.x2, op.y2, op.x, op.y)
        PathOp.Close -> p.close()
    }
    return p
}

/**
 * A parsed artwork with precompiled Android paths. Shared by studio, toy,
 * world, export and thumbnail rendering so every surface draws identically.
 */
class ArtModel(val def: TemplateDef, val preColored: Boolean = false) {
    val geometry = TemplateGeometry(def)

    class Region(val id: String, val partId: String, val path: Path, val sample: Int)
    class Detail(val kind: String, val width: Float, val path: Path)
    class Part(val def: PartDef, val index: Int, val silhouette: Path, val regions: List<Region>, val details: List<Detail>) {
        val bounds: RectF = RectF().also { silhouette.computeBounds(it, true); it.inset(-14f, -14f) }
    }

    val parts: List<Part> = def.parts.mapIndexed { i, pd ->
        Part(
            def = pd, index = i,
            silhouette = if (pd.silhouette.isBlank()) Path() else androidPath(pd.silhouette),
            regions = def.regions.filter { it.part == pd.id }.sortedBy { it.order }.map { r ->
                Region(r.id, pd.id, geometry.region(r.id)!!.ops.let(::androidPath), parseColor(r.sample))
            },
            details = def.details.filter { it.part == pd.id }.map { Detail(it.kind, it.w, androidPath(it.d)) },
        )
    }
    private val regionById = parts.flatMap { it.regions }.associateBy { it.id }
    private val partById = parts.associateBy { it.def.id }
    fun region(id: String?): Region? = id?.let { regionById[it] }
    fun part(id: String?): Part? = id?.let { partById[it] }
    val isFreePage get() = def.id == com.colora.world.content.ContentRepo.FREE_PAGE_ID

    /** Region clip minus the silhouettes of parts drawn above it (for live strokes). */
    private val visibleClips = HashMap<String, Path>()
    fun visibleClip(regionId: String): Path? {
        val r = regionById[regionId] ?: return null
        return visibleClips.getOrPut(regionId) {
            val out = Path(r.path)
            val idx = partById[r.partId]!!.index
            for (p in parts) if (p.index > idx) out.op(p.silhouette, Path.Op.DIFFERENCE)
            out
        }
    }

    val contentRect: RectF get() = def.contentBounds.let { RectF(it[0], it[1], it[2], it[3]) }

    companion object {
        fun parseColor(hex: String): Int = (0xFF000000.toInt() or hex.removePrefix("#").toLong(16).toInt())

        /** A blank sheet for free drawing: one page region, no outline, no rig. */
        fun freePage(): ArtModel {
            val d = "M24 24L1000 24L1000 1000L24 1000Z"
            val def = TemplateDef(
                schemaVersion = 1, id = com.colora.world.content.ContentRepo.FREE_PAGE_ID, contentVersion = 1, topicId = "free",
                labelKey = "ui.free_page", difficulty = "simple", access = Access("base", "base"), interactionPreset = "none",
                capabilities = listOf("decor"), audioSetId = "none", compatibleWorlds = emptyList(), designBounds = DesignBounds(1024, 1024),
                contentBounds = listOf(24f, 24f, 1000f, 1000f), parts = listOf(PartDef("page", null, listOf(512f, 512f), "bob", 4f, "")),
                regions = listOf(RegionDef("page", "page", 0, d, listOf(24f, 24f, 1000f, 1000f), "#FFFFFF")), details = emptyList(),
            )
            return ArtModel(def)
        }
    }
}
