package com.colora.world.engine

import com.colora.world.engine.world.SceneDocument
import com.colora.world.engine.world.SceneEditor
import com.colora.world.engine.world.SceneOp
import com.colora.world.engine.world.SceneRuleException
import com.colora.world.engine.world.SceneTransform
import com.colora.world.engine.world.ToyAnchor
import com.colora.world.engine.world.ToyCatalog
import com.colora.world.engine.world.ToyInstance
import com.colora.world.engine.world.WaypointMover
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SceneEditorTest {
    private val catalog = object : ToyCatalog {
        val caps = mapOf(
            "vehicles.car" to setOf("rideable"), "animals.rabbit" to setOf("passenger", "carryable"),
            "animals.cat" to setOf("passenger", "carryable"), "space.rocket" to setOf("rideable", "flying"),
            "machines.robot" to setOf("passenger", "carrier", "carryable"), "flowers.tulip" to setOf("plantable", "waterable"),
            "machines.excavator" to setOf("carrier"), "prop.soil_block" to setOf("carryable"),
        )
        override fun capabilities(templateId: String) = caps[templateId].orEmpty()
        override fun anchors(templateId: String) = when (templateId) {
            "vehicles.car" -> listOf(ToyAnchor("seat_front", "passenger", 0.05f, -0.06f), ToyAnchor("seat_back", "passenger", -0.04f, -0.06f))
            "space.rocket" -> listOf(ToyAnchor("cockpit", "passenger", 0f, -0.05f))
            "machines.excavator" -> listOf(ToyAnchor("bucket", "carry", -0.15f, 0.02f))
            "animals.rabbit" -> listOf(ToyAnchor("hold", "carry", 0f, -0.02f))
            else -> emptyList()
        }
    }

    private fun toy(id: String, t: String, x: Float = 0.5f, y: Float = 0.7f) = ToyInstance(id, "rev-$id", t, SceneTransform(x, y), 0)
    private fun editor() = SceneEditor(SceneDocument(id = "s", profileId = "p", worldId = "garden"), catalog)

    @Test fun rabbitRidesCarAndFollowsIt() {
        val e = editor()
        e.apply(SceneOp.Add(toy("car", "vehicles.car", 0.3f, 0.7f)))
        e.apply(SceneOp.Add(toy("bun", "animals.rabbit", 0.8f, 0.7f)))
        e.apply(SceneOp.Attach("bun", "car", "seat_front"))
        e.apply(SceneOp.Move("car", 0.6f, 0.72f))
        val bun = e.doc.obj("bun")!!
        assertEquals(0.65f, bun.transform.x, 1e-4f)
        assertEquals(0.66f, bun.transform.y, 1e-4f)
        assertTrue(bun.zOrder > e.doc.obj("car")!!.zOrder)
    }

    @Test fun incompatibleAndOccupiedAnchorsRejected() {
        val e = editor()
        e.apply(SceneOp.Add(toy("car", "vehicles.car")))
        e.apply(SceneOp.Add(toy("tulip", "flowers.tulip")))
        e.apply(SceneOp.Add(toy("cat", "animals.cat")))
        e.apply(SceneOp.Add(toy("bun", "animals.rabbit")))
        assertThrows { e.apply(SceneOp.Attach("tulip", "car", "seat_front")) }
        e.apply(SceneOp.Attach("cat", "car", "seat_front"))
        assertThrows { e.apply(SceneOp.Attach("bun", "car", "seat_front")) }
        e.apply(SceneOp.Attach("bun", "car", "seat_back"))
    }

    @Test fun singleParentAndNoCycles() {
        val e = editor()
        e.apply(SceneOp.Add(toy("bun", "animals.rabbit")))
        e.apply(SceneOp.Add(toy("robot", "machines.robot")))
        e.apply(SceneOp.Attach("robot", "bun", "hold"))
        assertThrows { e.apply(SceneOp.Attach("bun", "bun", "hold")) }
        // robot rides rabbit; rabbit cannot be carried by... rabbit's own child chain
        e.apply(SceneOp.Add(toy("rocket", "space.rocket")))
        e.apply(SceneOp.Attach("robot", "rocket", "cockpit"))   // re-attach moves, never two parents
        assertEquals("rocket", e.doc.obj("robot")!!.attachment!!.parentId)
        assertEquals(1, e.doc.objects.count { it.attachment != null })
    }

    @Test fun rocketReturnsWithoutLosingPassenger() {
        val e = editor()
        e.apply(SceneOp.Add(toy("rocket", "space.rocket", 0.5f, 0.8f)))
        e.apply(SceneOp.Add(toy("robot", "machines.robot", 0.2f, 0.8f)))
        e.apply(SceneOp.Attach("robot", "rocket", "cockpit"))
        e.apply(SceneOp.Move("rocket", 0.5f, 0.2f))
        e.apply(SceneOp.Move("rocket", 0.5f, 0.8f))
        assertEquals("rocket", e.doc.obj("robot")!!.attachment?.parentId)
        e.apply(SceneOp.Detach("robot"))
        val r = e.doc.obj("robot")!!
        assertNull(r.attachment)
        assertTrue(r.transform.x in 0.02f..0.98f && r.transform.y in 0.05f..0.98f)
    }

    @Test fun storingParentDetachesChildrenAndKeepsThem() {
        val e = editor()
        e.apply(SceneOp.Add(toy("car", "vehicles.car")))
        e.apply(SceneOp.Add(toy("bun", "animals.rabbit")))
        e.apply(SceneOp.Attach("bun", "car", "seat_front"))
        e.apply(SceneOp.Store("car"))
        assertNull(e.doc.obj("car"))
        assertNull(e.doc.obj("bun")!!.attachment)
    }

    @Test fun twentyUndoStepsAndDoubleAddIsIdempotent() {
        val e = editor()
        e.apply(SceneOp.Add(toy("cat", "animals.cat")))
        e.apply(SceneOp.Add(toy("cat", "animals.cat")))
        assertEquals(1, e.doc.objects.size)
        repeat(25) { e.apply(SceneOp.Move("cat", 0.1f + it * 0.02f, 0.6f)) }
        var n = 0
        while (n < 25 && e.undo()) n++
        assertEquals(25, n)
        assertEquals(0.5f, e.doc.obj("cat")!!.transform.x, 1e-4f)
    }

    @Test fun scaleIsClampedToDecorateRange() {
        val e = editor()
        e.apply(SceneOp.Add(toy("cat", "animals.cat")))
        e.apply(SceneOp.Scale("cat", 5f))
        assertEquals(1.6f, e.doc.obj("cat")!!.transform.scale)
        e.apply(SceneOp.Scale("cat", 0.1f))
        assertEquals(0.6f, e.doc.obj("cat")!!.transform.scale)
    }

    @Test fun waypointMoverUsesElapsedTimeAndClampsPauses() {
        val m = WaypointMover(speedPerSec = 0.2f, maxStepMs = 100)
        val path = listOf(0.5f to 0.5f)
        val (a, _) = m.step(0f, 0.5f, path, 0, 50)
        assertEquals(0.01f, a.x, 1e-4f)
        val (b, _) = m.step(0f, 0.5f, path, 0, 60_000) // app resumed after a minute: no teleport
        assertEquals(0.02f, b.x, 1e-4f)
    }

    private fun assertThrows(f: () -> Unit) {
        try { f(); throw AssertionError("expected rule violation") } catch (_: SceneRuleException) {}
    }
}
