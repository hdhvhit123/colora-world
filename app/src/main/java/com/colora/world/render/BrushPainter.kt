package com.colora.world.render

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import com.colora.world.engine.paint.BrushId
import com.colora.world.engine.paint.Brushes
import com.colora.world.engine.paint.PatternId
import com.colora.world.engine.paint.Stroke
import kotlin.math.hypot

/**
 * Deterministic brush rendering in template units. The same stroke renders
 * identically in the studio, on toys, in the world and in exports because it
 * only depends on the recorded points, size, colour and seed.
 */
object BrushPainter {
    private val clearMode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

    fun strokePath(pts: List<Float>, count: Int = pts.size / 4): Path {
        val p = Path()
        if (count == 0) return p
        p.moveTo(pts[0], pts[1])
        if (count == 1) { p.lineTo(pts[0] + 0.1f, pts[1]); return p }
        var px = pts[0]; var py = pts[1]
        for (i in 1 until count) {
            val x = pts[i * 4]; val y = pts[i * 4 + 1]
            val mx = (px + x) / 2f; val my = (py + y) / 2f
            if (i == 1) p.lineTo(mx, my) else p.quadTo(px, py, mx, my)
            px = x; py = y
        }
        p.lineTo(px, py)
        return p
    }

    private fun base(size: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = size; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }

    /** Draws [s]; [count] lets the studio draw a live stroke without copying. */
    fun draw(c: Canvas, s: Stroke, count: Int = s.pointCount, paperColor: Int = Color.WHITE) {
        if (count <= 0) return
        val pts = s.points
        when (s.brush) {
            BrushId.MARKER -> c.drawPath(strokePath(pts, count), base(s.sizeUnits).apply { color = s.color; alpha = (255 * s.opacity).toInt() })
            BrushId.CRAYON -> crayon(c, s, pts, count)
            BrushId.WATERCOLOR -> {
                val path = strokePath(pts, count)
                c.drawPath(path, base(s.sizeUnits).apply { color = s.color; alpha = (255 * 0.42f * s.opacity).toInt() })
                c.drawPath(path, base(s.sizeUnits * 0.55f).apply { color = s.color; alpha = (255 * 0.22f * s.opacity).toInt() })
            }
            BrushId.RAINBOW -> rainbow(c, s, pts, count)
            BrushId.PATTERN -> pattern(c, s, pts, count)
            BrushId.ERASER -> c.drawPath(strokePath(pts, count), base(s.sizeUnits).apply { xfermode = clearMode })
            BrushId.FILL -> Unit
        }
    }

    private fun crayon(c: Canvas, s: Stroke, pts: List<Float>, count: Int) {
        val path = strokePath(pts, count)
        c.drawPath(path, base(s.sizeUnits).apply { color = s.color; alpha = (255 * 0.93f * s.opacity).toInt() })
        // waxy grain: small deterministic holes that let the paper/base fill show through
        val rng = Brushes.Rng(s.seed)
        val hole = Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode = clearMode; alpha = 120 }
        val r = s.sizeUnits / 2f
        var carry = 0f
        for (i in 1 until count) {
            val x0 = pts[(i - 1) * 4]; val y0 = pts[(i - 1) * 4 + 1]; val x1 = pts[i * 4]; val y1 = pts[i * 4 + 1]
            val len = hypot(x1 - x0, y1 - y0)
            var t = carry
            while (t < len) {
                val f = if (len == 0f) 0f else t / len
                val cx = x0 + (x1 - x0) * f; val cy = y0 + (y1 - y0) * f
                repeat(3) {
                    val ox = (rng.next() * 2f - 1f) * r * 0.95f; val oy = (rng.next() * 2f - 1f) * r * 0.95f
                    c.drawCircle(cx + ox, cy + oy, 0.9f + rng.next() * 1.6f, hole)
                }
                t += 5f
            }
            carry = t - len
        }
    }

    private fun rainbow(c: Canvas, s: Stroke, pts: List<Float>, count: Int) {
        val p = base(s.sizeUnits)
        if (count == 1) { p.color = Brushes.rainbowColorAt(0f, s.seed); c.drawPoint(pts[0], pts[1], p); return }
        var dist = 0f
        for (i in 1 until count) {
            val x0 = pts[(i - 1) * 4]; val y0 = pts[(i - 1) * 4 + 1]; val x1 = pts[i * 4]; val y1 = pts[i * 4 + 1]
            p.color = Brushes.rainbowColorAt(dist, s.seed); p.alpha = (255 * s.opacity).toInt()
            c.drawLine(x0, y0, x1, y1, p)
            dist += hypot(x1 - x0, y1 - y0)
        }
    }

    private val tiles = HashMap<Pair<PatternId, Int>, BitmapShader>()

    private fun pattern(c: Canvas, s: Stroke, pts: List<Float>, count: Int) {
        val pid = s.pattern ?: PatternId.DOTS
        val shader = tiles.getOrPut(pid to s.color) { BitmapShader(PatternTiles.tile(pid, s.color), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT) }
        shader.setLocalMatrix(Matrix().apply { setScale(PatternTiles.UNITS / PatternTiles.PX, PatternTiles.UNITS / PatternTiles.PX) })
        // soft tint underneath so the pattern reads as a coloured area
        val path = strokePath(pts, count)
        c.drawPath(path, base(s.sizeUnits).apply { color = s.color; alpha = 46 })
        c.drawPath(path, base(s.sizeUnits).apply { this.shader = shader })
    }
}

/** Pattern tiles drawn in code so they stay crisp and need no bitmaps in assets. */
object PatternTiles {
    const val PX = 96f
    const val UNITS = 52f

    fun tile(id: PatternId, color: Int): Bitmap {
        val n = PX.toInt()
        val b = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        when (id) {
            PatternId.DOTS -> { c.drawCircle(24f, 24f, 13f, p); c.drawCircle(72f, 72f, 13f, p) }
            PatternId.STRIPES -> { p.strokeWidth = 18f; p.style = Paint.Style.STROKE; c.drawLine(-10f, 24f, 106f, 24f, p); c.drawLine(-10f, 72f, 106f, 72f, p) }
            PatternId.CHECKS -> { c.drawRect(0f, 0f, 48f, 48f, p); c.drawRect(48f, 48f, 96f, 96f, p) }
            PatternId.STARS -> { c.drawPath(star(28f, 28f, 20f), p); c.drawPath(star(74f, 72f, 15f), p) }
            PatternId.HEARTS -> { c.drawPath(heart(28f, 30f, 19f), p); c.drawPath(heart(72f, 74f, 15f), p) }
        }
        return b
    }

    fun star(cx: Float, cy: Float, r: Float): Path {
        val p = Path()
        for (i in 0 until 10) {
            val a = Math.PI / 2 + i * Math.PI / 5
            val rr = if (i % 2 == 0) r else r * 0.45f
            val x = cx + (rr * kotlin.math.cos(a)).toFloat(); val y = cy - (rr * kotlin.math.sin(a)).toFloat()
            if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close(); return p
    }

    fun heart(cx: Float, cy: Float, r: Float): Path = Path().apply {
        moveTo(cx, cy + r * 0.9f)
        cubicTo(cx - r * 1.5f, cy - r * 0.1f, cx - r * 0.7f, cy - r * 1.2f, cx, cy - r * 0.45f)
        cubicTo(cx + r * 0.7f, cy - r * 1.2f, cx + r * 1.5f, cy - r * 0.1f, cx, cy + r * 0.9f)
        close()
    }
}
