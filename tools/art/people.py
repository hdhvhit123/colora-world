"""Shared figure construction for family/jobs. Each template still adds its own
pose, outfit silhouette and props so no two templates read the same."""
from colora_art import *
from pal import *


class Fig:
    def __init__(self, a: Art, cx=512, top=140, s=1.0, child=False):
        self.a = a; self.cx = cx; self.s = s; self.child = child
        self.hr = (120 if child else 104) * s
        self.hy = top + self.hr
        self.neck = self.hy + self.hr * 0.95
        self.torso_h = (170 if child else 230) * s
        self.hip = self.neck + self.torso_h
        self.leg_h = (190 if child else 250) * s
        self.foot = self.hip + self.leg_h
        self.tw = (190 if child else 210) * s

    def X(self, dx):
        return self.cx + dx * self.s

    # ---- lower body -------------------------------------------------
    def legs(self, kind="pants", color=BLUE, shoe=DGREY, skin=SKIN1, sit=False):
        a, s, cx = self.a, self.s, self.cx
        lw = 64 * s
        if sit:
            a.region("legs", union(rrect(cx - self.tw / 2, self.hip - 30 * s, cx + self.tw / 2 + 120 * s, self.hip + 60 * s, 34 * s),
                                   rrect(cx + self.tw / 2 + 50 * s, self.hip, cx + self.tw / 2 + 120 * s, self.hip + 190 * s, 30 * s)), color)
            a.region("shoe", ellipse(cx + self.tw / 2 + 110 * s, self.hip + 196 * s, 60 * s, 28 * s), shoe)
            return
        for i, sx in enumerate((-1, 1)):
            x = cx + sx * 50 * s
            if kind in ("skirt", "dress", "shorts"):
                a.region(f"leg{i}", rrect(x - 26 * s, self.hip - 10 * s, x + 26 * s, self.foot, 24 * s), skin)
            else:
                a.region(f"leg{i}", rrect(x - lw / 2, self.hip - 20 * s, x + lw / 2, self.foot, 26 * s), color)
            a.region(f"shoe{i}", ellipse(x + sx * 14 * s, self.foot + 6 * s, 50 * s, 26 * s), shoe)
        if kind == "shorts":
            a.region("shorts", poly([(cx - self.tw / 2, self.hip - 30 * s), (cx + self.tw / 2, self.hip - 30 * s), (cx + self.tw / 2 + 8 * s, self.hip + 80 * s), (cx - self.tw / 2 - 8 * s, self.hip + 80 * s)], 18 * s), color)
        if kind == "skirt":
            a.region("skirt", poly([(cx - self.tw / 2 + 6 * s, self.hip - 40 * s), (cx + self.tw / 2 - 6 * s, self.hip - 40 * s), (cx + self.tw / 2 + 50 * s, self.hip + 110 * s), (cx - self.tw / 2 - 50 * s, self.hip + 110 * s)], 24 * s), color)

    def torso(self, color=CORAL, kind="shirt", coat=None):
        a, s, cx = self.a, self.s, self.cx
        top = self.neck - 6 * s
        if kind == "dress":
            sh = poly([(cx - self.tw / 2, top), (cx + self.tw / 2, top), (cx + self.tw / 2 + 70 * s, self.hip + 120 * s), (cx - self.tw / 2 - 70 * s, self.hip + 120 * s)], 34 * s)
        else:
            sh = rrect(cx - self.tw / 2, top, cx + self.tw / 2, self.hip + 10 * s, 56 * s)
        a.region("top", sh, color)
        if coat:
            for i, sx in enumerate((-1, 1)):
                flap = poly([(cx + sx * self.tw / 2, top), (cx + sx * 14 * s, top), (cx + sx * 14 * s, self.hip + 60 * s), (cx + sx * (self.tw / 2 + 10 * s), self.hip + 60 * s)], 26 * s)
                a.region(f"coat{i}", flap, coat)
        return sh

    def arm(self, name, pts, sleeve=CORAL, skin=SKIN1, part=None, pivot=None, motion=None, amp=0.0, w=54):
        a, s = self.a, self.s
        prev = a._part
        if part:
            a.part(part, parent="body", pivot=pivot or pts[0], motion=motion, amp=amp)
        P_ = [(self.cx + x * s, y) for x, y in pts]
        a.region(f"{name}", thick(P_, w * s), sleeve)
        a.region(f"{name}_hand", circle(P_[-1][0], P_[-1][1], 30 * s), skin)
        if part:
            a.part(prev)
        return P_[-1]

    # ---- head -------------------------------------------------------
    def head(self, skin=SKIN1, hair=HAIR1, style="short", face=True, back_hair=True):
        a, s, cx, hy, hr = self.a, self.s, self.cx, self.hy, self.hr
        a.part("head", parent="body", pivot=(cx, self.neck), motion="tilt", amp=4)
        if style in ("long", "ponytail", "bun", "curly_long") and back_hair:
            if style == "long":
                a.region("hair_back", rrect(cx - hr * 1.12, hy - hr * 0.6, cx + hr * 1.12, hy + hr * 1.35, hr * 0.6), hair)
            elif style == "ponytail":
                a.region("hair_back", blob([(cx + hr * 0.7, hy - hr * 0.6), (cx + hr * 1.6, hy - hr * 0.2), (cx + hr * 1.5, hy + hr * 0.8), (cx + hr * 1.0, hy + hr * 0.3)]), hair)
            elif style == "bun":
                a.region("hair_back", circle(cx, hy - hr * 1.05, hr * 0.48), hair)
            else:
                a.region("hair_back", scallop(cx, hy + hr * 0.2, hr * 1.05, hr * 1.1, 12, hr * 0.3), hair)
        a.region("ear_l", circle(cx - hr * 0.98, hy + hr * 0.08, hr * 0.2), skin)
        a.region("ear_r", circle(cx + hr * 0.98, hy + hr * 0.08, hr * 0.2), skin)
        a.region("face", ellipse(cx, hy, hr, hr * 0.98), skin)
        if style == "short":
            a.region("hair", union(inter(ellipse(cx, hy - hr * 0.1, hr * 1.06, hr * 1.0), rect(cx - 2 * hr, hy - 2 * hr, cx + 2 * hr, hy - hr * 0.35)), ellipse(cx - hr * 0.5, hy - hr * 0.45, hr * 0.5, hr * 0.28, -10)), hair)
        elif style == "curly":
            a.region("hair", inter(scallop(cx, hy - hr * 0.35, hr * 0.92, hr * 0.62, 11, hr * 0.28), rect(cx - 2 * hr, hy - 2 * hr, cx + 2 * hr, hy - hr * 0.2)), hair)
        elif style == "spiky":
            pts = [(cx - hr * 1.05, hy - hr * 0.2)]
            for k in range(7):
                t = k / 6
                pts.append((cx - hr * 0.95 + t * hr * 1.9, hy - hr * (1.25 if k % 2 == 0 else 0.85)))
            pts.append((cx + hr * 1.05, hy - hr * 0.2)); pts.append((cx, hy - hr * 0.55))
            a.region("hair", poly(pts, 12 * s), hair)
        elif style in ("long", "ponytail", "bun", "curly_long"):
            a.region("hair", union(inter(ellipse(cx, hy - hr * 0.1, hr * 1.06, hr * 1.0), rect(cx - 2 * hr, hy - 2 * hr, cx + 2 * hr, hy - hr * 0.4)), ellipse(cx + hr * 0.35, hy - hr * 0.5, hr * 0.62, hr * 0.3, 12)), hair)
        elif style == "bald":
            a.region("hair", union(ellipse(cx - hr * 0.95, hy - hr * 0.15, hr * 0.22, hr * 0.32), ellipse(cx + hr * 0.95, hy - hr * 0.15, hr * 0.22, hr * 0.32)), hair)
        if face:
            a.eyes(cx, hy + hr * 0.02, hr * 0.72, hr * 0.15)
            a.smile(cx, hy + hr * 0.38, hr * 0.5, 0.5, 6 * s + 1)
            a.cheeks(cx, hy + hr * 0.32, hr * 1.25, hr * 0.13)
        return hy

    def neckpiece(self, color, kind="collar"):
        a, s, cx = self.a, self.s, self.cx
        if kind == "collar":
            a.region("collar_l", poly([(cx, self.neck + 30 * s), (cx - 70 * s, self.neck - 6 * s), (cx - 20 * s, self.neck - 6 * s)], 10 * s), color)
            a.region("collar_r", poly([(cx, self.neck + 30 * s), (cx + 70 * s, self.neck - 6 * s), (cx + 20 * s, self.neck - 6 * s)], 10 * s), color)
        elif kind == "scarf":
            a.region("scarf", rrect(cx - 90 * s, self.neck - 16 * s, cx + 90 * s, self.neck + 28 * s, 20 * s), color)
            a.region("scarf_tail", rrect(cx + 30 * s, self.neck, cx + 80 * s, self.neck + 120 * s, 18 * s), color)
