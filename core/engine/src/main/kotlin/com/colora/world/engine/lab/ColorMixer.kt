package com.colora.world.engine.lab

import kotlin.math.roundToInt

/**
 * Art-directed paint mixing for the Colour Lab. This is an illustrative
 * model (RYB cube interpolation, after Gossett & Chen 2004) – not a pigment
 * simulation – chosen so classic pairs read the way children are taught:
 * red+yellow=orange, yellow+blue=green, red+blue=purple.
 */
object ColorMixer {
    data class Ryb(val r: Float, val y: Float, val b: Float)

    // RYB cube corner colours (art-directed sRGB)
    private val corners = arrayOf(
        floatArrayOf(1f, 1f, 1f),            // 000 white
        floatArrayOf(0.93f, 0.25f, 0.21f),    // r
        floatArrayOf(1f, 0.85f, 0.2f),        // y
        floatArrayOf(1f, 0.55f, 0.15f),       // r+y orange
        floatArrayOf(0.2f, 0.45f, 0.85f),     // b
        floatArrayOf(0.55f, 0.27f, 0.72f),    // r+b purple
        floatArrayOf(0.25f, 0.68f, 0.35f),    // y+b green
        floatArrayOf(0.47f, 0.29f, 0.17f),    // r+y+b warm brown (reads as "brown" to children)
    )

    fun rybToRgb(c: Ryb): Int {
        val r = c.r.coerceIn(0f, 1f); val y = c.y.coerceIn(0f, 1f); val b = c.b.coerceIn(0f, 1f)
        val out = FloatArray(3)
        for (ch in 0..2) {
            fun k(i: Int) = corners[i][ch]
            val c00 = k(0) * (1 - r) + k(1) * r
            val c10 = k(2) * (1 - r) + k(3) * r
            val c01 = k(4) * (1 - r) + k(5) * r
            val c11 = k(6) * (1 - r) + k(7) * r
            val c0 = c00 * (1 - y) + c10 * y
            val c1 = c01 * (1 - y) + c11 * y
            out[ch] = c0 * (1 - b) + c1 * b
        }
        return argb(out[0], out[1], out[2])
    }

    /** Mixes 2–3 drops given as RYB amounts plus optional white (tint). */
    fun mix(drops: List<Ryb>, white: Float = 0f): Int {
        require(drops.isNotEmpty() && drops.size <= 3)
        val n = drops.size.toFloat()
        var r = drops.sumOf { it.r.toDouble() }.toFloat() / n
        var y = drops.sumOf { it.y.toDouble() }.toFloat() / n
        var b = drops.sumOf { it.b.toDouble() }.toFloat() / n
        // normalise so the strongest channel stays saturated (mixing paint does not wash out)
        val m = maxOf(r, y, b)
        if (m > 0f) { r /= m; y /= m; b /= m }
        val base = rybToRgb(Ryb(r, y, b))
        return if (white <= 0f) base else lighten(base, white.coerceIn(0f, 0.85f))
    }

    val RED = Ryb(1f, 0f, 0f); val YELLOW = Ryb(0f, 1f, 0f); val BLUE = Ryb(0f, 0f, 1f)

    private fun lighten(c: Int, t: Float): Int {
        val r = ((c shr 16) and 0xFF) / 255f; val g = ((c shr 8) and 0xFF) / 255f; val b = (c and 0xFF) / 255f
        return argb(r + (1 - r) * t, g + (1 - g) * t, b + (1 - b) * t)
    }

    private fun argb(r: Float, g: Float, b: Float): Int =
        (0xFF shl 24) or ((r.coerceIn(0f, 1f) * 255).roundToInt() shl 16) or ((g.coerceIn(0f, 1f) * 255).roundToInt() shl 8) or (b.coerceIn(0f, 1f) * 255).roundToInt()

    /** Hue family for a colour, used only to label results (e.g. "orange"). */
    fun family(c: Int): String {
        val r = (c shr 16) and 0xFF; val g = (c shr 8) and 0xFF; val b = c and 0xFF
        val max = maxOf(r, g, b); val min = minOf(r, g, b)
        if (max - min < 24) return if (max > 200) "white" else if (max < 70) "black" else "grey"
        val d = (max - min).toFloat()
        val h = when (max) {
            r -> 60f * (((g - b) / d) % 6)
            g -> 60f * ((b - r) / d + 2)
            else -> 60f * ((r - g) / d + 4)
        }.let { if (it < 0) it + 360 else it }
        return when {
            max < 150 && h in 10f..50f -> "brown"
            (h < 15 || h >= 340) && min >= 130 -> "pink"
            h < 15 || h >= 340 -> "red"
            h < 45 -> "orange"
            h < 70 -> "yellow"
            h < 165 -> "green"
            h < 200 -> "teal"
            h < 255 -> "blue"
            h < 300 -> "purple"
            else -> "pink"
        }
    }

    const val MAX_CUSTOM_SWATCHES = 24
}
