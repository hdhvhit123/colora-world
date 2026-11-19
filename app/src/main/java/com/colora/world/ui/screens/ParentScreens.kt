package com.colora.world.ui.screens

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.colora.world.Autosave
import com.colora.world.data.Settings
import com.colora.world.export.Exporter
import com.colora.world.graph
import com.colora.world.storage.ChildProfile
import com.colora.world.storage.DocKind
import com.colora.world.storage.TrashRecord
import com.colora.world.ui.AppState
import com.colora.world.ui.Screen
import com.colora.world.ui.Strings
import com.colora.world.ui.components.*
import com.colora.world.ui.t
import com.colora.world.ui.theme.C
import com.colora.world.ui.theme.D
import com.colora.world.ui.theme.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

// ------------------------------------------------------------------ parent gate (UI-16)
/** Spells 10–99 in words so the child cannot simply copy digits. */
fun numberWords(n: Int, lang: String): String {
    if (lang == "vi") {
        val d = listOf("không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín")
        val tens = n / 10; val ones = n % 10
        val head = if (tens == 1) "mười" else d[tens] + " mươi"
        val tail = when { ones == 0 -> ""; ones == 1 && tens > 1 -> " mốt"; ones == 5 -> " lăm"; ones == 4 && tens > 1 -> " tư"; else -> " " + d[ones] }
        return head + tail
    }
    val ones = listOf("", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen")
    val tens = listOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")
    return if (n < 20) ones[n] else tens[n / 10] + if (n % 10 != 0) "-" + ones[n % 10] else ""
}

@Composable
fun ParentGateScreen(state: AppState, then: Screen) {
    val g = LocalContext.current.graph
    var held by remember { mutableFloatStateOf(0f) }
    var step by remember { mutableIntStateOf(0) } // 0 hold, 1 challenge or PIN
    var target by remember { mutableIntStateOf(Random.nextInt(21, 99)) }
    var input by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }
    var tries by remember { mutableIntStateOf(3) }
    val usePin = g.settings.hasPin
    fun pass() { state.parentTouch(); state.replace(then) }

    Column(Modifier.fillMaxSize().background(C.skyPale).safeDrawingPadding().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        TopBar(t("gate.title"), { state.back() }, t("back"))
        Spacer(Modifier.height(24.dp))
        if (step == 0) {
            Text(t("gate.hold"), style = T.title, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(24.dp))
            Box(Modifier.size(160.dp).semantics { contentDescription = "hold" }.clip(CircleShape).background(C.paper).border(4.dp, C.ink, CircleShape)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        val start = System.currentTimeMillis()
                        // hold for 2 s; releasing early resets
                        val job = g.appScope.launch { while (true) { held = ((System.currentTimeMillis() - start) / 2000f).coerceAtMost(1f); if (held >= 1f) { step = 1; break }; delay(16) } }
                        waitForUpOrCancellation(); job.cancel(); if (step == 0) held = 0f
                    }
                }, contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxSize(held).clip(CircleShape).background(C.turquoise))
                ColoraIcon(Ic.Lock, size = 56.dp)
            }
        } else {
            Panel(Modifier.padding(16.dp).widthIn(max = 460.dp), shape = RoundedCornerShape(D.panelRadius)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (usePin) t("gate.pin") else t("gate.question"), style = T.heading, textAlign = TextAlign.Center)
                    if (!usePin) Text(numberWords(target, state.lang), style = T.title.copy(color = C.coral), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    Text(if (usePin) "•".repeat(input.length).ifEmpty { " " } else input.ifEmpty { " " }, style = T.hero)
                    msg?.let { Text(it, style = T.label.copy(color = C.coral), textAlign = TextAlign.Center) }
                    Spacer(Modifier.height(10.dp))
                    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "OK")
                    keys.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { k ->
                                Box(Modifier.padding(4.dp).size(72.dp, 60.dp).semantics { contentDescription = k }.pressable {
                                    when (k) {
                                        "⌫" -> input = input.dropLast(1)
                                        "OK" -> if (usePin) {
                                            when (val r = g.settings.checkPin(input)) {
                                                Settings.PinResult.Ok -> pass()
                                                is Settings.PinResult.Wrong -> { msg = Strings.get(state.lang, "gate.wrong").replace("{0}", "${r.left}"); input = "" }
                                                is Settings.PinResult.Locked -> { msg = Strings.get(state.lang, "gate.locked"); input = "" }
                                            }
                                        } else if (input.toIntOrNull() == target) pass() else {
                                            tries--; input = ""
                                            if (tries <= 0) state.back() else { msg = Strings.get(state.lang, "gate.wrong").replace("{0}", "$tries"); target = Random.nextInt(21, 99) }
                                        }
                                        else -> if (input.length < 8) input += k
                                    }
                                }.clip(RoundedCornerShape(18.dp)).background(if (k == "OK") C.leaf else C.sand), contentAlignment = Alignment.Center) { Text(k, style = T.title) }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(t("gate.note"), style = T.small.copy(color = C.inkSoft), textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
    }
}

// ----------------------------------------------------------------- parent area (UI-17)
@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Panel(Modifier.fillMaxWidth(), shape = RoundedCornerShape(D.cardRadius)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = T.heading); content() }
    }
}

