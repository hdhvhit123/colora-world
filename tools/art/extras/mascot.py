"""Milo – the original Colora World mascot: a small soft paint drop with two
eyes and short arms (spec §3.4). Six expressions."""
from colora_art import *
from pal import *

DROP = [(512, 170), (600, 330), (720, 520), (730, 680), (640, 830), (512, 870), (384, 830), (294, 680), (304, 520), (424, 330)]


def base(name, tilt=0.0):
    a = Art(f"milo.{name}")
    a.part("shadow", pivot=(512, 900)); a.region("shadow", ellipse(512, 905, 190, 34), 0xFFEFE3D3)
    a._tilt = tilt
    return a


def body(a):
    a.part("body", pivot=(512, 880), motion="breathe", amp=4)
    a.region("drop", rotate(blob(DROP, 0.9), a._tilt, 512, 880), CORAL)
    a.shine(rotate(ellipse(420, 430, 34, 70, 25), a._tilt, 512, 880))
    a.shine(rotate(circle(452, 520, 14), a._tilt, 512, 880))


def arm(a, x, y, ang, length=120):
    import math
    ex = x + math.cos(math.radians(ang)) * length; ey = y + math.sin(math.radians(ang)) * length
    a.region(f"arm{len(a.regions)}", union(thick([(x, y), (ex, ey)], 62), circle(ex, ey, 42)), CORAL)
    return ex, ey


def greet():
    a = base("greet"); a.part("arms", parent="body", pivot=(700, 640), motion="wave", amp=20)
    arm(a, 310, 680, 140); arm(a, 712, 620, -50, 140)
    body(a); a.eyes(512, 600, 150, 30); a.smile(512, 690, 110, 0.6, 10); a.cheeks(512, 690, 260, 26); return a


def curious():
    a = base("curious", -8); a.part("arms", parent="body", pivot=(512, 700))
    arm(a, 316, 700, 160, 100); arm(a, 706, 680, 20, 100)
    body(a); a.eyes(500, 590, 150, 34, look=(10, -8)); a.ink(ellipse(500, 700, 26, 30)); a.cheeks(500, 690, 270, 24)
    a.line(curve([(560, 160), (610, 110), (650, 140), (620, 190)]), 10); a.ink(circle(616, 230, 9)); return a


def happy():
    a = base("happy"); a.part("arms", parent="body", pivot=(512, 600), motion="cheer", amp=10)
    arm(a, 316, 620, -140, 130); arm(a, 708, 620, -40, 130)
    body(a); a.eyes(512, 600, 150, 32, lids=True)
    a.region("mouth", diff(circle(512, 680, 70), rect(0, 0, 1024, 680)), 0xFFB5394A)
    a.cheeks(512, 680, 270, 28); return a


def guide():
    a = base("guide", 6); a.part("arms", parent="body", pivot=(720, 640), motion="point", amp=8)
    arm(a, 318, 700, 150, 100); ex, ey = arm(a, 712, 640, -20, 170)
    a.region("finger", circle(ex + 6, ey - 2, 28), CORAL)
    body(a); a.eyes(520, 600, 150, 30, look=(14, 0)); a.smile(520, 690, 90, 0.5, 9); a.cheeks(520, 690, 260, 24); return a


def listening():
    a = base("listening"); a.part("arms", parent="body", pivot=(512, 700))
    arm(a, 316, 700, 120, 90)
    ex, ey = arm(a, 712, 600, -35, 120)
    body(a); a.eyes(512, 610, 150, 30, look=(0, -6)); a.line(curve([(470, 700), (512, 710), (554, 700)]), 9)
    for i in range(2):
        a.line(arc(800, 420, 60 + i * 40, 60 + i * 40, -60, 120), 9)
    return a


def resting():
    a = base("resting"); a.part("arms", parent="body", pivot=(512, 760))
    arm(a, 320, 760, 60, 90); arm(a, 704, 760, 120, 90)
    body(a); a.eyes(512, 620, 150, 30, lids=True); a.smile(512, 700, 60, 0.4, 8); a.cheeks(512, 690, 260, 24)
    for i, (x, y, s) in enumerate([(700, 300, 36), (770, 220, 26), (820, 160, 18)]):
        a.line(polyline([(x - s, y - s), (x + s, y - s), (x - s, y + s), (x + s, y + s)]), 8)
    return a


ALL = [("milo.greet", greet), ("milo.curious", curious), ("milo.happy", happy),
       ("milo.guide", guide), ("milo.listening", listening), ("milo.resting", resting)]
