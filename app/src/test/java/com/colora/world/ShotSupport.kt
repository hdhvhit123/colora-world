package com.colora.world

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.captureToImage
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.colora.world.ui.AppState
import org.robolectric.shadows.ShadowLooper
import java.io.File

typealias Rule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

fun Rule.appState(): AppState = ViewModelProvider(activity)[AppStateHolder::class.java].state

/** Lets background IO (content loading, saving) finish and the UI settle. */
fun Rule.settle(ms: Long = 600) {
    val end = System.currentTimeMillis() + ms
    while (System.currentTimeMillis() < end) {
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(32))
        mainClock.advanceTimeByFrame()
        org.robolectric.Shadows.shadowOf(Autosave.looper).idleFor(java.time.Duration.ofMillis(200))
        Thread.sleep(40)
    }
    mainClock.advanceTimeBy(400)
    waitForIdle()
}

fun Rule.waitFor(timeoutMs: Long = 8000, cond: () -> Boolean) {
    val end = System.currentTimeMillis() + timeoutMs
    while (!cond() && System.currentTimeMillis() < end) { ShadowLooper.idleMainLooper(); org.robolectric.Shadows.shadowOf(Autosave.looper).idle(); Thread.sleep(30); mainClock.advanceTimeBy(50) }
}

/** Captures the real app UI rendered by Robolectric's native graphics into screenshots/<name>.png. */
fun Rule.shot(name: String): Bitmap {
    waitForIdle()
    val v = activity.window.decorView
    val bmp = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
    runOnUiThread { v.draw(android.graphics.Canvas(bmp)) }
    val dir = File(System.getProperty("colora.shots") ?: "build/shots").apply { mkdirs() }
    File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    return bmp
}
