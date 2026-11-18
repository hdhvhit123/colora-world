"""36 original stickers in 6 groups (spec: ≥36 stickers). Pre-coloured, simple
shapes in the shared outline style so they sit well on any artwork."""
import math
from colora_art import *
from pal import *

GROUPS = {"sparkle": [], "nature": [], "weather": [], "treats": [], "party": [], "friends": []}


def st(group, name):
    def deco(fn):
        GROUPS[group].append((f"sticker.{group}.{name}", fn))
        return fn
    return deco


def S(sid):
    a = Art(sid); a.part("sticker", pivot=(512, 512), motion="pop", amp=8); return a

# ---------------------------------------------------------------- sparkle
@st("sparkle", "star")
def _():
    a = S("sticker.sparkle.star"); a.region("star", star(512, 530, 340, 160, 5, round_r=30), SUN)
    a.region("shine", star(512, 530, 170, 80, 5, round_r=14), YELLOW); a.eyes(512, 530, 110, 22); a.smile(512, 590, 60); return a

@st("sparkle", "heart")
def _():
    a = S("sticker.sparkle.heart"); a.region("heart", heart(512, 540, 330), CORAL)
    a.region("inner", heart(512, 520, 180), PINK); a.shine(ellipse(380, 380, 50, 28, -40)); return a

@st("sparkle", "twinkle")
def _():
    a = S("sticker.sparkle.twinkle")
    a.region("big", star(450, 560, 280, 70, 4, round_r=20), LAV)
    a.region("small", star(720, 300, 130, 34, 4, round_r=10), SKY)
    a.region("dot", circle(740, 700, 50), PINK); return a

@st("sparkle", "rainbow_puff")
def _():
    a = S("sticker.sparkle.rainbow_puff")
    for i, c in enumerate([CORAL, SUN, LEAF, SKY]):
        r = 330 - i * 60
        a.region(f"band{i}", diff(circle(512, 640, r), circle(512, 640, r - 60), rect(0, 640, 1024, 2048)), c)
    a.region("puff_l", union(circle(210, 650, 80), circle(290, 660, 70)), WHITE)
    a.region("puff_r", union(circle(814, 650, 80), circle(734, 660, 70)), WHITE); return a

@st("sparkle", "crescent")
def _():
    a = S("sticker.sparkle.crescent"); a.region("moon", diff(circle(512, 512, 320), circle(650, 420, 270)), SUN)
    a.region("star", star(700, 640, 90, 40, 5, round_r=8), YELLOW)
    a.eyes(360, 520, 0.1, 22, lids=True); a.cheeks(380, 600, 0.1, 22); return a

@st("sparkle", "gem")
def _():
    a = S("sticker.sparkle.gem")
    a.region("crown", poly([(270, 400), (400, 260), (624, 260), (754, 400)], 12), MINT)
    a.region("body", poly([(270, 400), (754, 400), (512, 820)], 16), TURQ)
    a.region("facet", poly([(420, 400), (604, 400), (512, 760)], 10), SKY); return a

# ---------------------------------------------------------------- nature
@st("nature", "flower")
def _():
    a = S("sticker.nature.flower")
    for i, p in enumerate(petals(512, 512, 6, 90, 230, 170)): a.region(f"petal{i}", p, PINK if i % 2 else ROSE)
    a.region("center", circle(512, 512, 110), SUN); a.eyes(512, 500, 70, 14); a.smile(512, 540, 40); return a

@st("nature", "clover")
def _():
    a = S("sticker.nature.clover")
    for i in range(4): a.region(f"leaf{i}", rotate(heart(512, 360, 200), i * 90, 512, 512), GREEN if i % 2 else LEAF)
    a.region("stem", thick(curve([(512, 520), (560, 760), (640, 880)]), 30), GREEN); return a

@st("nature", "mushroom")
def _():
    a = S("sticker.nature.mushroom")
    a.region("stem", rrect(400, 480, 624, 860, 80), CREAM)
    a.region("cap", diff(ellipse(512, 500, 330, 250), rect(0, 520, 1024, 1024)), RED)
    for i, (x, y) in enumerate([(390, 380), (560, 330), (660, 450), (460, 470)]): a.region(f"spot{i}", circle(x, y, 40), WHITE)
    a.eyes(512, 660, 80, 16); return a

@st("nature", "ladybug")
def _():
    a = S("sticker.nature.ladybug")
    a.region("head", circle(512, 280, 120), NAVYL)
    a.region("shell", circle(512, 560, 280), RED)
    for i, (x, y) in enumerate([(400, 470), (624, 470), (380, 640), (644, 640), (512, 760)]): a.region(f"dot{i}", circle(x, y, 45), NAVYL)
    a.line([(512, 300), (512, 840)], 10); a.eyes(512, 260, 90, 18); return a

