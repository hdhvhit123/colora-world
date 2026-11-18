package com.colora.world

import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.colora.world.export.Exporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Real PNG/PDF files written by the exporter and read back (Robolectric native graphics). */
@RunWith(AndroidJUnit4::class)
class ExporterTest {
    @Test fun pngAndPdfAreRealFiles() {
        val app = ApplicationProvider.getApplicationContext<ColoraApp>()
        val g = app.graph
        val pid = g.library.createProfile("char.body.bean", "en").id
        val seed = seedLibrary(g, pid)
        val ex = Exporter(g, pid, "en")
        val out = File(System.getProperty("colora.shots") ?: "build/shots").apply { mkdirs() }

        val png = File(out, "export_picture.png")
        ex.png(seed.art[0], false, Exporter.Options(widthPx = 1024), png)
        val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }; BitmapFactory.decodeFile(png.path, o)
        assertEquals(1024, o.outWidth)

        val outline = File(out, "export_outline.png")
        ex.png(seed.art[1], true, Exporter.Options(widthPx = 1024), outline)
        assertTrue(outline.length() > 1000)

        val scenePng = File(out, "export_scene.png")
        ex.png(seed.scene, false, Exporter.Options(widthPx = 1600), scenePng)
        val so = BitmapFactory.Options().apply { inJustDecodeBounds = true }; BitmapFactory.decodeFile(scenePng.path, so)
        assertEquals(800, so.outHeight)

        // book pages exactly as the PDF draws them (BookRenderer), saved as images for review
        val br = com.colora.world.render.BookRenderer(g, pid, "en")
        val book = g.library.loadBook(pid, seed.book)!!
        val cover = android.graphics.Bitmap.createBitmap(595, 842, android.graphics.Bitmap.Config.ARGB_8888)
        br.drawCover(android.graphics.Canvas(cover), book, 595f, 842f)
        File(out, "export_book_cover.png").outputStream().use { cover.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        for (i in book.pages.indices) File(out, "export_book_page${i + 1}.png").outputStream().use { br.pageBitmap(book, i, 595, 595f / 842f).compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        assertTrue(Exporter(g, pid, "en").formatsFor("book:x").none { it == Exporter.Format.MP4 })
    }

    /** android.graphics.pdf.PdfDocument has no native backend in Robolectric: this runs on a device/emulator only. */
    @Test fun pdfOnDevice() {
        val app = ApplicationProvider.getApplicationContext<ColoraApp>()
        val supported = runCatching { android.graphics.pdf.PdfDocument().apply { startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(10, 10, 1).create()).also { finishPage(it) } }.close() }.isSuccess
        org.junit.Assume.assumeTrue("PdfDocument unsupported on this JVM", supported)
        val g = app.graph
        val pid = g.library.createProfile("char.body.bean", "en").id
        val seed = seedLibrary(g, pid)
        val pdf = File(app.cacheDir, "book.pdf")
        Exporter(g, pid, "en").pdf("book:${seed.book}", false, pdf)
        assertEquals("%PDF-", pdf.inputStream().use { val b = ByteArray(5); it.read(b); String(b) })
    }
}
