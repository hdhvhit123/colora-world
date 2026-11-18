package com.colora.world.ui.screens

import android.graphics.Bitmap
import android.graphics.Matrix
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.colora.world.data.BookDoc
import com.colora.world.data.BookItem
import com.colora.world.data.BookPage
import com.colora.world.data.CharacterDoc
import com.colora.world.data.LabDoc
import com.colora.world.data.Library
import com.colora.world.data.ToyRef
import com.colora.world.engine.character.CharacterRecipe
import com.colora.world.engine.character.PartChoice
import com.colora.world.engine.lab.ColorMixer
import com.colora.world.engine.paint.BrushId
import com.colora.world.engine.paint.Stroke
import com.colora.world.graph
import com.colora.world.render.BookRenderer
import com.colora.world.render.BrushPainter
import com.colora.world.render.CharacterRenderer
import com.colora.world.storage.DocKind
import com.colora.world.ui.AppState
import com.colora.world.ui.Screen
import com.colora.world.ui.Strings
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
import kotlin.random.Random

// ----------------------------------------------------------------------- colour lab (UI-12)
@Composable
fun LabScreen(state: AppState) {
    val g = LocalContext.current.graph
    val scope = rememberCoroutineScope()
    val drops = remember { mutableStateListOf<ColorMixer.Ryb>() }
    var white by remember { mutableFloatStateOf(0f) }
    var brush by remember { mutableStateOf(BrushId.CRAYON) }
    val strokes = remember { mutableStateListOf<Stroke>() }
    var live by remember { mutableStateOf<List<Float>>(emptyList()) }
    val mixed = if (drops.isEmpty()) null else ColorMixer.mix(drops, white)
    fun addDrop(d: ColorMixer.Ryb) { if (drops.size >= 3) drops.removeAt(0); drops.add(d); g.audio.sfx("sfx.water_pour", 250) }

    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        TopBar(t("lab.title"), { state.back() }, t("back"))
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth > maxHeight
            val mixer: @Composable (Modifier) -> Unit = { mod ->
                Panel(mod, shape = RoundedCornerShape(D.panelRadius)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            listOf(Triple(ColorMixer.RED, 0xFFEE4036.toInt(), "red"), Triple(ColorMixer.YELLOW, 0xFFFFD933.toInt(), "yellow"), Triple(ColorMixer.BLUE, 0xFF3373D9.toInt(), "blue")).forEach { (ryb, col, nm) ->
                                Pot(Color(col), t("color.$nm")) { addDrop(ryb) }
                            }
                            Pot(Color.White, t("lab.white")) { white = (white + 0.2f).coerceAtMost(0.8f) }
                        }
                        Spacer(Modifier.height(14.dp))
                        // mixing bowl
                        Box(Modifier.size(150.dp).clip(CircleShape).background(C.sand).border(4.dp, C.ink, CircleShape), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(118.dp).clip(CircleShape).background(mixed?.let { Color(it) } ?: C.paper).border(2.dp, C.ink.copy(alpha = 0.2f), CircleShape))
                            Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                drops.forEach { d -> Box(Modifier.size(14.dp).clip(CircleShape).background(Color(ColorMixer.rybToRgb(d))).border(1.dp, C.ink, CircleShape)) }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        if (mixed != null) Text(t("color.${ColorMixer.family(mixed)}"), style = T.title)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SoftButton(t("lab.reset"), Ic.Reset) { drops.clear(); white = 0f; strokes.clear() }
                            BigButton(t("lab.save"), icon = Ic.Check, height = 56.dp, enabled = mixed != null) {
                                val c = mixed ?: return@BigButton
                                scope.launch {
                                    val full = withContext(Dispatchers.IO) { val d = g.library.loadLab(state.pid); g.library.saveLab(state.pid, d.copy(swatches = (d.swatches - c) + c)); d.swatches.size >= ColorMixer.MAX_CUSTOM_SWATCHES }
                                    state.showToast(Strings.get(state.lang, if (full) "lab.full" else "lab.kept"))
                                }
                            }
                        }
                    }
                }
            }
            val paper: @Composable (Modifier) -> Unit = { mod ->
                Column(mod) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(t("lab.try"), style = T.label, modifier = Modifier.weight(1f))
                        listOf(BrushId.CRAYON to Ic.Crayon, BrushId.MARKER to Ic.Marker, BrushId.WATERCOLOR to Ic.Water).forEach { (b, ic) ->
                            RoundIcon(ic, b.name, selected = brush == b, size = 52.dp, accent = mixed?.let { Color(it) } ?: C.coral) { brush = b }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Canvas(Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)).background(C.paper).border(2.dp, C.line, RoundedCornerShape(24.dp)).semantics { contentDescription = "lab paper" }
                        .pointerInput(mixed, brush) {
                            awaitEachGesture {
                                val d = awaitFirstDown(); val col = mixed ?: return@awaitEachGesture
                                val k = 1024f / size.width
                                val pts = mutableListOf(d.position.x * k, d.position.y * k, 1f, 0f)
                                live = pts.toList()
                                while (true) {
                                    val ev = awaitPointerEvent(); val ch = ev.changes.first()
                                    if (!ch.pressed) break
                                    if (ch.positionChanged()) { pts += listOf(ch.position.x * k, ch.position.y * k, 1f, 16f); live = pts.toList(); ch.consume() }
                                }
                                strokes += Stroke(brush, 1, com.colora.world.engine.paint.Brushes.defaultSize(brush) * 1.6f, col, 1f, Random.nextLong(), null, null, pts.toList())
                                if (strokes.size > 40) strokes.removeAt(0)
                                live = emptyList()
                            }
                        }) {
                        val k = size.width / 1024f
                        drawIntoCanvas { cv ->
                            val nc = cv.nativeCanvas
                            val s = nc.save(); nc.scale(k, k)
                            val layer = nc.saveLayer(0f, 0f, 1024f, size.height / k, null)
                            strokes.forEach { BrushPainter.draw(nc, it) }
                            val col = mixed
                            if (live.size >= 4 && col != null) BrushPainter.draw(nc, Stroke(brush, 1, com.colora.world.engine.paint.Brushes.defaultSize(brush) * 1.6f, col, 1f, 1L, null, null, live))
                            nc.restoreToCount(layer); nc.restoreToCount(s)
                        }
                    }
                }
            }
            if (wide) Row(Modifier.fillMaxSize().padding(D.gutter), horizontalArrangement = Arrangement.spacedBy(16.dp)) { mixer(Modifier.weight(1f)); paper(Modifier.weight(1f).fillMaxHeight()) }
            else Column(Modifier.fillMaxSize().padding(D.gutter), verticalArrangement = Arrangement.spacedBy(14.dp)) { mixer(Modifier.fillMaxWidth()); paper(Modifier.fillMaxWidth().weight(1f)) }
        }
    }
}

