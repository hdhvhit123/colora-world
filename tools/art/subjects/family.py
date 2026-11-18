from colora_art import *
from pal import *
from people import Fig


def child():
    a = Art("family.child")
    a.part("balloon", pivot=(700, 520), motion="bob", amp=10)
    a.region("string", thick([(690, 560), (700, 400), (740, 300)], 8), DGREY)
    a.region("balloon", blob([(740, 120), (820, 170), (830, 260), (760, 310), (690, 270), (680, 180)]), RED)
    a.region("knot", poly([(740, 300), (760, 330), (720, 330)], 6), RED)
    a.part("body", pivot=(512, 900))
    f = Fig(a, 470, 200, 1.0, child=True)
    f.legs("shorts", BLUE, CORAL, SKIN2)
    f.torso(SUN)
    a.region("star", star(470, f.neck + 80, 40, 18, 5, -90, 6), ORANGE)
    f.arm("arm_l", [(-95, f.neck + 30), (-140, f.neck + 130), (-120, f.neck + 200)], SUN, SKIN2)
    f.arm("arm_r", [(95, f.neck + 30), (170, f.neck + 30), (220, 560)], SUN, SKIN2, part="wave", pivot=(565, f.neck + 30), motion="wave", amp=14)
    f.head(SKIN2, HAIR1, "ponytail")
    a.anchor("ride", "passenger", 470, f.foot); a.anchor("seat", "seat", 470, f.hip)
    return a


def caregiver():
    a = Art("family.caregiver")
    a.part("body", pivot=(512, 940))
    f = Fig(a, 512, 110, 1.0)
    f.legs("pants", NAVYL, BROWN, SKIN3)
    f.torso(TURQ)
    a.region("pocket", rrect(540, f.neck + 120, 600, f.neck + 180, 14), MINT)
    f.neckpiece(SUN, "scarf")
    a.region("bag_strap", thick([(420, f.neck), (600, f.hip - 20)], 18), BROWN)
    a.region("bag", rrect(580, f.hip - 60, 690, f.hip + 30, 22), ORANGE)
    f.arm("arm_l", [(-105, f.neck + 30), (-150, f.neck + 140), (-150, f.neck + 230)], TURQ, SKIN3)
    f.arm("arm_r", [(105, f.neck + 30), (180, f.neck - 40), (200, f.neck - 140)], TURQ, SKIN3, part="wave", pivot=(617, f.neck + 30), motion="wave", amp=14)
    f.head(SKIN3, HAIR1, "curly")
    a.anchor("ride", "passenger", 512, f.foot); a.anchor("hand", "carry", 362, f.neck + 230)
    return a


def mother():
    a = Art("family.mother")
    a.part("body", pivot=(512, 940))
    f = Fig(a, 470, 110, 1.0)
    f.legs("dress", SKIN1, PINK, SKIN1)
    f.torso(LAV, "dress")
    a.region("belt", rrect(470 - 120, f.hip - 70, 470 + 120, f.hip - 40, 12), PURPLE)
    for i, (x, y) in enumerate([(420, f.hip + 30), (520, f.hip + 60), (460, f.hip - 140)]):
        a.region(f"flower{i}", union(*[circle(x + 14 * math.cos(math.radians(k * 72)), y + 14 * math.sin(math.radians(k * 72)), 12) for k in range(5)]), WHITE)
    f.arm("arm_l", [(-105, f.neck + 30), (-140, f.neck + 140), (-120, f.neck + 230)], LAV, SKIN1)
    # holding a baby bundle
    a.region("baby_wrap", ellipse(640, f.neck + 140, 90, 70, -20), MINT)
    a.region("baby_face", circle(680, f.neck + 110, 40), SKIN1)
    a.ink(circle(670, f.neck + 106, 5)); a.ink(circle(694, f.neck + 112, 5))
    f.arm("arm_r", [(105, f.neck + 30), (150, f.neck + 160), (60, f.neck + 180)], LAV, SKIN1)
    f.head(SKIN1, HAIR2, "long")
    a.anchor("ride", "passenger", 470, f.foot)
    return a


