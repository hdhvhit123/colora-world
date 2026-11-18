"""Character Builder parts (spec §12.1): 6 bodies + 8 ears + 8 tails + 6 wings
+ 8 hats + 8 decor = 44 original parts. Every part is a colourable vector piece
with its own regions. Parts are drawn in a local frame whose socket point is
(512, 512); paired parts (ears, wings) are drawn for the LEFT side and mirrored.
Bodies declare socket positions, scale and z-order (behind/front)."""
from colora_art import *
from pal import *

O = 512  # local socket origin

# --------------------------------------------------------------------- bodies
BODIES = []


def body(bid, sockets):
    def deco(fn):
        BODIES.append((f"char.body.{bid}", fn, sockets)); return fn
    return deco


def face(a, cx, cy, gap=120, r=22):
    a.eyes(cx, cy, gap, r); a.smile(cx, cy + 60, gap * 0.45); a.cheeks(cx, cy + 50, gap * 1.7, r * 0.9)


def std_sockets(head_top, ear_x, ear_y, neck, tail, wing, s=1.0):
    return {
        "ear_l": {"x": O - ear_x, "y": ear_y, "scale": s, "z": "behind", "rot": -12},
        "ear_r": {"x": O + ear_x, "y": ear_y, "scale": s, "z": "behind", "rot": 12, "mirror": True},
        "hat": {"x": O, "y": head_top, "scale": s, "z": "front", "rot": 0},
        "decor": {"x": O, "y": neck, "scale": s, "z": "front", "rot": 0},
        "tail": {"x": tail[0], "y": tail[1], "scale": s, "z": "behind", "rot": 0},
        "wing_l": {"x": O - wing[0], "y": wing[1], "scale": s, "z": "behind", "rot": 0},
        "wing_r": {"x": O + wing[0], "y": wing[1], "scale": s, "z": "behind", "rot": 0, "mirror": True},
    }


@body("bean", std_sockets(250, 150, 300, 540, (700, 760), (170, 600)))
def bean():
    a = Art("char.body.bean"); a.part("body", pivot=(512, 900), motion="bounce", amp=10)
    a.region("feet", union(ellipse(420, 900, 80, 40), ellipse(604, 900, 80, 40)), CORAL)
    a.region("body", blob([(512, 240), (700, 330), (740, 620), (660, 880), (364, 880), (284, 620), (324, 330)]), SKY)
    a.region("belly", ellipse(512, 700, 150, 140), WHITE)
    face(a, 512, 420); return a


@body("pear", std_sockets(220, 120, 270, 500, (760, 800), (210, 620), 0.9))
def pear():
    a = Art("char.body.pear"); a.part("body", pivot=(512, 900), motion="wobble", amp=6)
    a.region("feet", union(ellipse(400, 910, 90, 40), ellipse(624, 910, 90, 40)), BROWN)
    a.region("body", blob([(512, 220), (640, 300), (660, 460), (800, 720), (700, 900), (324, 900), (224, 720), (364, 460), (384, 300)]), LEAF)
    a.region("belly", ellipse(512, 740, 190, 130), CREAM)
    a.region("arm_l", ellipse(260, 640, 50, 90, 30), GREEN); a.region("arm_r", ellipse(764, 640, 50, 90, -30), GREEN)
    face(a, 512, 370, 110, 20); return a


@body("tall", std_sockets(150, 110, 200, 430, (650, 820), (200, 560), 0.85))
def tall():
    a = Art("char.body.tall"); a.part("body", pivot=(512, 920), motion="sway", amp=5)
    a.region("legs", union(rrect(420, 760, 480, 920, 26), rrect(544, 760, 604, 920, 26)), NAVYL)
    a.region("torso", rrect(380, 400, 644, 800, 100), ORANGE)
    a.region("head", ellipse(512, 290, 170, 150), SUN)
    a.region("arm_l", rrect(320, 450, 380, 700, 30, 12), ORANGE); a.region("arm_r", rrect(644, 450, 704, 700, 30, -12), ORANGE)
    a.region("button", circle(512, 560, 30), CORAL)
    face(a, 512, 280, 110, 18); return a


