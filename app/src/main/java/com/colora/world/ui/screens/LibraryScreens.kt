package com.colora.world.ui.screens

import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.colora.world.content.ContentRepo
import com.colora.world.data.ArtworkDoc
import com.colora.world.data.DocSummary
import com.colora.world.engine.paint.PaintReducer
import com.colora.world.engine.paint.PaintState
import com.colora.world.graph
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
import kotlinx.coroutines.withContext

private val topicTints = listOf(C.rose, C.mint, C.butter, C.lilac, C.skyPale, C.sand)

@Composable
fun FirstRunScreen(state: AppState) {
    val picks = listOf("animals.cat" to "first.cat", "flowers.sunflower" to "first.flower", "vehicles.car" to "first.car")
    fun done(next: Screen?) {
        state.graph.settings.update { it.copy(onboarded = true) }
        state.home(); if (next != null) state.go(next)
    }
    Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(D.gutter), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(12.dp))
        Milo("greet", t("first.title"))
        Spacer(Modifier.height(24.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val wide = maxWidth > 600.dp
            val cardMod = if (wide) Modifier.width((maxWidth - 32.dp) / 3) else Modifier.fillMaxWidth()
            val content: @Composable () -> Unit = {
                picks.forEachIndexed { i, (id, label) ->
                    val entry = state.graph.content.template(id)
                    Panel(cardMod.padding(vertical = 6.dp).pressable { done(Screen.Studio(id)) }.semantics { contentDescription = "" }, color = topicTints[i]) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            if (entry != null) AssetThumb(entry.thumb, null, Modifier.size(if (wide) 200.dp else 150.dp))
                            Text(t(label), style = T.title, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            if (wide) Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { content() } else Column { content() }
        }
        Spacer(Modifier.height(16.dp))
        SoftButton(t("first.skip"), Ic.Home) { done(null) }
    }
}

@Composable
fun HomeScreen(state: AppState) {
    val ctx = LocalContext.current
    val g = ctx.graph
    val recent by produceState<Pair<DocSummary, PaintState>?>(null, state.profile) {
        value = withContext(Dispatchers.IO) {
            val pid = state.profile?.id ?: return@withContext null
            g.library.list(pid, DocKind.ARTWORK).firstOrNull()?.let { s -> g.library.loadArtwork(pid, s.id)?.let { s to PaintReducer.replay(it.second.commands) } }
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(C.skyPale, C.bgCream, C.bgCream))).safeDrawingPadding()) {
        val wide = maxWidth > maxHeight && maxWidth > 640.dp
        val hero: @Composable (Modifier) -> Unit = { mod ->
            Panel(mod, color = C.paper, padding = PaddingValues(0.dp), shape = RoundedCornerShape(D.panelRadius)) {
                val painter = remember { g.content.world("garden")?.let { com.colora.world.render.WorldPainter(it) } }
                androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                    val p = painter ?: return@Canvas
                    // vector world, cropped to fill and anchored on the ground so it stays crisp on tablets
                    val k = maxOf(size.width / p.width * 1.25f, size.height / p.height * 0.55f)
                    val top = (size.height / k - p.height).coerceAtLeast(0f)
                    drawIntoCanvas { cv -> val nc = cv.nativeCanvas; val sv = nc.save(); nc.translate((size.width - p.width * k) / 2f, size.height - p.height * k); nc.scale(k, k); p.draw(nc, 0f, false, parallax = false, extraTop = top); nc.restoreToCount(sv) }
                }
                val r = recent
                Box(Modifier.fillMaxSize().padding(14.dp)) {
                    if (r != null) {
                        val art = remember(r.first.sourceId) { r.first.sourceId?.let(g.content::art) }
                        Box(Modifier.align(Alignment.BottomEnd).size(150.dp).clip(RoundedCornerShape(24.dp)).background(C.paper.copy(alpha = 0.92f)).border(3.dp, C.paper, RoundedCornerShape(24.dp))
                            .pressable { state.go(Screen.Studio(r.first.sourceId ?: "", r.first.id)) }) {
                            ArtView(art, r.second, Modifier.fillMaxSize().padding(8.dp))
                            Text(t("home.continue"), style = T.small, modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp).clip(RoundedCornerShape(12.dp)).background(C.butter).padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                    Milo("greet", t("home.hello"), Modifier.align(Alignment.TopStart), size = 76.dp)
                }
            }
        }
        val actions: @Composable (Modifier) -> Unit = { mod ->
            Column(mod, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BigButton(t("home.color"), Modifier.fillMaxWidth(), icon = Ic.Crayon, height = 84.dp) { state.go(Screen.Topics) }
                // picture-book tiles: every destination shows its own illustration, so pre-readers can choose by picture
                val tiles = listOf<Pair<String, () -> Unit>>(
                    "world" to { state.go(Screen.WorldPicker()) }, "collection" to { state.go(Screen.Collection) },
                    "stories" to { state.go(Screen.Stories) }, "ideas" to { state.go(Screen.Ideas) },
                    "lab" to { state.go(Screen.Lab) }, "builder" to { state.go(Screen.Builder()) },
                )
                tiles.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { (key, go) ->
                            val col = when (key) { "world" -> C.turquoise; "collection" -> C.lavender; "stories" -> C.sky; "ideas" -> C.sunshine; "lab" -> C.rose; else -> C.leaf }
                            PictureTile(t("home.$key"), col, Modifier.weight(1f), go) {
                                when (key) {
                                    "world" -> WorldTileArt("world.garden")
                                    "collection" -> TileArt("animals.cat")
                                    "stories" -> { WorldTileArt("world.town"); TileArt("animals.rabbit", Modifier.align(Alignment.BottomCenter).fillMaxHeight(0.85f)) }
                                    "ideas" -> AssetThumb("thumbs/milo.curious.webp", null, Modifier.align(Alignment.Center).fillMaxHeight(0.9f))
                                    "lab" -> PotsArt()
                                    else -> AssetThumb("thumbs/char.body.fluffy.webp", null, Modifier.align(Alignment.Center).fillMaxHeight(0.9f))
                                }
                            }
                        }
                    }
                }
            }
        }
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = D.gutter, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Colora World", style = T.title.copy(color = C.ink), modifier = Modifier.weight(1f))
                RoundIcon(Ic.Lock, t("home.parent"), size = 48.dp) { state.gated(Screen.Parent) }
            }
            if (wide) {
                Row(Modifier.fillMaxSize().padding(D.gutter), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    hero(Modifier.weight(1.25f).fillMaxHeight())
                    actions(Modifier.weight(1f).verticalScroll(rememberScrollState()))
                }
            } else {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = D.gutter)) {
                    hero(Modifier.fillMaxWidth().aspectRatio(1.45f))
                    Spacer(Modifier.height(16.dp))
                    actions(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

/** A home tile: a coloured "page" with its own illustration and a big label underneath. */
@Composable
private fun PictureTile(label: String, color: Color, modifier: Modifier, onClick: () -> Unit, art: @Composable BoxScope.() -> Unit) {
    Column(modifier.semantics(mergeDescendants = true) { contentDescription = label }.pressable(onClick = onClick)
        .shadow(4.dp, RoundedCornerShape(26.dp), ambientColor = C.shadow, spotColor = C.shadow)
        .clip(RoundedCornerShape(26.dp)).background(C.paper).border(3.dp, color, RoundedCornerShape(26.dp)).padding(6.dp)) {
        Box(Modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = 0.32f)), content = art)
        Text(label, style = T.heading, textAlign = TextAlign.Center, maxLines = 2, modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp))
    }
}

