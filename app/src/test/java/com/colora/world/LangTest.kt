package com.colora.world
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.colora.world.ui.Lang
import com.colora.world.ui.Strings
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Locale

// All language checks run in ONE activity. Each test class also runs in its own JVM (forkEvery = 1):
// a second app/activity created later in a shared Robolectric sandbox did not reliably receive settings
// recompositions in this harness, which gave false failures.

private fun com.colora.world.Rule.present(text: String) =
    onAllNodes(hasText(text, substring = true) or hasContentDescription(text, substring = true), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

/** Polls up to ~6 s so a slow recomposition on a loaded machine does not read as a failure. */
private fun com.colora.world.Rule.has(text: String): Boolean { repeat(30) { if (present(text)) return true; settle(200) }; return present(text) }

/** Language switching, following the device, and every shipped language on the home screen. */
@RunWith(AndroidJUnit4::class) @Config(qualifiers = "w360dp-h640dp-xhdpi")
class LangTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val original = Locale.getDefault()
    @After fun restore() { Locale.setDefault(original) }

    @Test fun languages() {
        val s = rule.appState(); rule.waitFor { !s.loading }; rule.mainClock.autoAdvance = false
        val settings = rule.activity.graph.settings

        // 1. an explicit choice in the adult area updates the running UI immediately
        rule.runOnUiThread { settings.update { it.copy(onboarded = true, langPref = "vi") }; s.home() }
        rule.settle(1500)
        assertTrue(rule.has("Tô màu"))
        rule.runOnUiThread { settings.update { it.copy(langPref = "en") } }
        assertTrue(rule.has("Colour"))

        // 2. "Same as this device" follows the phone language, falling back to English
        rule.runOnUiThread { settings.update { it.copy(langPref = Lang.SYSTEM) } }
        for ((tag, code) in listOf("ja-JP" to "ja", "de-DE" to "de", "pt-PT" to "pt", "in-ID" to "id", "zh-Hans-CN" to "zh", "sw-KE" to "en")) {
            Locale.setDefault(Locale.forLanguageTag(tag))
            rule.runOnUiThread { settings.refreshLocale() }
            rule.settle(300)
            assertEquals(tag, code, settings.values.value.lang)
            val label = Strings.get(code, "home.color")
            assertTrue("$tag shows $label", rule.has(label))
        }
        rule.runOnUiThread { settings.update { it.copy(langPref = "ko") } }   // explicit choice wins
        assertEquals("ko", settings.values.value.lang)
        Locale.setDefault(original)

        // 3. home screen in every shipped language, for a visual check of Thai, Devanagari and CJK glyphs
        assertEquals(13, Lang.available().size)
        for ((code, _) in Lang.available()) {
            rule.runOnUiThread { settings.update { it.copy(langPref = code) } }
            assertTrue(code, rule.has(Strings.get(code, "home.hello")))
            rule.settle(300)
            rule.shot("lang_home_$code")
        }
    }
}
