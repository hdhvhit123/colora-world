package com.colora.world

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.colora.world.ui.Screen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Renders every screen of the real app with realistic saved work, phone portrait. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class AllScreensTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun everyScreenPortrait() = runAll(rule, "p")
}

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w891dp-h411dp-xxhdpi")
class AllScreensLandscapeTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun everyScreenLandscape() = runAll(rule, "l", short = true)
}

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w1280dp-h800dp-mdpi")
class AllScreensTabletTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun everyScreenTablet() = runAll(rule, "t", short = true)
}

fun runAll(rule: com.colora.world.Rule, tag: String, short: Boolean = false) {
    val s = rule.appState()
    rule.waitFor { !s.loading }
    rule.mainClock.autoAdvance = false
    val g = rule.activity.graph
    rule.runOnUiThread { g.settings.update { it.copy(onboarded = true) } }
    val seed = seedLibrary(g, s.pid)
    fun at(scr: Screen, name: String, ms: Long = 1500) { rule.runOnUiThread { s.home(); if (scr != Screen.Home) s.go(scr) }; rule.settle(ms); rule.shot("${tag}_$name") }
    at(Screen.Home, "home")
    at(Screen.Studio("animals.cat", docId = com.colora.world.data.ToyRef.parse(seed.art[0])!!.docId), "studio", 2000)
    at(Screen.World(com.colora.world.data.ToyRef.parse(seed.scene)!!.docId), "world", 4000)
    at(Screen.Collection, "collection", 2500)
    at(Screen.Book(seed.book), "book", 3000)
    at(Screen.Lab, "lab")
    at(Screen.Builder(com.colora.world.data.ToyRef.parse(seed.chr)!!.docId), "builder", 2000)
    if (short) return
    at(Screen.FirstRun, "first_run")
    at(Screen.Topics, "topics")
    at(Screen.Templates("dinosaurs"), "templates")
    at(Screen.Stories, "stories")
    at(Screen.Ideas, "ideas")
    // mix two drops in the lab
    rule.runOnUiThread { s.home(); s.go(Screen.Lab) }; rule.settle(800)
    rule.onAllNodesWithContentDescription(com.colora.world.ui.Strings.get(s.lang, "color.red"))[0].performClick()
    rule.onAllNodesWithContentDescription(com.colora.world.ui.Strings.get(s.lang, "color.blue"))[0].performClick()
    rule.settle(600); rule.shot("${tag}_lab_mixed")
    at(Screen.ParentGate(Screen.Parent), "parent_gate")
    rule.runOnUiThread { s.parentTouch() }
    at(Screen.Parent, "parent", 2000)
    rule.runOnUiThread { s.parentTouch() }
    at(Screen.Export(seed.art[1].substringBeforeLast(':')), "export", 3000)
    at(Screen.Rest, "rest")
}
