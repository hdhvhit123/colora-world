from colora_art import *
from pal import *


def birthday_cake():
    a = Art("celebrations.birthday_cake")
    a.part("candles", pivot=(512, 300), motion="flicker", amp=6)
    for i, x in enumerate((400, 512, 624)):
        a.region(f"candle{i}", rrect(x - 18, 240, x + 18, 380, 8), [SKY, PINK, SUN][i])
        a.region(f"flame{i}", blob([(x, 150), (x + 24, 200), (x, 232), (x - 24, 200)]), ORANGE)
    a.part("body", pivot=(512, 900))
    a.region("stand", union(ellipse(512, 860, 320, 50), rrect(452, 860, 572, 930, 16), ellipse(512, 930, 140, 24)), SKY)
    a.region("tier_low", rrect(200, 600, 824, 850, 40), LAV)
    a.region("tier_top", rrect(300, 380, 724, 610, 40), PINK)
    a.region("drip_top", union(rrect(290, 370, 734, 420, 24), *[circle(330 + k * 64, 430, 26) for k in range(7)]), WHITE)
    a.region("drip_low", union(rrect(190, 590, 834, 640, 24), *[circle(230 + k * 70, 650, 28) for k in range(9)]), WHITE)
    for i, x in enumerate((260, 380, 500, 620, 740)):
        a.region(f"sprinkle{i}", rrect(x, 740, x + 40, 760, 8, 30 * (i % 3 - 1)), [SUN, TURQ, CORAL][i % 3])
    a.anchor("table", "table_item", 512, 900)
    return a


def balloons():
    a = Art("celebrations.balloons")
    a.part("body", pivot=(512, 900), motion="bob", amp=12)
    specs = [(330, 330, 140, 170, RED, -12), (690, 330, 140, 170, BLUE, 12), (512, 230, 150, 180, SUN, 0), (420, 520, 120, 150, LEAF, -6), (610, 520, 120, 150, LAV, 6)]
    for i, (x, y, rx, ry, c, rot) in enumerate(specs):
        a.region(f"string{i}", thick([(x, y + ry), (x + (512 - x) * 0.6, 760), (512, 880)], 8), DGREY)
    for i, (x, y, rx, ry, c, rot) in enumerate(specs):
        a.region(f"balloon{i}", union(ellipse(x, y, rx, ry, rot), poly([(x, y + ry - 4), (x + 20, y + ry + 26), (x - 20, y + ry + 26)], 6)), c)
        a.shine(ellipse(x - rx * 0.4, y - ry * 0.4, rx * 0.16, ry * 0.24, -20))
    a.region("bow", union(ellipse(470, 880, 50, 30, -20), ellipse(554, 880, 50, 30, 20), circle(512, 880, 22)), CORAL)
    a.anchor("tie", "carry", 512, 880)
    return a


def gift():
    a = Art("celebrations.gift")
    a.part("lid", pivot=(512, 420), motion="pop", amp=20)
    a.region("bow_l", blob([(512, 380), (380, 250), (330, 330), (420, 400)]), PINK)
    a.region("bow_r", blob([(512, 380), (644, 250), (694, 330), (604, 400)]), PINK)
    a.region("lid", rrect(220, 380, 804, 500, 24), TURQ)
    a.region("lid_ribbon", rect(480, 380, 544, 500), PINK)
    a.region("knot", circle(512, 390, 40), CORAL)
    a.part("body", pivot=(512, 900))
    a.region("box", rrect(260, 490, 764, 880, 20), MINT)
    a.region("ribbon_v", rect(480, 490, 544, 880), PINK)
    a.region("ribbon_h", rect(260, 650, 764, 710), PINK)
    for i, (x, y) in enumerate([(340, 570), (680, 800), (380, 800), (660, 560)]):
        a.region(f"dot{i}", circle(x, y, 26), WHITE)
    a.anchor("inside", "carry", 512, 500)
    return a


def bunting():
    a = Art("celebrations.bunting")
    a.part("body", pivot=(512, 300), motion="sway", amp=4)
    rope = [(60, 260), (300, 360), (512, 390), (724, 360), (964, 260)]
    a.region("pole_l", rrect(40, 200, 80, 900, 14), BROWN)
    a.region("pole_r", rrect(944, 200, 984, 900, 14), BROWN)
    a.line(curve(rope), 8)
    cols = [RED, SUN, TURQ, PINK, LEAF, LAV, ORANGE]
    for i in range(7):
        t = (i + 0.5) / 7
        x = 80 + t * 864
        y = 260 + math.sin(t * math.pi) * 130
        a.region(f"flag{i}", poly([(x - 52, y - 4), (x + 52, y + 4 * (1 if t < 0.5 else -1)), (x, y + 150)], 12), cols[i])
    for i, (x, y) in enumerate([(200, 700), (512, 780), (820, 700)]):
        a.region(f"confetti{i}", star(x, y, 30, 12, 5, -90, 4), cols[i * 2])
    a.anchor("hang", "carry", 512, 400)
    return a


