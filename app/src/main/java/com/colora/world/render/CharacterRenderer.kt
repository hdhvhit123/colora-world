package com.colora.world.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.colora.world.content.ContentRepo
import com.colora.world.engine.character.CharacterRecipe
import com.colora.world.engine.character.PartChoice
import com.colora.world.engine.character.Socket
import com.colora.world.engine.content.CharPart
import com.colora.world.engine.paint.PaintState

/** Composes a character recipe (body + socketed parts) exactly like tools/art/build_extras.py draw_char. */
object CharacterRenderer {
    fun socketOf(kind: String): Socket = when (kind) { "ear" -> Socket.EARS; "tail" -> Socket.TAIL; "wing" -> Socket.WINGS; "hat" -> Socket.HAT; else -> Socket.DECOR }

    data class Layer(val artId: String, val art: ArtModel, val matrix: Matrix, val fills: Map<String, Int>, val socket: Socket?, val socketName: String?)

    /** Layers in paint order (behind parts → body → front parts) with their design-space matrices. */
    fun layers(content: ContentRepo, r: CharacterRecipe): List<Layer> {
        val pack = content.characters
        val body = pack.bodies.firstOrNull { it.id == r.bodyId } ?: return emptyList()
        val bodyArt = content.art(body.id) ?: return emptyList()
        val out = ArrayList<Layer>()
        fun parts(z: String) {
            for ((socket, choice) in r.parts) {
                val spec: CharPart = pack.parts.firstOrNull { it.id == choice.partId } ?: continue
                val art = content.art(spec.id) ?: continue
                for (sname in spec.sockets) {
                    val sk = body.sockets[sname] ?: continue
                    if (sk.z != z) continue
                    val limit = pack.scaleLimits["${body.id}|${spec.id}|$sname"] ?: spec.maxScale
                    val k = sk.scale * choice.scale.coerceIn(spec.minScale, minOf(spec.maxScale, limit))
                    val m = Matrix().apply {
                        setTranslate(-512f, -512f); postScale(k, k); postRotate(sk.rot)
                        if (sk.mirror) postScale(-1f, 1f)
                        postTranslate(sk.x, sk.y)
                    }
                    out += Layer(spec.id, art, m, choice.fills, socket, sname)
                }
            }
        }
        parts("behind")
        out += Layer(body.id, bodyArt, Matrix(), r.bodyFills, null, null)
        parts("front")
        return out
    }

    fun draw(c: Canvas, content: ContentRepo, r: CharacterRecipe, highlight: Pair<String, String>? = null) {
        for (l in layers(content, r)) {
            val s = c.save(); c.concat(l.matrix)
            ArtRenderer.draw(c, l.art, PaintState(fills = l.fills), ArtRenderer.Options(useSamples = true, highlightRegion = highlight?.takeIf { it.first == l.artId }?.second))
            c.restoreToCount(s)
        }
    }

    fun bake(content: ContentRepo, r: CharacterRecipe, scale: Float = 0.6f): Bitmap {
        val n = (1024 * scale).toInt()
        val b = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888)
        val c = Canvas(b); c.scale(scale, scale)
        draw(c, content, r)
        return b
    }

    fun withPart(r: CharacterRecipe, socket: Socket, partId: String?): CharacterRecipe =
        r.copy(parts = if (partId == null) r.parts - socket else r.parts + (socket to (r.parts[socket]?.takeIf { it.partId == partId } ?: PartChoice(partId))))
}
