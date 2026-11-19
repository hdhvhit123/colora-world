package com.colora.world

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap

/**
 * Serial, debounced persistence queue (spec §5.4, §17.1). Each document key has
 * at most one pending write; writes run in order on one background thread, so an
 * autosave and an explicit save can never interleave. The latest snapshot always
 * wins because the action reads the editor state at run time.
 */
object Autosave {
    private val thread = HandlerThread("colora-save").apply { start() }
    private val handler = Handler(thread.looper)
    internal val looper get() = thread.looper
    private val pending = ConcurrentHashMap<String, Runnable>()

    /** [action] returns true when the revision was durably committed. */
    fun schedule(key: String, delayMs: Long = 500, action: () -> Boolean) {
        pending.remove(key)?.let { handler.removeCallbacks(it) }
        val r = Runnable { pending.remove(key); run(key, action) }
        pending[key] = r
        handler.postDelayed(r, delayMs)
    }

    private fun run(key: String, action: () -> Boolean): Boolean = try { action() } catch (e: Exception) { Log.e("Colora", "save failed for $key", e); false }

    /** Runs the pending write for [key] now (if any) and waits for it. */
    suspend fun flush(key: String, timeoutMs: Long = 10_000): Boolean {
        val r = pending.remove(key)
        val done = CompletableDeferred<Unit>()
        if (r != null) handler.removeCallbacks(r)
        handler.post { r?.run(); done.complete(Unit) }
        return withTimeoutOrNull(timeoutMs) { done.await(); true } ?: false
    }

    /** Moves every pending write to the front of the queue (pause, profile switch, time's up). */
    fun flushAll() {
        val all = pending.keys.toList()
        for (k in all) pending.remove(k)?.let { handler.removeCallbacks(it); handler.postAtFrontOfQueue(it) }
    }

    fun hasPending(): Boolean = pending.isNotEmpty()

    /** Runs [block] on the save thread after everything queued so far. */
    fun enqueue(block: () -> Unit) { handler.post(block) }
}
