"""Builds everything that is not one of the 168 templates: props, stickers,
character parts, Milo, worlds, stories, prompts (with illustrations),
palettes, EN/VI content locales and the SHA-256 content index.
Run after build_content.py (or via it):  python3 tools/art/build_extras.py"""
from __future__ import annotations
import hashlib, json, os, sys
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
sys.path.insert(0, HERE); sys.path.insert(0, os.path.join(ROOT, "tools", "content"))
import skia
from PIL import Image
from render import draw_art, PAPER, hexc
from extras import props, stickers, characters as CH, mascot, worlds
from extras.world_render import render_world
import stories as ST, prompts as PR, palettes as PAL, labels as L

C = os.path.join(ROOT, "app/src/main/assets/content")
CS = os.path.join(ROOT, "docs/contact_sheets")
CREAM = skia.Color(0xFF, 0xF8, 0xEF)


def save_webp(surface, path, q=88):
    arr = surface.makeImageSnapshot().convert(alphaType=skia.kUnpremul_AlphaType, colorType=skia.kRGBA_8888_ColorType).toarray()
    Image.fromarray(arr).save(path, "WEBP", quality=q, method=6)


def dump(path, obj, compact=True):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False, separators=(",", ":") if compact else None, indent=None if compact else 1)


def thumb(art, path, px=256, samples=True):
    s = skia.Surface(px, px); c = s.getCanvas(); c.clear(skia.Color(0, 0, 0, 0))
    c.scale(px / 1024, px / 1024); draw_art(c, art, use_samples=samples); save_webp(s, path)


