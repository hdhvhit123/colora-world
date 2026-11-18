"""Seven world backdrops (spec §7.1), each with three parallax layers
(far / mid / near), a sky gradient, interaction zones and ≥5 anchors.
Design space 2048×1024; anchors and zones are exported normalised 0..1."""
import math
from colora_art import *
from pal import *

W, H = 2048, 1024


class World:
    def __init__(self, wid, sky_top, sky_bottom, ground_y):
        self.id = wid; self.sky = (sky_top, sky_bottom); self.ground_y = ground_y
        self.layers = {"far": [], "mid": [], "near": []}
        self.zones = []; self.anchors = []

    def add(self, layer, path, color, outline=True):
        self.layers[layer].append((path, color, outline)); return self

    def zone(self, zid, ztype, path):
        self.zones.append((zid, ztype, path)); return self

    def anchor(self, aid, atype, x, y):
        self.anchors.append({"id": aid, "type": atype, "x": round(x / W, 4), "y": round(y / H, 4)}); return self

    def to_json(self):
        par = {"far": 0.15, "mid": 0.45, "near": 1.0}
        layers = []
        for lid, shapes in self.layers.items():
            layers.append({"id": lid, "parallax": par[lid], "shapes": [
                {"d": to_svg(simplify(p)), "fill": f"#{c & 0xFFFFFF:06X}", "outline": (0 if not o else (5 if lid == "far" else (7 if lid == "mid" else 9)))}
                for p, c, o in shapes]})
        return {"schemaVersion": 1, "id": self.id, "contentVersion": 1, "labelKey": f"world.{self.id}",
                "designSize": [W, H], "sky": [f"#{self.sky[0] & 0xFFFFFF:06X}", f"#{self.sky[1] & 0xFFFFFF:06X}"],
                "groundY": round(self.ground_y / H, 4), "layers": layers,
                "zones": [{"id": z, "type": t, "d": to_svg(simplify(p)), "bounds": _b(p)} for z, t, p in self.zones],
                "anchors": self.anchors}


def _b(p):
    b = p.computeTightBounds(); return [round(b.left() / W, 4), round(b.top() / H, 4), round(b.right() / W, 4), round(b.bottom() / H, 4)]


def hills(y, amp, n, seed, bottom=H, phase=0.0):
    pts = [(-40, bottom)]
    for i in range(n + 1):
        x = -40 + i * (W + 80) / n
        pts.append((x, y - amp * (0.6 + 0.4 * math.sin(i * 1.7 + seed)) * (1 if i % 2 else 0.55)))
    pts.append((W + 40, bottom))
    return blob(pts, 0.8)


def band(y, amp=14, n=10, seed=0.0, bottom=H):
    p = P(); p.moveTo(-40, bottom); p.lineTo(-40, y)
    step = (W + 80) / n
    for i in range(n):
        x0 = -40 + i * step
        p.quadTo(x0 + step / 2, y - amp * math.sin(i + seed) - amp, x0 + step, y)
    p.lineTo(W + 40, bottom); p.close(); return p


def cloud(x, y, s):
    return union(circle(x - 60 * s, y, 50 * s), circle(x, y - 30 * s, 70 * s), circle(x + 70 * s, y, 52 * s), rrect(x - 110 * s, y, x + 120 * s, y + 46 * s, 23 * s))


def tree(x, y, s, round_=True):
    trunk = rrect(x - 16 * s, y - 120 * s, x + 16 * s, y, 8 * s)
    crown = union(circle(x, y - 170 * s, 80 * s), circle(x - 55 * s, y - 130 * s, 55 * s), circle(x + 55 * s, y - 130 * s, 55 * s)) if round_ else \
        poly([(x - 80 * s, y - 90 * s), (x, y - 300 * s), (x + 80 * s, y - 90 * s)], 20 * s)
    return trunk, crown


def flowers_row(w, layer, y0, xs, cols):
    for i, x in enumerate(xs):
        w.add(layer, thick([(x, y0), (x, y0 - 70)], 12), GREEN)
        w.add(layer, scallop(x, y0 - 90, 34, 34, 6, 12), cols[i % len(cols)])
        w.add(layer, circle(x, y0 - 90, 13), SUN)


