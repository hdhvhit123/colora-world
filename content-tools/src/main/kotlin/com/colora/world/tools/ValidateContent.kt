package com.colora.world.tools

import com.colora.world.engine.content.ArtDef
import com.colora.world.engine.content.Catalog
import com.colora.world.engine.content.CharacterPack
import com.colora.world.engine.content.ContentIndex
import com.colora.world.engine.content.ContentJson
import com.colora.world.engine.content.Extras
import com.colora.world.engine.content.PalettePack
import com.colora.world.engine.content.PromptPack
import com.colora.world.engine.content.StoryPack
import com.colora.world.engine.content.TemplateDef
import com.colora.world.engine.content.TemplateGeometry
import com.colora.world.engine.content.WorldDef
import com.colora.world.engine.prompts.Suggestions
import com.colora.world.engine.story.FreePathResolver
import com.colora.world.engine.story.Matcher
import com.colora.world.engine.story.StoryEvent
import com.colora.world.engine.world.AttachRules
import java.io.File
import java.security.MessageDigest
import kotlin.system.exitProcess

/**
 * Content validator (spec: counts, unique ids, asset paths, locales, geometry,
 * rig, anchors/capabilities, SHA-256 index, free path for every story).
 * Usage: ValidateContent <contentDir> [VS|LAUNCH|FULL]. Exit code 1 on any error.
 */
class ContentValidator(private val dir: File, private val milestone: String = "FULL") {
    val errors = ArrayList<String>()
    val notes = ArrayList<String>()
    private fun err(m: String) { errors += m }
    private fun file(p: String) = File(dir, p)
    private inline fun <reified T> read(p: String): T? = runCatching { ContentJson.decodeFromString<T>(file(p).readText()) }
        .onFailure { err("$p: ${it.message?.take(160)}") }.getOrNull()

    val requiredProps = listOf("soil_block", "watering_can", "plant_pot", "bouquet_wrap", "footprint", "stall", "basket", "lamp", "path", "book",
        "waypoint", "gift_toy", "picnic_mat", "bridge", "exploration_window", "leaf", "chair", "table", "house", "park", "food_plate").map { "prop.$it" }

