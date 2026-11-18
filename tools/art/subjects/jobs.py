from colora_art import *
from pal import *
from people import Fig


def doctor():
    a = Art("jobs.doctor")
    a.part("body", pivot=(512, 940))
    f = Fig(a, 512, 120, 1.0)
    f.legs("pants", NAVYL, DGREY, SKIN2)
    f.torso(SKY, coat=WHITE)
    a.region("pocket", rrect(560, f.neck + 140, 620, f.neck + 200, 12), WHITE)
    a.region("pen", rrect(580, f.neck + 110, 594, f.neck + 160, 6), CORAL)
    a.region("stetho", thick([(450, f.neck), (430, f.neck + 120), (512, f.neck + 170), (590, f.neck + 120), (574, f.neck)], 12), DGREY)
    a.region("stetho_disc", circle(512, f.neck + 180, 24), GREY)
    f.arm("arm_l", [(-105, f.neck + 30), (-100, f.neck + 170), (-10, f.neck + 210)], WHITE, SKIN2)
    a.region("clipboard", rrect(380, f.neck + 150, 500, f.neck + 300, 12), BROWN)
    a.region("paper", rrect(394, f.neck + 172, 486, f.neck + 288, 6), WHITE)
    a.region("clip", rrect(420, f.neck + 140, 460, f.neck + 164, 6), GREY)
    f.arm("arm_r", [(105, f.neck + 30), (170, f.neck + 140), (170, f.neck + 240)], WHITE, SKIN2)
    f.head(SKIN2, HAIR1, "bun")
    a.region("headband", rrect(512 - 90, f.hy - 96, 512 + 90, f.hy - 70, 12), WHITE)
    a.region("mirror", circle(512, f.hy - 84, 24), GREY)
    a.anchor("ride", "passenger", 512, f.foot)
    return a


def chef():
    a = Art("jobs.chef")
    a.part("body", pivot=(512, 940))
    f = Fig(a, 470, 180, 1.0)
    f.legs("pants", DGREY, DGREY, SKIN3)
    f.torso(WHITE)
    for k in range(2):
        for j in range(3):
            a.ink(circle(440 + k * 60, f.neck + 50 + j * 60, 8))
    a.region("neckerchief", poly([(410, f.neck), (530, f.neck), (470, f.neck + 60)], 10), RED)
    f.arm("arm_l", [(-105, f.neck + 30), (-140, f.neck + 140), (-120, f.neck + 230)], WHITE, SKIN3)
    a.part("ladle", parent="body", pivot=(575, f.neck + 30), motion="stir", amp=10)
    f.arm("arm_r", [(105, f.neck + 30), (190, f.neck + 80), (230, f.neck + 20)], WHITE, SKIN3)
    a.region("ladle", union(thick([(700, f.neck + 20), (760, f.neck - 140)], 16), inter(circle(770, f.neck - 160, 50), rect(700, f.neck - 160, 840, f.neck - 100))), GREY)
    a.part("body")
    a.region("pot", union(rrect(560, f.hip - 60, 840, f.hip + 140, 30), rrect(540, f.hip - 80, 860, f.hip - 40, 16)), DGREY)
    a.region("pot_handle_l", rrect(510, f.hip - 20, 560, f.hip + 10, 10), DGREY)
    a.region("pot_band", rect(564, f.hip + 30, 836, f.hip + 60), CORAL)
    f.head(SKIN3, HAIR1, "short")
    a.region("toque_band", rrect(470 - 100, f.hy - 120, 470 + 100, f.hy - 70, 14), WHITE)
    a.region("toque", union(circle(400, f.hy - 190, 70), circle(470, f.hy - 220, 80), circle(540, f.hy - 190, 70), rect(380, f.hy - 190, 560, f.hy - 110)), WHITE)
    a.anchor("table", "table_item", 700, f.hip - 80)
    return a