# ------------------------------------------------------------------ garden
def garden():
    w = World("garden", 0xFFBFE7FA, 0xFFF4FBFF, 760)
    w.add("far", cloud(380, 170, 1.1), WHITE, False).add("far", cloud(1500, 120, 0.9), WHITE, False)
    w.add("far", hills(520, 120, 6, 0.3), 0xFFC9EBC0, False)
    for x in (260, 610, 1660, 1880):
        t, c = tree(x, 560, 0.8); w.add("far", t, 0xFFC7A487, False).add("far", c, 0xFFA9D99B, False)
    w.add("mid", hills(640, 70, 5, 1.2), 0xFFB4E2A1)
    w.add("mid", rrect(1580, 470, 1900, 640, 24), CREAM).add("mid", poly([(1550, 480), (1740, 360), (1930, 480)], 16), CORAL)
    w.add("mid", rrect(1700, 540, 1770, 640, 30), SKY)
    for i in range(9): w.add("mid", rrect(80 + i * 60, 600, 100 + i * 60, 680, 8), 0xFFF2DDB0)
    w.add("mid", rect(70, 620, 610, 640), 0xFFF2DDB0)
    w.add("near", band(740, 10, 12, 0.5), LEAF)
    pond = ellipse(1250, 860, 260, 70); w.add("near", pond, SKY).add("near", ellipse(1180, 850, 70, 14), WHITE, False)
    soil = rrect(180, 800, 700, 900, 40); w.add("near", soil, BROWN)
    for i in range(5): w.add("near", ellipse(240 + i * 105, 840, 34, 12), HAIR2, False)
    path = blob([(800, 1024), (860, 900), (960, 780), (1040, 760), (1000, 860), (960, 1024)])
    w.add("near", path, SAND)
    w.add("near", rrect(1580, 820, 1880, 860, 18), TAN).add("near", rect(1610, 860, 1640, 950), BROWN).add("near", rect(1820, 860, 1850, 950), BROWN)
    flowers_row(w, "near", 1010, [70, 150, 1950, 2020], [PINK, LAV, CORAL])
    w.zone("soil", "soil", soil).zone("pond", "water", pond).zone("table", "table", rect(1580, 790, 1880, 830)).zone("sky", "sky", rect(0, 0, W, 420)).zone("path", "path", path)
    for i in range(3): w.anchor(f"bed{i}", "plant", 300 + i * 160, 820)
    w.anchor("pond", "water", 1250, 850).anchor("table", "table_item", 1730, 815).anchor("bench", "seat", 1500, 900)
    w.anchor("path_start", "waypoint", 900, 980).anchor("path_end", "waypoint", 1010, 780).anchor("sky", "sky", 1000, 220)
    return w


# -------------------------------------------------------------------- town
def town():
    w = World("town", 0xFFC8E6FF, 0xFFFFF5EA, 780)
    w.add("far", cloud(1700, 150, 1.0), WHITE, False)
    for i, (x, h, c) in enumerate([(100, 300, 0xFFD8D2F2), (300, 380, 0xFFCFE4F7), (560, 260, 0xFFF7D9D2), (1300, 340, 0xFFD8D2F2), (1520, 420, 0xFFCFE4F7), (1780, 300, 0xFFF7D9D2), (1960, 360, 0xFFD8D2F2)]):
        w.add("far", rect(x, 640 - h, x + 180, 660), c, False)
        for k in range(int(h / 80)):
            w.add("far", rect(x + 30, 660 - h + 30 + k * 80, x + 70, 660 - h + 70 + k * 80), 0xFFFFFFFF, False)
            w.add("far", rect(x + 110, 660 - h + 30 + k * 80, x + 150, 660 - h + 70 + k * 80), 0xFFFFFFFF, False)
    # school + shop + construction
    w.add("mid", rect(760, 420, 1180, 700), 0xFFFFE3B8).add("mid", poly([(730, 430), (970, 300), (1210, 430)], 14), RED)
    w.add("mid", circle(970, 380, 34), WHITE).add("mid", rrect(920, 580, 1020, 700, 20), BLUE)
    for x in (800, 1060): w.add("mid", rrect(x, 480, x + 80, 550, 12), SKY)
    w.add("mid", rect(1240, 560, 1460, 700), CREAM)
    for i in range(5): w.add("mid", union(rect(1230 + i * 46, 520, 1276 + i * 46, 560), circle(1253 + i * 46, 560, 23)), CORAL if i % 2 == 0 else WHITE)
    w.add("mid", rect(1520, 400, 1540, 700), ORANGE).add("mid", rect(1520, 400, 1720, 420), ORANGE).add("mid", rect(1700, 420, 1706, 520), DGREY)
    w.add("mid", rrect(1560, 640, 1760, 700, 10), 0xFFFFC94D)
    w.add("near", rect(-40, 760, W + 40, H), 0xFFD8DCE3)
    road = rect(-40, 830, W + 40, 960); w.add("near", road, 0xFF8C95A6)
    for i in range(12): w.add("near", rrect(30 + i * 180, 888, 120 + i * 180, 902, 6), WHITE, False)
    park = ellipse(330, 790, 300, 50); w.add("near", park, LEAF)
    t, c = tree(230, 790, 0.9); w.add("near", t, BROWN).add("near", c, GREEN)
    w.add("near", rrect(330, 740, 520, 760, 8), ORANGE)
    w.zone("road", "path", road).zone("park", "park", park).zone("stall", "table", rect(1240, 540, 1460, 580)).zone("site", "build", rect(1520, 600, 1780, 760)).zone("sky", "sky", rect(0, 0, W, 300))
    w.anchor("stop_a", "waypoint", 200, 895).anchor("stop_b", "waypoint", 1850, 895).anchor("school_door", "waypoint", 970, 700)
    w.anchor("stall", "table_item", 1350, 560).anchor("park_bench", "seat", 425, 750).anchor("site", "carry", 1660, 700).anchor("sky", "sky", 1000, 160)
    return w