@st("nature", "snail")
def _():
    a = S("sticker.nature.snail")
    a.region("body", union(rrect(180, 660, 860, 780, 60), circle(780, 600, 110)), TAN)
    a.region("shell", circle(450, 520, 220), ORANGE)
    a.region("swirl", diff(circle(450, 520, 140), circle(470, 510, 70)), SUN)
    a.line(curve([(770, 500), (740, 380)]), 10); a.line(curve([(820, 500), (850, 380)]), 10)
    a.eyes(800, 590, 70, 14); return a

@st("nature", "acorn")
def _():
    a = S("sticker.nature.acorn")
    a.region("nut", blob([(320, 460), (704, 460), (690, 640), (512, 860), (334, 640)]), BROWN)
    a.region("cap", union(ellipse(512, 440, 230, 120), rrect(492, 240, 532, 340, 16)), HAIR2)
    a.eyes(512, 600, 110, 18); a.cheeks(512, 650, 190, 20); return a

# ---------------------------------------------------------------- weather
@st("weather", "cloud")
def _():
    a = S("sticker.weather.cloud")
    a.region("cloud", union(circle(360, 560, 150), circle(540, 460, 200), circle(700, 580, 140), rrect(220, 560, 840, 720, 80)), WHITE)
    a.eyes(530, 560, 120, 20); a.smile(530, 610, 60); a.cheeks(530, 610, 200, 20); return a

@st("weather", "raindrop")
def _():
    a = S("sticker.weather.raindrop")
    a.region("drop", union(circle(512, 620, 230), poly([(300, 540), (512, 180), (724, 540)])), SKY)
    a.shine(ellipse(420, 560, 40, 70, 20)); a.eyes(530, 640, 100, 18); a.smile(530, 700, 50); return a

@st("weather", "snowflake")
def _():
    a = S("sticker.weather.snowflake")
    arm = union(thick([(512, 512), (512, 180)], 50), thick([(512, 300), (430, 230)], 34), thick([(512, 300), (594, 230)], 34))
    a.region("flake", union(*[rotate(arm, i * 60, 512, 512) for i in range(6)]), SKY)
    a.region("core", circle(512, 512, 90), WHITE); return a

@st("weather", "sunny")
def _():
    a = S("sticker.weather.sunny")
    a.region("rays", union(*[rotate(poly([(482, 230), (512, 130), (542, 230)], 10), i * 45, 512, 512) for i in range(8)]), ORANGE)
    a.region("sun", circle(512, 512, 250), SUN); a.eyes(512, 490, 120, 22); a.smile(512, 560, 90); return a

@st("weather", "umbrella")
def _():
    a = S("sticker.weather.umbrella")
    canopy = union(diff(circle(512, 520, 340), rect(0, 520, 1024, 1024)), *[circle(512 + k * 170, 520, 85) for k in (-1.5, -0.5, 0.5, 1.5)])
    canopy = diff(canopy, rect(0, 540, 1024, 1024))
    a.region("handle", thick(curve([(512, 500), (512, 800), (470, 860), (420, 820)]), 30), DGREY)
    for i in range(4):
        a.region(f"panel{i}", inter(canopy, rect(172 + i * 170, 0, 342 + i * 170, 1024)), CORAL if i % 2 else SUN)
    return a

@st("weather", "breeze")
def _():
    a = S("sticker.weather.breeze")
    for i, (y, c) in enumerate([(380, SKY), (520, MINT), (660, LAV)]):
        a.region(f"wind{i}", thick(curve([(200 + i * 40, y), (520, y - 30), (700, y), (760, y - 80), (700, y - 130)]), 44), c)
    a.region("leaf", ellipse(820, 640, 70, 36, -30), LEAF); return a

# ---------------------------------------------------------------- treats
@st("treats", "cupcake")
def _():
    a = S("sticker.treats.cupcake")
    a.region("cup", poly([(330, 540), (694, 540), (640, 860), (384, 860)], 20), SKY)
    a.region("frosting", union(circle(400, 520, 110), circle(624, 520, 110), circle(512, 420, 140)), PINK)
    a.region("cherry", circle(512, 260, 60), RED); return a

@st("treats", "lollipop")
def _():
    a = S("sticker.treats.lollipop")
    a.region("stick", rrect(492, 560, 532, 900, 18), CREAM)
    a.region("candy", circle(512, 400, 250), CORAL)
    a.region("swirl", diff(circle(512, 400, 170), circle(540, 390, 90)), SUN); return a

