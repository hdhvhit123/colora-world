package com.colora.world.ui.screens

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
                AssetThumb("thumbs/world.garden.webp", null, Modifier.fillMaxSize(), androidx.compose.ui.layout.ContentScale.Crop)
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
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeCard(t("home.world"), Ic.World, C.turquoise.copy(alpha = 0.35f), Modifier.weight(1f)) { state.go(Screen.WorldPicker()) }
                    HomeCard(t("home.collection"), Ic.Collection, C.lavender.copy(alpha = 0.45f), Modifier.weight(1f)) { state.go(Screen.Collection) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmallCard(t("home.stories"), Ic.Story, Modifier.weight(1f)) { state.go(Screen.Stories) }
                    SmallCard(t("home.ideas"), Ic.Star, Modifier.weight(1f)) { state.go(Screen.Ideas) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmallCard(t("home.lab"), Ic.Lab, Modifier.weight(1f)) { state.go(Screen.Lab) }
                    SmallCard(t("home.builder"), Ic.Character, Modifier.weight(1f)) { state.go(Screen.Builder()) }
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

@Composable
private fun HomeCard(text: String, ic: Ic, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Panel(modifier.height(118.dp).pressable(onClick = onClick), color = C.paper) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)).background(color)) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                ColoraIcon(ic, size = 44.dp)
                Spacer(Modifier.height(6.dp))
                Text(text, style = T.heading, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun SmallCard(text: String, ic: Ic, modifier: Modifier, onClick: () -> Unit) {
    Row(modifier.heightIn(min = 64.dp).pressable(onClick = onClick).clip(RoundedCornerShape(20.dp)).background(C.paper).border(2.dp, C.line, RoundedCornerShape(20.dp)).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        ColoraIcon(ic, size = 32.dp); Spacer(Modifier.width(10.dp)); Text(text, style = T.label)
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
