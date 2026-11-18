from colora_art import *
from pal import *


def wheel(a, name, cx, cy, r, tire=DGREY, hub=GREY, spokes=0, part=True):
    if part:
        a.part(name, parent="body", pivot=(cx, cy), motion="spin", amp=360)
    a.region(f"{name}_tire", circle(cx, cy, r), tire)
    a.region(f"{name}_hub", circle(cx, cy, r * 0.48), hub)
    a.ink(circle(cx, cy, r * 0.12))
    for k in range(spokes):
        t = math.radians(k * 360 / spokes)
        a.line([(cx + r * 0.16 * math.cos(t), cy + r * 0.16 * math.sin(t)), (cx + r * 0.86 * math.cos(t), cy + r * 0.86 * math.sin(t))], 4)


def waves(a, y, color=SKY, name="water", left=110, right=914, bottom=900, n=6):
    a.region(name, wave_band(y, left, right, bottom, n), color)


def car():
    a = Art("vehicles.car")
    a.part("body", pivot=(512, 760))
    a.region("body", blob([(160, 660), (190, 560), (330, 520), (400, 390), (640, 380), (740, 510), (860, 560), (880, 680), (820, 760), (200, 760)], 0.8), RED)
    a.region("window_f", poly([(560, 420), (680, 420), (740, 520), (560, 520)], 26), SKY)
    a.region("window_b", poly([(420, 420), (530, 420), (530, 520), (370, 520)], 26), SKY)
    a.region("door_trim", rrect(520, 560, 700, 600, 16), CORAL)
    a.region("headlight", ellipse(850, 610, 26, 34), YELLOW)
    a.region("taillight", rrect(160, 600, 196, 650, 12), ORANGE)
    a.region("bumper", rrect(160, 700, 880, 760, 28), GREY)
    a.line([(545, 420), (545, 700)], 6)
    a.eyes(810, 600, 0, 0) if False else None
    wheel(a, "wheel_b", 330, 760, 92, spokes=0)
    wheel(a, "wheel_f", 710, 760, 92, spokes=0)
    a.anchor("seat_front", "passenger", 620, 500); a.anchor("seat_back", "passenger", 450, 500)
    return a


def train():
    a = Art("vehicles.train")
    a.part("smoke", pivot=(330, 240), motion="puff", amp=10)
    a.region("puff1", scallop(330, 200, 60, 40, 7, 30), WHITE)
    a.region("puff2", scallop(240, 120, 40, 28, 6, 22), WHITE)
    a.part("body", pivot=(512, 800))
    a.region("chimney", poly([(290, 260), (370, 260), (350, 400), (310, 400)], 14), DGREY)
    a.region("boiler", rrect(170, 400, 560, 640, 70), BLUE)
    a.region("band", rect(300, 400, 330, 640), SUN)
    a.region("cab", rrect(540, 300, 820, 660, 30), RED)
    a.region("roof", rrect(520, 270, 850, 320, 24), DGREY)
    a.region("cab_window", rrect(600, 360, 760, 480, 24), SKY)
    a.region("front", circle(170, 520, 70), GREY)
    a.region("lamp", circle(170, 520, 30), YELLOW)
    a.region("chassis", rrect(120, 640, 860, 700, 24), DGREY)
    a.region("catcher", poly([(70, 760), (130, 640), (180, 640), (180, 760)], 10), RED)
    for i, x in enumerate((280, 470, 700)):
        wheel(a, f"wheel{i}", x, 760, 80 if i < 2 else 96, tire=DGREY, hub=RED, spokes=6)
    a.part("rod", parent="body", pivot=(380, 760))
    a.region("rod", rrect(270, 748, 490, 772, 12), GREY)
    a.anchor("cab", "passenger", 680, 470); a.anchor("roof", "passenger", 400, 400)
    return a


