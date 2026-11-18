package com.colora.world.engine

import com.colora.world.engine.lab.ColorMixer
import com.colora.world.engine.prompts.CreativePrompt
import com.colora.world.engine.prompts.Suggestions
import com.colora.world.engine.session.BudgetState
import com.colora.world.engine.session.SessionBudget
import com.colora.world.engine.story.Matcher
import com.colora.world.engine.story.SceneEvent
import com.colora.world.engine.story.StoryDef
import com.colora.world.engine.story.StoryEvent
import com.colora.world.engine.story.StoryProgress
import com.colora.world.engine.story.StoryRuntime
import com.colora.world.engine.story.StoryStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmallSystemsTest {
    @Test fun colourLabClassicPairs() {
        assertEquals("orange", ColorMixer.family(ColorMixer.mix(listOf(ColorMixer.RED, ColorMixer.YELLOW))))
        assertEquals("green", ColorMixer.family(ColorMixer.mix(listOf(ColorMixer.YELLOW, ColorMixer.BLUE))))
        assertEquals("purple", ColorMixer.family(ColorMixer.mix(listOf(ColorMixer.RED, ColorMixer.BLUE))))
        assertEquals("brown", ColorMixer.family(ColorMixer.mix(listOf(ColorMixer.RED, ColorMixer.YELLOW, ColorMixer.BLUE))))
        // deterministic and order independent
        assertEquals(ColorMixer.mix(listOf(ColorMixer.RED, ColorMixer.BLUE)), ColorMixer.mix(listOf(ColorMixer.BLUE, ColorMixer.RED)))
        // white tints lighten without changing family
        assertEquals("pink", ColorMixer.family(ColorMixer.mix(listOf(ColorMixer.RED), white = 0.6f)))
    }

    private val st01 = StoryDef(
        "ST-01", "story.st01.title", "garden",
        listOf(
            StoryStep("ride", "s1", StoryEvent.ATTACH, Matcher(listOf("animals.rabbit"), capability = "passenger"), Matcher(listOf("vehicles.car"), capability = "rideable")),
            StoryStep("arrive", "s2", StoryEvent.PLACE, Matcher(listOf("family.picnic"), listOf("prop.picnic_mat"))),
            StoryStep("food", "s3", StoryEvent.ATTACH, Matcher(capability = "table_item"), Matcher(listOf("family.picnic"), listOf("prop.picnic_mat")), count = 2),
        ), "book_page", listOf("animals.rabbit"),
    )

    @Test fun storyEventsAreIdempotentAndAcceptOldArtwork() {
        var p = StoryProgress("ST-01", "a1", "scene")
        val ride = SceneEvent(StoryEvent.ATTACH, "bun", "animals.cat", setOf("passenger"), "vehicles.bus", setOf("rideable"))
        p = StoryRuntime.onEvent(st01, p, ride)      // any passenger on any rideable counts
        assertEquals(1, p.stepIndex)
        p = StoryRuntime.onEvent(st01, p, SceneEvent(StoryEvent.PLACE, "mat", "prop.picnic_mat", emptySet()))
        val apple = SceneEvent(StoryEvent.ATTACH, "apple1", "food.apple", setOf("table_item"), "prop.picnic_mat", emptySet())
        p = StoryRuntime.onEvent(st01, p, apple)
        p = StoryRuntime.onEvent(st01, p, apple)     // duplicate: still 1
        assertFalse(p.completed)
        p = StoryRuntime.onEvent(st01, p, apple.copy(objectId = "bread1", templateId = "food.bread"))
        assertTrue(p.completed)
    }

    @Test fun skippingStillAdvancesAndCompletes() {
        var p = StoryProgress("ST-01", "a1", "scene")
        repeat(3) { p = StoryRuntime.skip(st01, p) }
        assertTrue(p.completed)
        assertEquals(3, p.skipped.size)
    }

    @Test fun budgetCountsOnlyForegroundAndSurvivesRestart() {
        val b = SessionBudget(BudgetState("s", limitMinutes = 5))
        b.resume(0); b.pause(120_000)           // 2 min played
        b.resume(10_000_000)                    // a long time in background is not counted
        assertEquals(120_000 + 60_000L, b.consumed(10_060_000))
        val restored = SessionBudget(b.checkpoint(10_060_000))
        restored.resume(0)
        assertEquals(SessionBudget.Signal.NONE, restored.poll(59_000))
        assertEquals(SessionBudget.Signal.WARN, restored.poll(61_000))
        assertEquals(SessionBudget.Signal.NONE, restored.poll(62_000))   // warns once
        assertEquals(SessionBudget.Signal.END, restored.poll(120_000))
        restored.extend(5)
        assertEquals(SessionBudget.Signal.NONE, restored.poll(120_000))
    }

    @Test fun unlimitedBudgetNeverEnds() {
        val b = SessionBudget(BudgetState("s", limitMinutes = null))
        b.resume(0)
        assertNull(b.remainingMs(10_000_000))
        assertEquals(SessionBudget.Signal.NONE, b.poll(10_000_000))
    }

    @Test fun suggestionsAreDeterministicAndNeverLocked() {
        val prompts = (1..30).map { CreativePrompt("CR-%02d".format(it), "p$it", listOf("animals.t$it"), "alt$it", listOf("animals.cat")) }
        val a = Suggestions.shuffled(prompts, 99).map { it.id }
        assertEquals(a, Suggestions.shuffled(prompts, 99).map { it.id })
        assertEquals(30, a.toSet().size)
        val ideas = Suggestions.nextIdeas(prompts, 99, accessible = { it == "animals.cat" }, favouriteTopics = emptyList(), topicOf = { "animals" })
        assertEquals(3, ideas.size)
        assertTrue(ideas.all { it.templateId == "animals.cat" && it.textKey.startsWith("alt") })
    }
}
