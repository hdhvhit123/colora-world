package com.colora.world.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.colora.world.graph

val LocalLang = staticCompositionLocalOf { "en" }

/** UI text. Content text (templates, stories, prompts…) lives in assets/content/locales. */
@Composable
fun t(key: String): String = Strings.get(LocalLang.current, key)

@Composable
fun t(key: String, vararg args: Any): String = Strings.get(LocalLang.current, key).let { s -> var r = s; args.forEachIndexed { i, a -> r = r.replace("{$i}", a.toString()) }; r }

/** Content label from the content pack. */
@Composable
fun cl(key: String): String = LocalContext.current.graph.content.label(key, LocalLang.current)

/** Languages the app ships, with their own names (shown in the parent area). */
object Lang {
    const val SYSTEM = "system"
    val SUPPORTED: List<Pair<String, String>> = listOf(
        "en" to "English", "vi" to "Tiếng Việt", "es" to "Español", "fr" to "Français", "de" to "Deutsch",
        "pt" to "Português (Brasil)", "it" to "Italiano", "id" to "Bahasa Indonesia",
        "ja" to "日本語", "ko" to "한국어", "zh" to "简体中文", "th" to "ไทย", "hi" to "हिन्दी",
    )
    private val codes = SUPPORTED.map { it.first }.toSet()
    /** Shipped languages only (a language is listed once its UI and content files exist). */
    fun available(): List<Pair<String, String>> = SUPPORTED.filter { Strings.has(it.first) }

    /** "system" follows the phone (or Android 13+ per-app) language, falling back to English. */
    fun resolve(pref: String?, device: java.util.Locale = java.util.Locale.getDefault()): String {
        if (pref != null && pref != SYSTEM && Strings.has(pref)) return pref
        val l = device.language.let { if (it == "in") "id" else it } // legacy Indonesian code
        return if (l in codes && Strings.has(l)) l else "en"
    }
}

/**
 * UI text, loaded from assets/ui/<lang>.json (English is the fallback for any
 * missing key). Content text (templates, stories, prompts…) lives in assets/content/locales.
 */
object Strings {
    private var assets: android.content.res.AssetManager? = null
    private val cache = java.util.concurrent.ConcurrentHashMap<String, Map<String, String>>()
    private var present: Set<String> = setOf("en")

    fun init(ctx: android.content.Context) {
        assets = ctx.assets
        present = (ctx.assets.list("ui") ?: emptyArray()).filter { it.endsWith(".json") }.map { it.removeSuffix(".json") }.toSet()
    }

    fun has(lang: String) = lang in present

    private fun table(lang: String): Map<String, String> = cache.getOrPut(lang) {
        val am = assets ?: return@getOrPut emptyMap()
        runCatching {
            val obj = kotlinx.serialization.json.Json.parseToJsonElement(am.open("ui/$lang.json").bufferedReader().use { it.readText() }) as kotlinx.serialization.json.JsonObject
            obj.mapValues { (it.value as kotlinx.serialization.json.JsonPrimitive).content }
        }.getOrElse { emptyMap() }
    }

    fun get(lang: String, key: String): String = table(lang)[key] ?: table("en")[key] ?: key
}