@body("boxy", std_sockets(230, 170, 280, 520, (720, 780), (190, 620)))
def boxy():
    a = Art("char.body.boxy"); a.part("body", pivot=(512, 900), motion="bob", amp=8)
    a.region("wheels", union(circle(400, 890, 50), circle(624, 890, 50)), DGREY)
    a.region("body", rrect(300, 520, 724, 870, 50), TURQ)
    a.region("head", rrect(320, 230, 704, 520, 70), MINT)
    a.region("screen", rrect(380, 290, 644, 470, 40), WHITE)
    a.region("panel", rrect(410, 620, 614, 760, 26), SUN)
    a.region("knob_l", circle(460, 690, 24), CORAL); a.region("knob_r", circle(564, 690, 24), SKY)
    face(a, 512, 360, 120, 20); return a


@body("star", std_sockets(130, 150, 300, 630, (690, 820), (160, 560), 0.9))
def starbody():
    a = Art("char.body.star"); a.part("body", pivot=(512, 900), motion="spin", amp=8)
    a.region("body", star(512, 540, 400, 220, 5, round_r=70), YELLOW)
    a.region("belly", circle(512, 600, 130), SUN)
    face(a, 512, 470, 120, 20); return a


@body("fluffy", std_sockets(250, 180, 330, 650, (740, 780), (170, 640)))
def fluffy():
    a = Art("char.body.fluffy"); a.part("body", pivot=(512, 900), motion="bounce", amp=12)
    a.region("feet", union(ellipse(420, 900, 70, 36), ellipse(604, 900, 70, 36)), PINK)
    a.region("body", scallop(512, 590, 280, 320, 14, 34), LAV)
    a.region("face", ellipse(512, 480, 180, 140), WHITE)
    face(a, 512, 470, 110, 20); return a


# ---------------------------------------------------------------------- parts
PARTS = []


def part(kind, pid, motion=None):
    def deco(fn):
        PARTS.append((f"char.{kind}.{pid}", fn, kind, motion)); return fn
    return deco


def P_(pid, motion=None, amp=0):
    a = Art(pid); a.part("part", pivot=(O, O), motion=motion, amp=amp); return a

# ears (left ear; socket = base of ear, ear points up)
@part("ear", "cat")
def _():
    a = P_("char.ear.cat", "twitch", 8); a.region("ear", poly([(400, 540), (470, 300), (600, 540)], 30), ORANGE)
    a.region("inner", poly([(450, 520), (480, 380), (550, 520)], 16), PINK); return a

@part("ear", "bunny")
def _():
    a = P_("char.ear.bunny", "flop", 10); a.region("ear", ellipse(512, 330, 70, 210, -8), WHITE)
    a.region("inner", ellipse(512, 350, 32, 150, -8), PINK); return a

@part("ear", "bear")
def _():
    a = P_("char.ear.bear", "twitch", 4); a.region("ear", circle(512, 450, 110), BROWN); a.region("inner", circle(520, 460, 56), TAN); return a

@part("ear", "floppy")
def _():
    a = P_("char.ear.floppy", "flop", 12); a.region("ear", blob([(470, 480), (560, 470), (600, 640), (540, 760), (450, 720), (430, 580)]), HAIR2)
    a.region("spot", ellipse(520, 650, 40, 50), TAN); return a

@part("ear", "elephant")
def _():
    a = P_("char.ear.elephant", "flap", 10); a.region("ear", blob([(530, 400), (360, 360), (280, 520), (340, 700), (500, 680), (540, 560)]), GREY)
    a.region("inner", blob([(500, 440), (380, 420), (330, 530), (380, 640), (480, 620)]), PINK); return a

@part("ear", "mouse")
def _():
    a = P_("char.ear.mouse", "twitch", 6); a.region("ear", circle(490, 400, 150), GREY); a.region("inner", circle(500, 410, 90), ROSE); return a

