from colora_art import *
from pal import *
from subjects.vehicles import wheel


def tracks(a, l, r, y, h=90, color=DGREY):
    a.region("track", rrect(l, y, r, y + h, h / 2), color)
    n = 4
    for k in range(n):
        x = l + h / 2 + k * (r - l - h) / (n - 1)
        a.region(f"roller{k}", circle(x, y + h / 2, h * 0.3), GREY)


def excavator():
    a = Art("machines.excavator")
    a.part("body", pivot=(560, 860))
    tracks(a, 340, 860, 780, 100)
    a.region("base", rrect(400, 720, 820, 790, 20), DGREY)
    a.region("house", rrect(420, 500, 840, 730, 40), YELLOW)
    a.region("cab", rrect(600, 360, 800, 600, 30), YELLOW)
    a.region("cab_glass", rrect(630, 390, 770, 520, 22), SKY)
    a.region("stripe", rect(424, 640, 836, 670), DGREY)
    a.region("exhaust", rrect(460, 430, 490, 510, 10), GREY)
    a.part("boom", parent="body", pivot=(500, 560), motion="dig", amp=10)
    a.region("boom", thick([(500, 560), (330, 300), (220, 300)], 64), ORANGE)
    a.region("piston", thick([(470, 640), (380, 420)], 22), GREY)
    a.part("bucket", parent="boom", pivot=(220, 320), motion="scoop", amp=16)
    a.region("stick", thick([(220, 300), (190, 520)], 50), ORANGE)
    a.region("bucket", poly([(130, 500), (260, 500), (240, 620), (110, 600)], 24), DGREY)
    for x in (130, 170, 210):
        a.region(f"tooth{x}", poly([(x, 600), (x + 30, 605), (x + 14, 640)], 6), GREY)
    a.anchor("bucket", "carry", 190, 560); a.anchor("cab", "passenger", 700, 450)
    return a


def crane():
    a = Art("machines.crane")
    a.part("body", pivot=(400, 940))
    a.region("base", rrect(250, 860, 560, 940, 20), DGREY)
    a.region("tower", rect(360, 220, 450, 870), YELLOW)
    for k in range(7):
        y = 230 + k * 90
        a.line([(360, y), (450, y + 90)], 6); a.line([(450, y), (360, y + 90)], 6)
    a.region("cab", rrect(430, 250, 560, 360, 20), RED)
    a.region("cab_glass", rrect(460, 270, 540, 330, 14), SKY)
    a.part("jib", parent="body", pivot=(405, 200), motion="swing", amp=4)
    a.region("jib", rect(120, 180, 900, 230), YELLOW)
    for k in range(9):
        x = 120 + k * 86
        a.line([(x, 180), (x + 86, 230)], 5)
    a.region("counter", rrect(120, 220, 250, 310, 16), DGREY)
    a.region("apex", poly([(380, 180), (430, 180), (405, 90)], 10), YELLOW)
    a.line([(405, 100), (850, 190)], 5); a.line([(405, 100), (150, 190)], 5)
    a.part("hook", parent="jib", pivot=(800, 230), motion="swing", amp=8)
    a.region("trolley", rrect(770, 225, 830, 255, 8), DGREY)
    a.line([(790, 255), (790, 520)], 5); a.line([(810, 255), (810, 520)], 5)
    a.region("block", rrect(760, 520, 840, 580, 14), RED)
    a.region("hook", diff(thick(arc(800, 620, 34, 34, 0, 270), 20), P()), GREY)
    a.region("crate", rrect(720, 680, 880, 800, 14), BROWN)
    a.line([(720, 680), (880, 800)], 5); a.line([(880, 680), (720, 800)], 5)
    a.anchor("hook", "carry", 800, 660)
    return a


def tractor():
    a = Art("machines.tractor")
    a.part("body", pivot=(512, 820))
    a.region("hood", rrect(480, 470, 860, 680, 40), RED)
    a.region("grille", rrect(820, 500, 880, 650, 16), GREY)
    for y in (530, 570, 610):
        a.line([(830, y), (870, y)], 5)
    a.region("exhaust", rrect(700, 330, 730, 480, 12), DGREY)
    a.region("exhaust_cap", rrect(690, 320, 740, 345, 8), DGREY)
    a.region("cab", poly([(240, 300), (480, 300), (520, 680), (230, 680)], 24), RED)
    a.region("roof", rrect(210, 270, 510, 320, 20), WHITE)
    a.region("window", poly([(280, 340), (450, 340), (470, 500), (280, 500)], 20), SKY)
    a.region("seat", rrect(290, 440, 370, 500, 14), DGREY)
    a.region("step", rrect(420, 680, 520, 710, 10), DGREY)
    wheel(a, "wheel_b", 340, 720, 170, tire=DGREY, hub=SUN, spokes=0)
    a.part("body")
    for k in range(12):
        t = math.radians(k * 30)
        a.region(f"tread{k}", rotate(rrect(340 - 22, 720 - 186, 340 + 22, 720 - 160, 8), k * 30, 340, 720), DGREY)
    wheel(a, "wheel_f", 760, 780, 100, tire=DGREY, hub=SUN)
    a.anchor("driver", "passenger", 330, 440); a.anchor("hitch", "carry", 200, 640)
    return a


