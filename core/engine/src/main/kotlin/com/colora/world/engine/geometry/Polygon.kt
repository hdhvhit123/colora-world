package com.colora.world.engine.geometry

import kotlin.math.sqrt

/** Even-odd polygon queries over flattened contours (all contours of one shape). */
object Polygon {
    fun contains(contours: List<FloatArray>, x: Float, y: Float): Boolean {
        var inside = false
        for (c in contours) {
            val n = c.size / 2
            var j = n - 1
            for (i in 0 until n) {
                val xi = c[i * 2]; val yi = c[i * 2 + 1]
                val xj = c[j * 2]; val yj = c[j * 2 + 1]
                if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) inside = !inside
                j = i
            }
        }
        return inside
    }

    /** Shortest distance from (x,y) to any edge. */
    fun distanceToEdge(contours: List<FloatArray>, x: Float, y: Float): Float {
        var best = Float.MAX_VALUE
        for (c in contours) {
            val n = c.size / 2
            var j = n - 1
            for (i in 0 until n) {
                val d = segDist(x, y, c[j * 2], c[j * 2 + 1], c[i * 2], c[i * 2 + 1])
                if (d < best) best = d
                j = i
            }
        }
        return best
    }

    /** Sum of contour areas regardless of winding (approximate for regions with holes). */
    fun area(contours: List<FloatArray>): Float {
        var total = 0f
        for (c in contours) {
            val n = c.size / 2
            var s = 0f
            var j = n - 1
            for (i in 0 until n) { s += c[j * 2] * c[i * 2 + 1] - c[i * 2] * c[j * 2 + 1]; j = i }
            total += kotlin.math.abs(s / 2f)
        }
        return total
    }

    private fun segDist(px: Float, py: Float, ax: Float, ay: Float, bx: Float, by: Float): Float {
        val dx = bx - ax; val dy = by - ay
        val l2 = dx * dx + dy * dy
        var t = if (l2 == 0f) 0f else ((px - ax) * dx + (py - ay) * dy) / l2
        t = t.coerceIn(0f, 1f)
        val qx = ax + t * dx - px; val qy = ay + t * dy - py
        return sqrt(qx * qx + qy * qy)
    }
}

/** 2D affine transform (a c e / b d f), used for viewport and rig maths. */
data class Affine(val a: Float = 1f, val b: Float = 0f, val c: Float = 0f, val d: Float = 1f, val e: Float = 0f, val f: Float = 0f) {
    fun map(x: Float, y: Float): Pair<Float, Float> = Pair(a * x + c * y + e, b * x + d * y + f)
    fun then(o: Affine) = Affine(
        o.a * a + o.c * b, o.b * a + o.d * b,
        o.a * c + o.c * d, o.b * c + o.d * d,
        o.a * e + o.c * f + o.e, o.b * e + o.d * f + o.f,
    )
    fun inverse(): Affine {
        val det = a * d - b * c
        require(det != 0f) { "singular transform" }
        val ia = d / det; val ib = -b / det; val ic = -c / det; val id = a / det
        return Affine(ia, ib, ic, id, -(ia * e + ic * f), -(ib * e + id * f))
    }
    companion object {
        val IDENTITY = Affine()
        fun translate(x: Float, y: Float) = Affine(e = x, f = y)
        fun scale(s: Float, sy: Float = s) = Affine(a = s, d = sy)
        fun rotateDeg(deg: Float): Affine {
            val r = Math.toRadians(deg.toDouble())
            val cs = kotlin.math.cos(r).toFloat(); val sn = kotlin.math.sin(r).toFloat()
            return Affine(cs, sn, -sn, cs, 0f, 0f)
        }
    }
}
