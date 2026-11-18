package com.colora.world.engine.story

import kotlinx.serialization.Serializable

enum class StoryEvent { PLACE, ATTACH, INTERACT, MOOD, MOVE }

/** What can satisfy a step. A thing matches if its template id is listed or it has [capability]. */
@Serializable
data class Matcher(
    val templates: List<String> = emptyList(),
    /** Free alternatives (base-tier templates or props) that always resolve the step. */
    val fallbacks: List<String> = emptyList(),
    val capability: String? = null,
) {
    /** An empty matcher accepts anything (e.g. "save your scene"). */
    val isAny: Boolean get() = templates.isEmpty() && fallbacks.isEmpty() && capability == null

    fun matches(templateId: String, caps: Set<String>): Boolean =
        isAny || templateId in templates || templateId in fallbacks || (capability != null && capability in caps)
}

@Serializable
data class StoryStep(
    val id: String,
    val textKey: String,
    val event: StoryEvent,
    val subject: Matcher,
    /** For ATTACH: what the subject attaches to. */
    val target: Matcher? = null,
    /** For INTERACT: action id (tap, water, open, blow, wave...). For MOOD: mood id or "any". */
    val action: String? = null,
    val count: Int = 1,
)

@Serializable
data class StoryDef(
    val id: String,
    val titleKey: String,
    val worldId: String,
    val steps: List<StoryStep>,
    /** Visible-before-start reward, e.g. "book_page" or "sticker:star". No gacha. */
    val reward: String,
    val iconTemplates: List<String>,
)

/** Something that happened in the scene, reported by the world runtime. */
data class SceneEvent(
    val type: StoryEvent,
    val objectId: String,
    val templateId: String,
    val caps: Set<String>,
    val targetTemplateId: String? = null,
    val targetCaps: Set<String> = emptySet(),
    val action: String? = null,
    /** Distinguishes repeated events of one object, e.g. which waypoint was reached. */
    val detail: String? = null,
)

@Serializable
data class StoryProgress(
    val storyId: String,
    val attemptId: String,
    val sceneId: String,
    val stepIndex: Int = 0,
    /** step id -> distinct object ids that counted (idempotent). */
    val matched: Map<String, List<String>> = emptyMap(),
    val skipped: List<String> = emptyList(),
    val completed: Boolean = false,
)

object StoryRuntime {
    fun currentStep(def: StoryDef, p: StoryProgress): StoryStep? = def.steps.getOrNull(p.stepIndex)

    /** Applies an event; duplicate events for the same object never double-count. */
    fun onEvent(def: StoryDef, p: StoryProgress, e: SceneEvent): StoryProgress {
        if (p.completed) return p
        val step = currentStep(def, p) ?: return p.copy(completed = true)
        if (!matches(step, e)) return p
        val key = when (step.event) {
            StoryEvent.MOOD, StoryEvent.MOVE -> "${e.objectId}:${e.action}:${e.detail ?: ""}"
            StoryEvent.INTERACT -> "${e.objectId}:${e.action}"
            else -> e.objectId
        }
        val got = p.matched[step.id].orEmpty()
        if (key in got) return p
        val now = got + key
        var next = p.copy(matched = p.matched + (step.id to now))
        if (now.size >= step.count) next = advance(def, next)
        return next
    }

    fun skip(def: StoryDef, p: StoryProgress): StoryProgress {
        val step = currentStep(def, p) ?: return p
        return advance(def, p.copy(skipped = p.skipped + step.id))
    }

    private fun advance(def: StoryDef, p: StoryProgress): StoryProgress {
        val idx = p.stepIndex + 1
        return p.copy(stepIndex = idx, completed = idx >= def.steps.size)
    }

    fun matches(step: StoryStep, e: SceneEvent): Boolean {
        if (e.type != step.event) return false
        if (step.event == StoryEvent.MOOD) return step.action == null || step.action == "any" || step.action == e.action
        if (!step.subject.matches(e.templateId, e.caps)) return false
        if (step.event == StoryEvent.ATTACH) {
            val t = step.target ?: return true
            return e.targetTemplateId != null && t.matches(e.targetTemplateId, e.targetCaps)
        }
        if (step.event == StoryEvent.INTERACT && step.action != null) return step.action == e.action
        return true
    }
}

/**
 * Validates that a story can be finished using free content only. [isFree]
 * reports base-tier templates and props; [capsOf] gives capabilities of free things.
 */
object FreePathResolver {
    data class Problem(val storyId: String, val stepId: String, val reason: String)

    fun check(def: StoryDef, freeThings: Collection<String>, capsOf: (String) -> Set<String>): List<Problem> {
        val out = ArrayList<Problem>()
        for (s in def.steps) {
            if (s.event == StoryEvent.MOOD) continue
            if (!resolvable(s.subject, freeThings, capsOf)) out += Problem(def.id, s.id, "subject has no free option")
            if (s.event == StoryEvent.ATTACH && s.target != null && !resolvable(s.target, freeThings, capsOf))
                out += Problem(def.id, s.id, "target has no free option")
        }
        return out
    }

    private fun resolvable(m: Matcher, free: Collection<String>, capsOf: (String) -> Set<String>): Boolean =
        m.isAny || free.any { it in m.templates || it in m.fallbacks || (m.capability != null && m.capability in capsOf(it)) }
}
