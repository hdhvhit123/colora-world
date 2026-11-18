package com.colora.world.engine.world

import kotlin.math.hypot
import kotlin.math.min

/** Injectable monotonic clock (ms) so tests and video export are deterministic. */
fun interface Clock { fun nowMs(): Long }

enum class ToyState { IDLE, SELECTED, DRAGGING, INTERACTING, MOVING, ATTACHED, PAUSED }

/**
 * Waypoint mover using elapsed time (not frame counts). Large gaps (app was
 * paused) are clamped so resuming never teleports a toy.
 */
class WaypointMover(private val speedPerSec: Float = 0.18f, private val maxStepMs: Long = 100) {
    data class Motion(val x: Float, val y: Float, val done: Boolean)

    fun step(x: Float, y: Float, path: List<Pair<Float, Float>>, index: Int, elapsedMs: Long): Pair<Motion, Int> {
        if (index >= path.size) return Motion(x, y, true) to index
        var remaining = speedPerSec * min(elapsedMs, maxStepMs) / 1000f
        var cx = x; var cy = y; var i = index
        while (remaining > 0 && i < path.size) {
            val (tx, ty) = path[i]
            val d = hypot(tx - cx, ty - cy)
            if (d <= remaining) { cx = tx; cy = ty; remaining -= d; i++ } else {
                cx += (tx - cx) / d * remaining; cy += (ty - cy) / d * remaining; remaining = 0f
            }
        }
        return Motion(cx, cy, i >= path.size) to i
    }
}