def father():
    a = Art("family.father")
    a.part("body", pivot=(512, 940))
    f = Fig(a, 512, 100, 1.08)
    f.legs("pants", BROWN, DGREY, SKIN2)
    f.torso(BLUE)
    for k in range(3):
        a.region(f"check{k}", rect(512 - 110 + k * 80, f.neck + 30, 512 - 80 + k * 80, f.hip), DEEPBLUE)
    f.neckpiece(WHITE, "collar")
    f.arm("arm_l", [(-110, f.neck + 30), (-130, f.neck + 140), (-60, f.neck + 200)], BLUE, SKIN2)
    a.region("mug", rrect(430, f.neck + 160, 500, f.neck + 240, 12), CORAL)
    a.region("mug_handle", diff(circle(420, f.neck + 200, 26), circle(420, f.neck + 200, 12)), CORAL)
    f.arm("arm_r", [(110, f.neck + 30), (170, f.neck + 150), (160, f.neck + 250)], BLUE, SKIN2, part="wave", pivot=(630, f.neck + 30), motion="wave", amp=10)
    f.head(SKIN2, HAIR1, "short")
    a.region("beard", inter(ellipse(512, f.hy + 50, 100, 80), rect(380, f.hy + 40, 640, f.hy + 160)), HAIR1)
    a.region("mouth", ellipse(512, f.hy + 50, 22, 12), PINK)
    a.anchor("ride", "passenger", 512, f.foot)
    return a


def grandparent():
    a = Art("family.grandparent")
    a.part("body", pivot=(512, 940))
    f = Fig(a, 480, 150, 1.0)
    a.region("cane", union(thick([(640, f.neck + 180), (640, f.foot + 20)], 18), thick(arc(610, f.neck + 180, 30, 30, 180, 180), 18)), BROWN)
    f.legs("pants", GREY, BROWN, SKIN1)
    f.torso(GREEN)
    a.region("cardigan_l", poly([(480 - 105, f.neck), (470, f.neck), (470, f.hip + 20), (480 - 112, f.hip + 20)], 26), OLIVE)
    a.region("cardigan_r", poly([(480 + 105, f.neck), (490, f.neck), (490, f.hip + 20), (480 + 112, f.hip + 20)], 26), OLIVE)
    for k in range(3):
        a.ink(circle(500, f.neck + 60 + k * 60, 7))
    f.arm("arm_l", [(-105, f.neck + 30), (-140, f.neck + 140), (-110, f.neck + 220)], OLIVE, SKIN1)
    f.arm("arm_r", [(105, f.neck + 30), (150, f.neck + 120), (150, f.neck + 180)], OLIVE, SKIN1)
    f.head(SKIN1, GREY, "bun")
    hy = f.hy
    a.region("glass_l", diff(circle(480 - 38, hy + 2, 30), circle(480 - 38, hy + 2, 22)), DGREY)
    a.region("glass_r", diff(circle(480 + 38, hy + 2, 30), circle(480 + 38, hy + 2, 22)), DGREY)
    a.anchor("seat", "seat", 480, f.hip)
    return a


def sibling():
    a = Art("family.sibling")
    a.part("body", pivot=(512, 940))
    big = Fig(a, 380, 140, 0.95, child=True)
    a.region("backpack", rrect(380 - 140, big.neck + 10, 380 - 40, big.neck + 170, 30), RED)
    big.legs("pants", DEEPBLUE, WHITE, SKIN3)
    big.torso(GREEN)
    big.arm("arm_l", [(-95, big.neck + 30), (-130, big.neck + 120), (-120, big.neck + 190)], GREEN, SKIN3)
    big.arm("arm_hold", [(95, big.neck + 30), (160, big.neck + 120), (230, big.neck + 160)], GREEN, SKIN3)
    a.part("head_big", parent="body", pivot=(380, big.neck))
    big.head(SKIN3, HAIR1, "spiky")
    a.part("small", pivot=(660, 940), motion="hop", amp=8)
    sm = Fig(a, 680, 400, 0.72, child=True)
    sm.legs("skirt", PINK, CORAL, SKIN3)
    sm.torso(YELLOW)
    a.region("small_arm", thick([(680 - 68, sm.neck + 20), (620, sm.neck + 100)], 40), YELLOW)
    a.region("small_hand", circle(616, sm.neck + 104, 22), SKIN3)
    a.region("small_ear_l", circle(680 - 84, sm.hy + 6, 18), SKIN3)
    a.region("small_ear_r", circle(680 + 84, sm.hy + 6, 18), SKIN3)
    a.region("small_face", ellipse(680, sm.hy, sm.hr, sm.hr * 0.98), SKIN3)
    a.region("small_buns", union(circle(680 - 70, sm.hy - 70, 34), circle(680 + 70, sm.hy - 70, 34)), HAIR1)
    a.region("small_hair", inter(ellipse(680, sm.hy - 8, sm.hr * 1.04, sm.hr), rect(500, 0, 860, sm.hy - 30)), HAIR1)
    a.eyes(680, sm.hy + 4, 60, 12)
    a.smile(680, sm.hy + 34, 40, 0.5, 5)
    a.anchor("ride", "passenger", 380, big.foot)
    return a


