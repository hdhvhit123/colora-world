from colora_art import *
from pal import *


def _stem(a, x0, y0, x1, y1, w=34, bend=40, color=GREEN):
    a.region("stem", thick([(x0, y0), ((x0 + x1) / 2 + bend, (y0 + y1) / 2), (x1, y1)], w), color)


def _leaf(a, name, base, tip, width, color=LEAF, vein=True):
    bx, by = base; tx, ty = tip
    mx, my = (bx + tx) / 2, (by + ty) / 2
    dx, dy = tx - bx, ty - by
    L = math.hypot(dx, dy); nx, ny = -dy / L, dx / L
    a.region(name, blob([(bx, by), (mx + nx * width, my + ny * width), (tx, ty), (mx - nx * width * 0.6, my - ny * width * 0.6)]), color)
    if vein:
        a.line([(bx + dx * 0.1, by + dy * 0.1), (mx + nx * 6, my + ny * 6), (bx + dx * 0.85, by + dy * 0.85)], 5)


def _pot(a, cx=512, top=780, w=220, color=CORAL):
    a.region("pot", poly([(cx - w / 2, top), (cx + w / 2, top), (cx + w * 0.38, 960), (cx - w * 0.38, 960)], 24), color)
    a.region("pot_rim", rrect(cx - w / 2 - 20, top - 30, cx + w / 2 + 20, top + 30, 20), color)


def sunflower():
    a = Art("flowers.sunflower")
    a.part("stem", pivot=(512, 960))
    _stem(a, 512, 960, 512, 520, 40, 30)
    _leaf(a, "leaf_l", (520, 800), (330, 700), 70)
    _leaf(a, "leaf_r", (520, 700), (720, 620), 64)
    a.part("head", parent="stem", pivot=(512, 520), motion="sway", amp=6)
    for i, p in enumerate(petals(512, 360, 14, 112, 120, 74, 0, "round")):
        a.region(f"petal{i}", p, YELLOW if i % 2 else SUN)
    a.region("center", circle(512, 360, 122), BROWN)
    a.region("center_in", circle(512, 360, 74), HAIR2)
    for ang in range(0, 360, 40):
        a.ink(circle(512 + 98 * math.cos(math.radians(ang)), 360 + 98 * math.sin(math.radians(ang)), 6))
    a.eyes(512, 344, 64, 14); a.smile(512, 380, 50, 0.5, 6)
    a.anchor("soil", "plant", 512, 960)
    return a


def rose():
    a = Art("flowers.rose")
    a.part("stem", pivot=(512, 960))
    _stem(a, 512, 960, 512, 480, 30, -30)
    for i, (x, y) in enumerate([(498, 700), (520, 820)]):
        a.region(f"thorn{i}", poly([(x - 14, y), (x + 14, y), (x + (34 if i else -34), y - 26)], 6), GREEN)
    _leaf(a, "leaf_l", (500, 760), (320, 680), 56)
    _leaf(a, "leaf_r", (520, 640), (700, 590), 50)
    a.part("head", parent="stem", pivot=(512, 480), motion="sway", amp=5)
    a.region("sepal", poly([(430, 470), (512, 430), (594, 470), (512, 520)], 20), GREEN)
    a.region("back_petals", blob([(512, 160), (650, 220), (690, 340), (620, 470), (512, 490), (404, 470), (334, 340), (374, 220)]), RED)
    a.region("petal_l", blob([(380, 250), (470, 300), (500, 420), (430, 470), (360, 400)]), CORAL)
    a.region("petal_r", blob([(644, 250), (554, 300), (524, 420), (594, 470), (664, 400)]), CORAL)
    a.region("petal_front", blob([(420, 380), (512, 340), (604, 380), (590, 450), (512, 480), (434, 450)]), PINK)
    a.region("bud", blob([(512, 210), (580, 250), (586, 320), (512, 360), (440, 320), (446, 250)]), CORAL)
    a.line(curve([(470, 280), (500, 250), (550, 270), (560, 310), (520, 330), (494, 300)]), 6)
    a.anchor("soil", "plant", 512, 960)
    return a


