package com.colora.world

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.colora.world.data.Library
import com.colora.world.data.ToyRef
import com.colora.world.engine.world.SceneDocument
import com.colora.world.engine.world.SceneTransform
import com.colora.world.engine.world.ToyInstance
import com.colora.world.ui.Screen
import com.colora.world.ui.Strings
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

/**
 * Real app captures for the Google Play listing (9:16 phone, 16:10 tablets),
 * in English and Vietnamese. Run: ./gradlew :app:testOfflineFullDebugUnitTest --tests '*StoreShots*'
 * Output: play-store/raw/<device>/<lang>/NN_name.png
 */
@RunWith(AndroidJUnit4::class) @Config(qualifiers = "w411dp-h731dp-xxhdpi")
class StoreShotsPhoneTest { @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun en() = storeShots(rule, "phone", "en", wide = false)
    @Test fun vi() = storeShots(rule, "phone", "vi", wide = false) }

@RunWith(AndroidJUnit4::class) @Config(qualifiers = "w1280dp-h800dp-hdpi")
class StoreShotsTablet10Test { @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun en() = storeShots(rule, "tablet10", "en", wide = true)
    @Test fun vi() = storeShots(rule, "tablet10", "vi", wide = true) }

@RunWith(AndroidJUnit4::class) @Config(qualifiers = "w960dp-h600dp-hdpi")
class StoreShotsTablet7Test { @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun en() = storeShots(rule, "tablet7", "en", wide = true)
    @Test fun vi() = storeShots(rule, "tablet7", "vi", wide = true) }

fun storeShots(rule: com.colora.world.Rule, device: String, lang: String, wide: Boolean) {
    val s = rule.appState()
    rule.waitFor { !s.loading }
    rule.mainClock.autoAdvance = false
    val g = rule.activity.graph
    rule.runOnUiThread { g.settings.update { it.copy(onboarded = true, langPref = lang) } }
    val seed = seedLibrary(g, s.pid)
    // a lively scene placed inside the first camera view of this screen shape
    val xs = if (wide) listOf(0.12f, 0.30f, 0.48f, 0.62f, 0.78f, 0.40f) else listOf(0.06f, 0.17f, 0.30f, 0.41f, 0.24f, 0.36f)
    val sid = Library.newDocId("s")
    val car = g.content.art("vehicles.car")!!
    val carDoc = com.colora.world.data.ArtworkDoc(templateId = "vehicles.car", commands = car.parts.flatMap { it.regions }.map { com.colora.world.engine.paint.PaintCommand.FillRegion(it.id, it.sample) })
    val carId = Library.newDocId("a"); val carRev = g.library.saveArtwork(s.pid, carId, carDoc).revisionId
    g.library.saveScene(s.pid, SceneDocument(id = sid, profileId = s.pid, worldId = "garden", objects = listOf(
        ToyInstance("o1", seed.art[0], "animals.cat", SceneTransform(xs[0], 0.90f, 1.15f), 0),
        ToyInstance("o2", ToyRef.art(carId, carRev).toString(), "vehicles.car", SceneTransform(xs[1], 0.93f, 1.15f), 1),
        ToyInstance("o3", seed.art[1], "dinosaurs.spinosaurus", SceneTransform(xs[2], 0.95f, 1.2f), 2),
        ToyInstance("o4", seed.chr, "char.body.bean", SceneTransform(xs[3], 0.92f, 1.0f), 3),
        ToyInstance("o5", seed.art[2], "animals.butterfly", SceneTransform(xs[4], 0.55f, 0.8f), 4),
    )))
    val dir = File(File(System.getProperty("colora.shots") ?: "build/shots").parentFile, "play-store/raw/$device/$lang").apply { mkdirs() }
    fun shot(name: String) {
        val b = rule.shot("store_tmp")
        File(dir, "$name.png").outputStream().use { b.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    }
    fun at(scr: Screen, name: String, ms: Long = 1800) { rule.runOnUiThread { s.home(); if (scr != Screen.Home) s.go(scr) }; rule.settle(ms); shot(name) }
    val spino = ToyRef.parse(seed.art[1])!!.docId
    at(Screen.Home, "01_home")
    at(Screen.Studio("dinosaurs.spinosaurus", docId = spino), "02_studio", 2200)
    at(Screen.Reveal(spino), "03_toy", 2500)
    at(Screen.World(sid), "04_world", 4500)
    at(Screen.Stories, "05_stories", 2500)
    at(Screen.Book(seed.book), "06_book", 3000)
    at(Screen.Builder(ToyRef.parse(seed.chr)!!.docId), "07_friend", 2200)
    rule.runOnUiThread { s.home(); s.go(Screen.Lab) }; rule.settle(800)
    rule.onAllNodesWithContentDescription(Strings.get(lang, "color.yellow"))[0].performClick()
    rule.onAllNodesWithContentDescription(Strings.get(lang, "color.blue"))[0].performClick()
    rule.settle(700); shot("08_lab")
    File(System.getProperty("colora.shots") ?: "build/shots", "store_tmp.png").delete()
}
