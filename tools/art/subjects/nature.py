from colora_art import *
from pal import *


def tree():
    a = Art("nature.tree")
    a.part("trunk", pivot=(512, 940))
    a.region("trunk", poly([(450, 940), (574, 940), (550, 560), (474, 560)], 20), BROWN)
    a.region("branch", thick([(530, 660), (620, 580)], 30), BROWN)
    a.region("hole", ellipse(512, 760, 26, 36), HAIR1)
    a.region("grass", scallop(512, 940, 150, 20, 9, 26), LEAF)
    a.part("crown", parent="trunk", pivot=(512, 560), motion="sway", amp=4)
    a.region("crown", union(circle(512, 330, 220), circle(340, 430, 140), circle(690, 430, 140), circle(380, 250, 130), circle(650, 250, 130)), GREEN)
    a.region("crown_light", union(circle(430, 260, 70), circle(500, 230, 60)), LEAF)
    for i, (x, y) in enumerate([(600, 380), (420, 450), (700, 300), (330, 330), (520, 500)]):
        a.region(f"apple{i}", circle(x, y, 30), RED)
    a.anchor("base", "plant", 512, 940); a.anchor("branch", "seat", 610, 560)
    return a


def sun():
    a = Art("nature.sun")
    a.part("rays", pivot=(512, 512), motion="spinslow", amp=360)
    for i in range(12):
        ray = poly([(512 - 34, 512 - 250), (512, 512 - 360), (512 + 34, 512 - 250)], 16)
        a.region(f"ray{i}", rotate(ray, i * 30, 512, 512), ORANGE if i % 2 else SUN)
    a.part("face", pivot=(512, 512), motion="pulse", amp=4)
    a.region("disc", circle(512, 512, 240), YELLOW)
    a.region("glow", circle(512, 512, 180), SUN)
    a.eyes(512, 480, 140, 26, lids=True)
    a.smile(512, 570, 140, 0.45, 9)
    a.cheeks(512, 560, 250, 24)
    a.anchor("sky", "sky", 512, 512)
    return a


def cloud():
    a = Art("nature.cloud")
    a.part("body", pivot=(512, 560), motion="drift", amp=20)
    a.region("cloud", union(circle(330, 560, 140), circle(500, 470, 190), circle(690, 540, 150), rrect(220, 540, 820, 700, 80)), WHITE)
    a.part("babies", pivot=(512, 760), motion="drift", amp=12)
    a.region("baby_l", union(circle(200, 800, 50), circle(260, 780, 60), rrect(150, 790, 320, 850, 30)), SKY)
    a.region("baby_r", union(circle(780, 820, 46), circle(840, 800, 56), rrect(730, 810, 900, 866, 28)), SKY)
    a.part("body")
    a.region("belly", inter(union(circle(330, 560, 140), circle(500, 470, 190), circle(690, 540, 150), rrect(220, 540, 820, 700, 80)), rect(200, 640, 840, 720)), LAV)
    a.eyes(512, 540, 120, 22)
    a.smile(512, 590, 70, 0.6, 7)
    a.cheeks(512, 590, 190, 18)
    a.anchor("sky", "sky", 512, 560)
    return a


def rainbow():
    a = Art("nature.rainbow")
    a.part("arc", pivot=(512, 760), motion="shimmer", amp=0)
    cols = [RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE]
    for i, c in enumerate(cols):
        r_out = 420 - i * 48; r_in = r_out - 48
        band = diff(circle(512, 720, r_out), circle(512, 720, r_in), rect(0, 720, 1024, 2048))
        a.region(f"band{i}", band, c)
    a.region("cloud_l", union(circle(130, 720, 70), circle(210, 690, 80), circle(280, 730, 60), rrect(90, 720, 330, 800, 40)), WHITE)
    a.region("cloud_r", union(circle(744, 730, 60), circle(814, 690, 80), circle(894, 720, 70), rrect(694, 720, 934, 800, 40)), WHITE)
    a.anchor("under", "sky", 512, 720)
    return a


