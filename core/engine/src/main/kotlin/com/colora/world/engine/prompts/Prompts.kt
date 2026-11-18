package com.colora.world.engine.prompts

import kotlinx.serialization.Serializable

@Serializable
data class CreativePrompt(
    val id: String,
    val textKey: String,
    /** Templates that make a good start; first accessible one is opened by "Try". */
    val suggestedTemplates: List<String> = emptyList(),
    /** Alternate wording + templates when the preferred ones are not accessible. */
    val altTextKey: String? = null,
    val altTemplates: List<String> = emptyList(),
    val paletteId: String? = null,
    val destination: String = "studio",
)

/**
 * Local, deterministic suggestion order: unfinished work first, then favourite
 * topics, then topics not yet explored. No deadlines, no penalties for time away.
 */
object Suggestions {
    fun shuffled(prompts: List<CreativePrompt>, seed: Long): List<CreativePrompt> {
        val list = prompts.toMutableList()
        var s = seed xor 0x2545F4914F6CDD1DL
        for (i in list.size - 1 downTo 1) {
            s = s * 6364136223846793005L + 1442695040888963407L
            val j = ((s ushr 33) % (i + 1)).toInt()
            val t = list[i]; list[i] = list[j]; list[j] = t
        }
        return list
    }

    data class Resolved(val prompt: CreativePrompt, val textKey: String, val templateId: String?)

    /** Resolves to accessible content or swaps to the alternate; never returns a locked item. */
    fun resolve(p: CreativePrompt, accessible: (String) -> Boolean): Resolved? {
        if (p.suggestedTemplates.isEmpty()) return Resolved(p, p.textKey, null)
        p.suggestedTemplates.firstOrNull(accessible)?.let { return Resolved(p, p.textKey, it) }
        val alt = p.altTemplates.firstOrNull(accessible) ?: return null
        return Resolved(p, p.altTextKey ?: p.textKey, alt)
    }

    fun nextIdeas(
        all: List<CreativePrompt>, seed: Long, accessible: (String) -> Boolean,
        favouriteTopics: List<String>, topicOf: (String) -> String?, count: Int = 3,
    ): List<Resolved> {
        val resolved = shuffled(all, seed).mapNotNull { resolve(it, accessible) }
        val (fav, rest) = resolved.partition { r -> r.templateId?.let(topicOf) in favouriteTopics }
        return (fav + rest).take(count)
    }
}
