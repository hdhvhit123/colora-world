package com.colora.world

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.colora.world.engine.session.SessionBudget
import com.colora.world.ui.AppState
import com.colora.world.ui.LocalLang
import com.colora.world.ui.Screen
import com.colora.world.ui.components.AssetThumb
import com.colora.world.ui.components.Toast
import com.colora.world.ui.screens.*
import com.colora.world.ui.t
import com.colora.world.ui.theme.C
import com.colora.world.ui.theme.ColoraTheme
import kotlinx.coroutines.delay

/** Holds AppState across configuration changes (orientation keeps tools, camera and undo). */
class AppStateHolder(app: android.app.Application) : androidx.lifecycle.AndroidViewModel(app) {
    val state = AppState((app as ColoraApp).graph)
}

class MainActivity : ComponentActivity() {
    private lateinit var holder: AppStateHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        holder = ViewModelProvider(this)[AppStateHolder::class.java]
        val state = holder.state
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                graph.audio.onForeground(true)
                state.budget?.resume(SystemClock.elapsedRealtime())
            }
            override fun onStop(owner: LifecycleOwner) {
                graph.audio.onForeground(false)
                state.parentEnd()
                state.budget?.pause(SystemClock.elapsedRealtime())
                state.persistBudget(SystemClock.elapsedRealtime())
                Autosave.flushAll()
            }
        })
        setContent { ColoraTheme { Root(state) } }
    }
}

@Composable
fun Root(state: AppState) {
    val settings by state.graph.settings.values.collectAsState()
    LaunchedEffect(Unit) {
        if (state.loading) state.start()
        state.ensureBudget(); state.budget?.resume(SystemClock.elapsedRealtime())
        state.graph.audio.preload(listOf("ui.tap", "ui.fill_pop", "ui.select_color", "ui.undo", "ui.redo", "ui.sticker_pop", "ui.save_chime", "ui.open_panel"))
    }
    // foreground play-time budget (monotonic clock, paused in the parent area)
    LaunchedEffect(settings.timeLimitMinutes) {
        state.ensureBudget(); state.budget?.resume(SystemClock.elapsedRealtime())
        while (true) {
            delay(1000)
            val b = state.budget ?: continue
            val now = SystemClock.elapsedRealtime()
            val inParent = state.current is Screen.Parent || state.current is Screen.ParentGate || state.current is Screen.Export
            if (inParent) b.pause(now) else b.resume(now)
            when (b.poll(now)) {
                SessionBudget.Signal.WARN -> state.budgetWarn = true
                SessionBudget.Signal.END -> if (state.current !is Screen.Rest && !inParent) { Autosave.flushAll(); state.go(Screen.Rest) }
                SessionBudget.Signal.NONE -> Unit
            }
            if (now / 1000 % 10 == 0L) state.persistBudget(now)
        }
    }
    LaunchedEffect(state.current) {
        val track = when (state.current) {
            is Screen.World, is Screen.WorldPicker, is Screen.Stories -> "music.play_parade"
            is Screen.Studio, is Screen.Lab, is Screen.Builder -> "music.dream_sky"
            is Screen.Parent, is Screen.ParentGate, is Screen.Export, is Screen.Rest -> null
            else -> "music.home_meadow"
        }
        state.graph.audio.setMusic(track)
    }
    CompositionLocalProvider(LocalLang provides settings.lang) {
        Box(Modifier.fillMaxSize().background(C.bgCream)) {
            if (state.loading) {
                AssetThumb("thumbs/milo.greet.webp", null, Modifier.size(140.dp).align(Alignment.Center))
                return@Box
            }
            BackHandler(enabled = state.stack.size > 1) { state.back() }
            AnimatedContent(
                targetState = state.current,
                transitionSpec = { (fadeIn(tween(if (settings.reducedMotion) 0 else 220)) togetherWith fadeOut(tween(if (settings.reducedMotion) 0 else 160))) },
                label = "screens",
            ) { screen ->
                Box(Modifier.fillMaxSize()) { ScreenFor(state, screen) }
            }
            if (state.budgetWarn) Toast(t("budget.warn")) { state.budgetWarn = false }
            Toast(state.toast) { state.toast = null }
        }
    }
}

@Composable
private fun ScreenFor(state: AppState, s: Screen) {
    when (s) {
        Screen.FirstRun -> FirstRunScreen(state)
        Screen.Home -> HomeScreen(state)
        Screen.Topics -> TopicsScreen(state)
        is Screen.Templates -> TemplatesScreen(state, s.topicId)
        is Screen.Studio -> StudioScreen(state, s)
        is Screen.Reveal -> RevealScreen(state, s.docId)
        is Screen.WorldPicker -> WorldPickerScreen(state, s)
        is Screen.World -> WorldScreen(state, s)
        Screen.Stories -> StoriesScreen(state)
        Screen.Ideas -> IdeasScreen(state)
        Screen.Collection -> CollectionScreen(state)
        is Screen.Book -> BookScreen(state, s.bookId)
        Screen.Lab -> LabScreen(state)
        is Screen.Builder -> BuilderScreen(state, s.docId)
        is Screen.ParentGate -> ParentGateScreen(state, s.then)
        Screen.Parent -> ParentScreen(state)
        is Screen.Export -> ExportScreen(state, s.ref)
        Screen.Rest -> RestScreen(state)
    }
}
