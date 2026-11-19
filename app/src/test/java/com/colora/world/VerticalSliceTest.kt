package com.colora.world

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.colora.world.data.Library
import com.colora.world.data.ToyRef
import com.colora.world.engine.paint.PaintCommand
import com.colora.world.storage.DocKind
import com.colora.world.ui.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.math.max
import kotlin.math.min

/**
 * End-to-end vertical slice on the real app UI (Robolectric, native graphics):
 * pick → colour (real touch input) → save → toy keeps strokes → place in world →
 * a fresh Library (as after process death) still has everything.
 */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class VerticalSliceTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun canvasPoint(designX: Float, designY: Float, cb: List<Float>): Offset {
        val node = rule.onNodeWithContentDescription("canvas").fetchSemanticsNode()
        val w = node.size.width.toFloat(); val h = node.size.height.toFloat()
        val size = max(cb[2] - cb[0], cb[3] - cb[1]) * 1.08f
        val k = min(w, h) / size
        return Offset((designX - (cb[0] + cb[2]) / 2f) * k + w / 2f, (designY - (cb[1] + cb[3]) / 2f) * k + h / 2f)
    }

    @Test fun pickColourSaveToyWorldRestart() {
        org.robolectric.shadows.ShadowLog.stream = System.out
        val s = rule.appState()
        rule.waitFor { !s.loading }
        rule.mainClock.autoAdvance = false // the world and toys animate forever
        rule.runOnUiThread { rule.activity.graph.settings.update { it.copy(onboarded = true) }; s.home() }
        rule.settle(1500); rule.shot("01_home")
        rule.runOnUiThread { s.go(Screen.Topics) }; rule.settle(1200); rule.shot("02_topics")
        rule.runOnUiThread { s.go(Screen.Templates("animals")) }; rule.settle(1500); rule.shot("03_templates")
        rule.runOnUiThread { s.go(Screen.Studio("animals.cat")) }
        rule.waitFor { runCatching { rule.onNodeWithContentDescription("canvas").fetchSemanticsNode() }.isSuccess }
        rule.settle(800); rule.shot("04_studio_blank")

        val g = rule.activity.graph
        val art = g.content.art("animals.cat")!!
        val cb = art.def.contentBounds
        // fill several regions with different colours by real taps
        val colours = listOf("#FFA94D", "#FFE066", "#F25C54", "#42C8BD", "#9B6BDE", "#FFA8C5", "#4FB477")
        val targets = art.geometry.hitOrder.filter { r -> val b = r.bounds; art.geometry.hitTest((b.left + b.right) / 2, (b.top + b.bottom) / 2) == r.def.id }.take(7)
        targets.forEachIndexed { i, r ->
            rule.onNodeWithContentDescription(colours[i % colours.size]).performClick()
            val b = r.bounds
            rule.onNodeWithContentDescription("canvas").performTouchInput { click(canvasPoint((b.left + b.right) / 2, (b.top + b.bottom) / 2, cb)) }
            rule.settle(150)
        }
        // a crayon stroke across the biggest region, clipped to it
        rule.onNodeWithContentDescription(com.colora.world.ui.Strings.get(s.lang, "tool.crayon")).performClick()
        rule.onNodeWithContentDescription("#5B8DEF").performClick()
        val big = art.geometry.regions.maxBy { it.area }
        val bb = big.bounds
        rule.onNodeWithContentDescription("canvas").performTouchInput {
            val a = canvasPoint(bb.left + bb.width * 0.2f, bb.top + bb.height * 0.5f, cb)
            val z = canvasPoint(bb.right - bb.width * 0.2f, bb.top + bb.height * 0.6f, cb)
            down(a); for (i in 1..20) moveTo(Offset(a.x + (z.x - a.x) * i / 20f, a.y + (z.y - a.y) * i / 20f + kotlin.math.sin(i / 3f) * 30f)); up()
        }
        rule.settle(1500)
        rule.shot("05_studio_coloured")

        val pid = s.pid
        val docs = g.library.list(pid, DocKind.ARTWORK)
        assertEquals(1, docs.size)
        val saved = g.library.loadArtwork(pid, docs[0].id)!!.second
        assertTrue("fills saved", saved.commands.count { it is PaintCommand.FillRegion } >= 5)
        assertTrue("stroke saved", saved.commands.any { it is PaintCommand.AddStroke })

        rule.onNodeWithContentDescription(com.colora.world.ui.Strings.get(s.lang, "studio.play"), substring = true, useUnmergedTree = true)
        rule.runOnUiThread { s.replace(Screen.Reveal(docs[0].id)) }
        rule.settle(2500); rule.shot("06_toy_reveal")
        val rev = g.library.store.pointer(pid, docs[0].id)!!.currentRevisionId
        rule.runOnUiThread { s.replace(Screen.WorldPicker(ToyRef.art(docs[0].id, rev).toString(), "animals.cat")) }
        rule.settle(1500); rule.shot("07_world_picker")
        // open the garden through the picker's own flow
        val sceneDoc = com.colora.world.engine.world.SceneDocument(id = Library.newDocId("s"), profileId = pid, worldId = "garden")
        g.library.saveScene(pid, sceneDoc)
        rule.runOnUiThread { s.replace(Screen.World(sceneDoc.id, ToyRef.art(docs[0].id, rev).toString(), "animals.cat")) }
        rule.settle(12000); rule.shot("08_world_with_toy")

        // "process death": a brand-new Library over the same files
        Autosave.flushAll(); rule.settle(1000)
        val fresh = Library(rule.activity.filesDir, g.content)
        fresh.recover()
        val scene = fresh.loadScene(pid, sceneDoc.id)!!
        assertEquals(1, scene.objects.size)
        val ref = ToyRef.parse(scene.objects[0].artworkRevisionId)!!
        val toyDoc = fresh.artworkRevision(pid, ref.docId, ref.revId!!)!!
        assertEquals(saved.commands, toyDoc.commands) // the toy replays exactly the child's commands
        rule.runOnUiThread { s.home() }; rule.settle(1500); rule.shot("09_home_after")
        rule.runOnUiThread { s.go(Screen.World(sceneDoc.id)) }; rule.settle(3000); rule.shot("10_world_reopened")
    }
}
