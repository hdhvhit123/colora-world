package com.colora.world.ui.screens

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.colora.world.Autosave
import com.colora.world.content.ContentRepo
import com.colora.world.engine.paint.BrushId
import com.colora.world.engine.paint.PaintCommand
import com.colora.world.engine.paint.PatternId
import com.colora.world.graph
import com.colora.world.render.ArtRenderer
import com.colora.world.render.BrushPainter
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
import kotlin.math.hypot
import kotlin.math.max

@Composable
fun StudioScreen(state: AppState, screen: Screen.Studio) {
    val ctx = LocalContext.current
    val g = ctx.graph
    val ctrl by produceState<StudioController?>(null, screen) {
        value = withContext(Dispatchers.IO) {
            val art = g.content.art(screen.templateId) ?: return@withContext null
            val existing = screen.docId?.let { g.library.loadArtwork(state.pid, it)?.second }
            StudioController(g, state.pid, art, if (existing != null) screen.docId else null, existing?.commands.orEmpty()).also { c ->
                screen.paletteId?.let { pid -> g.content.palettes.palettes.firstOrNull { it.id == pid }?.colors?.firstOrNull()?.let { c.color = com.colora.world.render.ArtModel.parseColor(it) } }
                if (state.profile?.skillMode == "little") c.sizeStep = 2
            }
        }
    }
    DisposableEffect(screen) { onDispose { Autosave.flushAll() } }
    val c = ctrl
    if (c == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Milo("curious", null) }
        return
    }
    StudioContent(state, c)
}

@Composable
private fun StudioContent(state: AppState, c: StudioController) {
    val g = LocalContext.current.graph
    val scope = rememberCoroutineScope()
    var confirmReset by remember { mutableStateOf(false) }
    val skill = state.profile?.skillMode ?: "explorer"
    val tools = if (skill == "little") listOf(Tool.FILL, Tool.CRAYON, Tool.ERASER)
        else listOf(Tool.FILL, Tool.CRAYON, Tool.MARKER, Tool.WATER, Tool.RAINBOW, Tool.PATTERN, Tool.STICKER, Tool.ERASER)
    val title = if (c.art.isFreePage) t("home.free") else cl("template.${c.art.def.id}")
    fun play() {
        scope.launch {
            val rev = c.flush()
            if (rev != null) state.replace(Screen.Reveal(c.docId)) else if (c.saveStatus == SaveStatus.ERROR) state.showToast(Strings_saveError(state)) else state.back()
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(C.bgCream).safeDrawingPadding()) {
        val landscape = maxWidth > maxHeight
        val topBar: @Composable () -> Unit = {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                RoundIcon(Ic.Back, t("back"), size = 52.dp) { scope.launch { c.flush(); state.back() } }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = T.heading, maxLines = 1)
                    SaveBadge(c.saveStatus)
                }
                RoundIcon(Ic.Undo, t("tool.undo"), enabled = c.doc.canUndo.also { c.version }, size = 52.dp) { c.undo() }
                Spacer(Modifier.width(8.dp))
                RoundIcon(Ic.Redo, t("tool.redo"), enabled = c.doc.canRedo.also { c.version }, size = 52.dp) { c.redo() }
                if (skill != "little") {
                    Spacer(Modifier.width(8.dp))
                    RoundIcon(Ic.Trash, t("tool.reset_page"), size = 52.dp) { confirmReset = true }
                    Spacer(Modifier.width(8.dp))
                    RoundIcon(Ic.Reset, t("tool.reset_region"), selected = c.tool == Tool.CLEAR_REGION, size = 52.dp) { c.tool = if (c.tool == Tool.CLEAR_REGION) Tool.FILL else Tool.CLEAR_REGION; if (c.tool == Tool.CLEAR_REGION) state.showToast(Strings_get(state, "confirm.reset_region")) }
                }
            }
        }
        val canvas: @Composable (Modifier) -> Unit = { mod -> PaperCanvas(c, state, mod) }
        val toolTray: @Composable (Boolean) -> Unit = { vertical ->
            val items: @Composable () -> Unit = {
                tools.forEach { tl -> ToolButton(tl, c) }
            }
            if (vertical) Column(Modifier.verticalScroll(rememberScrollState()).padding(6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { items() }
            else BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                // every tool visible at once: buttons shrink (never below 52 dp) instead of hiding off-screen
                val sz = ((maxWidth - 6.dp * (tools.size - 1)) / tools.size).coerceIn(52.dp, 64.dp)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) { tools.forEach { tl -> ToolButton(tl, c, sz) } }
            }
        }
        val playButton: @Composable (Modifier) -> Unit = { mod ->
            BigButton(t("studio.play"), mod, icon = Ic.Play, color = C.turquoise, height = 68.dp) { play() }
        }

        if (!landscape) {
            Column(Modifier.fillMaxSize()) {
                topBar()
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    canvas(Modifier.fillMaxSize())
                    playButton(Modifier.align(Alignment.BottomEnd).padding(12.dp))
                }
                toolTray(false)
                OptionsBar(c, state)
                PaletteBar(c, state, vertical = false)
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                topBar()
                Row(Modifier.weight(1f)) {
                    Panel(Modifier.padding(start = 8.dp, bottom = 8.dp).width(84.dp).fillMaxHeight(), padding = PaddingValues(4.dp)) { toolTray(true) }
                    Box(Modifier.weight(1f).fillMaxHeight()) { canvas(Modifier.fillMaxSize()) }
                    Column(Modifier.width(260.dp).fillMaxHeight().padding(end = 8.dp, bottom = 8.dp)) {
                        OptionsBar(c, state)
                        Box(Modifier.weight(1f)) { PaletteBar(c, state, vertical = true) }
                        playButton(Modifier.fillMaxWidth().padding(top = 8.dp))
                    }
                }
            }
        }
        if (confirmReset) ConfirmDialog(t("confirm.reset_page"), onYes = { confirmReset = false; c.push(PaintCommand.ClearPage) }, onNo = { confirmReset = false })
    }
}