def robot():
    a = Art("machines.robot")
    a.part("body", pivot=(512, 940))
    for i, x in enumerate((420, 604)):
        a.region(f"leg{i}", rrect(x - 36, 720, x + 36, 880, 24), GREY)
        a.region(f"foot{i}", rrect(x - 66, 870, x + 66, 930, 26), TURQ)
    a.region("torso", rrect(330, 450, 694, 760, 60), SKY)
    a.region("panel", rrect(420, 520, 604, 680, 30), WHITE)
    a.region("heart", heart(512, 590, 40), CORAL)
    for i, x in enumerate((450, 512, 574)):
        a.region(f"button{i}", circle(x, 650, 16), [SUN, LEAF, LAV][i])
    a.part("arm_l", parent="body", pivot=(330, 500), motion="dance", amp=20)
    a.region("arm_l", thick([(330, 500), (250, 600), (230, 690)], 56), GREY)
    a.region("claw_l", diff(circle(230, 710, 50), poly([(230, 710), (190, 790), (270, 790)])), TURQ)
    a.part("arm_r", parent="body", pivot=(694, 500), motion="dance", amp=-20)
    a.region("arm_r", thick([(694, 500), (780, 420), (800, 330)], 56), GREY)
    a.region("claw_r", diff(circle(800, 310, 50), poly([(800, 310), (760, 230), (840, 230)])), TURQ)
    a.part("head", parent="body", pivot=(512, 450), motion="tilt", amp=6)
    a.region("neck", rect(480, 400, 544, 470), GREY)
    a.region("antenna", thick([(512, 210), (512, 140)], 14), GREY)
    a.region("antenna_ball", circle(512, 130, 30), CORAL)
    a.region("head", rrect(370, 200, 654, 420, 70), SKY)
    a.region("ear_l", rrect(340, 260, 380, 360, 14), TURQ)
    a.region("ear_r", rrect(644, 260, 684, 360, 14), TURQ)
    a.region("visor", rrect(410, 250, 614, 350, 44), NAVYL)
    a.shine(circle(460, 300, 26)); a.shine(circle(564, 300, 26))
    a.ink(circle(464, 304, 12)); a.ink(circle(568, 304, 12))
    a.smile(512, 380, 70, 0.4, 7)
    a.anchor("hand", "carry", 800, 300); a.anchor("ride", "passenger", 512, 930)
    return a


def bulldozer():
    a = Art("machines.bulldozer")
    a.part("body", pivot=(512, 860))
    tracks(a, 260, 760, 770, 110)
    a.region("body", rrect(300, 560, 740, 780, 36), ORANGE)
    a.region("cab", rrect(420, 330, 660, 580, 30), ORANGE)
    a.region("glass", rrect(450, 360, 630, 500, 22), SKY)
    a.region("roof", rrect(400, 310, 680, 350, 14), DGREY)
    a.region("exhaust", rrect(340, 440, 370, 570, 10), DGREY)
    a.part("blade", parent="body", pivot=(800, 700), motion="push", amp=12)
    a.region("arm", thick([(740, 680), (840, 700)], 40), DGREY)
    a.region("blade", blob([(820, 520), (920, 540), (950, 700), (930, 880), (820, 880), (860, 700)], 0.8), YELLOW)
    a.line([(880, 580), (900, 700), (880, 840)], 5)
    a.anchor("blade", "carry", 900, 700); a.anchor("cab", "passenger", 540, 430)
    return a


