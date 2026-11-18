package com.colora.world.engine.paint

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Brushes are interpreted only by renderers; business logic treats them as ids. */
enum class BrushId(val minSkill: Int) { FILL(0), CRAYON(0), MARKER(1), WATERCOLOR(1), RAINBOW(1), PATTERN(1), ERASER(0) }

enum class PatternId { DOTS, STRIPES, STARS, HEARTS, CHECKS }

/**
 * Stroke samples in template units: x, y, pressure, timeDelta(ms) per point.
 * Stored as a flat array so serialisation stays compact and replay exact.
 */
@Serializable
data class Stroke(
    val brush: BrushId,
    val brushVersion: Int = 1,
    val sizeUnits: Float,
    val color: Int,
    val opacity: Float = 1f,
    val seed: Long,
    val clipRegionId: String? = null,
    val pattern: PatternId? = null,
    val points: List<Float>,
) {
    init {
        require(points.size % 4 == 0) { "points must be x,y,pressure,dt quadruples" }
        require(sizeUnits in 1f..400f) { "brush size out of range" }
        require(opacity in 0f..1f)
    }
    val pointCount get() = points.size / 4
}

@Serializable
data class StickerPlacement(
    val instanceId: String,
    val stickerId: String,
    val x: Float,
    val y: Float,
    val scale: Float = 1f,
    val rotationDeg: Float = 0f,
    val partId: String? = null,
)

@Serializable
sealed interface PaintCommand {
    @Serializable @SerialName("fill")
    data class FillRegion(val regionId: String, val color: Int) : PaintCommand
    @Serializable @SerialName("stroke")
    data class AddStroke(val stroke: Stroke) : PaintCommand
    @Serializable @SerialName("sticker_add")
    data class AddSticker(val sticker: StickerPlacement) : PaintCommand
    @Serializable @SerialName("sticker_move")
    data class TransformSticker(val instanceId: String, val x: Float, val y: Float, val scale: Float, val rotationDeg: Float) : PaintCommand
    @Serializable @SerialName("sticker_remove")
    data class RemoveSticker(val instanceId: String) : PaintCommand
    @Serializable @SerialName("clear_region")
    data class ClearRegion(val regionId: String) : PaintCommand
    @Serializable @SerialName("clear_page")
    data object ClearPage : PaintCommand
    @Serializable @SerialName("paper")
    data class SetPaperStyle(val style: String) : PaintCommand
}

/** Fully resolved paint state after replaying commands in order. */
data class PaintState(
    val fills: Map<String, Int> = emptyMap(),
    /** Strokes in paint order. Eraser strokes are kept so renderers replay exact layering. */
    val strokes: List<Stroke> = emptyList(),
    val stickers: Map<String, StickerPlacement> = linkedMapOf(),
    val paper: String = "plain",
    /** Index into [strokes] before which strokes are hidden by a ClearRegion/ClearPage of their region. */
    val clearedStrokeIds: Set<Int> = emptySet(),
) {
    val visibleStrokes: List<Stroke> get() = strokes.filterIndexed { i, _ -> i !in clearedStrokeIds }
    val isBlank: Boolean get() = fills.isEmpty() && visibleStrokes.isEmpty() && stickers.isEmpty()
}

object PaintReducer {
    fun apply(state: PaintState, cmd: PaintCommand): PaintState = when (cmd) {
        // Fills are the base colour under the paint layer; the eraser removes brush
        // strokes only (renderers apply it with DST_OUT inside the stroke layer), so a
        // new fill never resurrects strokes the child erased (ADR-016).
        is PaintCommand.FillRegion -> state.copy(fills = state.fills + (cmd.regionId to cmd.color))
        is PaintCommand.AddStroke -> state.copy(strokes = state.strokes + cmd.stroke)
        is PaintCommand.AddSticker -> state.copy(stickers = LinkedHashMap(state.stickers).apply { put(cmd.sticker.instanceId, cmd.sticker) })
        is PaintCommand.TransformSticker -> {
            val s = state.stickers[cmd.instanceId]
            if (s == null) state else state.copy(stickers = LinkedHashMap(state.stickers).apply {
                put(cmd.instanceId, s.copy(x = cmd.x, y = cmd.y, scale = cmd.scale.coerceIn(0.3f, 3f), rotationDeg = cmd.rotationDeg))
            })
        }
        is PaintCommand.RemoveSticker -> state.copy(stickers = LinkedHashMap(state.stickers).apply { remove(cmd.instanceId) })
        is PaintCommand.ClearRegion -> {
            val hidden = state.clearedStrokeIds.toMutableSet()
            state.strokes.forEachIndexed { i, s -> if (s.clipRegionId == cmd.regionId) hidden += i }
            state.copy(fills = state.fills - cmd.regionId, clearedStrokeIds = hidden)
        }
        PaintCommand.ClearPage -> state.copy(fills = emptyMap(), clearedStrokeIds = state.strokes.indices.toSet(), stickers = linkedMapOf())
        is PaintCommand.SetPaperStyle -> state.copy(paper = cmd.style)
    }

    fun replay(commands: List<PaintCommand>): PaintState = commands.fold(PaintState()) { s, c -> apply(s, c) }
}