@st("treats", "ice_cream")
def _():
    a = S("sticker.treats.ice_cream")
    a.region("cone", poly([(360, 520), (664, 520), (512, 900)], 20), TAN)
    a.region("scoop_b", circle(430, 440, 130), MINT); a.region("scoop_a", circle(600, 430, 130), PINK)
    a.region("top", circle(512, 300, 120), CREAM); return a

@st("treats", "cherries")
def _():
    a = S("sticker.treats.cherries")
    a.region("stems", union(thick(curve([(380, 640), (470, 360), (560, 220)]), 22), thick(curve([(660, 640), (600, 380), (560, 220)]), 22)), GREEN)
    a.region("leaf", ellipse(640, 240, 90, 44, -20), LEAF)
    a.region("cherry_l", circle(370, 690, 140), RED); a.region("cherry_r", circle(670, 690, 140), CORAL); return a

@st("treats", "donut")
def _():
    a = S("sticker.treats.donut")
    a.region("dough", diff(circle(512, 512, 320), circle(512, 512, 100)), TAN)
    a.region("icing", diff(scallop(512, 500, 270, 260, 10, 26), circle(512, 512, 120)), PINK)
    for i in range(7):
        ang = math.radians(i * 51)
        a.region(f"sprinkle{i}", rrect(-14, -40, 14, 40, 12, i * 40).__class__() if False else rotate(rrect(498, 272, 526, 332, 12), i * 51, 512, 512), [SUN, SKY, LEAF][i % 3])
    return a

@st("treats", "candy")
def _():
    a = S("sticker.treats.candy")
    a.region("wrap_l", poly([(140, 380), (330, 512), (140, 640)], 24), LAV)
    a.region("wrap_r", poly([(884, 380), (694, 512), (884, 640)], 24), LAV)
    a.region("candy", ellipse(512, 512, 220, 160), PURPLE)
    a.region("stripe", inter(ellipse(512, 512, 220, 160), union(rect(400, 0, 450, 1024), rect(560, 0, 610, 1024))), PINK); return a

# ---------------------------------------------------------------- party
@st("party", "balloon")
def _():
    a = S("sticker.party.balloon")
    a.region("string", thick(curve([(512, 720), (470, 820), (540, 920)]), 12), DGREY)
    a.region("balloon", ellipse(512, 420, 230, 290), CORAL)
    a.region("knot", poly([(480, 730), (544, 730), (512, 690)], 8), RED); a.shine(ellipse(420, 320, 40, 80, 20)); return a

@st("party", "party_hat")
def _():
    a = S("sticker.party.party_hat")
    a.region("hat", poly([(260, 820), (764, 820), (512, 200)], 30), TURQ)
    a.region("stripe", inter(poly([(260, 820), (764, 820), (512, 200)], 30), union(rect(0, 460, 1024, 520), rect(0, 640, 1024, 700))), SUN)
    a.region("pom", circle(512, 190, 70), PINK); return a

@st("party", "crown")
def _():
    a = S("sticker.party.crown")
    a.region("crown", poly([(220, 720), (220, 340), (370, 500), (512, 300), (654, 500), (804, 340), (804, 720)], 24), SUN)
    a.region("band", rrect(220, 640, 804, 740, 20), ORANGE)
    for i, x in enumerate((330, 512, 694)): a.region(f"jewel{i}", circle(x, 690, 34), [RED, SKY, LEAF][i])
    return a

@st("party", "confetti")
def _():
    a = S("sticker.party.confetti")
    pieces = [(300, 300, SUN), (520, 220, PINK), (720, 330, SKY), (380, 520, LEAF), (640, 540, CORAL), (300, 760, LAV), (560, 780, TURQ), (760, 720, ORANGE)]
    for i, (x, y, c) in enumerate(pieces):
        shape = circle(x, y, 50) if i % 3 == 0 else (rrect(x - 30, y - 70, x + 30, y + 70, 18, i * 35) if i % 3 == 1 else star(x, y, 70, 30, 5, round_r=6))
        a.region(f"bit{i}", shape, c)
    return a

@st("party", "music_note")
def _():
    a = S("sticker.party.music_note")
    a.region("beam", poly([(420, 260), (760, 200), (760, 290), (420, 350)], 12), PURPLE)
    a.region("stem_l", rect(420, 300, 460, 720), PURPLE); a.region("stem_r", rect(720, 240, 760, 660), PURPLE)
    a.region("head_l", ellipse(380, 730, 100, 76, -20), LAV); a.region("head_r", ellipse(680, 670, 100, 76, -20), LAV); return a

