package com.colora.world.engine.session

import kotlinx.serialization.Serializable

/**
 * Foreground play-time budget measured with a monotonic clock. Only active
 * (foreground, not parent-area) time counts; restarts restore consumed time
 * from the persisted checkpoint so relaunching never resets the budget.
 * This is a product option, not an OS-level parental control.
 */
@Serializable
data class BudgetState(
    val sessionId: String,
    val limitMinutes: Int?,
    val consumedMs: Long = 0,
    val extendedMs: Long = 0,
    val warned: Boolean = false,
)

class SessionBudget(private var state: BudgetState) {
    private var runningSince: Long? = null

    val snapshot: BudgetState get() = state

    fun resume(nowMono: Long) { if (runningSince == null) runningSince = nowMono }

    fun pause(nowMono: Long) {
        val start = runningSince ?: return
        state = state.copy(consumedMs = state.consumedMs + (nowMono - start).coerceAtLeast(0))
        runningSince = null
    }

    fun consumed(nowMono: Long): Long = state.consumedMs + (runningSince?.let { (nowMono - it).coerceAtLeast(0) } ?: 0)

    fun remainingMs(nowMono: Long): Long? {
        val limit = state.limitMinutes ?: return null
        return (limit * 60_000L + state.extendedMs - consumed(nowMono)).coerceAtLeast(0)
    }

    enum class Signal { NONE, WARN, END }

    /** WARN once ~1 minute before the end; END when exhausted. */
    fun poll(nowMono: Long): Signal {
        val rem = remainingMs(nowMono) ?: return Signal.NONE
        if (rem == 0L) return Signal.END
        if (rem <= 60_000L && !state.warned) { state = state.copy(warned = true); return Signal.WARN }
        return Signal.NONE
    }

    /** Parent-approved extension (after the gate). */
    fun extend(minutes: Int) { state = state.copy(extendedMs = state.extendedMs + minutes * 60_000L, warned = false) }

    fun checkpoint(nowMono: Long): BudgetState = state.copy(consumedMs = consumed(nowMono))

    companion object { val LIMIT_OPTIONS = listOf<Int?>(null, 5, 10, 15, 20, 30) }
}