    fun run(): Boolean {
        val en = read<Map<String, String>>("locales/en.json") ?: emptyMap()
        // every shipped content language must be complete (VI is required by the spec; others are optional but all-or-nothing)
        val langs = (file("locales").listFiles() ?: emptyArray()).filter { it.name.endsWith(".json") }.map { it.name.removeSuffix(".json") }.filter { it != "en" }.sorted()
        if ("vi" !in langs) err("locales/vi.json missing")
        val others = langs.associateWith { read<Map<String, String>>("locales/$it.json") ?: emptyMap() }
        fun key(k: String?, where: String) {
            if (k == null) return
            if (en[k].isNullOrBlank()) err("$where: missing EN text for $k")
            others.forEach { (l, m) -> if (m[k].isNullOrBlank()) err("$where: missing ${l.uppercase()} text for $k") }
        }
        others.forEach { (l, m) ->
            (en.keys - m.keys).forEach { err("locale key only in EN (missing in ${l.uppercase()}): $it") }
            (m.keys - en.keys).forEach { err("locale key only in ${l.uppercase()}: $it") }
            m.forEach { (k, v) -> if (v.isBlank()) err("empty ${l.uppercase()} text for $k") }
        }
        // UI strings (assets/ui/<lang>.json, next to the content pack): same keys and the same {n} placeholders as English
        val uiDir = File(dir.parentFile, "ui")
        if (uiDir.isDirectory) {
            val uiEn = runCatching { ContentJson.decodeFromString<Map<String, String>>(File(uiDir, "en.json").readText()) }.getOrElse { err("ui/en.json unreadable"); emptyMap() }
            val ph = Regex("\\{\\d}")
            val uiLangs = (uiDir.listFiles() ?: emptyArray()).filter { it.name.endsWith(".json") && it.name != "en.json" }
            for (f in uiLangs) {
                val l = f.name.removeSuffix(".json")
                val m = runCatching { ContentJson.decodeFromString<Map<String, String>>(f.readText()) }.getOrElse { err("ui/${f.name} unreadable"); continue }
                (uiEn.keys - m.keys).forEach { err("ui/$l: missing $it") }
                (m.keys - uiEn.keys).forEach { err("ui/$l: unknown key $it") }
                m.forEach { (k, v) ->
                    if (v.isBlank()) err("ui/$l: empty $k")
                    val want = ph.findAll(uiEn[k] ?: "").map { it.value }.toSet()
                    if (ph.findAll(v).map { it.value }.toSet() != want) err("ui/$l: placeholders differ for $k")
                }
                if (l !in langs) err("ui/$l has no matching content locale")
            }
            langs.filter { !File(uiDir, "$it.json").isFile }.forEach { err("content locale $it has no ui/$it.json") }
            notes += "ui: ${uiLangs.size + 1} languages, ${uiEn.size} keys"
        }
        notes += "content languages: ${(listOf("en") + langs).joinToString()}"

        // ---------------------------------------------------------- integrity index
        val index = read<ContentIndex>("index.json")
        if (index != null) {
            val listed = index.files.associateBy { it.path }
            for (e in index.files) {
                val f = file(e.path)
                if (!f.isFile) { err("index: missing ${e.path}"); continue }
                val b = f.readBytes()
                if (b.size.toLong() != e.size || sha(b) != e.sha256) err("index: digest mismatch ${e.path}")
            }
            dir.walkTopDown().filter { it.isFile && it.name != "index.json" }.forEach {
                val rel = it.relativeTo(dir).invariantSeparatorsPath
                if (rel !in listed) err("index: unlisted file $rel")
            }
            notes += "index: ${index.files.size} files verified"
        }

        // ---------------------------------------------------------------- templates
        val catalog = read<Catalog>("catalog.json") ?: return finish()
        val wantTopics = mapOf("VS" to 6, "LAUNCH" to 6, "FULL" to 14).getValue(milestone)
        if (catalog.topics.size < wantTopics) err("catalog: ${catalog.topics.size} topics < $wantTopics")
        val ids = catalog.templates.map { it.id }
        if (ids.toSet().size != ids.size) err("catalog: duplicate template ids")
        if (milestone == "FULL" && ids.size != 168) err("catalog: ${ids.size} templates, FULL needs 168")
        val templates = HashMap<String, TemplateDef>()
        val geometryHashes = HashMap<String, String>()
        for (t in catalog.templates) {
            key("template.${t.id}", t.id)
            if (!file(t.thumb).isFile) err("${t.id}: thumbnail missing")
            val def = runCatching { TemplateDef.parse(file(t.file).readText()) }.getOrElse { err("${t.id}: ${it.message?.take(120)}"); null } ?: continue
            if (def.id != t.id || def.topicId != t.topicId) err("${t.id}: id/topic mismatch with catalog")
            if (def.interactionPreset.isBlank()) err("${t.id}: no interaction preset")
            if (def.capabilities.isEmpty()) err("${t.id}: no capabilities")
            checkArt(def, t.id)
            val h = sha(def.regions.joinToString("|") { it.d }.toByteArray())
            geometryHashes.put(h, t.id)?.let { other -> err("${t.id}: identical geometry to $other") }
            templates[t.id] = def
        }
        for (topic in catalog.topics) {
            key(topic.labelKey, topic.id)
            if (topic.templates.size != 12) err("topic ${topic.id}: ${topic.templates.size} templates")
            val base = catalog.templates.count { it.topicId == topic.id && it.tier == "base" }
            if (base < 6) err("topic ${topic.id}: only $base base templates")
        }

        // ------------------------------------------------------------------- extras
        val extras = read<Extras>("extras.json") ?: return finish()
        val props = HashMap<String, ArtDef>()
        for (p in extras.props) {
            key(p.labelKey, p.id)
            val a = art(p.file, p.id) ?: continue
            if (!file(p.thumb).isFile) err("${p.id}: thumbnail missing")
            if (a.capabilities.isEmpty()) err("${p.id}: no capabilities")
            props[p.id] = a
        }
        (requiredProps - props.keys).forEach { err("missing required prop $it") }
        if (extras.stickers.size < 36) err("stickers: ${extras.stickers.size} < 36")
        extras.stickers.groupBy { it.group }.forEach { (g, l) -> key("stickergroup.$g", "sticker group"); if (l.size < 6) err("sticker group $g has ${l.size}") }
        extras.stickers.forEach { s -> art(s.file, s.id); if (!file(s.thumb).isFile) err("${s.id}: thumbnail missing") }
        if (extras.mascot.size < 6) err("mascot: ${extras.mascot.size} expressions < 6")
        extras.mascot.forEach { m -> art(m.file, m.id); key("milo.${m.id.substringAfter('.')}", m.id) }
        val wantWorlds = mapOf("VS" to 1, "LAUNCH" to 2, "FULL" to 7).getValue(milestone)
        if (extras.worlds.size < wantWorlds) err("worlds: ${extras.worlds.size} < $wantWorlds")
        val worldIds = HashSet<String>()
        for (w in extras.worlds) {
            key(w.labelKey, w.id)
            val def = runCatching { WorldDef.parse(file(w.file).readText()) }.getOrElse { err("${w.id}: ${it.message?.take(120)}"); null } ?: continue
            worldIds += def.id
            if (def.layers.map { it.id } != listOf("far", "mid", "near")) err("${w.id}: needs far/mid/near layers")
            if (def.anchors.size < 5) err("${w.id}: ${def.anchors.size} anchors < 5")
            if (def.anchors.any { it.x !in 0f..1f || it.y !in 0f..1f }) err("${w.id}: anchor outside scene")
            def.layers.flatMap { it.shapes }.forEach { s -> parsePath(s.d, "${w.id} shape"); hex(s.fill, w.id) }
            def.zones.forEach { z -> parsePath(z.d, "${w.id} zone ${z.id}") }
            if (!file(w.thumb).isFile) err("${w.id}: thumbnail missing")
        }

        // --------------------------------------------------------------- characters
        val chars = read<CharacterPack>("characters.json")
        if (chars != null) {
            val total = chars.bodies.size + chars.parts.size
            if (milestone == "FULL" && total < 44) err("character parts: $total < 44")
            val kinds = chars.parts.groupingBy { it.kind }.eachCount()
            val want = mapOf("ear" to 8, "tail" to 8, "wing" to 6, "hat" to 8, "decor" to 8)
            if (milestone == "FULL") { if (chars.bodies.size < 6) err("bodies ${chars.bodies.size} < 6"); want.forEach { (k, n) -> if ((kinds[k] ?: 0) < n) err("character $k: ${kinds[k]} < $n") } }
            for (b in chars.bodies) {
                art(b.file, b.id); key("part.${b.id.removePrefix("char.")}", b.id)
                for (p in chars.parts) for (s in p.sockets) if (s !in b.sockets) err("${b.id}: missing socket $s for ${p.id}")
            }
            chars.parts.forEach { p -> art(p.file, p.id); key("part.${p.id.removePrefix("char.")}", p.id); if (p.minScale > p.maxScale) err("${p.id}: scale range") }
            chars.scaleLimits.forEach { (k, v) -> if (v < 0.8f) err("character combo $k cannot fit at min scale") }
        }

        // ------------------------------------------------------------------ stories
        val capsOf: (String) -> Set<String> = { id -> templates[id]?.capabilities?.toSet() ?: props[id]?.capabilities?.toSet() ?: emptySet() }
        val anchorsOf: (String) -> List<String> = { id -> (templates[id]?.anchors ?: props[id]?.anchors ?: emptyList()).map { it.type } }
        val free = catalog.templates.filter { it.tier == "base" }.map { it.id } + props.keys
        val known = templates.keys + props.keys
        val stories = read<StoryPack>("stories.json")
        if (stories != null) {
            if (milestone == "FULL" && stories.stories.size != 28) err("stories: ${stories.stories.size} != 28")
            if (stories.stories.map { it.id }.toSet().size != stories.stories.size) err("stories: duplicate ids")
            for (s in stories.stories) {
                key(s.titleKey, s.id)
                if (s.worldId !in worldIds) err("${s.id}: unknown world ${s.worldId}")
                if (s.steps.size !in 2..5) err("${s.id}: ${s.steps.size} steps (2–5 allowed)")
                s.iconTemplates.forEach { if (it !in known) err("${s.id}: unknown icon $it") }
                if (s.reward.startsWith("sticker:") && extras.stickers.none { it.id == s.reward.removePrefix("sticker:") }) err("${s.id}: unknown reward sticker")
                for (st in s.steps) {
                    key(st.textKey, "${s.id}/${st.id}")
                    listOfNotNull(st.subject, st.target).forEach { m -> (m.templates + m.fallbacks).forEach { if (it !in known) err("${s.id}/${st.id}: unknown ref $it") } }
                    if (st.count < 1) err("${s.id}/${st.id}: count < 1")
                    val tgt = st.target
                    if (st.event == StoryEvent.ATTACH && tgt != null && !tgt.isAny) {
                        // a free subject must be attachable to some anchor of a free target
                        val subjects = free.filter { resolves(st.subject, it, capsOf) }
                        val targets = free.filter { resolves(tgt, it, capsOf) }
                        val ok = subjects.any { sj -> targets.any { tg -> tg != sj && anchorsOf(tg).any { AttachRules.accepts(it, capsOf(sj)) } } }
                        if (!ok) err("${s.id}/${st.id}: no free subject/target pair with a compatible anchor")
                    }
                }
                FreePathResolver.check(s, free, capsOf).forEach { err("${it.storyId}/${it.stepId}: ${it.reason}") }
            }
            // spec-declared fallbacks
            mapOf("ST-01" to listOf("prop.picnic_mat", "prop.basket"), "ST-06" to listOf("prop.bridge"), "ST-07" to listOf("prop.book"),
                "ST-14" to listOf("vehicles.boat", "prop.exploration_window"), "ST-16" to listOf("prop.leaf")).forEach { (sid, need) ->
                val st = stories.stories.firstOrNull { it.id == sid }
                val have = st?.steps?.flatMap { listOfNotNull(it.subject, it.target) }?.flatMap { it.fallbacks }.orEmpty().toSet()
                need.forEach { if (it !in have) err("$sid: required free fallback $it not declared") }
            }
        }

        // ------------------------------------------------------------------ prompts
        val prompts = read<PromptPack>("prompts.json")
        if (prompts != null) {
            if (milestone == "FULL" && prompts.prompts.size != 30) err("prompts: ${prompts.prompts.size} != 30")
            for (p in prompts.prompts) {
                key(p.textKey, p.id); key(p.altTextKey, p.id)
                if (!file(p.illustration).isFile) err("${p.id}: illustration missing")
                (p.suggestedTemplates + p.altTemplates).forEach { if (it !in known) err("${p.id}: unknown template $it") }
                if (Suggestions.resolve(p.toPrompt()) { it in free } == null) err("${p.id}: no free way to start")
            }
        }
        val palettes = read<PalettePack>("palettes.json")
        palettes?.palettes?.forEach { p -> key(p.labelKey, p.id); p.colors.forEach { hex(it, p.id) } }
        prompts?.prompts?.forEach { p -> if (p.paletteId != null && palettes?.palettes?.none { it.id == p.paletteId } == true) err("${p.id}: unknown palette") }
        notes += "templates ${templates.size}, props ${props.size}, stickers ${extras.stickers.size}, worlds ${worldIds.size}, " +
            "character parts ${(chars?.bodies?.size ?: 0) + (chars?.parts?.size ?: 0)}, stories ${stories?.stories?.size}, prompts ${prompts?.prompts?.size}, locale keys ${en.size}"
        return finish()
    }