@Composable
private fun SwitchRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = T.parent, modifier = Modifier.weight(1f))
        Switch(value, onChange, colors = SwitchDefaults.colors(checkedTrackColor = C.turquoise, checkedThumbColor = C.paper, uncheckedTrackColor = C.sand, uncheckedThumbColor = C.inkSoft, uncheckedBorderColor = C.inkSoft))
    }
}

@Composable
private fun SliderRow(label: String, value: Float, onChange: (Float) -> Unit) {
    Column {
        Text(label, style = T.parent)
        Slider(value, onChange, colors = SliderDefaults.colors(thumbColor = C.coral, activeTrackColor = C.coral))
    }
}

@Composable
fun ParentScreen(state: AppState) {
    val ctx = LocalContext.current
    val g = ctx.graph
    val scope = rememberCoroutineScope()
    val v by g.settings.values.collectAsState()
    // a handful of tiny JSON files: read directly so the list is there on the first frame
    var profiles by remember { mutableStateOf(g.library.profiles()) }
    var trash by remember { mutableStateOf<List<TrashRecord>>(emptyList()) }
    var stats by remember { mutableStateOf(listOf(0, 0, 0)) }
    var storage by remember { mutableLongStateOf(0L) }
    var confirm by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }
    var pinInput by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf<String?>(null) }
    fun touch() = state.parentTouch()
    LaunchedEffect(Unit) { while (true) { delay(1000); if (!state.parentActive) { state.replace(Screen.ParentGate(Screen.Parent)); break } } }
    LaunchedEffect(refresh, state.profile) {
        val pid = state.pid
        val r = withContext(Dispatchers.IO) {
            listOf(g.library.profiles(), g.library.trashList(pid).sortedByDescending { it.deletedAt },
                listOf(DocKind.ARTWORK, DocKind.SCENE, DocKind.BOOK).map { g.library.list(pid, it).size }, g.library.storageBytes())
        }
        @Suppress("UNCHECKED_CAST") run { profiles = r[0] as List<ChildProfile>; trash = r[1] as List<TrashRecord>; stats = r[2] as List<Int>; storage = r[3] as Long }
    }
    val backupOut = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        busy = Strings.get(state.lang, "export.working")
        scope.launch {
            val r = withContext(Dispatchers.IO) {
                runCatching {
                    Autosave.flush("")
                    val tmp = File(ctx.cacheDir, "backup.tmp"); tmp.outputStream().use { g.library.backup.export(profiles.map { it.id }, it) }
                    g.library.backup.inspect(tmp) // verify what we wrote before handing it over
                    ctx.contentResolver.openOutputStream(uri, "w")!!.use { o -> tmp.inputStream().use { it.copyTo(o) } }
                    val n = tmp.length(); tmp.delete(); n
                }
            }
            busy = null
            state.showToast(r.fold({ Strings.get(state.lang, "parent.backup.ok").replace("{0}", fmtBytes(it)) }, { Strings.get(state.lang, "parent.backup.fail").replace("{0}", it.message ?: "?") }))
        }
    }
    val backupIn = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        busy = Strings.get(state.lang, "export.working")
        scope.launch {
            val r = withContext(Dispatchers.IO) {
                runCatching {
                    val tmp = File(ctx.cacheDir, "import.tmp")
                    ctx.contentResolver.openInputStream(uri)!!.use { i -> tmp.outputStream().use { i.copyTo(it) } }
                    try { g.library.backup.importAsNewProfiles(tmp).profileIdMap.size } finally { tmp.delete() }
                }
            }
            busy = null; refresh++
            state.showToast(r.fold({ Strings.get(state.lang, "parent.backup.imported").replace("{0}", "$it") }, { Strings.get(state.lang, "parent.backup.fail").replace("{0}", it.message ?: "?") }))
        }
    }

    Box(Modifier.fillMaxSize().background(C.bgCream)) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            TopBar(t("parent.title"), { state.parentEnd(); state.back() }, t("parent.done"))
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(D.gutter).widthIn(max = 720.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // profiles
                Section(t("parent.profiles")) {
                    profiles.forEachIndexed { idx, p ->
                        val cur = p.id == state.profile?.id
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (cur) C.butter else C.sand).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            val thumb = g.content.characters.bodies.firstOrNull { it.id == p.avatarId }?.thumb
                            Box(Modifier.size(56.dp).clip(CircleShape).background(C.paper)) { if (thumb != null) AssetThumb(thumb, null, Modifier.fillMaxSize().padding(4.dp)) }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text((p.localAlias ?: "${idx + 1}") + " · " + t("skill.${p.skillMode}"), style = T.label)
                                // avatar choice
                                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val pickDesc = t("avatar.pick")
                                    g.content.characters.bodies.forEach { b ->
                                        Box(Modifier.size(40.dp).semantics { contentDescription = pickDesc }.pressable { touch(); scope.launch { withContext(Dispatchers.IO) { g.library.updateProfile(p.copy(avatarId = b.id)) }; if (cur) state.profile = p.copy(avatarId = b.id); refresh++ } }
                                            .clip(CircleShape).background(if (p.avatarId == b.id) C.focus.copy(alpha = 0.3f) else C.paper)) { AssetThumb(b.thumb, null, Modifier.fillMaxSize().padding(3.dp)) }
                                    }
                                }
                            }
                            if (!cur) RoundIcon(Ic.Check, t("parent.switch"), size = 48.dp) { touch(); state.switchProfile(p); state.parentTouch(); refresh++ }
                            if (profiles.size > 1) RoundIcon(Ic.Trash, t("parent.delete_profile"), size = 48.dp) {
                                touch()
                                confirm = Strings.get(state.lang, "parent.delete_confirm") to {
                                    scope.launch {
                                        withContext(Dispatchers.IO) { g.library.deleteProfile(p.id) }
                                        if (cur) profiles.firstOrNull { it.id != p.id }?.let { state.switchProfile(it); state.parentTouch() }
                                        refresh++
                                    }
                                }
                            }
                        }
                    }
                    if (profiles.size < 5) SoftButton(t("parent.add_profile"), Ic.Plus) {
                        touch()
                        scope.launch { withContext(Dispatchers.IO) { g.library.createProfile(g.content.characters.bodies.random().id, v.lang) }; refresh++ }
                    }
                }
                Section(t("parent.skill")) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("little", "explorer", "master").forEach { s ->
                            Chip(t("skill.$s"), state.profile?.skillMode == s) {
                                touch(); val p = state.profile ?: return@Chip
                                val n = p.copy(skillMode = s); state.profile = n
                                scope.launch(Dispatchers.IO) { g.library.updateProfile(n) }
                            }
                        }
                    }
                }
                Section(t("parent.language")) {
                    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Chip(t("parent.language.device"), v.langPref == com.colora.world.ui.Lang.SYSTEM) { touch(); g.settings.update { it.copy(langPref = com.colora.world.ui.Lang.SYSTEM) } }
                        com.colora.world.ui.Lang.available().forEach { (code, name) ->
                            Chip(name, v.langPref == code) { touch(); g.settings.update { it.copy(langPref = code) } }
                        }
                    }
                }
                Section(t("parent.sound")) {
                    SliderRow(t("parent.music"), v.musicVolume) { touch(); g.settings.update { s -> s.copy(musicVolume = it) } }
                    SliderRow(t("parent.effects"), v.sfxVolume) { touch(); g.settings.update { s -> s.copy(sfxVolume = it) } }
                    SwitchRow(t("parent.mute"), v.muted) { touch(); g.settings.update { s -> s.copy(muted = it) } }
                    SwitchRow(t("parent.motion"), v.reducedMotion) { touch(); g.settings.update { s -> s.copy(reducedMotion = it) } }
                    SwitchRow(t("parent.haptics"), v.haptics) { touch(); g.settings.update { s -> s.copy(haptics = it) } }
                    SwitchRow(t("parent.turns"), v.turnReminder) { touch(); g.settings.update { s -> s.copy(turnReminder = it) } }
                }
                Section(t("parent.time")) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Chip(t("parent.time.none"), v.timeLimitMinutes == null) { touch(); g.settings.update { it.copy(timeLimitMinutes = null) }; state.ensureBudget() }
                        listOf(10, 15, 20, 30, 45, 60).forEach { m ->
                            Chip(t("parent.time.min", m), v.timeLimitMinutes == m) { touch(); g.settings.update { it.copy(timeLimitMinutes = m) }; g.settings.saveBudget(null); state.ensureBudget() }
                        }
                    }
                    Text(t("parent.time_note"), style = T.small.copy(color = C.inkSoft))
                }
                Section(t("parent.stats")) {
                    Text(t("parent.stats.art", stats[0]) + " · " + t("parent.stats.scenes", stats[1]) + " · " + t("parent.stats.books", stats[2]), style = T.parent)
                    Text(t("parent.stats.storage", fmtBytes(storage)), style = T.parent)
                }
                Section(t("parent.backup")) {
                    Text(t("parent.backup_note"), style = T.small.copy(color = C.inkSoft))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SoftButton(t("parent.backup.export"), Ic.Export, Modifier.weight(1f)) { touch(); backupOut.launch("colora-backup-${System.currentTimeMillis() / 1000}.colora.zip") }
                        SoftButton(t("parent.backup.import"), Ic.Restore, Modifier.weight(1f)) { touch(); backupIn.launch(arrayOf("application/zip", "application/octet-stream")) }
                    }
                }
                Section(t("parent.trash")) {
                    if (trash.isEmpty()) Text(t("parent.trash.empty"), style = T.parent)
                    trash.forEach { r ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${r.kind.name.lowercase()} · ${java.text.DateFormat.getDateInstance().format(java.util.Date(r.deletedAt))}", style = T.parent, modifier = Modifier.weight(1f))
                            SoftButton(t("parent.restore"), Ic.Restore) { touch(); scope.launch { withContext(Dispatchers.IO) { g.library.restore(state.pid, r.documentId) }; refresh++ } }
                            RoundIcon(Ic.Trash, t("parent.delete_forever"), size = 48.dp) {
                                touch(); confirm = Strings.get(state.lang, "parent.delete_forever") to { scope.launch { withContext(Dispatchers.IO) { g.library.deleteForever(state.pid, r.documentId) }; refresh++ } }
                            }
                        }
                    }
                }
                Section(t("parent.pin")) {
                    Text(t("parent.pin.hint"), style = T.small.copy(color = C.inkSoft))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(pinInput, { s -> touch(); pinInput = s.filter { it.isDigit() }.take(8) }, Modifier.weight(1f), singleLine = true,
                            visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword))
                        SoftButton(t("parent.pin.set"), Ic.Lock) { touch(); if (pinInput.length in 4..8) { g.settings.setPin(pinInput); pinInput = ""; state.showToast(Strings.get(state.lang, "parent.pin.saved")) } }
                    }
                    if (g.settings.hasPin) SoftButton(t("parent.pin.clear"), Ic.Close) { touch(); g.settings.clearPin(); refresh++ }
                }
                Section(t("parent.content")) { ContentSection(state) }
                Section(t("parent.offscreen")) { Text(t("parent.offscreen.text"), style = T.parent) }
                Section(t("parent.privacy")) { Text(t("parent.privacy.text"), style = T.parent) }
                Section(t("parent.check")) {
                    SoftButton(t("parent.check"), Ic.Check) {
                        touch(); scope.launch {
                            val bad = withContext(Dispatchers.IO) { g.content.verifyIndex() }
                            state.showToast(if (bad.isEmpty()) Strings.get(state.lang, "parent.check_ok") else Strings.get(state.lang, "parent.check_bad").replace("{0}", "${bad.size}"))
                        }
                    }
                }
                SoftButton(t("parent.delete_all"), Ic.Trash, color = C.rose.copy(alpha = 0.4f)) {
                    touch(); confirm = Strings.get(state.lang, "parent.delete_all_confirm") to {
                        scope.launch {
                            withContext(Dispatchers.IO) { Autosave.flushAll(); g.library.profiles().forEach { g.library.deleteProfile(it.id) }; g.settings.clearPin(); g.settings.saveBudget(null) }
                            g.settings.update { it.copy(onboarded = false, currentProfile = null) }
                            state.parentEnd(); state.start()
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
        busy?.let { Box(Modifier.fillMaxSize().background(C.ink.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) { Panel { Text(it, style = T.heading) } } }
        confirm?.let { (msg, act) -> ConfirmDialog(msg, onYes = { confirm = null; act() }, onNo = { confirm = null }) }
    }
}

@Composable
private fun ContentSection(state: AppState) {
    val ctx = LocalContext.current
    val g = ctx.graph
    val ent = g.entitlements
    if (ent.allUnlocked) { Text(t("parent.content.offline"), style = T.parent); return }
    val owned by ent.owned.collectAsState()
    val products by ent.products.collectAsState()
    val bstate by ent.state.collectAsState()
    LaunchedEffect(Unit) { ent.connect() }
    if (products.isEmpty()) Text(t("parent.content.unavailable") + " (${bstate})", style = T.parent)
    products.forEach { p ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(p.title, style = T.label); Text(p.description, style = T.small) }
            if (p.productId in owned) Text(t("parent.content.owned"), style = T.label.copy(color = C.leaf))
            else SoftButton(p.formattedPrice, Ic.Store) { state.parentTouch(); (ctx as? Activity)?.let { ent.purchase(it, p.productId) } }
        }
    }
    SoftButton(t("parent.content.restore"), Ic.Restore) { state.parentTouch(); ent.restore() }
}

