"""Colora World vector art DSL.

Every template, prop, sticker, character part and world layer is authored here
as layered vector shapes in a 1024x1024 design space (y grows downward). The
output is a JSON contract consumed by the Android renderer and the JVM engine:
closed fillable regions grouped into rig parts, a fixed outline/detail layer,
pivots and anchors. Path data is emitted as absolute SVG commands M/L/Q/C/Z only.

All artwork produced by this module is original and authored in this
repository (see docs/ASSET_LICENSES.md).
"""
from __future__ import annotations

import json
import math
from dataclasses import dataclass, field
from typing import Iterable, Sequence

import skia

INK = 0xFF24324B
WHITE = 0xFFFFFFFF
REGION_OUTLINE_W = 9.0
SILHOUETTE_OUTLINE_W = 13.0
DETAIL_W = 7.0

Pt = tuple[float, float]

# ---------------------------------------------------------------- primitives

def P() -> skia.Path:
    return skia.Path()


def circle(x: float, y: float, r: float) -> skia.Path:
    p = P(); p.addCircle(x, y, r); return p


def ellipse(cx: float, cy: float, rx: float, ry: float, rot: float = 0.0) -> skia.Path:
    p = P(); p.addOval(skia.Rect(cx - rx, cy - ry, cx + rx, cy + ry))
    return rotate(p, rot, cx, cy) if rot else p


def rect(l: float, t: float, r: float, b: float) -> skia.Path:
    p = P(); p.addRect(skia.Rect(l, t, r, b)); return p


def rrect(l: float, t: float, r: float, b: float, rad: float, rot: float = 0.0) -> skia.Path:
    p = P(); p.addRRect(skia.RRect.MakeRectXY(skia.Rect(l, t, r, b), rad, rad))
    return rotate(p, rot, (l + r) / 2, (t + b) / 2) if rot else p


def poly(points: Sequence[Pt], r: float = 0.0) -> skia.Path:
    """Closed polygon; r>0 rounds every corner with a quadratic fillet."""
    pts = list(points)
    p = P()
    n = len(pts)
    if r <= 0:
        p.moveTo(*pts[0])
        for q in pts[1:]:
            p.lineTo(*q)
        p.close(); return p
    def toward(a: Pt, b: Pt, d: float) -> Pt:
        dx, dy = b[0] - a[0], b[1] - a[1]
        L = math.hypot(dx, dy) or 1.0
        d = min(d, L / 2.0)
        return (a[0] + dx * d / L, a[1] + dy * d / L)
    for i in range(n):
        prev, cur, nxt = pts[i - 1], pts[i], pts[(i + 1) % n]
        a = toward(cur, prev, r); b = toward(cur, nxt, r)
        if i == 0:
            p.moveTo(*a)
        else:
            p.lineTo(*a)
        p.quadTo(cur[0], cur[1], b[0], b[1])
    p.close(); return p


def _catmull(points: Sequence[Pt], closed: bool, tension: float = 1.0):
    pts = list(points)
    n = len(pts)
    segs = []
    rng = range(n) if closed else range(n - 1)
    for i in rng:
        p0 = pts[(i - 1) % n] if closed else pts[max(i - 1, 0)]
        p1 = pts[i]
        p2 = pts[(i + 1) % n]
        p3 = pts[(i + 2) % n] if closed else pts[min(i + 2, n - 1)]
        t = tension / 6.0
        c1 = (p1[0] + (p2[0] - p0[0]) * t, p1[1] + (p2[1] - p0[1]) * t)
        c2 = (p2[0] - (p3[0] - p1[0]) * t, p2[1] - (p3[1] - p1[1]) * t)
        segs.append((p1, c1, c2, p2))
    return segs


