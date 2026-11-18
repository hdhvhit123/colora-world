package com.colora.world.engine

import com.colora.world.engine.paint.BrushId
import com.colora.world.engine.paint.Brushes
import com.colora.world.engine.paint.PaintCommand
import com.colora.world.engine.paint.PaintDocument
import com.colora.world.engine.paint.PaintReducer
import com.colora.world.engine.paint.Stroke
import com.colora.world.engine.paint.StickerPlacement
import com.colora.world.engine.content.ContentJson
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaintDocumentTest {
    private fun stroke(region: String?, brush: BrushId = BrushId.CRAYON, seed: Long = 7) =
        Stroke(brush, sizeUnits = 24f, color = 0xFF5B8DEF.toInt(), seed = seed, clipRegionId = region,
            points = listOf(100f, 100f, 1f, 0f, 140f, 120f, 1f, 16f, 180f, 160f, 0.8f, 16f))

    @Test fun undoRedoRestoresExactOrderIncludingFillAfterStroke() {
        val d = PaintDocument()
        d.push(PaintCommand.AddStroke(stroke("body")))
        d.push(PaintCommand.FillRegion("body", 0xFFFF0000.toInt()))
        d.push(PaintCommand.FillRegion("head", 0xFF00FF00.toInt()))
        val full = d.state()
        assertTrue(d.undo()); assertTrue(d.undo())
        assertEquals(emptyMap<String, Int>(), d.state().fills)
        assertEquals(1, d.state().strokes.size)
        assertTrue(d.redo()); assertTrue(d.redo())
        assertEquals(full, d.state())
        assertFalse(d.redo())
    }

    @Test fun newCommandAfterUndoDropsRedoBranch() {
        val d = PaintDocument()
        d.push(PaintCommand.FillRegion("a", 1)); d.push(PaintCommand.FillRegion("b", 2))
        d.undo()
        d.push(PaintCommand.FillRegion("c", 3))
        assertFalse(d.canRedo)
        assertEquals(setOf("a", "c"), d.state().fills.keys)
    }

    @Test fun undoKeepsAtLeastFiftyStepsAndSavingNeverNeedsHistory() {
        val d = PaintDocument(undoLimit = 60)
        repeat(120) { d.push(PaintCommand.FillRegion("r$it", it)) }
        var n = 0
        while (d.undo()) n++
        assertEquals(60, n)
        // the committed log still holds everything before the undo floor
        assertEquals(60, d.commands.size)
        assertEquals(60, d.state().fills.size)
    }

    @Test fun checkpointsGiveSameStateAsFullReplay() {
        val d = PaintDocument()
        repeat(100) { i -> d.push(if (i % 3 == 0) PaintCommand.AddStroke(stroke("r${i % 5}", seed = i.toLong())) else PaintCommand.FillRegion("r${i % 7}", i)) }
        val viaDoc = d.state()
        assertEquals(PaintReducer.replay(d.commands), viaDoc)
        d.undo(); d.undo(); d.redo()
        assertEquals(PaintReducer.replay(d.commands), d.state())
    }

    @Test fun commandLogRoundTripsThroughJson() {
        val cmds = listOf(
            PaintCommand.FillRegion("head", 0xFFFFA94D.toInt()),
            PaintCommand.AddStroke(stroke("body", BrushId.RAINBOW, 42)),
            PaintCommand.AddSticker(StickerPlacement("s1", "sticker.star", 300f, 400f, 1.2f, 15f, "body")),
            PaintCommand.TransformSticker("s1", 320f, 410f, 1.4f, 20f),
            PaintCommand.ClearRegion("head"),
            PaintCommand.ClearPage,
        )
        val ser = ListSerializer(serializer<PaintCommand>())
        val json = ContentJson.encodeToString(ser, cmds)
        val back = ContentJson.decodeFromString(ser, json)
        assertEquals(cmds, back)
        assertEquals(PaintReducer.replay(cmds), PaintReducer.replay(back))
    }

    @Test fun clearRegionHidesOnlyThatRegionsStrokes() {
        val s = PaintReducer.replay(listOf(
            PaintCommand.AddStroke(stroke("body")), PaintCommand.AddStroke(stroke("head")),
            PaintCommand.FillRegion("body", 1), PaintCommand.ClearRegion("body"),
        ))
        assertEquals(listOf("head"), s.visibleStrokes.map { it.clipRegionId })
        assertFalse("body" in s.fills)
    }

    @Test fun fillAfterEraserRestoresColourInThatRegion() {
        val s = PaintReducer.replay(listOf(
            PaintCommand.FillRegion("body", 1),
            PaintCommand.AddStroke(stroke("body", BrushId.ERASER)),
            PaintCommand.AddStroke(stroke("head", BrushId.ERASER)),
            PaintCommand.FillRegion("body", 2),
        ))
        assertEquals(listOf("head"), s.visibleStrokes.map { it.clipRegionId })
    }

    @Test fun rainbowBrushIsDeterministicPerSeed() {
        val a = (0..20).map { Brushes.rainbowColorAt(it * 13f, 5) }
        val b = (0..20).map { Brushes.rainbowColorAt(it * 13f, 5) }
        val c = (0..20).map { Brushes.rainbowColorAt(it * 13f, 6) }
        assertEquals(a, b)
        assertTrue(a != c)
    }

    @Test(expected = IllegalArgumentException::class)
    fun malformedPointsRejected() {
        Stroke(BrushId.CRAYON, sizeUnits = 10f, color = 0, seed = 1, points = listOf(1f, 2f, 3f))
    }
}