def lotus():
    a = Art("flowers.lotus")
    a.part("pad", pivot=(512, 820))
    a.region("water", ellipse(512, 860, 380, 90), SKY)
    a.line(arc(512, 860, 300, 60, 200, 140), 5)
    a.region("pad_l", diff(ellipse(300, 820, 170, 60), poly([(300, 820), (220, 760), (190, 790)])), GREEN)
    a.region("pad_r", diff(ellipse(740, 840, 140, 50), poly([(740, 840), (820, 790), (850, 815)])), LEAF)
    a.part("flower", pivot=(512, 760), motion="bloom", amp=8)
    back = []
    for ang, s in [(-60, 0.9), (-30, 1.0), (0, 1.05), (30, 1.0), (60, 0.9)]:
        pet = blob([(512, 760), (450, 620), (512, 380), (574, 620)])
        back.append(rotate(scale(pet, s, s, 512, 760), ang, 512, 760))
    for i, p in enumerate(back):
        a.region(f"petal_back{i}", p, PINK)
    for i, ang in enumerate((-35, 35)):
        a.region(f"petal_mid{i}", rotate(blob([(512, 770), (440, 650), (512, 470), (584, 650)]), ang, 512, 770), ROSE)
    a.region("petal_front", blob([(512, 780), (430, 690), (512, 500), (594, 690)]), PINK)
    a.line([(512, 760), (512, 560)], 5)
    a.anchor("water", "plant", 512, 860)
    return a


def tulip():
    a = Art("flowers.tulip")
    a.part("stem", pivot=(512, 960))
    _stem(a, 512, 960, 512, 470, 30, 10)
    a.region("leaf_l", blob([(500, 940), (380, 760), (360, 560), (430, 720), (510, 860)]), LEAF)
    a.region("leaf_r", blob([(524, 940), (650, 800), (690, 620), (610, 760), (516, 880)]), GREEN)
    a.part("head", parent="stem", pivot=(512, 480), motion="sway", amp=7)
    a.region("petal_back", blob([(512, 210), (600, 300), (620, 420), (512, 500), (404, 420), (424, 300)]), CORAL)
    a.region("petal_l", blob([(390, 260), (470, 330), (520, 500), (420, 480), (380, 400)]), RED)
    a.region("petal_r", blob([(634, 260), (554, 330), (504, 500), (604, 480), (644, 400)]), RED)
    a.region("petal_mid", blob([(512, 280), (560, 360), (552, 470), (512, 500), (472, 470), (464, 360)]), CORAL)
    a.anchor("soil", "plant", 512, 960)
    return a


def daisy():
    a = Art("flowers.daisy")
    a.part("stem", pivot=(512, 960))
    _stem(a, 512, 960, 512, 520, 26, -40)
    _leaf(a, "leaf_l", (500, 860), (360, 820), 40)
    _leaf(a, "leaf_r", (520, 760), (660, 720), 38)
    a.part("head", parent="stem", pivot=(512, 520), motion="sway", amp=6)
    for i, p in enumerate(petals(512, 360, 18, 70, 160, 54, 0, "round")):
        a.region(f"petal{i}", p, WHITE if i % 2 else ROSE)
    a.region("center", circle(512, 360, 82), YELLOW)
    a.eyes(512, 346, 56, 12); a.smile(512, 380, 40, 0.5, 5)
    a.cheeks(512, 376, 100, 10)
    a.anchor("soil", "plant", 512, 960)
    return a


def hibiscus():
    a = Art("flowers.hibiscus")
    a.part("branch", pivot=(512, 960))
    a.region("branch", thick([(300, 960), (420, 780), (470, 600)], 30), BROWN)
    _leaf(a, "leaf_l", (420, 780), (220, 700), 70, GREEN)
    _leaf(a, "leaf_r", (440, 860), (640, 900), 60, GREEN)
    a.part("flower", pivot=(512, 430), motion="bloom", amp=6)
    for i in range(5):
        ang = -90 + i * 72
        cx = 512 + 150 * math.cos(math.radians(ang)); cy = 430 + 150 * math.sin(math.radians(ang))
        pet = scallop(cx, cy, 118, 104, 7, 30, ang + 25)
        a.region(f"petal{i}", diff(pet, circle(512, 430, 40)) if False else pet, RED if i % 2 == 0 else CORAL)
    a.region("heart", circle(512, 430, 70), ROSE)
    a.region("stamen", thick([(512, 430), (560, 330), (600, 250)], 18), SUN)
    for x, y in [(600, 240), (620, 266), (578, 226), (626, 222)]:
        a.region(f"pollen{int(x)}{int(y)}", circle(x, y, 16), YELLOW)
    for ang in (0, 72, 144, 216, 288):
        a.line([(512 + 76 * math.cos(math.radians(ang - 90)), 430 + 76 * math.sin(math.radians(ang - 90))), (512 + 130 * math.cos(math.radians(ang - 90)), 430 + 130 * math.sin(math.radians(ang - 90)))], 5)
    a.anchor("soil", "plant", 300, 960)
    return a


