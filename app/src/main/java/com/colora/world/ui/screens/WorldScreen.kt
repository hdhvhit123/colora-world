package com.colora.world.ui.screens

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.colora.world.Autosave
import com.colora.world.content.ToyGeometry
import com.colora.world.data.Library
import com.colora.world.data.ToyRef
import com.colora.world.engine.paint.PaintReducer
import com.colora.world.engine.paint.PaintState
import com.colora.world.engine.story.StoryRuntime
import com.colora.world.engine.world.SceneDocument
import com.colora.world.engine.world.SceneEnvironment
import com.colora.world.engine.world.SceneOp
import com.colora.world.graph
import com.colora.world.render.ArtModel
import com.colora.world.render.ArtRenderer
import com.colora.world.render.ToyVisual
import com.colora.world.render.WorldPainter
import com.colora.world.storage.DocKind
import com.colora.world.ui.AppState
import com.colora.world.ui.Screen
import com.colora.world.ui.cl
import com.colora.world.ui.components.*
import com.colora.world.ui.t
import com.colora.world.ui.theme.C
import com.colora.world.ui.theme.D
import com.colora.world.ui.theme.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.sin

/** Portrait phones see ~55% of the world's width; the world sits low with sky above and ground below. */
fun worldFit(w: Float, h: Float, p: WorldPainter): Pair<Float, Float> {
    // landscape: whole height; portrait: the ground fills ~72% of the screen (pan to see the rest) instead of a band under a tall empty sky
    val k = minOf(h / p.height, maxOf(w / (p.width * 0.55f), h * 0.72f / p.height))
    val oy = (h - p.height * k) * 0.62f
    return k to oy
}

/** Opens or creates the scene, adds an incoming toy, then hosts the interactive world. */
@Composable
fun WorldScreen(state: AppState, s: Screen.World) {
    val g = LocalContext.current.graph
    val ctrl by produceState<WorldController?>(null, s.sceneId) {
        value = withContext(Dispatchers.IO) { runCatching {
            val doc = g.library.loadScene(state.pid, s.sceneId) ?: return@runCatching null.also { android.util.Log.w("Colora", "scene missing") }
            val def = g.content.world(doc.worldId) ?: return@runCatching null
            val wp = WorldPainter(def)
            WorldController(g, state.pid, doc, wp).also { c ->
                c.loadStory(g.content.stories)
                for (o in doc.objects) ToyVisual.resolve(g, state.pid, o.artworkRevisionId, o.templateId)?.let { c.visuals[o.id] = it }
            }
        }.onFailure { android.util.Log.e("Colora", "world load failed", it) }.getOrNull() }
    }
    val c = ctrl ?: run { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Milo("curious", null) }; return }
    LaunchedEffect(c, s.addRef) {
        val ref = s.addRef ?: return@LaunchedEffect
        val tid = s.addTemplate ?: return@LaunchedEffect
        if (c.doc.objects.any { it.artworkRevisionId == ref }) return@LaunchedEffect
        withContext(Dispatchers.IO) { ToyVisual.resolve(g, state.pid, ref, tid) }?.let { v ->
            val before = c.doc.objects.map { it.id }.toSet()
            c.add(ref, tid, 0.16f + (c.doc.objects.size % 5) * 0.08f)
            c.doc.objects.firstOrNull { it.id !in before }?.let { c.visuals[it.id] = v }
        }
    }
    DisposableEffect(c) { onDispose { Autosave.flushAll() } }
    WorldContent(state, c)
}

