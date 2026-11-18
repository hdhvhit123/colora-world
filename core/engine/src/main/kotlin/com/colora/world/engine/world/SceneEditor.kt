package com.colora.world.engine.world

/** Lookup of what a placed object can do; supplied by the content layer. */
interface ToyCatalog {
    fun capabilities(templateId: String): Set<String>
    /** Anchors in toy-local normalised units relative to the toy's centre (dx, dy in scene fraction at scale 1). */
    fun anchors(templateId: String): List<ToyAnchor>
}

data class ToyAnchor(val id: String, val type: String, val dx: Float, val dy: Float)

sealed interface SceneOp {
    data class Add(val toy: ToyInstance) : SceneOp
    data class Move(val id: String, val x: Float, val y: Float) : SceneOp
    data class Scale(val id: String, val scale: Float) : SceneOp
    data class Flip(val id: String) : SceneOp
    data class BringFront(val id: String) : SceneOp
    data class SendBack(val id: String) : SceneOp
    /** Stores the toy back in the collection. Never deletes the artwork. */
    data class Store(val id: String) : SceneOp
    data class Attach(val childId: String, val parentId: String, val anchorId: String) : SceneOp
    data class Detach(val childId: String) : SceneOp
    data class SetEnvironment(val env: SceneEnvironment) : SceneOp
}

class SceneRuleException(msg: String) : IllegalStateException(msg)

/**
 * Pure, validated scene mutations with undo/redo (≥20 steps). Every op returns
 * a new immutable [SceneDocument]; persistence is the caller's job.
 */
class SceneEditor(initial: SceneDocument, private val catalog: ToyCatalog, private val undoLimit: Int = 40) {
    var doc: SceneDocument = initial; private set
    private val undo = ArrayDeque<SceneDocument>()
    private val redo = ArrayDeque<SceneDocument>()
    val canUndo get() = undo.isNotEmpty()
    val canRedo get() = redo.isNotEmpty()

    fun apply(op: SceneOp): SceneDocument {
        val next = reduce(doc, op)
        if (next == doc) return doc
        undo.addLast(doc); if (undo.size > undoLimit) undo.removeFirst()
        redo.clear()
        doc = next.copy(revision = doc.revision + 1)
        return doc
    }

    fun undo(): Boolean { val p = undo.removeLastOrNull() ?: return false; redo.addLast(doc); doc = p.copy(revision = doc.revision + 1); return true }
    fun redo(): Boolean { val n = redo.removeLastOrNull() ?: return false; undo.addLast(doc); doc = n.copy(revision = doc.revision + 1); return true }

    private fun reduce(d: SceneDocument, op: SceneOp): SceneDocument = when (op) {
        is SceneOp.Add -> {
            if (d.objects.size >= SceneDocument.MAX_OBJECTS) throw SceneRuleException("scene full")
            if (d.obj(op.toy.id) != null) d // idempotent: double-tap does not duplicate
            else d.copy(objects = d.objects + op.toy.copy(transform = op.toy.transform.clamped(), zOrder = (d.objects.maxOfOrNull { it.zOrder } ?: 0) + 1, attachment = null))
        }
        is SceneOp.Move -> update(d, op.id) { t ->
            if (t.attachment != null) t else t.copy(transform = t.transform.copy(x = op.x, y = op.y).clamped())
        }.let { moved -> syncChildren(moved) }
        is SceneOp.Scale -> update(d, op.id) { it.copy(transform = it.transform.copy(scale = op.scale).clamped()) }.let { syncChildren(it) }
        is SceneOp.Flip -> update(d, op.id) { it.copy(transform = it.transform.copy(flipX = !it.transform.flipX)) }
        is SceneOp.BringFront -> update(d, op.id) { it.copy(zOrder = (d.objects.maxOf { o -> o.zOrder }) + 1) }
        is SceneOp.SendBack -> update(d, op.id) { it.copy(zOrder = (d.objects.minOf { o -> o.zOrder }) - 1) }
        is SceneOp.Store -> {
            // children riding on the stored toy are detached to the ground first
            var cur = d
            for (child in d.objects.filter { it.attachment?.parentId == op.id }) cur = detach(cur, child.id)
            cur.copy(objects = cur.objects.filterNot { it.id == op.id })
        }
        is SceneOp.Attach -> attach(d, op)
        is SceneOp.Detach -> detach(d, op.childId)
        is SceneOp.SetEnvironment -> d.copy(environment = op.env)
    }

    private fun attach(d: SceneDocument, op: SceneOp.Attach): SceneDocument {
        val child = d.obj(op.childId) ?: throw SceneRuleException("no child")
        val parent = d.obj(op.parentId) ?: throw SceneRuleException("no parent")
        if (child.id == parent.id) throw SceneRuleException("self attach")
        // no cycles: parent chain must not contain child
        var p: ToyInstance? = parent
        while (p != null) {
            if (p.id == child.id) throw SceneRuleException("cycle")
            p = p.attachment?.let { d.obj(it.parentId) }
        }
        val anchor = catalog.anchors(parent.templateId).firstOrNull { it.id == op.anchorId } ?: throw SceneRuleException("no anchor")
        if (!AttachRules.accepts(anchor.type, catalog.capabilities(child.templateId))) throw SceneRuleException("incompatible")
        if (d.objects.any { it.attachment == Attachment(parent.id, anchor.id) && it.id != child.id }) throw SceneRuleException("anchor occupied")
        val pos = anchorPosition(parent, anchor)
        val moved = d.objects.map {
            if (it.id == child.id) it.copy(attachment = Attachment(parent.id, anchor.id), transform = it.transform.copy(x = pos.first, y = pos.second), zOrder = parent.zOrder + 1)
            else it
        }
        return d.copy(objects = moved)
    }

    private fun detach(d: SceneDocument, childId: String): SceneDocument {
        val child = d.obj(childId) ?: return d
        val att = child.attachment ?: return d
        val parent = d.obj(att.parentId)
        // drop beside the parent, on the ground line, within scene bounds
        val px = parent?.transform?.x ?: child.transform.x
        val ground = ((parent?.transform?.y ?: child.transform.y) + 0.02f)
        val side = if (px > 0.5f) -0.12f else 0.12f
        return update(d, childId) { it.copy(attachment = null, transform = it.transform.copy(x = px + side, y = ground).clamped()) }
    }

    /** Keeps attached children glued to their parent anchors after the parent moved. */
    fun syncChildren(d: SceneDocument): SceneDocument {
        var changed = true
        var cur = d
        var guard = 0
        while (changed && guard++ < 8) {
            changed = false
            cur = cur.copy(objects = cur.objects.map { o ->
                val att = o.attachment ?: return@map o
                val parent = cur.obj(att.parentId) ?: return@map o.copy(attachment = null)
                val anchor = catalog.anchors(parent.templateId).firstOrNull { it.id == att.anchorId } ?: return@map o.copy(attachment = null)
                val (x, y) = anchorPosition(parent, anchor)
                if (x != o.transform.x || y != o.transform.y) { changed = true; o.copy(transform = o.transform.copy(x = x, y = y)) } else o
            })
        }
        return cur
    }

    private fun update(d: SceneDocument, id: String, f: (ToyInstance) -> ToyInstance) =
        d.copy(objects = d.objects.map { if (it.id == id) f(it) else it })

    companion object {
        fun anchorPosition(parent: ToyInstance, a: ToyAnchor): Pair<Float, Float> {
            val s = parent.transform.scale
            val dx = if (parent.transform.flipX) -a.dx else a.dx
            return Pair(parent.transform.x + dx * s, parent.transform.y + a.dy * s)
        }
    }
}