def cement_mixer():
    a = Art("machines.cement_mixer")
    a.part("body", pivot=(512, 820))
    a.region("chassis", rrect(120, 650, 900, 720, 20), DGREY)
    a.region("cab", poly([(680, 420), (840, 420), (900, 540), (900, 680), (680, 680)], 30), BLUE)
    a.region("window", poly([(700, 450), (820, 450), (860, 540), (700, 540)], 18), SKY)
    a.region("chute", poly([(120, 520), (180, 520), (110, 640), (70, 620)], 12), GREY)
    a.part("drum", parent="body", pivot=(400, 520), motion="spin", amp=60)
    drum = blob([(170, 520), (240, 400), (420, 360), (620, 420), (660, 520), (620, 620), (420, 680), (240, 640)], 0.9)
    a.region("drum", drum, ORANGE)
    for i, x in enumerate((280, 400, 520)):
        a.region(f"swirl{i}", inter(drum, rotate(rrect(x - 26, 340, x + 26, 700, 20), 25, x, 520)), WHITE)
    a.part("body")
    a.region("stand", poly([(300, 600), (520, 600), (480, 660), (340, 660)], 10), DGREY)
    for i, x in enumerate((240, 420, 780)):
        wheel(a, f"wheel{i}", x, 740, 74)
    a.anchor("load", "carry", 420, 380)
    return a


def road_roller():
    a = Art("machines.road_roller")
    a.part("body", pivot=(512, 820))
    a.region("frame", rrect(200, 560, 700, 700, 30), SUN)
    a.region("cab", poly([(380, 330), (620, 330), (640, 570), (360, 570)], 24), SUN)
    a.region("canopy", rrect(340, 300, 680, 350, 18), CORAL)
    a.region("glass", rrect(410, 370, 590, 500, 18), SKY)
    a.region("yoke", poly([(700, 560), (800, 560), (820, 760), (700, 760)], 16), DGREY)
    a.part("drum", parent="body", pivot=(780, 760), motion="spin", amp=360)
    a.region("drum", rrect(640, 620, 920, 900, 140), GREY)
    a.region("drum_end", circle(780, 760, 70), DGREY)
    a.ink(circle(780, 760, 14))
    a.part("body")
    wheel(a, "wheel_b", 300, 760, 120, tire=DGREY, hub=SUN)
    a.anchor("driver", "passenger", 500, 450)
    return a


def forklift():
    a = Art("machines.forklift")
    a.part("body", pivot=(512, 820))
    a.region("counterweight", rrect(150, 520, 300, 760, 30), DGREY)
    a.region("body", rrect(260, 560, 640, 760, 30), ORANGE)
    a.region("cage", union(thick([(300, 560), (330, 300)], 20), thick([(600, 560), (580, 300)], 20), thick([(320, 300), (590, 300)], 20)), DGREY)
    a.region("seat", rrect(360, 470, 460, 560, 20), DGREY)
    a.region("steer", thick([(520, 480), (560, 540)], 14), DGREY)
    a.region("mast", rrect(650, 220, 700, 780, 12), GREY)
    a.part("forks", parent="body", pivot=(700, 640), motion="lift", amp=40)
    a.region("carriage", rrect(690, 560, 730, 700, 10), GREY)
    a.region("forks", union(rrect(700, 680, 900, 706, 8), rrect(700, 640, 900, 660, 8)), GREY)
    a.region("box", rrect(740, 500, 890, 640, 14), BROWN)
    a.region("box_tape", rect(805, 500, 825, 640), SAND)
    a.part("body")
    wheel(a, "wheel_b", 280, 780, 70)
    wheel(a, "wheel_f", 580, 780, 80)
    a.anchor("forks", "carry", 800, 650); a.anchor("driver", "passenger", 410, 470)
    return a


def harvester():
    a = Art("machines.harvester")
    a.part("body", pivot=(512, 820))
    a.region("body", rrect(300, 420, 820, 720, 40), GREEN)
    a.region("cab", rrect(560, 240, 780, 440, 26), GREEN)
    a.region("glass", rrect(590, 270, 750, 400, 18), SKY)
    a.region("auger", union(thick([(380, 440), (260, 300), (150, 290)], 40)), YELLOW)
    a.region("tank", rrect(330, 330, 540, 440, 20), YELLOW)
    a.region("header", rrect(780, 620, 960, 760, 24), YELLOW)
    a.part("reel", parent="body", pivot=(880, 600), motion="spin", amp=180)
    a.region("reel", diff(circle(880, 600, 90), circle(880, 600, 70)), RED)
    for k in range(4):
        a.line([(880, 600), (880 + 70 * math.cos(math.radians(k * 45)), 600 + 70 * math.sin(math.radians(k * 45)))], 6)
        a.line([(880, 600), (880 - 70 * math.cos(math.radians(k * 45)), 600 - 70 * math.sin(math.radians(k * 45)))], 6)
    a.part("body")
    wheel(a, "wheel_b", 400, 760, 90)
    wheel(a, "wheel_f", 680, 740, 120)
    a.anchor("tank", "carry", 430, 330)
    return a