    private fun resolves(m: Matcher, id: String, capsOf: (String) -> Set<String>) = m.matches(id, capsOf(id)) && !m.isAny

    private fun art(path: String, id: String): ArtDef? {
        val a = runCatching { ArtDef.parse(file(path).readText()) }.getOrElse { err("$id: ${it.message?.take(120)}"); return null }
        if (a.id != id) err("$id: id mismatch in $path")
        checkArt(a.asTemplate(), id)
        return a
    }

    private fun checkArt(def: TemplateDef, id: String) {
        val partIds = def.parts.map { it.id }
        if (partIds.toSet().size != partIds.size) err("$id: duplicate part ids")
        for (p in def.parts) {
            if (p.parent != null && p.parent !in partIds) err("$id: part ${p.id} has unknown parent ${p.parent}")
            var cur: String? = p.parent; var hops = 0
            while (cur != null) { if (cur == p.id || ++hops > partIds.size) { err("$id: rig cycle at ${p.id}"); break }; cur = def.parts.firstOrNull { it.id == cur }?.parent }
        }
        val rids = def.regions.map { it.id }
        if (rids.toSet().size != rids.size) err("$id: duplicate region ids")
        def.regions.forEach { r -> if (r.part !in partIds) err("$id: region ${r.id} in unknown part"); hex(r.sample, id) }
        def.anchors.forEach { if (it.x !in 0f..def.designBounds.width.toFloat() || it.y !in 0f..def.designBounds.height.toFloat()) err("$id: anchor ${it.id} outside") }
        if (def.anchors.map { it.id }.toSet().size != def.anchors.size) err("$id: duplicate anchor ids")
        runCatching { TemplateGeometry(def).regions.forEach { r -> if (r.contours.isEmpty()) err("$id: region ${r.def.id} empty") } }
            .onFailure { err("$id: geometry ${it.message}") }
    }

    private fun parsePath(d: String, where: String) = runCatching { com.colora.world.engine.geometry.PathData.parse(d) }.onFailure { err("$where: bad path") }
    private fun hex(s: String, where: String) { if (!Regex("^#[0-9A-Fa-f]{6}$").matches(s)) err("$where: bad colour $s") }
    private fun finish() = errors.isEmpty()
    private fun sha(b: ByteArray) = MessageDigest.getInstance("SHA-256").digest(b).joinToString("") { "%02x".format(it) }
}

fun main(args: Array<String>) {
    val dir = File(args.getOrElse(0) { "app/src/main/assets/content" })
    val v = ContentValidator(dir, args.getOrElse(1) { "FULL" })
    val ok = v.run()
    v.notes.forEach { println("ok    $it") }
    if (ok) { println("CONTENT OK (${args.getOrElse(1) { "FULL" }})"); return }
    v.errors.take(200).forEach { System.err.println("ERROR $it") }
    System.err.println("${v.errors.size} content error(s)")
    exitProcess(1)
}
