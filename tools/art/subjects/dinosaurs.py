from colora_art import *
from pal import *


def _legs(a, xs, top, bottom, w=70, color=GREEN, far=False):
    for i, x in enumerate(xs):
        a.region(f"{'far' if far else 'leg'}{i}", rrect(x - w / 2, top, x + w / 2, bottom, w * 0.45), color)
        if not far:
            for k in (-1, 0, 1):
                a.line(arc(x + k * w * 0.25, bottom - 4, 8, 6, 180, 180), 4)


def biped_leg(a, name, hx, hy, fy, color, w=1.0):
    a.region(name + "_thigh", ellipse(hx, hy + 40 * w, 72 * w, 96 * w, -10), color)
    a.region(name + "_shin", rrect(hx - 30 * w, hy + 90 * w, hx + 30 * w, fy, 28 * w), color)
    a.region(name + "_foot", ellipse(hx - 14 * w, fy, 70 * w, 28 * w), color)
    for k in (-1, 0, 1):
        a.line(arc(hx - 44 * w + (k + 1) * 30 * w, fy + 2, 8, 6, 180, 180), 4)


def triceratops():
    a = Art("dinosaurs.triceratops")
    a.part("tail", pivot=(800, 560), motion="wag", amp=10)
    a.region("tail", poly([(760, 520), (940, 600), (780, 640)], 30), GREEN)
    a.part("body", pivot=(560, 760))
    _legs(a, (520, 760), 600, 800, 80, OLIVE, far=True)
    a.region("body", blob([(380, 500), (560, 420), (760, 460), (820, 580), (740, 680), (480, 700), (380, 620)]), GREEN)
    a.region("belly", inter(blob([(380, 500), (560, 420), (760, 460), (820, 580), (740, 680), (480, 700), (380, 620)]), rect(300, 630, 900, 720)), LEAF)
    _legs(a, (460, 700), 620, 830, 90, GREEN)
    a.part("head", pivot=(380, 560), motion="nod", amp=5)
    a.region("frill", scallop(330, 420, 150, 160, 9, 34), ORANGE)
    a.region("frill_in", ellipse(330, 420, 100, 110), SUN)
    a.region("horn_l", poly([(290, 360), (220, 230), (330, 340)], 14), CREAM)
    a.region("horn_r", poly([(380, 370), (360, 230), (420, 360)], 14), CREAM)
    a.region("head", blob([(200, 420), (350, 380), (420, 480), (380, 600), (220, 620), (130, 560)]), GREEN)
    a.region("nose_horn", poly([(150, 470), (110, 400), (190, 450)], 10), CREAM)
    a.region("beak", poly([(130, 500), (90, 560), (170, 590)], 16), SUN)
    a.eyes(270, 470, 0, 18)
    a.smile(200, 570, 60, 0.3, 6)
    a.anchor("ride", "passenger", 580, 430)
    return a


def stegosaurus():
    a = Art("dinosaurs.stegosaurus")
    a.part("plates", pivot=(512, 400))
    for i, (x, y, s) in enumerate([(350, 440, 0.7), (440, 380, 0.95), (540, 360, 1.05), (640, 380, 0.95), (730, 440, 0.75)]):
        a.region(f"plate{i}", poly([(x - 60 * s, y + 70), (x, y - 90 * s), (x + 60 * s, y + 70)], 22), ORANGE if i % 2 else CORAL)
    a.part("tail", pivot=(800, 560), motion="wag", amp=10)
    a.region("tail", poly([(760, 500), (960, 620), (780, 620)], 30), TURQ)
    for i, (x, y) in enumerate([(900, 560), (940, 580)]):
        a.region(f"spike{i}", poly([(x - 14, y + 10), (x + 10, y - 70), (x + 18, y + 18)], 6), CREAM)
    a.part("body", pivot=(512, 760))
    _legs(a, (380, 680), 600, 800, 70, MINT, far=True)
    a.region("body", blob([(260, 560), (420, 440), (640, 440), (800, 560), (700, 680), (380, 680)]), TURQ)
    _legs(a, (330, 620), 620, 830, 80, TURQ)
    a.part("head", pivot=(260, 580), motion="nod", amp=6)
    a.region("neck", thick([(300, 580), (200, 560)], 80), TURQ)
    a.region("head", ellipse(150, 560, 90, 64), TURQ)
    a.eyes(160, 540, 0, 14)
    a.smile(110, 580, 40, 0.3, 5)
    a.anchor("ride", "passenger", 540, 430)
    return a


