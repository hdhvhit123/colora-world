package com.colora.world.engine.geometry

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** One drawing command of normalised path data (absolute coordinates only). */
sealed interface PathOp {
    data class MoveTo(val x: Float, val y: Float) : PathOp
    data class LineTo(val x: Float, val y: Float) : PathOp
    data class QuadTo(val x1: Float, val y1: Float, val x: Float, val y: Float) : PathOp
    data class CubicTo(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val x: Float, val y: Float) : PathOp
    data object Close : PathOp
}

data class Bounds(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width get() = right - left
    val height get() = bottom - top
    fun contains(x: Float, y: Float, pad: Float = 0f) =
        x >= left - pad && x <= right + pad && y >= top - pad && y <= bottom + pad
    fun union(o: Bounds) = Bounds(min(left, o.left), min(top, o.top), max(right, o.right), max(bottom, o.bottom))
    companion object {
        val EMPTY = Bounds(0f, 0f, 0f, 0f)
    }
}

/**
 * Parser for the content pipeline's path data: absolute M/L/Q/C/Z only. The
 * pipeline normalises every source shape to this subset so the runtime never
 * needs a general SVG parser (and never parses per frame).
 */
object PathData {
    class ParseException(msg: String) : IllegalArgumentException(msg)

    fun parse(d: String): List<PathOp> {
        val ops = ArrayList<PathOp>()
        var i = 0
        val n = d.length
        val nums = FloatArray(6)
        fun skipSep() {
            while (i < n && (d[i] == ' ' || d[i] == ',' || d[i] == '\n' || d[i] == '\t')) i++
        }
        fun readNum(): Float {
            skipSep()
            val start = i
            if (i < n && (d[i] == '-' || d[i] == '+')) i++
            while (i < n && (d[i].isDigit() || d[i] == '.' || d[i] == 'e' || d[i] == 'E' ||
                    ((d[i] == '-' || d[i] == '+') && (d[i - 1] == 'e' || d[i - 1] == 'E')))) i++
            if (start == i) throw ParseException("number expected at $start")
            return d.substring(start, i).toFloat()
        }
        while (true) {
            skipSep()
            if (i >= n) break
            val c = d[i++]
            when (c) {
                'M' -> { ops += PathOp.MoveTo(readNum(), readNum()) }
                'L' -> { ops += PathOp.LineTo(readNum(), readNum()) }
                'Q' -> { for (k in 0 until 4) nums[k] = readNum(); ops += PathOp.QuadTo(nums[0], nums[1], nums[2], nums[3]) }
                'C' -> { for (k in 0 until 6) nums[k] = readNum(); ops += PathOp.CubicTo(nums[0], nums[1], nums[2], nums[3], nums[4], nums[5]) }
                'Z' -> ops += PathOp.Close
                else -> throw ParseException("unsupported command '$c' at ${i - 1}")
            }
        }
        if (ops.isNotEmpty() && ops.first() !is PathOp.MoveTo) throw ParseException("path must start with M")
        return ops
    }

    /**
     * Flattens curves into closed polygons (one per contour) with a fixed
     * subdivision so results are deterministic across devices.
     */
    fun flatten(ops: List<PathOp>, tolerance: Float = 1.5f): List<FloatArray> {
        val contours = ArrayList<FloatArray>()
        var cur = FloatList()
        var cx = 0f; var cy = 0f; var sx = 0f; var sy = 0f
        fun finish() {
            if (cur.size >= 6) contours += cur.toArray()
            cur = FloatList()
        }
        for (op in ops) when (op) {
            is PathOp.MoveTo -> { finish(); cx = op.x; cy = op.y; sx = cx; sy = cy; cur.add(cx, cy) }
            is PathOp.LineTo -> { cx = op.x; cy = op.y; cur.add(cx, cy) }
            is PathOp.QuadTo -> {
                val len = dist(cx, cy, op.x1, op.y1) + dist(op.x1, op.y1, op.x, op.y)
                val steps = segs(len, tolerance)
                for (s in 1..steps) {
                    val t = s.toFloat() / steps; val u = 1 - t
                    cur.add(u * u * cx + 2 * u * t * op.x1 + t * t * op.x, u * u * cy + 2 * u * t * op.y1 + t * t * op.y)
                }
                cx = op.x; cy = op.y
            }
            is PathOp.CubicTo -> {
                val len = dist(cx, cy, op.x1, op.y1) + dist(op.x1, op.y1, op.x2, op.y2) + dist(op.x2, op.y2, op.x, op.y)
                val steps = segs(len, tolerance)
                for (s in 1..steps) {
                    val t = s.toFloat() / steps; val u = 1 - t
                    val a = u * u * u; val b = 3 * u * u * t; val c = 3 * u * t * t; val e = t * t * t
                    cur.add(a * cx + b * op.x1 + c * op.x2 + e * op.x, a * cy + b * op.y1 + c * op.y2 + e * op.y)
                }
                cx = op.x; cy = op.y
            }
            PathOp.Close -> { cx = sx; cy = sy; finish() }
        }
        finish()
        return contours
    }

    private fun segs(len: Float, tol: Float) = (len / (tol * 6f)).toInt().coerceIn(2, 48)
    private fun dist(ax: Float, ay: Float, bx: Float, by: Float) = sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay))

    fun bounds(contours: List<FloatArray>): Bounds {
        var l = Float.MAX_VALUE; var t = Float.MAX_VALUE; var r = -Float.MAX_VALUE; var b = -Float.MAX_VALUE
        for (c in contours) {
            var k = 0
            while (k < c.size) { l = min(l, c[k]); r = max(r, c[k]); t = min(t, c[k + 1]); b = max(b, c[k + 1]); k += 2 }
        }
        return if (l == Float.MAX_VALUE) Bounds.EMPTY else Bounds(l, t, r, b)
    }
}

/** Minimal growable float buffer to avoid boxing while flattening. */
internal class FloatList {
    private var data = FloatArray(64)
    var size = 0; private set
    fun add(x: Float, y: Float) {
        if (size + 2 > data.size) data = data.copyOf(data.size * 2)
        data[size++] = x; data[size++] = y
    }
    fun toArray(): FloatArray = data.copyOf(size)
}
