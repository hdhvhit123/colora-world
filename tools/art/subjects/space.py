from colora_art import *
from pal import *
from people import Fig


def rocket():
    a = Art("space.rocket")
    a.part("flame", pivot=(512, 820), motion="flicker", amp=10)
    a.region("flame_out", blob([(430, 800), (594, 800), (570, 900), (512, 990), (454, 900)]), ORANGE)
    a.region("flame_in", blob([(470, 800), (554, 800), (540, 870), (512, 930), (484, 870)]), YELLOW)
    a.part("body", pivot=(512, 700), motion="hover", amp=10)
    a.region("fin_l", poly([(380, 560), (260, 800), (400, 760)], 24), CORAL)
    a.region("fin_r", poly([(644, 560), (764, 800), (624, 760)], 24), CORAL)
    body = blob([(512, 100), (620, 280), (650, 520), (630, 780), (394, 780), (374, 520), (404, 280)], 0.8)
    a.region("body", body, WHITE)
    a.region("nose", inter(body, rect(300, 60, 724, 290)), RED)
    a.region("band", inter(body, rect(300, 640, 724, 700)), RED)
    a.region("window_rim", circle(512, 430, 96), GREY)
    a.region("window", circle(512, 430, 70), SKY)
    a.region("nozzle", poly([(440, 770), (584, 770), (604, 820), (420, 820)], 10), DGREY)
    a.region("fin_c", rrect(496, 660, 528, 800, 12), CORAL)
    a.anchor("cockpit", "passenger", 512, 430)
    return a


def planet():
    a = Art("space.planet")
    a.part("body", pivot=(512, 512), motion="spinslow", amp=12)
    ball = circle(512, 512, 300)
    a.region("planet", ball, TURQ)
    for i, (x, y, rx, ry) in enumerate([(340, 330, 110, 60), (700, 650, 110, 70), (330, 690, 70, 40)]):
        a.region(f"land{i}", inter(ball, blob([(x - rx, y), (x - rx * 0.4, y - ry), (x + rx * 0.6, y - ry * 0.8), (x + rx, y + ry * 0.2), (x + rx * 0.2, y + ry), (x - rx * 0.7, y + ry * 0.8)])), LEAF)
    a.region("ice", inter(ball, ellipse(512, 220, 160, 50)), WHITE)
    a.eyes(512, 500, 140, 24)
    a.smile(512, 560, 70, 0.6, 7)
    a.part("moonlet", pivot=(820, 230), motion="orbit", amp=20)
    a.region("moonlet", circle(830, 210, 60), SUN)
    a.region("crater", circle(810, 200, 16), ORANGE)
    a.anchor("surface", "plant", 512, 220)
    return a


def astronaut():
    a = Art("space.astronaut")
    a.part("body", pivot=(512, 940), motion="float", amp=10)
    a.region("pack", rrect(330, 380, 694, 700, 50), GREY)
    f = Fig(a, 512, 110, 1.05)
    for i, sx in enumerate((-1, 1)):
        x = 512 + sx * 60
        a.region(f"leg{i}", rrect(x - 44, f.hip - 30, x + 44, f.foot, 40), WHITE)
        a.region(f"boot{i}", rrect(x - 56, f.foot - 50, x + 56, f.foot + 20, 26), CORAL)
    a.region("suit", rrect(512 - 140, f.neck - 10, 512 + 140, f.hip + 20, 70), WHITE)
    a.region("panel", rrect(450, f.neck + 60, 574, f.neck + 160, 18), SKY)
    for i, x in enumerate((475, 512, 549)):
        a.region(f"btn{i}", circle(x, f.neck + 110, 13), [RED, SUN, LEAF][i])
    a.region("belt", rect(512 - 138, f.hip - 40, 512 + 138, f.hip - 10), CORAL)
    f.arm("arm_l", [(-140, f.neck + 40), (-190, f.neck + 160), (-170, f.neck + 240)], WHITE, CORAL, w=66)
    f.arm("arm_r", [(140, f.neck + 40), (200, f.neck - 20), (230, f.neck - 110)], WHITE, CORAL, part="wave", pivot=(652, f.neck + 40), motion="wave", amp=14, w=66)
    a.part("head", parent="body", pivot=(512, f.neck))
    a.region("helmet", circle(512, f.hy, 150), WHITE)
    a.region("visor", ellipse(512, f.hy + 10, 110, 96), SKY)
    a.region("face", ellipse(512, f.hy + 20, 80, 70), SKIN2)
    a.eyes(512, f.hy + 14, 60, 13)
    a.smile(512, f.hy + 48, 36, 0.5, 5)
    a.shine(ellipse(450, f.hy - 40, 26, 14, -30))
    a.region("antenna", union(thick([(600, f.hy - 130), (630, f.hy - 190)], 12), circle(634, f.hy - 196, 18)), GREY)
    a.anchor("ride", "passenger", 512, f.foot)
    return a