def reading():
    a = Art("family.reading")
    a.part("body", pivot=(512, 900))
    a.region("cushion", ellipse(470, 860, 260, 70), CORAL)
    a.line([(250, 860), (690, 860)], 5)
    f = Fig(a, 420, 200, 1.0)
    f.hip = 790
    f.legs("pants", PURPLE, TURQ, SKIN1, sit=True)
    f.torso(SUN)
    f.arm("arm_l", [(-100, f.neck + 30), (-60, f.neck + 180), (60, f.neck + 200)], SUN, SKIN1)
    a.region("book_l", poly([(470, f.neck + 110), (600, f.neck + 80), (600, f.neck + 230), (470, f.neck + 250)], 10), TURQ)
    a.region("book_r", poly([(600, f.neck + 80), (720, f.neck + 110), (720, f.neck + 250), (600, f.neck + 230)], 10), BLUE)
    a.region("page_l", poly([(486, f.neck + 120), (596, f.neck + 96), (596, f.neck + 218), (486, f.neck + 238)], 6), WHITE)
    a.region("page_r", poly([(604, f.neck + 96), (704, f.neck + 120), (704, f.neck + 238), (604, f.neck + 218)], 6), WHITE)
    for k in range(3):
        a.line([(500, f.neck + 140 + k * 26), (584, f.neck + 122 + k * 26)], 4)
        a.line([(616, f.neck + 122 + k * 26), (690, f.neck + 140 + k * 26)], 4)
    f.arm("arm_r", [(100, f.neck + 30), (170, f.neck + 140), (290, f.neck + 180)], SUN, SKIN1)
    f.head(SKIN1, HAIR2, "curly_long")
    a.anchor("seat", "seat", 420, 790)
    return a


def cooking():
    a = Art("family.cooking")
    a.part("table", pivot=(512, 940))
    a.region("table", rrect(560, 640, 940, 690, 18), BROWN)
    a.region("table_leg", rrect(880, 680, 910, 940, 12), BROWN)
    a.region("tray", rrect(620, 600, 900, 650, 20), GREY)
    for i, x in enumerate((670, 760, 850)):
        a.region(f"cookie{i}", circle(x, 600, 34), TAN)
        a.ink(circle(x - 8, 594, 5)); a.ink(circle(x + 10, 606, 5))
    a.part("body", pivot=(420, 940))
    f = Fig(a, 400, 130, 1.0)
    f.legs("pants", DGREY, BROWN, SKIN4)
    f.torso(RED)
    a.region("apron", union(rrect(400 - 80, f.neck + 60, 400 + 80, f.hip + 90, 30), rrect(400 - 50, f.neck, 400 + 50, f.neck + 80, 16)), WHITE)
    a.region("apron_pocket", rrect(400 - 40, f.neck + 180, 400 + 40, f.neck + 230, 12), CORAL)
    a.region("bowl", union(inter(circle(520, f.neck + 140, 90), rect(400, f.neck + 140, 640, f.neck + 260)), rrect(420, f.neck + 130, 620, f.neck + 156, 12)), SKY)
    a.region("spoon", thick([(540, f.neck + 140), (600, f.neck + 20)], 16), BROWN)
    f.arm("arm_l", [(-105, f.neck + 30), (-80, f.neck + 170), (40, f.neck + 200)], RED, SKIN4)
    f.arm("arm_r", [(105, f.neck + 30), (170, f.neck + 80), (190, f.neck + 40)], RED, SKIN4, part="stir", pivot=(505, f.neck + 30), motion="stir", amp=10)
    f.head(SKIN4, HAIR1, "short")
    a.anchor("table", "table_item", 760, 640)
    return a