def blob(points: Sequence[Pt], tension: float = 1.0) -> skia.Path:
    """Smooth closed curve through points (Catmull-Rom -> cubic)."""
    segs = _catmull(points, True, tension)
    p = P(); p.moveTo(*segs[0][0])
    for _, c1, c2, e in segs:
        p.cubicTo(c1[0], c1[1], c2[0], c2[1], e[0], e[1])
    p.close(); return p


def curve(points: Sequence[Pt], tension: float = 1.0) -> skia.Path:
    """Smooth open curve through points."""
    if len(points) == 2:
        p = P(); p.moveTo(*points[0]); p.lineTo(*points[1]); return p
    segs = _catmull(points, False, tension)
    p = P(); p.moveTo(*segs[0][0])
    for _, c1, c2, e in segs:
        p.cubicTo(c1[0], c1[1], c2[0], c2[1], e[0], e[1])
    return p


def polyline(points: Sequence[Pt]) -> skia.Path:
    p = P(); p.moveTo(*points[0])
    for q in points[1:]:
        p.lineTo(*q)
    return p


def arc(cx: float, cy: float, rx: float, ry: float, start: float, sweep: float) -> skia.Path:
    p = P(); p.addArc(skia.Rect(cx - rx, cy - ry, cx + rx, cy + ry), start, sweep); return p


def thick(path_or_points, w: float, cap: str = "round") -> skia.Path:
    """Turn an open curve into a filled outline shape of width w."""
    src = path_or_points if isinstance(path_or_points, skia.Path) else curve(path_or_points)
    paint = skia.Paint()
    paint.setStyle(skia.Paint.kStroke_Style)
    paint.setStrokeWidth(w)
    paint.setStrokeCap(skia.Paint.kRound_Cap if cap == "round" else skia.Paint.kButt_Cap)
    paint.setStrokeJoin(skia.Paint.kRound_Join)
    dst = P(); paint.getFillPath(src, dst)
    return simplify(dst)


def star(cx: float, cy: float, r_out: float, r_in: float, n: int = 5, rot: float = -90, round_r: float = 0.0) -> skia.Path:
    pts = []
    for i in range(n * 2):
        a = math.radians(rot + i * 180.0 / n)
        r = r_out if i % 2 == 0 else r_in
        pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
    return poly(pts, round_r)


def heart(cx: float, cy: float, s: float) -> skia.Path:
    p = P()
    p.moveTo(cx, cy + s * 0.9)
    p.cubicTo(cx - s * 1.25, cy + s * 0.05, cx - s * 0.85, cy - s * 0.95, cx, cy - s * 0.35)
    p.cubicTo(cx + s * 0.85, cy - s * 0.95, cx + s * 1.25, cy + s * 0.05, cx, cy + s * 0.9)
    p.close(); return p


def petals(cx: float, cy: float, n: int, r_center: float, length: float, width: float, rot: float = 0.0, shape: str = "round") -> list[skia.Path]:
    out = []
    for i in range(n):
        a = rot + i * 360.0 / n
        if shape == "round":
            pet = ellipse(cx, cy - r_center - length / 2 + 6, width / 2, length / 2)
        elif shape == "pointed":
            pet = blob([(cx, cy - r_center + 8), (cx - width / 2, cy - r_center - length * 0.45), (cx, cy - r_center - length), (cx + width / 2, cy - r_center - length * 0.45)])
        else:  # heart-tipped
            pet = blob([(cx, cy - r_center + 8), (cx - width / 2, cy - r_center - length * 0.55), (cx - width * 0.22, cy - r_center - length), (cx, cy - r_center - length * 0.86), (cx + width * 0.22, cy - r_center - length), (cx + width / 2, cy - r_center - length * 0.55)])
        out.append(rotate(pet, a, cx, cy))
    return out

# ---------------------------------------------------------------- transforms & ops

def rotate(p: skia.Path, deg: float, cx: float, cy: float) -> skia.Path:
    m = skia.Matrix(); m.setRotate(deg, cx, cy)
    q = skia.Path(p); q.transform(m); return q