fun fmtBytes(n: Long): String = when { n >= 1 shl 30 -> "%.1f GB".format(n / (1 shl 30).toFloat()); n >= 1 shl 20 -> "%.1f MB".format(n / (1 shl 20).toFloat()); else -> "${n / 1024} KB" }

// ------------------------------------------------------------------------ export (UI-18)
@Composable
fun ExportScreen(state: AppState, ref: String) {
    val ctx = LocalContext.current
    val g = ctx.graph
    val scope = rememberCoroutineScope()
    val exporter = remember(state.profile, state.lang) { Exporter(g, state.pid, state.lang) }
    val formats = remember(ref) { exporter.formatsFor(ref) }
    var transparent by remember { mutableStateOf(!g.settings.v.exportPaper) }
    var small by remember { mutableStateOf(false) }
    var landscape by remember { mutableStateOf(ref.startsWith("scene")) }
    var pending by remember { mutableStateOf<Exporter.Format?>(null) }
    var progress by remember { mutableFloatStateOf(-1f) }
    var done by remember { mutableStateOf<File?>(null) }
    var job by remember { mutableStateOf<Job?>(null) }
    val cancel = remember { AtomicBoolean() }
    val canVideo = remember(landscape) { Exporter.canEncode(landscape) }
    val preview by produceState<Bitmap?>(null, ref) {
        value = withContext(Dispatchers.IO) { runCatching { val f = File(ctx.cacheDir, "exports/preview.png").apply { parentFile?.mkdirs() }; if (ref.startsWith("book")) exporter.pdf(ref, false, File(ctx.cacheDir, "exports/preview.pdf")).let { null } else { exporter.png(ref, false, Exporter.Options(widthPx = 720), f); android.graphics.BitmapFactory.decodeFile(f.path) } }.getOrNull() }
    }
    LaunchedEffect(Unit) { while (true) { delay(1000); if (job == null) { if (!state.parentActive) { state.back(); break } } else state.parentTouch() } }

    fun run(uri: Uri) {
        val f = pending ?: return
        cancel.set(false); progress = 0f; done = null
        job = scope.launch {
            val name = exporter.fileName(ref, f)
            val r = withContext(Dispatchers.Default) {
                runCatching {
                    val dir = File(ctx.cacheDir, "exports").apply { mkdirs() }
                    dir.listFiles()?.filter { it.name != "preview.png" }?.forEach { it.delete() }
                    val out = File(dir, name)
                    val o = Exporter.Options(transparent = transparent, widthPx = if (small) 1024 else 2048, landscape = landscape, low = g.settings.v.videoLow)
                    when (f) {
                        Exporter.Format.PNG -> exporter.png(ref, false, o, out, cancel)
                        Exporter.Format.OUTLINE -> exporter.png(ref, true, o, out, cancel)
                        Exporter.Format.PDF_A4 -> exporter.pdf(ref, false, out, cancel) { progress = it }
                        Exporter.Format.PDF_LETTER -> exporter.pdf(ref, true, out, cancel) { progress = it }
                        Exporter.Format.MP4 -> exporter.mp4(ref, o, out, cancel) { progress = it }
                    }
                    ctx.contentResolver.openOutputStream(uri, "w")!!.use { os -> out.inputStream().use { it.copyTo(os) } }
                    out
                }
            }
            job = null; progress = -1f
            r.onSuccess { done = it; state.showToast(Strings.get(state.lang, "export.ok").replace("{0}", it.name)) }
            r.onFailure { e ->
                runCatching { android.provider.DocumentsContract.deleteDocument(ctx.contentResolver, uri) }
                if (e !is Exporter.Cancelled) state.showToast(Strings.get(state.lang, "export.fail").replace("{0}", e.message ?: e.javaClass.simpleName))
            }
            pending = null
        }
    }
    val pngL = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { it?.let(::run) ?: run { pending = null } }
    val pdfL = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { it?.let(::run) ?: run { pending = null } }
    val mp4L = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("video/mp4")) { it?.let(::run) ?: run { pending = null } }
    fun start(f: Exporter.Format) {
        state.parentTouch(); pending = f
        val name = exporter.fileName(ref, f)
        when (f.mime) { "image/png" -> pngL.launch(name); "application/pdf" -> pdfL.launch(name); else -> mp4L.launch(name) }
    }

    Box(Modifier.fillMaxSize().background(C.bgCream)) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            TopBar(t("export.title"), { if (job == null) state.back() }, t("back"))
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(D.gutter).widthIn(max = 720.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.fillMaxWidth().heightIn(max = 300.dp).aspectRatio(if (ref.startsWith("scene")) 2f else 1.3f).clip(RoundedCornerShape(D.cardRadius))
                    .background(if (transparent) C.sand else C.paper).border(2.dp, C.line, RoundedCornerShape(D.cardRadius)), contentAlignment = Alignment.Center) {
                    if (ref.startsWith("book")) ColoraIcon(Ic.Book, size = 96.dp) else BitmapImage(preview, t("export.preview"), Modifier.fillMaxSize().padding(8.dp))
                }
                if (Exporter.Format.PNG in formats) Section(t("export.png")) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Chip(t("export.paper"), !transparent) { transparent = false; g.settings.update { it.copy(exportPaper = true) } }
                        Chip(t("export.transparent"), transparent) { transparent = true; g.settings.update { it.copy(exportPaper = false) } }
                        Chip("2048 px", !small) { small = false }
                        Chip("1024 px", small) { small = true }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BigButton(t("export.png"), Modifier.weight(1f), icon = Ic.Export, height = 60.dp, enabled = job == null) { start(Exporter.Format.PNG) }
                        if (Exporter.Format.OUTLINE in formats) BigButton(t("export.outline"), Modifier.weight(1f), icon = Ic.Brush, color = C.butter, height = 60.dp, enabled = job == null) { start(Exporter.Format.OUTLINE) }
                    }
                }
                Section("PDF") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BigButton(t("export.pdf_a4"), Modifier.weight(1f), icon = Ic.Book, color = C.turquoise, height = 60.dp, enabled = job == null) { start(Exporter.Format.PDF_A4) }
                        BigButton(t("export.pdf_letter"), Modifier.weight(1f), icon = Ic.Book, color = C.sky, height = 60.dp, enabled = job == null) { start(Exporter.Format.PDF_LETTER) }
                    }
                }
                if (Exporter.Format.MP4 in formats) Section(t("export.mp4")) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Chip(t("export.portrait"), !landscape) { landscape = false }
                        Chip(t("export.landscape"), landscape) { landscape = true }
                    }
                    if (!canVideo) Text(t("export.no_codec"), style = T.parent.copy(color = C.coral))
                    BigButton(t("export.mp4"), Modifier.fillMaxWidth(), icon = Ic.Play, color = C.lavender, height = 60.dp, enabled = job == null && canVideo) { start(Exporter.Format.MP4) }
                }
                done?.let { f ->
                    SoftButton(t("export.share"), Ic.Export, Modifier.fillMaxWidth(), color = C.mint) {
                        state.parentTouch()
                        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".files", f)
                        val mime = when (f.extension) { "png" -> "image/png"; "pdf" -> "application/pdf"; else -> "video/mp4" }
                        ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType(mime).putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), null))
                    }
                }
            }
        }
        if (progress >= 0f) {
            Box(Modifier.fillMaxSize().background(C.ink.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                Panel(Modifier.widthIn(max = 360.dp).fillMaxWidth(0.85f), shape = RoundedCornerShape(D.panelRadius)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Milo("happy", null, size = 72.dp)
                        Text(t("export.working"), style = T.heading)
                        Box(Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp)).background(C.sand)) {
                            Box(Modifier.fillMaxHeight().fillMaxWidth(progress.coerceIn(0.02f, 1f)).clip(RoundedCornerShape(8.dp)).background(C.turquoise))
                        }
                        SoftButton(t("export.cancel"), Ic.Close) { cancel.set(true) }
                    }
                }
            }
        }
    }
}
