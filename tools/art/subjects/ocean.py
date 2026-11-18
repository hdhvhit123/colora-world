from colora_art import *
from pal import *


def _fish_tail(x, y, w, h, flip=False):
    s = -1 if flip else 1
    return poly([(x, y), (x + s * w, y - h), (x + s * w * 0.8, y), (x + s * w, y + h)], 20)


def clownfish():
    a = Art("ocean.clownfish")
    a.part("tail", pivot=(720, 512), motion="wag", amp=18)
    a.region("tail", _fish_tail(700, 512, 180, 130), ORANGE)
    a.part("body", pivot=(512, 512), motion="swim", amp=10)
    body = ellipse(470, 512, 270, 180)
    a.region("fin_top", blob([(380, 350), (480, 250), (600, 280), (620, 360)]), ORANGE)
    a.region("body", body, ORANGE)
    for i, x in enumerate((340, 520, 670)):
        band = inter(body, blob([(x - 40, 300), (x + 30, 310), (x + 50, 512), (x + 30, 720), (x - 40, 730), (x - 10, 512)]))
        a.region(f"band{i}", band, WHITE)
    a.region("fin_side", blob([(440, 560), (520, 540), (540, 640), (460, 620)]), ORANGE)
    a.eyes(290, 470, 0, 30)
    a.smile(260, 560, 60, 0.4, 7)
    a.anchor("swim", "aquatic", 470, 512)
    return a


def turtle():
    a = Art("ocean.turtle")
    a.part("flippers", pivot=(512, 600), motion="paddle", amp=14)
    a.region("flip_fl", ellipse(330, 720, 100, 44, 30), LEAF)
    a.region("flip_fr", ellipse(690, 730, 90, 40, -30), LEAF)
    a.region("flip_bl", ellipse(330, 380, 90, 38, -30), LEAF)
    a.region("flip_br", ellipse(700, 380, 80, 36, 30), LEAF)
    a.region("tail", poly([(780, 560), (850, 540), (780, 600)], 12), LEAF)
    a.part("body", pivot=(512, 560), motion="swim", amp=8)
    shell = ellipse(540, 560, 260, 200)
    a.region("shell", shell, GREEN)
    hexes = [(540, 560), (420, 500), (660, 500), (420, 630), (660, 630), (540, 420), (540, 700)]
    for i, (x, y) in enumerate(hexes):
        a.region(f"scute{i}", inter(shell, poly([(x + 56 * math.cos(math.radians(k * 60)), y + 56 * math.sin(math.radians(k * 60))) for k in range(6)], 14)), OLIVE)
    a.region("rim", diff(shell, ellipse(540, 560, 230, 172)), SUN)
    a.part("head", parent="body", pivot=(300, 560), motion="peek", amp=8)
    a.region("head", ellipse(230, 540, 110, 90), LEAF)
    a.eyes(200, 520, 70, 16)
    a.smile(200, 570, 50, 0.4, 6)
    a.cheeks(200, 560, 120, 12)
    a.anchor("ride", "passenger", 540, 380); a.anchor("swim", "aquatic", 540, 560)
    return a


def dolphin():
    a = Art("ocean.dolphin")
    a.part("tail", pivot=(820, 380), motion="wag", amp=14)
    a.region("fluke", union(ellipse(880, 330, 60, 26, -40), ellipse(890, 410, 60, 26, 30)), BLUE)
    a.part("body", pivot=(512, 520), motion="leap", amp=20)
    body = blob([(120, 560), (200, 470), (380, 400), (600, 360), (780, 340), (850, 380), (760, 460), (580, 560), (380, 640), (220, 640)], 0.9)
    a.region("body", body, BLUE)
    a.region("belly", inter(body, blob([(140, 580), (300, 560), (480, 560), (640, 520), (520, 660), (220, 680)])), SKY)
    a.region("fin", poly([(520, 380), (560, 240), (640, 370)], 24), BLUE)
    a.region("flipper", blob([(380, 600), (440, 620), (420, 720), (360, 680)]), DEEPBLUE)
    a.eyes(270, 520, 0, 18)
    a.smile(170, 590, 60, 0.3, 6)
    a.part("splash", pivot=(512, 820))
    a.region("water", wave_band(800, 140, 884, 920, 6), SKY)
    a.region("drops", union(circle(300, 760, 20), circle(360, 730, 14), circle(700, 760, 18)), WHITE)
    a.anchor("ride", "passenger", 560, 380); a.anchor("swim", "aquatic", 480, 500)
    return a