@part("ear", "fox")
def _():
    a = P_("char.ear.fox", "twitch", 8); a.region("ear", poly([(420, 560), (440, 220), (620, 540)], 40), ORANGE)
    a.region("tip", inter(poly([(420, 560), (440, 220), (620, 540)], 40), rect(0, 0, 1024, 330)), HAIR1)
    a.region("inner", poly([(460, 520), (470, 360), (560, 520)], 16), CREAM); return a

@part("ear", "antenna")
def _():
    a = P_("char.ear.antenna", "wobble", 14); a.region("stalk", thick(curve([(512, 540), (490, 420), (520, 300)]), 22), DGREY)
    a.region("bobble", circle(525, 270, 60), CORAL); a.shine(circle(505, 250, 16)); return a

# tails (socket at body join, tail extends right/up)
@part("tail", "cat")
def _():
    a = P_("char.tail.cat", "swish", 14); a.region("tail", thick(curve([(500, 520), (640, 500), (700, 380), (650, 300)]), 50), ORANGE)
    a.region("tip", circle(650, 300, 34), WHITE); return a

@part("tail", "fox")
def _():
    a = P_("char.tail.fox", "swish", 10); a.region("tail", blob([(500, 530), (620, 470), (760, 330), (820, 380), (760, 520), (600, 580)]), ORANGE)
    a.region("tip", inter(blob([(500, 530), (620, 470), (760, 330), (820, 380), (760, 520), (600, 580)]), circle(800, 360, 90)), WHITE); return a

@part("tail", "puff")
def _():
    a = P_("char.tail.puff", "wiggle", 8); a.region("puff", scallop(560, 500, 90, 90, 9, 16), WHITE); return a

@part("tail", "dino")
def _():
    a = P_("char.tail.dino", "swish", 6); a.region("tail", poly([(480, 450), (840, 560), (480, 600)], 40), GREEN)
    for i in range(3): a.region(f"spike{i}", poly([(540 + i * 90, 470 + i * 26), (580 + i * 90, 400 + i * 30), (620 + i * 90, 490 + i * 24)], 10), SUN)
    return a

@part("tail", "fin")
def _():
    a = P_("char.tail.fin", "swish", 12); a.region("stem", rrect(480, 470, 600, 550, 30), SKY)
    a.region("fin", poly([(580, 510), (780, 380), (720, 510), (780, 640)], 24), TURQ); return a

@part("tail", "lion")
def _():
    a = P_("char.tail.lion", "swish", 12); a.region("tail", thick(curve([(500, 520), (620, 540), (700, 470)]), 30), SUN)
    a.region("tuft", scallop(720, 450, 70, 70, 8, 16), HAIR2); return a

@part("tail", "peacock")
def _():
    a = P_("char.tail.peacock", "fan", 10)
    for i in range(5):
        f = rotate(ellipse(512, 300, 70, 200), -60 + i * 30, 512, 520)
        a.region(f"feather{i}", f, [TURQ, BLUE, GREEN, BLUE, TURQ][i])
    for i in range(5):
        a.region(f"eye{i}", rotate(circle(512, 170, 36), -60 + i * 30, 512, 520), SUN)
    return a

@part("tail", "curly")
def _():
    a = P_("char.tail.curly", "wiggle", 10)
    a.region("curl", thick(curve([(500, 520), (600, 520), (640, 450), (590, 410), (550, 450), (590, 480), (660, 470)]), 26), PINK); return a

# wings (left wing; socket at shoulder, wing extends left)
@part("wing", "feather")
def _():
    a = P_("char.wing.feather", "flap", 20)
    for i in range(3): a.region(f"feather{i}", ellipse(420 - i * 30, 470 + i * 60, 140 - i * 10, 50, -20 + i * 15), [SKY, BLUE, DEEPBLUE][i])
    a.region("cover", ellipse(470, 470, 90, 70, -20), WHITE); return a

