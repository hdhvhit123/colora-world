package com.colora.world.engine.paint

import kotlin.math.abs
import kotlin.math.floor

/** Deterministic brush maths shared by renderer and tests. */
object Brushes {
    /** Rainbow brush hue cycle: colour depends only on distance along stroke and seed. */
    private val rainbow = intArrayOf(
        0xFFF25C54.toInt(), 0xFFFFA94D.toInt(), 0xFFFFE066.toInt(), 0xFF4FB477.toInt(),
        0xFF42C8BD.toInt(), 0xFF5B8DEF.toInt(), 0xFF9B6BDE.toInt(),
    )

    fun rainbowColorAt(distanceUnits: Float, seed: Long, bandUnits: Float = 60f): Int {
        val offset = (abs(seed) % rainbow.size).toInt()
        val t = distanceUnits / bandUnits
        val i = floor(t).toInt()
        val f = t - i
        val a = rainbow[(i + offset) % rainbow.size]
        val b = rainbow[(i + 1 + offset) % rainbow.size]
        return lerpColor(a, b, f)
    }

    fun lerpColor(a: Int, b: Int, t: Float): Int {
        fun ch(c: Int, s: Int) = (c ushr s) and 0xFF
        fun mix(s: Int) = (ch(a, s) + (ch(b, s) - ch(a, s)) * t).toInt().coerceIn(0, 255)
        return (mix(24) shl 24) or (mix(16) shl 16) or (mix(8) shl 8) or mix(0)
    }

    /** Small LCG for texture jitter so crayon grain is identical after reload. */
    class Rng(seed: Long) {
        private var s = seed xor 0x5DEECE66DL
        fun next(): Float { s = (s * 6364136223846793005L + 1442695040888963407L); return ((s ushr 33) and 0xFFFFFF).toFloat() / 0xFFFFFF.toFloat() }
    }

    /** Default size presets in template units per brush (renderers scale by zoom). */
    fun defaultSize(b: BrushId): Float = when (b) {
        BrushId.CRAYON -> 26f; BrushId.MARKER -> 22f; BrushId.WATERCOLOR -> 44f
        BrushId.RAINBOW -> 30f; BrushId.PATTERN -> 56f; BrushId.ERASER -> 40f; BrushId.FILL -> 1f
    }

    /** Minimum spacing between recorded samples (template units) to bound sample rate. */
    const val MIN_SAMPLE_SPACING = 2.5f
}
