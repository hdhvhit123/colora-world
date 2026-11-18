package com.colora.world

import com.colora.world.data.ArtworkDoc
import com.colora.world.data.BookDoc
import com.colora.world.data.BookItem
import com.colora.world.data.BookPage
import com.colora.world.data.CharacterDoc
import com.colora.world.data.Library
import com.colora.world.data.ToyRef
import com.colora.world.engine.character.CharacterRecipe
import com.colora.world.engine.paint.PaintCommand
import com.colora.world.engine.world.SceneDocument
import com.colora.world.engine.world.SceneTransform
import com.colora.world.engine.world.ToyInstance
import com.colora.world.render.CharacterRenderer

/** Realistic saved work for screenshot/export tests: coloured pictures, a character, a scene and a book. */
class Seed(val art: List<String>, val chr: String, val scene: String, val book: String)

fun seedLibrary(g: AppGraph, pid: String): Seed {
    val templates = listOf("animals.cat", "dinosaurs.spinosaurus", "animals.butterfly")
    val artRefs = templates.map { tid ->
        val model = g.content.art(tid)!!
        val cmds = model.parts.flatMap { it.regions }.map { PaintCommand.FillRegion(it.id, it.sample) }
        val id = Library.newDocId("a")
        val meta = g.library.saveArtwork(pid, id, ArtworkDoc(templateId = tid, commands = cmds))
        ToyRef.art(id, meta.revisionId).toString()
    }
    val pack = g.content.characters
    var recipe = CharacterRecipe(id = "seedchr", bodyId = "char.body.bean", preset = pack.bodies.first().preset)
    recipe = CharacterRenderer.withPart(recipe, CharacterRenderer.socketOf("ear"), "char.ear.bunny")
    recipe = CharacterRenderer.withPart(recipe, CharacterRenderer.socketOf("tail"), "char.tail.fox")
    pack.parts.firstOrNull { it.kind == "hat" }?.let { recipe = CharacterRenderer.withPart(recipe, CharacterRenderer.socketOf("hat"), it.id) }
    val bodyArt = g.content.art(recipe.bodyId)!!
    recipe = recipe.copy(bodyFills = bodyArt.parts.flatMap { it.regions }.associate { it.id to 0xFFFFC98B.toInt() })
    val cid = Library.newDocId("c")
    val cmeta = g.library.saveCharacter(pid, cid, CharacterDoc(recipe = recipe.copy(id = cid)))
    val chrRef = ToyRef.character(cid, cmeta.revisionId).toString()

    val sid = Library.newDocId("s")
    val objs = listOf(
        ToyInstance("o1", artRefs[0], templates[0], SceneTransform(0.30f, 0.86f, 1f), 0),
        ToyInstance("o2", artRefs[1], templates[1], SceneTransform(0.62f, 0.90f, 1.1f), 1),
        ToyInstance("o3", artRefs[2], templates[2], SceneTransform(0.45f, 0.45f, 0.7f), 2),
        ToyInstance("o4", chrRef, recipe.bodyId, SceneTransform(0.80f, 0.88f, 0.8f), 3),
    )
    g.library.saveScene(pid, SceneDocument(id = sid, profileId = pid, worldId = "garden", objects = objs))

    val bid = Library.newDocId("b")
    g.library.saveBook(pid, bid, BookDoc(pages = listOf(
        BookPage("one", listOf(BookItem(artRefs[0]))),
        BookPage("scene", listOf(BookItem("scene:$sid"))),
        BookPage("two", listOf(BookItem(artRefs[1]), BookItem(chrRef))),
        BookPage("one", listOf(BookItem(artRefs[2]))),
    )))
    return Seed(artRefs, chrRef, "scene:$sid", bid)
}