def satellite():
    a = Art("space.satellite")
    a.part("body", pivot=(512, 512), motion="rock", amp=6)
    for i, (l, r) in enumerate([(80, 360), (664, 944)]):
        a.region(f"panel{i}", rrect(l, 420, r, 600, 14), BLUE)
        for k in range(1, 4):
            x = l + (r - l) * k / 4
            a.line([(x, 424), (x, 596)], 5)
        a.line([(l + 4, 510), (r - 4, 510)], 5)
    a.region("arm", rrect(340, 496, 684, 524, 10), GREY)
    a.region("body", rrect(400, 380, 624, 640, 30), SUN)
    a.region("foil", inter(rrect(400, 380, 624, 640, 30), rect(380, 560, 640, 660)), ORANGE)
    a.region("dish", inter(circle(512, 300, 110), rect(380, 180, 640, 300)), WHITE)
    a.region("feed", thick([(512, 300), (512, 220)], 12), GREY)
    a.region("light", circle(512, 470, 30), RED)
    a.anchor("dock", "carry", 512, 640)
    return a


def moon_rover():
    a = Art("space.moon_rover")
    a.part("body", pivot=(512, 780))
    a.region("ground", union(ellipse(512, 860, 420, 60), circle(250, 860, 30)), GREY)
    a.region("crater", ellipse(760, 860, 70, 20), DGREY)
    a.region("mast", rect(640, 280, 664, 480), GREY)
    a.region("camera", rrect(600, 230, 720, 300, 20), WHITE)
    a.region("lens", circle(700, 265, 22), SKY)
    a.region("dish", inter(circle(300, 360, 80), rect(200, 270, 400, 360)), WHITE)
    a.region("dish_pole", rect(290, 350, 310, 480), GREY)
    a.region("body", rrect(220, 470, 800, 640, 40), SUN)
    a.region("panel", rrect(260, 440, 760, 490, 14), BLUE)
    a.region("seat", rrect(420, 400, 520, 470, 16), CORAL)
    for i, x in enumerate((280, 512, 740)):
        a.part(f"wheel{i}", parent="body", pivot=(x, 720), motion="spin", amp=360)
        a.region(f"wheel{i}", circle(x, 720, 80), DGREY)
        a.region(f"hub{i}", circle(x, 720, 40), GREY)
        for k in range(8):
            t = math.radians(k * 45)
            a.ink(circle(x + 70 * math.cos(t), 720 + 70 * math.sin(t), 8))
    a.anchor("seat", "passenger", 470, 400)
    return a


def star_deco():
    a = Art("space.star")
    a.part("body", pivot=(512, 512), motion="twinkle", amp=8)
    a.region("star", star(512, 520, 380, 170, 5, -90, 60), YELLOW)
    a.region("inner", star(512, 520, 230, 110, 5, -90, 40), SUN)
    a.eyes(512, 500, 110, 24)
    a.smile(512, 570, 70, 0.6, 8)
    a.cheeks(512, 556, 200, 18)
    a.part("sparkles", pivot=(512, 512))
    for i, (x, y, r) in enumerate([(150, 200, 40), (880, 260, 30), (860, 860, 36)]):
        a.region(f"sparkle{i}", star(x, y, r, r * 0.3, 4, -90, 2), LAV)
    a.anchor("sky", "sky", 512, 512)
    return a


def station():
    a = Art("space.station")
    a.part("body", pivot=(512, 512), motion="rock", amp=3)
    a.region("truss", rrect(100, 492, 924, 532, 12), GREY)
    for i, (x, y) in enumerate([(160, 330), (160, 560), (760, 330), (760, 560)]):
        a.region(f"array{i}", rrect(x, y, x + 120, y + 140, 10), BLUE)
        a.line([(x + 60, y + 4), (x + 60, y + 136)], 4)
    a.region("module_a", rrect(320, 430, 520, 600, 60), WHITE)
    a.region("module_b", rrect(500, 400, 700, 630, 70), WHITE)
    a.region("dome", circle(512, 340, 80), SKY)
    a.region("dock", rrect(470, 600, 554, 700, 20), GREY)
    for i, x in enumerate((380, 460, 560, 640)):
        a.region(f"window{i}", circle(x, 515, 24), SKY)
    a.region("flag", rrect(600, 430, 660, 470, 6), CORAL)
    a.anchor("dock", "carry", 512, 700); a.anchor("cupola", "passenger", 512, 340)
    return a


def comet():
    a = Art("space.comet")
    a.part("body", pivot=(300, 700), motion="zoom", amp=20)
    tail_pts = [(260, 760), (500, 520), (900, 160), (720, 520), (380, 830)]
    a.region("tail_out", blob([(250, 650), (520, 420), (920, 120), (760, 480), (420, 840)], 0.8), LAV)
    a.region("tail_mid", blob([(300, 700), (540, 480), (860, 200), (700, 500), (400, 790)], 0.8), SKY)
    a.region("tail_in", blob([(330, 720), (560, 540), (780, 300), (650, 540), (420, 760)], 0.8), WHITE)
    a.region("head", circle(300, 740, 130), SUN)
    a.region("glow", circle(300, 740, 80), YELLOW)
    a.eyes(290, 730, 90, 18)
    a.smile(290, 780, 50, 0.6, 6)
    a.anchor("sky", "sky", 300, 740)
    return a


