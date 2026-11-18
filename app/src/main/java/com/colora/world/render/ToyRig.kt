package com.colora.world.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.LruCache
import com.colora.world.content.ContentRepo
import com.colora.world.engine.paint.PaintState
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Turns a part's `motion` + `amp` from the content rig into a matrix around its
 * pivot. [energy] is 0..1: idle uses a gentle fraction, a tap/interaction uses
 * full amplitude, [moving] spins wheels.
 */
object ToyRig {
    fun partMatrices(art: ArtModel, tSec: Float, energy: Float, moving: Boolean = false, seed: Int = 0): Map<String, Matrix> {
        val out = HashMap<String, Matrix>()
        for (p in art.parts) {
            val m = Matrix()
            val px = p.def.pivot.getOrElse(0) { 512f }; val py = p.def.pivot.getOrElse(1) { 512f }
            local(m, p.def.motion, p.def.amp, px, py, tSec + p.index * 0.37f + seed * 0.11f, energy, moving)
            p.def.parent?.let { parent -> out[parent]?.let { m.postConcat(it) } }
            out[p.def.id] = m
        }
        return out
    }

    private fun local(m: Matrix, motion: String?, amp: Float, px: Float, py: Float, t: Float, e: Float, moving: Boolean) {
        if (motion == null || e <= 0f && !moving) return
        val w = sin(2f * PI.toFloat() * 0.9f * t)
        val slow = sin(2f * PI.toFloat() * 0.45f * t)
        when (motion) {
            "spin" -> if (moving) m.postRotate((t * 420f) % 360f, px, py) else m.postRotate(amp.coerceAtMost(20f) * 0.15f * e * w, px, py)
            "spinslow", "orbit", "swirl" -> m.postRotate((t * 40f * (0.4f + e)) % 360f, px, py)
            "spinflat" -> m.postScale(0.75f + 0.25f * abs(sin(t * 2.4f)), 1f, px, py)
            "bob", "hover", "float", "drift", "rise", "swim", "lift", "zoom", "fall" -> m.postTranslate(0f, -amp * e * (0.5f + 0.5f * slow))
            "bounce", "hop", "leap" -> m.postTranslate(0f, -amp * e * abs(sin(t * 4.2f)))
            "slide", "scuttle", "push" -> m.postTranslate(amp * e * w, 0f)
            "flapx" -> m.postScale(1f - 0.35f * e * abs(w), 1f, px, py)
            "squash" -> { val k = 0.08f * e * w; m.postScale(1f + k, 1f - k, px, py) }
            "pulse", "glow", "bloom", "puff", "pop", "shimmer", "twinkle", "flicker", "splash", "spray" -> {
                val k = 1f + (amp / 100f).coerceIn(0.04f, 0.25f) * e * (0.5f + 0.5f * sin(t * 6.1f))
                m.postScale(k, k, px, py)
            }
            "flap" -> m.postRotate(amp * e * sin(t * 9f), px, py)
            else -> m.postRotate(amp * e * w, px, py) // wag, sway, nod, tilt, rock, swing, wave, wiggle, dance, …
        }
    }
}

/** Baked per-part textures of one artwork revision: the toy keeps the child's exact paint. */
class ToyTexture(val art: ArtModel, private val parts: Map<String, Pair<Bitmap, RectF>>) {
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    val bytes: Int get() = parts.values.sumOf { it.first.byteCount }

    /** Draws in design units (caller sets the transform). */
    fun draw(c: Canvas, matrices: Map<String, Matrix>?) {
        for (p in art.parts) {
            val (bmp, rect) = parts[p.def.id] ?: continue
            val s = c.save()
            matrices?.get(p.def.id)?.let { c.concat(it) }
            c.drawBitmap(bmp, null, rect, paint)
            c.restoreToCount(s)
        }
    }
}

class ToyCache(private val content: ContentRepo) {
    private val cache = object : LruCache<String, ToyTexture>(72 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ToyTexture) = value.bytes
    }

    fun peek(key: String): ToyTexture? = cache.get(key)

    /** Bakes (or returns cached) part textures. Call off the main thread. */
    fun bake(key: String, art: ArtModel, state: PaintState?, scale: Float = 0.7f): ToyTexture {
        cache.get("$key@${ArtRenderer.VERSION}")?.let { return it }
        val map = HashMap<String, Pair<Bitmap, RectF>>()
        val useSamples = state == null
        for (p in art.parts) {
            val r = RectF(p.bounds)
            if (art.isFreePage) r.set(0f, 0f, 1024f, 1024f)
            val w = (r.width() * scale).toInt().coerceAtLeast(1); val h = (r.height() * scale).toInt().coerceAtLeast(1)
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp)
            c.scale(scale, scale)
            c.translate(-r.left, -r.top)
            ArtRenderer.draw(c, art, state, ArtRenderer.Options(useSamples = useSamples, onlyPart = p.def.id, stickerArt = content::art))
            map[p.def.id] = bmp to r
        }
        return ToyTexture(art, map).also { cache.put("$key@${ArtRenderer.VERSION}", it) }
    }

    fun clear() = cache.evictAll()
}