def gardening():
    a = Art("family.gardening")
    a.part("plants", pivot=(760, 900))
    a.region("soil", ellipse(760, 900, 200, 40), BROWN)
    for i, x in enumerate((680, 840)):
        a.region(f"sprout_stem{i}", thick([(x, 900), (x, 790)], 14), GREEN)
        a.region(f"sprout_leaf{i}", union(ellipse(x - 30, 790, 34, 18, -20), ellipse(x + 30, 790, 34, 18, 20)), LEAF)
    a.part("body", pivot=(400, 940))
    f = Fig(a, 380, 160, 1.0)
    f.legs("pants", OLIVE, BROWN, SKIN2)
    f.torso(PEACH)
    a.region("overall", union(rrect(380 - 95, f.neck + 100, 380 + 95, f.hip + 20, 26), thick([(380 - 70, f.neck + 110), (380 - 80, f.neck)], 20), thick([(380 + 70, f.neck + 110), (380 + 80, f.neck)], 20)), OLIVE)
    f.arm("arm_l", [(-105, f.neck + 30), (-140, f.neck + 140), (-120, f.neck + 230)], PEACH, SKIN2)
    a.part("can", parent="body", pivot=(560, f.neck + 120), motion="tip", amp=14)
    a.region("can", rrect(540, f.neck + 100, 680, f.neck + 220, 26), TURQ)
    a.region("can_spout", thick([(670, f.neck + 180), (760, f.neck + 120)], 22), TURQ)
    a.region("can_rose", circle(770, f.neck + 112, 24), GREY)
    a.region("can_handle", diff(circle(610, f.neck + 100, 50), circle(610, f.neck + 100, 34), rect(500, f.neck + 100, 720, f.neck + 200)), TURQ)
    for i, (x, y) in enumerate([(800, f.neck + 160), (820, f.neck + 210), (790, f.neck + 250)]):
        a.region(f"drop{i}", blob([(x, y - 18), (x + 12, y), (x, y + 12), (x - 12, y)]), SKY)
    a.part("body")
    f.arm("arm_r", [(105, f.neck + 30), (150, f.neck + 110), (190, f.neck + 110)], PEACH, SKIN2)
    f.head(SKIN2, HAIR2, "long", back_hair=True)
    a.region("hat_brim", ellipse(380, f.hy - 70, 190, 40), SUN)
    a.region("hat_top", inter(ellipse(380, f.hy - 80, 110, 90), rect(200, 0, 600, f.hy - 70)), SUN)
    a.region("hat_band", rect(380 - 104, f.hy - 110, 380 + 104, f.hy - 84), CORAL)
    a.anchor("soil", "plant", 760, 900)
    return a


def picnic():
    a = Art("family.picnic")
    a.part("body", pivot=(512, 800))
    mat = poly([(120, 640), (900, 600), (960, 860), (60, 900)], 24)
    a.region("mat", mat, WHITE)
    for k in range(4):
        x0 = 120 + k * 200
        a.region(f"stripe{k}", inter(mat, poly([(x0 + 40, 560), (x0 + 130, 560), (x0 + 110, 960), (x0 + 10, 960)])), RED)
    a.region("basket", rrect(330, 520, 620, 700, 34), TAN)
    a.region("basket_lid", union(inter(ellipse(475, 520, 160, 50), rect(300, 470, 650, 520)), rrect(316, 500, 634, 540, 16)), BROWN)
    a.region("handle", diff(ellipse(475, 500, 110, 110), ellipse(475, 500, 86, 86), rect(300, 500, 650, 650)), BROWN)
    for k in range(3):
        a.line([(350, 580 + k * 40), (600, 580 + k * 40)], 5)
    a.region("napkin", poly([(560, 520), (640, 470), (660, 540)], 10), SKY)
    a.region("plate", ellipse(760, 740, 100, 40), WHITE)
    a.region("sandwich", poly([(700, 740), (820, 720), (790, 690)], 10), SAND)
    a.region("juice", rrect(200, 620, 270, 760, 20), ORANGE)
    a.region("straw", thick([(250, 630), (270, 560)], 10), PINK)
    a.anchor("food", "table_item", 760, 730); a.anchor("seat1", "seat", 250, 800); a.anchor("seat2", "seat", 800, 800)
    return a