@part("wing", "butterfly")
def _():
    a = P_("char.wing.butterfly", "flap", 24)
    a.region("upper", blob([(512, 500), (420, 330), (270, 330), (260, 470), (400, 520)]), PINK)
    a.region("lower", blob([(512, 520), (400, 540), (320, 650), (400, 700), (490, 610)]), LAV)
    a.region("dot", circle(350, 420, 40), SUN); return a

@part("wing", "bat")
def _():
    a = P_("char.wing.bat", "flap", 20)
    a.region("wing", poly([(512, 470), (300, 340), (210, 460), (280, 470), (300, 560), (380, 520), (420, 600), (512, 560)], 20), PURPLE)
    a.region("bone", thick([(512, 480), (310, 360)], 14), LAV); return a

@part("wing", "fairy")
def _():
    a = P_("char.wing.fairy", "shimmer", 16)
    a.region("upper", ellipse(390, 400, 140, 70, 30), MINT); a.region("lower", ellipse(410, 560, 110, 55, -25), SKY)
    a.shine(ellipse(380, 390, 40, 14, 30)); return a

@part("wing", "dragon")
def _():
    a = P_("char.wing.dragon", "flap", 18)
    a.region("membrane", poly([(512, 500), (380, 300), (330, 380), (250, 360), (240, 460), (180, 470), (240, 560), (512, 560)], 20), CORAL)
    a.region("arm", thick(curve([(512, 500), (420, 380), (380, 300)]), 22), RED); return a

@part("wing", "leaf")
def _():
    a = P_("char.wing.leaf", "sway", 12)
    lf = blob([(512, 500), (420, 420), (260, 420), (220, 480), (300, 560), (440, 560)])
    a.region("leaf", lf, LEAF); a.line([(500, 500), (260, 470)], 7); return a

# hats (socket = top centre of head, hat sits above)
@part("hat", "party")
def _():
    a = P_("char.hat.party", "bob", 6); a.region("hat", poly([(400, 540), (624, 540), (512, 260)], 20), TURQ)
    a.region("band", inter(poly([(400, 540), (624, 540), (512, 260)], 20), rect(0, 430, 1024, 470)), SUN)
    a.region("pom", circle(512, 250, 40), PINK); return a

@part("hat", "crown")
def _():
    a = P_("char.hat.crown", "shine", 0); a.region("crown", poly([(380, 540), (380, 370), (450, 440), (512, 330), (574, 440), (644, 370), (644, 540)], 16), SUN)
    a.region("gem", circle(512, 490, 30), RED); return a

@part("hat", "top_hat")
def _():
    a = P_("char.hat.top_hat", "bob", 4); a.region("brim", rrect(360, 500, 664, 550, 24), NAVYL)
    a.region("crown", rrect(410, 300, 614, 510, 20), NAVYL); a.region("band", rect(410, 440, 614, 490), CORAL); return a

@part("hat", "beanie")
def _():
    a = P_("char.hat.beanie", "bob", 4); a.region("cap", diff(ellipse(512, 540, 170, 180), rect(0, 540, 1024, 1024)), BLUE)
    a.region("cuff", rrect(330, 500, 694, 560, 28), SKY); a.region("pom", circle(512, 350, 50), WHITE); return a

@part("hat", "flower_crown")
def _():
    a = P_("char.hat.flower_crown", "sway", 4); a.region("vine", thick(curve([(350, 520), (512, 480), (674, 520)]), 24), GREEN)
    for i, x in enumerate((380, 450, 512, 574, 644)):
        a.region(f"bloom{i}", scallop(x, 500 - (2 - abs(i - 2)) * 12, 40, 40, 6, 10), [PINK, SUN, CORAL, SUN, PINK][i])
    return a

@part("hat", "chef")
def _():
    a = P_("char.hat.chef", "bob", 4); a.region("band", rrect(400, 460, 624, 550, 20), WHITE)
    a.region("puff", union(circle(430, 400, 80), circle(512, 350, 100), circle(594, 400, 80)), CREAM); return a