@st("party", "ribbon_badge")
def _():
    a = S("sticker.party.ribbon_badge")
    a.region("tail_l", poly([(400, 560), (330, 900), (420, 850), (470, 900), (500, 600)], 10), CORAL)
    a.region("tail_r", poly([(624, 560), (694, 900), (604, 850), (554, 900), (524, 600)], 10), CORAL)
    a.region("rosette", scallop(512, 420, 250, 250, 14, 30), SUN)
    a.region("center", circle(512, 420, 150), WHITE); a.region("star", star(512, 425, 100, 46, 5, round_r=8), ORANGE); return a

# ---------------------------------------------------------------- friends
@st("friends", "bee")
def _():
    a = S("sticker.friends.bee")
    a.region("wing_l", ellipse(420, 300, 110, 150, -20), WHITE); a.region("wing_r", ellipse(604, 300, 110, 150, 20), WHITE)
    body = ellipse(512, 560, 270, 220)
    a.region("body", body, SUN)
    a.region("stripes", inter(body, union(rect(420, 0, 480, 1024), rect(560, 0, 620, 1024))), NAVYL)
    a.eyes(370, 540, 90, 18); a.smile(380, 610, 50); return a

@st("friends", "chick")
def _():
    a = S("sticker.friends.chick")
    a.region("body", union(circle(512, 600, 250), circle(512, 360, 180)), YELLOW)
    a.region("wing", ellipse(660, 620, 90, 60, -30), SUN)
    a.region("beak", poly([(470, 400), (554, 400), (512, 460)], 10), ORANGE)
    a.region("tuft", union(ellipse(490, 170, 30, 60, -20), ellipse(540, 170, 30, 60, 20)), SUN)
    a.eyes(512, 340, 120, 18); a.cheeks(512, 410, 220, 20); return a

@st("friends", "fish")
def _():
    a = S("sticker.friends.fish")
    a.region("tail", poly([(700, 512), (880, 360), (880, 664)], 26), ORANGE)
    a.region("body", ellipse(470, 512, 290, 210), SKY)
    a.region("fin", poly([(420, 320), (520, 230), (580, 330)], 16), TURQ)
    a.region("stripe", inter(ellipse(470, 512, 290, 210), rect(500, 0, 560, 1024)), WHITE)
    a.eyes(320, 480, 0.1, 26); a.smile(300, 580, 50); return a

@st("friends", "bunny_face")
def _():
    a = S("sticker.friends.bunny_face")
    a.region("ear_l", ellipse(400, 260, 80, 200, -10), WHITE); a.region("ear_r", ellipse(624, 260, 80, 200, 10), WHITE)
    a.region("inner_l", ellipse(400, 270, 36, 140, -10), PINK); a.region("inner_r", ellipse(624, 270, 36, 140, 10), PINK)
    a.region("face", ellipse(512, 610, 300, 250), WHITE)
    a.eyes(512, 580, 160, 22); a.ink(ellipse(512, 650, 26, 18)); a.cheeks(512, 670, 290, 26); return a

@st("friends", "bear_face")
def _():
    a = S("sticker.friends.bear_face")
    a.region("ear_l", circle(300, 330, 110), BROWN); a.region("ear_r", circle(724, 330, 110), BROWN)
    a.region("face", circle(512, 560, 300), TAN)
    a.region("muzzle", ellipse(512, 660, 130, 100), CREAM)
    a.eyes(512, 540, 170, 22); a.ink(ellipse(512, 630, 36, 24)); a.smile(512, 690, 60); return a

@st("friends", "frog_face")
def _():
    a = S("sticker.friends.frog_face")
    a.region("eye_l", circle(360, 330, 120), GREEN); a.region("eye_r", circle(664, 330, 120), GREEN)
    a.region("face", ellipse(512, 590, 320, 230), LEAF)
    a.region("white_l", circle(360, 320, 70), WHITE); a.region("white_r", circle(664, 320, 70), WHITE)
    a.ink(circle(370, 330, 30)); a.ink(circle(654, 330, 30))
    a.line(curve([(330, 640), (512, 740), (694, 640)]), 12); a.cheeks(512, 650, 440, 30)
    return a


ALL = [(sid, fn, [g]) for g, items in GROUPS.items() for sid, fn in items]
assert len(ALL) == 36 and all(len(v) == 6 for v in GROUPS.values()), {k: len(v) for k, v in GROUPS.items()}