private fun Strings_get(state: AppState, key: String) = com.colora.world.ui.Strings.get(state.lang, key)
private fun Strings_saveError(state: AppState) = Strings_get(state, "studio.save_error")

@Composable
private fun SaveBadge(s: SaveStatus) {
    val (text, color) = when (s) {
        SaveStatus.CLEAN -> "" to C.inkSoft
        SaveStatus.PENDING -> t("studio.saving") to C.inkSoft
        SaveStatus.SAVED -> t("studio.saved") to Color(0xFF2E7D5B)
        SaveStatus.ERROR -> t("studio.save_error") to C.coralDeep
    }
    if (text.isNotEmpty()) Row(verticalAlignment = Alignment.CenterVertically) {
        if (s == SaveStatus.SAVED) { ColoraIcon(Ic.Check, size = 14.dp, tint = color); Spacer(Modifier.width(4.dp)) }
        Text(text, style = T.small.copy(color = color), maxLines = 2)
    }
}

@Composable
private fun ToolButton(tl: Tool, c: StudioController, size: androidx.compose.ui.unit.Dp = 64.dp) {
    val (ic, key) = when (tl) {
        Tool.FILL -> Ic.Fill to "tool.fill"; Tool.CRAYON -> Ic.Crayon to "tool.crayon"; Tool.MARKER -> Ic.Marker to "tool.marker"
        Tool.WATER -> Ic.Water to "tool.water"; Tool.RAINBOW -> Ic.Rainbow to "tool.rainbow"; Tool.PATTERN -> Ic.Pattern to "tool.pattern"
        Tool.STICKER -> Ic.Sticker to "tool.sticker"; Tool.ERASER -> Ic.Eraser to "tool.eraser"; Tool.CLEAR_REGION -> Ic.Reset to "tool.reset_region"
    }
    val selected = c.tool == tl
    val label = t(key)
    Box(
        Modifier.size(size).semantics { contentDescription = label; this.selected = selected; role = Role.RadioButton }
            .pressable { c.tool = tl; c.graph.audio.sfx("ui.select_color") }
            .clip(RoundedCornerShape(20.dp)).background(if (selected) C.butter else C.paper)
            .border(if (selected) 3.dp else 2.dp, if (selected) C.focus else C.line, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center,
    ) { ColoraIcon(ic, size = size * 0.62f, accent = Color(c.color)) }
}

@Composable private fun cTool(key: String) = t(key)

@Composable
private fun OptionsBar(c: StudioController, state: AppState) {
    val b = c.tool.brush()
    if (b != null && b != BrushId.FILL) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(t("tool.size"), style = T.small, modifier = Modifier.padding(end = 8.dp))
            val sizeLabel = t("tool.size")
            for (i in 0..2) {
                val sel = c.sizeStep == i
                Box(Modifier.size(52.dp).semantics { this.selected = sel; contentDescription = "$sizeLabel ${i + 1}" }.pressable { c.sizeStep = i }
                    .clip(CircleShape).background(if (sel) C.butter else Color.Transparent).border(if (sel) 3.dp else 0.dp, if (sel) C.focus else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center) {
                    Box(Modifier.size((10 + i * 9).dp).clip(CircleShape).background(if (b == BrushId.ERASER) C.inkSoft else Color(c.color)).border(1.dp, C.ink.copy(alpha = 0.25f), CircleShape))
                }
            }
            if (b == BrushId.PATTERN) {
                Spacer(Modifier.width(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PatternId.entries.forEach { p -> Chip(t("pattern.${p.name}"), c.pattern == p) { c.pattern = p } }
                }
            }
        }
    }
    if (c.tool == Tool.STICKER) StickerBar(c)
}

@Composable
private fun StickerBar(c: StudioController) {
    val g = LocalContext.current.graph
    val groups = remember { g.content.extras.stickers.groupBy { it.group } }
    var group by remember { mutableStateOf(groups.keys.first()) }
    Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            groups.keys.forEach { k -> Chip(cl("stickergroup.$k"), group == k) { group = k } }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
            items(groups[group].orEmpty(), key = { it.id }) { s ->
                val sel = c.stickerId == s.id
                Box(Modifier.size(60.dp).semantics { selected = sel }.pressable { c.stickerId = s.id }.clip(RoundedCornerShape(16.dp))
                    .background(if (sel) C.butter else C.paper).border(if (sel) 3.dp else 2.dp, if (sel) C.focus else C.line, RoundedCornerShape(16.dp)).padding(6.dp)) {
                    AssetThumb(s.thumb, null, Modifier.fillMaxSize())
                }
            }
        }
        val sel = c.selectedSticker?.let { id -> c.state.stickers[id].also { c.version } }
        if (sel != null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            RoundIcon(Ic.Plus, t("world.bigger"), size = 48.dp) { c.transformSticker(sel.instanceId, sel.x, sel.y, (sel.scale * 1.2f).coerceAtMost(3f), sel.rotationDeg) }
            Box(Modifier.size(48.dp).pressable { c.transformSticker(sel.instanceId, sel.x, sel.y, (sel.scale / 1.2f).coerceAtLeast(0.3f), sel.rotationDeg) }.clip(CircleShape).background(C.paper).border(2.dp, C.line, CircleShape)
                .semantics { contentDescription = "smaller" }, contentAlignment = Alignment.Center) { Box(Modifier.width(18.dp).height(3.dp).background(C.ink)) }
            RoundIcon(Ic.Restore, "rotate", size = 48.dp) { c.transformSticker(sel.instanceId, sel.x, sel.y, sel.scale, sel.rotationDeg + 20f) }
            RoundIcon(Ic.Trash, t("tool.sticker_delete"), size = 48.dp) { c.removeSticker(sel.instanceId) }
            SoftButton(t("tool.done_sticker"), Ic.Check) { c.selectedSticker = null }
        }
    }
}