def gardener():
    a = Art("jobs.gardener")
    a.part("body", pivot=(512, 940))
    a.region("pot", poly([(640, 760), (840, 760), (810, 920), (670, 920)], 20), CORAL)
    a.region("pot_flower_stem", thick([(740, 760), (740, 620)], 14), GREEN)
    a.region("pot_flower", union(*[circle(740 + 30 * math.cos(math.radians(k * 60)), 600 + 30 * math.sin(math.radians(k * 60)), 26) for k in range(6)]), SUN)
    a.region("pot_flower_c", circle(740, 600, 22), ORANGE)
    f = Fig(a, 420, 140, 1.0)
    f.legs("pants", BLUE, BROWN, SKIN1)
    f.torso(RED)
    for k in range(3):
        a.line([(420 - 100, f.neck + 60 + k * 50), (420 + 100, f.neck + 60 + k * 50)], 5)
    a.region("dungaree", union(rrect(420 - 90, f.neck + 110, 420 + 90, f.hip + 40, 24), thick([(420 - 70, f.neck + 120), (420 - 76, f.neck)], 20), thick([(420 + 70, f.neck + 120), (420 + 76, f.neck)], 20)), BLUE)
    a.region("dung_pocket", rrect(380, f.neck + 140, 460, f.neck + 200, 10), SKY)
    f.arm("arm_l", [(-105, f.neck + 30), (-140, f.neck + 140), (-120, f.neck + 230)], RED, SKIN1)
    a.region("shovel", union(thick([(600, f.neck - 20), (600, f.foot - 40)], 18), poly([(560, f.foot - 60), (640, f.foot - 60), (630, f.foot + 20), (600, f.foot + 40), (570, f.foot + 20)], 12)), GREY)
    a.region("shovel_grip", rrect(570, f.neck - 50, 630, f.neck - 10, 12), BROWN)
    f.arm("arm_r", [(105, f.neck + 30), (150, f.neck + 80), (180, f.neck + 20)], RED, SKIN1)
    f.head(SKIN1, HAIR2, "curly")
    a.region("cap", union(inter(ellipse(420, f.hy - 60, 110, 80), rect(300, 0, 540, f.hy - 60)), ellipse(330, f.hy - 64, 80, 22)), LEAF)
    a.anchor("soil", "plant", 740, 760)
    return a


def firefighter():
    a = Art("jobs.firefighter")
    a.part("body", pivot=(512, 940))
    f = Fig(a, 450, 150, 1.0)
    f.legs("pants", ORANGE, DGREY, SKIN4)
    f.torso(ORANGE)
    for y in (f.neck + 140, f.hip - 30):
        a.region(f"reflect{int(y)}", rect(450 - 103, y, 450 + 103, y + 30), YELLOW)
    a.region("tank", rrect(260, f.neck + 20, 340, f.neck + 200, 30), GREY)
    f.arm("arm_l", [(-105, f.neck + 30), (-80, f.neck + 160), (50, f.neck + 190)], ORANGE, SKIN4)
    f.arm("arm_r", [(105, f.neck + 30), (170, f.neck + 120), (210, f.neck + 160)], ORANGE, SKIN4)
    a.part("hose", parent="body", pivot=(660, f.neck + 160), motion="spray", amp=8)
    a.region("hose", thick([(500, f.neck + 200), (620, f.neck + 180), (740, f.neck + 140)], 30), RED)
    a.region("nozzle", rotate(rrect(730, f.neck + 110, 820, f.neck + 160, 12), -15, 760, f.neck + 140), GREY)
    a.region("spray", union(circle(870, f.neck + 100, 30), circle(910, f.neck + 60, 24), circle(940, f.neck + 120, 22)), SKY)
    a.part("body")
    f.head(SKIN4, HAIR1, "short")
    a.region("helmet_brim", ellipse(450, f.hy - 60, 160, 36), RED)
    a.region("helmet", inter(ellipse(450, f.hy - 70, 120, 110), rect(300, 0, 600, f.hy - 60)), RED)
    a.region("badge", star(450, f.hy - 120, 30, 13, 5, -90, 4), SUN)
    a.anchor("ride", "passenger", 450, f.foot)
    return a