def greeting_card():
    a = Art("celebrations.greeting_card")
    a.part("back", pivot=(512, 900))
    a.region("inside", poly([(512, 260), (860, 200), (860, 840), (512, 900)], 16), CREAM)
    for k in range(3):
        a.line([(560, 520 + k * 60), (800, 500 + k * 60)], 5)
    a.part("cover", pivot=(512, 580), motion="open", amp=20)
    a.region("cover", poly([(512, 260), (164, 200), (164, 840), (512, 900)], 16), CORAL)
    a.region("heart", heart(340, 520, 110), PINK)
    a.region("heart_small", heart(260, 330, 40), SUN)
    a.region("heart_small2", heart(430, 760, 36), SUN)
    a.part("envelope", pivot=(780, 860))
    a.region("envelope", rrect(600, 760, 960, 960, 16), SKY)
    a.region("flap", poly([(600, 770), (960, 770), (780, 880)], 12), BLUE)
    a.anchor("give", "carry", 340, 560)
    return a


def paper_lantern():
    a = Art("celebrations.paper_lantern")
    a.part("body", pivot=(512, 120), motion="sway", amp=6)
    a.region("string", thick([(512, 40), (512, 200)], 10), DGREY)
    a.region("cap_top", rrect(400, 190, 624, 240, 14), SUN)
    body = ellipse(512, 470, 280, 240)
    a.region("paper", body, RED)
    for i, dx in enumerate((-180, -90, 0, 90, 180)):
        a.line(curve([(512 + dx * 0.55, 240), (512 + dx * 1.1, 470), (512 + dx * 0.55, 700)]), 5)
    a.region("glow", ellipse(512, 470, 90, 140), ORANGE)
    a.region("cap_bottom", rrect(400, 690, 624, 740, 14), SUN)
    a.region("tassel", poly([(470, 740), (554, 740), (580, 940), (444, 940)], 14), SUN)
    for x in (480, 512, 544):
        a.line([(x, 790), (x, 930)], 4)
    a.anchor("hang", "carry", 512, 120)
    return a


def spring_blossom():
    a = Art("celebrations.spring_blossom")
    a.part("body", pivot=(300, 900), motion="sway", amp=4)
    a.region("vase", union(ellipse(512, 800, 140, 120), rrect(450, 640, 574, 700, 20)), TURQ)
    a.region("vase_band", inter(ellipse(512, 800, 140, 120), rect(360, 780, 670, 820)), SUN)
    a.region("branch", union(thick([(500, 660), (420, 420), (260, 230)], 26), thick([(440, 480), (600, 320), (760, 240)], 20), thick([(370, 330), (420, 200)], 16)), BROWN)
    for i, (x, y) in enumerate([(260, 220), (420, 190), (760, 230), (600, 310), (330, 380), (520, 420), (680, 380)]):
        a.region(f"blossom{i}", union(*[circle(x + 26 * math.cos(math.radians(k * 72 - 90)), y + 26 * math.sin(math.radians(k * 72 - 90)), 24) for k in range(5)]), PINK if i % 2 else ROSE)
        a.ink(circle(x, y, 8))
    for i, (x, y) in enumerate([(220, 520), (780, 520)]):
        a.region(f"petal_fall{i}", ellipse(x, y, 22, 14, 30), PINK)
    a.anchor("table", "table_item", 512, 900)
    return a


def lantern_festival():
    a = Art("celebrations.lantern_festival")
    a.part("body", pivot=(512, 900), motion="sway", amp=6)
    a.region("stick", thick([(200, 900), (520, 160)], 20), BROWN)
    a.region("hook", thick([(520, 160), (560, 200), (560, 260)], 10), DGREY)
    star_l = star(560, 520, 300, 140, 5, -90, 30)
    a.region("star", star_l, SUN)
    a.region("star_in", star(560, 520, 170, 80, 5, -90, 18), YELLOW)
    a.region("tail_l", thick([(420, 780), (390, 900)], 16), RED)
    a.region("tail_r", thick([(700, 780), (730, 900)], 16), RED)
    a.region("candle_glow", circle(560, 540, 50), ORANGE)
    a.part("glow_dots", pivot=(560, 520), motion="twinkle", amp=6)
    for i, (x, y) in enumerate([(820, 300), (300, 340), (850, 760)]):
        a.region(f"spark{i}", star(x, y, 30, 10, 4, -90, 3), SUN)
    a.anchor("hold", "carry", 200, 900)
    return a


