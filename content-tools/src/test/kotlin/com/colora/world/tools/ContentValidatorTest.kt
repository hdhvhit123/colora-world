package com.colora.world.tools

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ContentValidatorTest {
    @get:Rule val tmp = TemporaryFolder()
    private val real = File(System.getProperty("colora.content"))
    private fun copy(): File = tmp.newFolder("c").also { real.copyRecursively(it, overwrite = true) }

    @Test fun shippedContentIsValidForFull() {
        val v = ContentValidator(real, "FULL")
        assertTrue(v.errors.joinToString("\n"), v.run())
    }

    @Test fun tamperedFileFailsIntegrityIndex() {
        val d = copy()
        File(d, "templates/animals.cat.json").appendText(" ")
        val v = ContentValidator(d); assertFalse(v.run())
        assertTrue(v.errors.any { "digest mismatch templates/animals.cat.json" in it })
    }

    @Test fun missingTranslationIsReported() {
        val d = copy()
        val f = File(d, "locales/vi.json")
        f.writeText(f.readText().replace(Regex("\"template\\.animals\\.cat\":\\s*\"[^\"]*\",?"), ""))
        val v = ContentValidator(d); assertFalse(v.run())
        assertTrue(v.errors.any { "missing VI text for template.animals.cat" in it })
    }

    @Test fun storyWithoutFreePathIsReported() {
        val d = copy()
        val f = File(d, "stories.json")
        // make ST-14 depend only on the premium submarine
        f.writeText(f.readText().replace("\"vehicles.boat\",", "").replace("\"prop.exploration_window\"", "\"ocean.submarine\""))
        val v = ContentValidator(d); assertFalse(v.run())
        assertTrue(v.errors.joinToString("\n"), v.errors.any { it.startsWith("ST-14") })
    }

    @Test fun strayFileIsReported() {
        val d = copy()
        File(d, "templates/extra.json").writeText("{}")
        val v = ContentValidator(d); assertFalse(v.run())
        assertTrue(v.errors.any { "unlisted file templates/extra.json" in it })
    }
}
