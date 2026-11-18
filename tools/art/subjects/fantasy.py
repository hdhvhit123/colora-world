from colora_art import *
from pal import *


def dragon():
    a = Art("fantasy.dragon")
    a.part("wing", pivot=(560, 460), motion="flap", amp=18)
    a.region("wing", blob([(540, 470), (620, 260), (760, 180), (740, 300), (840, 290), (780, 400), (860, 420), (700, 520)], 0.7), LAV)
    a.part("tail", pivot=(680, 680), motion="wag", amp=12)
    a.region("tail", thick([(660, 700), (800, 760), (900, 700)], 70), GREEN)
    a.region("tail_tip", poly([(880, 650), (970, 640), (930, 740)], 16), SUN)
    a.part("body", pivot=(512, 880))
    a.region("leg_far", rrect(560, 700, 640, 860, 34), OLIVE)
    a.region("body", ellipse(540, 640, 170, 160), GREEN)
    a.region("belly", ellipse(500, 670, 90, 120), SUN)
    for y in (600, 650, 700, 750):
        a.line([(430, y), (570, y)], 5)
    a.region("leg", rrect(420, 720, 510, 880, 40), GREEN)
    a.region("foot", ellipse(460, 880, 70, 28), GREEN)
    for i, (x, y) in enumerate([(560, 470), (640, 500), (700, 560)]):
        a.region(f"spike{i}", poly([(x - 26, y + 20), (x, y - 40), (x + 26, y + 20)], 8), SUN)
    a.part("head", parent="body", pivot=(440, 500), motion="nod", amp=6)
    a.region("horn_l", poly([(330, 250), (300, 150), (370, 230)], 10), CREAM)
    a.region("horn_r", poly([(450, 240), (470, 140), (500, 240)], 10), CREAM)
    a.region("head", blob([(260, 300), (420, 220), (530, 300), (520, 430), (400, 480), (240, 460), (200, 380)]), GREEN)
    a.region("snout", ellipse(260, 410, 90, 60), LEAF)
    a.ink(ellipse(230, 390, 10, 8)); a.ink(ellipse(280, 390, 10, 8))
    a.eyes(400, 330, 90, 22)
    a.smile(280, 440, 60, 0.4, 6)
    a.cheeks(380, 400, 180, 16)
    a.anchor("ride", "passenger", 600, 480)
    return a


def unicorn():
    a = Art("fantasy.unicorn")
    body = blob([(400, 470), (640, 450), (780, 500), (800, 600), (740, 660), (470, 670), (390, 600)])
    a.part("tail", pivot=(790, 520), motion="wag", amp=14)
    a.region("tail_a", blob([(790, 510), (900, 520), (940, 640), (880, 740), (850, 640)]), PINK)
    a.region("tail_b", blob([(800, 540), (860, 600), (860, 720), (820, 640)]), LAV)
    a.part("body", pivot=(600, 900))
    for i, x in enumerate((470, 710)):
        a.region(f"leg_far{i}", rrect(x + 20, 600, x + 70, 860, 22), CREAM)
    a.region("body", body, WHITE)
    for i, x in enumerate((440, 680)):
        a.region(f"leg{i}", rrect(x, 610, x + 56, 880, 24), WHITE)
        a.region(f"hoof{i}", rrect(x - 4, 850, x + 60, 896, 14), LAV)
    a.region("star_mark", star(620, 560, 40, 18, 5, -90, 6), SUN)
    a.part("head", pivot=(430, 480), motion="nod", amp=6)
    a.region("neck", poly([(380, 540), (500, 520), (440, 300), (340, 320)], 40), WHITE)
    a.region("head", blob([(310, 260), (400, 250), (430, 320), (370, 420), (240, 460), (200, 410), (230, 320)]), WHITE)
    a.region("horn", poly([(330, 270), (390, 250), (380, 90)], 12), SUN)
    for k in range(3):
        a.line([(342, 230 - k * 40), (380, 220 - k * 40)], 4)
    a.region("ear", poly([(400, 260), (420, 180), (440, 270)], 12), WHITE)
    a.region("mane_a", blob([(410, 250), (470, 290), (500, 400), (520, 500), (470, 480), (430, 360)]), PINK)
    a.region("mane_b", blob([(440, 300), (500, 340), (530, 450), (480, 430)]), LAV)
    a.region("muzzle", ellipse(235, 420, 50, 40, -20), PINK)
    a.eyes(330, 330, 0, 18)
    a.cheeks(300, 390, 0, 16)
    a.anchor("ride", "passenger", 610, 440)
    return a