def brachiosaurus():
    a = Art("dinosaurs.brachiosaurus")
    a.part("tail", pivot=(740, 700), motion="wag", amp=8)
    a.region("tail", thick([(720, 700), (860, 760), (950, 740)], 70), LAV)
    a.part("body", pivot=(560, 900))
    _legs(a, (470, 720), 700, 940, 70, PURPLE, far=True)
    a.region("body", blob([(380, 600), (560, 540), (760, 620), (780, 740), (600, 800), (420, 780)]), LAV)
    _legs(a, (420, 660), 720, 950, 80, LAV)
    a.part("neck", pivot=(420, 620), motion="sway", amp=4)
    a.region("neck", thick([(440, 640), (380, 400), (360, 200)], 100), LAV)
    for i, y in enumerate((300, 400, 500)):
        a.region(f"neck_spot{i}", inter(thick([(440, 640), (380, 400), (360, 200)], 100), circle(400 - (i - 1) * 10, y, 22)), PURPLE)
    a.part("head", parent="neck", pivot=(360, 200), motion="nod", amp=6)
    a.region("head", union(ellipse(300, 150, 100, 64), circle(350, 120, 54)), LAV)
    a.eyes(320, 120, 0, 14)
    a.smile(240, 170, 50, 0.4, 6)
    a.cheeks(280, 160, 0, 12)
    a.anchor("ride", "passenger", 600, 540)
    return a


def tyrannosaurus():
    a = Art("dinosaurs.tyrannosaurus")
    a.part("tail", pivot=(640, 600), motion="wag", amp=10)
    a.region("tail", poly([(600, 520), (960, 760), (620, 700)], 40), GREEN)
    a.part("body", pivot=(512, 900))
    biped_leg(a, "leg_far", 590, 620, 890, OLIVE)
    a.region("body", blob([(360, 440), (520, 420), (660, 560), (620, 700), (460, 720), (340, 600)]), GREEN)
    a.region("belly", blob([(380, 500), (460, 520), (520, 640), (440, 700), (360, 620)]), SUN)
    biped_leg(a, "leg", 500, 640, 900, GREEN, 1.15)
    a.part("arm", parent="body", pivot=(420, 520), motion="wave", amp=20)
    a.region("arm", thick([(420, 520), (360, 560), (340, 540)], 30), GREEN)
    a.part("head", parent="body", pivot=(400, 420), motion="nod", amp=6)
    a.region("head", blob([(260, 200), (420, 200), (480, 300), (440, 420), (300, 440), (200, 390), (180, 280)]), GREEN)
    a.region("jaw", blob([(190, 360), (320, 380), (430, 400), (380, 450), (220, 440)]), SUN)
    for x in range(220, 400, 40):
        a.region(f"tooth{x}", poly([(x, 372), (x + 24, 372), (x + 12, 396)], 4), WHITE)
    a.eyes(360, 280, 0, 22)
    a.region("spots", union(circle(300, 240, 18), circle(250, 270, 12)), OLIVE)
    a.anchor("ride", "passenger", 560, 440)
    return a


def ankylosaurus():
    a = Art("dinosaurs.ankylosaurus")
    a.part("tail", pivot=(800, 620), motion="wag", amp=14)
    a.region("tail", thick([(780, 620), (900, 600)], 50), BROWN)
    a.region("club", ellipse(930, 600, 60, 46), TAN)
    a.part("body", pivot=(512, 820))
    _legs(a, (400, 700), 680, 840, 80, HAIR2, far=True)
    shell = union(inter(ellipse(560, 660, 300, 220), rect(200, 400, 900, 700)), rrect(260, 640, 860, 700, 30))
    a.region("shell", shell, BROWN)
    for i, x in enumerate((320, 420, 520, 620, 720, 800)):
        a.region(f"spike{i}", poly([(x - 30, 680), (x, 740), (x + 30, 680)], 8), SAND)
    for i, (x, y) in enumerate([(420, 540), (560, 500), (700, 540), (490, 620), (630, 620)]):
        a.region(f"knob{i}", circle(x, y, 34), TAN)
    _legs(a, (350, 640), 690, 860, 90, BROWN)
    a.part("head", pivot=(270, 640), motion="nod", amp=5)
    a.region("head", blob([(130, 600), (240, 560), (300, 620), (260, 700), (140, 700)]), BROWN)
    a.eyes(220, 620, 0, 14)
    a.smile(170, 670, 40, 0.3, 5)
    a.anchor("ride", "passenger", 560, 450)
    return a


