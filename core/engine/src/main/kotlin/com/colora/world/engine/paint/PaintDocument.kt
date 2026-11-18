package com.colora.world.engine.paint

/**
 * Command-log document with bounded undo/redo.
 *
 * The committed log is the source of truth for saving; undo only moves a
 * cursor, so saving never depends on undo history being available. Checkpoints
 * of resolved state every [checkpointEvery] commands keep replay cheap.
 */
class PaintDocument(
    initial: List<PaintCommand> = emptyList(),
    private val undoLimit: Int = 100,
    private val checkpointEvery: Int = 32,
) {
    private val log = ArrayList<PaintCommand>(initial)
    /** Number of commands currently applied (log[0 until cursor]). */
    var cursor: Int = log.size; private set
    /** The oldest index undo may rewind to. */
    private var undoFloor: Int = log.size
    private val checkpoints = HashMap<Int, PaintState>()
    var version: Long = 0; private set

    val commands: List<PaintCommand> get() = log.subList(0, cursor).toList()
    val canUndo get() = cursor > undoFloor
    val canRedo get() = cursor < log.size

    fun push(cmd: PaintCommand) {
        while (log.size > cursor) log.removeAt(log.size - 1)
        checkpoints.keys.removeAll { it > cursor }
        log += cmd
        cursor = log.size
        undoFloor = maxOf(undoFloor, cursor - undoLimit)
        version++
    }

    fun undo(): Boolean {
        if (!canUndo) return false
        cursor--; version++; return true
    }

    fun redo(): Boolean {
        if (!canRedo) return false
        cursor++; version++; return true
    }

    fun state(): PaintState {
        val cp = (cursor / checkpointEvery) * checkpointEvery
        var base = if (cp == 0) PaintState() else checkpoints[cp]
        var from = cp
        if (base == null) {
            // build missing checkpoints progressively
            base = PaintState(); from = 0
            var s: PaintState = base
            for (i in 0 until cp) {
                s = PaintReducer.apply(s, log[i])
                if ((i + 1) % checkpointEvery == 0) checkpoints[i + 1] = s
            }
            base = s; from = cp
        }
        var s: PaintState = base
        for (i in from until cursor) s = PaintReducer.apply(s, log[i])
        return s
    }
}