def ringed_planet():
    a = Art("space.ringed_planet")
    a.part("body", pivot=(512, 512), motion="rock", amp=6)
    ring_back = diff(ellipse(512, 540, 440, 120, -15), ellipse(512, 540, 360, 80, -15))
    a.region("ring_back", inter(ring_back, rotate(rect(0, 0, 1024, 540), -15, 512, 540)), SUN)
    a.region("planet", circle(512, 512, 250), PEACH)
    for i, y in enumerate((380, 470, 600)):
        a.region(f"stripe{i}", inter(circle(512, 512, 250), rotate(rect(200, y, 830, y + 40), -15, 512, 512)), CORAL if i % 2 else ORANGE)
    a.part("ring", pivot=(512, 540))
    a.region("ring_front", inter(ring_back, rotate(rect(0, 540, 1024, 1024), -15, 512, 540)), SUN)
    a.region("ring_line", inter(diff(ellipse(512, 540, 410, 104, -15), ellipse(512, 540, 390, 94, -15)), rotate(rect(0, 540, 1024, 1024), -15, 512, 540)), ORANGE)
    a.anchor("sky", "sky", 512, 512)
    return a


def telescope():
    a = Art("space.telescope")
    a.part("body", pivot=(512, 900))
    for i, (x0, x1) in enumerate([(512, 330), (512, 694), (512, 512)]):
        a.region(f"leg{i}", thick([(x0, 600), (x1, 900)], 26), BROWN)
    a.region("mount", circle(512, 590, 50), DGREY)
    a.part("tube", parent="body", pivot=(512, 590), motion="tilt", amp=6)
    a.region("tube", rotate(rrect(300, 520, 760, 640, 30), -25, 512, 580), BLUE)
    a.region("lens_end", rotate(rrect(740, 500, 800, 660, 20), -25, 512, 580), DEEPBLUE)
    a.region("eyepiece", rotate(rrect(250, 550, 310, 610, 12), -25, 512, 580), DGREY)
    a.region("band", rotate(rect(500, 520, 540, 640), -25, 512, 580), SUN)
    a.region("finder", rotate(rrect(420, 470, 600, 500, 12), -25, 512, 580), GREY)
    a.part("stars", pivot=(800, 200))
    a.region("star_a", star(820, 220, 60, 26, 5, -90, 8), SUN)
    a.region("star_b", star(680, 140, 34, 14, 5, -90, 5), SUN)
    a.anchor("base", "plant", 512, 900)
    return a


def alien_friend():
    a = Art("space.alien_friend")
    a.part("body", pivot=(512, 920), motion="hop", amp=10)
    for i, x in enumerate((420, 604)):
        a.region(f"leg{i}", rrect(x - 30, 720, x + 30, 880, 26), LEAF)
        a.region(f"foot{i}", ellipse(x, 890, 60, 30), GREEN)
    a.region("body", ellipse(512, 640, 160, 140), LEAF)
    a.region("belly", ellipse(512, 670, 90, 80), MINT)
    a.region("arm_l", thick([(370, 600), (280, 660), (260, 740)], 40), LEAF)
    a.region("arm_r", thick([(654, 600), (744, 540), (780, 460)], 40), LEAF)
    a.part("head", parent="body", pivot=(512, 520), motion="tilt", amp=6)
    a.region("antenna_l", union(thick([(440, 250), (380, 140)], 16), circle(374, 130, 30)), SUN)
    a.region("antenna_r", union(thick([(584, 250), (644, 140)], 16), circle(650, 130, 30)), SUN)
    a.region("head", ellipse(512, 370, 200, 160), LEAF)
    a.region("eye_white", circle(512, 350, 80), WHITE)
    a.ink(circle(520, 356, 40)); a.shine(circle(536, 340, 14))
    a.smile(512, 460, 90, 0.6, 8)
    a.cheeks(512, 440, 260, 20)
    a.anchor("ride", "passenger", 512, 890)
    return a


def galaxy():
    a = Art("space.galaxy")
    a.part("body", pivot=(512, 512), motion="spinslow", amp=20)
    arms = []
    for k in range(3):
        pts = []
        for i in range(14):
            t = i / 13
            ang = math.radians(k * 120 + t * 260)
            r = 40 + t * 380
            pts.append((512 + r * math.cos(ang), 512 + r * math.sin(ang) * 0.8))
        a.region(f"arm{k}", thick(pts, 90 - 0, cap="round"), [LAV, SKY, PINK][k])
    a.region("core", circle(512, 512, 110), SUN)
    a.region("core_in", circle(512, 512, 60), YELLOW)
    for i, (x, y) in enumerate([(200, 180), (840, 220), (860, 820), (170, 840), (512, 140)]):
        a.region(f"dot{i}", star(x, y, 28, 11, 5, -90, 4), SUN)
    a.anchor("sky", "sky", 512, 512)
    return a


ALL = [rocket, planet, astronaut, satellite, moon_rover, star_deco, station, comet, ringed_planet, telescope, alien_friend, galaxy]
