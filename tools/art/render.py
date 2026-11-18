"""Reference renderer (skia) mirroring the Android ArtRenderer layer order:
for each rig part in order: region fills -> paint -> region outlines ->
part silhouette outline -> fixed details. Used for thumbnails, contact sheets
and golden images."""
from __future__ import annotations
import skia
from colora_art import from_svg

INK = skia.Color(0x24, 0x32, 0x4B)
BLUSH = skia.Color(0xFF, 0x9C, 0x93, 0x99)
PAPER = skia.Color(0xFF, 0xFF, 0xFF)


def _paint(color, stroke=None):
    p = skia.Paint(AntiAlias=True, Color=color)
    if stroke:
        p.setStyle(skia.Paint.kStroke_Style)
        p.setStrokeWidth(stroke)
        p.setStrokeCap(skia.Paint.kRound_Cap)
        p.setStrokeJoin(skia.Paint.kRound_Join)
    return p


def hexc(s: str):
    v = int(s[1:], 16)
    return skia.Color((v >> 16) & 255, (v >> 8) & 255, v & 255)


def draw_art(canvas: skia.Canvas, art: dict, colors: dict | None = None, use_samples: bool = False, part_xf: dict | None = None):
    """colors: regionId -> skia color. Unlisted regions stay paper white."""
    regions = art["regions"]
    for part in art["parts"]:
        pid = part["id"]
        canvas.save()
        if part_xf and pid in part_xf:
            canvas.concat(part_xf[pid])
        prs = [r for r in regions if r["part"] == pid]
        for r in prs:
            c = None
            if colors and r["id"] in colors:
                c = colors[r["id"]]
            elif use_samples:
                c = hexc(r["sample"])
            canvas.drawPath(from_svg(r["d"]), _paint(c if c is not None else PAPER))
        for r in prs:
            canvas.drawPath(from_svg(r["d"]), _paint(INK, 9))
        canvas.drawPath(from_svg(part["silhouette"]), _paint(INK, 13))
        for d in art["details"]:
            if d["part"] != pid:
                continue
            path = from_svg(d["d"])
            if d["kind"] == "line":
                canvas.drawPath(path, _paint(INK, d["w"]))
            elif d["kind"] == "ink":
                canvas.drawPath(path, _paint(INK))
            elif d["kind"] == "white":
                canvas.drawPath(path, _paint(PAPER))
            elif d["kind"] == "blush":
                canvas.drawPath(path, _paint(BLUSH))
        canvas.restore()


def render_png(art: dict, out_path: str, px: int = 512, use_samples=False, bg=PAPER, fit=True):
    surf = skia.Surface(px, px)
    c = surf.getCanvas()
    c.clear(bg)
    s = px / art["designBounds"]["width"]
    c.scale(s, s)
    draw_art(c, art, use_samples=use_samples)
    img = surf.makeImageSnapshot()
    img.save(out_path, skia.kPNG)
    return img