def castle():
    a = Art("fantasy.castle")
    a.part("body", pivot=(512, 920))
    def tower(x, w, top, h, cone_c, name):
        a.region(f"{name}_wall", rect(x - w / 2, top, x + w / 2, top + h), LAV)
        a.region(f"{name}_cone", poly([(x - w / 2 - 20, top + 6), (x, top - w * 1.2), (x + w / 2 + 20, top + 6)], 14), cone_c)
        a.region(f"{name}_flag", poly([(x, top - w * 1.2 - 60), (x + 60, top - w * 1.2 - 40), (x, top - w * 1.2 - 20)], 6), SUN)
        a.line([(x, top - w * 1.2), (x, top - w * 1.2 - 64)], 5)
    tower(200, 150, 400, 500, PINK, "tower_l")
    tower(824, 150, 400, 500, PINK, "tower_r")
    a.region("keep", rect(280, 480, 744, 900), WHITE)
    a.region("battlement", union(*[rect(290 + k * 92, 430, 340 + k * 92, 490) for k in range(5)]), WHITE)
    tower(512, 180, 260, 240, PURPLE, "tower_c")
    a.region("gate", union(rect(430, 700, 594, 900), circle(512, 700, 82)), BROWN)
    for x in (470, 512, 554):
        a.line([(x, 640), (x, 900)], 5)
    a.region("window_c", union(rect(480, 330, 544, 400), circle(512, 330, 32)), SKY)
    for i, x in enumerate((200, 824)):
        a.region(f"win{i}", union(rect(176, 560, 224, 640), circle(200, 560, 24)) if i == 0 else union(rect(800, 560, 848, 640), circle(824, 560, 24)), SKY)
    a.anchor("gate", "carry", 512, 900); a.anchor("top", "seat", 512, 260)
    return a


def fairy_house():
    a = Art("fantasy.fairy_house")
    a.part("body", pivot=(512, 900))
    a.region("grass", scallop(512, 900, 300, 30, 12, 30), LEAF)
    a.region("stem", union(rrect(320, 460, 704, 900, 120)), CREAM)
    a.region("cap", blob([(512, 140), (740, 220), (860, 420), (780, 500), (512, 520), (244, 500), (164, 420), (284, 220)]), PINK)
    for i, (x, y, r) in enumerate([(400, 300, 36), (600, 260, 30), (700, 400, 28), (310, 420, 24), (512, 400, 34)]):
        a.region(f"spot{i}", circle(x, y, r), WHITE)
    a.region("door", union(rrect(450, 720, 574, 900, 12), circle(512, 720, 62)), TURQ)
    a.ink(circle(552, 810, 8))
    a.region("window", circle(400, 610, 44), SKY)
    a.line([(356, 610), (444, 610)], 4); a.line([(400, 566), (400, 654)], 4)
    a.region("ladder_flower", union(*[circle(660 + 22 * math.cos(math.radians(k * 72)), 620 + 22 * math.sin(math.radians(k * 72)), 18) for k in range(5)]), SUN)
    a.region("chimney", rrect(640, 110, 690, 210, 10), BROWN)
    a.anchor("door", "carry", 512, 900)
    return a


def magic_tree():
    a = Art("fantasy.magic_tree")
    a.part("body", pivot=(512, 920))
    a.region("roots", union(thick([(440, 880), (330, 930)], 40), thick([(584, 880), (700, 930)], 40)), BROWN)
    a.region("trunk", poly([(420, 900), (604, 900), (570, 520), (640, 420), (560, 440), (512, 380), (464, 440), (384, 420), (454, 520)], 30), BROWN)
    a.region("face_hole", ellipse(512, 680, 60, 80), HAIR1)
    a.part("crown", parent="body", pivot=(512, 420), motion="sway", amp=4)
    a.region("crown", union(circle(512, 300, 200), circle(330, 360, 140), circle(694, 360, 140)), LAV)
    a.region("crown_in", union(circle(450, 260, 90), circle(600, 300, 80)), PINK)
    for i, (x, y) in enumerate([(300, 320), (700, 300), (512, 180), (420, 420), (620, 430)]):
        a.region(f"glow{i}", star(x, y, 34, 15, 5, -90, 5), SUN)
    a.eyes(512, 670, 60, 12, lids=True)
    a.anchor("base", "plant", 512, 900); a.anchor("hole", "carry", 512, 680)
    return a