def airplane():
    a = Art("vehicles.airplane")
    a.part("wing_back", pivot=(500, 470))
    a.region("wing_back", poly([(440, 470), (560, 470), (660, 300), (620, 290)], 30), BLUE)
    a.part("body", pivot=(512, 520))
    a.region("tail", poly([(760, 470), (900, 280), (930, 300), (890, 500)], 30), RED)
    a.region("fuselage", blob([(140, 500), (220, 420), (520, 400), (860, 440), (920, 500), (860, 560), (500, 600), (200, 590)], 0.9), WHITE)
    a.region("nose", inter(blob([(140, 500), (220, 420), (520, 400), (860, 440), (920, 500), (860, 560), (500, 600), (200, 590)], 0.9), rect(100, 380, 230, 620)), RED)
    a.region("cockpit", blob([(230, 450), (300, 430), (330, 490), (250, 500)]), SKY)
    for i, x in enumerate((400, 490, 580, 670)):
        a.region(f"window{i}", circle(x, 480, 26), SKY)
    a.region("stripe", inter(rect(300, 540, 880, 570), blob([(140, 500), (220, 420), (520, 400), (860, 440), (920, 500), (860, 560), (500, 600), (200, 590)], 0.9)), SUN)
    a.region("wing", poly([(420, 520), (580, 520), (700, 740), (640, 760)], 34), BLUE)
    a.region("stabilizer", poly([(780, 520), (880, 520), (930, 600), (900, 610)], 20), BLUE)
    a.part("propeller", parent="body", pivot=(120, 500), motion="spin", amp=360)
    a.region("blade_t", ellipse(120, 420, 22, 70), DGREY)
    a.region("blade_b", ellipse(120, 580, 22, 70), DGREY)
    a.region("spinner", circle(120, 500, 34), SUN)
    a.anchor("pilot", "passenger", 270, 450)
    return a


def bicycle():
    a = Art("vehicles.bicycle")
    for name, cx in (("wheel_b", 280), ("wheel_f", 760)):
        a.part(name, parent="frame", pivot=(cx, 700), motion="spin", amp=360)
        a.region(f"{name}_tire", diff(circle(cx, 700, 160), circle(cx, 700, 126)), DGREY)
        a.region(f"{name}_rim", diff(circle(cx, 700, 126), circle(cx, 700, 112)), GREY)
        a.ink(circle(cx, 700, 14))
        for k in range(8):
            t = math.radians(k * 45)
            a.line([(cx, 700), (cx + 112 * math.cos(t), 700 + 112 * math.sin(t))], 4)
    a.part("frame", pivot=(512, 700))
    frame = union(thick([(280, 700), (470, 700), (620, 480), (400, 480), (280, 700)], 30), thick([(470, 700), (400, 480), (380, 420)], 30), thick([(620, 480), (760, 700)], 30), thick([(640, 420), (600, 380)], 26))
    a.region("frame", frame, TURQ)
    a.region("seat", blob([(320, 400), (430, 390), (440, 420), (330, 430)]), BROWN)
    a.region("handle", thick([(560, 380), (640, 360), (690, 380)], 22), DGREY)
    a.region("basket", poly([(660, 400), (800, 400), (780, 500), (680, 500)], 16), SUN)
    for x in (700, 740):
        a.line([(x, 410), (x - 4, 490)], 4)
    a.region("pedal", rrect(440, 690, 500, 720, 10), DGREY)
    a.anchor("rider", "passenger", 400, 380); a.anchor("basket", "passenger", 730, 400)
    return a


def bus():
    a = Art("vehicles.bus")
    a.part("body", pivot=(512, 760))
    a.region("body", rrect(100, 330, 924, 760, 70), YELLOW)
    a.region("roof", rrect(140, 300, 884, 350, 24), ORANGE)
    a.region("sign", rrect(380, 250, 640, 310, 20), WHITE)
    for i, x in enumerate((140, 300, 460)):
        a.region(f"window{i}", rrect(x, 380, x + 140, 520, 24), SKY)
    a.region("door", rrect(630, 380, 750, 720, 20), SKY)
    a.line([(690, 380), (690, 720)], 6)
    a.region("windshield", rrect(780, 380, 900, 540, 26), SKY)
    a.region("stripe", rect(104, 560, 920, 600), CORAL)
    a.region("headlight", circle(890, 650, 28), WHITE)
    a.region("bumper", rrect(90, 700, 934, 760, 26), GREY)
    wheel(a, "wheel_b", 260, 760, 84)
    wheel(a, "wheel_f", 760, 760, 84)
    a.anchor("seat1", "passenger", 210, 520); a.anchor("seat2", "passenger", 370, 520); a.anchor("seat3", "passenger", 530, 520)
    return a