@part("hat", "cap")
def _():
    a = P_("char.hat.cap", "bob", 4); a.region("cap", diff(ellipse(512, 540, 160, 130), rect(0, 540, 1024, 1024)), CORAL)
    a.region("visor", ellipse(640, 540, 130, 30), RED); a.region("button", circle(512, 412, 20), SUN); return a

@part("hat", "wizard")
def _():
    a = P_("char.hat.wizard", "bob", 6); a.region("brim", ellipse(512, 530, 200, 40), PURPLE)
    a.region("cone", poly([(400, 530), (624, 530), (560, 260), (620, 220)], 24), PURPLE)
    a.region("star", star(512, 440, 46, 20, 5, round_r=4), SUN); return a

# decor (socket = neck/chest centre)
@part("decor", "bow_tie")
def _():
    a = P_("char.decor.bow_tie", None); a.region("left", poly([(512, 512), (400, 450), (400, 574)], 20), RED)
    a.region("right", poly([(512, 512), (624, 450), (624, 574)], 20), RED); a.region("knot", circle(512, 512, 30), CORAL); return a

@part("decor", "scarf")
def _():
    a = P_("char.decor.scarf", "sway", 4); a.region("wrap", rrect(380, 480, 644, 550, 34), BLUE)
    a.region("tail", rrect(560, 520, 620, 680, 20, -10), SKY); return a

@part("decor", "beads")
def _():
    a = P_("char.decor.beads", None)
    for i in range(7):
        x = 380 + i * 44; y = 500 + 50 * (1 - ((x - 512) / 140) ** 2)
        a.region(f"bead{i}", circle(x, y, 24), [PINK, SUN, SKY, LEAF, SKY, SUN, PINK][i])
    return a

@part("decor", "medal")
def _():
    a = P_("char.decor.medal", None); a.region("ribbon", poly([(460, 470), (564, 470), (540, 560), (484, 560)], 10), BLUE)
    a.region("medal", circle(512, 600, 60), SUN); a.region("star", star(512, 600, 34, 15, 5, round_r=3), ORANGE); return a

@part("decor", "bell_collar")
def _():
    a = P_("char.decor.bell_collar", "jingle", 6); a.region("collar", rrect(370, 480, 654, 530, 24), CORAL)
    a.region("bell", union(circle(512, 570, 46), rect(466, 560, 558, 600)), SUN); return a

@part("decor", "heart_pocket")
def _():
    a = P_("char.decor.heart_pocket", None); a.region("pocket", rrect(440, 480, 584, 600, 24), WHITE)
    a.region("heart", heart(512, 540, 40), PINK); return a

@part("decor", "lei")
def _():
    a = P_("char.decor.lei", None)
    for i in range(6):
        x = 390 + i * 49; y = 500 + 40 * (1 - ((x - 512) / 130) ** 2)
        a.region(f"flower{i}", scallop(x, y, 32, 32, 5, 9), [CORAL, SUN, PINK, CORAL, SUN, PINK][i])
    return a

@part("decor", "bandana")
def _():
    a = P_("char.decor.bandana", "sway", 4); a.region("cloth", poly([(380, 480), (644, 480), (512, 640)], 20), RED)
    for i, (x, y) in enumerate([(460, 520), (560, 520), (512, 580)]): a.region(f"dot{i}", circle(x, y, 14), WHITE)
    return a


assert len(BODIES) == 6
KIND_COUNT = {k: sum(1 for p in PARTS if p[2] == k) for k in ("ear", "tail", "wing", "hat", "decor")}
assert KIND_COUNT == {"ear": 8, "tail": 8, "wing": 6, "hat": 8, "decor": 8}, KIND_COUNT
SOCKETS_OF_KIND = {"ear": ["ear_l", "ear_r"], "tail": ["tail"], "wing": ["wing_l", "wing_r"], "hat": ["hat"], "decor": ["decor"]}
PRESETS = {"char.body.bean": "hop", "char.body.pear": "waddle", "char.body.tall": "wave",
           "char.body.boxy": "roll", "char.body.star": "twirl", "char.body.fluffy": "bounce"}