def friendly_monster():
    a = Art("fantasy.friendly_monster")
    a.part("body", pivot=(512, 920), motion="squash", amp=8)
    fur = blob(ring_points(512, 560, 330, 300, 14), 0.9)
    a.region("fur", fur, TURQ)
    a.region("belly", ellipse(512, 660, 170, 160), MINT)
    a.region("horn_l", poly([(360, 300), (330, 160), (420, 280)], 14), SUN)
    a.region("horn_r", poly([(664, 300), (694, 160), (604, 280)], 14), SUN)
    a.region("eye_white", ellipse(512, 470, 110, 90), WHITE)
    a.ink(circle(520, 480, 44)); a.shine(circle(536, 462, 15))
    a.region("mouth", union(inter(ellipse(512, 610, 120, 80), rect(380, 610, 650, 700))), DGREY)
    a.region("tooth_l", poly([(450, 610), (490, 610), (470, 650)], 6), WHITE)
    a.region("tooth_r", poly([(534, 610), (574, 610), (554, 650)], 6), WHITE)
    for i, x in enumerate((400, 624)):
        a.region(f"foot{i}", ellipse(x, 880, 80, 40), TURQ)
    a.region("arm_l", ellipse(200, 600, 50, 90, 30), TURQ)
    a.region("arm_r", ellipse(824, 560, 50, 90, -40), TURQ)
    a.anchor("ride", "passenger", 512, 880)
    return a


def flying_cat():
    a = Art("fantasy.flying_cat")
    a.part("wing_back", pivot=(560, 440), motion="flap", amp=16)
    a.region("wing_back", blob([(560, 440), (680, 260), (800, 230), (760, 350), (820, 380), (700, 480)]), SKY)
    a.part("tail", pivot=(700, 560), motion="wag", amp=12)
    a.region("tail", thick([(700, 580), (820, 560), (880, 460)], 44), GREY)
    a.part("body", pivot=(512, 560), motion="float", amp=12)
    a.region("body", ellipse(560, 580, 170, 100), GREY)
    a.region("legs", union(ellipse(450, 670, 40, 26), ellipse(660, 670, 40, 26)), WHITE)
    a.part("wing_front", parent="body", pivot=(560, 480), motion="flap", amp=-16)
    a.region("wing", blob([(540, 500), (620, 330), (760, 300), (720, 420), (780, 460), (640, 540)]), WHITE)
    for k in range(2):
        a.line([(600 + k * 50, 470), (680 + k * 30, 380)], 5)
    a.part("head", parent="body", pivot=(400, 520), motion="tilt", amp=6)
    a.region("ear_l", poly([(250, 380), (270, 270), (340, 350)], 16), GREY)
    a.region("ear_r", poly([(420, 340), (470, 260), (490, 370)], 16), GREY)
    a.region("head", ellipse(370, 460, 140, 120), GREY)
    a.region("muzzle", ellipse(350, 510, 60, 40), WHITE)
    a.region("collar", rrect(420, 520, 520, 560, 16), CORAL)
    a.eyes(360, 450, 110, 18)
    a.ink(poly([(336, 494), (364, 494), (350, 510)], 6))
    a.cheeks(360, 500, 190, 14)
    a.part("sparkle", pivot=(200, 300), motion="twinkle", amp=8)
    a.region("sparkle1", star(160, 300, 40, 12, 4, -90, 3), SUN)
    a.region("sparkle2", star(240, 220, 26, 8, 4, -90, 2), SUN)
    a.anchor("ride", "passenger", 560, 480)
    return a


def cloud_house():
    a = Art("fantasy.cloud_house")
    a.part("cloud", pivot=(512, 760), motion="drift", amp=12)
    a.region("cloud", union(circle(300, 760, 120), circle(480, 720, 150), circle(680, 750, 130), rrect(200, 740, 820, 870, 60)), WHITE)
    a.region("cloud_shade", inter(union(circle(300, 760, 120), circle(480, 720, 150), circle(680, 750, 130), rrect(200, 740, 820, 870, 60)), rect(150, 820, 900, 900)), SKY)
    a.part("house", parent="cloud", pivot=(512, 640))
    a.region("walls", rrect(360, 400, 664, 650, 20), PEACH)
    a.region("roof", union(poly([(320, 420), (512, 240), (704, 420)], 30)), SKY)
    a.region("door", union(rrect(470, 520, 554, 650, 10), circle(512, 520, 42)), CORAL)
    a.region("window", circle(412, 500, 34), SUN)
    a.region("window2", circle(612, 500, 34), SUN)
    a.region("balloon", circle(760, 220, 60), PINK)
    a.line([(760, 280), (680, 400)], 4)
    a.anchor("porch", "seat", 512, 650)
    return a