@Composable
private fun PaletteBar(c: StudioController, state: AppState, vertical: Boolean) {
    val g = LocalContext.current.graph
    val palettes = g.content.palettes.palettes
    var pal by remember { mutableStateOf(if (state.profile?.skillMode == "little") "bright_basic" else palettes.first().id) }
    val myColors by produceState(emptyList<Int>(), state.profile) { value = withContext(Dispatchers.IO) { g.library.loadLab(state.pid).swatches } }
    val colors: List<Int> = if (pal == "mine") myColors else palettes.first { it.id == pal }.colors.map { com.colora.world.render.ArtModel.parseColor(it) }
    Column(Modifier.fillMaxWidth().background(C.paper.copy(alpha = 0.6f)).padding(vertical = 6.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            palettes.forEach { p -> Chip(cl(p.labelKey), pal == p.id) { pal = p.id } }
            if (myColors.isNotEmpty()) Chip(t("tool.my_colors"), pal == "mine", icon = Ic.Lab) { pal = "mine" }
        }
        Spacer(Modifier.height(6.dp))
        val swatch: @Composable (Int) -> Unit = { col ->
            val sel = c.color == col
            Box(Modifier.size(52.dp).semantics { selected = sel; contentDescription = colorName(col) }.pressable(sound = false) { c.color = col; if (c.tool == Tool.ERASER || c.tool == Tool.STICKER || c.tool == Tool.CLEAR_REGION) c.tool = Tool.FILL; g.audio.sfx("ui.select_color") },
                contentAlignment = Alignment.Center) {
                Box(Modifier.size(if (sel) 46.dp else 40.dp).shadow(if (sel) 0.dp else 2.dp, CircleShape).clip(CircleShape).background(Color(col))
                    .border(if (sel) 4.dp else 2.dp, if (sel) C.focus else C.ink.copy(alpha = if (col == 0xFFFFFFFF.toInt()) 0.45f else 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                    if (sel) ColoraIcon(Ic.Check, size = 22.dp, tint = if (luma(col) < 0.45f) C.paper else C.ink)
                }
            }
        }
        if (vertical) {
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(androidx.compose.foundation.lazy.grid.GridCells.Adaptive(54.dp), contentPadding = PaddingValues(6.dp)) {
                items(colors.size) { i -> swatch(colors[i]) }
            }
        } else {
            // wrap into rows so every colour of the palette is visible without scrolling
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                val perRow = maxOf(1, ((maxWidth + 2.dp) / 54.dp).toInt())
                val rows = colors.chunked(if (colors.size > perRow) minOf(perRow, (colors.size + 1) / 2) else perRow)
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    rows.forEach { r -> Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) { r.forEach { swatch(it) } } }
                }
            }
        }
    }
}

