"""21 base props (spec §10). Pre-coloured play pieces drawn in the same
picture-book style as templates; they are not counted among the 168 templates
and are never presented as finished child artwork."""
from colora_art import *
from pal import *

PROPS = {}


def prop(pid, caps):
    def deco(fn):
        PROPS[pid] = (fn, caps)
        return fn
    return deco


@prop("prop.soil_block", ["carryable", "soil"])
def soil_block():
    a = Art("prop.soil_block")
    a.part("block", pivot=(512, 760))
    a.region("side", poly([(300, 560), (724, 560), (700, 800), (324, 800)], 26), BROWN)
    a.region("top", union(scallop(512, 560, 212, 46, 9, 22), rrect(300, 530, 724, 590, 26)), HAIR2)
    for i, (x, y) in enumerate([(390, 650), (520, 700), (630, 640), (450, 750), (600, 740)]):
        a.region(f"pebble{i}", ellipse(x, y, 22, 15, i * 30), TAN)
    a.region("sprout", union(thick([(512, 540), (512, 450)], 16), ellipse(470, 440, 44, 22, -30), ellipse(556, 430, 44, 22, 30)), LEAF)
    a.anchor("plant", "plant", 512, 540); a.anchor("grab", "carry", 512, 560)
    return a


@prop("prop.watering_can", ["tool_water", "carryable"])
def watering_can():
    a = Art("prop.watering_can")
    a.part("can", pivot=(470, 720), motion="tilt", amp=30)
    a.region("spout", poly([(600, 640), (800, 470), (840, 500), (650, 700)], 16), TURQ)
    a.region("rose", rrect(790, 430, 880, 510, 26, -40), TURQ)
    a.region("body", rrect(280, 520, 640, 800, 70), SKY)
    a.region("handle", diff(ellipse(400, 520, 140, 110), ellipse(400, 520, 98, 72), rect(0, 560, 1024, 1024)), DEEPBLUE)
    a.region("band", rect(280, 640, 640, 690), SUN)
    a.region("drop", union(circle(460, 600, 28), poly([(436, 590), (460, 540), (484, 590)])), WHITE)
    a.anchor("pour", "water", 860, 470); a.anchor("grab", "carry", 400, 440)
    return a


@prop("prop.plant_pot", ["carryable", "plant_target"])
def plant_pot():
    a = Art("prop.plant_pot")
    a.part("pot", pivot=(512, 850))
    a.region("pot", poly([(350, 560), (674, 560), (630, 860), (394, 860)], 30), ORANGE)
    a.region("rim", rrect(320, 520, 704, 600, 34), CORAL)
    a.region("soil", ellipse(512, 528, 168, 26), BROWN)
    a.region("heart", heart(512, 720, 90), PINK)
    a.anchor("plant", "plant", 512, 520); a.anchor("grab", "carry", 512, 560)
    return a


@prop("prop.bouquet_wrap", ["bouquet", "carryable"])
def bouquet_wrap():
    a = Art("prop.bouquet_wrap")
    a.part("wrap", pivot=(512, 880))
    a.region("paper_back", poly([(300, 380), (724, 380), (540, 900), (484, 900)], 30), CREAM)
    a.region("paper_l", poly([(280, 420), (520, 520), (500, 900)], 30), PINK)
    a.region("paper_r", poly([(744, 420), (504, 520), (524, 900)], 30), LAV)
    a.region("bow", union(ellipse(450, 700, 70, 40, -25), ellipse(574, 700, 70, 40, 25)), RED)
    a.region("knot", circle(512, 712, 30), CORAL)
    for i, x in enumerate((420, 512, 604)):
        a.anchor(f"stem{i}", "bouquet", x, 420)
    a.anchor("grab", "carry", 512, 760)
    return a


@prop("prop.footprint", ["waypoint", "trail"])
def footprint():
    a = Art("prop.footprint")
    a.part("print", pivot=(512, 600))
    a.region("pad", blob([(420, 560), (512, 500), (610, 560), (630, 690), (512, 760), (400, 690)]), SAND)
    for i, (x, y) in enumerate([(380, 440), (470, 380), (570, 380), (650, 440)]):
        a.region(f"toe{i}", ellipse(x, y, 40, 52, (i - 1.5) * 12), TAN)
    a.anchor("step", "waypoint", 512, 620)
    return a