def translate(p: skia.Path, dx: float, dy: float) -> skia.Path:
    q = skia.Path(p); q.offset(dx, dy); return q


def scale(p: skia.Path, sx: float, sy: float | None = None, cx: float = 512, cy: float = 512) -> skia.Path:
    m = skia.Matrix(); m.setScale(sx, sx if sy is None else sy, cx, cy)
    q = skia.Path(p); q.transform(m); return q


def mirror(p: skia.Path, cx: float = 512) -> skia.Path:
    return scale(p, -1, 1, cx, 0)


def simplify(p: skia.Path) -> skia.Path:
    out = skia.Simplify(p)
    return out if out is not None else p


def union(*paths: skia.Path) -> skia.Path:
    flat = [q for q in _flatten(paths) if q is not None and not q.isEmpty()]
    acc = skia.Path(flat[0])
    for q in flat[1:]:
        r = skia.Op(acc, q, skia.PathOp.kUnion_PathOp)
        if r is not None:
            acc = r
    return simplify(acc)


def diff(a: skia.Path, *b: skia.Path) -> skia.Path:
    acc = a
    for q in _flatten(b):
        r = skia.Op(acc, q, skia.PathOp.kDifference_PathOp)
        if r is not None:
            acc = r
    return acc


def inter(a: skia.Path, b: skia.Path) -> skia.Path:
    r = skia.Op(a, b, skia.PathOp.kIntersect_PathOp)
    return r if r is not None else P()


def _flatten(items) -> list:
    out = []
    for it in items:
        if isinstance(it, (list, tuple)):
            out.extend(_flatten(it))
        else:
            out.append(it)
    return out

# ---------------------------------------------------------------- serialization

def _fmt(v: float) -> str:
    s = f"{v:.1f}"
    if s.endswith(".0"):
        s = s[:-2]
    return "0" if s == "-0" else s


def to_svg(p: skia.Path) -> str:
    out = []
    it = skia.Path.Iter(p, False)
    while True:
        verb, pts = it.next()
        if verb == skia.Path.kDone_Verb:
            break
        if verb == skia.Path.kMove_Verb:
            out.append(f"M{_fmt(pts[0].fX)} {_fmt(pts[0].fY)}")
        elif verb == skia.Path.kLine_Verb:
            out.append(f"L{_fmt(pts[1].fX)} {_fmt(pts[1].fY)}")
        elif verb == skia.Path.kQuad_Verb:
            out.append(f"Q{_fmt(pts[1].fX)} {_fmt(pts[1].fY)} {_fmt(pts[2].fX)} {_fmt(pts[2].fY)}")
        elif verb == skia.Path.kConic_Verb:
            quads = skia.Path.ConvertConicToQuads(pts[0], pts[1], pts[2], it.conicWeight(), 2)
            # quads: list of points p0, c1, p1, c2, p2 ...
            for i in range(1, len(quads), 2):
                c, e = quads[i], quads[i + 1]
                out.append(f"Q{_fmt(c.fX)} {_fmt(c.fY)} {_fmt(e.fX)} {_fmt(e.fY)}")
        elif verb == skia.Path.kCubic_Verb:
            out.append("C" + " ".join(f"{_fmt(q.fX)} {_fmt(q.fY)}" for q in pts[1:4]))
        elif verb == skia.Path.kClose_Verb:
            out.append("Z")
    return "".join(out)


def from_svg(d: str) -> skia.Path:
    import re
    toks = re.findall(r"[MLQCZ]|-?\d+(?:\.\d+)?", d)
    p = P(); i = 0
    while i < len(toks):
        c = toks[i]; i += 1
        if c == "M":
            p.moveTo(float(toks[i]), float(toks[i + 1])); i += 2
        elif c == "L":
            p.lineTo(float(toks[i]), float(toks[i + 1])); i += 2
        elif c == "Q":
            p.quadTo(*[float(t) for t in toks[i:i + 4]]); i += 4
        elif c == "C":
            p.cubicTo(*[float(t) for t in toks[i:i + 6]]); i += 6
        elif c == "Z":
            p.close()
    return p