def mountain():
    a = Art("nature.mountain")
    a.part("body", pivot=(512, 880))
    a.region("far", poly([(560, 880), (760, 380), (960, 880)], 40), LAV)
    a.region("far_snow", inter(poly([(560, 880), (760, 380), (960, 880)], 40), poly([(640, 540), (700, 580), (760, 520), (820, 580), (880, 540), (760, 300)])), WHITE)
    a.region("main", poly([(70, 880), (420, 220), (760, 880)], 50), DEEPBLUE)
    a.region("snow", inter(poly([(70, 880), (420, 220), (760, 880)], 50), poly([(250, 470), (330, 520), (410, 450), (480, 520), (560, 450), (420, 120)])), WHITE)
    a.region("meadow", union(ellipse(300, 900, 300, 70), ellipse(720, 910, 300, 60)), LEAF)
    a.region("pine1", poly([(180, 840), (230, 700), (280, 840)], 12), GREEN)
    a.region("pine2", poly([(800, 850), (850, 720), (900, 850)], 12), GREEN)
    a.anchor("peak", "sky", 420, 220); a.anchor("base", "plant", 300, 880)
    return a


def pond():
    a = Art("nature.pond")
    a.part("body", pivot=(512, 700))
    a.region("bank", blob([(110, 600), (300, 480), (720, 470), (930, 580), (900, 800), (512, 880), (130, 790)]), SAND)
    a.region("water", blob([(180, 610), (330, 530), (700, 520), (850, 600), (820, 740), (512, 800), (200, 740)]), SKY)
    a.region("ripple", diff(ellipse(560, 650, 110, 40), ellipse(560, 650, 80, 26)), BLUE)
    a.region("lilypad", diff(ellipse(360, 650, 80, 34), poly([(360, 650), (430, 620), (440, 640)])), GREEN)
    a.region("reed1", thick([(820, 600), (830, 420)], 14), GREEN)
    a.region("reed_top1", ellipse(830, 410, 18, 48), BROWN)
    a.region("reed2", thick([(860, 610), (880, 470)], 14), GREEN)
    a.region("reed_top2", ellipse(880, 460, 16, 42), BROWN)
    a.region("stone1", ellipse(170, 560, 60, 40), GREY)
    a.region("stone2", ellipse(240, 540, 40, 28), DGREY)
    a.anchor("water", "water", 512, 660); a.anchor("shore", "plant", 170, 700)
    return a


def waterfall():
    a = Art("nature.waterfall")
    a.part("body", pivot=(512, 900))
    a.region("cliff_l", poly([(80, 940), (100, 220), (300, 160), (400, 220), (420, 940)], 40), DGREY)
    a.region("cliff_r", poly([(604, 940), (624, 240), (760, 170), (930, 220), (944, 940)], 40), DGREY)
    a.region("grass_l", union(ellipse(250, 190, 190, 50)), LEAF)
    a.region("grass_r", union(ellipse(780, 200, 180, 50)), LEAF)
    fall = rrect(380, 210, 644, 820, 30)
    a.region("fall", fall, SKY)
    for i, x in enumerate((440, 512, 584)):
        a.line([(x, 260), (x + 6, 500), (x, 760)], 6)
    a.region("pool", wave_band(800, 120, 904, 940, 6), BLUE)
    a.region("splash", union(circle(400, 800, 40), circle(470, 780, 46), circle(554, 780, 46), circle(624, 800, 40)), WHITE)
    a.region("rock", ellipse(220, 870, 70, 40), GREY)
    a.anchor("pool", "water", 512, 870)
    return a


def mushroom():
    a = Art("nature.mushroom")
    a.part("body", pivot=(512, 900), motion="squash", amp=6)
    a.region("grass", scallop(512, 900, 220, 24, 11, 28), LEAF)
    a.region("stem", blob([(440, 560), (584, 560), (604, 760), (590, 880), (434, 880), (420, 760)]), CREAM)
    a.region("cap", blob([(512, 230), (720, 300), (810, 470), (760, 580), (512, 600), (264, 580), (214, 470), (304, 300)]), RED)
    a.region("gills", inter(ellipse(512, 590, 250, 40), blob([(512, 230), (720, 300), (810, 470), (760, 580), (512, 600), (264, 580), (214, 470), (304, 300)])), TAN)
    for i, (x, y, r) in enumerate([(512, 330, 50), (380, 440, 40), (650, 430, 44), (300, 530, 24), (730, 530, 26), (520, 500, 30)]):
        a.region(f"spot{i}", circle(x, y, r), WHITE)
    a.region("door", rrect(470, 720, 554, 880, 40), BROWN)
    a.ink(circle(536, 800, 7))
    a.anchor("base", "plant", 512, 900)
    return a