@Composable
private fun WorldContent(state: AppState, c: WorldController) {
    val g = LocalContext.current.graph
    val scope = rememberCoroutineScope()
    val reduced = g.settings.v.reducedMotion
    var tick by remember { mutableLongStateOf(0L) }
    var camX by remember { mutableFloatStateOf(0f) }
    var drag by remember { mutableStateOf<Pair<String, Offset>?>(null) }
    var trayOpen by remember { mutableStateOf(c.doc.objects.isEmpty()) }
    var newer by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    LaunchedEffect(c) {
        newer = withContext(Dispatchers.IO) {
            c.doc.objects.mapNotNull { o ->
                val r = ToyRef.parse(o.artworkRevisionId) ?: return@mapNotNull null
                if (r.kind != "art") return@mapNotNull null
                val cur = g.library.store.pointer(state.pid, r.docId)?.currentRevisionId ?: return@mapNotNull null
                if (cur != r.revId) o.id to ToyRef.art(r.docId, cur).toString() else null
            }.toMap()
        }
    }
    LaunchedEffect(c) {
        var last = SystemClock.uptimeMillis()
        while (true) {
            withFrameMillis { }
            val now = SystemClock.uptimeMillis()
            c.step(now, now - last); last = now
            if (!reduced || c.animating) tick = now
        }
    }
    c.message?.let { key -> LaunchedEffect(key) { state.showToast(com.colora.world.ui.Strings.get(state.lang, key)); c.message = null } }

    Box(Modifier.fillMaxSize().background(C.bgCream)) {
        // ------------------------------------------------------------ scene
        Canvas(Modifier.fillMaxSize().semantics { contentDescription = "world" }.pointerInput(c) {
            awaitEachGesture {
                val down = awaitFirstDown()
                val (k, oy) = worldFit(size.width.toFloat(), size.height.toFloat(), c.painter)
                fun toScene(p: Offset) = Offset((p.x / k + camX) / c.painter.width, (p.y - oy) / k / c.painter.height)
                val sp = toScene(down.position)
                val hit = c.hit(sp.x, sp.y)
                var moved = 0f
                val start = down.position
                var lastX = down.position.x
                while (true) {
                    val ev = awaitPointerEvent()
                    val ch = ev.changes.firstOrNull { it.id == down.id } ?: break
                    if (!ch.pressed) break
                    if (!ch.positionChanged()) continue
                    moved = max(moved, (ch.position - start).getDistance())
                    if (hit != null && moved > 18f) drag = hit.id to toScene(ch.position)
                    else if (hit == null) {
                        val maxCam = max(0f, c.painter.width - size.width / k)
                        camX = (camX - (ch.position.x - lastX) / k).coerceIn(0f, maxCam)
                    }
                    lastX = ch.position.x
                    ch.consume()
                }
                val d = drag
                drag = null
                if (hit == null) { if (moved < 18f) c.selected = null; return@awaitEachGesture }
                if (d == null) {
                    if (c.mode == WorldMode.PLAY) c.tap(hit, SystemClock.uptimeMillis()) else c.selected = hit.id
                    return@awaitEachGesture
                }
                val p = d.second
                if (!c.tryAttach(hit.id, p.x, p.y)) {
                    if (hit.attachment != null) c.apply(SceneOp.Detach(hit.id))
                    if (c.mode == WorldMode.DECORATE || hit.attachment != null) { c.apply(SceneOp.Move(hit.id, p.x, p.y)); c.snapToWorldAnchor(hit.id) }
                }
                c.selected = hit.id
            }
        }) {
            tick; c.version
            val (k, oy) = worldFit(size.width, size.height, c.painter)
            val maxCam = max(0f, c.painter.width - size.width / k)
            if (camX > maxCam) camX = maxCam
            val night = c.doc.environment.timeOfDay == "night"
            val tSec = (tick % 1_000_000L) / 1000f
            drawIntoCanvas { cv ->
                val nc = cv.nativeCanvas
                val base = nc.save()
                nc.translate(0f, oy); nc.scale(k, k); nc.translate(-camX, 0f)
                c.painter.draw(nc, camX, night, !reduced, extraTop = oy / k, extraBottom = (size.height - oy) / k - c.painter.height)
                val W = c.painter.width; val H = c.painter.height
                // weather from mood toys
                when (c.doc.environment.weather) {
                    "rainbow" -> listOf(0xFFF25C54, 0xFFFFA94D, 0xFFFFE066, 0xFF4FB477, 0xFF5B8DEF, 0xFF9B6BDE).forEachIndexed { i, col ->
                        nc.drawArc(RectF(W * 0.25f + i * 18f, H * 0.12f + i * 18f, W * 0.75f - i * 18f, H * 1.1f - i * 18f), 180f, 180f, false,
                            Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 18f; color = col.toInt(); alpha = 150 })
                    }
                    "rain", "cloudy" -> if (c.doc.environment.weather == "rain") {
                        val rp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x997FB8E6.toInt(); strokeWidth = 5f; strokeCap = Paint.Cap.ROUND }
                        for (i in 0 until 70) { val x = (i * 97f) % W; val y = ((i * 53f + tSec * 420f) % (H * 0.8f)); nc.drawLine(x, y, x - 8f, y + 26f, rp) }
                    }
                }
                val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x2624324B }
                for (o in c.ordered()) {
                    val v = c.visuals[o.id] ?: continue
                    var (x, y) = c.drawPos(o)
                    drag?.takeIf { it.first == o.id }?.let { x = it.second.x; y = it.second.y }
                    val anim = c.anims[o.id]
                    val energy = when { anim != null -> 1f; reduced -> 0f; else -> 0.3f }
                    val s = ToyGeometry.TOY_HEIGHT * H / 1024f * o.transform.scale
                    val cb = v.art.def.contentBounds
                    if (o.attachment == null && "sky" !in v.caps) {
                        val wHalf = (cb[2] - cb[0]) * s * 0.42f
                        nc.drawOval(RectF(x * W - wHalf, y * H - 10f, x * W + wHalf, y * H + 12f), shadow)
                    }
                    if (night && "light" in v.caps) nc.drawCircle(x * W, y * H - (cb[3] - cb[1]) * s * 0.6f, (cb[3] - cb[1]) * s * 0.8f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x44FFE680 })
                    val sv = nc.save()
                    nc.translate(x * W, y * H)
                    nc.scale(if (o.transform.flipX) -s else s, s)
                    nc.translate(-512f, -v.footY)
                    v.draw(nc, tSec + (anim?.let { (SystemClock.uptimeMillis() - it.startMs) / 1000f } ?: 0f), energy, anim?.path?.isNotEmpty() == true, o.id.hashCode())
                    nc.restoreToCount(sv)
                    if (o.id == c.selected && c.mode == WorldMode.DECORATE) {
                        val b = c.hitBox(o)
                        nc.drawRoundRect(RectF(b[0] * W - 12f, b[1] * H - 12f, b[2] * W + 12f, b[3] * H + 12f), 28f, 28f,
                            Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 7f; color = 0xFF34548D.toInt(); pathEffect = DashPathEffect(floatArrayOf(22f, 14f), 0f) })
                    }
                }
                nc.restoreToCount(base)
            }
        }

        // -------------------------------------------------------------- top bar
        Column(Modifier.fillMaxWidth().safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoundIcon(Ic.Back, t("back"), size = 52.dp) { scope.launch { Autosave.flushAll(); state.back() } }
                Row(Modifier.clip(RoundedCornerShape(26.dp)).background(C.paper).border(2.dp, C.line, RoundedCornerShape(26.dp)).padding(4.dp)) {
                    ModeTab(t("world.play"), Ic.Play, c.mode == WorldMode.PLAY) { c.mode = WorldMode.PLAY; c.selected = null }
                    ModeTab(t("world.decorate"), Ic.Move, c.mode == WorldMode.DECORATE) { c.mode = WorldMode.DECORATE }
                }
                Spacer(Modifier.weight(1f))
                RoundIcon(Ic.Undo, t("tool.undo"), enabled = c.editor.canUndo.also { c.version }, size = 52.dp) { c.undo() }
                RoundIcon(Ic.Redo, t("tool.redo"), enabled = c.editor.canRedo.also { c.version }, size = 52.dp) { c.redo() }
                val night = c.doc.environment.timeOfDay == "night"
                RoundIcon(if (night) Ic.Sun else Ic.Moon, t(if (night) "world.day" else "world.night"), size = 52.dp) {
                    c.apply(SceneOp.SetEnvironment(SceneEnvironment(if (night) "day" else "night", c.doc.environment.weather)))
                }
            }
            if (newer.isNotEmpty()) {
                Panel(Modifier.padding(horizontal = 12.dp), color = C.butter) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(t("world.use_new"), style = T.label, modifier = Modifier.weight(1f))
                        SoftButton(t("world.keep_old")) { newer = emptyMap() }
                        Spacer(Modifier.width(8.dp))
                        SoftButton(t("world.use_new_yes"), Ic.Check, color = C.leaf) {
                            val m = newer; newer = emptyMap()
                            scope.launch {
                                m.forEach { (id, ref) -> withContext(Dispatchers.IO) { ToyVisual.resolve(g, state.pid, ref, c.doc.obj(id)?.templateId ?: "") }?.let { c.visuals[id] = it }; c.apply(SceneOp.Retarget(id, ref)) }
                            }
                        }
                    }
                }
            }
            StoryCard(state, c)
            val sel = c.selected?.let { c.doc.obj(it) }.also { c.version }
            if (sel != null && c.mode == WorldMode.DECORATE) {
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoundIcon(Ic.Plus, t("world.bigger"), size = 52.dp) { c.apply(SceneOp.Scale(sel.id, sel.transform.scale * 1.15f)) }
                    RoundIcon(Ic.Shapes, t("world.smaller"), size = 52.dp) { c.apply(SceneOp.Scale(sel.id, sel.transform.scale / 1.15f)) }
                    RoundIcon(Ic.Flip, t("world.flip"), size = 52.dp) { c.apply(SceneOp.Flip(sel.id)) }
                    RoundIcon(Ic.Front, t("world.front"), size = 52.dp) { c.apply(SceneOp.BringFront(sel.id)) }
                    RoundIcon(Ic.BackLayer, t("world.back"), size = 52.dp) { c.apply(SceneOp.SendBack(sel.id)) }
                    RoundIcon(Ic.Store, t("world.store"), size = 52.dp) { c.apply(SceneOp.Store(sel.id)); c.selected = null }
                }
            }
        }

        // ------------------------------------------------------------- toy tray
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().safeDrawingPadding()) {
            Row(Modifier.align(Alignment.End).padding(12.dp)) {
                BigButton(t("world.tray"), icon = Ic.Store, color = C.sunshine, height = 60.dp) { trayOpen = !trayOpen }
            }
            if (trayOpen) Tray(state, c) { ref, tid, v ->
                val before = c.doc.objects.map { it.id }.toSet()
                val x = (camX / c.painter.width + 0.12f + (c.doc.objects.size % 4) * 0.08f).coerceIn(0.08f, 0.92f)
                c.add(ref, tid, x)
                c.doc.objects.firstOrNull { it.id !in before }?.let { c.visuals[it.id] = v }
                trayOpen = false
            }
        }

        if (c.storyDone) RewardOverlay(state, c)
    }
}

