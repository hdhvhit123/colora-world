package com.colora.world.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.colora.world.AppGraph
import com.colora.world.Autosave
import com.colora.world.data.ArtworkDoc
import com.colora.world.data.Library
import com.colora.world.engine.paint.BrushId
import com.colora.world.engine.paint.Brushes
import com.colora.world.engine.paint.PaintCommand
import com.colora.world.engine.paint.PaintDocument
import com.colora.world.engine.paint.PaintState
import com.colora.world.engine.paint.PatternId
import com.colora.world.engine.paint.StickerPlacement
import com.colora.world.engine.paint.Stroke
import com.colora.world.render.ArtModel
import com.colora.world.render.ArtRenderer
import kotlin.random.Random

enum class Tool { FILL, CRAYON, MARKER, WATER, RAINBOW, PATTERN, STICKER, ERASER, CLEAR_REGION }

fun Tool.brush(): BrushId? = when (this) {
    Tool.CRAYON -> BrushId.CRAYON; Tool.MARKER -> BrushId.MARKER; Tool.WATER -> BrushId.WATERCOLOR
    Tool.RAINBOW -> BrushId.RAINBOW; Tool.PATTERN -> BrushId.PATTERN; Tool.ERASER -> BrushId.ERASER; else -> null
}

enum class SaveStatus { CLEAN, PENDING, SAVED, ERROR }

/**
 * Editor state for one artwork. Holds the command log (source of truth), the
 * live stroke, the viewport and a cached raster of the paint layer. Outlines
 * are drawn as vectors every frame so they stay crisp at any zoom.
 */
class StudioController(val graph: AppGraph, val pid: String, val art: ArtModel, docId: String?, initial: List<PaintCommand>) {
    var docId: String = docId ?: Library.newDocId("a"); private set
    private var committedOnce = docId != null
    val doc = PaintDocument(initial, undoLimit = 60)
    var version by mutableIntStateOf(0); private set
    var tool by mutableStateOf(Tool.FILL)
    var color by mutableIntStateOf(0xFFF25C54.toInt())
    var sizeStep by mutableIntStateOf(1) // 0 small, 1 medium, 2 large
    var pattern by mutableStateOf(PatternId.DOTS)
    var stickerId by mutableStateOf("sticker.sparkle.star")
    var selectedSticker by mutableStateOf<String?>(null)
    var saveStatus by mutableStateOf(if (committedOnce) SaveStatus.SAVED else SaveStatus.CLEAN)
    var lastRevision: String? = null; private set
    var flash by mutableStateOf<String?>(null) // region just filled (for the spread effect)

    var state: PaintState = doc.state(); private set

    fun sizeUnits(b: BrushId): Float = Brushes.defaultSize(b) * floatArrayOf(0.6f, 1f, 1.7f)[sizeStep]

    // ---------------------------------------------------------- commands
    fun push(cmd: PaintCommand) {
        synchronized(this) { doc.push(cmd) }; refresh(); scheduleSave()
    }

    fun undo() { if (synchronized(this) { doc.undo() }) { refresh(); scheduleSave(); graph.audio.sfx("ui.undo") } }
    fun redo() { if (synchronized(this) { doc.redo() }) { refresh(); scheduleSave(); graph.audio.sfx("ui.redo") } }

    private fun refresh() { state = doc.state(); version++ }

    fun fillAt(x: Float, y: Float, tolerance: Float) {
        val r = art.geometry.hitTest(x, y, tolerance) ?: return
        if (tool == Tool.CLEAR_REGION) { push(PaintCommand.ClearRegion(r)); tool = Tool.FILL; return }
        if (state.fills[r] == color) return
        push(PaintCommand.FillRegion(r, color)); flash = r
        graph.audio.sfx("ui.fill_pop")
    }

    // -------------------------------------------------------- live stroke
    var live: Stroke? = null; private set
    private val livePts = ArrayList<Float>(512)
    var liveCount by mutableIntStateOf(0); private set
    private var lastT = 0L

    fun begin(x: Float, y: Float, t: Long, tolerance: Float): Boolean {
        val b = tool.brush() ?: return false
        val region = art.geometry.hitTest(x, y, tolerance) ?: return false
        livePts.clear(); livePts.add(x); livePts.add(y); livePts.add(1f); livePts.add(0f)
        lastT = t
        live = Stroke(b, 1, sizeUnits(b), color, 1f, Random.nextLong(), region, if (b == BrushId.PATTERN) pattern else null, emptyList())
        liveCount = 1
        if (b != BrushId.ERASER) graph.audio.sfx("ui.brush_soft", 600) else graph.audio.sfx("ui.eraser", 600)
        return true
    }