@prop("prop.stall", ["surface", "decor"])
def stall():
    a = Art("prop.stall")
    a.part("stall", pivot=(512, 900))
    a.region("post_l", rect(260, 330, 300, 900), BROWN); a.region("post_r", rect(724, 330, 764, 900), BROWN)
    a.region("counter", rrect(200, 600, 824, 700, 20), TAN)
    a.region("front", rrect(220, 700, 804, 900, 16), CREAM)
    for i in range(6):
        x0 = 200 + i * 104
        a.region(f"awning{i}", union(rect(x0, 250, x0 + 104, 330), circle(x0 + 52, 330, 52)), RED if i % 2 == 0 else WHITE)
    a.region("sign", rrect(380, 760, 644, 850, 30), SUN)
    for i, x in enumerate((330, 512, 694)):
        a.anchor(f"shelf{i}", "table_item", x, 600)
    return a


@prop("prop.basket", ["carryable", "surface"])
def basket():
    a = Art("prop.basket")
    a.part("basket", pivot=(512, 820))
    a.region("handle", diff(ellipse(512, 560, 230, 250), ellipse(512, 560, 196, 216), rect(0, 560, 1024, 1024)), BROWN)
    a.region("body", poly([(270, 560), (754, 560), (700, 840), (324, 840)], 40), TAN)
    for i in range(3):
        a.region(f"weave{i}", rect(300 + i * 4, 620 + i * 70, 724 - i * 4, 650 + i * 70), SAND)
    a.region("cloth", poly([(300, 540), (724, 540), (660, 610), (512, 580), (364, 610)], 16), PINK)
    for i, x in enumerate((420, 600)):
        a.anchor(f"in{i}", "table_item", x, 550)
    a.anchor("grab", "carry", 512, 320)
    return a


@prop("prop.lamp", ["light", "decor", "carryable"])
def lamp():
    a = Art("prop.lamp")
    a.part("post", pivot=(512, 930))
    a.region("base", rrect(410, 870, 614, 930, 24), NAVYL)
    a.region("pole", rect(494, 380, 530, 880), DGREY)
    a.part("head", parent="post", pivot=(512, 380), motion="glow", amp=1)
    a.region("glow", circle(512, 280, 120), SUN)
    a.region("cage", union(poly([(420, 330), (604, 330), (570, 380), (454, 380)], 10), poly([(430, 180), (594, 180), (620, 210), (404, 210)], 10)), DEEPBLUE)
    a.region("bulb", ellipse(512, 270, 62, 66), YELLOW)
    a.anchor("light", "light", 512, 270)
    return a


@prop("prop.path", ["path", "decor"])
def path():
    a = Art("prop.path")
    a.part("path", pivot=(512, 700))
    a.region("ground", blob([(120, 640), (512, 560), (904, 640), (920, 760), (512, 820), (104, 760)]), LEAF)
    for i in range(5):
        x = 200 + i * 156
        a.region(f"stone{i}", blob([(x - 60, 690), (x, 640 + (i % 2) * 14), (x + 60, 690), (x + 48, 740), (x - 48, 742)]), GREY if i % 2 else SAND)
    for i in range(2):
        a.anchor(f"end{i}", "waypoint", 200 + i * 624, 690)
    return a


@prop("prop.book", ["carryable", "readable", "table_item"])
def book():
    a = Art("prop.book")
    a.part("book", pivot=(512, 720), motion="flap", amp=6)
    a.region("cover", rrect(240, 360, 784, 760, 40), BLUE)
    a.region("page_l", poly([(270, 390), (506, 420), (506, 730), (270, 700)], 16), CREAM)
    a.region("page_r", poly([(754, 390), (518, 420), (518, 730), (754, 700)], 16), WHITE)
    a.region("star", star(640, 540, 70, 32, 5, round_r=6), SUN)
    a.region("sun", circle(390, 520, 54), ORANGE)
    a.line([(320, 620), (470, 630)], 8); a.line([(320, 660), (460, 668)], 8)
    a.anchor("grab", "carry", 512, 560)
    return a


@prop("prop.waypoint", ["waypoint"])
def waypoint():
    a = Art("prop.waypoint")
    a.part("flag", pivot=(512, 900), motion="sway", amp=6)
    a.region("ring", diff(ellipse(512, 880, 170, 52), ellipse(512, 880, 110, 30)), SUN)
    a.region("pole", rect(500, 380, 524, 880), DGREY)
    a.region("flag", poly([(524, 380), (760, 450), (524, 540)], 20), CORAL)
    a.region("dot", circle(600, 455, 26), WHITE)
    a.anchor("point", "waypoint", 512, 880)
    return a


