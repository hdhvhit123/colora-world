package com.colora.world.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.colora.world.AppGraph
import com.colora.world.engine.session.BudgetState
import com.colora.world.engine.session.SessionBudget
import com.colora.world.storage.ChildProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

sealed interface Screen {
    data object FirstRun : Screen
    data object Home : Screen
    data object Topics : Screen
    data class Templates(val topicId: String) : Screen
    /** docId == null → new artwork from [templateId]. */
    data class Studio(val templateId: String, val docId: String? = null, val paletteId: String? = null) : Screen
    data class Reveal(val docId: String) : Screen
    data class WorldPicker(val addRef: String? = null, val addTemplate: String? = null) : Screen
    data class World(val sceneId: String, val addRef: String? = null, val addTemplate: String? = null) : Screen
    data object Stories : Screen
    data object Ideas : Screen
    data object Collection : Screen
    data class Book(val bookId: String) : Screen
    data object Lab : Screen
    data class Builder(val docId: String? = null) : Screen
    data class ParentGate(val then: Screen) : Screen
    data object Parent : Screen
    data class Export(val ref: String) : Screen
    data object Rest : Screen
}

/** Navigation stack + current profile + parent session. Survives configuration changes via the Activity's retained holder. */
@Stable
class AppState(val graph: AppGraph) {
    val stack = mutableStateListOf<Screen>()
    val current: Screen get() = stack.lastOrNull() ?: Screen.Home
    var profile by mutableStateOf<ChildProfile?>(null)
    var toast by mutableStateOf<String?>(null)
    var loading by mutableStateOf(true)
    var budgetWarn by mutableStateOf(false)

    /** Parent session: never restored after process death, expires after 2 min idle or background. */
    private var parentUntil = 0L
    val parentActive: Boolean get() = System.currentTimeMillis() < parentUntil
    fun parentTouch() { parentUntil = System.currentTimeMillis() + 120_000L }
    fun parentEnd() { parentUntil = 0L }

    val pid: String get() = profile?.id ?: error("no profile")
    val lang: String get() = graph.settings.v.lang

    fun go(s: Screen) {
        if (stack.lastOrNull() == s) return // double-tap guard
        stack.add(s)
    }
    fun replace(s: Screen) { if (stack.isNotEmpty()) stack.removeAt(stack.size - 1); stack.add(s) }
    fun back(): Boolean { if (stack.size <= 1) return false; stack.removeAt(stack.size - 1); return true }
    fun home() { stack.clear(); stack.add(Screen.Home) }

    /** Opens a sensitive destination behind the parent gate (or directly while a parent session is active). */
    fun gated(target: Screen) { if (parentActive) { parentTouch(); go(target) } else go(Screen.ParentGate(target)) }

    suspend fun start() {
        val (profiles, _) = withContext(Dispatchers.IO) { graph.library.profiles() to Unit }
        val s = graph.settings
        var p = profiles.firstOrNull { it.id == s.v.currentProfile } ?: profiles.firstOrNull()
        if (p == null) {
            p = withContext(Dispatchers.IO) { graph.library.createProfile("char.body.bean", s.v.lang) }
        }
        s.update { it.copy(currentProfile = p.id) }
        profile = p
        stack.clear()
        stack.add(if (s.v.onboarded) Screen.Home else Screen.FirstRun)
        loading = false
    }

    fun switchProfile(p: ChildProfile) {
        profile = p
        graph.settings.update { it.copy(currentProfile = p.id) }
        parentEnd()
    }

    // ------------------------------------------------------------ time budget
    var budget: SessionBudget? = null; private set

    fun ensureBudget() {
        val limit = graph.settings.v.timeLimitMinutes
        if (limit == null) { budget = null; graph.settings.saveBudget(null); return }
        val saved = graph.settings.budget()
        val st = if (saved != null && saved.limitMinutes == limit) saved else BudgetState(UUID.randomUUID().toString(), limit)
        budget = SessionBudget(st)
    }

    fun persistBudget(nowMono: Long) { budget?.let { graph.settings.saveBudget(it.checkpoint(nowMono)) } }

    fun showToast(msg: String) { toast = msg }

    fun launchIO(block: suspend () -> Unit) = graph.appScope.launch { block() }
}