def teacher():
    a = Art("jobs.teacher")
    a.part("board", pivot=(700, 900))
    a.region("board_frame", rrect(560, 180, 960, 520, 20), BROWN)
    a.region("board", rrect(584, 204, 936, 496, 12), GREEN)
    a.line([(620, 280), (660, 240), (700, 280), (740, 240)], 6)
    a.line([(620, 360), (800, 360)], 6)
    a.region("abc_a", circle(860, 300, 36), WHITE)
    a.region("easel_legs", union(thick([(640, 510), (600, 900)], 20), thick([(880, 510), (920, 900)], 20)), BROWN)
    a.part("body", pivot=(380, 940))
    f = Fig(a, 360, 160, 1.0)
    f.legs("skirt", TURQ, BROWN, SKIN2)
    f.torso(SUN)
    a.region("cardigan_l", poly([(360 - 105, f.neck), (350, f.neck), (350, f.hip + 20), (360 - 112, f.hip + 20)], 26), CORAL)
    a.region("lanyard", union(thick([(330, f.neck), (360, f.neck + 120), (390, f.neck)], 8), rrect(340, f.neck + 110, 380, f.neck + 160, 6)), BLUE)
    a.region("books", union(rrect(180, f.neck + 180, 300, f.neck + 220, 6), rrect(190, f.neck + 220, 290, f.neck + 260, 6)), LAV)
    f.arm("arm_l", [(-105, f.neck + 30), (-140, f.neck + 140), (-110, f.neck + 200)], CORAL, SKIN2)
    a.part("pointer", parent="body", pivot=(465, f.neck + 30), motion="point", amp=10)
    f.arm("arm_r", [(105, f.neck + 30), (170, f.neck - 20), (220, f.neck - 60)], CORAL, SKIN2)
    a.region("pointer", thick([(590, f.neck - 70), (700, f.neck - 160)], 12), BROWN)
    a.part("body")
    f.head(SKIN2, HAIR1, "long")
    a.region("glass_l", diff(circle(330, f.hy + 2, 28), circle(330, f.hy + 2, 20)), DGREY)
    a.region("glass_r", diff(circle(390, f.hy + 2, 28), circle(390, f.hy + 2, 20)), DGREY)
    a.anchor("board", "carry", 760, 500)
    return a


def builder():
    a = Art("jobs.builder")
    a.part("body", pivot=(512, 940))
    a.region("bricks", union(*[rrect(640 + (k % 2) * 40 + (j * 90), 820 - k * 50, 720 + (k % 2) * 40 + (j * 90), 860 - k * 50, 6) for k in range(3) for j in range(2)]), RED)
    f = Fig(a, 420, 140, 1.0)
    f.legs("pants", BLUE, BROWN, SKIN3)
    f.torso(ORANGE)
    a.region("vest_stripe", rect(420 - 103, f.neck + 120, 420 + 103, f.neck + 150), YELLOW)
    a.region("toolbelt", rrect(420 - 110, f.hip - 50, 420 + 110, f.hip - 10, 10), BROWN)
    a.region("tool_pouch", rrect(470, f.hip - 30, 540, f.hip + 50, 10), TAN)
    f.arm("arm_l", [(-105, f.neck + 30), (-140, f.neck + 140), (-120, f.neck + 230)], ORANGE, SKIN3)
    a.part("hammer", parent="body", pivot=(525, f.neck + 30), motion="tap", amp=14)
    f.arm("arm_r", [(105, f.neck + 30), (180, f.neck + 100), (240, f.neck + 60)], ORANGE, SKIN3)
    a.region("hammer_handle", thick([(660, f.neck + 60), (700, f.neck - 60)], 18), BROWN)
    a.region("hammer_head", rotate(rrect(650, f.neck - 100, 760, f.neck - 50, 10), 15, 700, f.neck - 75), GREY)
    a.part("body")
    f.head(SKIN3, HAIR1, "short")
    a.region("hardhat", union(inter(ellipse(420, f.hy - 60, 120, 100), rect(280, 0, 560, f.hy - 60)), rrect(280, f.hy - 76, 560, f.hy - 50, 12)), YELLOW)
    a.region("hat_ridge", rrect(410, f.hy - 160, 430, f.hy - 70, 8), SUN)
    a.anchor("bricks", "carry", 720, 760)
    return a


