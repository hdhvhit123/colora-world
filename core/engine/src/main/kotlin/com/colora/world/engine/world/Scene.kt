package com.colora.world.engine.world

import kotlinx.serialization.Serializable

/** Capability tags; logic never switches on template names. */
object Caps {
    const val RIDEABLE = "rideable"
    const val PASSENGER = "passenger"
    const val PLANTABLE = "plantable"
    const val WATERABLE = "waterable"
    const val SEATABLE = "seatable"
    const val TABLE_ITEM = "table_item"
    const val FLYING = "flying"
    const val AQUATIC = "aquatic"
    const val CARRYABLE = "carryable"
    const val CARRIER = "carrier"
    const val SURFACE = "surface"
    const val SEAT_FURNITURE = "seat_furniture"
    const val SKY = "sky"
    const val WATER_BODY = "water_body"
}

/** Which child capability may attach to which parent anchor type. */
object AttachRules {
    private val rules: Map<String, Set<String>> = mapOf(
        "passenger" to setOf(Caps.PASSENGER),
        "seat" to setOf(Caps.PASSENGER, Caps.SEATABLE),
        "carry" to setOf(Caps.CARRYABLE, Caps.TABLE_ITEM),
        "table_item" to setOf(Caps.TABLE_ITEM, Caps.CARRYABLE),
        "plant" to setOf(Caps.PLANTABLE),
        "water" to setOf(Caps.AQUATIC),
    )

    fun accepts(anchorType: String, childCaps: Collection<String>): Boolean =
        rules[anchorType]?.any { it in childCaps } == true
}

@Serializable
data class SceneTransform(
    /** Normalised to logical scene bounds (0..1). */
    val x: Float,
    val y: Float,
    val scale: Float = 1f,
    val rotationDeg: Float = 0f,
    val flipX: Boolean = false,
) {
    fun clamped() = copy(x = x.coerceIn(0.02f, 0.98f), y = y.coerceIn(0.05f, 0.98f), scale = scale.coerceIn(SCALE_MIN, SCALE_MAX))
    companion object { const val SCALE_MIN = 0.6f; const val SCALE_MAX = 1.6f }
}

@Serializable
data class Attachment(val parentId: String, val anchorId: String)

@Serializable
data class ToyInstance(
    val id: String,
    /** Immutable artwork revision (or prop id prefixed `prop:`). */
    val artworkRevisionId: String,
    val templateId: String,
    val transform: SceneTransform,
    val zOrder: Int,
    val attachment: Attachment? = null,
    val state: String? = null,
)

@Serializable
data class SceneEnvironment(val timeOfDay: String = "day", val weather: String = "clear")

@Serializable
data class SceneDocument(
    val schemaVersion: Int = 1,
    val id: String,
    val profileId: String,
    val worldId: String,
    val worldVersion: Int = 1,
    val revision: Long = 0,
    val environment: SceneEnvironment = SceneEnvironment(),
    val objects: List<ToyInstance> = emptyList(),
    val storyId: String? = null,
) {
    fun obj(id: String) = objects.firstOrNull { it.id == id }
    companion object { const val MAX_OBJECTS = 40; const val SOFT_OBJECTS = 30 }
}
