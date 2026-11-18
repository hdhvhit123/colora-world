"""Reference world renderer mirroring the Android WorldRenderer."""
import skia
from colora_art import from_svg
from render import INK, hexc


def render_world(canvas, w, offset=0.0, anchors=False):
    Wd, Hd = w["designSize"]
    sky = skia.GradientShader.MakeLinear([skia.Point(0, 0), skia.Point(0, Hd)], [hexc(w["sky"][0]), hexc(w["sky"][1])])
    canvas.drawRect(skia.Rect(0, 0, Wd, Hd), skia.Paint(Shader=sky))
    for layer in w["layers"]:
        canvas.save(); canvas.translate(-offset * layer["parallax"], 0)
        for s in layer["shapes"]:
            p = from_svg(s["d"])
            canvas.drawPath(p, skia.Paint(AntiAlias=True, Color=hexc(s["fill"])))
            if s["outline"]:
                canvas.drawPath(p, skia.Paint(AntiAlias=True, Color=INK, Style=skia.Paint.kStroke_Style, StrokeWidth=s["outline"], StrokeJoin=skia.Paint.kRound_Join))
        canvas.restore()
    if anchors:
        for a in w["anchors"]:
            canvas.drawCircle(a["x"] * Wd, a["y"] * Hd, 14, skia.Paint(AntiAlias=True, Color=skia.Color(0xFF, 0x30, 0x30)))
