package com.colora.world.engine.character

import kotlinx.serialization.Serializable

enum class Socket { BODY, EARS, TAIL, WINGS, HAT, DECOR }

@Serializable
data class PartSpec(
    val id: String,
    val socket: Socket,
    /** Allowed scale range so parts never leave the frame. */
    val minScale: Float = 0.8f,
    val maxScale: Float = 1.2f,
    val zOrder: Int,
    val regions: Int,
)

@Serializable
data class BodySpec(val id: String, val sockets: Map<Socket, List<Float>>, val preset: String)

@Serializable
data class PartChoice(val partId: String, val scale: Float = 1f, val fills: Map<String, Int> = emptyMap())

@Serializable
data class CharacterRecipe(
    val schemaVersion: Int = 1,
    val id: String,
    val version: Int = 1,
    val bodyId: String,
    val bodyFills: Map<String, Int> = emptyMap(),
    val parts: Map<Socket, PartChoice> = emptyMap(),
    val preset: String,
)

class RecipeException(msg: String) : IllegalArgumentException(msg)

object CharacterRules {
    /** Validates socket/part compatibility and clamps scales. Returns the normalised recipe. */
    fun normalise(r: CharacterRecipe, bodies: Map<String, BodySpec>, parts: Map<String, PartSpec>): CharacterRecipe {
        val body = bodies[r.bodyId] ?: throw RecipeException("unknown body ${r.bodyId}")
        val fixed = r.parts.mapValues { (socket, choice) ->
            val spec = parts[choice.partId] ?: throw RecipeException("unknown part ${choice.partId}")
            if (spec.socket != socket) throw RecipeException("${spec.id} does not fit $socket")
            if (socket !in body.sockets) throw RecipeException("body ${body.id} has no $socket socket")
            choice.copy(scale = choice.scale.coerceIn(spec.minScale, spec.maxScale))
        }
        return r.copy(parts = fixed)
    }
}
