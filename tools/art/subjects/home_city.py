from colora_art import *
from pal import *


def house():
    a = Art("home_city.house")
    a.part("body", pivot=(512, 900))
    a.region("chimney", rrect(640, 200, 720, 360, 12), BROWN)
    a.region("walls", rrect(220, 440, 804, 880, 24), SUN)
    a.region("roof", poly([(160, 470), (512, 170), (864, 470)], 34), RED)
    a.region("attic", circle(512, 350, 50), SKY)
    a.region("window_l", rrect(280, 520, 420, 650, 20), SKY)
    a.region("window_r", rrect(604, 520, 744, 650, 20), SKY)
    for x in (350, 674):
        a.line([(x, 520), (x, 650)], 5); a.line([(x - 70, 585), (x + 70, 585)], 5)
    a.region("box_l", rrect(270, 650, 430, 690, 12), BROWN)
    a.region("flowers_l", union(circle(300, 640, 20), circle(350, 636, 22), circle(400, 640, 20)), PINK)
    a.part("door", parent="body", pivot=(450, 880), motion="open", amp=30)
    a.region("door", union(rrect(450, 700, 574, 880, 10), circle(512, 700, 62)), TURQ)
    a.ink(circle(552, 800, 9))
    a.part("light", parent="body", pivot=(620, 720), motion="glow", amp=0)
    a.region("lamp", union(rrect(600, 700, 640, 760, 10), poly([(590, 700), (650, 700), (620, 670)], 6)), YELLOW)
    a.anchor("door", "carry", 512, 880); a.anchor("roof", "seat", 512, 300)
    return a


def school():
    a = Art("home_city.school")
    a.part("body", pivot=(512, 920))
    a.region("wing_l", rrect(80, 500, 360, 900, 20), CORAL)
    a.region("wing_r", rrect(664, 500, 944, 900, 20), CORAL)
    a.region("center", rrect(320, 380, 704, 900, 24), PEACH)
    a.region("pediment", poly([(290, 400), (512, 230), (734, 400)], 24), RED)
    a.region("clock", circle(512, 330, 54), WHITE)
    a.line([(512, 330), (512, 296)], 6); a.line([(512, 330), (540, 340)], 6)
    a.region("bell_tower", rrect(470, 140, 554, 240, 16), SUN)
    a.region("bell", union(inter(circle(512, 210, 26), rect(480, 180, 544, 210)), rrect(484, 206, 540, 220, 6)), YELLOW)
    a.region("flagpole", rect(506, 40, 518, 150), GREY)
    a.region("flag", poly([(518, 40), (600, 66), (518, 92)], 8), TURQ)
    for i, x in enumerate((120, 230, 704, 814)):
        a.region(f"win{i}", rrect(x, 560, x + 90, 680, 16), SKY)
        a.region(f"winb{i}", rrect(x, 730, x + 90, 850, 16), SKY)
    a.region("door", union(rrect(440, 700, 584, 900, 10), circle(512, 700, 72)), BROWN)
    a.line([(512, 640), (512, 900)], 5)
    a.region("steps", rrect(400, 880, 624, 920, 10), GREY)
    a.anchor("door", "carry", 512, 900)
    return a


def park():
    a = Art("home_city.park")
    a.part("body", pivot=(512, 900))
    a.region("lawn", ellipse(512, 820, 440, 120), LEAF)
    a.region("path", poly([(440, 940), (584, 940), (560, 720), (464, 720)], 20), SAND)
    a.region("trunk", rrect(230, 520, 280, 760, 16), BROWN)
    a.region("crown", union(circle(255, 430, 120), circle(190, 500, 80), circle(320, 500, 80)), GREEN)
    a.region("bench_back", rrect(600, 600, 860, 640, 12), BROWN)
    a.region("bench_seat", rrect(590, 670, 870, 710, 12), BROWN)
    a.region("bench_legs", union(rrect(610, 700, 636, 780, 8), rrect(824, 700, 850, 780, 8)), DGREY)
    a.region("lamp_post", rrect(500, 380, 524, 720, 8), DGREY)
    a.region("lamp", union(poly([(470, 380), (554, 380), (530, 330), (494, 330)], 10)), YELLOW)
    a.region("bush", scallop(820, 860, 90, 40, 8, 30), OLIVE)
    a.region("flowerbed", union(*[circle(130 + k * 50, 860, 22) for k in range(5)]), PINK)
    a.anchor("bench", "seat", 730, 670); a.anchor("lawn", "plant", 350, 820)
    return a