# ------------------------------------------------------------------- ocean
def ocean():
    w = World("ocean", 0xFF7CCFF0, 0xFF2F86C8, 900)
    w.add("far", band(140, 10, 14, 0.2, H), 0xFF5DB7E6, False)
    for i in range(8): w.add("far", ellipse(150 + i * 260, 300 + (i % 3) * 120, 90, 16, 0), 0xFF8CD3F2, False)
    w.add("far", hills(860, 120, 6, 2.0), 0xFF3F8FC4, False)
    w.add("mid", hills(900, 90, 7, 0.7), 0xFFE9D3A6)
    for x, h in ((250, 220), (420, 160), (1700, 240), (1880, 180)):
        w.add("mid", thick(curve([(x, 900), (x - 30, 900 - h / 2), (x + 20, 900 - h)]), 26), 0xFF5CC08C)
    # dock
    w.add("mid", rect(1500, 120, 2100, 160), TAN).add("mid", rect(1560, 160, 1590, 420), BROWN).add("mid", rect(1800, 160, 1830, 420), BROWN)
    reef = union(circle(980, 900, 120), circle(1120, 880, 90), circle(860, 920, 80))
    w.add("near", band(950, 10, 10, 1.4), SAND)
    w.add("near", reef, CORAL)
    for i, (x, y) in enumerate([(900, 840), (1000, 800), (1110, 820)]): w.add("near", circle(x, y, 22), PINK)
    w.add("near", star(1500, 980, 40, 18, 5, round_r=6), ORANGE)
    w.add("near", ellipse(400, 990, 60, 22), WHITE)
    water = rect(0, 160, W, 940)
    w.zone("water", "water", water).zone("reef", "reef", reef).zone("dock", "dock", rect(1500, 100, 2048, 180)).zone("floor", "floor", rect(0, 930, W, H))
    w.anchor("reef", "decor", 990, 860).anchor("dock", "seat", 1700, 120).anchor("wp1", "waypoint", 400, 400).anchor("wp2", "waypoint", 1000, 600)
    w.anchor("wp3", "waypoint", 1600, 500).anchor("floor", "plant", 1400, 960)
    return w


