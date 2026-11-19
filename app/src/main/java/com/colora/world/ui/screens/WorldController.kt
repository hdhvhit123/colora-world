package com.colora.world.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.colora.world.AppGraph
import com.colora.world.Autosave
import com.colora.world.content.ToyGeometry
import com.colora.world.data.Library
import com.colora.world.data.RewardsDoc
import com.colora.world.engine.content.StoryPack
import com.colora.world.engine.story.SceneEvent
import com.colora.world.engine.story.StoryDef
import com.colora.world.engine.story.StoryEvent
import com.colora.world.engine.story.StoryProgress
import com.colora.world.engine.story.StoryRuntime
import com.colora.world.engine.world.AttachRules
import com.colora.world.engine.world.SceneDocument
import com.colora.world.engine.world.SceneEditor
import com.colora.world.engine.world.SceneEnvironment
import com.colora.world.engine.world.SceneOp
import com.colora.world.engine.world.SceneRuleException
import com.colora.world.engine.world.SceneTransform
import com.colora.world.engine.world.ToyInstance
import com.colora.world.engine.world.WaypointMover
import com.colora.world.render.ToyVisual
import com.colora.world.render.WorldPainter
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.hypot

enum class WorldMode { PLAY, DECORATE }

/** Transient (never saved) animation of one toy: a tap reaction or a short trip along waypoints. */
class ToyAnim(var startMs: Long, var untilMs: Long, var path: List<Pair<Float, Float>> = emptyList(), var idx: Int = 0, var x: Float = 0f, var y: Float = 0f, var action: String? = null)

class WorldController(val graph: AppGraph, val pid: String, initial: SceneDocument, val painter: WorldPainter) {
    val editor = SceneEditor(initial, graph.content.toyCatalog)
    var version by mutableIntStateOf(0); private set
    var mode by mutableStateOf(WorldMode.PLAY)
    var selected by mutableStateOf<String?>(null)
    var message by mutableStateOf<String?>(null)
    val visuals = ConcurrentHashMap<String, ToyVisual>()
    val anims = HashMap<String, ToyAnim>()
    private val mover = WaypointMover(speedPerSec = 0.28f)
    val doc: SceneDocument get() = editor.doc

    // --------------------------------------------------------------- story
    var story by mutableStateOf<StoryDef?>(null)
    var progress by mutableStateOf<StoryProgress?>(null)
    var storyDone by mutableStateOf(false)
    var reward by mutableStateOf<String?>(null)

    fun loadStory(stories: StoryPack) {
        val id = doc.storyId ?: return
        val def = stories.stories.firstOrNull { it.id == id } ?: return
        story = def
        progress = graph.library.storyProgress(pid, id)?.takeIf { it.sceneId == doc.id && !it.completed }
            ?: StoryProgress(id, Library.uid(), doc.id)
    }

    fun emit(e: SceneEvent) {
        val def = story ?: return
        val p = progress ?: return
        val n = StoryRuntime.onEvent(def, p, e)
        if (n != p) setProgress(def, n)
    }

    fun skipStep() { val def = story ?: return; val p = progress ?: return; setProgress(def, StoryRuntime.skip(def, p)) }

    private fun setProgress(def: StoryDef, n: StoryProgress) {
        progress = n
        Autosave.enqueue { runCatching { graph.library.saveStoryProgress(pid, n) } }
        if (n.completed && !storyDone) {
            storyDone = true; reward = def.reward
            graph.audio.sfx("sfx.celebration", 1000)
            Autosave.enqueue {
                runCatching {
                    val r = graph.library.loadRewards(pid)
                    val upd = if (def.reward.startsWith("sticker:")) r.copy(stickers = (r.stickers + def.reward.removePrefix("sticker:")).distinct())
                        else r.copy(bookPages = (r.bookPages + "${def.id}:${doc.id}").distinct())
                    graph.library.saveRewards(pid, upd)
                }
            }
        }
    }

    // --------------------------------------------------------------- edits
    fun apply(op: SceneOp): Boolean = try {
        editor.apply(op); version++; save(); true
    } catch (e: SceneRuleException) {
        if (e.message == "scene full") message = "world.full"
        graph.audio.sfx("ui.gentle_no"); false
    }

    fun undo() { if (editor.undo()) { version++; save() } }
    fun redo() { if (editor.redo()) { version++; save() } }

    fun save() { val d = editor.doc; Autosave.schedule("scene:$pid:${d.id}", 400) { graph.library.saveScene(pid, editor.doc); true } }