def bed():
    a = Art("home_city.bed")
    a.part("body", pivot=(512, 880))
    a.region("headboard", union(rrect(140, 360, 300, 840, 40), circle(220, 360, 80)), BROWN)
    a.region("footboard", rrect(800, 560, 900, 840, 30), BROWN)
    a.region("frame", rrect(200, 640, 860, 760, 20), TAN)
    a.region("mattress", rrect(250, 560, 840, 660, 30), WHITE)
    a.region("pillow", blob([(270, 480), (420, 470), (440, 560), (280, 580)]), SKY)
    a.region("blanket", union(rrect(420, 520, 860, 700, 40)), LAV)
    for i, (x, y) in enumerate([(520, 580), (640, 620), (760, 580)]):
        a.region(f"star{i}", star(x, y, 30, 13, 5, -90, 5), SUN)
    a.region("legs", union(rrect(220, 760, 260, 860, 10), rrect(820, 760, 860, 860, 10)), BROWN)
    a.anchor("sleep", "seat", 560, 520)
    return a


def table():
    a = Art("home_city.table")
    a.part("body", pivot=(512, 900))
    a.region("top", rrect(140, 440, 884, 510, 30), BROWN)
    a.region("cloth", poly([(180, 440), (844, 440), (880, 600), (800, 560), (720, 610), (640, 560), (560, 610), (480, 560), (400, 610), (320, 560), (240, 610), (150, 600)], 14), WHITE)
    for k in range(4):
        a.region(f"check{k}", inter(poly([(180, 440), (844, 440), (880, 600), (150, 600)], 14), rect(230 + k * 160, 440, 300 + k * 160, 620)), CORAL)
    a.region("leg_l", rrect(200, 560, 250, 900, 20), BROWN)
    a.region("leg_r", rrect(774, 560, 824, 900, 20), BROWN)
    a.region("vase", union(ellipse(512, 400, 50, 60), rrect(490, 320, 534, 360, 10)), SKY)
    a.region("flower", union(*[circle(512 + 26 * math.cos(math.radians(k * 72)), 290 + 26 * math.sin(math.radians(k * 72)), 22) for k in range(5)]), PINK)
    a.anchor("top_l", "table_item", 330, 440); a.anchor("top_r", "table_item", 700, 440)
    a.anchor("seat_l", "seat", 120, 700); a.anchor("seat_r", "seat", 900, 700)
    return a


def chair():
    a = Art("home_city.chair")
    a.part("body", pivot=(512, 900))
    a.region("back", union(rrect(320, 200, 704, 520, 70)), TURQ)
    a.region("back_heart", heart(512, 350, 70), PINK)
    a.region("seat", rrect(280, 520, 744, 610, 30), TURQ)
    a.region("cushion", rrect(300, 480, 724, 540, 26), SUN)
    for i, x in enumerate((310, 680)):
        a.region(f"leg{i}", rrect(x, 600, x + 36, 900, 14), BROWN)
    a.region("rail", rect(330, 760, 700, 790), BROWN)
    a.anchor("seat", "seat", 512, 480)
    return a


def bridge():
    a = Art("home_city.bridge")
    a.part("body", pivot=(512, 800))
    a.region("water", wave_band(700, 60, 964, 900, 7), SKY)
    arch = diff(rrect(100, 480, 924, 760, 20), inter(circle(512, 820, 280), rect(0, 500, 1024, 1024)))
    a.region("arch", arch, CREAM)
    for i, x in enumerate((180, 300, 724, 844)):
        a.line([(x, 500), (x, 560)], 5)
    a.region("deck", rrect(80, 440, 944, 500, 16), BROWN)
    a.region("rail", union(rect(100, 340, 924, 364), *[rect(110 + k * 100, 340, 130 + k * 100, 440) for k in range(9)]), RED)
    a.region("lamp_l", union(rect(140, 240, 160, 340), circle(150, 230, 30)), YELLOW)
    a.region("lamp_r", union(rect(864, 240, 884, 340), circle(874, 230, 30)), YELLOW)
    a.anchor("deck_l", "seat", 300, 440); a.anchor("deck_r", "seat", 720, 440)
    return a


def library():
    a = Art("home_city.library")
    a.part("body", pivot=(512, 920))
    a.region("building", rrect(140, 360, 884, 880, 20), LAV)
    a.region("roof", poly([(110, 380), (512, 160), (914, 380)], 24), PURPLE)
    a.region("sign", rrect(380, 280, 644, 340, 16), WHITE)
    a.region("book_icon", union(poly([(470, 290), (512, 300), (512, 330), (470, 320)]), poly([(554, 290), (512, 300), (512, 330), (554, 320)])), SKY)
    for i, x in enumerate((190, 300, 650, 760)):
        a.region(f"column{i}", rrect(x, 420, x + 60, 860, 12), WHITE)
    a.region("door", union(rrect(440, 620, 584, 880, 10), circle(512, 620, 72)), BROWN)
    a.region("win_l", rrect(380, 440, 430, 560, 14), SKY)
    a.region("win_r", rrect(594, 440, 644, 560, 14), SKY)
    a.region("steps", rrect(120, 870, 904, 920, 10), GREY)
    a.region("books", union(rrect(840, 820, 870, 880, 4), rrect(874, 830, 904, 880, 4)), CORAL)
    a.anchor("door", "carry", 512, 880)
    return a