# ---------------------------------------------------------------- dinosaur
def dinosaur():
    w = World("dinosaur", 0xFFFFE2B8, 0xFFFFF6E3, 780)
    w.add("far", circle(1650, 230, 110), 0xFFFFD27A, False)
    w.add("far", poly([(-40, 620), (220, 260), (420, 520), (640, 300), (900, 620)], 30), 0xFFE7C3A3, False)
    w.add("far", poly([(1100, 640), (1400, 330), (1620, 560), (1820, 380), (2090, 640)], 30), 0xFFE0B892, False)
    w.add("far", ellipse(220, 270, 50, 20), WHITE, False)
    w.add("mid", hills(700, 80, 6, 2.2), 0xFFB9DB8A)
    for x in (140, 520, 1880):
        w.add("mid", rrect(x - 14, 520, x + 14, 700, 8), BROWN)
        for k in range(5): w.add("mid", rotate(ellipse(x, 500, 22, 90), -70 + k * 35, x, 520), 0xFF6DBB6A)
    cave = diff(ellipse(1650, 760, 220, 200), rect(0, 760, W, H)); w.add("mid", cave, 0xFFBFA28A)
    w.add("mid", diff(ellipse(1650, 760, 110, 120), rect(0, 760, W, H)), 0xFF6C5A50)
    w.add("near", band(770, 12, 10, 0.9), 0xFFA6D67C)
    lake = ellipse(760, 880, 300, 70); w.add("near", lake, SKY)
    bridge = diff(ellipse(760, 860, 340, 120), ellipse(760, 880, 300, 110), rect(0, 860, W, H)); w.add("near", bridge, TAN)
    for i in range(6): w.add("near", ellipse(1150 + i * 70, 960 - (i % 2) * 20, 30, 18), HAIR2)
    w.add("near", ellipse(300, 900, 90, 40), 0xFFB9DB8A)
    w.zone("lake", "water", lake).zone("bridge", "path", bridge).zone("cave", "shelter", ellipse(1650, 720, 110, 80)).zone("meadow", "floor", rect(0, 780, W, H)).zone("sky", "sky", rect(0, 0, W, 330))
    for i in range(3): w.anchor(f"track{i}", "waypoint", 1150 + i * 140, 950 - (i % 2) * 20)
    w.anchor("lake", "water", 760, 880).anchor("bridge_top", "waypoint", 760, 750).anchor("cave", "seat", 1650, 740).anchor("tree", "plant", 300, 880)
    return w


# ------------------------------------------------------------------- space
def space():
    w = World("space", 0xFF26315E, 0xFF4B4C8C, 860)
    for i in range(40):
        x = (i * 397) % W; y = (i * 211) % 620 + 20
        w.add("far", star(x, y, 9 + (i % 3) * 4, 4, 4), 0xFFFFF1B3, False)
    w.add("far", circle(1700, 220, 120), 0xFFB9A3F0, False).add("far", ellipse(1700, 230, 200, 30, -12), 0xFFDDD0FF, False)
    w.add("far", circle(300, 180, 60), 0xFFFFC0A8, False)
    orbit = thick(arc(1024, 420, 640, 160, 0, 359.9), 10)
    w.add("mid", orbit, 0xFF7E86C7, False)
    w.add("mid", rrect(1300, 330, 1500, 400, 30), GREY).add("mid", rect(1230, 350, 1300, 380), SKY).add("mid", rect(1500, 350, 1570, 380), SKY)
    w.add("mid", circle(1400, 365, 26), YELLOW)
    w.add("near", band(860, 18, 8, 0.4), 0xFFC9CFE0)
    for i, (x, y, r) in enumerate([(300, 930, 50), (900, 960, 34), (1500, 920, 60), (1850, 980, 30)]):
        w.add("near", ellipse(x, y, r, r * 0.4), 0xFFAEB5CC)
    pad = rrect(140, 820, 520, 860, 12); w.add("near", pad, ORANGE)
    w.add("near", rect(170, 860, 200, 960), DGREY).add("near", rect(460, 860, 490, 960), DGREY)
    w.zone("pad", "launch", pad).zone("orbit", "sky", ellipse(1024, 420, 640, 160)).zone("ground", "floor", rect(0, 860, W, H)).zone("station", "dock", rect(1230, 320, 1570, 410))
    w.anchor("pad", "seat", 330, 820).anchor("planet", "waypoint", 1700, 360).anchor("station", "waypoint", 1400, 420)
    w.anchor("orbit_l", "waypoint", 400, 420).anchor("orbit_r", "waypoint", 1640, 420).anchor("crater", "plant", 1500, 920)
    return w


