package com.colora.world.data

import android.content.Context
import com.colora.world.engine.content.ContentJson
import com.colora.world.engine.session.BudgetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale

/**
 * Parent/device settings. Kept in SharedPreferences, outside the document store,
 * so they are never exported in a backup (ADR-007) and never auto-backed up.
 */
class Settings(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("colora.settings", Context.MODE_PRIVATE)

    data class Values(
        val lang: String,
        val musicVolume: Float,
        val sfxVolume: Float,
        val muted: Boolean,
        val reducedMotion: Boolean,
        val haptics: Boolean,
        val timeLimitMinutes: Int?,
        val currentProfile: String?,
        val onboarded: Boolean,
        val turnReminder: Boolean,
        val exportPaper: Boolean,
        val videoLow: Boolean,
    )

    private val _values = MutableStateFlow(read())
    val values: StateFlow<Values> = _values
    val v: Values get() = _values.value

    private fun read() = Values(
        lang = prefs.getString("lang", null) ?: if (Locale.getDefault().language == "vi") "vi" else "en",
        musicVolume = prefs.getFloat("music", 0.45f),
        sfxVolume = prefs.getFloat("sfx", 0.8f),
        muted = prefs.getBoolean("muted", false),
        reducedMotion = prefs.getBoolean("reducedMotion", false),
        haptics = prefs.getBoolean("haptics", true),
        timeLimitMinutes = prefs.getInt("limit", 0).takeIf { it > 0 },
        currentProfile = prefs.getString("profile", null),
        onboarded = prefs.getBoolean("onboarded", false),
        turnReminder = prefs.getBoolean("turnReminder", false),
        exportPaper = prefs.getBoolean("exportPaper", true),
        videoLow = prefs.getBoolean("videoLow", false),
    )

    fun update(f: (Values) -> Values) {
        val n = f(_values.value)
        prefs.edit()
            .putString("lang", n.lang).putFloat("music", n.musicVolume).putFloat("sfx", n.sfxVolume)
            .putBoolean("muted", n.muted).putBoolean("reducedMotion", n.reducedMotion).putBoolean("haptics", n.haptics)
            .putInt("limit", n.timeLimitMinutes ?: 0).putString("profile", n.currentProfile).putBoolean("onboarded", n.onboarded)
            .putBoolean("turnReminder", n.turnReminder).putBoolean("exportPaper", n.exportPaper).putBoolean("videoLow", n.videoLow)
            .commit()
        _values.value = n
    }

    // -------------------------------------------------------------- time budget
    fun budget(): BudgetState? = prefs.getString("budget", null)?.let { runCatching { ContentJson.decodeFromString(BudgetState.serializer(), it) }.getOrNull() }
    fun saveBudget(b: BudgetState?) { prefs.edit().apply { if (b == null) remove("budget") else putString("budget", ContentJson.encodeToString(BudgetState.serializer(), b)) }.apply() }

    // -------------------------------------------------------------------- PIN
    // Salted, iterated SHA-256; never stored in plaintext. Not a legal parental-verification mechanism.
    val hasPin: Boolean get() = prefs.contains("pinHash")

    fun setPin(pin: String) {
        require(pin.length in 4..8 && pin.all(Char::isDigit))
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.edit().putString("pinSalt", salt.toHex()).putString("pinHash", hash(pin, salt)).putInt("pinFails", 0).putLong("pinLockUntil", 0).commit()
    }

    fun clearPin() { prefs.edit().remove("pinSalt").remove("pinHash").remove("pinFails").remove("pinLockUntil").commit() }

    sealed interface PinResult { data object Ok : PinResult; data class Wrong(val left: Int) : PinResult; data class Locked(val untilMs: Long) : PinResult }

    fun checkPin(pin: String, now: Long = System.currentTimeMillis()): PinResult {
        val lock = prefs.getLong("pinLockUntil", 0)
        if (now < lock) return PinResult.Locked(lock)
        val salt = prefs.getString("pinSalt", null)?.fromHex() ?: return PinResult.Ok
        if (hash(pin, salt) == prefs.getString("pinHash", null)) { prefs.edit().putInt("pinFails", 0).apply(); return PinResult.Ok }
        val fails = prefs.getInt("pinFails", 0) + 1
        return if (fails >= 5) {
            val until = now + 60_000L * (1 shl ((fails - 5).coerceAtMost(4)))
            prefs.edit().putInt("pinFails", fails).putLong("pinLockUntil", until).apply(); PinResult.Locked(until)
        } else { prefs.edit().putInt("pinFails", fails).apply(); PinResult.Wrong(5 - fails) }
    }

    private fun hash(pin: String, salt: ByteArray): String {
        var h = salt + pin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        repeat(20_000) { h = md.digest(h + salt) }
        return h.toHex()
    }

    // ---------------------------------------------------- store entitlements
    fun cachedEntitlements(): Set<String> = prefs.getStringSet("ent", emptySet()) ?: emptySet()
    fun cacheEntitlements(s: Set<String>) { prefs.edit().putStringSet("ent", s).apply() }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
    private fun String.fromHex() = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
