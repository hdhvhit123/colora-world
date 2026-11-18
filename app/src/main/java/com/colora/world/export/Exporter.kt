package com.colora.world.export

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import com.colora.world.AppGraph
import com.colora.world.data.ToyRef
import com.colora.world.engine.paint.PaintReducer
import com.colora.world.render.ArtRenderer
import com.colora.world.render.BookRenderer
import com.colora.world.render.CharacterRenderer
import com.colora.world.render.SceneSnapshot
import com.colora.world.render.ToyVisual
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Real file export (PNG, outline PNG, PDF, MP4). Every file is written to the
 * app cache first, verified by reading it back, and only then copied to the
 * destination the parent chose. Nothing here is simulated.
 */
class Exporter(private val g: AppGraph, private val pid: String, private val lang: String) {
    class Cancelled : Exception("cancelled")

    enum class Format(val mime: String, val ext: String) {
        PNG("image/png", "png"), OUTLINE("image/png", "png"), PDF_A4("application/pdf", "pdf"), PDF_LETTER("application/pdf", "pdf"), MP4("video/mp4", "mp4")
    }

    data class Options(val transparent: Boolean = false, val widthPx: Int = 2048, val landscape: Boolean = false, val low: Boolean = false, val seconds: Int = 8)

    /** Which formats apply to a reference (art/chr/scene/book). */
    fun formatsFor(ref: String): List<Format> = when (ToyRef.parse(ref)?.kind) {
        "art" -> listOf(Format.PNG, Format.OUTLINE, Format.PDF_A4, Format.PDF_LETTER, Format.MP4)
        "chr" -> listOf(Format.PNG, Format.PDF_A4, Format.PDF_LETTER, Format.MP4)
        "scene" -> listOf(Format.PNG, Format.PDF_A4, Format.PDF_LETTER, Format.MP4)
        "book" -> listOf(Format.PDF_A4, Format.PDF_LETTER)
        else -> emptyList()
    }

    fun fileName(ref: String, f: Format): String {
        val kind = ToyRef.parse(ref)?.kind ?: "file"
        val base = when (kind) { "art" -> "colora-picture"; "chr" -> "colora-friend"; "scene" -> "colora-world"; "book" -> "colora-book"; else -> "colora" }
        val suffix = if (f == Format.OUTLINE) "-outline" else ""
        return "$base$suffix-${System.currentTimeMillis() / 1000}.${f.ext}"
    }

    // ----------------------------------------------------------------- drawing
    private inner class Source(ref: String) {
        val r = ToyRef.parse(ref) ?: error("bad reference")
        val art = if (r.kind == "art") (r.revId?.let { g.library.artworkRevision(pid, r.docId, it) } ?: g.library.loadArtwork(pid, r.docId)?.second) else null
        val artModel = art?.let { g.content.art(it.templateId) }
        val paint = art?.let { PaintReducer.replay(it.commands) }
        val chr = if (r.kind == "chr") (r.revId?.let { g.library.characterRevision(pid, r.docId, it) } ?: g.library.loadCharacter(pid, r.docId)?.second) else null
        val scene = if (r.kind == "scene") g.library.loadScene(pid, r.docId)?.let { SceneSnapshot(g, pid, it) } else null
        val book = if (r.kind == "book") g.library.loadBook(pid, r.docId) else null
        init {
            when (r.kind) {
                "art" -> require(artModel != null && paint != null) { "picture not found" }
                "chr" -> require(chr != null) { "character not found" }
                "scene" -> require(scene?.painter != null) { "scene not found" }
                "book" -> require(book != null && book.pages.isNotEmpty()) { "book is empty" }
            }
        }

        /** Still image into a w×h box. */
        fun drawStill(c: Canvas, w: Float, h: Float, outline: Boolean) {
            when (r.kind) {
                "art" -> ArtRenderer.drawFitted(c, artModel!!, if (outline) null else paint, w, h, 0.05f, ArtRenderer.Options(drawPaint = !outline))
                "chr" -> { val k = minOf(w, h) / 1024f; val s = c.save(); c.translate((w - 1024 * k) / 2, (h - 1024 * k) / 2); c.scale(k, k); CharacterRenderer.draw(c, g.content, chr!!.recipe); c.restoreToCount(s) }
                "scene" -> { val p = scene!!.painter!!; val k = minOf(w / p.width, h / p.height); val s = c.save(); c.translate((w - p.width * k) / 2, (h - p.height * k) / 2); c.scale(k, k); scene.draw(c); c.restoreToCount(s) }
            }
        }
        val stillAspect: Float get() = if (r.kind == "scene") 2f else 1f
    }

    private fun check(cancel: AtomicBoolean) { if (cancel.get()) throw Cancelled() }

