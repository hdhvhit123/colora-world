package com.colora.world

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Renders the Google Play hi-res icon (512×512, opaque) from the real adaptive launcher layers, so the
 * store icon is exactly the app icon. Play applies its own mask, so the square is full-bleed; the outer
 * 11 dp of the 108 dp adaptive canvas (outside the area any launcher mask shows) is cropped away.
 */
@RunWith(AndroidJUnit4::class)
class StoreIconTest {
    @Test fun playIcon() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val full = 108f; val keep = 86f                 // dp of the adaptive canvas kept in the store icon
        val out = 512; val big = Math.round(out * full / keep)
        val layers = Bitmap.createBitmap(big, big, Bitmap.Config.ARGB_8888)
        val c = Canvas(layers)
        for (id in listOf(R.drawable.ic_launcher_background, R.drawable.ic_launcher_foreground)) {
            ContextCompat.getDrawable(ctx, id)!!.apply { setBounds(0, 0, big, big); draw(c) }
        }
        val off = (big - out) / 2
        val icon = Bitmap.createBitmap(layers, off, off, out, out).copy(Bitmap.Config.ARGB_8888, false)
        assertEquals(512, icon.width)
        val dir = File(File(System.getProperty("colora.shots") ?: "build/shots").parentFile, "play-store/raw").apply { mkdirs() }
        File(dir, "icon_512.png").outputStream().use { icon.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