def shop():
    a = Art("home_city.shop")
    a.part("body", pivot=(512, 900))
    a.region("building", rrect(160, 300, 864, 880, 20), MINT)
    a.region("sign", rrect(300, 230, 724, 320, 24), SUN)
    a.region("sign_shape", union(circle(420, 275, 26), circle(512, 275, 26), circle(604, 275, 26)), CORAL)
    for i in range(7):
        a.region(f"awning{i}", poly([(160 + i * 100.6, 340), (260 + i * 100.6, 340), (260 + i * 100.6, 440), (210 + i * 100.6, 470), (160 + i * 100.6, 440)], 8), CORAL if i % 2 == 0 else WHITE)
    a.region("window", rrect(200, 500, 560, 760, 20), SKY)
    for i, (x, c) in enumerate([(260, RED), (340, YELLOW), (420, ORANGE), (500, LEAF)]):
        a.region(f"fruit{i}", circle(x, 720, 30), c)
    a.region("door", rrect(620, 500, 800, 880, 16), TURQ)
    a.region("door_glass", rrect(650, 530, 770, 700, 14), SKY)
    a.anchor("counter", "table_item", 380, 760); a.anchor("door", "carry", 710, 880)
    return a


def lamp():
    a = Art("home_city.lamp")
    a.part("body", pivot=(512, 900))
    a.region("rug", ellipse(512, 900, 330, 60), PINK)
    a.region("base", union(ellipse(512, 870, 160, 40), rrect(400, 830, 624, 880, 20)), BROWN)
    a.region("pole", rrect(496, 420, 528, 860, 10), DGREY)
    a.part("shade", parent="body", pivot=(512, 430), motion="glow", amp=0)
    a.region("shade", poly([(360, 430), (664, 430), (590, 200), (434, 200)], 24), TURQ)
    for i, x in enumerate((420, 512, 604)):
        a.region(f"dot{i}", circle(x, 330, 22), SUN)
    a.region("bulb", inter(circle(512, 440, 50), rect(400, 430, 624, 520)), YELLOW)
    a.region("pull", union(thick([(600, 430), (600, 520)], 6), circle(600, 530, 14)), GREY)
    a.anchor("light", "carry", 512, 440)
    return a


def playground():
    a = Art("home_city.playground")
    a.part("body", pivot=(512, 900))
    a.region("ground", ellipse(512, 880, 460, 60), SAND)
    a.region("ladder", union(rect(140, 380, 164, 880), rect(240, 380, 264, 880), *[rect(160, 440 + k * 90, 244, 460 + k * 90) for k in range(5)]), GREY)
    a.region("platform", rrect(120, 360, 420, 400, 10), BROWN)
    a.region("roof", poly([(110, 370), (270, 200), (430, 370)], 20), RED)
    a.region("slide", union(thick([(400, 390), (650, 760), (760, 820)], 70)), SUN)
    a.region("slide_edge", thick([(420, 360), (670, 730), (780, 790)], 18), ORANGE)
    a.region("swing_frame", union(thick([(700, 860), (780, 380)], 22), thick([(940, 860), (860, 380)], 22), thick([(780, 380), (860, 380)], 22)), TURQ)
    a.part("swing", parent="body", pivot=(820, 390), motion="swing", amp=16)
    a.line([(800, 390), (800, 640)], 5); a.line([(840, 390), (840, 640)], 5)
    a.region("swing_seat", rrect(780, 640, 860, 670, 10), CORAL)
    a.anchor("slide_top", "seat", 270, 360); a.anchor("swing", "seat", 820, 640)
    return a


def fountain():
    a = Art("home_city.fountain")
    a.part("water", pivot=(512, 300), motion="splash", amp=10)
    a.region("jet", union(thick([(512, 420), (512, 200)], 30), circle(512, 180, 40), thick([(512, 220), (400, 300), (360, 400)], 22), thick([(512, 220), (624, 300), (664, 400)], 22)), SKY)
    a.part("body", pivot=(512, 900))
    a.region("pool_water", ellipse(512, 760, 380, 80), SKY)
    a.region("basin", diff(union(ellipse(512, 760, 420, 110), rrect(92, 760, 932, 880, 40)), ellipse(512, 760, 380, 80)), GREY)
    a.region("pillar", rrect(470, 480, 554, 720, 16), WHITE)
    a.region("bowl", union(inter(circle(512, 440, 160), rect(340, 440, 690, 640)), ellipse(512, 440, 170, 30)), WHITE)
    a.region("bowl_water", ellipse(512, 440, 140, 20), SKY)
    for i, (x, c) in enumerate([(300, RED), (700, YELLOW)]):
        a.region(f"fish{i}", union(ellipse(x, 770, 40, 22), poly([(x + 36, 770), (x + 70, 750), (x + 70, 790)])), c)
    a.anchor("pool", "water", 512, 760)
    return a


ALL = [house, school, park, bed, table, chair, bridge, library, shop, lamp, playground, fountain]