    fun add(ref: String, templateId: String, x: Float) {
        if (doc.objects.size >= SceneDocument.MAX_OBJECTS) { message = "world.full"; graph.audio.sfx("ui.gentle_no"); return }
        val id = "o" + Library.uid().take(12)
        val caps = graph.content.capabilities(templateId)
        val y = when { "sky" in caps -> 0.32f; "flying" in caps -> 0.45f; else -> 0.86f }
        if (apply(SceneOp.Add(ToyInstance(id, ref, templateId, SceneTransform(x, y, 1f), 0)))) {
            selected = id
            graph.audio.sfx("ui.sticker_pop")
            emit(SceneEvent(StoryEvent.PLACE, id, templateId, caps))
            snapToWorldAnchor(id)
        }
    }

    // ------------------------------------------------------------ geometry
    /** Toy-local box in normalised scene space, for hit-testing. */
    fun hitBox(o: ToyInstance): FloatArray {
        val v = visuals[o.id]
        val cb = v?.art?.def?.contentBounds ?: listOf(200f, 200f, 824f, 1000f)
        val k = ToyGeometry.TOY_HEIGHT / 1024f * o.transform.scale
        val (px, py) = drawPos(o)
        val l = px + (cb[0] - 512f) * k / 2f; val r = px + (cb[2] - 512f) * k / 2f
        val foot = v?.footY ?: 1000f
        val t = py + (cb[1] - foot) * k; val b = py + (cb[3] - foot) * k
        return floatArrayOf(minOf(l, r), t, maxOf(l, r), b)
    }

    /** Current draw position including transient trips; attached children follow their parent's drawn anchor. */
    fun drawPos(o: ToyInstance, depth: Int = 0): Pair<Float, Float> {
        anims[o.id]?.takeIf { it.path.isNotEmpty() }?.let { return it.x to it.y }
        val att = o.attachment
        if (att != null && depth < 6) {
            val parent = doc.obj(att.parentId) ?: return o.transform.x to o.transform.y
            val a = graph.content.toyCatalog.anchors(parent.templateId).firstOrNull { it.id == att.anchorId } ?: return o.transform.x to o.transform.y
            val (px, py) = drawPos(parent, depth + 1)
            val dx = if (parent.transform.flipX) -a.dx else a.dx
            return px + dx * parent.transform.scale to py + a.dy * parent.transform.scale
        }
        return o.transform.x to o.transform.y
    }

    fun ordered(): List<ToyInstance> = doc.objects.sortedBy { it.zOrder }

    fun hit(x: Float, y: Float): ToyInstance? = ordered().reversed().firstOrNull { o ->
        val b = hitBox(o); x in b[0]..b[2] && y in b[1]..b[3]
    }

    /** After a drop: attach to the nearest compatible free anchor of another toy (within reach). */
    fun tryAttach(id: String, x: Float, y: Float): Boolean {
        val child = doc.obj(id) ?: return false
        val caps = graph.content.capabilities(child.templateId)
        var best: Triple<ToyInstance, String, Float>? = null
        for (p in doc.objects) {
            if (p.id == id) continue
            for (a in graph.content.toyCatalog.anchors(p.templateId)) {
                if (!AttachRules.accepts(a.type, caps)) continue
                if (doc.objects.any { it.attachment?.parentId == p.id && it.attachment?.anchorId == a.id && it.id != id }) continue
                val (px, py) = drawPos(p)
                val ax = px + (if (p.transform.flipX) -a.dx else a.dx) * p.transform.scale; val ay = py + a.dy * p.transform.scale
                val d = hypot((ax - x) * 2f, ay - y)
                if (d < 0.12f && (best == null || d < best.third)) best = Triple(p, a.id, d)
            }
        }
        val (parent, anchor, _) = best ?: return false
        if (child.attachment != null) apply(SceneOp.Detach(id))
        if (!apply(SceneOp.Attach(id, parent.id, anchor))) return false
        graph.audio.sfx("ui.sticker_pop")
        val pcaps = graph.content.capabilities(parent.templateId)
        emit(SceneEvent(StoryEvent.ATTACH, id, child.templateId, caps, parent.templateId, pcaps))
        val aType = graph.content.toyCatalog.anchors(parent.templateId).firstOrNull { it.id == anchor }?.type
        if (aType == "plant") emit(SceneEvent(StoryEvent.INTERACT, id, child.templateId, caps, action = "plant"))
        return true
    }