fun luma(c: Int): Float = (0.299f * ((c shr 16) and 255) + 0.587f * ((c shr 8) and 255) + 0.114f * (c and 255)) / 255f

fun colorName(c: Int): String = "#%06X".format(c and 0xFFFFFF)

/** The paper: one-finger paint, two-finger pan/zoom (1–4×), tap to fill. */
@Composable
private fun PaperCanvas(c: StudioController, state: AppState, modifier: Modifier) {
    val g = LocalContext.current.graph
    val haptic = LocalHapticFeedback.current
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var gesture by remember { mutableStateOf(false) }
    var viewSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val flash = remember { Animatable(1f) }
    var flashAt by remember { mutableStateOf(Offset.Zero) }
    var flashRegion by remember { mutableStateOf<String?>(null) }
    var flashOld by remember { mutableIntStateOf(0) }
    val reduced = g.settings.v.reducedMotion
    LaunchedEffect(flashRegion, c.version) { if (flashRegion != null && !reduced) { flash.snapTo(0f); flash.animateTo(1f, tween(220)) } }

    fun matrix(w: Float, h: Float): Matrix {
        val cb = c.art.contentRect
        val size = max(cb.width(), cb.height()) * 1.08f
        val k = minOf(w, h) / size * zoom
        return Matrix().apply { setTranslate(-cb.centerX(), -cb.centerY()); postScale(k, k); postTranslate(w / 2f + pan.x, h / 2f + pan.y) }
    }
    fun toDesign(m: Matrix, p: Offset): FloatArray { val inv = Matrix(); m.invert(inv); val a = floatArrayOf(p.x, p.y); inv.mapPoints(a); return a }

    val fitVisible = zoom > 1.01f || pan != Offset.Zero
    Box(modifier) {
        Canvas(
            Modifier.fillMaxSize()
                .semantics {
                    contentDescription = "canvas"
                    if (c.tool == Tool.FILL) customActions = c.art.parts.flatMap { it.regions }.take(40).map { r ->
                        CustomAccessibilityAction(r.id.replace('_', ' ')) { c.push(PaintCommand.FillRegion(r.id, c.color)); true }
                    }
                }
                .pointerInput(c) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val m0 = matrix(size.width.toFloat(), size.height.toFloat())
                        val k0 = run { val v = FloatArray(9); m0.getValues(v); v[0] }
                        val tol = 22f / k0 * 3f
                        var p = toDesign(m0, down.position)
                        var multi = false
                        var moved = 0f
                        var stroking = c.tool.brush()?.let { c.begin(p[0], p[1], SystemClock.uptimeMillis(), tol) } ?: false
                        var dragSticker: String? = if (c.tool == Tool.STICKER) c.stickerAt(p[0], p[1]) else null
                        if (dragSticker != null) c.selectedSticker = dragSticker
                        val startPos = down.position
                        while (true) {
                            val ev = awaitPointerEvent()
                            val pressed = ev.changes.count { it.pressed }
                            if (pressed == 0) break
                            if (pressed >= 2) {
                                if (!multi) { multi = true; if (stroking) { c.cancel(); stroking = false }; gesture = true }
                                if (state.profile?.skillMode != "little") {
                                    val z = ev.calculateZoom(); val pn = ev.calculatePan(); val cen = ev.calculateCentroid()
                                    val nz = (zoom * z).coerceIn(1f, 4f)
                                    val real = nz / zoom
                                    val cx = size.width / 2f; val cy = size.height / 2f
                                    pan = Offset((pan.x - (cen.x - cx)) * real + (cen.x - cx) + pn.x, (pan.y - (cen.y - cy)) * real + (cen.y - cy) + pn.y)
                                    zoom = nz
                                    if (zoom <= 1.001f) pan = Offset.Zero
                                }
                                ev.changes.forEach { it.consume() }
                                continue
                            }
                            val ch = ev.changes.first { it.pressed }
                            if (!ch.positionChanged()) continue
                            moved = max(moved, (ch.position - startPos).getDistance())
                            val m = matrix(size.width.toFloat(), size.height.toFloat())
                            p = toDesign(m, ch.position)
                            if (stroking) c.extend(p[0], p[1], SystemClock.uptimeMillis(), 2.5f / zoom)
                            else if (dragSticker != null) {
                                val s = c.state.stickers[dragSticker]
                                if (s != null) c.transformSticker(dragSticker, p[0], p[1], s.scale, s.rotationDeg)
                            }
                            ch.consume()
                        }
                        gesture = false
                        if (multi) return@awaitEachGesture
                        if (stroking) { c.end(); return@awaitEachGesture }
                        if (moved < 24f) when (c.tool) {
                            Tool.FILL, Tool.CLEAR_REGION -> {
                                val before = c.version
                                val r = c.art.geometry.hitTest(p[0], p[1], tol)
                                flashOld = r?.let { c.state.fills[it] } ?: android.graphics.Color.WHITE
                                c.fillAt(p[0], p[1], tol)
                                if (c.version != before) {
                                    flashAt = Offset(p[0], p[1]); flashRegion = r
                                    if (g.settings.v.haptics) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                            Tool.STICKER -> if (dragSticker == null) c.placeSticker(p[0], p[1])
                            else -> Unit
                        }
                    }
                }
        ) {
            viewSize = size
            c.version; c.liveCount
            val w = size.width; val h = size.height
            val m = matrix(w, h)
            drawIntoCanvas { cv ->
                val nc = cv.nativeCanvas
                // paper sheet with a soft contact shadow
                val page = RectF(c.art.contentRect).apply { inset(-36f, -36f) }
                val pr = RectF(); m.mapRect(pr, if (c.art.isFreePage) RectF(24f, 24f, 1000f, 1000f) else page)
                val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x1A24324B }
                nc.drawRoundRect(RectF(pr.left + 4f, pr.top + 10f, pr.right + 4f, pr.bottom + 12f), 36f, 36f, shadow)
                nc.drawRoundRect(pr, 34f, 34f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE })
                // cached paint layer (fills, strokes, stickers)
                val layer = c.paintLayer(w.toInt().coerceAtLeast(1), h.toInt().coerceAtLeast(1), m, gesture)
                val delta = Matrix(); c.cacheMatrix.invert(delta); delta.postConcat(m)
                nc.drawBitmap(layer, delta, Paint(Paint.FILTER_BITMAP_FLAG))
                // fill spread effect: previous colour retreats from the tap point
                val fr = flashRegion
                if (fr != null && flash.value < 1f) {
                    val reg = c.art.region(fr)
                    if (reg != null) {
                        val s = nc.save(); nc.concat(m); nc.clipPath(c.art.visibleClip(fr) ?: reg.path)
                        val circle = Path().apply { addCircle(flashAt.x, flashAt.y, 40f + flash.value * 900f, Path.Direction.CW) }
                        nc.clipOutPath(circle)
                        nc.drawPaint(Paint().apply { color = flashOld })
                        nc.restoreToCount(s)
                    }
                }
                // live stroke, clipped to the region locked at pointer-down
                c.liveStroke()?.let { s ->
                    val sv = nc.save(); nc.concat(m)
                    c.art.visibleClip(s.clipRegionId ?: "")?.let { nc.clipPath(it) }
                    if (s.brush == BrushId.ERASER) {
                        val base = c.state.fills[s.clipRegionId] ?: android.graphics.Color.WHITE
                        BrushPainter.draw(nc, s.copy(brush = BrushId.MARKER, color = base))
                    } else BrushPainter.draw(nc, s)
                    nc.restoreToCount(sv)
                }
                // crisp vector outlines + details on top
                val so = nc.save(); nc.concat(m)
                for (part in c.art.parts) ArtRenderer.drawOutlines(nc, c.art, part)
                c.selectedSticker?.let { id -> c.state.stickers[id] }?.takeIf { c.tool == Tool.STICKER }?.let { st ->
                    val r = ArtRenderer.STICKER_UNITS * st.scale / 2f + 10f
                    nc.drawCircle(st.x, st.y, r, Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 6f; color = 0xFF34548D.toInt(); pathEffect = android.graphics.DashPathEffect(floatArrayOf(16f, 12f), 0f) })
                }
                nc.restoreToCount(so)
            }
        }
        if (fitVisible) RoundIcon(Ic.Fit, t("tool.fit"), Modifier.align(Alignment.TopEnd).padding(10.dp), size = 52.dp) { zoom = 1f; pan = Offset.Zero }
    }
}

@Composable
fun ConfirmDialog(text: String, onYes: () -> Unit, onNo: () -> Unit, yes: String? = null, no: String? = null) {
    Box(Modifier.fillMaxSize().background(C.ink.copy(alpha = 0.35f)).pressable(sound = false) { onNo() }, contentAlignment = Alignment.Center) {
        Panel(Modifier.padding(24.dp).widthIn(max = 460.dp).pressable(sound = false) {}, shape = RoundedCornerShape(D.panelRadius), padding = PaddingValues(20.dp)) {
            Column {
                Text(text, style = T.heading)
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SoftButton(no ?: t("no"), Ic.Close, Modifier.weight(1f)) { onNo() }
                    BigButton(yes ?: t("yes"), Modifier.weight(1f), icon = Ic.Check, height = 56.dp) { onYes() }
                }
            }
        }
    }
}