@prop("prop.gift_toy", ["carryable", "openable", "table_item"])
def gift_toy():
    a = Art("prop.gift_toy")
    a.part("box", pivot=(512, 860))
    a.region("box", rrect(320, 520, 704, 860, 26), TURQ)
    a.region("ribbon_v", rect(482, 520, 542, 860), SUN)
    a.part("lid", parent="box", pivot=(512, 500), motion="pop", amp=60)
    a.region("lid", rrect(290, 440, 734, 530, 26), MINT)
    a.region("ribbon_l", rect(482, 440, 542, 530), SUN)
    a.region("bow", union(ellipse(450, 400, 70, 44, -20), ellipse(574, 400, 70, 44, 20)), CORAL)
    a.region("knot", circle(512, 420, 32), RED)
    a.anchor("grab", "carry", 512, 520)
    return a


@prop("prop.picnic_mat", ["surface", "seat_furniture"])
def picnic_mat():
    a = Art("prop.picnic_mat")
    a.part("mat", pivot=(512, 700))
    mat = poly([(170, 600), (854, 600), (954, 840), (70, 840)], 24)
    a.region("mat", mat, WHITE)
    for i in range(4):
        x0 = 120 + i * 200
        a.region(f"check{i}", inter(mat, poly([(x0 + 60, 600), (x0 + 160, 600), (x0 + 150 + (i - 1.5) * 30, 840), (x0 + 40 + (i - 1.5) * 30, 840)])), RED)
    a.region("band", inter(mat, rect(0, 690, 1024, 750)), CORAL)
    for i, x in enumerate((340, 512, 684)):
        a.anchor(f"dish{i}", "table_item", x, 700)
    a.anchor("seat_l", "seat", 200, 760); a.anchor("seat_r", "seat", 824, 760)
    return a


@prop("prop.bridge", ["bridge", "decor"])
def bridge():
    a = Art("prop.bridge")
    a.part("bridge", pivot=(512, 760))
    a.region("arch", diff(ellipse(512, 760, 420, 200), ellipse(512, 800, 330, 160), rect(0, 760, 1024, 1024)), TAN)
    a.region("deck", diff(ellipse(512, 760, 420, 210), ellipse(512, 760, 420, 170), rect(0, 760, 1024, 1024)), BROWN)
    for i in range(7):
        x = 190 + i * 107
        y = 760 - 200 * (1 - ((x - 512) / 420) ** 2) ** 0.5
        a.region(f"post{i}", rrect(x - 14, y - 100, x + 14, y, 10), HAIR2)
    a.region("rail", thick(curve([(150, 640), (512, 460), (874, 640)]), 22), ORANGE)
    a.anchor("start", "waypoint", 130, 760); a.anchor("top", "waypoint", 512, 560); a.anchor("end", "waypoint", 894, 760)
    return a


@prop("prop.exploration_window", ["rideable", "aquatic", "carrier"])
def exploration_window():
    a = Art("prop.exploration_window")
    a.part("bell", pivot=(512, 560), motion="bob", amp=10)
    a.region("hull", circle(512, 540, 300), SUN)
    a.region("window", circle(512, 520, 190), SKY)
    a.region("glint", ellipse(450, 440, 60, 30, -30), WHITE)
    for i in range(8):
        ang = i * 45
        a.region(f"bolt{i}", rotate(circle(512, 300, 18), ang, 512, 520), ORANGE)
    a.region("fin_l", poly([(230, 700), (150, 860), (300, 800)], 20), CORAL)
    a.region("fin_r", poly([(794, 700), (874, 860), (724, 800)], 20), CORAL)
    a.region("lamp", rrect(472, 210, 552, 250, 16), YELLOW)
    a.anchor("seat", "passenger", 512, 600)
    return a


@prop("prop.leaf", ["carryable"])
def leaf():
    a = Art("prop.leaf")
    a.part("leaf", pivot=(512, 840), motion="sway", amp=8)
    shape = blob([(512, 180), (680, 360), (700, 560), (560, 760), (512, 820), (464, 760), (324, 560), (344, 360)])
    a.region("left", inter(shape, rect(0, 0, 512, 1024)), GREEN)
    a.region("right", inter(shape, rect(512, 0, 1024, 1024)), LEAF)
    a.region("stem", thick([(512, 780), (512, 900)], 22), BROWN)
    for y in (380, 500, 620):
        a.line([(512, y + 40), (430, y - 20)], 7); a.line([(512, y + 40), (594, y - 20)], 7)
    a.line([(512, 260), (512, 800)], 8)
    a.anchor("grab", "carry", 512, 520)
    return a