def leaf():
    a = Art("nature.leaf")
    a.part("body", pivot=(300, 820), motion="sway", amp=8)
    shape = blob([(230, 840), (300, 600), (450, 380), (700, 200), (840, 170), (820, 330), (700, 560), (500, 760), (330, 850)])
    a.region("stem", thick([(150, 930), (260, 830)], 26), BROWN)
    halves = [inter(shape, poly([(150, 950), (860, 150), (1024, 1024), (100, 1024)])), inter(shape, poly([(150, 950), (860, 150), (0, 0)]))]
    a.region("half_a", halves[0], GREEN)
    a.region("half_b", halves[1], LEAF)
    a.line(curve([(250, 830), (480, 560), (830, 180)]), 7)
    for t, side in [(0.3, 1), (0.5, 1), (0.7, 1), (0.35, -1), (0.55, -1), (0.75, -1)]:
        x = 250 + (830 - 250) * t; y = 830 + (180 - 830) * t
        a.line([(x, y), (x + side * 70 + 40, y + side * 60 - 40)], 5)
    a.region("drop", blob([(620, 520), (650, 560), (630, 590), (604, 570)]), SKY)
    a.anchor("carry", "carry", 480, 560)
    return a


def moon():
    a = Art("nature.moon")
    a.part("body", pivot=(512, 512), motion="rock", amp=6)
    crescent = diff(circle(500, 512, 300), circle(640, 430, 260))
    a.region("moon", crescent, YELLOW)
    a.region("crater1", circle(320, 600, 36), SUN)
    a.region("crater2", circle(400, 720, 26), SUN)
    a.eyes(330, 470, 0, 0) if False else None
    a.line(arc(300, 470, 26, 20, 200, 140), 8)
    a.smile(330, 540, 50, 0.6, 7)
    a.blush(ellipse(270, 530, 22, 14))
    a.part("stars", pivot=(700, 700), motion="twinkle", amp=10)
    a.region("star1", star(760, 700, 60, 26, 5, -90, 8), SUN)
    a.region("star2", star(850, 560, 36, 16, 5, -90, 6), SUN)
    a.region("star3", star(700, 860, 30, 13, 5, -90, 5), SUN)
    a.anchor("sky", "sky", 512, 512)
    return a


def rain():
    a = Art("nature.rain")
    a.part("drops", pivot=(512, 700), motion="fall", amp=30)
    for i, (x, y) in enumerate([(320, 680), (440, 760), (560, 690), (680, 770), (380, 860), (620, 880)]):
        a.region(f"drop{i}", blob([(x, y - 46), (x + 26, y), (x, y + 26), (x - 26, y)], 1), SKY)
    a.part("cloud", pivot=(512, 450), motion="drift", amp=10)
    body = union(circle(320, 470, 120), circle(480, 380, 170), circle(680, 450, 140), rrect(220, 450, 800, 600, 70))
    a.region("cloud", body, GREY)
    a.region("under", inter(body, rect(200, 540, 840, 620)), DGREY)
    a.eyes(510, 470, 110, 18, lids=True)
    a.line(curve([(470, 520), (510, 506), (550, 520)]), 7)
    a.anchor("sky", "sky", 512, 450)
    return a


def island():
    a = Art("nature.island")
    a.part("body", pivot=(512, 780))
    a.region("sea", wave_band(700, 80, 944, 900, 7), SKY)
    a.region("sand", blob([(260, 720), (400, 610), (640, 600), (800, 700), (700, 760), (330, 770)]), SAND)
    a.region("trunk", thick([(520, 650), (500, 500), (540, 330)], 40), BROWN)
    for i, ang in enumerate((-160, -110, -60, -10, 30)):
        lf = blob([(540, 330), (540 + 130 * math.cos(math.radians(ang + 20)), 330 + 110 * math.sin(math.radians(ang + 20)) - 20), (540 + 230 * math.cos(math.radians(ang)), 330 + 200 * math.sin(math.radians(ang)) + 60), (540 + 120 * math.cos(math.radians(ang - 20)), 330 + 90 * math.sin(math.radians(ang - 20)) + 20)])
        a.region(f"frond{i}", lf, GREEN if i % 2 else LEAF)
    a.region("coconuts", union(circle(520, 350, 28), circle(560, 360, 26)), HAIR2)
    a.region("shell", heart(360, 690, 26), PINK)
    a.region("starfish", star(700, 690, 34, 14, 5, -90, 6), ORANGE)
    a.anchor("beach", "plant", 420, 690); a.anchor("sea", "water", 860, 800)
    return a


ALL = [tree, sun, cloud, rainbow, mountain, pond, waterfall, mushroom, leaf, moon, rain, island]