def parasaurolophus():
    a = Art("dinosaurs.parasaurolophus")
    a.part("tail", pivot=(700, 600), motion="wag", amp=10)
    a.region("tail", poly([(660, 540), (960, 700), (680, 680)], 40), SKY)
    a.part("body", pivot=(560, 900))
    biped_leg(a, "leg_far", 620, 620, 880, BLUE)
    a.region("body", blob([(380, 500), (560, 460), (720, 560), (660, 720), (480, 740), (380, 640)]), SKY)
    a.region("stripe", inter(blob([(380, 500), (560, 460), (720, 560), (660, 720), (480, 740), (380, 640)]), rect(300, 520, 900, 560)), BLUE)
    biped_leg(a, "leg", 540, 650, 890, SKY)
    a.region("arm", thick([(420, 580), (360, 650), (380, 690)], 34), SKY)
    a.part("head", pivot=(400, 500), motion="call", amp=6)
    a.region("neck", thick([(430, 520), (350, 330)], 90), SKY)
    a.region("crest", thick([(380, 260), (520, 170), (600, 140)], 46), ORANGE)
    a.region("head", blob([(260, 250), (380, 220), (420, 300), (360, 360), (200, 360), (170, 310)]), SKY)
    a.region("bill", ellipse(190, 330, 54, 30), SUN)
    a.eyes(330, 280, 0, 16)
    a.anchor("ride", "passenger", 560, 460)
    return a


def diplodocus():
    a = Art("dinosaurs.diplodocus")
    a.part("tail", pivot=(680, 600), motion="wag", amp=6)
    a.region("tail", thick([(660, 580), (820, 600), (980, 520)], 60), CORAL)
    a.part("body", pivot=(512, 820))
    _legs(a, (440, 640), 620, 820, 60, RED, far=True)
    a.region("body", ellipse(540, 600, 180, 110), CORAL)
    for i, x in enumerate((470, 540, 610)):
        a.region(f"plate{i}", poly([(x - 24, 500), (x, 470), (x + 24, 500)], 6), SUN)
    _legs(a, (400, 600), 640, 840, 70, CORAL)
    a.part("neck", pivot=(400, 580), motion="sway", amp=5)
    a.region("neck", thick([(400, 580), (240, 470), (90, 450)], 70), CORAL)
    a.region("head", ellipse(70, 440, 60, 40), CORAL)
    a.eyes(70, 425, 0, 12)
    a.smile(40, 455, 30, 0.3, 5)
    a.anchor("ride", "passenger", 540, 490)
    return a


def spinosaurus():
    a = Art("dinosaurs.spinosaurus")
    a.part("tail", pivot=(680, 600), motion="wag", amp=10)
    a.region("tail", poly([(660, 540), (980, 660), (660, 680)], 40), TURQ)
    a.part("body", pivot=(512, 900))
    a.region("sail", blob([(380, 480), (420, 250), (520, 200), (640, 260), (700, 480)], 0.8), CORAL)
    for x in (450, 520, 590, 650):
        a.line([(x, 470), (x - 10 + (x - 520) * 0.2, 250)], 5)
    biped_leg(a, "leg_far", 600, 620, 880, MINT)
    a.region("body", blob([(340, 480), (540, 440), (700, 540), (650, 700), (460, 720), (340, 620)]), TURQ)
    biped_leg(a, "leg", 520, 640, 890, TURQ)
    a.region("arm", thick([(400, 560), (340, 650), (360, 700)], 34), TURQ)
    a.part("head", parent="body", pivot=(360, 480), motion="nod", amp=6)
    a.region("head", blob([(380, 440), (330, 370), (230, 370), (120, 400), (60, 430), (90, 470), (240, 470), (340, 500)]), TURQ)
    a.region("crest", ellipse(260, 380, 30, 16), CORAL)
    a.eyes(290, 410, 0, 14)
    a.line([(110, 446), (250, 448)], 5)
    a.anchor("ride", "passenger", 520, 440)
    return a


def iguanodon():
    a = Art("dinosaurs.iguanodon")
    a.part("tail", pivot=(700, 560), motion="wag", amp=10)
    a.region("tail", poly([(660, 500), (980, 560), (680, 640)], 40), OLIVE)
    a.part("body", pivot=(512, 900))
    biped_leg(a, "leg_far", 640, 600, 880, GREEN)
    a.region("arm_far", rrect(300, 600, 350, 880, 22), GREEN)
    a.region("body", blob([(300, 520), (480, 440), (700, 480), (740, 600), (600, 680), (380, 660), (290, 600)]), OLIVE)
    a.region("belly", inter(blob([(300, 520), (480, 440), (700, 480), (740, 600), (600, 680), (380, 660), (290, 600)]), rect(250, 620, 800, 700)), CREAM)
    biped_leg(a, "leg", 600, 620, 900, OLIVE, 1.05)
    a.part("arm", parent="body", pivot=(360, 600), motion="wave", amp=10)
    a.region("arm", rrect(330, 600, 390, 900, 26), OLIVE)
    a.region("hand", ellipse(350, 900, 50, 24), OLIVE)
    a.region("thumb", poly([(380, 860), (430, 800), (400, 880)], 8), CREAM)
    a.part("head", parent="body", pivot=(320, 520), motion="nod", amp=6)
    a.region("neck", thick([(340, 540), (240, 420)], 80), OLIVE)
    a.region("head", blob([(170, 330), (280, 330), (310, 400), (250, 450), (110, 440), (90, 390)]), OLIVE)
    a.region("beak", ellipse(100, 420, 40, 26), SUN)
    a.eyes(230, 380, 0, 16)
    a.anchor("ride", "passenger", 520, 450)
    return a