@Composable
private fun BoxScope.TileArt(templateId: String, modifier: Modifier = Modifier.align(Alignment.Center).fillMaxHeight()) {
    val g = LocalContext.current.graph
    ArtView(remember(templateId) { g.content.art(templateId) }, null, modifier.aspectRatio(1f), useSamples = true, pad = 0.02f)
}

@Composable
private fun BoxScope.WorldTileArt(thumb: String) {
    AssetThumb("thumbs/$thumb.webp", null, Modifier.matchParentSize(), androidx.compose.ui.layout.ContentScale.Crop)
}

@Composable
private fun BoxScope.PotsArt() {
    Row(Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        listOf(0xFFEE4036, 0xFFFFD933, 0xFF3373D9).forEachIndexed { i, c ->
            Box(Modifier.size(if (i == 1) 54.dp else 44.dp).clip(RoundedCornerShape(16.dp, 16.dp, 22.dp, 22.dp)).background(Color(c.toInt())).border(3.dp, C.ink, RoundedCornerShape(16.dp, 16.dp, 22.dp, 22.dp)))
        }
    }
}

@Composable
fun TopicsScreen(state: AppState) {
    val g = LocalContext.current.graph
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        TopBar(t("topics.title"), { state.back() }, t("back"))
        LazyVerticalGrid(GridCells.Adaptive(158.dp), contentPadding = PaddingValues(D.gutter), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Panel(Modifier.pressable { state.go(Screen.Studio(ContentRepo.FREE_PAGE_ID)) }, color = C.paper) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.size(120.dp).clip(RoundedCornerShape(18.dp)).background(C.sand), contentAlignment = Alignment.Center) { ColoraIcon(Ic.Brush, size = 64.dp) }
                        Spacer(Modifier.height(6.dp)); Text(t("home.free"), style = T.label, textAlign = TextAlign.Center)
                    }
                }
            }
            items(g.content.catalog.topics, key = { it.id }) { topic ->
                val i = g.content.catalog.topics.indexOf(topic)
                Panel(Modifier.pressable { state.go(Screen.Templates(topic.id)) }, color = C.paper) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.size(120.dp).clip(RoundedCornerShape(18.dp)).background(topicTints[i % topicTints.size]), contentAlignment = Alignment.Center) {
                            AssetThumb(g.content.template(topic.coverTemplate)!!.thumb, null, Modifier.size(112.dp))
                        }
                        Spacer(Modifier.height(6.dp)); Text(cl(topic.labelKey), style = T.label, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun TemplatesScreen(state: AppState, topicId: String) {
    val g = LocalContext.current.graph
    val topic = g.content.catalog.topics.first { it.id == topicId }
    val owned by g.entitlements.owned.collectAsState()
    val existing by produceState<Map<String, List<DocSummary>>>(emptyMap(), state.profile) {
        value = withContext(Dispatchers.IO) { g.library.list(state.pid, DocKind.ARTWORK).groupBy { it.sourceId ?: "" } }
    }
    var choose by remember { mutableStateOf<String?>(null) }
    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(cl(topic.labelKey), { state.back() }, t("back"))
            LazyVerticalGrid(GridCells.Adaptive(150.dp), contentPadding = PaddingValues(D.gutter), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(topic.templates, key = { it }) { id ->
                    val e = g.content.template(id)!!
                    val locked = !(g.entitlements.allUnlocked || e.packId == "base" || e.packId in owned)
                    val mine = existing[id].orEmpty()
                    Panel(Modifier.pressable {
                        when {
                            locked -> state.gated(Screen.Parent)
                            mine.isEmpty() -> state.go(Screen.Studio(id))
                            else -> choose = id
                        }
                    }, color = C.paper, padding = PaddingValues(8.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Box {
                                AssetThumb(e.thumb, cl("template.$id"), Modifier.size(132.dp))
                                if (locked) Box(Modifier.align(Alignment.TopEnd).size(40.dp).clip(CircleShape).background(C.butter), contentAlignment = Alignment.Center) { ColoraIcon(Ic.Lock, size = 26.dp) }
                                if (mine.isNotEmpty()) Box(Modifier.align(Alignment.TopStart).clip(RoundedCornerShape(12.dp)).background(C.leaf).padding(horizontal = 8.dp, vertical = 2.dp)) { ColoraIcon(Ic.Check, size = 18.dp) }
                                if (mine.any { it.pointer.favorite }) Box(Modifier.align(Alignment.BottomStart)) { ColoraIcon(Ic.Heart, size = 24.dp) }
                            }
                            Text(cl("template.$id"), style = T.small, textAlign = TextAlign.Center, maxLines = 2)
                            LevelDots(e.difficulty)
                        }
                    }
                }
            }
        }
        choose?.let { id ->
            Box(Modifier.fillMaxSize().background(C.ink.copy(alpha = 0.35f)).pressable(sound = false) { choose = null }, contentAlignment = Alignment.Center) {
                Panel(Modifier.padding(24.dp).widthIn(max = 420.dp), shape = RoundedCornerShape(D.panelRadius)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AssetThumb(g.content.template(id)!!.thumb, null, Modifier.size(140.dp))
                        Spacer(Modifier.height(12.dp))
                        val latest = existing[id]!!.first()
                        BigButton(t("templates.continue"), Modifier.fillMaxWidth(), icon = Ic.Crayon) { choose = null; state.go(Screen.Studio(id, latest.id)) }
                        Spacer(Modifier.height(10.dp))
                        SoftButton(t("templates.again"), Ic.Plus, Modifier.fillMaxWidth()) { choose = null; state.go(Screen.Studio(id)) }
                    }
                }
            }
        }
    }
}

@Composable
fun LevelDots(level: String) {
    val n = when (level) { "simple" -> 1; "medium" -> 2; else -> 3 }
    Row(Modifier.padding(top = 4.dp).semantics { contentDescription = level }, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { i -> Box(Modifier.size(9.dp).clip(CircleShape).background(if (i < n) C.coral else C.line)) }
    }
}
