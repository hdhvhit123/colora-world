package com.colora.world.ui.screens

import android.graphics.Bitmap
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.colora.world.content.ContentRepo
import com.colora.world.data.ArtworkDoc
import com.colora.world.data.BookDoc
import com.colora.world.data.DocSummary
import com.colora.world.data.Library
import com.colora.world.data.ToyRef
import com.colora.world.engine.paint.PaintReducer
import com.colora.world.engine.paint.PaintState
import com.colora.world.engine.prompts.Suggestions
import com.colora.world.engine.world.SceneDocument
import com.colora.world.graph
import com.colora.world.render.SceneSnapshot
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

// ------------------------------------------------------------------ stories (UI-10)
@Composable
fun StoriesScreen(state: AppState) {
    val g = LocalContext.current.graph
    val scope = rememberCoroutineScope()
    val progress by produceState(emptyMap<String, Pair<Int, Boolean>>(), state.profile) {
        value = withContext(Dispatchers.IO) { g.content.stories.stories.mapNotNull { s -> g.library.storyProgress(state.pid, s.id)?.let { s.id to (it.stepIndex to it.completed) } }.toMap() }
    }
    fun start(storyId: String, worldId: String) {
        scope.launch {
            val id = withContext(Dispatchers.IO) {
                val prev = g.library.storyProgress(state.pid, storyId)?.takeIf { !it.completed }
                if (prev != null && g.library.loadScene(state.pid, prev.sceneId) != null) prev.sceneId
                else SceneDocument(id = Library.newDocId("s"), profileId = state.pid, worldId = worldId, storyId = storyId).also { g.library.saveScene(state.pid, it) }.id
            }
            state.go(Screen.World(id))
        }
    }
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        TopBar(t("stories.title"), { state.back() }, t("back"))
        LazyVerticalGrid(GridCells.Adaptive(300.dp), contentPadding = PaddingValues(D.gutter), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(g.content.stories.stories, key = { it.id }) { s ->
                val w = g.content.extras.worlds.firstOrNull { it.id == s.worldId }
                val p = progress[s.id]
                Panel(Modifier.pressable { start(s.id, s.worldId) }, padding = PaddingValues(10.dp)) {
                    Column {
                        Box {
                            if (w != null) AssetThumb(w.thumb, null, Modifier.fillMaxWidth().aspectRatio(2.4f).clip(RoundedCornerShape(18.dp)), ContentScale.Crop)
                            Row(Modifier.align(Alignment.BottomStart).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                s.iconTemplates.take(3).forEach { id ->
                                    Box(Modifier.size(54.dp).clip(RoundedCornerShape(14.dp)).background(C.paper.copy(alpha = 0.92f)).padding(3.dp)) { AssetThumb("thumbs/$id.webp", null, Modifier.fillMaxSize()) }
                                }
                            }
                            if (p?.second == true) Box(Modifier.align(Alignment.TopEnd).padding(8.dp).clip(CircleShape).background(C.leaf).padding(6.dp)) { ColoraIcon(Ic.Check, size = 20.dp) }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(cl(s.titleKey), style = T.heading)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Text(t("stories.steps", s.steps.size), style = T.small.copy(color = C.inkSoft), modifier = Modifier.weight(1f))
                            Text(t("stories.reward") + ":", style = T.small.copy(color = C.inkSoft))
                            Spacer(Modifier.width(4.dp))
                            if (s.reward.startsWith("sticker:")) AssetThumb("thumbs/${s.reward.removePrefix("sticker:")}.webp", t("reward.sticker"), Modifier.size(30.dp)) else ColoraIcon(Ic.Book, size = 26.dp, desc = t("reward.book_page"))
                        }
                        if (p != null && !p.second) Text(t("stories.continue"), style = T.small.copy(color = C.focus))
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------- ideas
@Composable
fun IdeasScreen(state: AppState) {
    val g = LocalContext.current.graph
    var seed by remember { mutableLongStateOf(state.profile?.createdAt ?: 7L) }
    val favTopics by produceState(emptyList<String>(), state.profile) {
        value = withContext(Dispatchers.IO) { g.library.list(state.pid, DocKind.ARTWORK).mapNotNull { it.sourceId?.let(g.content::topicOf) }.groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.map { it.key }.take(2) }
    }
    val ideas = remember(seed, favTopics) {
        Suggestions.nextIdeas(g.content.prompts.prompts.map { it.toPrompt() }, seed, { id -> g.content.template(id)?.let { g.entitlements.hasPack(it.packId) } ?: false }, favTopics, g.content::topicOf, 3)
    }
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        TopBar(t("ideas.title"), { state.back() }, t("back")) {
            SoftButton(t("ideas.shuffle"), Ic.Restore) { seed += 1 }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(D.gutter), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Milo("guide", t("home.hello"))
            ideas.forEach { r ->
                val entry = g.content.prompts.prompts.first { it.id == r.prompt.id }
                Panel(Modifier.fillMaxWidth(), padding = PaddingValues(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AssetThumb(entry.illustration, null, Modifier.size(120.dp).clip(RoundedCornerShape(18.dp)))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(cl(r.textKey), style = T.heading)
                            Spacer(Modifier.height(8.dp))
                            BigButton(t("ideas.try"), icon = Ic.Crayon, height = 56.dp) {
                                val tid = r.templateId ?: ContentRepo.FREE_PAGE_ID
                                if (entry.destination == "world") state.go(Screen.WorldPicker()) else state.go(Screen.Studio(tid, null, r.prompt.paletteId))
                            }
                        }
                    }
                }
            }
            SoftButton(t("ideas.free"), Ic.Brush, Modifier.fillMaxWidth()) { state.go(Screen.Studio(ContentRepo.FREE_PAGE_ID)) }
        }
    }
}

// ---------------------------------------------------------------- collection (UI-14)
private data class ArtItem(val s: DocSummary, val templateId: String, val state: PaintState)

@Composable
fun CollectionScreen(state: AppState) {
    val g = LocalContext.current.graph
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf("art") }
    var refresh by remember { mutableIntStateOf(0) }
    var menu by remember { mutableStateOf<ArtItem?>(null) }
    val arts by produceState(emptyList<ArtItem>(), state.profile, refresh) {
        value = withContext(Dispatchers.IO) {
            g.library.list(state.pid, DocKind.ARTWORK).mapNotNull { s -> g.library.loadArtwork(state.pid, s.id)?.second?.let { ArtItem(s, it.templateId, PaintReducer.replay(it.commands)) } }
                .sortedByDescending { it.s.pointer.favorite }
        }
    }
    val chars by produceState(emptyList<DocSummary>(), state.profile, refresh) { value = withContext(Dispatchers.IO) { g.library.list(state.pid, DocKind.CHARACTER) } }
    val scenes by produceState(emptyList<Pair<DocSummary, Bitmap?>>(), state.profile, refresh, tab) {
        if (tab != "scenes") return@produceState
        value = withContext(Dispatchers.IO) { g.library.list(state.pid, DocKind.SCENE).map { s -> s to g.library.loadScene(state.pid, s.id)?.let { SceneSnapshot(g, state.pid, it).bitmap(480) } } }
    }
    val books by produceState(emptyList<Pair<DocSummary, BookDoc?>>(), state.profile, refresh) { value = withContext(Dispatchers.IO) { g.library.list(state.pid, DocKind.BOOK).map { it to g.library.loadBook(state.pid, it.id) } } }

    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(t("collection.title"), { state.back() }, t("back"))
            Row(Modifier.padding(horizontal = D.gutter), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(t("collection.art"), tab == "art", icon = Ic.Crayon) { tab = "art" }
                Chip(t("collection.characters"), tab == "chr", icon = Ic.Character) { tab = "chr" }
                Chip(t("collection.scenes"), tab == "scenes", icon = Ic.World) { tab = "scenes" }
                Chip(t("collection.books"), tab == "books", icon = Ic.Book) { tab = "books" }
            }
            val empty = when (tab) { "art" -> arts.isEmpty(); "chr" -> chars.isEmpty(); "scenes" -> scenes.isEmpty(); else -> false }
            if (empty) { EmptyState(t("collection.empty"), t("collection.make")) { state.go(when (tab) { "chr" -> Screen.Builder(); "scenes" -> Screen.WorldPicker(); else -> Screen.Topics }) }; return@Column }
            LazyVerticalGrid(GridCells.Adaptive(if (tab == "scenes") 260.dp else 150.dp), contentPadding = PaddingValues(D.gutter), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (tab) {
                    "art" -> items(arts, key = { it.s.id }) { a ->
                        Panel(Modifier.pressable { menu = a }, padding = PaddingValues(8.dp)) {
                            Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
                                ArtView(remember(a.templateId) { g.content.art(a.templateId) }, a.state, Modifier.fillMaxSize())
                                if (a.s.pointer.favorite) Box(Modifier.align(Alignment.TopEnd)) { ColoraIcon(Ic.Heart, size = 26.dp) }
                            }
                        }
                    }
                    "chr" -> items(chars, key = { it.id }) { c ->
                        Panel(Modifier.pressable { state.go(Screen.Builder(c.id)) }, padding = PaddingValues(8.dp)) {
                            CharacterThumb(state, ToyRef.character(c.id, c.revisionId).toString(), Modifier.fillMaxWidth().aspectRatio(1f))
                        }
                    }
                    "scenes" -> items(scenes, key = { it.first.id }) { (s, bmp) ->
                        Panel(Modifier.pressable { state.go(Screen.World(s.id)) }, padding = PaddingValues(8.dp)) {
                            Column {
                                BitmapImage(bmp, null, Modifier.fillMaxWidth().aspectRatio(2f).clip(RoundedCornerShape(16.dp)))
                                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SoftButton(t("collection.export"), Ic.Export) { state.gated(Screen.Export("scene:${s.id}")) }
                                    RoundIcon(Ic.Trash, t("collection.trash"), size = 48.dp) {
                                        scope.launch { withContext(Dispatchers.IO) { g.library.trash(state.pid, s.id) }; state.showToast(Strings.get(state.lang, "collection.trashed")); refresh++ }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        item {
                            Panel(Modifier.pressable {
                                scope.launch {
                                    val id = Library.newDocId("b")
                                    withContext(Dispatchers.IO) { g.library.saveBook(state.pid, id, BookDoc()) }
                                    state.go(Screen.Book(id))
                                }
                            }, padding = PaddingValues(8.dp), color = C.butter) {
                                Column(Modifier.fillMaxWidth().aspectRatio(0.8f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    ColoraIcon(Ic.Plus, size = 48.dp); Text(t("collection.new_book"), style = T.label, textAlign = TextAlign.Center)
                                }
                            }
                        }
                        items(books, key = { it.first.id }) { (s, b) ->
                            Panel(Modifier.pressable { state.go(Screen.Book(s.id)) }, padding = PaddingValues(8.dp)) {
                                Column(Modifier.fillMaxWidth().aspectRatio(0.8f).clip(RoundedCornerShape(14.dp)).background(androidx.compose.ui.graphics.Color(b?.coverColor ?: 0xFFFF786D.toInt())).padding(10.dp),
                                    verticalArrangement = Arrangement.SpaceBetween) {
                                    Text(b?.customTitle ?: t(b?.titleKey ?: "book.title.my_book"), style = T.heading)
                                    Text(t("book.pages", b?.pages?.size ?: 0), style = T.small)
                                }
                            }
                        }
                    }
                }
            }
        }
        menu?.let { a ->
            Box(Modifier.fillMaxSize().background(C.ink.copy(alpha = 0.35f)).pressable(sound = false) { menu = null }, contentAlignment = Alignment.Center) {
                Panel(Modifier.padding(24.dp).widthIn(max = 440.dp).pressable(sound = false) {}, shape = RoundedCornerShape(D.panelRadius), padding = PaddingValues(18.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ArtView(remember(a.templateId) { g.content.art(a.templateId) }, a.state, Modifier.fillMaxWidth().height(180.dp))
                        BigButton(t("templates.continue"), Modifier.fillMaxWidth(), icon = Ic.Crayon, height = 60.dp) { menu = null; state.go(Screen.Studio(a.templateId, a.s.id)) }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SoftButton(t("collection.play"), Ic.Play, Modifier.weight(1f)) { menu = null; state.go(Screen.Reveal(a.s.id)) }
                            SoftButton(t("collection.duplicate"), Ic.Copy, Modifier.weight(1f)) {
                                menu = null
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        val d = g.library.loadArtwork(state.pid, a.s.id)?.second ?: return@withContext
                                        g.library.saveArtwork(state.pid, Library.newDocId("a"), d.copy(copiedFrom = a.s.revisionId))
                                    }
                                    refresh++
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SoftButton(t("collection.favorite"), Ic.Heart, Modifier.weight(1f), color = if (a.s.pointer.favorite) C.rose else C.paper) {
                                menu = null; scope.launch { withContext(Dispatchers.IO) { g.library.setFavorite(state.pid, a.s.id, !a.s.pointer.favorite) }; refresh++ }
                            }
                            SoftButton(t("collection.export"), Ic.Export, Modifier.weight(1f)) { menu = null; state.gated(Screen.Export("art:${a.s.id}")) }
                        }
                        SoftButton(t("collection.trash"), Ic.Trash, Modifier.fillMaxWidth()) {
                            menu = null
                            scope.launch { withContext(Dispatchers.IO) { g.library.trash(state.pid, a.s.id) }; state.showToast(Strings.get(state.lang, "collection.trashed")); refresh++ }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------- rest (UI-16)
@Composable
fun RestScreen(state: AppState) {
    val g = LocalContext.current.graph
    val saved by produceState<Boolean?>(null) {
        value = withContext(Dispatchers.IO) { com.colora.world.Autosave.flushAll(); kotlinx.coroutines.delay(600); !com.colora.world.Autosave.hasPending() }
    }
    Column(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(C.lilac, C.bgCream))).safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        AssetThumb("thumbs/milo.resting.webp", null, Modifier.size(180.dp))
        Text(t("rest.title"), style = T.hero, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        if (saved != null) Text(t(if (saved == true) "rest.saved" else "rest.not_saved"), style = T.heading, textAlign = TextAlign.Center)
        Spacer(Modifier.height(30.dp))
        SoftButton(t("rest.extend"), Ic.Lock) { state.gated(Screen.Parent) }
    }
}