def petcare():
    a = Art("family.petcare")
    a.part("pet", pivot=(720, 880), motion="wag", amp=6)
    a.region("pet_body", ellipse(720, 820, 150, 80), CREAM)
    a.region("pet_tail", thick([(860, 800), (910, 730)], 30), CREAM)
    a.region("pet_leg1", rrect(620, 860, 666, 920, 18), CREAM)
    a.region("pet_leg2", rrect(770, 860, 816, 920, 18), CREAM)
    a.region("pet_ear", ellipse(560, 730, 40, 70, 30), BROWN)
    a.region("pet_head", circle(610, 760, 86), CREAM)
    a.region("pet_ear2", ellipse(660, 700, 30, 60, -30), BROWN)
    a.eyes(600, 750, 60, 12, lids=True)
    a.ink(ellipse(586, 790, 14, 10))
    a.region("bubbles", union(circle(760, 720, 30), circle(810, 700, 22), circle(700, 690, 18)), SKY)
    a.part("body", pivot=(360, 940))
    f = Fig(a, 340, 160, 0.95, child=True)
    f.legs("pants", TURQ, ORANGE, SKIN1)
    f.torso(PINK)
    f.arm("arm_l", [(-95, f.neck + 30), (-130, f.neck + 140), (-110, f.neck + 210)], PINK, SKIN1)
    a.part("brush", parent="body", pivot=(430, f.neck + 30), motion="brush", amp=10)
    f.arm("arm_r", [(95, f.neck + 30), (200, f.neck + 140), (300, f.neck + 180)], PINK, SKIN1)
    a.region("brush_handle", thick([(640, f.neck + 170), (700, f.neck + 140)], 18), CORAL)
    a.region("brush_head", rrect(690, f.neck + 110, 760, f.neck + 160, 12), BROWN)
    a.part("body")
    f.head(SKIN1, HAIR1, "short")
    a.anchor("ride", "passenger", 340, f.foot)
    return a


def celebrate():
    a = Art("family.celebrate")
    a.part("confetti", pivot=(512, 200), motion="twinkle", amp=8)
    for i, (x, y, c) in enumerate([(240, 200, SUN), (780, 180, TURQ), (300, 360, PINK), (760, 380, LAV)]):
        a.region(f"confetti{i}", star(x, y, 30, 13, 5, -90 + i * 20, 5) if i % 2 == 0 else rrect(x - 26, y - 12, x + 26, y + 12, 6, 30 * i), c)
    a.part("body", pivot=(512, 940))
    f = Fig(a, 512, 150, 1.0)
    f.legs("skirt", CORAL, DGREY, SKIN3)
    f.torso(MINT)
    a.region("gift", rrect(430, f.neck + 120, 594, f.neck + 260, 18), LAV)
    a.region("ribbon_v", rect(498, f.neck + 120, 526, f.neck + 260), PINK)
    a.region("bow", union(ellipse(482, f.neck + 112, 34, 22, -20), ellipse(542, f.neck + 112, 34, 22, 20)), PINK)
    f.arm("arm_l", [(-105, f.neck + 30), (-120, f.neck + 160), (-60, f.neck + 200)], MINT, SKIN3)
    f.arm("arm_r", [(105, f.neck + 30), (120, f.neck + 160), (60, f.neck + 200)], MINT, SKIN3)
    f.head(SKIN3, HAIR1, "curly")
    a.region("party_hat", poly([(450, f.hy - 80), (574, f.hy - 80), (520, f.hy - 230)], 14), SUN)
    a.region("pompom", circle(520, f.hy - 232, 22), CORAL)
    a.anchor("ride", "passenger", 512, f.foot)
    return a


ALL = [child, caregiver, mother, father, grandparent, sibling, reading, cooking, gardening, picnic, petcare, celebrate]