def lily():
    a = Art("flowers.lily")
    a.part("stem", pivot=(512, 960))
    _stem(a, 512, 960, 512, 520, 26, 20)
    a.region("leaf_l", blob([(510, 900), (360, 820), (300, 700), (420, 790)]), GREEN)
    a.region("leaf_r", blob([(516, 780), (660, 720), (720, 600), (610, 690)]), LEAF)
    a.part("head", parent="stem", pivot=(512, 520), motion="sway", amp=6)
    for i in range(6):
        ang = -90 + i * 60 + 30
        pet = blob([(512, 420), (470, 330), (512, 160), (554, 330)])
        a.region(f"petal{i}", rotate(pet, ang + 90, 512, 420), WHITE if i % 2 else PEACH)
    a.region("throat", circle(512, 420, 44), SUN)
    for k, ang in enumerate((-120, -90, -60)):
        ex = 512 + 110 * math.cos(math.radians(ang)); ey = 420 + 110 * math.sin(math.radians(ang))
        a.line([(512, 420), (ex, ey)], 5)
        a.region(f"anther{k}", ellipse(ex, ey, 16, 10, ang), ORANGE)
    a.anchor("soil", "plant", 512, 960)
    return a


def orchid():
    a = Art("flowers.orchid")
    a.part("pot", pivot=(512, 960))
    a.region("leaf_l", blob([(500, 800), (300, 760), (220, 700), (360, 720)]), GREEN)
    a.region("leaf_r", blob([(524, 800), (740, 770), (820, 700), (680, 720)]), GREEN)
    _pot(a, 512, 800, 240, TURQ)
    a.region("spike", thick([(512, 780), (500, 560), (560, 340), (680, 250)], 18), OLIVE)
    a.part("flower", parent="pot", pivot=(560, 380), motion="bloom", amp=6)
    cx, cy = 470, 380
    a.region("sepal_t", blob([(cx, cy), (cx - 50, cy - 90), (cx, cy - 160), (cx + 50, cy - 90)]), LAV)
    a.region("sepal_l", rotate(blob([(cx, cy), (cx - 46, cy - 80), (cx, cy - 150), (cx + 46, cy - 80)]), -130, cx, cy), LAV)
    a.region("sepal_r", rotate(blob([(cx, cy), (cx - 46, cy - 80), (cx, cy - 150), (cx + 46, cy - 80)]), 130, cx, cy), LAV)
    a.region("petal_l", circle(cx - 100, cy - 10, 80), PINK)
    a.region("petal_r", circle(cx + 100, cy - 10, 80), PINK)
    a.region("lip", blob([(cx, cy + 10), (cx + 50, cy + 50), (cx + 30, cy + 110), (cx, cy + 130), (cx - 30, cy + 110), (cx - 50, cy + 50)]), PURPLE)
    a.region("column", ellipse(cx, cy, 26, 22), SUN)
    a.region("bud1", ellipse(660, 270, 34, 28, -30), LAV)
    a.region("bud2", ellipse(720, 250, 24, 20, -20), LAV)
    a.anchor("table", "table_item", 512, 960)
    return a


def lavender():
    a = Art("flowers.lavender")
    a.part("bunch", pivot=(512, 900), motion="sway", amp=4)
    for i, (dx, top, rot) in enumerate([(-140, 260, -16), (-60, 200, -6), (20, 180, 3), (100, 220, 12), (170, 300, 20)]):
        bx = 512 + dx * 0.3; by = 760
        tx = 512 + dx; ty = top
        a.region(f"stalk{i}", thick([(bx, by), (tx, ty + 240)], 16), GREEN)
        buds = []
        for k in range(6):
            t = k / 6
            x = tx + (bx - tx) * t * 0.35; y = ty + t * 240
            buds.append(ellipse(x - 14, y, 22, 28, rot)); buds.append(ellipse(x + 14, y + 14, 22, 28, rot))
        a.region(f"buds{i}", union(*buds), PURPLE if i % 2 else LAV)
    a.region("stems", poly([(470, 740), (560, 740), (590, 930), (440, 930)], 20), GREEN)
    a.region("ribbon", rrect(440, 740, 584, 800, 22), PINK)
    a.region("bow_l", blob([(500, 770), (400, 720), (380, 800), (440, 820)]), PINK)
    a.region("bow_r", blob([(524, 770), (624, 720), (644, 800), (584, 820)]), PINK)
    a.region("knot", circle(512, 770, 30), CORAL)
    a.anchor("hold", "carry", 512, 770)
    return a