def magic_carpet():
    a = Art("fantasy.magic_carpet")
    a.part("body", pivot=(512, 560), motion="wave", amp=10)
    carpet = blob([(140, 520), (300, 470), (512, 520), (720, 470), (884, 520), (860, 640), (700, 600), (512, 650), (320, 600), (160, 640)], 0.8)
    a.region("carpet", carpet, RED)
    a.region("border", diff(carpet, scale(carpet, 0.86, 0.7, 512, 560)), SUN)
    a.region("medallion", ellipse(512, 560, 90, 36), TURQ)
    a.region("diamond_l", poly([(300, 560), (340, 530), (380, 560), (340, 590)], 6), LAV)
    a.region("diamond_r", poly([(644, 560), (684, 530), (724, 560), (684, 590)], 6), LAV)
    for i, x in enumerate((140, 884)):
        for k in range(4):
            a.line([(x + (i * 2 - 1) * 0, 540 + k * 28), (x + (i * 2 - 1) * 50, 550 + k * 28)], 5)
    a.part("sparkles", pivot=(512, 800))
    for i, (x, y) in enumerate([(300, 760), (512, 820), (720, 760)]):
        a.region(f"sparkle{i}", star(x, y, 40, 12, 4, -90, 3), SUN)
    a.anchor("ride1", "passenger", 400, 500); a.anchor("ride2", "passenger", 640, 500)
    return a


def crystal():
    a = Art("fantasy.crystal")
    a.part("body", pivot=(512, 880), motion="glow", amp=0)
    a.region("rock", blob([(200, 860), (300, 780), (512, 760), (740, 780), (840, 860), (700, 900), (320, 900)]), GREY)
    a.region("small_l", poly([(270, 820), (300, 600), (340, 560), (380, 600), (380, 820)], 10), PINK)
    a.region("small_r", poly([(640, 820), (650, 640), (700, 590), (750, 640), (760, 820)], 10), SKY)
    a.region("big", poly([(400, 840), (390, 360), (512, 160), (634, 360), (624, 840)], 14), LAV)
    a.region("facet_l", poly([(400, 840), (390, 360), (512, 160), (480, 380), (490, 840)], 10), PURPLE)
    a.region("facet_r", poly([(624, 840), (634, 360), (512, 160), (560, 380), (550, 840)], 10), LAV)
    a.shine(ellipse(450, 400, 18, 60, 10))
    a.part("sparkles", pivot=(512, 300), motion="twinkle", amp=8)
    a.region("spark_a", star(260, 360, 40, 12, 4, -90, 3), SUN)
    a.region("spark_b", star(780, 300, 30, 9, 4, -90, 2), SUN)
    a.anchor("base", "plant", 512, 880)
    return a


def wand():
    a = Art("fantasy.wand")
    a.part("body", pivot=(300, 820), motion="wave", amp=16)
    a.region("handle", thick([(220, 900), (560, 460)], 56), PURPLE)
    a.region("grip", inter(thick([(220, 900), (560, 460)], 56), circle(270, 840, 110)), LAV)
    a.region("band", inter(thick([(220, 900), (560, 460)], 56), rotate(rect(380, 600, 420, 740), 37, 400, 670)), SUN)
    a.region("star", star(620, 380, 190, 84, 5, -100, 30), YELLOW)
    a.region("star_in", star(620, 380, 100, 46, 5, -100, 16), SUN)
    a.eyes(620, 370, 70, 14)
    a.smile(620, 410, 40, 0.6, 5)
    a.part("trail", pivot=(800, 200), motion="twinkle", amp=8)
    for i, (x, y, r) in enumerate([(820, 220, 40), (880, 380, 26), (760, 130, 22)]):
        a.region(f"sparkle{i}", star(x, y, r, r * 0.3, 4, -90, 2), PINK)
    a.anchor("hold", "carry", 260, 860)
    return a


def story_gate():
    a = Art("fantasy.story_gate")
    a.part("body", pivot=(512, 900))
    gate = union(rect(220, 400, 804, 900), circle(512, 400, 292))
    opening = union(rect(330, 440, 694, 900), circle(512, 440, 182))
    a.region("arch", diff(gate, opening), LAV)
    a.region("portal", opening, SKY)
    a.region("swirl", inter(opening, thick([(512, 520), (600, 560), (580, 660), (480, 680), (420, 600), (460, 500), (560, 460)], 30)), WHITE)
    for i, (x, y) in enumerate([(270, 500), (754, 500), (270, 700), (754, 700)]):
        a.region(f"stone{i}", rrect(x - 40, y - 40, x + 40, y + 40, 14), PURPLE)
    a.region("keystone", poly([(470, 110), (554, 110), (540, 200), (484, 200)], 10), SUN)
    a.region("book", union(poly([(420, 240), (512, 260), (512, 320), (420, 300)], 6), poly([(604, 240), (512, 260), (512, 320), (604, 300)], 6)), WHITE)
    a.region("vine", union(*[circle(240 + k * 18, 420 - k * 40, 20) for k in range(6)]), LEAF)
    a.region("step", rrect(300, 880, 724, 920, 10), GREY)
    a.anchor("portal", "carry", 512, 860)
    return a


ALL = [dragon, unicorn, castle, fairy_house, magic_tree, friendly_monster, flying_cat, cloud_house, magic_carpet, crystal, wand, story_gate]
