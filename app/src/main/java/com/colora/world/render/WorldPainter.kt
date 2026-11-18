package com.colora.world.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import com.colora.world.engine.content.WorldDef

/**
 * Draws a world's sky + far/mid/near layers in world design units (2048×1024).
 * Paths are compiled once. [camX] is the camera's left edge in world units and
 * drives gentle parallax (disabled with reduced motion).
 */
class WorldPainter(val def: WorldDef) {
    private class Shape(val path: Path, val fill: Int, val outline: Float)
    private class Layer(val parallax: Float, val shapes: List<Shape>)
    private val layers = def.layers.map { l -> Layer(l.parallax, l.shapes.map { Shape(androidPath(it.d), ArtModel.parseColor(it.fill), it.outline) }) }
    val width = def.designSize.getOrElse(0) { 2048 }.toFloat()
    val height = def.designSize.getOrElse(1) { 1024 }.toFloat()
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = ArtRenderer.INK; strokeJoin = Paint.Join.ROUND; strokeCap = Paint.Cap.ROUND }
    private val skyTop = ArtModel.parseColor(def.sky.getOrElse(0) { "#BFE7FA" })
    private val skyBottom = ArtModel.parseColor(def.sky.getOrElse(1) { "#F4FBFF" })

    fun draw(c: Canvas, camX: Float, night: Boolean, parallax: Boolean = true, extraTop: Float = 0f, extraBottom: Float = 0f) {
        val sky = Paint().apply { shader = LinearGradient(0f, -extraTop, 0f, height * def.groundY, if (night) 0xFF2B3A67.toInt() else skyTop, if (night) 0xFF5B6AA0.toInt() else skyBottom, Shader.TileMode.CLAMP) }
        c.drawRect(-width, -extraTop - 10f, width * 2, height * def.groundY + 40f, sky)
        c.drawRect(-width, height * 0.9f, width * 2, height + extraBottom + 10f, Paint().apply { color = groundColor })
        for (l in layers) {
            val s = c.save()
            if (parallax) c.translate(camX * (1f - l.parallax), 0f)
            for (sh in l.shapes) {
                fill.color = sh.fill
                c.drawPath(sh.path, fill)
                if (sh.outline > 0f) { line.strokeWidth = sh.outline; c.drawPath(sh.path, line) }
            }
            c.restoreToCount(s)
        }
        if (night) c.drawRect(-width, -extraTop - 10f, width * 2, height + extraBottom + 10f, Paint().apply { color = 0x3A1B2550 })
    }

    /** Static snapshot for thumbnails/exports. */
    fun snapshot(px: Int): Bitmap {
        val k = px / width
        val b = Bitmap.createBitmap(px, (height * k).toInt(), Bitmap.Config.ARGB_8888)
        val c = Canvas(b); c.scale(k, k); draw(c, 0f, false, false)
        return b
    }

    /** Colour used to extend the ground below the design area on tall screens. */
    val groundColor: Int = def.layers.lastOrNull()?.shapes?.firstOrNull()?.let { ArtModel.parseColor(it.fill) } ?: 0xFF9DDC86.toInt()

    val bounds get() = RectF(0f, 0f, width, height)
}
