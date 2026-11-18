package com.colora.world.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.colora.world.ui.theme.C

/** Hand-drawn, rounded icon set in the picture-book line style (24-unit grid). No emoji, no stock icons. */
enum class Ic { Back, Undo, Redo, Play, Fill, Crayon, Marker, Water, Rainbow, Pattern, Sticker, Eraser, Fit, Home, World, Collection, Lock, Star, Heart, Trash, Plus, Check, Close, Export, Book, Palette, Character, Brush, Music, Sound, Story, Front, BackLayer, Flip, Store, Move, Hand, Sun, Moon, Copy, Left, Right, Restore, Gear, Reset, Shapes, Lab }

@Composable
fun ColoraIcon(ic: Ic, modifier: Modifier = Modifier, tint: Color = C.ink, size: Dp = 28.dp, accent: Color = C.coral, desc: String? = null) {
    val m = if (desc != null) modifier.semantics { contentDescription = desc } else modifier
    Canvas(m.size(size)) {
        val s = this.size.minDimension / 24f
        scale(s, s, pivot = Offset.Zero) { drawIcon(ic, tint, accent) }
    }
}

private fun p(build: Path.() -> Unit) = Path().apply(build)

private fun DrawScope.drawIcon(ic: Ic, ink: Color, accent: Color) {
    val st = Stroke(width = 2.3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    fun line(vararg pts: Float) { val path = p { moveTo(pts[0], pts[1]); var i = 2; while (i < pts.size) { lineTo(pts[i], pts[i + 1]); i += 2 } }; drawPath(path, ink, style = st) }
    when (ic) {
        Ic.Back, Ic.Left -> line(14.5f, 5f, 7.5f, 12f, 14.5f, 19f)
        Ic.Right -> line(9.5f, 5f, 16.5f, 12f, 9.5f, 19f)
        Ic.Undo -> { drawPath(p { moveTo(8f, 9f); lineTo(15f, 9f); cubicTo(19.5f, 9f, 20f, 18f, 15f, 18f); lineTo(9f, 18f) }, ink, style = st); line(11f, 5.5f, 7f, 9f, 11f, 12.5f) }
        Ic.Redo -> { drawPath(p { moveTo(16f, 9f); lineTo(9f, 9f); cubicTo(4.5f, 9f, 4f, 18f, 9f, 18f); lineTo(15f, 18f) }, ink, style = st); line(13f, 5.5f, 17f, 9f, 13f, 12.5f) }
        Ic.Play -> { val t = p { moveTo(8f, 5f); lineTo(19f, 12f); lineTo(8f, 19f); close() }; drawPath(t, accent); drawPath(t, ink, style = st) }
        Ic.Fill -> {
            val b = p { moveTo(5f, 11f); lineTo(11f, 5f); lineTo(18f, 12f); lineTo(12f, 18f); close() }
            drawPath(b, Color.White); drawPath(b, ink, style = st)
            drawPath(p { moveTo(5f, 11f); lineTo(18f, 12f); lineTo(12f, 18f); close() }, accent)
            drawCircle(accent, 2.2f, Offset(19.5f, 18.5f)); line(11f, 5f, 8.5f, 2.5f)
        }
        Ic.Crayon -> {
            val body = p { moveTo(6f, 18f); lineTo(15f, 9f); lineTo(18f, 12f); lineTo(9f, 21f); close() }
            drawPath(body, accent); drawPath(body, ink, style = st)
            val tip = p { moveTo(15f, 9f); lineTo(19.5f, 4.5f); lineTo(18f, 12f) }; drawPath(tip, accent); drawPath(tip, ink, style = st)
            line(8.5f, 15.5f, 11.5f, 18.5f)
        }
        Ic.Marker -> {
            drawRoundRect(accent, Offset(9f, 3f), Size(6f, 5f), CornerRadius(1.5f))
            val b = p { moveTo(8f, 8f); lineTo(16f, 8f); lineTo(16f, 17f); lineTo(14f, 21f); lineTo(10f, 21f); lineTo(8f, 17f); close() }
            drawPath(b, Color.White); drawPath(b, ink, style = st); line(8f, 12f, 16f, 12f)
        }
        Ic.Water -> {
            val d = p { moveTo(12f, 3.5f); cubicTo(16f, 9f, 18.5f, 12f, 18.5f, 15f); cubicTo(18.5f, 18.8f, 15.5f, 21f, 12f, 21f); cubicTo(8.5f, 21f, 5.5f, 18.8f, 5.5f, 15f); cubicTo(5.5f, 12f, 8f, 9f, 12f, 3.5f); close() }
            drawPath(d, accent.copy(alpha = 0.55f)); drawPath(d, ink, style = st)
            drawPath(p { moveTo(9f, 15f); cubicTo(9f, 17f, 10f, 18f, 12f, 18.3f) }, Color.White, style = Stroke(1.8f, cap = StrokeCap.Round))
        }
        Ic.Rainbow -> {
            listOf(Color(0xFFF25C54), Color(0xFFFFD66B), Color(0xFF42C8BD), Color(0xFF5B8DEF)).forEachIndexed { i, c ->
                val r = 9f - i * 2f
                drawArc(c, 180f, 180f, false, Offset(12f - r, 17f - r), Size(r * 2, r * 2), style = Stroke(2.1f, cap = StrokeCap.Round))
            }
        }
        Ic.Pattern -> {
            drawRoundRect(Color.White, Offset(4f, 4f), Size(16f, 16f), CornerRadius(4f)); drawRoundRect(ink, Offset(4f, 4f), Size(16f, 16f), CornerRadius(4f), style = st)
            for (x in 0..1) for (y in 0..1) drawCircle(accent, 1.9f, Offset(8.5f + x * 7f, 8.5f + y * 7f))
            drawCircle(accent, 1.9f, Offset(12f, 12f))
        }
        Ic.Sticker -> {
            val s = p { moveTo(12f, 3.5f); lineTo(14.6f, 9f); lineTo(20.5f, 9.6f); lineTo(16f, 13.6f); lineTo(17.3f, 19.5f); lineTo(12f, 16.5f); lineTo(6.7f, 19.5f); lineTo(8f, 13.6f); lineTo(3.5f, 9.6f); lineTo(9.4f, 9f); close() }
            drawPath(s, C.sunshine); drawPath(s, ink, style = st)
        }
        Ic.Eraser -> {
            val b = p { moveTo(4.5f, 15f); lineTo(13f, 6.5f); lineTo(19.5f, 13f); lineTo(13.5f, 19f); lineTo(8.5f, 19f); close() }
            drawPath(b, Color(0xFFFFC2CF)); drawPath(p { moveTo(4.5f, 15f); lineTo(8.8f, 10.7f); lineTo(15.3f, 17.2f); lineTo(13.5f, 19f); lineTo(8.5f, 19f); close() }, Color.White)
            drawPath(b, ink, style = st); line(8.8f, 10.7f, 15.3f, 17.2f); line(13f, 21f, 20f, 21f)
        }
        Ic.Fit -> { line(4f, 9f, 4f, 4f, 9f, 4f); line(15f, 4f, 20f, 4f, 20f, 9f); line(20f, 15f, 20f, 20f, 15f, 20f); line(9f, 20f, 4f, 20f, 4f, 15f) }
        Ic.Home -> { val h = p { moveTo(4f, 11f); lineTo(12f, 4f); lineTo(20f, 11f); lineTo(20f, 20f); lineTo(4f, 20f); close() }; drawPath(h, accent.copy(alpha = 0.35f)); drawPath(h, ink, style = st); drawRoundRect(ink, Offset(10f, 14f), Size(4f, 6f), CornerRadius(1f), style = st) }
        Ic.World -> {
            drawCircle(C.sky, 8.5f, Offset(12f, 12f)); drawPath(p { moveTo(4f, 14f); cubicTo(8f, 11f, 12f, 16f, 20f, 12.5f); lineTo(19f, 16.5f); cubicTo(16f, 21f, 8f, 21f, 5f, 17f); close() }, C.leaf)
            drawCircle(ink, 8.5f, Offset(12f, 12f), style = st)
        }
        Ic.Collection -> {
            drawRoundRect(C.lavender, Offset(6.5f, 4f), Size(13f, 13f), CornerRadius(2.5f)); drawRoundRect(ink, Offset(6.5f, 4f), Size(13f, 13f), CornerRadius(2.5f), style = st)
            drawRoundRect(Color.White, Offset(3.5f, 8f), Size(13f, 12f), CornerRadius(2.5f)); drawRoundRect(ink, Offset(3.5f, 8f), Size(13f, 12f), CornerRadius(2.5f), style = st)
            drawPath(p { moveTo(5.5f, 18f); lineTo(9f, 13.5f); lineTo(11.5f, 16f); lineTo(13f, 14.5f); lineTo(15f, 18f) }, ink, style = Stroke(1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        Ic.Lock -> { drawRoundRect(C.sunshine, Offset(5f, 10.5f), Size(14f, 10f), CornerRadius(2.5f)); drawRoundRect(ink, Offset(5f, 10.5f), Size(14f, 10f), CornerRadius(2.5f), style = st); drawPath(p { moveTo(8f, 10.5f); lineTo(8f, 8f); cubicTo(8f, 3f, 16f, 3f, 16f, 8f); lineTo(16f, 10.5f) }, ink, style = st); drawCircle(ink, 1.4f, Offset(12f, 15.5f)) }
        Ic.Star -> { val s = p { moveTo(12f, 3.5f); lineTo(14.6f, 9f); lineTo(20.5f, 9.6f); lineTo(16f, 13.6f); lineTo(17.3f, 19.5f); lineTo(12f, 16.5f); lineTo(6.7f, 19.5f); lineTo(8f, 13.6f); lineTo(3.5f, 9.6f); lineTo(9.4f, 9f); close() }; drawPath(s, accent); drawPath(s, ink, style = st) }
        Ic.Heart -> { val h = p { moveTo(12f, 20f); cubicTo(2f, 13f, 4f, 4.5f, 9f, 5f); cubicTo(10.5f, 5.2f, 11.5f, 6f, 12f, 7.5f); cubicTo(12.5f, 6f, 13.5f, 5.2f, 15f, 5f); cubicTo(20f, 4.5f, 22f, 13f, 12f, 20f); close() }; drawPath(h, accent); drawPath(h, ink, style = st) }
        Ic.Trash -> { line(4.5f, 7f, 19.5f, 7f); line(9.5f, 7f, 10f, 4.5f, 14f, 4.5f, 14.5f, 7f); val b = p { moveTo(6.5f, 7f); lineTo(7.5f, 20f); lineTo(16.5f, 20f); lineTo(17.5f, 7f) }; drawPath(b, ink, style = st); line(10.3f, 10.5f, 10.5f, 16.5f); line(13.7f, 10.5f, 13.5f, 16.5f) }
        Ic.Plus -> { line(12f, 5f, 12f, 19f); line(5f, 12f, 19f, 12f) }
        Ic.Check -> drawPath(p { moveTo(5f, 12.5f); lineTo(10f, 17.5f); lineTo(19f, 7f) }, ink, style = Stroke(3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        Ic.Close -> { line(6f, 6f, 18f, 18f); line(18f, 6f, 6f, 18f) }
        Ic.Export -> { line(12f, 14f, 12f, 3.5f); line(8f, 7.5f, 12f, 3.5f, 16f, 7.5f); drawPath(p { moveTo(5f, 12f); lineTo(5f, 20f); lineTo(19f, 20f); lineTo(19f, 12f) }, ink, style = st) }
        Ic.Book -> {
            val l = p { moveTo(12f, 6.5f); cubicTo(9f, 4.5f, 6f, 4.5f, 3.5f, 5.5f); lineTo(3.5f, 19f); cubicTo(6f, 18f, 9f, 18f, 12f, 20f); close() }
            val r = p { moveTo(12f, 6.5f); cubicTo(15f, 4.5f, 18f, 4.5f, 20.5f, 5.5f); lineTo(20.5f, 19f); cubicTo(18f, 18f, 15f, 18f, 12f, 20f); close() }
            drawPath(l, accent.copy(alpha = 0.4f)); drawPath(r, Color.White); drawPath(l, ink, style = st); drawPath(r, ink, style = st)
        }
        Ic.Palette -> {
            val pal = p { moveTo(12f, 3.5f); cubicTo(5f, 3.5f, 3f, 9f, 3.5f, 13f); cubicTo(4f, 17.5f, 8f, 20.5f, 12f, 20.5f); cubicTo(14f, 20.5f, 14f, 18f, 13f, 17f); cubicTo(12f, 15.5f, 13f, 14f, 15f, 14f); cubicTo(18f, 14f, 20.5f, 13f, 20.5f, 10f); cubicTo(20.5f, 6f, 17f, 3.5f, 12f, 3.5f); close() }
            drawPath(pal, Color.White); drawPath(pal, ink, style = st)
            drawCircle(Color(0xFFF25C54), 1.7f, Offset(8f, 9f)); drawCircle(Color(0xFFFFD66B), 1.7f, Offset(12.5f, 7.5f)); drawCircle(Color(0xFF42C8BD), 1.7f, Offset(16.5f, 9.5f)); drawCircle(Color(0xFF9B6BDE), 1.7f, Offset(7.5f, 14f))
        }
        Ic.Character -> {
            drawCircle(accent.copy(alpha = 0.5f), 7.5f, Offset(12f, 13.5f)); drawCircle(ink, 7.5f, Offset(12f, 13.5f), style = st)
            drawPath(p { moveTo(6.5f, 9f); lineTo(5.5f, 3.5f); lineTo(10f, 6.5f) }, ink, style = st); drawPath(p { moveTo(17.5f, 9f); lineTo(18.5f, 3.5f); lineTo(14f, 6.5f) }, ink, style = st)
            drawCircle(ink, 1.2f, Offset(9.5f, 13f)); drawCircle(ink, 1.2f, Offset(14.5f, 13f))
        }
        Ic.Brush -> { drawPath(p { moveTo(19f, 4f); lineTo(10.5f, 13f) }, ink, style = Stroke(3f, cap = StrokeCap.Round)); val tip = p { moveTo(10.5f, 12.5f); cubicTo(7f, 12.5f, 6.5f, 16f, 6f, 18.5f); cubicTo(5.8f, 19.5f, 4.5f, 20f, 4f, 20f); cubicTo(9f, 21f, 12.5f, 18f, 11.8f, 14f); close() }; drawPath(tip, accent); drawPath(tip, ink, style = st) }
        Ic.Music -> { drawPath(p { moveTo(9f, 17f); lineTo(9f, 5.5f); lineTo(19f, 3.5f); lineTo(19f, 15f) }, ink, style = st); drawCircle(accent, 2.6f, Offset(6.6f, 17f)); drawCircle(accent, 2.6f, Offset(16.6f, 15f)) }
        Ic.Sound -> { val b = p { moveTo(4f, 9.5f); lineTo(8f, 9.5f); lineTo(12.5f, 5f); lineTo(12.5f, 19f); lineTo(8f, 14.5f); lineTo(4f, 14.5f); close() }; drawPath(b, accent.copy(alpha = 0.4f)); drawPath(b, ink, style = st); drawArc(ink, -45f, 90f, false, Offset(11f, 8f), Size(8f, 8f), style = st) }
        Ic.Story -> { drawRoundRect(C.butter, Offset(5f, 3.5f), Size(14f, 17f), CornerRadius(2.5f)); drawRoundRect(ink, Offset(5f, 3.5f), Size(14f, 17f), CornerRadius(2.5f), style = st); line(8.5f, 8.5f, 15.5f, 8.5f); line(8.5f, 12f, 15.5f, 12f); line(8.5f, 15.5f, 12.5f, 15.5f) }
        Ic.Front -> { drawRoundRect(C.lilac, Offset(4f, 4f), Size(11f, 11f), CornerRadius(2f)); drawRoundRect(ink, Offset(4f, 4f), Size(11f, 11f), CornerRadius(2f), style = st); drawRoundRect(accent, Offset(9f, 9f), Size(11f, 11f), CornerRadius(2f)); drawRoundRect(ink, Offset(9f, 9f), Size(11f, 11f), CornerRadius(2f), style = st) }
        Ic.BackLayer -> { drawRoundRect(accent, Offset(9f, 9f), Size(11f, 11f), CornerRadius(2f)); drawRoundRect(ink, Offset(9f, 9f), Size(11f, 11f), CornerRadius(2f), style = st); drawRoundRect(C.lilac, Offset(4f, 4f), Size(11f, 11f), CornerRadius(2f)); drawRoundRect(ink, Offset(4f, 4f), Size(11f, 11f), CornerRadius(2f), style = st) }
        Ic.Flip -> { line(12f, 3.5f, 12f, 20.5f); drawPath(p { moveTo(9f, 6f); lineTo(3.5f, 17f); lineTo(9f, 17f); close() }, ink, style = st); val r = p { moveTo(15f, 6f); lineTo(20.5f, 17f); lineTo(15f, 17f); close() }; drawPath(r, accent); drawPath(r, ink, style = st) }
        Ic.Store -> { val b = p { moveTo(4f, 9f); lineTo(20f, 9f); lineTo(18.5f, 20f); lineTo(5.5f, 20f); close() }; drawPath(b, C.sunshine); drawPath(b, ink, style = st); drawPath(p { moveTo(8f, 9f); cubicTo(8f, 3.5f, 16f, 3.5f, 16f, 9f) }, ink, style = st) }
        Ic.Move, Ic.Hand -> { line(12f, 3.5f, 12f, 20.5f); line(3.5f, 12f, 20.5f, 12f); line(9.5f, 6f, 12f, 3.5f, 14.5f, 6f); line(9.5f, 18f, 12f, 20.5f, 14.5f, 18f); line(6f, 9.5f, 3.5f, 12f, 6f, 14.5f); line(18f, 9.5f, 20.5f, 12f, 18f, 14.5f) }
        Ic.Sun -> { drawCircle(C.sunshine, 4.5f, Offset(12f, 12f)); drawCircle(ink, 4.5f, Offset(12f, 12f), style = st); for (i in 0 until 8) { val a = i * Math.PI / 4; val c = kotlin.math.cos(a).toFloat(); val s2 = kotlin.math.sin(a).toFloat(); line(12f + c * 7.2f, 12f + s2 * 7.2f, 12f + c * 9.5f, 12f + s2 * 9.5f) } }
        Ic.Moon -> { val m = p { moveTo(15f, 4f); cubicTo(8f, 4f, 5f, 9f, 5f, 12.5f); cubicTo(5f, 17f, 9f, 20.5f, 13f, 20.5f); cubicTo(16.5f, 20.5f, 19f, 18.5f, 20f, 16f); cubicTo(13f, 17.5f, 9.5f, 10f, 15f, 4f); close() }; drawPath(m, C.lavender); drawPath(m, ink, style = st) }
        Ic.Copy -> { drawRoundRect(ink, Offset(8f, 8f), Size(12f, 12f), CornerRadius(2.5f), style = st); drawPath(p { moveTo(5f, 15.5f); lineTo(4f, 15.5f); lineTo(4f, 4f); lineTo(15.5f, 4f); lineTo(15.5f, 5f) }, ink, style = st) }
        Ic.Restore, Ic.Reset -> { drawArc(ink, 200f, 290f, false, Offset(5f, 5f), Size(14f, 14f), style = st); line(4.5f, 5f, 5.5f, 9.5f, 10f, 8.5f) }
        Ic.Gear -> { drawCircle(ink, 3f, Offset(12f, 12f), style = st); for (i in 0 until 8) { val a = i * Math.PI / 4; val c = kotlin.math.cos(a).toFloat(); val s2 = kotlin.math.sin(a).toFloat(); line(12f + c * 6f, 12f + s2 * 6f, 12f + c * 8.5f, 12f + s2 * 8.5f) }; drawCircle(ink, 6f, Offset(12f, 12f), style = st) }
        Ic.Shapes -> { drawCircle(C.sky, 4.5f, Offset(8f, 8f)); drawCircle(ink, 4.5f, Offset(8f, 8f), style = st); val t = p { moveTo(16.5f, 4f); lineTo(21f, 12f); lineTo(12f, 12f); close() }; drawPath(t, C.sunshine); drawPath(t, ink, style = st); drawRoundRect(accent, Offset(8f, 13.5f), Size(8f, 7f), CornerRadius(1.5f)); drawRoundRect(ink, Offset(8f, 13.5f), Size(8f, 7f), CornerRadius(1.5f), style = st) }
        Ic.Lab -> { val f = p { moveTo(9.5f, 3.5f); lineTo(9.5f, 9.5f); lineTo(4.5f, 18.5f); cubicTo(4f, 19.5f, 4.5f, 20.5f, 5.8f, 20.5f); lineTo(18.2f, 20.5f); cubicTo(19.5f, 20.5f, 20f, 19.5f, 19.5f, 18.5f); lineTo(14.5f, 9.5f); lineTo(14.5f, 3.5f) }; drawPath(p { moveTo(7f, 14f); lineTo(17f, 14f); lineTo(19.5f, 18.5f); cubicTo(20f, 19.5f, 19.5f, 20.5f, 18.2f, 20.5f); lineTo(5.8f, 20.5f); cubicTo(4.5f, 20.5f, 4f, 19.5f, 4.5f, 18.5f); close() }, accent); drawPath(f, ink, style = st); line(8f, 3.5f, 16f, 3.5f) }
    }
}
