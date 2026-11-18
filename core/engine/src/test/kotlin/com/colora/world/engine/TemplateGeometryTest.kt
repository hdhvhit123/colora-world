package com.colora.world.engine

import com.colora.world.engine.content.TemplateDef
import com.colora.world.engine.content.TemplateGeometry
import com.colora.world.engine.geometry.Polygon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TemplateGeometryTest {
    private val dir = File(System.getProperty("colora.content"), "templates")
    private fun load(id: String) = TemplateGeometry(TemplateDef.parse(File(dir, "$id.json").readText()))

    @Test fun catHitTestPicksTopmostRegion() {
        val g = load("animals.cat")
        assertEquals("belly", g.hitTest(512f, 715f))
        assertEquals("muzzle", g.hitTest(512f, 460f))       // muzzle drawn over head
        assertEquals("head", g.hitTest(380f, 330f))
        assertEquals("ear_in_l", g.hitTest(393f, 224f))
        assertNull(g.hitTest(40f, 40f))                     // paper
    }

    @Test fun headPartWinsOverBodyWhereTheyOverlap() {
        val g = load("animals.cat")
        // (512, 500) is inside both body (y from 470) and head (to 520): head part is in front
        val id = g.hitTest(512f, 500f)
        assertEquals("head", g.def.regions.first { it.id == id }.part)
    }

    @Test fun toleranceSnapsOnlyWithinThreshold() {
        val g = load("animals.cat")
        // just outside the right paw edge
        val near = g.hitTest(633f, 845f, tolerance = 14f)
        assertEquals("paw_r", near)
        assertNull(g.hitTest(700f, 960f, tolerance = 14f))
    }

    @Test fun everyRegionOfEveryTemplateIsReachableByTap() {
        val files = dir.listFiles { f -> f.name.endsWith(".json") }!!.sortedBy { it.name }
        assertEquals(168, files.size)
        var checked = 0
        for (f in files) {
            val g = TemplateGeometry(TemplateDef.parse(f.readText()))
            for (r in g.regions) {
                assertTrue("${g.def.id}/${r.def.id} has no area", Polygon.area(r.contours) > 20f)
                // find an interior sample point of the region and verify the tap
                // resolves to this region or something drawn above it, never below
                val p = interiorPoint(r.contours, r.bounds) ?: continue
                val hit = g.hitTest(p.first, p.second)!!
                val hr = g.region(hit)!!
                val above = hr.partIndex > r.partIndex || (hr.partIndex == r.partIndex && hr.def.order >= r.def.order)
                assertTrue("${g.def.id}: tap in ${r.def.id} hit lower ${hit}", above)
                checked++
            }
        }
        assertTrue(checked > 1500)
    }

    private fun interiorPoint(c: List<FloatArray>, b: com.colora.world.engine.geometry.Bounds): Pair<Float, Float>? {
        val steps = 24
        for (i in 1 until steps) for (j in 1 until steps) {
            val x = b.left + b.width * i / steps; val y = b.top + b.height * j / steps
            if (Polygon.contains(c, x, y) && Polygon.distanceToEdge(c, x, y) > 3f) return x to y
        }
        return null
    }
}