def artist():
    a = Art("jobs.artist")
    a.part("easel", pivot=(740, 900))
    a.region("easel", union(thick([(740, 300), (620, 900)], 20), thick([(740, 300), (860, 900)], 20), thick([(740, 300), (740, 900)], 16)), BROWN)
    a.region("canvas", rrect(600, 320, 900, 600, 10), WHITE)
    a.region("canvas_sun", circle(800, 400, 40), SUN)
    a.region("canvas_hill", inter(rrect(600, 320, 900, 600, 10), circle(700, 700, 200)), LEAF)
    a.part("body", pivot=(380, 940))
    f = Fig(a, 360, 160, 1.0)
    f.legs("pants", SKY, CORAL, SKIN1)
    f.torso(WHITE)
    for i, (x, y, c) in enumerate([(320, f.neck + 80, RED), (390, f.neck + 140, BLUE), (340, f.neck + 200, SUN)]):
        a.region(f"splat{i}", blob([(x, y - 20), (x + 22, y - 6), (x + 16, y + 18), (x - 12, y + 20), (x - 22, y)]), c)
    f.neckpiece(RED, "scarf")
    a.region("palette", diff(blob([(140, f.neck + 140), (260, f.neck + 110), (300, f.neck + 190), (220, f.neck + 260), (130, f.neck + 230)]), circle(170, f.neck + 190, 20)), TAN)
    for i, (x, y, c) in enumerate([(200, f.neck + 140, RED), (250, f.neck + 160, BLUE), (250, f.neck + 210, SUN), (200, f.neck + 230, LEAF)]):
        a.region(f"paint{i}", circle(x, y, 16), c)
    f.arm("arm_l", [(-105, f.neck + 30), (-140, f.neck + 140), (-140, f.neck + 180)], WHITE, SKIN1)
    a.part("brush", parent="body", pivot=(465, f.neck + 30), motion="paint", amp=12)
    f.arm("arm_r", [(105, f.neck + 30), (180, f.neck + 80), (240, f.neck + 40)], WHITE, SKIN1)
    a.region("brush", union(thick([(600, f.neck + 40), (660, f.neck - 40)], 12), ellipse(668, f.neck - 54, 14, 24, 35)), BROWN)
    a.part("body")
    f.head(SKIN1, HAIR1, "short")
    a.region("beret", union(ellipse(360, f.hy - 90, 120, 50, -10), circle(360, f.hy - 140, 14)), PURPLE)
    a.anchor("canvas", "carry", 750, 460)
    return a


def astronaut_job():
    a = Art("jobs.astronaut")
    a.part("panel", pivot=(780, 600))
    a.region("station_wall", rrect(620, 200, 980, 840, 30), GREY)
    a.region("panel_door", rrect(680, 320, 920, 600, 20), WHITE)
    for i, (x, c) in enumerate([(730, RED), (800, SUN), (870, LEAF)]):
        a.region(f"light{i}", circle(x, 380, 22), c)
    a.region("socket", rrect(720, 460, 880, 560, 14), DGREY)
    a.region("bolt", circle(800, 510, 22), SUN)
    a.part("body", pivot=(380, 900), motion="float", amp=8)
    f = Fig(a, 360, 150, 0.95)
    a.region("pack", rrect(220, f.neck - 10, 500, f.hip, 40), DGREY)
    for i, sx in enumerate((-1, 1)):
        x = 360 + sx * 56
        a.region(f"leg{i}", rrect(x - 42, f.hip - 30, x + 42, f.foot, 38), WHITE)
        a.region(f"boot{i}", rrect(x - 52, f.foot - 50, x + 52, f.foot + 20, 24), BLUE)
    a.region("suit", rrect(360 - 130, f.neck - 10, 360 + 130, f.hip + 20, 64), WHITE)
    a.region("tether", thick([(250, f.hip - 40), (150, f.hip + 100), (200, f.foot)], 10), SUN)
    a.region("flag_patch", rrect(390, f.neck + 40, 460, f.neck + 90, 8), CORAL)
    f.arm("arm_l", [(-130, f.neck + 40), (-170, f.neck + 160), (-150, f.neck + 240)], WHITE, BLUE, w=62)
    f.arm("arm_r", [(130, f.neck + 40), (260, f.neck + 80), (380, f.neck + 60)], WHITE, BLUE, w=62)
    a.region("wrench", union(thick([(720, f.neck + 60), (790, f.neck + 30)], 18), diff(circle(800, f.neck + 26, 26), poly([(800, f.neck + 26), (830, f.neck), (830, f.neck + 50)]))), GREY)
    a.part("head", parent="body", pivot=(360, f.neck))
    a.region("helmet", circle(360, f.hy, 140), WHITE)
    a.region("visor", ellipse(370, f.hy + 10, 104, 90), SUN)
    a.shine(ellipse(320, f.hy - 30, 30, 16, -30))
    a.region("lamp", rrect(440, f.hy - 140, 500, f.hy - 100, 10), YELLOW)
    a.anchor("dock", "carry", 800, 510)
    return a


