package com.colora.world.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import com.colora.world.engine.paint.PaintState
import com.colora.world.engine.paint.StickerPlacement
import com.colora.world.engine.paint.Stroke

/**
 * The one renderer for layered artwork (mirrors tools/art/render.py).
 * Per rig part, in order: region base fills → brush strokes (own layer, clipped
 * per region, eraser = DST_OUT) → stickers (clipped to the part) → region
 * outlines → part silhouette → fixed details. Toys pass a per-part matrix so
 * the child's exact strokes move with the part they were painted on.
 */
object ArtRenderer {
    const val INK = 0xFF24324B.toInt()
    const val BLUSH = 0x99FF9C93.toInt()
    const val PAPER = Color.WHITE
    const val STICKER_UNITS = 220f
    /** Bumped whenever output pixels change; part of toy/thumbnail cache keys. */
    const val VERSION = 3

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND; color = INK }
    private val layerPaint = Paint()

    class Options(
        val useSamples: Boolean = false,
        val drawPaint: Boolean = true,
        val drawOutlines: Boolean = true,
        val partMatrix: ((ArtModel.Part) -> Matrix?)? = null,
        /** Only draw these parts (toy texture baking). */
        val onlyPart: String? = null,
        val stickerArt: ((String) -> ArtModel?)? = null,
        val highlightRegion: String? = null,
    )

    fun draw(c: Canvas, art: ArtModel, state: PaintState?, o: Options = Options()) {
        val byPart = HashMap<String, MutableList<Stroke>>()
        state?.visibleStrokes?.forEach { s ->
            val pid = art.region(s.clipRegionId)?.partId ?: art.parts.firstOrNull()?.def?.id ?: return@forEach
            byPart.getOrPut(pid) { ArrayList() } += s
        }
        val stickersByPart = HashMap<String, MutableList<StickerPlacement>>()
        state?.stickers?.values?.forEach { st ->
            val pid = art.part(st.partId)?.def?.id ?: art.parts.last().def.id
            stickersByPart.getOrPut(pid) { ArrayList() } += st
        }
        for (part in art.parts) {
            if (o.onlyPart != null && o.onlyPart != part.def.id) continue
            val save = c.save()
            o.partMatrix?.invoke(part)?.let { c.concat(it) }
            if (o.drawPaint) {
                for (r in part.regions) {
                    fill.color = state?.fills?.get(r.id) ?: if (o.useSamples || art.preColored) r.sample else PAPER
                    c.drawPath(r.path, fill)
                }
                byPart[part.def.id]?.let { strokes ->
                    val b = part.bounds
                    val layer = c.saveLayer(b.left, b.top, b.right, b.bottom, layerPaint)
                    for (s in strokes) {
                        val clip = art.region(s.clipRegionId)?.path
                        val cs = c.save()
                        if (clip != null) c.clipPath(clip)
                        BrushPainter.draw(c, s)
                        c.restoreToCount(cs)
                    }
                    c.restoreToCount(layer)
                }
                stickersByPart[part.def.id]?.let { list ->
                    val cs = c.save()
                    if (!part.silhouette.isEmpty) c.clipPath(part.silhouette)
                    for (st in list) drawSticker(c, st, o.stickerArt)
                    c.restoreToCount(cs)
                }
            }
            if (o.drawOutlines) drawOutlines(c, art, part, o.highlightRegion)
            c.restoreToCount(save)
        }
    }

    fun drawOutlines(c: Canvas, art: ArtModel, part: ArtModel.Part, highlight: String? = null) {
        if (art.isFreePage) return
        line.strokeWidth = 9f; line.color = INK
        for (r in part.regions) c.drawPath(r.path, line)
        if (!part.silhouette.isEmpty) { line.strokeWidth = 13f; c.drawPath(part.silhouette, line) }
        for (d in part.details) when (d.kind) {
            "line" -> { line.strokeWidth = d.width; c.drawPath(d.path, line) }
            "ink" -> { fill.color = INK; c.drawPath(d.path, fill) }
            "white" -> { fill.color = PAPER; c.drawPath(d.path, fill) }
            "blush" -> { fill.color = BLUSH; c.drawPath(d.path, fill) }
        }
        if (highlight != null) part.regions.firstOrNull { it.id == highlight }?.let {
            line.color = 0xFF34548D.toInt(); line.strokeWidth = 7f
            line.pathEffect = android.graphics.DashPathEffect(floatArrayOf(18f, 14f), 0f)
            c.drawPath(it.path, line)
            line.pathEffect = null; line.color = INK
        }
    }

    fun drawSticker(c: Canvas, st: StickerPlacement, lookup: ((String) -> ArtModel?)?) {
        val art = lookup?.invoke(st.stickerId) ?: return
        val s = c.save()
        c.translate(st.x, st.y)
        c.rotate(st.rotationDeg)
        val k = STICKER_UNITS * st.scale / 1024f
        c.scale(k, k)
        c.translate(-512f, -512f)
        draw(c, art, null, Options(useSamples = true))
        c.restoreToCount(s)
    }

    /** Draws an artwork fitted into a w×h box (letterboxed, centred on its content). */
    fun drawFitted(c: Canvas, art: ArtModel, state: PaintState?, w: Float, h: Float, pad: Float = 0.06f, o: Options = Options()) {
        val cb = art.contentRect
        val size = maxOf(cb.width(), cb.height()) * (1 + pad * 2)
        val k = minOf(w, h) / size
        val s = c.save()
        c.translate(w / 2f, h / 2f)
        c.scale(k, k)
        c.translate(-cb.centerX(), -cb.centerY())
        draw(c, art, state, o)
        c.restoreToCount(s)
    }
}