    fun extend(x: Float, y: Float, t: Long, minSpacing: Float) {
        if (live == null) return
        val n = livePts.size
        val dx = x - livePts[n - 4]; val dy = y - livePts[n - 3]
        if (dx * dx + dy * dy < minSpacing * minSpacing) return
        if (livePts.size >= 4 * 4000) return // hard cap per stroke
        livePts.add(x); livePts.add(y); livePts.add(1f); livePts.add((t - lastT).toFloat().coerceIn(0f, 10_000f))
        lastT = t
        liveCount = livePts.size / 4
    }

    fun liveStroke(): Stroke? = live?.copy(points = livePts.toList())

    fun end() {
        val s = live ?: return
        live = null; liveCount = 0
        push(PaintCommand.AddStroke(s.copy(points = livePts.toList())))
    }

    /** System cancel / second finger: drop the uncommitted stroke (nothing half-saved). */
    fun cancel() { live = null; liveCount = 0; version++ }

    // ------------------------------------------------------------ stickers
    fun placeSticker(x: Float, y: Float) {
        val region = art.geometry.hitTest(x, y, 30f)
        val part = art.region(region)?.partId ?: art.parts.last().def.id
        val id = "s" + Library.uid().take(10)
        push(PaintCommand.AddSticker(StickerPlacement(id, stickerId, x, y, 1f, 0f, part)))
        selectedSticker = id
        graph.audio.sfx("ui.sticker_pop")
    }

    fun stickerAt(x: Float, y: Float): String? = state.stickers.values.reversed().firstOrNull { s ->
        val r = ArtRenderer.STICKER_UNITS * s.scale / 2f
        (x - s.x) * (x - s.x) + (y - s.y) * (y - s.y) <= r * r
    }?.instanceId

    fun transformSticker(id: String, x: Float, y: Float, scale: Float, rot: Float) {
        // collapse a drag into one undo step: replace a trailing move of the same sticker
        val last = doc.commands.lastOrNull()
        if (last is PaintCommand.TransformSticker && last.instanceId == id && doc.canUndo) synchronized(this) { doc.undo() }
        push(PaintCommand.TransformSticker(id, x, y, scale, rot))
    }

    fun removeSticker(id: String) { push(PaintCommand.RemoveSticker(id)); selectedSticker = null }

    // ---------------------------------------------------------- persistence
    private fun snapshot() = ArtworkDoc(templateId = art.def.id, templateVersion = art.def.contentVersion, commands = doc.commands)

    fun scheduleSave() {
        saveStatus = SaveStatus.PENDING
        val key = "art:$pid:$docId"
        Autosave.schedule(key, 500) { saveNow() }
    }

    /** Runs on the save thread. Status reflects the real result (never "saved" on failure). */
    private fun saveNow(): Boolean {
        val snap = synchronized(this) { snapshot() }
        return try {
            val meta = graph.library.saveArtwork(pid, docId, snap)
            lastRevision = meta.revisionId; committedOnce = true
            saveStatus = SaveStatus.SAVED; true
        } catch (e: Exception) { saveStatus = SaveStatus.ERROR; false }
    }

    /** Flushes pending changes. Returns the current revision id, or null if nothing is saved. */
    suspend fun flush(): String? {
        if (saveStatus == SaveStatus.CLEAN) return null
        if (saveStatus == SaveStatus.ERROR) { scheduleSave() }
        Autosave.flush("art:$pid:$docId")
        if (lastRevision == null && committedOnce) lastRevision = graph.library.store.pointer(pid, docId)?.currentRevisionId
        return if (saveStatus == SaveStatus.SAVED) lastRevision ?: graph.library.store.pointer(pid, docId)?.currentRevisionId else null
    }

    val hasContent: Boolean get() = !state.isBlank || committedOnce

    // ------------------------------------------------------------ paint cache
    private var cache: Bitmap? = null
    private var cacheVersion = -1
    val cacheMatrix = Matrix()

    /** Raster of fills+strokes+stickers for [m] at w×h. Re-rendered only when content or viewport changes. */
    fun paintLayer(w: Int, h: Int, m: Matrix, gestureActive: Boolean): Bitmap {
        var b = cache
        val sameView = cacheMatrix == m
        if (b != null && b.width == w && b.height == h && cacheVersion == version && (sameView || gestureActive)) return b
        if (b == null || b.width != w || b.height != h) { b?.recycle(); b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); cache = b }
        b.eraseColor(0)
        val c = Canvas(b)
        c.concat(m)
        ArtRenderer.draw(c, art, state, ArtRenderer.Options(drawOutlines = false, stickerArt = graph.content::art))
        cacheVersion = version; cacheMatrix.set(m)
        return b
    }
}