    /** World anchors (soil beds, pond, table, bench, sky): snap and report story events. */
    fun snapToWorldAnchor(id: String) {
        val o = doc.obj(id) ?: return
        if (o.attachment != null) return
        val caps = graph.content.capabilities(o.templateId)
        val a = painter.def.anchors.filter { wa ->
            AttachRules.accepts(wa.type, caps) || (wa.type == "sky" && ("sky" in caps || "flying" in caps)) || (wa.type == "water" && "aquatic" in caps)
        }.minByOrNull { hypot((it.x - o.transform.x) * 2f, it.y - o.transform.y) } ?: return
        if (hypot((a.x - o.transform.x) * 2f, a.y - o.transform.y) > 0.10f) return
        editor.apply(SceneOp.Move(id, a.x, a.y)); version++; save()
        if (a.type == "plant") emit(SceneEvent(StoryEvent.INTERACT, id, o.templateId, caps, action = "plant"))
    }

    // ---------------------------------------------------------- play taps
    fun tap(o: ToyInstance, now: Long) {
        val v = visuals[o.id] ?: return
        val caps = v.caps
        graph.audio.toySound(v.audioSetId, v.preset)
        val base = SceneEvent(StoryEvent.INTERACT, o.id, o.templateId, caps)
        val actions = buildList {
            add("tap")
            if ("waterable" in caps || "plantable" in caps) add("water")
            if ("openable" in caps || "door" in caps || "readable" in caps) { add("open"); add("sticker") }
            if ("light" in caps) add("light")
            if ("candles" in caps || o.templateId.contains("cake")) add("blow")
            if ("aquatic" in caps) add("swim")
        }
        actions.forEach { emit(base.copy(action = it)) }
        caps.firstOrNull { it.startsWith("mood_") }?.let { mood ->
            val env = when (mood) {
                "mood_night" -> SceneEnvironment("night", doc.environment.weather); "mood_day" -> SceneEnvironment("day", "clear")
                "mood_rain" -> SceneEnvironment(doc.environment.timeOfDay, "rain"); "mood_rainbow" -> SceneEnvironment(doc.environment.timeOfDay, "rainbow")
                else -> SceneEnvironment(doc.environment.timeOfDay, "cloudy")
            }
            apply(SceneOp.SetEnvironment(env))
            emit(SceneEvent(StoryEvent.MOOD, o.id, o.templateId, caps, action = env.weather))
        }
        val (x, y) = drawPos(o)
        val trip: List<Pair<Float, Float>>? = when {
            o.attachment != null -> null
            "rideable" in caps && "flying" !in caps -> { val dir = if (x < 0.5f) 1f else -1f; listOf(x + 0.22f * dir to y, x + 0.36f * dir to y, x to y) }
            "flying" in caps || "sky" in caps && "rideable" in caps -> listOf(x + 0.08f to y - 0.16f, x + 0.18f to y - 0.08f, x to y)
            "aquatic" in caps -> listOf(x + 0.1f to y - 0.03f, x - 0.06f to y, x to y)
            else -> null
        }
        anims[o.id] = ToyAnim(now, now + if (trip != null) 6000 else 1400, trip.orEmpty(), 0, x, y)
    }

    /** Advances trips with elapsed time (clamped: a pause never teleports a toy). */
    fun step(now: Long, dtMs: Long) {
        val done = ArrayList<String>()
        for ((id, a) in anims) {
            if (a.path.isNotEmpty()) {
                val (m, idx) = mover.step(a.x, a.y, a.path, a.idx, dtMs)
                if (idx != a.idx) {
                    val o = doc.obj(id)
                    if (o != null) {
                        val caps = graph.content.capabilities(o.templateId)
                        val detail = "wp$idx"
                        val action = when { idx >= a.path.size -> "return"; idx == 1 -> "waypoint"; else -> "arrive" }
                        for (act in listOf(action, "waypoint", "arrive", "loop", "cross")) emit(SceneEvent(StoryEvent.MOVE, id, o.templateId, caps, action = act, detail = detail))
                        doc.objects.filter { it.attachment?.parentId == id }.forEach { ch ->
                            val cc = graph.content.capabilities(ch.templateId)
                            for (act in listOf("waypoint", "arrive", "cross")) emit(SceneEvent(StoryEvent.MOVE, ch.id, ch.templateId, cc, action = act, detail = detail))
                        }
                    }
                }
                a.x = m.x; a.y = m.y; a.idx = idx
                if (m.done) done += id
            } else if (now > a.untilMs) done += id
        }
        done.forEach { anims.remove(it) }
    }

    val animating: Boolean get() = anims.isNotEmpty()
}
