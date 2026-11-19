package com.colora.world.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.colora.world.AppGraph
import com.colora.world.content.ToyGeometry
import com.colora.world.engine.world.SceneDocument
import com.colora.world.engine.world.ToyInstance

/**
 * Deterministic still or animated frame of a scene from its immutable snapshot
 * (used by book pages, PNG/PDF export and MP4 frames). Transforms, colours,
 * z-order and attachments are exactly those saved in the scene document.
 */
class SceneSnapshot(private val g: AppGraph, private val pid: String, val scene: SceneDocument) {
    val painter = g.content.world(scene.worldId)?.let(::WorldPainter)
    private val visuals: Map<String, ToyVisual> = scene.objects.mapNotNull { o -> ToyVisual.resolve(g, pid, o.artworkRevisionId, o.templateId)?.let { o.id to it } }.toMap()

    private fun pos(o: ToyInstance, depth: Int = 0): Pair<Float, Float> {
        val att = o.attachment ?: return o.transform.x to o.transform.y
        val p = scene.obj(att.parentId) ?: return o.transform.x to o.transform.y
        val a = g.content.toyCatalog.anchors(p.templateId).firstOrNull { it.id == att.anchorId } ?: return o.transform.x to o.transform.y
        if (depth > 6) return o.transform.x to o.transform.y
        val (px, py) = pos(p, depth + 1)
        return px + (if (p.transform.flipX) -a.dx else a.dx) * p.transform.scale to py + a.dy * p.transform.scale
    }

    /** Draws the whole 2048×1024 scene into [c] (caller sets the scale). [tSec] < 0 = still. */
    fun draw(c: Canvas, tSec: Float = -1f) {
        val p = painter ?: return
        p.draw(c, 0f, scene.environment.timeOfDay == "night", parallax = false)
        val W = p.width; val H = p.height
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x2624324B }
        for (o in scene.objects.sortedBy { it.zOrder }) {
            val v = visuals[o.id] ?: continue
            val (x, y) = pos(o)
            val s = ToyGeometry.TOY_HEIGHT * H / 1024f * o.transform.scale
            val cb = v.art.def.contentBounds
            if (o.attachment == null && "sky" !in v.caps) {
                val wHalf = (cb[2] - cb[0]) * s * 0.42f
                c.drawOval(RectF(x * W - wHalf, y * H - 10f, x * W + wHalf, y * H + 12f), shadow)
            }
            val sv = c.save()
            c.translate(x * W, y * H); c.scale(if (o.transform.flipX) -s else s, s); c.translate(-512f, -v.footY)
            v.draw(c, maxOf(tSec, 0f), if (tSec < 0f) 0f else 0.8f, tSec >= 0f && "rideable" in v.caps, o.id.hashCode())
            c.restoreToCount(sv)
        }
    }

    fun bitmap(widthPx: Int): Bitmap {
        val p = painter
        val h = if (p == null) widthPx / 2 else (widthPx * p.height / p.width).toInt()
        val b = Bitmap.createBitmap(widthPx, h, Bitmap.Config.ARGB_8888)
        if (p != null) { val c = Canvas(b); c.scale(widthPx / p.width, widthPx / p.width); draw(c) }
        return b
    }
}