def windmill():
    a = Art("machines.windmill")
    a.part("body", pivot=(512, 940))
    a.region("tower", poly([(400, 940), (624, 940), (580, 380), (444, 380)], 20), CREAM)
    a.region("roof", poly([(420, 400), (604, 400), (512, 260)], 20), RED)
    a.region("door", rrect(470, 800, 554, 940, 40), BROWN)
    a.region("window", circle(512, 600, 40), SKY)
    a.region("grass", scallop(512, 940, 260, 20, 12, 26), LEAF)
    a.part("sails", parent="body", pivot=(512, 360), motion="spin", amp=120)
    for i in range(4):
        sail = union(rrect(504, 90, 520, 360, 6), rrect(524, 100, 600, 310, 10))
        a.region(f"sail{i}", rotate(sail, i * 90 + 20, 512, 360), WHITE if i % 2 else SKY)
        for y in (160, 220, 280):
            a.line(rotate(polyline([(524, y), (600, y)]), i * 90 + 20, 512, 360), 4)
    a.region("hub", circle(512, 360, 36), DGREY)
    a.anchor("door", "carry", 512, 900)
    return a


def conveyor():
    a = Art("machines.conveyor")
    a.part("body", pivot=(512, 860))
    a.region("leg_l", rrect(200, 600, 240, 900, 12), DGREY)
    a.region("leg_r", rrect(784, 600, 824, 900, 12), DGREY)
    a.region("belt", rrect(100, 540, 924, 640, 50), DGREY)
    a.region("belt_top", rrect(130, 556, 894, 580, 12), GREY)
    for k in range(6):
        a.region(f"roller{k}", circle(160 + k * 140, 600, 28), SUN)
        a.ink(circle(160 + k * 140, 600, 6))
    a.region("hopper", poly([(60, 380), (260, 380), (220, 540), (100, 540)], 20), TURQ)
    a.region("bin", poly([(860, 680), (1000, 680), (980, 860), (880, 860)], 20), CORAL)
    a.part("cargo", parent="body", pivot=(512, 540), motion="slide", amp=60)
    a.region("box1", rrect(300, 440, 420, 550, 14), BROWN)
    a.region("ball", circle(560, 500, 48), RED)
    a.region("block", rrect(660, 450, 780, 550, 14), LAV)
    a.ink(star(720, 500, 26, 11, 5, -90, 4))
    a.anchor("start", "carry", 160, 540); a.anchor("end", "carry", 930, 680)
    return a


def submarine_robot():
    a = Art("machines.submarine_robot")
    a.part("bubbles", pivot=(300, 300), motion="rise", amp=20)
    for i, (x, y, r) in enumerate([(280, 300, 26), (230, 230, 18), (300, 180, 14)]):
        a.region(f"bubble{i}", circle(x, y, r), SKY)
    a.part("body", pivot=(512, 600), motion="bob", amp=8)
    a.region("fin_t", poly([(700, 380), (760, 300), (800, 420)], 16), CORAL)
    a.region("hull", blob([(260, 600), (340, 420), (560, 380), (760, 440), (820, 600), (740, 760), (520, 800), (320, 750)]), SUN)
    a.region("porthole", circle(470, 560, 100), GREY)
    a.region("glass", circle(470, 560, 74), SKY)
    a.eyes(470, 550, 60, 16)
    a.smile(470, 590, 40, 0.6, 6)
    a.region("light", union(rrect(560, 340, 600, 400, 10), circle(580, 330, 24)), YELLOW)
    a.region("stripe", inter(blob([(260, 600), (340, 420), (560, 380), (760, 440), (820, 600), (740, 760), (520, 800), (320, 750)]), rect(600, 380, 650, 800)), CORAL)
    a.part("prop", parent="body", pivot=(860, 600), motion="spin", amp=360)
    a.region("prop_shaft", rrect(810, 585, 870, 615, 8), DGREY)
    a.region("blade_t", ellipse(880, 550, 20, 50), TURQ)
    a.region("blade_b", ellipse(880, 650, 20, 50), TURQ)
    a.part("claw", parent="body", pivot=(380, 740), motion="grab", amp=14)
    a.region("claw_arm", thick([(380, 740), (300, 840), (220, 840)], 34), DGREY)
    a.region("claw_a", thick(arc(190, 840, 40, 40, 120, 160), 18), GREY)
    a.region("claw_b", thick(arc(190, 840, 40, 40, 80, -160), 18), GREY)
    a.anchor("claw", "carry", 190, 850); a.anchor("pilot", "passenger", 470, 560)
    return a


ALL = [excavator, crane, tractor, robot, bulldozer, cement_mixer, road_roller, forklift, harvester, windmill, conveyor, submarine_robot]