def boat():
    a = Art("vehicles.boat")
    a.part("water", pivot=(512, 800))
    waves(a, 760)
    a.part("body", pivot=(512, 760), motion="bob", amp=8)
    a.region("cabin", rrect(360, 430, 640, 620, 30), WHITE)
    a.region("cabin_roof", rrect(340, 400, 660, 450, 22), CORAL)
    a.region("porthole1", circle(430, 530, 36), SKY)
    a.region("porthole2", circle(570, 530, 36), SKY)
    a.region("mast", rect(500, 250, 520, 410), BROWN)
    a.region("flag", poly([(520, 250), (640, 290), (520, 330)], 10), TURQ)
    a.region("hull", poly([(140, 600), (900, 600), (780, 800), (250, 800)], 40), BLUE)
    a.region("hull_stripe", inter(poly([(140, 600), (900, 600), (780, 800), (250, 800)], 40), rect(100, 640, 940, 680)), WHITE)
    a.region("lifebuoy", diff(circle(300, 700, 44), circle(300, 700, 20)), RED)
    a.anchor("deck1", "passenger", 260, 600); a.anchor("deck2", "passenger", 760, 600)
    return a


def tram():
    a = Art("vehicles.tram")
    a.part("rails", pivot=(512, 860))
    a.region("rail", rrect(60, 840, 964, 880, 16), GREY)
    a.part("body", pivot=(512, 820))
    a.region("pantograph", union(thick([(440, 310), (512, 200), (584, 310)], 14), thick([(470, 200), (554, 200)], 14)), DGREY)
    a.region("roof", rrect(140, 300, 884, 360, 30), WHITE)
    a.region("body", rrect(120, 340, 904, 800, 50), TURQ)
    a.region("front", inter(rrect(120, 340, 904, 800, 50), rect(780, 340, 904, 800)), SKY)
    for i, x in enumerate((160, 300, 440)):
        a.region(f"window{i}", rrect(x, 400, x + 110, 540, 20), SKY)
    a.region("door", rrect(590, 400, 720, 780, 16), SUN)
    a.line([(655, 400), (655, 780)], 6)
    a.region("light", circle(860, 700, 26), YELLOW)
    a.region("stripe", rect(124, 600, 590, 640), WHITE)
    for i, x in enumerate((260, 760)):
        wheel(a, f"wheel{i}", x, 820, 48)
    a.anchor("seat1", "passenger", 215, 540); a.anchor("seat2", "passenger", 355, 540)
    return a


def hot_air_balloon():
    a = Art("vehicles.hot_air_balloon")
    a.part("body", pivot=(512, 900), motion="bob", amp=10)
    env = blob([(512, 90), (700, 160), (770, 360), (680, 560), (580, 640), (444, 640), (344, 560), (254, 360), (324, 160)])
    a.region("rope_l", thick([(450, 640), (440, 780)], 10), BROWN)
    a.region("rope_r", thick([(574, 640), (584, 780)], 10), BROWN)
    cols = [RED, SUN, TURQ, SUN, RED]
    xs = [200, 380, 470, 554, 644, 830]
    for i in range(5):
        g = blob([(512, 92), (xs[i] + (xs[i + 1] - xs[i]) / 2, 360), (512, 640)], 1) if False else None
        lens = inter(env, poly([(512, 60), (xs[i + 1] if i >= 2 else xs[i], 60), (xs[i + 1], 700), (xs[i], 700)] if False else [(xs[i], 40), (xs[i + 1], 40), (xs[i + 1], 700), (xs[i], 700)]))
        a.region(f"gore{i}", lens, cols[i])
    a.region("band", inter(env, rect(200, 470, 830, 520)), WHITE)
    a.region("skirt", poly([(450, 630), (574, 630), (560, 680), (464, 680)], 10), CORAL)
    a.region("basket", rrect(420, 770, 604, 900, 24), BROWN)
    a.region("basket_rim", rrect(408, 760, 616, 800, 16), TAN)
    a.line([(440, 840), (584, 840)], 5)
    a.anchor("basket", "passenger", 512, 770)
    return a