def farmer():
    a = Art("jobs.farmer")
    a.part("body", pivot=(512, 940))
    f = Fig(a, 440, 160, 1.0)
    f.legs("pants", BLUE, BROWN, SKIN2)
    f.torso(GREEN)
    for k in range(3):
        a.region(f"plaid{k}", rect(440 - 100 + k * 70, f.neck + 10, 440 - 80 + k * 70, f.hip), OLIVE)
    a.region("pitchfork", union(thick([(250, f.neck - 160), (250, f.foot)], 16), thick([(210, f.neck - 160), (210, f.neck - 220)], 10), thick([(250, f.neck - 160), (250, f.neck - 230)], 10), thick([(290, f.neck - 160), (290, f.neck - 220)], 10), rrect(200, f.neck - 170, 300, f.neck - 150, 8)), GREY)
    f.arm("arm_l", [(-105, f.neck + 30), (-160, f.neck + 80), (-190, f.neck + 30)], GREEN, SKIN2)
    a.region("basket", union(rrect(560, f.neck + 170, 760, f.neck + 300, 24), diff(circle(660, f.neck + 170, 80), circle(660, f.neck + 170, 64), rect(560, f.neck + 170, 760, f.neck + 260))), TAN)
    for i, (x, c) in enumerate([(600, RED), (650, ORANGE), (710, LEAF)]):
        a.region(f"veg{i}", circle(x, f.neck + 168, 30), c)
    f.arm("arm_r", [(105, f.neck + 30), (160, f.neck + 140), (190, f.neck + 200)], GREEN, SKIN2)
    f.head(SKIN2, HAIR2, "short")
    a.region("straw_brim", ellipse(440, f.hy - 70, 200, 40), SAND)
    a.region("straw_top", inter(ellipse(440, f.hy - 80, 110, 90), rect(300, 0, 600, f.hy - 70)), SAND)
    a.region("straw_band", rect(440 - 106, f.hy - 108, 440 + 106, f.hy - 84), RED)
    a.anchor("basket", "carry", 660, f.neck + 170)
    return a


def veterinarian():
    a = Art("jobs.veterinarian")
    a.part("body", pivot=(512, 940))
    f = Fig(a, 420, 150, 1.0)
    f.legs("pants", TURQ, DGREY, SKIN3)
    f.torso(TURQ)
    a.region("scrub_pocket", rrect(450, f.neck + 140, 510, f.neck + 190, 10), MINT)
    a.region("paw_badge", union(circle(370, f.neck + 80, 16), circle(350, f.neck + 56, 8), circle(370, f.neck + 50, 8), circle(390, f.neck + 56, 8)), WHITE)
    f.arm("arm_l", [(-105, f.neck + 30), (-60, f.neck + 180), (80, f.neck + 200)], TURQ, SKIN3)
    a.region("bunny_body", ellipse(580, f.neck + 160, 90, 70), WHITE)
    a.region("bunny_ear1", ellipse(620, f.neck + 30, 22, 60, 15), WHITE)
    a.region("bunny_ear2", ellipse(660, f.neck + 40, 22, 58, 30), WHITE)
    a.region("bunny_head", circle(630, f.neck + 110, 50), WHITE)
    a.region("bandage", rrect(600, f.neck + 66, 664, f.neck + 92, 10, 30), PINK)
    a.ink(circle(640, f.neck + 110, 7)); a.ink(circle(616, f.neck + 112, 7))
    f.arm("arm_r", [(105, f.neck + 30), (150, f.neck + 160), (140, f.neck + 200)], TURQ, SKIN3)
    f.head(SKIN3, HAIR1, "ponytail")
    a.anchor("pet", "carry", 600, f.neck + 160)
    return a