@Composable
private fun Pot(color: Color, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(64.dp).semantics { contentDescription = label }.pressable(onClick = onClick).clip(RoundedCornerShape(20.dp, 20.dp, 28.dp, 28.dp)).background(color).border(3.dp, C.ink, RoundedCornerShape(20.dp, 20.dp, 28.dp, 28.dp)),
            contentAlignment = Alignment.Center) { ColoraIcon(Ic.Water, size = 30.dp, accent = color) }
        Text(label, style = T.small)
    }
}

// -------------------------------------------------------------------- builder (UI-13)
@Composable
fun BuilderScreen(state: AppState, docId: String?) {
    val g = LocalContext.current.graph
    val scope = rememberCoroutineScope()
    val pack = g.content.characters
    var id by remember { mutableStateOf(docId ?: Library.newDocId("c")) }
    var recipe by remember { mutableStateOf(CharacterRecipe(id = id, bodyId = pack.bodies.first().id, preset = pack.bodies.first().preset)) }
    LaunchedEffect(docId) { if (docId != null) withContext(Dispatchers.IO) { g.library.loadCharacter(state.pid, docId)?.second?.recipe }?.let { recipe = it } }
    var kind by remember { mutableStateOf("body") }
    var color by remember { mutableIntStateOf(0xFFFFA94D.toInt()) }
    var tryMove by remember { mutableLongStateOf(0L) }
    var tick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(tryMove) { if (tryMove > 0) { val end = System.currentTimeMillis() + 1500; while (System.currentTimeMillis() < end) withFrameMillis { tick = it }; tick = 0 } }
    val undo = remember { mutableStateListOf<CharacterRecipe>() }
    fun set(r: CharacterRecipe) { undo.add(recipe); if (undo.size > 30) undo.removeAt(0); recipe = r }

    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        TopBar(t("builder.title"), { state.back() }, t("back")) {
            RoundIcon(Ic.Undo, t("tool.undo"), enabled = undo.isNotEmpty(), size = 52.dp) { recipe = undo.removeAt(undo.size - 1) }
        }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth > maxHeight
            val stage: @Composable (Modifier) -> Unit = { mod ->
                Box(mod.clip(RoundedCornerShape(D.panelRadius)).background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(C.skyPale, C.mint)))) {
                    Canvas(Modifier.fillMaxSize().semantics { contentDescription = "character" }.pointerInput(recipe, color) {
                        awaitEachGesture {
                            val d = awaitFirstDown()
                            val k = minOf(size.width, size.height) / 1024f * 0.9f
                            val ox = size.width / 2f - 512f * k; val oy = size.height / 2f - 512f * k
                            val x = (d.position.x - ox) / k; val y = (d.position.y - oy) / k
                            // topmost layer containing the point gets the colour
                            for (l in CharacterRenderer.layers(g.content, recipe).reversed()) {
                                val inv = Matrix(); l.matrix.invert(inv); val p = floatArrayOf(x, y); inv.mapPoints(p)
                                val rid = l.art.geometry.hitTest(p[0], p[1], 6f) ?: continue
                                set(if (l.socket == null) recipe.copy(bodyFills = recipe.bodyFills + (rid to color))
                                    else recipe.copy(parts = recipe.parts + (l.socket to recipe.parts[l.socket]!!.let { it.copy(fills = it.fills + (rid to color)) })))
                                g.audio.sfx("ui.fill_pop"); break
                            }
                        }
                    }) {
                        tick
                        val k = minOf(size.width, size.height) / 1024f * 0.9f
                        drawIntoCanvas { cv ->
                            val nc = cv.nativeCanvas; val s = nc.save()
                            nc.translate(size.width / 2f - 512f * k, size.height / 2f - 512f * k)
                            val hop = if (tick > 0) kotlin.math.abs(kotlin.math.sin(tick / 160f)) * 60f else 0f
                            nc.translate(0f, -hop); nc.scale(k, k)
                            CharacterRenderer.draw(nc, g.content, recipe)
                            nc.restoreToCount(s)
                        }
                    }
                    Text(t("builder.paint"), style = T.small, modifier = Modifier.align(Alignment.TopCenter).padding(8.dp).clip(RoundedCornerShape(12.dp)).background(C.paper.copy(alpha = 0.85f)).padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
            val controls: @Composable (Modifier) -> Unit = { mod ->
                Column(mod.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("body", "ear", "tail", "wing", "hat", "decor").forEach { k -> Chip(t("builder.$k"), kind == k) { kind = k } }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (kind == "body") items(pack.bodies, key = { it.id }) { b ->
                            OptionTile(b.thumb, cl("part.${b.id.removePrefix("char.")}"), recipe.bodyId == b.id) { set(recipe.copy(bodyId = b.id, preset = b.preset, bodyFills = emptyMap())) }
                        } else {
                            val socket = CharacterRenderer.socketOf(kind)
                            item { OptionTile(null, t("builder.none"), recipe.parts[socket] == null) { set(CharacterRenderer.withPart(recipe, socket, null)) } }
                            items(pack.parts.filter { it.kind == kind }, key = { it.id }) { p ->
                                OptionTile(p.thumb, cl("part.${p.id.removePrefix("char.")}"), recipe.parts[socket]?.partId == p.id) { set(CharacterRenderer.withPart(recipe, socket, p.id)) }
                            }
                        }
                    }
                    if (kind != "body") recipe.parts[CharacterRenderer.socketOf(kind)]?.let { choice ->
                        val spec = pack.parts.first { it.id == choice.partId }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(t("builder.size"), style = T.label)
                            listOf(spec.minScale, 1f, spec.maxScale).forEach { sc ->
                                Chip("×%.1f".format(sc), kotlin.math.abs(choice.scale - sc) < 0.01f) { set(recipe.copy(parts = recipe.parts + (CharacterRenderer.socketOf(kind) to choice.copy(scale = sc)))) }
                            }
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val cols = g.content.palettes.palettes.first { it.id == "bright_basic" }.colors + g.content.palettes.palettes.first { it.id == "pastel" }.colors
                        items(cols.size) { i ->
                            val c = com.colora.world.render.ArtModel.parseColor(cols[i]); val sel = c == color
                            Box(Modifier.size(50.dp).pressable(sound = false) { color = c }, contentAlignment = Alignment.Center) {
                                Box(Modifier.size(if (sel) 44.dp else 38.dp).clip(CircleShape).background(Color(c)).border(if (sel) 4.dp else 2.dp, if (sel) C.focus else C.ink.copy(alpha = 0.15f), CircleShape))
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SoftButton(t("builder.try"), Ic.Play, Modifier.weight(1f)) { tryMove = System.currentTimeMillis(); g.audio.sfx("sfx.robot_dance", 500) }
                        BigButton(t("builder.save"), Modifier.weight(1f), icon = Ic.Check, height = 60.dp) {
                            scope.launch {
                                val ok = withContext(Dispatchers.IO) { runCatching { g.library.saveCharacter(state.pid, id, CharacterDoc(recipe = recipe.copy(id = id))) }.isSuccess }
                                state.showToast(Strings.get(state.lang, if (ok) "builder.saved" else "studio.save_error"))
                            }
                        }
                    }
                }
            }
            if (wide) Row(Modifier.fillMaxSize().padding(D.gutter), horizontalArrangement = Arrangement.spacedBy(16.dp)) { stage(Modifier.weight(1f).fillMaxHeight()); controls(Modifier.weight(1f)) }
            else Column(Modifier.fillMaxSize().padding(D.gutter), verticalArrangement = Arrangement.spacedBy(12.dp)) { stage(Modifier.fillMaxWidth().weight(1f)); controls(Modifier.fillMaxWidth()) }
        }
    }
}