def daffodil():
    a = Art("flowers.daffodil")
    a.part("stem", pivot=(512, 960))
    _stem(a, 512, 960, 512, 520, 28, -20)
    a.region("leaf_l", blob([(500, 960), (400, 760), (410, 560), (450, 760), (510, 920)]), GREEN)
    a.region("leaf_r", blob([(524, 960), (640, 760), (660, 600), (600, 770), (520, 920)]), LEAF)
    a.part("head", parent="stem", pivot=(512, 520), motion="sway", amp=6)
    for i in range(6):
        pet = blob([(512, 400), (450, 330), (512, 210), (574, 330)])
        a.region(f"petal{i}", rotate(pet, i * 60, 512, 400), YELLOW)
    a.region("trumpet", union(circle(512, 400, 78), ellipse(512, 400, 96, 90)), ORANGE)
    a.region("trumpet_in", circle(512, 400, 50), SUN)
    a.line(scallop(512, 400, 80, 76, 12, 16), 0.01) if False else None
    for ang in range(0, 360, 30):
        a.line([(512 + 52 * math.cos(math.radians(ang)), 400 + 52 * math.sin(math.radians(ang))), (512 + 84 * math.cos(math.radians(ang)), 400 + 80 * math.sin(math.radians(ang)))], 4)
    a.anchor("soil", "plant", 512, 960)
    return a


def marigold():
    a = Art("flowers.marigold")
    a.part("stem", pivot=(512, 960))
    _stem(a, 512, 960, 512, 560, 30, 30)
    for i, (base, tip) in enumerate([((520, 820), (330, 760)), ((516, 700), (700, 650))]):
        bx, by = base; tx, ty = tip
        lobes = [ellipse(bx + (tx - bx) * t, by + (ty - by) * t, 40 * (1.1 - t * 0.5), 26, math.degrees(math.atan2(ty - by, tx - bx))) for t in (0.25, 0.5, 0.75, 0.95)]
        a.region(f"leaf{i}", union(*lobes), GREEN)
    a.part("head", parent="stem", pivot=(512, 560), motion="sway", amp=5)
    a.region("ruffle_out", scallop(512, 380, 190, 170, 16, 46), ORANGE)
    a.region("ruffle_mid", scallop(512, 380, 130, 116, 12, 40, 15), SUN)
    a.region("ruffle_in", scallop(512, 380, 66, 60, 9, 30, 8), YELLOW)
    a.region("core", circle(512, 380, 34), ORANGE)
    a.anchor("soil", "plant", 512, 960)
    return a


def hydrangea():
    a = Art("flowers.hydrangea")
    a.part("stem", pivot=(512, 960))
    _stem(a, 512, 960, 512, 600, 34, 0)
    _leaf(a, "leaf_l", (510, 760), (270, 720), 90, GREEN)
    _leaf(a, "leaf_r", (514, 700), (760, 660), 84, GREEN)
    a.part("head", parent="stem", pivot=(512, 600), motion="sway", amp=4)
    a.region("ball", circle(512, 400, 240), LAV)
    centers = [(512, 400)]
    for ring, (r, n) in enumerate([(110, 7), (196, 12)]):
        for k in range(n):
            t = math.radians(k * 360 / n + ring * 15)
            centers.append((512 + r * math.cos(t), 400 + r * math.sin(t) * 0.95))
    cols = [SKY, LAV, PINK, BLUE]
    for i, (x, y) in enumerate(centers):
        fl = union(*[circle(x + 26 * math.cos(math.radians(45 + 90 * k)), y + 26 * math.sin(math.radians(45 + 90 * k)), 26) for k in range(4)])
        a.region(f"floret{i}", fl, cols[i % 4])
        a.ink(circle(x, y, 5))
    a.anchor("soil", "plant", 512, 960)
    return a


ALL = [sunflower, rose, lotus, tulip, daisy, hibiscus, lily, orchid, lavender, daffodil, marigold, hydrangea]