    // --------------------------------------------------------------------- PNG
    fun png(ref: String, outline: Boolean, o: Options, dst: File, cancel: AtomicBoolean = AtomicBoolean()) {
        val src = Source(ref)
        val w = o.widthPx
        val h = (w / src.stillAspect).toInt()
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        if (!o.transparent || outline) c.drawColor(Color.WHITE)
        src.drawStill(c, w.toFloat(), h.toFloat(), outline)
        check(cancel)
        FileOutputStream(dst).use { if (!bmp.compress(Bitmap.CompressFormat.PNG, 100, it)) error("PNG encoder failed") }
        bmp.recycle()
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(dst.path, opts)
        if (opts.outWidth != w || opts.outHeight != h) error("PNG verification failed")
    }

    // --------------------------------------------------------------------- PDF
    fun pdf(ref: String, letter: Boolean, dst: File, cancel: AtomicBoolean = AtomicBoolean(), progress: (Float) -> Unit = {}) {
        val src = Source(ref)
        // PostScript points: A4 595×842, US Letter 612×792
        val pw = if (letter) 612 else 595; val ph = if (letter) 792 else 842
        val doc = PdfDocument()
        try {
            fun page(n: Int, draw: (Canvas, Float, Float) -> Unit) {
                val p = doc.startPage(PdfDocument.PageInfo.Builder(pw, ph, n).create())
                draw(p.canvas, pw.toFloat(), ph.toFloat())
                doc.finishPage(p)
            }
            if (src.book != null) {
                val br = BookRenderer(g, pid, lang)
                val b = src.book
                page(1) { c, w, h -> br.drawCover(c, b, w, h) }
                b.pages.indices.forEach { i ->
                    check(cancel); progress((i + 1f) / (b.pages.size + 1))
                    page(i + 2) { c, w, h -> br.drawPage(c, b, i, w, h) }
                }
            } else {
                val landscape = src.stillAspect > 1.2f
                val p = doc.startPage(PdfDocument.PageInfo.Builder(if (landscape) ph else pw, if (landscape) pw else ph, 1).create())
                val c = p.canvas
                val w = c.width.toFloat(); val h = c.height.toFloat()
                val m = 36f // half-inch margin for home printers
                c.save(); c.translate(m, m); src.drawStill(c, w - 2 * m, h - 2 * m, false); c.restore()
                doc.finishPage(p)
            }
            check(cancel)
            FileOutputStream(dst).use { doc.writeTo(it) }
        } finally { doc.close() }
        val head = dst.inputStream().use { val b = ByteArray(5); it.read(b); String(b) }
        if (head != "%PDF-" || dst.length() < 200) error("PDF verification failed")
    }