def sheet(arts, path, cols=7, cell=220, samples=True):
    rows = (len(arts) + cols - 1) // cols
    s = skia.Surface(cell * cols, cell * rows); c = s.getCanvas(); c.clear(CREAM)
    for i, a in enumerate(arts):
        c.save(); c.translate((i % cols) * cell + 4, (i // cols) * cell + 4)
        c.drawRect(skia.Rect(0, 0, cell - 8, cell - 8), skia.Paint(Color=PAPER))
        c.scale((cell - 8) / 1024, (cell - 8) / 1024); draw_art(c, a, use_samples=samples); c.restore()
    s.makeImageSnapshot().save(path, skia.kPNG)


def build_items(kind, items, meta_fn):
    out = []; arts = []
    os.makedirs(os.path.join(C, kind), exist_ok=True); os.makedirs(os.path.join(C, "thumbs"), exist_ok=True)
    for it in items:
        iid, fn = it[0], it[1]
        art = fn().to_json(meta_fn(it))
        dump(os.path.join(C, kind, f"{iid}.json"), art)
        thumb(art, os.path.join(C, "thumbs", f"{iid}.webp"))
        arts.append(art); out.append(art)
    return out, arts


def main():
    en, vi = {}, {}

    def lab(key, pair):
        en[key], vi[key] = pair

    for t, p in L.TOPICS.items(): lab(f"topic.{t}", p)
    for t, p in L.TEMPLATES.items(): lab(f"template.{t}", p)
    for w, p in L.WORLDS.items(): lab(f"world.{w}", p)
    for k, p in L.PALETTES.items(): lab(f"palette.{k}", p)
    for k, p in L.STICKER_GROUPS.items(): lab(f"stickergroup.{k}", p)
    for k, p in L.PART_KINDS.items(): lab(f"partkind.{k}", p)
    for k, p in L.MILO.items(): lab(f"milo.{k}", p)

    # ---- props
    plist, parts_ = build_items("props", props.ALL, lambda it: {"kind": "prop", "capabilities": it[2], "access": {"tier": "base", "packId": "base"},
                                                                "labelKey": f"prop.{it[0].split('.', 1)[1]}", "preColored": True})
    for pid, _, caps in props.ALL: lab(f"prop.{pid.split('.', 1)[1]}", L.PROPS[pid.split('.', 1)[1]])
    sheet(parts_, os.path.join(CS, "props.png"))
    props_index = [{"id": a["id"], "file": f"props/{a['id']}.json", "thumb": f"thumbs/{a['id']}.webp", "capabilities": a["capabilities"],
                    "anchors": a["anchors"], "labelKey": a["labelKey"]} for a in plist]

    # ---- stickers
    slist, sarts = build_items("stickers", stickers.ALL, lambda it: {"kind": "sticker", "group": it[2][0], "preColored": True})
    sheet(sarts, os.path.join(CS, "stickers.png"), cols=6)
    stick_index = [{"id": a["id"], "group": a["group"], "file": f"stickers/{a['id']}.json", "thumb": f"thumbs/{a['id']}.webp"} for a in slist]

    # ---- mascot
    mlist, marts = build_items("mascot", mascot.ALL, lambda it: {"kind": "mascot", "preColored": True})
    sheet(marts, os.path.join(CS, "milo.png"), cols=6)

    # ---- characters
    bodies = []; part_specs = []; part_art = {}
    os.makedirs(os.path.join(C, "characters"), exist_ok=True)
    for bid, fn, sockets in CH.BODIES:
        art = fn().to_json({"kind": "char_body", "sockets": sockets, "preset": CH.PRESETS[bid], "labelKey": f"part.{bid.split('.', 1)[1]}"})
        dump(os.path.join(C, "characters", f"{bid}.json"), art); thumb(art, os.path.join(C, "thumbs", f"{bid}.webp"))
        bodies.append({"id": bid, "file": f"characters/{bid}.json", "thumb": f"thumbs/{bid}.webp", "preset": CH.PRESETS[bid], "sockets": sockets,
                       "regions": len(art["regions"])})
        lab(f"part.{bid.split('.', 1)[1]}", L.PARTS[bid.split('.', 1)[1]])
        part_art[bid] = art
    for pid, fn, kind, motion in CH.PARTS:
        art = fn().to_json({"kind": "char_part", "partKind": kind, "sockets": CH.SOCKETS_OF_KIND[kind], "labelKey": f"part.{pid.split('.', 1)[1]}"})
        dump(os.path.join(C, "characters", f"{pid}.json"), art); thumb(art, os.path.join(C, "thumbs", f"{pid}.webp"))
        part_specs.append({"id": pid, "kind": kind, "sockets": CH.SOCKETS_OF_KIND[kind], "file": f"characters/{pid}.json",
                           "thumb": f"thumbs/{pid}.webp", "minScale": 0.8, "maxScale": 1.2, "regions": len(art["regions"])})
        lab(f"part.{pid.split('.', 1)[1]}", L.PARTS[pid.split('.', 1)[1]])
        part_art[pid] = art
    # automatic socket-combination check. The builder stage shows the body at
    # STAGE scale around the centre; for every body x part x socket we compute the
    # largest part scale that keeps the composite inside the stage and store it
    # as a clamp. A combination that would need < minScale is a content error.
    STAGE = 0.72
    problems = []; limits = {}
    for b in bodies:
        for p in part_specs:
            bx = part_art[p["id"]]["contentBounds"]
            for sname in p["sockets"]:
                sk = b["sockets"][sname]
                def inside(k):
                    xs = [sk["x"] + (x - 512) * sk["scale"] * k * (-1 if sk.get("mirror") else 1) for x in (bx[0], bx[2])]
                    ys = [sk["y"] + (y - 512) * sk["scale"] * k for y in (bx[1], bx[3])]
                    xs = [512 + (x - 512) * STAGE for x in xs]; ys = [512 + (y - 512) * STAGE for y in ys]
                    return min(xs) >= 8 and max(xs) <= 1016 and min(ys) >= 8 and max(ys) <= 1016
                k = p["maxScale"]
                while k > 0.5 and not inside(k): k = round(k - 0.02, 2)
                if k < p["maxScale"]: limits[f"{b['id']}|{p['id']}|{sname}"] = k
                if k < p["minScale"]: problems.append(f"{b['id']}+{p['id']}@{sname} fits only at {k}")
    dump(os.path.join(C, "characters.json"), {"schemaVersion": 1, "stageScale": STAGE, "bodies": bodies, "parts": part_specs, "scaleLimits": limits,
                                                  "comboCheck": {"checked": sum(len(p["sockets"]) for p in part_specs) * len(bodies), "cannotFit": problems}}, compact=False)
    print("characters:", len(bodies), "bodies,", len(part_specs), "parts, clamped:", len(limits), "cannot fit:", problems)
    combo_sheet(bodies, part_specs, part_art)

    # ---- worlds
    os.makedirs(os.path.join(C, "worlds"), exist_ok=True)
    wl = []
    big = skia.Surface(1024, 512 * len(worlds.ALL)); bc = big.getCanvas()
    for i, fn in enumerate(worlds.ALL):
        w = fn().to_json()
        dump(os.path.join(C, "worlds", f"{w['id']}.json"), w)
        s = skia.Surface(512, 256); c = s.getCanvas(); c.scale(0.25, 0.25); render_world(c, w); save_webp(s, os.path.join(C, "thumbs", f"world.{w['id']}.webp"))
        bc.save(); bc.translate(0, i * 512); bc.scale(0.5, 0.5); render_world(bc, w, anchors=True); bc.restore()
        wl.append({"id": w["id"], "file": f"worlds/{w['id']}.json", "thumb": f"thumbs/world.{w['id']}.webp", "labelKey": w["labelKey"],
                   "anchors": len(w["anchors"]), "layers": [l["id"] for l in w["layers"]], "tier": "base" if w["id"] in ("garden", "town") else "full"})
    big.makeImageSnapshot().save(os.path.join(CS, "worlds.png"), skia.kPNG)

    # ---- stories
    story_defs = []
    for s in ST.S:
        n = s["id"].lower().replace("-", "")
        lab(f"story.{n}.title", (s["en"], s["vi"]))
        steps = []
        for stp in s["steps"]:
            key = f"story.{n}.{stp['id']}"; lab(key, (stp["en"], stp["vi"]))
            steps.append({"id": stp["id"], "textKey": key, "event": stp["event"], "subject": stp["subject"], "target": stp["target"],
                          "action": stp["action"], "count": stp["count"]})
        story_defs.append({"id": s["id"], "titleKey": f"story.{n}.title", "worldId": s["world"], "steps": steps, "reward": s["reward"], "iconTemplates": s["icons"]})
    dump(os.path.join(C, "stories.json"), {"schemaVersion": 1, "stories": story_defs}, compact=False)

    # ---- prompts + illustrations
    tmpl = {}
    def tart(tid):
        if tid not in tmpl:
            base = "templates" if not tid.startswith("prop.") else "props"
            tmpl[tid] = json.load(open(os.path.join(C, base, f"{tid}.json")))
        return tmpl[tid]
    pdefs = []; os.makedirs(os.path.join(C, "prompts"), exist_ok=True)
    ill_arts = []
    for (pid, pen, pvi, sugg, aen, avi, alt, pal, dest) in PR.P:
        n = pid.lower().replace("-", ""); lab(f"prompt.{n}", (pen, pvi))
        if aen: lab(f"prompt.{n}.alt", (aen, avi))
        pdefs.append({"id": pid, "textKey": f"prompt.{n}", "suggestedTemplates": sugg, "altTextKey": f"prompt.{n}.alt" if aen else None,
                      "altTemplates": alt, "paletteId": pal, "destination": dest, "illustration": f"prompts/{pid}.webp"})
        illustrate(pid, sugg, pal, dest, tart, part_art, os.path.join(C, "prompts", f"{pid}.webp"))
    dump(os.path.join(C, "prompts.json"), {"schemaVersion": 1, "prompts": pdefs}, compact=False)
    prompt_sheet([os.path.join(C, "prompts", f"{p[0]}.webp") for p in PR.P], os.path.join(CS, "prompts.png"))

    dump(os.path.join(C, "palettes.json"), {"schemaVersion": 1, "palettes": [{"id": k, "labelKey": f"palette.{k}", "colors": v} for k, v in PAL.PALETTES.items()],
                                            "extended": PAL.EXTENDED}, compact=False)
    dump(os.path.join(C, "extras.json"), {"schemaVersion": 1, "props": props_index, "stickers": stick_index,
                                          "mascot": [{"id": a["id"], "file": f"mascot/{a['id']}.json", "thumb": f"thumbs/{a['id']}.webp"} for a in mlist],
                                          "worlds": wl}, compact=False)
    for t in L.STICKER_GROUPS: pass
    dump(os.path.join(C, "locales/en.json"), dict(sorted(en.items())), compact=False)
    dump(os.path.join(C, "locales/vi.json"), dict(sorted(vi.items())), compact=False)

    # ---- integrity index (validated by content-tools and at app start)
    idx = []
    for root, _, files in os.walk(C):
        for fn in sorted(files):
            if fn == "index.json": continue
            p = os.path.join(root, fn); rel = os.path.relpath(p, C)
            with open(p, "rb") as f: b = f.read()
            idx.append({"path": rel, "size": len(b), "sha256": hashlib.sha256(b).hexdigest()})
    idx.sort(key=lambda e: e["path"])
    dump(os.path.join(C, "index.json"), {"schemaVersion": 1, "contentVersion": 1, "files": idx}, compact=False)
    print("props", len(plist), "stickers", len(slist), "worlds", len(wl), "stories", len(story_defs), "prompts", len(pdefs),
          "locale keys", len(en), "indexed files", len(idx))


def draw_char(c, body, parts, part_art, sel):
    def put(pid, sock):
        c.save(); c.translate(sock["x"], sock["y"])
        if sock.get("mirror"): c.scale(-1, 1)
        c.rotate(sock.get("rot", 0)); c.scale(sock["scale"], sock["scale"]); c.translate(-512, -512)
        draw_art(c, part_art[pid], use_samples=True); c.restore()
    for z in ("behind", None, "front"):
        if z is None:
            draw_art(c, part_art[body["id"]], use_samples=True); continue
        for pid in sel:
            spec = next(p for p in parts if p["id"] == pid)
            for sname in spec["sockets"]:
                if body["sockets"][sname]["z"] == z: put(pid, body["sockets"][sname])


def combo_sheet(bodies, parts, part_art):
    by = {}
    for p in parts: by.setdefault(p["kind"], []).append(p["id"])
    s = skia.Surface(6 * 240, 8 * 240); c = s.getCanvas(); c.clear(CREAM)
    for row in range(8):
        for i, b in enumerate(bodies):
            k = row * 6 + i
            sel = [by["ear"][k % 8], by["tail"][(k * 3) % 8], by["hat"][(k + row) % 8], by["decor"][(k * 5) % 8]]
            if row % 2: sel.append(by["wing"][k % 6])
            c.save(); c.translate(i * 240 + 4, row * 240 + 4); c.drawRect(skia.Rect(0, 0, 232, 232), skia.Paint(Color=PAPER))
            c.scale(232 / 1024, 232 / 1024); c.translate(512, 512); c.scale(0.82, 0.82); c.translate(-512, -512)
            draw_char(c, b, parts, part_art, sel); c.restore()
    s.makeImageSnapshot().save(os.path.join(CS, "characters.png"), skia.kPNG)


BG = {"bright_basic": 0xFFFFF3D6, "warm": 0xFFFFE6E0, "cool": 0xFFDDF2FB, "pastel": 0xFFF1EAFF, "rainbow": 0xFFFFF3D6,
      "earth": 0xFFF3E6D3, "nature": 0xFFE6F6DD, "night": 0xFF2D3A66, "skin": 0xFFFFEDE2}


def illustrate(pid, sugg, pal, dest, tart, part_art, path):
    W, H = 512, 384
    s = skia.Surface(W, H); c = s.getCanvas()
    c.drawRRect(skia.RRect.MakeRectXY(skia.Rect(0, 0, W, H), 40, 40), skia.Paint(AntiAlias=True, Color=skia.Color4f.FromColor(BG[pal]).toColor()))
    # palette dots along the bottom
    cols = PAL.PALETTES[pal][:6]
    for i, h in enumerate(cols):
        c.drawCircle(W / 2 + (i - (len(cols) - 1) / 2) * 44, H - 34, 15, skia.Paint(AntiAlias=True, Color=hexc(h)))
        c.drawCircle(W / 2 + (i - (len(cols) - 1) / 2) * 44, H - 34, 15, skia.Paint(AntiAlias=True, Color=skia.Color(0x24, 0x32, 0x4B), Style=skia.Paint.kStroke_Style, StrokeWidth=3))
    def at(a, cx, cy, size, samples=True, colors=None):
        c.save(); c.translate(cx - size / 2, cy - size / 2); c.scale(size / 1024, size / 1024)
        draw_art(c, a, colors=colors, use_samples=samples); c.restore()
    ink = skia.Color(0x24, 0x32, 0x4B)
    if pid == "CR-20":      # a character with wings
        c.save(); c.translate(W / 2 - 160, 160 - 160); c.scale(320 / 1024, 320 / 1024)
        c.translate(512, 512); c.scale(0.8, 0.8); c.translate(-512, -512)
        bodies = {b[0]: b for b in CH.BODIES}
        body = {"id": "char.body.fluffy", "sockets": bodies["char.body.fluffy"][2]}
        specs = [{"id": pid_, "sockets": CH.SOCKETS_OF_KIND[k]} for pid_, _, k, _ in CH.PARTS]
        draw_char(c, body, specs, part_art, ["char.wing.fairy", "char.ear.bunny", "char.hat.flower_crown"]); c.restore()
    elif pid == "CR-21":    # same picture, old palette -> new palette
        a = tart("animals.cat"); ids = [r["id"] for r in a["regions"]]
        old_ = {r: hexc(PAL.PALETTES["earth"][i % 6]) for i, r in enumerate(ids)}
        new_ = {r: hexc(PAL.PALETTES[pal][i % 6]) for i, r in enumerate(ids)}
        at(a, 140, 160, 220, colors=old_); at(a, 372, 160, 220, colors=new_)
        arrow = skia.Path(); arrow.moveTo(236, 160); arrow.lineTo(276, 160); arrow.moveTo(262, 146); arrow.lineTo(278, 160); arrow.lineTo(262, 174)
        c.drawPath(arrow, skia.Paint(AntiAlias=True, Color=ink, Style=skia.Paint.kStroke_Style, StrokeWidth=7, StrokeCap=skia.Paint.kRound_Cap, StrokeJoin=skia.Paint.kRound_Join))
    elif pid == "CR-24":    # three artworks gathered into a scene
        for i, t in enumerate(["animals.cat", "vehicles.car", "flowers.tulip"]):
            at(tart(t), 120 + i * 136, 175 - (i % 2) * 30, 160)
    elif pid == "CR-26":    # colour lab: red + blue drops make purple
        drops = [("#F25C54", 130), ("#5B8DEF", 256), ("#9B6BDE", 400)]
        for i, (h, x) in enumerate(drops):
            d = skia.Path(); d.moveTo(x, 70); d.cubicTo(x + 30, 120, x + 62, 150, x + 62, 190); d.cubicTo(x + 62, 230, x + 30, 252, x, 252)
            d.cubicTo(x - 30, 252, x - 62, 230, x - 62, 190); d.cubicTo(x - 62, 150, x - 30, 120, x, 70); d.close()
            c.drawPath(d, skia.Paint(AntiAlias=True, Color=hexc(h)))
            c.drawPath(d, skia.Paint(AntiAlias=True, Color=ink, Style=skia.Paint.kStroke_Style, StrokeWidth=7))
            c.drawCircle(x - 20, 170, 10, skia.Paint(AntiAlias=True, Color=skia.ColorWHITE))
        f = skia.Font(skia.Typeface.MakeFromFile(os.path.join(ROOT, "app/src/main/res/font/nunito_extrabold.ttf")), 64)
        c.drawString("+", 182, 190, f, skia.Paint(AntiAlias=True, Color=ink)); c.drawString("=", 318, 190, f, skia.Paint(AntiAlias=True, Color=ink))
    elif pid == "CR-30":    # a finished picture coming out on paper
        c.drawRRect(skia.RRect.MakeRectXY(skia.Rect(150, 40, 362, 290), 14, 14), skia.Paint(AntiAlias=True, Color=skia.ColorWHITE))
        c.drawRRect(skia.RRect.MakeRectXY(skia.Rect(150, 40, 362, 290), 14, 14), skia.Paint(AntiAlias=True, Color=ink, Style=skia.Paint.kStroke_Style, StrokeWidth=7))
        at(tart("nature.sun"), 256, 165, 190)
        at(json.load(open(os.path.join(C, "mascot", "milo.happy.json"))), 410, 230, 140)
    else:
        arts = [tart(t) for t in sugg[:3]]
        if not arts:
            fallback = {"character": ["char.body.fluffy"], "album": ["milo.happy"], "world": ["prop.house", "prop.park", "prop.lamp"],
                        "lab": ["milo.guide"], "book": ["prop.book"]}[dest]
            arts = [part_art.get(f) or tart(f) for f in fallback]
        n = len(arts); size = {1: 300, 2: 230, 3: 180}[n]
        for i, a in enumerate(arts):
            at(a, W / 2 + (i - (n - 1) / 2) * (size * 0.95), 160, size, samples=pid != "CR-25")
    save_webp(s, path, q=90)


def prompt_sheet(paths, out):
    imgs = [Image.open(p) for p in paths]
    w, h = 256, 192; cols = 6
    sheet = Image.new("RGB", (cols * (w + 8), ((len(imgs) + cols - 1) // cols) * (h + 8)), (255, 248, 239))
    for i, im in enumerate(imgs):
        bg = Image.new("RGBA", im.size, (255, 248, 239, 255)); bg.alpha_composite(im.convert("RGBA"))
        sheet.paste(bg.resize((w, h)).convert("RGB"), ((i % cols) * (w + 8) + 4, (i // cols) * (h + 8) + 4))
    sheet.save(out)


if __name__ == "__main__":
    main()