def helicopter():
    a = Art("vehicles.helicopter")
    a.part("body", pivot=(512, 700))
    a.region("tail_boom", poly([(620, 470), (880, 440), (890, 490), (620, 560)], 20), ORANGE)
    a.region("tail_fin", blob([(860, 380), (910, 400), (900, 500), (860, 480)]), ORANGE)
    a.region("cabin", blob([(260, 420), (420, 340), (620, 380), (680, 500), (600, 620), (360, 640), (240, 560)]), ORANGE)
    a.region("glass", blob([(260, 440), (400, 380), (460, 500), (360, 560), (270, 540)]), SKY)
    a.region("door", rrect(480, 450, 590, 590, 20), SUN)
    a.region("mast", rect(470, 290, 500, 350), DGREY)
    a.region("skid", union(thick([(280, 720), (640, 720), (680, 690)], 20), thick([(360, 630), (340, 720)], 16), thick([(560, 620), (560, 720)], 16)), DGREY)
    a.part("rotor", parent="body", pivot=(485, 290), motion="spinflat", amp=360)
    a.region("rotor", rrect(170, 276, 800, 300, 12), DGREY)
    a.region("rotor_hub", circle(485, 290, 22), GREY)
    a.part("tail_rotor", parent="body", pivot=(880, 440), motion="spin", amp=360)
    a.region("tail_rotor", union(rrect(872, 380, 888, 500, 8), rrect(820, 432, 940, 448, 8)), DGREY)
    a.anchor("pilot", "passenger", 340, 500)
    return a


def sailboat():
    a = Art("vehicles.sailboat")
    a.part("water", pivot=(512, 820))
    waves(a, 780, SKY)
    a.part("body", pivot=(512, 780), motion="bob", amp=8)
    a.region("mast", rect(500, 160, 524, 680), BROWN)
    a.region("sail_main", blob([(530, 180), (760, 560), (740, 640), (530, 640)], 0.6), WHITE)
    a.region("sail_front", blob([(490, 240), (480, 640), (290, 640), (300, 600)], 0.6), CORAL)
    a.region("sail_stripe", inter(blob([(530, 180), (760, 560), (740, 640), (530, 640)], 0.6), rect(520, 440, 800, 500)), TURQ)
    a.region("flag", poly([(524, 150), (620, 120), (524, 100)], 8), SUN)
    a.region("hull", blob([(200, 660), (840, 660), (760, 800), (300, 800)], 0.5), BROWN)
    a.region("deck_stripe", inter(blob([(200, 660), (840, 660), (760, 800), (300, 800)], 0.5), rect(150, 690, 900, 720)), SAND)
    a.anchor("deck", "passenger", 400, 660)
    return a


def scooter():
    a = Art("vehicles.scooter")
    a.part("body", pivot=(512, 780))
    a.region("stem", thick([(740, 760), (680, 300)], 30), TURQ)
    a.region("handle", thick([(600, 290), (760, 300)], 30), DGREY)
    a.region("grip_l", rrect(580, 270, 630, 310, 14), PINK)
    a.region("grip_r", rrect(740, 280, 790, 320, 14), PINK)
    a.region("deck", rrect(220, 720, 760, 780, 26), CORAL)
    a.region("deck_tape", rrect(280, 726, 640, 746, 10), DGREY)
    a.region("fender", diff(circle(270, 820, 90), rect(150, 820, 400, 940), circle(270, 820, 70)), TURQ)
    wheel(a, "wheel_b", 270, 820, 64)
    wheel(a, "wheel_f", 760, 820, 64)
    a.anchor("rider", "passenger", 480, 720)
    return a


def camper():
    a = Art("vehicles.camper")
    a.part("body", pivot=(512, 760))
    a.region("popup", poly([(220, 330), (640, 330), (600, 250), (260, 250)], 20), CREAM)
    a.region("body", rrect(120, 320, 900, 740, 60), MINT)
    a.region("top_half", inter(rrect(120, 320, 900, 740, 60), rect(100, 300, 920, 500)), WHITE)
    a.region("windshield", poly([(740, 360), (860, 360), (890, 500), (740, 500)], 24), SKY)
    a.region("round_window", circle(300, 430, 56), SKY)
    a.region("window2", rrect(420, 380, 560, 480, 22), SKY)
    a.region("door", rrect(600, 400, 700, 720, 20), TURQ)
    for i in range(5):
        a.region(f"awning{i}", poly([(160 + i * 80, 500), (240 + i * 80, 500), (240 + i * 80, 560), (200 + i * 80, 580), (160 + i * 80, 560)], 6), CORAL if i % 2 == 0 else WHITE)
    a.region("bumper", rrect(110, 700, 914, 750, 24), GREY)
    a.region("light", circle(880, 640, 24), YELLOW)
    wheel(a, "wheel_b", 280, 750, 84)
    wheel(a, "wheel_f", 760, 750, 84)
    a.anchor("seat", "passenger", 800, 500); a.anchor("roof", "passenger", 430, 250)
    return a


ALL = [car, train, airplane, bicycle, bus, boat, tram, hot_air_balloon, helicopter, sailboat, scooter, camper]