# ---------------------------------------------------------------- template builder

@dataclass
class Region:
    id: str
    part: str
    path: skia.Path
    sample: int


@dataclass
class Detail:
    part: str
    path: skia.Path
    kind: str  # "line" | "ink" | "white" | "blush"
    width: float = DETAIL_W


@dataclass
class Part:
    id: str
    parent: str | None
    pivot: Pt
    motion: str | None
    amp: float


class Art:
    """Builder for one layered vector artwork (template, prop, sticker...)."""

    def __init__(self, art_id: str, size: int = 1024):
        self.id = art_id
        self.size = size
        self.parts: list[Part] = []
        self.regions: list[Region] = []
        self.details: list[Detail] = []
        self.anchors: list[dict] = []
        self._part = None
        self._rid = 0
        self._part = "body"

    # parts are drawn in declaration order (later = in front)
    def part(self, pid: str, parent: str | None = None, pivot: Pt = (512, 512), motion: str | None = None, amp: float = 0.0) -> "Art":
        existing = next((p for p in self.parts if p.id == pid), None)
        if existing is None:
            self.parts.append(Part(pid, parent, pivot, motion, amp))
        self._part = pid
        return self

    def region(self, name: str, path: skia.Path, sample: int) -> "Art":
        if not any(p.id == self._part for p in self.parts):
            self.parts.append(Part(self._part, None, (512, 512), None, 0.0))
        self.regions.append(Region(name, self._part, simplify(path), sample))
        return self

    def line(self, path_or_points, w: float = DETAIL_W) -> "Art":
        p = path_or_points if isinstance(path_or_points, skia.Path) else curve(path_or_points)
        self.details.append(Detail(self._part, p, "line", w))
        return self

    def ink(self, path: skia.Path) -> "Art":
        self.details.append(Detail(self._part, path, "ink"))
        return self

    def shine(self, path: skia.Path) -> "Art":
        self.details.append(Detail(self._part, path, "white"))
        return self

    def blush(self, path: skia.Path) -> "Art":
        self.details.append(Detail(self._part, path, "blush"))
        return self

    def anchor(self, aid: str, kind: str, x: float, y: float) -> "Art":
        self.anchors.append({"id": aid, "type": kind, "x": round(x, 1), "y": round(y, 1)})
        return self

    # ---- faces ------------------------------------------------------
    def eyes(self, cx: float, cy: float, gap: float, r: float = 17, look: Pt = (0, 0), lids: bool = False) -> "Art":
        for sx in (-1, 1):
            x = cx + sx * gap / 2
            if lids:
                self.line(arc(x, cy, r * 1.05, r * 0.8, 200, 140), w=8)
            else:
                self.ink(ellipse(x + look[0], cy + look[1], r * 0.86, r))
                self.shine(circle(x + look[0] + r * 0.32, cy + look[1] - r * 0.36, r * 0.32))
        return self

    def smile(self, cx: float, cy: float, w: float, depth: float = 0.5, width: float = 7) -> "Art":
        self.line(curve([(cx - w / 2, cy), (cx, cy + w * depth * 0.5), (cx + w / 2, cy)]), w=width)
        return self

    def cheeks(self, cx: float, cy: float, gap: float, r: float = 15) -> "Art":
        for sx in (-1, 1):
            self.blush(ellipse(cx + sx * gap / 2, cy, r * 1.25, r * 0.8))
        return self

    # ---- export ------------------------------------------------------
    def bounds(self) -> tuple[float, float, float, float]:
        sil = self.silhouette()
        b = sil.computeTightBounds()
        return (b.left(), b.top(), b.right(), b.bottom())

    def part_silhouette(self, pid: str) -> skia.Path:
        ps = [r.path for r in self.regions if r.part == pid]
        return union(*ps) if ps else P()

    def silhouette(self) -> skia.Path:
        return union(*[r.path for r in self.regions])

    def visible_regions(self) -> list[tuple[Region, skia.Path]]:
        """Within each part, later regions occlude earlier ones; the stored
        geometry is the visible (non-overlapping) piece so taps, clipping and
        outlines agree. Across parts the full shapes are kept so animated joints
        overlap instead of revealing paper."""
        out = []
        for i, r in enumerate(self.regions):
            above = [q.path for q in self.regions[i + 1:] if q.part == r.part]
            vis = diff(r.path, *above) if above else r.path
            vis = simplify(vis)
            out.append((r, vis))
        return out

    def to_json(self, extra: dict) -> dict:
        vis = self.visible_regions()
        ids = set()
        regions = []
        for order, (r, path) in enumerate(vis):
            rid = r.id
            k = 2
            while rid in ids:
                rid = f"{r.id}{k}"; k += 1
            ids.add(rid)
            b = path.computeTightBounds()
            if path.isEmpty() or b.width() < 1 or b.height() < 1:
                raise ValueError(f"{self.id}: region {rid} fully hidden")
            regions.append({
                "id": rid, "part": r.part, "order": order,
                "d": to_svg(path),
                "bounds": [round(b.left(), 1), round(b.top(), 1), round(b.right(), 1), round(b.bottom(), 1)],
                "sample": f"#{r.sample & 0xFFFFFF:06X}",
            })
        parts = []
        for p in self.parts:
            if not any(r.part == p.id for r in self.regions):
                continue
            sil = self.part_silhouette(p.id)
            parts.append({
                "id": p.id, "parent": p.parent,
                "pivot": [round(p.pivot[0], 1), round(p.pivot[1], 1)],
                "motion": p.motion, "amp": p.amp,
                "silhouette": to_svg(sil),
            })
        details = [{"part": d.part, "kind": d.kind, "w": d.width, "d": to_svg(d.path)} for d in self.details]
        bl, bt, br, bb = self.bounds()
        out = {"schemaVersion": 1, "id": self.id, "designBounds": {"width": self.size, "height": self.size},
               "contentBounds": [round(bl, 1), round(bt, 1), round(br, 1), round(bb, 1)]}
        out.update(extra)
        out.update({"parts": parts, "regions": regions, "details": details, "anchors": self.anchors})
        return out