def octopus():
    a = Art("ocean.octopus")
    a.part("arms", pivot=(512, 640), motion="wiggle", amp=10)
    for i in range(6):
        x = 260 + i * 100
        bend = 60 if i % 2 else -60
        arm = thick([(x + 20, 600), (x + bend * 0.5, 760), (x + bend, 860), (x + bend * 1.4, 830)], 56)
        a.region(f"arm{i}", arm, PURPLE if i % 2 else LAV)
        for k in range(2):
            a.ink(circle(x + bend * (0.3 + k * 0.35), 720 + k * 90, 7))
    a.part("body", pivot=(512, 600), motion="bob", amp=10)
    a.region("head", union(circle(512, 400, 250), ellipse(512, 560, 240, 120)), LAV)
    for i, (x, y, r) in enumerate([(420, 280, 30), (620, 300, 22), (560, 220, 18)]):
        a.region(f"spot{i}", circle(x, y, r), PINK)
    a.eyes(512, 470, 160, 30)
    a.smile(512, 560, 80, 0.5, 8)
    a.cheeks(512, 540, 260, 22)
    a.anchor("swim", "aquatic", 512, 500)
    return a


def seahorse():
    a = Art("ocean.seahorse")
    a.part("body", pivot=(512, 600), motion="bob", amp=12)
    body = blob([(470, 280), (560, 300), (600, 420), (580, 560), (520, 680), (470, 760), (440, 700), (470, 560), (430, 440), (420, 340)])
    a.region("tail", thick([(480, 740), (480, 860), (560, 890), (600, 840), (560, 800)], 56), SUN)
    a.region("body", body, SUN)
    a.region("belly", inter(body, rect(400, 380, 470, 760)) if False else blob([(560, 420), (590, 470), (570, 600), (520, 680), (500, 600), (530, 480)]), CREAM)
    for k in range(4):
        a.line([(500 + k * 4, 440 + k * 60), (570 - k * 6, 450 + k * 60)], 5)
    a.region("fin", blob([(430, 470), (350, 430), (330, 520), (420, 560)]), ORANGE)
    a.region("crest", poly([(430, 230), (360, 230), (400, 270), (340, 300), (410, 330)], 10), ORANGE)
    a.region("head", blob([(420, 220), (520, 200), (570, 280), (520, 360), (440, 360), (400, 300)]), SUN)
    a.region("snout", rrect(560, 270, 700, 320, 24), SUN)
    a.eyes(490, 280, 0, 20)
    a.cheeks(510, 330, 0, 14)
    a.part("bubbles", pivot=(720, 200), motion="rise", amp=16)
    a.region("bubble1", circle(730, 230, 24), SKY)
    a.region("bubble2", circle(780, 170, 16), SKY)
    a.anchor("swim", "aquatic", 500, 500)
    return a


def coral():
    a = Art("ocean.coral")
    a.part("body", pivot=(512, 900), motion="sway", amp=4)
    a.region("sand", ellipse(512, 900, 360, 60), SAND)
    a.region("branch_a", union(thick([(400, 880), (380, 600), (300, 420)], 50), thick([(385, 640), (470, 470), (480, 360)], 44), thick([(330, 480), (230, 400)], 36)), CORAL)
    a.region("branch_b", union(thick([(620, 880), (640, 640), (720, 460)], 50), thick([(640, 700), (560, 560)], 40), thick([(700, 500), (800, 420)], 36)), PINK)
    a.region("fan", inter(circle(512, 720, 180), rect(300, 520, 720, 720)), LAV)
    for ang in range(200, 341, 28):
        a.line([(512 + 44 * math.cos(math.radians(ang)), 720 + 44 * math.sin(math.radians(ang))), (512 + 170 * math.cos(math.radians(ang)), 720 + 170 * math.sin(math.radians(ang)))], 5)
    a.region("brain", scallop(250, 840, 90, 60, 9, 26), ORANGE)
    a.region("shell", heart(780, 860, 34), WHITE)
    a.region("pebble", ellipse(860, 890, 40, 24), GREY)
    a.anchor("reef", "plant", 512, 880)
    return a


