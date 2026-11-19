package com.colora.world.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.colora.world.AppGraph
import com.colora.world.data.BookDoc
import com.colora.world.data.ToyRef
import com.colora.world.engine.paint.PaintReducer
import com.colora.world.ui.Strings

/**
 * Lays out the child's book pages (cover, one picture, two pictures, scene)
 * from saved revisions only, so a page never changes after it was added.
 * Used for on-screen pages, the reader and PDF export.
 */
class BookRenderer(private val g: AppGraph, private val pid: String, private val lang: String) {
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ArtRenderer.INK; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER }
    private val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeJoin = Paint.Join.ROUND }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)

    fun title(b: BookDoc) = b.customTitle ?: Strings.get(lang, b.titleKey)

    fun pageBitmap(b: BookDoc, index: Int, widthPx: Int, aspect: Float = 0.72f): Bitmap {
        val h = (widthPx / aspect).toInt()
        val bmp = Bitmap.createBitmap(widthPx, h, Bitmap.Config.ARGB_8888)
        drawPage(Canvas(bmp), b, index, widthPx.toFloat(), h.toFloat())
        return bmp
    }

    /** Cover: book colour, title, the first picture. */
    fun drawCover(c: Canvas, b: BookDoc, w: Float, h: Float) {
        fill.color = b.coverColor
        c.drawRect(0f, 0f, w, h, fill)
        fill.color = ArtRenderer.PAPER
        val inner = RectF(w * 0.08f, h * 0.22f, w * 0.92f, h * 0.86f)
        c.drawRoundRect(inner, w * 0.05f, w * 0.05f, fill)
        frame.color = ArtRenderer.INK; frame.strokeWidth = w * 0.008f
        c.drawRoundRect(inner, w * 0.05f, w * 0.05f, frame)
        ink.textSize = w * 0.085f
        c.drawText(title(b), w / 2f, h * 0.14f, ink)
        b.pages.firstOrNull()?.items?.firstOrNull()?.let { drawItem(c, it.ref, RectF(inner.left + w * 0.04f, inner.top + w * 0.04f, inner.right - w * 0.04f, inner.bottom - w * 0.04f)) }
    }

    fun drawPage(c: Canvas, b: BookDoc, index: Int, w: Float, h: Float) {
        val page = b.pages.getOrNull(index) ?: return
        fill.color = ArtRenderer.PAPER
        c.drawRect(0f, 0f, w, h, fill)
        // soft coloured border in the book's colour, like a picture-book frame
        frame.color = b.coverColor; frame.strokeWidth = w * 0.035f
        val m = w * 0.04f
        c.drawRoundRect(RectF(m, m, w - m, h - m), w * 0.05f, w * 0.05f, frame)
        val area = RectF(w * 0.1f, w * 0.1f, w * 0.9f, h - w * 0.16f)
        when (page.layout) {
            "two" -> {
                val mid = area.centerY()
                page.items.getOrNull(0)?.let { drawItem(c, it.ref, RectF(area.left, area.top, area.right, mid - w * 0.02f)) }
                page.items.getOrNull(1)?.let { drawItem(c, it.ref, RectF(area.left, mid + w * 0.02f, area.right, area.bottom)) }
            }
            else -> page.items.firstOrNull()?.let { drawItem(c, it.ref, area) }
        }
        ink.textSize = w * 0.05f
        c.drawText("${index + 1}", w / 2f, h - w * 0.07f, ink)
    }

    /** Draws a saved picture, character or scene snapshot fitted into [r]. */
    fun drawItem(c: Canvas, ref: String, r: RectF) {
        val tr = ToyRef.parse(ref) ?: return
        val s = c.save()
        c.clipRect(r)
        c.translate(r.left, r.top)
        when (tr.kind) {
            "art" -> {
                val doc = tr.revId?.let { g.library.artworkRevision(pid, tr.docId, it) } ?: g.library.loadArtwork(pid, tr.docId)?.second
                val art = doc?.let { g.content.art(it.templateId) }
                if (doc != null && art != null) ArtRenderer.drawFitted(c, art, PaintReducer.replay(doc.commands), r.width(), r.height(), 0.04f)
            }
            "chr" -> {
                val doc = tr.revId?.let { g.library.characterRevision(pid, tr.docId, it) } ?: g.library.loadCharacter(pid, tr.docId)?.second
                if (doc != null) {
                    val k = minOf(r.width(), r.height()) / 1024f
                    c.translate((r.width() - 1024f * k) / 2f, (r.height() - 1024f * k) / 2f); c.scale(k, k)
                    CharacterRenderer.draw(c, g.content, doc.recipe)
                }
            }
            "scene" -> {
                val scene = g.library.loadScene(pid, tr.docId)
                if (scene != null) {
                    val snap = SceneSnapshot(g, pid, scene)
                    val p = snap.painter
                    if (p != null) {
                        // fill the box (crop the sides) so the scene reads as a full illustration
                        val k = maxOf(r.width() / p.width, r.height() / p.height)
                        // centre the crop on where the child placed things
                        val cx = if (scene.objects.isEmpty()) 0.5f else scene.objects.map { it.transform.x }.average().toFloat()
                        val tx = (r.width() / 2f - cx * p.width * k).coerceIn(r.width() - p.width * k, 0f)
                        c.translate(tx, (r.height() - p.height * k) / 2f); c.scale(k, k)
                        snap.draw(c)
                    }
                }
            }
        }
        c.restoreToCount(s)
    }
}