def scallop(cx: float, cy: float, rx: float, ry: float, n: int, bump: float, rot: float = 0.0) -> skia.Path:
    """Cloud / mane / wool outline: bumps of radius `bump` around an ellipse."""
    shapes = [ellipse(cx, cy, rx, ry)]
    for i in range(n):
        t = math.radians(rot + i * 360.0 / n)
        shapes.append(circle(cx + rx * math.cos(t), cy + ry * math.sin(t), bump))
    return union(*shapes)


def ring_points(cx: float, cy: float, r1: float, r2: float, n: int, rot: float = -90) -> list[Pt]:
    out = []
    for i in range(n * 2):
        a = math.radians(rot + i * 180.0 / n)
        r = r1 if i % 2 == 0 else r2
        out.append((cx + r * math.cos(a), cy + r * math.sin(a)))
    return out


def wave_band(y: float, left: float = 110, right: float = 914, bottom: float = 900, n: int = 6, amp: float = 22) -> skia.Path:
    """Water body with a scalloped wavy top edge and rounded bottom."""
    p = P(); step = (right - left) / n
    p.moveTo(left, y)
    for i in range(n):
        x0 = left + i * step
        p.quadTo(x0 + step / 2, y - amp * 1.6, x0 + step, y)
    p.quadTo(right + 20, (y + bottom) / 2, right - 40, bottom)
    p.lineTo(left + 40, bottom)
    p.quadTo(left - 20, (y + bottom) / 2, left, y)
    p.close(); return p