def whale():
    a = Art("ocean.whale")
    a.part("spout", pivot=(380, 300), motion="puff", amp=12)
    a.region("spout", union(thick([(380, 320), (380, 220)], 30), circle(330, 190, 40), circle(430, 190, 40), circle(380, 160, 44)), SKY)
    a.part("tail", pivot=(780, 520), motion="wag", amp=12)
    a.region("tail", union(thick([(760, 540), (860, 440)], 70), ellipse(880, 380, 80, 34, -50), ellipse(920, 470, 80, 34, 20)), DEEPBLUE)
    a.part("body", pivot=(512, 560), motion="bob", amp=8)
    body = blob([(140, 560), (220, 400), (420, 340), (640, 400), (800, 520), (700, 680), (420, 720), (200, 680)])
    a.region("body", body, DEEPBLUE)
    a.region("belly", inter(body, rect(100, 600, 900, 800)), SKY)
    for x in range(200, 700, 60):
        a.line([(x, 640), (x + 10, 700)], 5)
    a.region("flipper", blob([(400, 620), (480, 640), (460, 760), (400, 720)]), BLUE)
    a.eyes(280, 520, 0, 20)
    a.smile(220, 600, 80, 0.4, 7)
    a.cheeks(300, 580, 0, 18)
    a.anchor("ride", "passenger", 460, 360); a.anchor("swim", "aquatic", 460, 540)
    return a


def starfish():
    a = Art("ocean.starfish")
    a.part("body", pivot=(512, 540), motion="wiggle", amp=6)
    a.region("star", star(512, 540, 360, 170, 5, -90, 70), ORANGE)
    a.region("center", star(512, 540, 160, 90, 5, -90, 40), SUN)
    for i in range(5):
        t = math.radians(-90 + i * 72)
        for k, d in enumerate((200, 260)):
            a.region(f"dot{i}_{k}", circle(512 + d * math.cos(t), 540 + d * math.sin(t), 20 - k * 4), CREAM)
    a.eyes(512, 520, 100, 22)
    a.smile(512, 580, 60, 0.6, 7)
    a.anchor("floor", "plant", 512, 540)
    return a


def crab():
    a = Art("ocean.crab")
    a.part("legs", pivot=(512, 640), motion="scuttle", amp=10)
    for side in (-1, 1):
        for k in range(3):
            x0 = 512 + side * (160 + k * 30); y0 = 600 + k * 40
            a.region(f"leg{side}_{k}", thick([(x0, y0), (x0 + side * 110, y0 + 40), (x0 + side * 140, y0 + 120)], 30), RED)
    a.part("claw_l", pivot=(330, 500), motion="pinch", amp=12)
    a.region("arm_l", thick([(380, 540), (260, 450), (240, 360)], 40), RED)
    a.region("claw_l", diff(circle(220, 300, 90), poly([(220, 300), (160, 160), (110, 220)])), CORAL)
    a.part("claw_r", pivot=(694, 500), motion="pinch", amp=-12)
    a.region("arm_r", thick([(644, 540), (764, 450), (784, 360)], 40), RED)
    a.region("claw_r", diff(circle(804, 300, 90), poly([(804, 300), (864, 160), (914, 220)])), CORAL)
    a.part("body", pivot=(512, 600))
    a.region("body", ellipse(512, 580, 230, 150), RED)
    a.region("shell_pattern", inter(ellipse(512, 580, 230, 150), ellipse(512, 640, 150, 60)), CORAL)
    a.region("stalk_l", thick([(460, 460), (440, 360)], 20), RED)
    a.region("stalk_r", thick([(564, 460), (584, 360)], 20), RED)
    a.region("eyeball_l", circle(440, 340, 40), WHITE)
    a.region("eyeball_r", circle(584, 340, 40), WHITE)
    a.ink(circle(444, 344, 18)); a.ink(circle(580, 344, 18))
    a.smile(512, 580, 90, 0.5, 8)
    a.anchor("floor", "plant", 512, 700)
    return a


