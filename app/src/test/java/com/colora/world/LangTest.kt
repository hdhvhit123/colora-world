package com.colora.world
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Switching the language in settings updates the running UI immediately. */
@RunWith(AndroidJUnit4::class)
class LangTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun switchingLanguageUpdatesUi() {
        val s = rule.appState(); rule.waitFor { !s.loading }; rule.mainClock.autoAdvance = false
        rule.runOnUiThread { rule.activity.graph.settings.update { it.copy(onboarded = true, langPref = "vi") }; s.home() }
        rule.settle(1500)
        assertTrue(rule.onAllNodesWithText("Tô màu", substring = true).fetchSemanticsNodes().isNotEmpty())
        rule.runOnUiThread { rule.activity.graph.settings.update { it.copy(langPref = "en") } }
        rule.settle(800)
        assertTrue(rule.onAllNodesWithText("Colour", substring = true).fetchSemanticsNodes().isNotEmpty())
    }
}
