package com.colora.world.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.colora.world.AppGraph
import com.colora.world.data.ToyRef
import com.colora.world.engine.paint.PaintReducer
import kotlin.math.abs
import kotlin.math.sin

/**
 * Everything needed to draw one placed thing: artwork toys keep the child's
 * strokes in per-part textures; characters are a baked composite; props use
 * their own pre-coloured art.
 */
class ToyVisual(
    val ref: String,
    val templateId: String,
    val art: ArtModel,
    private val texture: ToyTexture?,
    private val composite: Bitmap?,
    val caps: Set<String>,
    val preset: String,
    val audioSetId: String?,
) {
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    val footY: Float get() = art.def.contentBounds.getOrElse(3) { 1000f }

    /** Draws in toy design units (1024), origin at the design origin. */
    fun draw(c: Canvas, tSec: Float, energy: Float, moving: Boolean, seed: Int) {
        if (texture != null) {
            texture.draw(c, ToyRig.partMatrices(art, tSec, energy, moving, seed))
        } else if (composite != null) {
            val s = c.save()
            val hop = if (energy > 0f) abs(sin(tSec * 4.5f)) * 26f * energy else 0f
            c.translate(0f, -hop)
            c.drawBitmap(composite, null, RectF(0f, 0f, 1024f, 1024f), paint)
            c.restoreToCount(s)
        }
    }

    companion object {
        /** Loads (IO thread) the visual for a scene/book reference. Null if the source is missing. */
        fun resolve(g: AppGraph, pid: String, refStr: String, templateId: String): ToyVisual? {
            val ref = ToyRef.parse(refStr) ?: return null
            return when (ref.kind) {
                "art" -> {
                    val doc = ref.revId?.let { g.library.artworkRevision(pid, ref.docId, it) } ?: g.library.loadArtwork(pid, ref.docId)?.second ?: return null
                    val art = g.content.art(doc.templateId) ?: return null
                    val tex = g.toyCache.bake(refStr, art, PaintReducer.replay(doc.commands))
                    val e = g.content.template(doc.templateId)
                    ToyVisual(refStr, doc.templateId, art, tex, null, g.content.capabilities(doc.templateId), e?.preset ?: "none", art.def.audioSetId)
                }
                "prop" -> {
                    val art = g.content.art(ref.docId) ?: return null
                    val tex = g.toyCache.bake(refStr, art, null)
                    ToyVisual(refStr, ref.docId, art, tex, null, g.content.capabilities(ref.docId), "nature_react", null)
                }
                "chr" -> {
                    val doc = ref.revId?.let { g.library.characterRevision(pid, ref.docId, it) } ?: g.library.loadCharacter(pid, ref.docId)?.second ?: return null
                    val body = g.content.art(doc.recipe.bodyId) ?: return null
                    val bmp = CharacterRenderer.bake(g.content, doc.recipe)
                    ToyVisual(refStr, doc.recipe.bodyId, body, null, bmp, g.content.capabilities(doc.recipe.bodyId), doc.recipe.preset, null)
                }
                else -> null
            }
        }
    }
}