def winter_tree():
    a = Art("celebrations.winter_tree")
    a.part("body", pivot=(512, 920))
    a.region("trunk", rrect(470, 790, 554, 900, 12), BROWN)
    a.region("pot", poly([(400, 860), (624, 860), (600, 960), (424, 960)], 14), RED)
    a.region("tier3", poly([(220, 820), (804, 820), (512, 520)], 30), GREEN)
    a.region("tier2", poly([(280, 620), (744, 620), (512, 330)], 30), GREEN)
    a.region("tier1", poly([(340, 420), (684, 420), (512, 180)], 30), GREEN)
    a.region("snow1", inter(poly([(340, 420), (684, 420), (512, 180)], 30), poly([(400, 330), (450, 370), (512, 330), (570, 370), (630, 330), (512, 160)])), WHITE)
    for i, (x, y, c) in enumerate([(420, 500, RED), (600, 540, SKY), (380, 720, SUN), (560, 700, PINK), (680, 760, LAV), (480, 380, SUN)]):
        a.region(f"ornament{i}", circle(x, y, 26), c)
    a.region("garland", inter(union(poly([(220, 820), (804, 820), (512, 520)], 30), poly([(280, 620), (744, 620), (512, 330)], 30)), thick([(300, 600), (520, 660), (720, 560)], 18)), SUN)
    a.region("topper", star(512, 160, 70, 30, 5, -90, 8), YELLOW)
    a.anchor("base", "plant", 512, 920)
    return a


def festival_lights():
    a = Art("celebrations.festival_lights")
    a.part("body", pivot=(512, 400), motion="twinkle", amp=6)
    wires = [[(60, 260), (300, 380), (512, 410), (724, 380), (964, 260)], [(60, 560), (300, 680), (512, 710), (724, 680), (964, 560)]]
    cols = [RED, SUN, TURQ, PINK, LEAF, ORANGE, LAV]
    for w, wire in enumerate(wires):
        a.line(curve(wire), 7)
        for i in range(6):
            t = (i + 0.5) / 6
            x = 60 + t * 904
            y = wire[0][1] + math.sin(t * math.pi) * 150
            a.region(f"socket{w}_{i}", rrect(x - 16, y, x + 16, y + 30, 6), DGREY)
            a.region(f"bulb{w}_{i}", blob([(x, y + 26), (x + 34, y + 70), (x, y + 130), (x - 34, y + 70)]), cols[(i + w * 3) % 7])
    a.anchor("hang", "carry", 512, 410)
    return a


def kite_festival():
    a = Art("celebrations.kite_festival")
    a.part("body", pivot=(480, 400), motion="sway", amp=10)
    kite = poly([(480, 100), (720, 380), (480, 700), (240, 380)], 20)
    a.region("q1", inter(kite, rect(0, 0, 480, 380)), RED)
    a.region("q2", inter(kite, rect(480, 0, 1024, 380)), SUN)
    a.region("q3", inter(kite, rect(0, 380, 480, 1024)), TURQ)
    a.region("q4", inter(kite, rect(480, 380, 1024, 1024)), LAV)
    a.part("tail", parent="body", pivot=(480, 700), motion="wave", amp=14)
    a.line(curve([(480, 700), (560, 780), (500, 860), (600, 940)]), 6)
    for i, (x, y) in enumerate([(540, 770), (520, 850), (580, 920)]):
        a.region(f"bow{i}", union(poly([(x, y), (x - 40, y - 26), (x - 40, y + 26)], 6), poly([(x, y), (x + 40, y - 26), (x + 40, y + 26)], 6)), [PINK, LEAF, ORANGE][i])
    a.part("string", pivot=(480, 380))
    a.line([(480, 380), (860, 960)], 4)
    a.region("spool", rrect(830, 920, 900, 990, 14), BROWN)
    a.anchor("hold", "carry", 860, 950)
    return a


def family_banner():
    a = Art("celebrations.family_banner")
    a.part("body", pivot=(512, 400), motion="sway", amp=3)
    a.region("pole_l", rrect(80, 150, 120, 900, 14), BROWN)
    a.region("pole_r", rrect(904, 150, 944, 900, 14), BROWN)
    a.region("knob_l", circle(100, 140, 30), SUN)
    a.region("knob_r", circle(924, 140, 30), SUN)
    banner = poly([(120, 220), (904, 220), (904, 560), (512, 620), (120, 560)], 20)
    a.region("banner", banner, CREAM)
    a.region("trim", diff(banner, poly([(150, 250), (874, 250), (874, 530), (512, 586), (150, 530)], 16)), CORAL)
    for i, (x, h) in enumerate([(300, 1.0), (430, 0.75), (580, 0.85), (720, 0.6)]):
        hy = 470 - 120 * h
        a.region(f"person{i}", union(circle(x, hy, 40 * h + 10), rrect(x - 40 * h - 10, hy + 40, x + 40 * h + 10, 520, 30)), [TURQ, LAV, SUN, PINK][i])
    a.region("heart", heart(512, 290, 40), RED)
    a.anchor("hang", "carry", 512, 220)
    return a


ALL = [birthday_cake, balloons, gift, bunting, greeting_card, paper_lantern, spring_blossom, lantern_festival, winter_tree, festival_lights, kite_festival, family_banner]