def pachycephalosaurus():
    a = Art("dinosaurs.pachycephalosaurus")
    a.part("tail", pivot=(640, 640), motion="wag", amp=10)
    a.region("tail", poly([(600, 580), (950, 680), (620, 720)], 40), PEACH)
    a.part("body", pivot=(512, 900))
    biped_leg(a, "leg_far", 580, 660, 900, ORANGE, 0.9)
    a.region("body", ellipse(500, 620, 150, 150), PEACH)
    a.region("belly", ellipse(470, 660, 80, 100), CREAM)
    biped_leg(a, "leg", 500, 680, 910, PEACH, 0.95)
    a.region("arm", thick([(410, 560), (350, 630), (360, 680)], 30), PEACH)
    a.part("head", parent="body", pivot=(420, 470), motion="nod", amp=8)
    a.region("neck", thick([(430, 500), (380, 380)], 80), PEACH)
    a.region("face", blob([(260, 330), (420, 320), (450, 400), (380, 440), (240, 430), (210, 380)]), PEACH)
    a.region("dome", inter(circle(350, 330, 120), rect(200, 180, 500, 340)), ORANGE)
    for i, x in enumerate((240, 300, 400, 460)):
        a.region(f"knob{i}", circle(x, 330 + abs(x - 350) * 0.1, 16), SUN)
    a.eyes(340, 370, 0, 16)
    a.smile(250, 410, 40, 0.3, 5)
    a.anchor("ride", "passenger", 520, 470)
    return a


def egg():
    a = Art("dinosaurs.egg")
    a.part("nest", pivot=(512, 860))
    a.region("nest", union(ellipse(512, 820, 300, 90), scallop(512, 820, 280, 70, 14, 30)), TAN)
    for k in range(4):
        a.line([(280 + k * 150, 790), (380 + k * 150, 850)], 5)
    a.part("body", pivot=(512, 800), motion="wobble", amp=8)
    shell = ellipse(512, 520, 220, 290)
    a.region("top", inter(shell, poly([(200, 200), (830, 200), (830, 480), (720, 440), (640, 500), (560, 430), (480, 500), (400, 430), (300, 490), (200, 460)])), CREAM)
    a.region("bottom", diff(shell, poly([(200, 200), (830, 200), (830, 480), (720, 440), (640, 500), (560, 430), (480, 500), (400, 430), (300, 490), (200, 460)])), CREAM)
    for i, (x, y, r) in enumerate([(420, 330, 40), (600, 360, 30), (520, 260, 22), (420, 640, 36), (610, 640, 44), (520, 740, 26)]):
        a.region(f"spot{i}", inter(shell, circle(x, y, r)), LEAF if i % 2 else TURQ)
    a.anchor("nest", "carry", 512, 780)
    return a


def fossil():
    a = Art("dinosaurs.fossil")
    a.part("body", pivot=(512, 800))
    slab = blob([(140, 300), (500, 220), (880, 300), (920, 640), (820, 820), (300, 840), (110, 640)], 0.7)
    a.region("slab", slab, SAND)
    a.region("crack", inter(slab, poly([(700, 220), (760, 400), (720, 480), (900, 500), (920, 220)])), TAN)
    bone = lambda pts, w: thick(pts, w)
    a.region("spine", bone([(300, 500), (500, 440), (700, 470)], 30), WHITE)
    for i, x in enumerate((380, 450, 520, 590, 660)):
        a.region(f"rib{i}", bone([(x, 470), (x - 10, 620)], 22), WHITE)
    a.region("skull", union(ellipse(250, 520, 90, 64), rrect(170, 520, 320, 580, 20)), WHITE)
    a.region("eye_hole", circle(270, 500, 22), TAN)
    a.region("tail_bones", bone([(700, 470), (800, 520), (840, 600)], 22), WHITE)
    a.region("leg_bone_a", bone([(420, 620), (400, 740)], 26), WHITE)
    a.region("leg_bone_b", bone([(620, 620), (640, 740)], 26), WHITE)
    a.region("brush", union(thick([(760, 720), (900, 640)], 18), ellipse(740, 730, 30, 20, -30)), SUN)
    a.anchor("display", "carry", 512, 520)
    return a


ALL = [triceratops, stegosaurus, brachiosaurus, tyrannosaurus, ankylosaurus, parasaurolophus, diplodocus, spinosaurus, iguanodon, pachycephalosaurus, egg, fossil]