@Composable
private fun OptionTile(thumb: String?, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(Modifier.width(92.dp).pressable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(84.dp).clip(RoundedCornerShape(20.dp)).background(if (selected) C.butter else C.paper).border(if (selected) 3.dp else 2.dp, if (selected) C.focus else C.line, RoundedCornerShape(20.dp)).padding(6.dp),
            contentAlignment = Alignment.Center) {
            if (thumb != null) AssetThumb(thumb, null, Modifier.fillMaxSize()) else ColoraIcon(Ic.Close, size = 36.dp)
        }
        Text(label, style = T.small, textAlign = TextAlign.Center, maxLines = 2)
    }
}

// ------------------------------------------------------------------------- book (UI-15)
@Composable
fun BookScreen(state: AppState, bookId: String) {
    val g = LocalContext.current.graph
    val scope = rememberCoroutineScope()
    var book by remember { mutableStateOf<BookDoc?>(null) }
    var removed by remember { mutableStateOf<Pair<Int, BookPage>?>(null) }
    var picking by remember { mutableStateOf<Int?>(null) } // page index to add into; -1 = new page
    var preview by remember { mutableStateOf(false) }
    var current by remember { mutableIntStateOf(0) }
    LaunchedEffect(bookId) { book = withContext(Dispatchers.IO) { g.library.loadBook(state.pid, bookId) ?: BookDoc() } }
    val b = book ?: return
    fun update(n: BookDoc) { book = n; Autosave.schedule("book:${state.pid}:$bookId", 400) { g.library.saveBook(state.pid, bookId, book!!); true } }
    DisposableEffect(Unit) { onDispose { Autosave.flushAll() } }

    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(b.customTitle ?: t(b.titleKey), { state.back() }, t("back")) {
                RoundIcon(Ic.Book, t("book.preview"), size = 52.dp, enabled = b.pages.isNotEmpty()) { preview = true; current = 0 }
                Spacer(Modifier.width(8.dp))
                RoundIcon(Ic.Export, t("book.export"), size = 52.dp, enabled = b.pages.isNotEmpty()) { scope.launch { Autosave.flush("book:${state.pid}:$bookId"); state.gated(Screen.Export("book:$bookId")) } }
            }
            Text(if (b.pages.size in 4..12) t("book.pages", b.pages.size) else t("book.draft"), style = T.small.copy(color = C.inkSoft), modifier = Modifier.padding(horizontal = D.gutter))
            Row(Modifier.padding(horizontal = D.gutter, vertical = 6.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0xFFFF786D, 0xFF42C8BD, 0xFFB8A0F3, 0xFFFFD66B, 0xFF8BD4F6, 0xFF9DDC86).forEach { c ->
                    Box(Modifier.size(44.dp).pressable { update(b.copy(coverColor = c.toInt())) }.clip(CircleShape).background(Color(c.toInt())).border(if (b.coverColor == c.toInt()) 4.dp else 2.dp, if (b.coverColor == c.toInt()) C.focus else C.line, CircleShape))
                }
            }
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val pageW = ((maxHeight - 120.dp) * 0.72f).coerceIn(180.dp, 440.dp)
            LazyRow(Modifier.fillMaxSize(), contentPadding = PaddingValues(D.gutter), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(b.pages.size) { i ->
                    val page = b.pages[i]
                    Column(Modifier.width(pageW)) {
                        PageView(state, b, i, Modifier.fillMaxWidth().aspectRatio(0.72f))
                        Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            RoundIcon(Ic.Left, "←", size = 44.dp, enabled = i > 0) { val l = b.pages.toMutableList(); val p = l.removeAt(i); l.add(i - 1, p); update(b.copy(pages = l)) }
                            RoundIcon(Ic.Right, "→", size = 44.dp, enabled = i < b.pages.size - 1) { val l = b.pages.toMutableList(); val p = l.removeAt(i); l.add(i + 1, p); update(b.copy(pages = l)) }
                            RoundIcon(Ic.Copy, t("collection.duplicate"), size = 44.dp, enabled = b.pages.size < 12) { val l = b.pages.toMutableList(); l.add(i + 1, page); update(b.copy(pages = l)) }
                            RoundIcon(Ic.Plus, t("book.pick"), size = 44.dp, enabled = page.items.size < (if (page.layout == "two") 2 else 1)) { picking = i }
                            RoundIcon(Ic.Trash, t("book.remove"), size = 44.dp) { removed = i to page; update(b.copy(pages = b.pages.filterIndexed { j, _ -> j != i })) }
                        }
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("one", "two", "scene").forEach { lay -> Chip(t("book.layout.$lay"), page.layout == lay) { update(b.copy(pages = b.pages.mapIndexed { j, p -> if (j == i) p.copy(layout = lay, items = p.items.take(if (lay == "two") 2 else 1)) else p })) } }
                        }
                    }
                }
                if (b.pages.size < 12) item {
                    Box(Modifier.width(pageW * 0.7f).aspectRatio(0.72f).pressable { picking = -1 }.clip(RoundedCornerShape(20.dp)).background(C.butter).border(2.dp, C.line, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { ColoraIcon(Ic.Plus, size = 48.dp); Text(t("book.add_page"), style = T.label) }
                    }
                }
            }
            }
        }
        removed?.let { (i, p) ->
            Box(Modifier.fillMaxSize().padding(bottom = 24.dp), contentAlignment = Alignment.BottomCenter) {
                SoftButton(t("book.undo"), Ic.Undo, color = C.butter) { val l = b.pages.toMutableList(); l.add(i.coerceAtMost(l.size), p); update(b.copy(pages = l)); removed = null }
            }
            LaunchedEffect(removed) { kotlinx.coroutines.delay(6000); removed = null }
        }
        picking?.let { idx -> BookPicker(state, onDismiss = { picking = null }) { ref ->
            picking = null
            if (idx == -1) update(b.copy(pages = b.pages + BookPage(layout = if (ref.startsWith("scene")) "scene" else "one", items = listOf(BookItem(ref)))))
            else update(b.copy(pages = b.pages.mapIndexed { j, p -> if (j == idx) p.copy(items = p.items + BookItem(ref)) else p }))
        } }
        if (preview) {
            Box(Modifier.fillMaxSize().background(C.ink.copy(alpha = 0.85f)).pressable(sound = false) {}, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PageView(state, b, current, Modifier.widthIn(max = 420.dp).fillMaxWidth(0.85f).aspectRatio(0.72f))
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        RoundIcon(Ic.Left, "←", enabled = current > 0) { current--; g.audio.sfx("ui.page_turn") }
                        Text("${current + 1} / ${b.pages.size}", style = T.heading.copy(color = C.paper))
                        RoundIcon(Ic.Right, "→", enabled = current < b.pages.size - 1) { current++; g.audio.sfx("ui.page_turn") }
                        RoundIcon(Ic.Close, t("close")) { preview = false }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageView(state: AppState, b: BookDoc, index: Int, modifier: Modifier) {
    val g = LocalContext.current.graph
    val page = b.pages.getOrNull(index) ?: return
    val bmp by produceState<Bitmap?>(null, page, index) { value = withContext(Dispatchers.IO) { BookRenderer(g, state.pid, state.lang).pageBitmap(b, index, 520) } }
    Box(modifier.clip(RoundedCornerShape(16.dp)).background(C.paper).border(2.dp, C.line, RoundedCornerShape(16.dp))) { BitmapImage(bmp, t("book.page", index + 1), Modifier.fillMaxSize()) }
}

@Composable
private fun BookPicker(state: AppState, onDismiss: () -> Unit, onPick: (String) -> Unit) {
    val g = LocalContext.current.graph
    val items by produceState(emptyList<Pair<String, String>>(), state.profile) {
        value = withContext(Dispatchers.IO) {
            g.library.list(state.pid, DocKind.ARTWORK).map { "art:${it.id}:${it.revisionId}" to (it.sourceId ?: "") } +
                g.library.list(state.pid, DocKind.CHARACTER).map { "chr:${it.id}:${it.revisionId}" to "" } +
                g.library.list(state.pid, DocKind.SCENE).map { "scene:${it.id}:${it.revisionId}" to "" }
        }
    }
    Box(Modifier.fillMaxSize().background(C.ink.copy(alpha = 0.35f)).pressable(sound = false) { onDismiss() }, contentAlignment = Alignment.BottomCenter) {
        Panel(Modifier.fillMaxWidth().padding(8.dp).pressable(sound = false) {}, shape = RoundedCornerShape(D.panelRadius)) {
            Column {
                Text(t("book.pick"), style = T.heading)
                Spacer(Modifier.height(8.dp))
                if (items.isEmpty()) Text(t("collection.empty"), style = T.label)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.first }) { (ref, tid) ->
                        Box(Modifier.size(110.dp).pressable { onPick(ref) }.clip(RoundedCornerShape(18.dp)).background(C.sand).padding(6.dp)) {
                            when {
                                ref.startsWith("art") -> {
                                    val st by produceState<com.colora.world.engine.paint.PaintState?>(null, ref) {
                                        value = withContext(Dispatchers.IO) { ToyRef.parse(ref)?.let { r -> g.library.artworkRevision(state.pid, r.docId, r.revId!!) }?.let { com.colora.world.engine.paint.PaintReducer.replay(it.commands) } }
                                    }
                                    ArtView(remember(tid) { g.content.art(tid) }, st, Modifier.fillMaxSize())
                                }
                                ref.startsWith("chr") -> CharacterThumb(state, ref, Modifier.fillMaxSize())
                                else -> {
                                    val bmp by produceState<Bitmap?>(null, ref) { value = withContext(Dispatchers.IO) { ToyRef.parse(ref)?.let { r -> g.library.loadScene(state.pid, r.docId) }?.let { com.colora.world.render.SceneSnapshot(g, state.pid, it).bitmap(300) } } }
                                    BitmapImage(bmp, null, Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