def scientist():
    a = Art("jobs.scientist")
    a.part("body", pivot=(512, 940))
    a.region("bench", rrect(560, 700, 960, 750, 14), GREY)
    a.region("bench_leg", rrect(900, 740, 930, 940, 10), GREY)
    a.region("beaker", union(rrect(780, 560, 860, 700, 10), rrect(770, 550, 870, 570, 6)), WHITE)
    a.region("beaker_liquid", rect(784, 630, 856, 696), LEAF)
    f = Fig(a, 420, 150, 1.0)
    f.legs("pants", PURPLE, DGREY, SKIN1)
    f.torso(LAV, coat=WHITE)
    f.arm("arm_l", [(-105, f.neck + 30), (-140, f.neck + 140), (-120, f.neck + 230)], WHITE, SKIN1)
    a.part("flask", parent="body", pivot=(525, f.neck + 30), motion="swirl", amp=10)
    f.arm("arm_r", [(105, f.neck + 30), (160, f.neck + 110), (210, f.neck + 130)], WHITE, SKIN1)
    a.region("flask", union(poly([(610, f.neck - 10), (690, f.neck - 10), (740, f.neck + 100), (560, f.neck + 100)], 18), rrect(630, f.neck - 70, 670, f.neck, 6)), WHITE)
    a.region("flask_liquid", poly([(590, f.neck + 40), (710, f.neck + 40), (736, f.neck + 96), (564, f.neck + 96)], 10), PINK)
    a.region("bubbles", union(circle(650, f.neck - 110, 18), circle(690, f.neck - 150, 12)), PINK)
    a.part("body")
    f.head(SKIN1, GREY, "curly")
    a.region("goggles", union(circle(420 - 36, f.hy - 40, 32), circle(420 + 36, f.hy - 40, 32), rect(420 - 100, f.hy - 50, 420 + 100, f.hy - 34)), SKY)
    a.anchor("bench", "table_item", 700, 700)
    return a


def musician():
    a = Art("jobs.musician")
    a.part("notes", pivot=(780, 220), motion="rise", amp=14)
    for i, (x, y) in enumerate([(720, 240), (840, 180)]):
        a.region(f"note{i}", union(ellipse(x, y + 60, 30, 22, -20), rect(x + 18, y - 20, x + 30, y + 60), rect(x + 18, y - 20, x + 60, y)), [SUN, CORAL][i])
    a.part("body", pivot=(512, 940))
    f = Fig(a, 440, 170, 1.0)
    f.legs("pants", DGREY, RED, SKIN4)
    f.torso(PURPLE)
    a.region("guitar_neck", rotate(rrect(520, f.neck - 40, 760, f.neck - 6, 8), -30, 520, f.neck + 140), BROWN)
    a.region("headstock", rotate(rrect(740, f.neck - 50, 800, f.neck + 4, 8), -30, 520, f.neck + 140), DGREY)
    a.region("guitar_body", union(circle(400, f.neck + 200, 110), circle(480, f.neck + 130, 80)), ORANGE)
    a.region("soundhole", circle(440, f.neck + 170, 34), DGREY)
    a.region("bridge", rotate(rrect(330, f.neck + 220, 400, f.neck + 240, 6), -30, 365, f.neck + 230), BROWN)
    f.arm("arm_l", [(-105, f.neck + 30), (-90, f.neck + 140), (-20, f.neck + 180)], PURPLE, SKIN4)
    f.arm("arm_r", [(105, f.neck + 30), (200, f.neck + 30), (250, f.neck + 0)], PURPLE, SKIN4)
    f.head(SKIN4, HAIR1, "curly_long")
    a.anchor("stage", "seat", 440, f.foot)
    return a


ALL = [doctor, chef, gardener, firefighter, teacher, builder, artist, astronaut_job, farmer, veterinarian, scientist, musician]