@prop("prop.chair", ["seat_furniture", "carryable"])
def chair():
    a = Art("prop.chair")
    a.part("chair", pivot=(512, 900))
    a.region("back", rrect(360, 260, 664, 560, 50), CORAL)
    a.region("back_heart", heart(512, 410, 80), PINK)
    a.region("leg_l", rect(370, 600, 410, 900), BROWN); a.region("leg_r", rect(614, 600, 654, 900), BROWN)
    a.region("seat", rrect(330, 560, 694, 630, 26), ORANGE)
    a.anchor("seat", "seat", 512, 560)
    return a


@prop("prop.table", ["surface", "carryable"])
def table():
    a = Art("prop.table")
    a.part("table", pivot=(512, 900))
    a.region("leg_l", rect(270, 560, 316, 900), BROWN); a.region("leg_r", rect(708, 560, 754, 900), BROWN)
    a.region("cloth", poly([(180, 480), (844, 480), (874, 640), (150, 640)], 24), TURQ)
    a.region("top", rrect(170, 450, 854, 510, 26), TAN)
    for i in range(5):
        a.region(f"scallop{i}", circle(220 + i * 146, 650, 40), MINT)
    for i, x in enumerate((300, 512, 724)):
        a.anchor(f"item{i}", "table_item", x, 450)
    return a


@prop("prop.house", ["decor", "door", "light"])
def house():
    a = Art("prop.house")
    a.part("house", pivot=(512, 900))
    a.region("walls", rect(260, 470, 764, 900), CREAM)
    a.region("roof", poly([(200, 500), (512, 200), (824, 500)], 30), RED)
    a.region("chimney", rect(640, 250, 700, 380), BROWN)
    a.region("window_l", rrect(310, 560, 430, 680, 20), SKY)
    a.region("window_r", rrect(594, 560, 714, 680, 20), SKY)
    a.part("door", parent="house", pivot=(462, 900), motion="swing", amp=40)
    a.region("door", union(rect(452, 720, 572, 900), circle(512, 720, 60)), BLUE)
    a.region("knob", circle(548, 820, 12), SUN)
    a.anchor("door", "waypoint", 512, 900); a.anchor("light", "light", 370, 620)
    return a


@prop("prop.park", ["seat_furniture", "decor"])
def park():
    a = Art("prop.park")
    a.part("park", pivot=(512, 900))
    a.region("lawn", blob([(80, 820), (512, 760), (944, 820), (930, 920), (512, 950), (94, 920)]), LEAF)
    a.region("trunk", rect(220, 520, 260, 820), BROWN)
    a.region("crown", union(circle(240, 440, 110), circle(170, 520, 70), circle(310, 520, 70)), GREEN)
    a.region("bench_back", rrect(470, 620, 860, 690, 20), ORANGE)
    a.region("bench_seat", rrect(450, 720, 880, 770, 20), CORAL)
    a.region("bench_leg_l", rect(490, 770, 520, 860), DGREY); a.region("bench_leg_r", rect(810, 770, 840, 860), DGREY)
    a.anchor("seat_l", "seat", 580, 720); a.anchor("seat_r", "seat", 760, 720)
    return a


@prop("prop.food_plate", ["table_item", "carryable"])
def food_plate():
    a = Art("prop.food_plate")
    a.part("plate", pivot=(512, 640))
    a.region("plate", ellipse(512, 620, 330, 130), WHITE)
    a.region("rim", diff(ellipse(512, 620, 330, 130), ellipse(512, 610, 260, 96)), SKY)
    a.region("rice", union(circle(440, 560, 70), circle(510, 540, 70), circle(470, 600, 60)), CREAM)
    a.region("egg", ellipse(610, 590, 70, 48), WHITE)
    a.region("yolk", circle(610, 586, 26), SUN)
    a.region("greens", union(ellipse(560, 650, 40, 20, 20), ellipse(390, 640, 36, 18, -20)), LEAF)
    a.anchor("grab", "carry", 512, 560)
    return a


ALL = [(pid, fn, caps) for pid, (fn, caps) in PROPS.items()]
assert len(ALL) == 21, len(ALL)