@Composable
private fun ModeTab(text: String, ic: Ic, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.heightIn(min = 48.dp).pressable(onClick = onClick).clip(RoundedCornerShape(22.dp)).background(if (selected) C.butter else C.paper)
        .border(if (selected) 2.dp else 0.dp, if (selected) C.focus else C.paper, RoundedCornerShape(22.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        ColoraIcon(ic, size = 22.dp); Spacer(Modifier.width(6.dp)); Text(text, style = T.small)
    }
}

private data class TrayItem(val ref: String, val templateId: String, val state: PaintState?, val kind: String)

@Composable
private fun Tray(state: AppState, c: WorldController, onAdd: (String, String, ToyVisual) -> Unit) {
    val g = LocalContext.current.graph
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf("art") }
    val items by produceState(emptyList<TrayItem>(), tab, state.profile) {
        value = withContext(Dispatchers.IO) {
            when (tab) {
                "art" -> g.library.list(state.pid, DocKind.ARTWORK).mapNotNull { s ->
                    val d = g.library.loadArtwork(state.pid, s.id)?.second ?: return@mapNotNull null
                    TrayItem(ToyRef.art(s.id, s.revisionId).toString(), d.templateId, PaintReducer.replay(d.commands), "art")
                }
                "chr" -> g.library.list(state.pid, DocKind.CHARACTER).mapNotNull { s -> g.library.loadCharacter(state.pid, s.id)?.let { TrayItem(ToyRef.character(s.id, s.revisionId).toString(), it.second.recipe.bodyId, null, "chr") } }
                else -> g.content.extras.props.map { TrayItem(ToyRef.prop(it.id).toString(), it.id, null, "prop") }
            }
        }
    }
    Panel(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), shape = RoundedCornerShape(D.panelRadius), padding = PaddingValues(10.dp)) {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(t("world.tray"), tab == "art", icon = Ic.Crayon) { tab = "art" }
                Chip(t("world.characters"), tab == "chr", icon = Ic.Character) { tab = "chr" }
                Chip(t("world.props"), tab == "prop", icon = Ic.Shapes) { tab = "prop" }
            }
            Spacer(Modifier.height(8.dp))
            if (items.isEmpty()) Text(t("world.empty_tray"), style = T.label, modifier = Modifier.padding(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items, key = { it.ref }) { it2 ->
                    Box(Modifier.size(96.dp).pressable {
                        scope.launch { withContext(Dispatchers.IO) { ToyVisual.resolve(g, state.pid, it2.ref, it2.templateId) }?.let { v -> onAdd(it2.ref, it2.templateId, v) } }
                    }.clip(RoundedCornerShape(20.dp)).background(C.sand).padding(6.dp)) {
                        when (it2.kind) {
                            "art" -> ArtView(remember(it2.templateId) { g.content.art(it2.templateId) }, it2.state, Modifier.fillMaxSize())
                            "prop" -> AssetThumb("thumbs/${it2.templateId}.webp", cl("prop.${it2.templateId.removePrefix("prop.")}"), Modifier.fillMaxSize())
                            else -> CharacterThumb(state, it2.ref, Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterThumb(state: AppState, ref: String, modifier: Modifier) {
    val g = LocalContext.current.graph
    val bmp by produceState<android.graphics.Bitmap?>(null, ref) {
        value = withContext(Dispatchers.IO) {
            val r = ToyRef.parse(ref) ?: return@withContext null
            val d = r.revId?.let { g.library.characterRevision(state.pid, r.docId, it) } ?: g.library.loadCharacter(state.pid, r.docId)?.second ?: return@withContext null
            com.colora.world.render.CharacterRenderer.bake(g.content, d.recipe, 0.25f)
        }
    }
    BitmapImage(bmp, null, modifier)
}

@Composable
private fun StoryCard(state: AppState, c: WorldController) {
    val def = c.story ?: return
    val p = c.progress ?: return
    if (p.completed) return
    val step = StoryRuntime.currentStep(def, p) ?: return
    Panel(Modifier.padding(horizontal = 12.dp, vertical = 4.dp).widthIn(max = 560.dp), color = C.paper, shape = RoundedCornerShape(D.cardRadius)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AssetThumb("thumbs/milo.guide.webp", null, Modifier.size(56.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    def.steps.indices.forEach { i -> Box(Modifier.size(width = 22.dp, height = 8.dp).clip(CircleShape).background(if (i < p.stepIndex) C.turquoise else if (i == p.stepIndex) C.sunshine else C.line)) }
                }
                Spacer(Modifier.height(4.dp))
                Text(cl(step.textKey), style = T.label)
            }
            val icon = (step.subject.templates + step.subject.fallbacks).firstOrNull()
            if (icon != null) AssetThumb("thumbs/$icon.webp", null, Modifier.size(52.dp))
            Spacer(Modifier.width(6.dp))
            RoundIcon(Ic.Right, t("stories.skip"), size = 48.dp) { c.skipStep() }
        }
    }
}

@Composable
private fun RewardOverlay(state: AppState, c: WorldController) {
    val reward = c.reward ?: ""
    Box(Modifier.fillMaxSize().background(C.ink.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
        Panel(Modifier.padding(24.dp).widthIn(max = 460.dp), shape = RoundedCornerShape(D.panelRadius), padding = PaddingValues(22.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AssetThumb("thumbs/milo.happy.webp", null, Modifier.size(110.dp))
                Text(t("stories.done"), style = T.title, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (reward.startsWith("sticker:")) AssetThumb("thumbs/${reward.removePrefix("sticker:")}.webp", null, Modifier.size(64.dp)) else ColoraIcon(Ic.Book, size = 52.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(t("stories.reward_got") + " " + t(if (reward.startsWith("sticker:")) "reward.sticker" else "reward.book_page"), style = T.label)
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SoftButton(t("stories.home"), Ic.Home) { state.home() }
                    BigButton(t("stories.keep_playing"), icon = Ic.Play, height = 56.dp) { c.storyDone = false; c.story = null }
                }
            }
        }
    }
}

/** UI-08: seven places, recent scenes per place, new scene. */
@Composable
fun WorldPickerScreen(state: AppState, s: Screen.WorldPicker) {
    val g = LocalContext.current.graph
    val scope = rememberCoroutineScope()
    val scenes by produceState(emptyList<Pair<String, SceneDocument>>(), state.profile) {
        value = withContext(Dispatchers.IO) { g.library.list(state.pid, DocKind.SCENE).mapNotNull { sm -> g.library.loadScene(state.pid, sm.id)?.let { sm.id to it } } }
    }
    fun open(worldId: String, fresh: Boolean) {
        scope.launch {
            val existing = scenes.firstOrNull { it.second.worldId == worldId && it.second.storyId == null }
            val id = if (!fresh && existing != null) existing.first else withContext(Dispatchers.IO) {
                val d = SceneDocument(id = Library.newDocId("s"), profileId = state.pid, worldId = worldId)
                g.library.saveScene(state.pid, d); d.id
            }
            if (scenes.size >= 30 && fresh) state.showToast(com.colora.world.ui.Strings.get(state.lang, "world.scene_limit"))
            state.replace(Screen.World(id, s.addRef, s.addTemplate))
        }
    }
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        TopBar(t("world.pick"), { state.back() }, t("back"))
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(androidx.compose.foundation.lazy.grid.GridCells.Adaptive(300.dp), contentPadding = PaddingValues(D.gutter),
            horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(g.content.extras.worlds.size) { i ->
                val w = g.content.extras.worlds[i]
                val mine = scenes.count { it.second.worldId == w.id }
                Panel(Modifier.pressable { open(w.id, false) }, padding = PaddingValues(8.dp)) {
                    Column {
                        AssetThumb(w.thumb, cl(w.labelKey), Modifier.fillMaxWidth().aspectRatio(2f).clip(RoundedCornerShape(18.dp)), androidx.compose.ui.layout.ContentScale.Crop)
                        Row(Modifier.padding(top = 8.dp, start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(cl(w.labelKey), style = T.heading, modifier = Modifier.weight(1f))
                            if (mine > 0) Text("$mine", style = T.small, modifier = Modifier.clip(CircleShape).background(C.mint).padding(horizontal = 10.dp, vertical = 4.dp))
                            Spacer(Modifier.width(6.dp))
                            SoftButton(t("world.new_scene"), Ic.Plus) { open(w.id, true) }
                        }
                    }
                }
            }
        }
    }
}

/** UI-07: the artwork comes alive with the child's own colours and strokes. */
@Composable
fun RevealScreen(state: AppState, docId: String) {
    val g = LocalContext.current.graph
    // optional take-turns reminder for families sharing one device (parent setting)
    LaunchedEffect(docId) {
        if (g.settings.v.turnReminder && withContext(Dispatchers.IO) { g.library.profiles().size } > 1) {
            kotlinx.coroutines.delay(2500); state.showToast(com.colora.world.ui.Strings.get(state.lang, "turn.next"))
        }
    }
    val visual by produceState<ToyVisual?>(null, docId) {
        value = withContext(Dispatchers.IO) {
            val ptr = g.library.store.pointer(state.pid, docId) ?: return@withContext null
            val d = g.library.loadArtwork(state.pid, docId)?.second ?: return@withContext null
            ToyVisual.resolve(g, state.pid, ToyRef.art(docId, ptr.currentRevisionId).toString(), d.templateId)
        }
    }
    var playStart by remember { mutableLongStateOf(SystemClock.uptimeMillis()) }
    var tick by remember { mutableLongStateOf(0L) }
    val reduced = g.settings.v.reducedMotion
    LaunchedEffect(visual) { visual?.let { g.audio.toySound(it.audioSetId, it.preset); playStart = SystemClock.uptimeMillis() }; while (true) { withFrameMillis { tick = it } } }
    Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.radialGradient(listOf(C.butter, C.bgCream), radius = 1400f)).safeDrawingPadding()) {
        val v = visual
        Canvas(Modifier.fillMaxSize().pointerInput(v) { awaitEachGesture { awaitFirstDown(); playStart = SystemClock.uptimeMillis(); v?.let { g.audio.toySound(it.audioSetId, it.preset) } } }
            .semantics { contentDescription = "toy" }) {
            tick
            val vv = v ?: return@Canvas
            val el = (SystemClock.uptimeMillis() - playStart) / 1000f
            val energy = when { reduced -> if (el < 1.2f) 0.6f else 0f; el < 1.4f -> 1f; else -> 0.35f }
            val cb = vv.art.def.contentBounds
            val h = cb[3] - cb[1]; val w = cb[2] - cb[0]
            val k = minOf(size.height * 0.55f / h, size.width * 0.8f / w)
            val footX = size.width / 2f; val footY = size.height * 0.72f
            drawIntoCanvas { cv ->
                val nc = cv.nativeCanvas
                nc.drawOval(RectF(footX - w * k * 0.45f, footY - 18f, footX + w * k * 0.45f, footY + 22f), Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x2224324B })
                val hop = if (!reduced && el < 1.4f) sin(el / 1.4f * Math.PI.toFloat()) * 40f else 0f
                val s = nc.save()
                nc.translate(footX, footY - hop); nc.scale(k, k); nc.translate(-(cb[0] + cb[2]) / 2f, -vv.footY)
                vv.draw(nc, el, energy, el < 1.4f, 0)
                nc.restoreToCount(s)
            }
        }
        Milo("happy", t("reveal.tap"), Modifier.align(Alignment.TopStart).padding(16.dp), size = 72.dp)
        Row(Modifier.align(Alignment.BottomCenter).padding(20.dp).fillMaxWidth().widthIn(max = 640.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SoftButton(t("reveal.color_more"), Ic.Crayon, Modifier.weight(1f).height(64.dp)) {
                val tid = v?.templateId ?: return@SoftButton
                state.replace(Screen.Studio(tid, docId))
            }
            BigButton(t("reveal.to_world"), Modifier.weight(1.3f), icon = Ic.World, color = C.turquoise) {
                val vv = v ?: return@BigButton
                state.replace(Screen.WorldPicker(vv.ref, vv.templateId))
            }
        }
    }
}