def jellyfish():
    a = Art("ocean.jellyfish")
    a.part("tentacles", pivot=(512, 560), motion="wiggle", amp=12)
    for i, x in enumerate((360, 430, 512, 594, 664)):
        a.region(f"tent{i}", thick([(x, 540), (x - 30, 660), (x + 20, 760), (x - 20, 880)], 28), LAV if i % 2 else PINK)
    a.part("bell", pivot=(512, 420), motion="pulse", amp=8)
    bell = union(inter(ellipse(512, 470, 240, 230), rect(200, 200, 830, 470)), scallop(512, 480, 220, 30, 9, 30))
    a.region("bell", bell, PINK)
    a.region("bell_top", inter(bell, ellipse(512, 300, 150, 80)), ROSE)
    a.eyes(512, 420, 120, 22)
    a.smile(512, 470, 50, 0.6, 6)
    a.cheeks(512, 460, 200, 16)
    a.anchor("swim", "aquatic", 512, 450)
    return a


def seashell():
    a = Art("ocean.seashell")
    a.part("body", pivot=(512, 760), motion="open", amp=6)
    shell = union(inter(circle(512, 720, 380), rect(100, 300, 924, 720)), poly([(430, 700), (594, 700), (560, 830), (464, 830)], 24))
    a.region("shell", shell, PEACH)
    for i in range(7):
        ang = 200 + i * 20
        rib = poly([(512, 760), (512 + 400 * math.cos(math.radians(ang - 6)), 720 + 400 * math.sin(math.radians(ang - 6))), (512 + 400 * math.cos(math.radians(ang + 6)), 720 + 400 * math.sin(math.radians(ang + 6)))])
        a.region(f"rib{i}", inter(shell, rib), PINK if i % 2 else CORAL)
    a.region("hinge", poly([(380, 760), (644, 760), (600, 860), (424, 860)], 24), PEACH)
    a.anchor("floor", "plant", 512, 860)
    return a


def submarine():
    a = Art("ocean.submarine")
    a.part("body", pivot=(512, 560), motion="bob", amp=8)
    a.region("periscope", union(rrect(560, 200, 590, 340, 10), rrect(560, 190, 640, 230, 14)), DGREY)
    a.region("tower", rrect(450, 300, 660, 420, 40), RED)
    hull = rrect(140, 380, 860, 700, 160)
    a.region("hull", hull, YELLOW)
    a.region("stripe", inter(hull, rect(100, 600, 900, 640)), RED)
    for i, x in enumerate((320, 500, 680)):
        a.region(f"porthole{i}", circle(x, 520, 54), GREY)
        a.region(f"glass{i}", circle(x, 520, 38), SKY)
    a.region("light", poly([(140, 540), (40, 480), (40, 600)], 14), SUN)
    a.part("prop", parent="body", pivot=(890, 540), motion="spin", amp=360)
    a.region("blade1", ellipse(900, 480, 20, 56), TURQ)
    a.region("blade2", ellipse(900, 600, 20, 56), TURQ)
    a.region("cap", circle(890, 540, 24), DGREY)
    a.anchor("seat1", "passenger", 320, 520); a.anchor("seat2", "passenger", 500, 520); a.anchor("swim", "aquatic", 500, 540)
    return a


ALL = [clownfish, turtle, dolphin, octopus, seahorse, coral, whale, starfish, crab, jellyfish, seashell, submarine]