    // --------------------------------------------------------------------- MP4
    companion object {
        const val FPS = 24
        private const val MIME = MediaFormat.MIMETYPE_VIDEO_AVC

        fun videoSize(landscape: Boolean) = if (landscape) 1280 to 720 else 720 to 1280

        /** True when the device has an H.264 encoder that accepts YUV420 flexible input at this size. */
        fun canEncode(landscape: Boolean): Boolean = runCatching {
            val (w, h) = videoSize(landscape)
            val f = MediaFormat.createVideoFormat(MIME, w, h).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                setInteger(MediaFormat.KEY_FRAME_RATE, FPS)
            }
            MediaCodecList(MediaCodecList.REGULAR_CODECS).findEncoderForFormat(f) != null
        }.getOrDefault(false)
    }

    /**
     * Encodes an animated clip: scenes pan slowly across the world with toys
     * moving; pictures and characters dance on paper. H.264 + MediaMuxer,
     * exact presentation timestamps (frame / 24 s).
     */
    fun mp4(ref: String, o: Options, dst: File, cancel: AtomicBoolean = AtomicBoolean(), progress: (Float) -> Unit = {}) {
        val src = Source(ref)
        val (w, h) = videoSize(o.landscape)
        val frames = o.seconds.coerceIn(6, 10) * FPS
        val toy: ToyVisual? = when (src.r.kind) {
            "art", "chr" -> ToyVisual.resolve(g, pid, ref, src.art?.templateId ?: src.chr!!.recipe.bodyId)
            else -> null
        }
        val format = MediaFormat.createVideoFormat(MIME, w, h).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            setInteger(MediaFormat.KEY_BIT_RATE, if (o.low) 2_000_000 else 5_000_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, FPS)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }
        val name = MediaCodecList(MediaCodecList.REGULAR_CODECS).findEncoderForFormat(format) ?: error("no video encoder")
        val codec = MediaCodec.createByCodecName(name)
        val muxer = MediaMuxer(dst.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val px = IntArray(w * h)
        var track = -1; var muxing = false
        val info = MediaCodec.BufferInfo()
        fun drain(end: Boolean) {
            while (true) {
                val idx = codec.dequeueOutputBuffer(info, if (end) 10_000 else 0)
                when {
                    idx == MediaCodec.INFO_TRY_AGAIN_LATER -> if (!end) return
                    idx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> { track = muxer.addTrack(codec.outputFormat); muxer.start(); muxing = true }
                    idx >= 0 -> {
                        val buf = codec.getOutputBuffer(idx)!!
                        if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) info.size = 0
                        if (info.size > 0 && muxing) { buf.position(info.offset); buf.limit(info.offset + info.size); muxer.writeSampleData(track, buf, info) }
                        codec.releaseOutputBuffer(idx, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return
                    }
                }
            }
        }
        try {
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()
            var f = 0
            while (f <= frames) {
                check(cancel)
                val inIdx = codec.dequeueInputBuffer(10_000)
                if (inIdx >= 0) {
                    val pts = f * 1_000_000L / FPS
                    if (f == frames) codec.queueInputBuffer(inIdx, 0, 0, pts, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    else {
                        drawFrame(canvas, src, toy, w.toFloat(), h.toFloat(), f / FPS.toFloat(), f / frames.toFloat())
                        bmp.getPixels(px, 0, w, 0, 0, w, h)
                        val img = codec.getInputImage(inIdx) ?: error("encoder has no image input")
                        writeYuv(px, w, h, img)
                        codec.queueInputBuffer(inIdx, 0, w * h * 3 / 2, pts, 0)
                    }
                    f++
                    progress(f / (frames + 1f))
                }
                drain(false)
            }
            drain(true)
        } finally {
            runCatching { codec.stop() }; codec.release()
            runCatching { if (muxing) muxer.stop() }; runCatching { muxer.release() }
            bmp.recycle()
        }
        verifyMp4(dst, frames)
    }

    private fun drawFrame(c: Canvas, src: Source, toy: ToyVisual?, w: Float, h: Float, t: Float, u: Float) {
        c.drawColor(ArtRenderer.PAPER)
        val scene = src.scene
        if (scene != null) {
            val p = scene.painter!!
            val k = h / p.height
            val travel = (p.width * k - w).coerceAtLeast(0f)
            // ease-in-out pan across the world
            val e = (1 - kotlin.math.cos(Math.PI * u).toFloat()) / 2f
            val s = c.save(); c.translate(-travel * e, 0f); c.scale(k, k); scene.draw(c, t); c.restoreToCount(s)
        } else if (toy != null) {
            val k = minOf(w, h) / 1024f * 0.86f
            val s = c.save()
            c.translate(w / 2f, h * 0.55f + 512f * k * 0.8f); c.scale(k, k); c.translate(-512f, -toy.footY)
            toy.draw(c, t, 1f, false, 7)
            c.restoreToCount(s)
        }
    }

    private fun writeYuv(argb: IntArray, w: Int, h: Int, img: android.media.Image) {
        val (yP, uP, vP) = img.planes.let { Triple(it[0], it[1], it[2]) }
        val yb = yP.buffer; val ub = uP.buffer; val vb = vP.buffer
        val yRow = yP.rowStride; val yPix = yP.pixelStride
        val uRow = uP.rowStride; val uPix = uP.pixelStride
        val vRow = vP.rowStride; val vPix = vP.pixelStride
        for (j in 0 until h) {
            val ro = j * w
            for (i in 0 until w) {
                val c = argb[ro + i]
                val r = (c shr 16) and 0xFF; val gg = (c shr 8) and 0xFF; val b = c and 0xFF
                yb.put(j * yRow + i * yPix, (((66 * r + 129 * gg + 25 * b + 128) shr 8) + 16).toByte())
                if (j and 1 == 0 && i and 1 == 0) {
                    val uu = ((-38 * r - 74 * gg + 112 * b + 128) shr 8) + 128
                    val vv = ((112 * r - 94 * gg - 18 * b + 128) shr 8) + 128
                    ub.put((j / 2) * uRow + (i / 2) * uPix, uu.coerceIn(0, 255).toByte())
                    vb.put((j / 2) * vRow + (i / 2) * vPix, vv.coerceIn(0, 255).toByte())
                }
            }
        }
    }

    private fun verifyMp4(f: File, frames: Int) {
        val ex = MediaExtractor()
        try {
            ex.setDataSource(f.path)
            val fmt = (0 until ex.trackCount).map { ex.getTrackFormat(it) }.firstOrNull { it.getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true } ?: error("MP4 has no video track")
            val dur = if (fmt.containsKey(MediaFormat.KEY_DURATION)) fmt.getLong(MediaFormat.KEY_DURATION) else 0L
            if (dur < (frames - 2) * 1_000_000L / FPS) error("MP4 verification failed (duration $dur)")
        } finally { ex.release() }
    }

}