# ----------------------------------------------------------------- fantasy
def fantasy():
    w = World("fantasy", 0xFFE5D5FF, 0xFFFFEFF6, 780)
    w.add("far", cloud(300, 200, 1.2), WHITE, False).add("far", cloud(1150, 140, 0.9), WHITE, False)
    w.add("far", hills(560, 160, 5, 3.1), 0xFFD9CBF7, False)
    # castle
    w.add("mid", rect(1400, 380, 1760, 700), 0xFFF7E9FF)
    for x in (1360, 1720):
        w.add("mid", rect(x, 320, x + 80, 700), 0xFFEBD9FF).add("mid", poly([(x - 16, 330), (x + 40, 220), (x + 96, 330)], 10), PINK)
    w.add("mid", poly([(1460, 390), (1580, 260), (1700, 390)], 12), LAV)
    w.add("mid", union(rect(1540, 600, 1620, 700), circle(1580, 600, 40)), PURPLE)
    w.add("mid", hills(700, 60, 6, 0.1), 0xFFBFE6B5)
    # magic tree
    w.add("mid", rrect(380, 480, 430, 700, 20), HAIR2)
    w.add("mid", union(circle(405, 420, 120), circle(320, 470, 70), circle(490, 470, 70)), 0xFFF5A8D0)
    for i, (x, y) in enumerate([(360, 400), (450, 380), (410, 480)]): w.add("mid", star(x, y, 18, 8, 5, round_r=2), YELLOW)
    w.add("near", band(770, 12, 10, 2.5), 0xFFB0E0A0)
    gate = diff(union(rect(860, 700, 1180, 900), ellipse(1020, 700, 160, 120)), union(rect(910, 720, 1130, 900), ellipse(1020, 720, 110, 80)))
    w.add("near", gate, SUN)
    w.add("near", cloud(1900, 900, 0.8), WHITE)
    w.zone("castle", "door", rect(1540, 560, 1620, 700)).zone("gate", "portal", rect(910, 680, 1130, 900)).zone("tree", "plant", rect(330, 300, 480, 700))
    w.zone("clouds", "sky", rect(0, 0, W, 300)).zone("hill", "floor", rect(0, 780, W, H))
    w.anchor("castle_door", "waypoint", 1580, 700).anchor("tower", "seat", 1400, 320).anchor("gate", "waypoint", 1020, 880)
    w.anchor("tree", "plant", 405, 700).anchor("cloud_seat", "seat", 1900, 870).anchor("sky", "sky", 900, 200)
    return w


# ------------------------------------------------------------- celebration
def celebration():
    w = World("celebration", 0xFFFFE9D6, 0xFFFFF8EF, 760)
    w.add("far", rect(-40, 0, W + 40, 620), 0xFFFFEFE2, False)
    for i in range(11):
        x = 40 + i * 190
        w.add("far", poly([(x, 120), (x + 120, 120), (x + 60, 220)], 10), [0xFFFFC2B8, 0xFFFFE6A3, 0xFFBDEAE4][i % 3], False)
    w.add("far", thick(curve([(-40, 110), (1024, 150), (W + 40, 110)]), 6), 0xFFD8B79C, False)
    # stage
    stage = rrect(1360, 560, 1960, 700, 20)
    w.add("mid", rect(1380, 260, 1940, 560), 0xFFFFD7E6).add("mid", stage, 0xFFE8A374)
    w.add("mid", poly([(1360, 260), (1500, 260), (1460, 560), (1360, 560)], 20), CORAL).add("mid", poly([(1960, 260), (1820, 260), (1860, 560), (1960, 560)], 20), CORAL)
    w.add("mid", rect(80, 300, 520, 640), 0xFFFFF3D6)
    for i in range(3): w.add("mid", rrect(120 + i * 140, 360, 220 + i * 140, 460, 20), [SKY, PINK, LEAF][i])
    w.add("near", rect(-40, 740, W + 40, H), 0xFFF4D7B5)
    for i in range(12): w.add("near", rect(i * 180, 740, i * 180 + 90, H), 0xFFEFC9A2, False)
    table = rrect(700, 760, 1260, 820, 24)
    w.add("near", poly([(700, 800), (1260, 800), (1290, 960), (670, 960)], 24), WHITE).add("near", table, CREAM)
    for i in range(4): w.add("near", circle(760 + i * 160, 960, 40), PINK)
    w.zone("table", "table", table).zone("stage", "stage", stage).zone("bunting", "hang", rect(0, 90, W, 230)).zone("family", "seat", rect(80, 640, 560, 900))
    w.anchor("table_a", "table_item", 820, 770).anchor("table_b", "table_item", 980, 770).anchor("table_c", "table_item", 1140, 770)
    w.anchor("stage", "seat", 1660, 560).anchor("hang", "hang", 1024, 150).anchor("family", "seat", 300, 800)
    return w


ALL = [garden, town, ocean, dinosaur, space, fantasy, celebration]
