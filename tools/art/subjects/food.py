from colora_art import *
from pal import *


def _face(a, x, y, g=90, r=18):
    a.eyes(x, y, g, r)
    a.smile(x, y + r * 2.4, g * 0.5, 0.6, 6)
    a.cheeks(x, y + r * 2, g * 1.6, r * 0.8)


def apple():
    a = Art("food.apple")
    a.part("body", pivot=(512, 860), motion="bounce", amp=8)
    a.region("apple", union(circle(420, 560, 250), circle(604, 560, 250), ellipse(512, 700, 300, 180)), RED)
    a.region("shine", ellipse(380, 440, 50, 90, 20), PINK)
    a.region("stem", thick([(512, 340), (530, 220)], 30), BROWN)
    a.region("leaf", blob([(540, 260), (640, 180), (740, 200), (650, 280)]), LEAF)
    a.line([(560, 250), (700, 205)], 5)
    _face(a, 512, 580, 140, 22)
    a.anchor("table", "table_item", 512, 900)
    return a


def banana():
    a = Art("food.banana")
    a.part("body", pivot=(512, 760), motion="rock", amp=6)
    spine = [(230, 330), (300, 560), (500, 700), (760, 680), (860, 560)]
    a.region("peel", thick(spine, 170), YELLOW)
    a.region("stripe", inter(thick(spine, 170), thick([(p[0], p[1] + 50) for p in spine], 40)), SUN)
    a.region("tip", circle(225, 290, 40), BROWN)
    a.region("end", ellipse(880, 530, 36, 26, -40), OLIVE)
    _face(a, 520, 610, 110, 18)
    a.anchor("table", "table_item", 512, 760)
    return a


def carrot():
    a = Art("food.carrot")
    a.part("top", pivot=(512, 320), motion="sway", amp=8)
    for i, ang in enumerate((-30, 0, 30)):
        a.region(f"leaf{i}", rotate(blob([(512, 330), (470, 220), (512, 90), (554, 220)]), ang, 512, 330), GREEN if i % 2 else LEAF)
    a.part("body", pivot=(512, 900))
    root = blob([(380, 330), (644, 330), (600, 600), (530, 860), (494, 860), (424, 600)], 0.9)
    a.region("root", root, ORANGE)
    for i, y in enumerate((440, 560, 680)):
        a.line([(430 + i * 20, y), (490 + i * 10, y + 10)], 6)
    _face(a, 512, 420, 100, 18)
    a.anchor("table", "table_item", 512, 880); a.anchor("carry", "carry", 512, 500)
    return a


def strawberry():
    a = Art("food.strawberry")
    a.part("body", pivot=(512, 880), motion="bounce", amp=8)
    berry = blob([(512, 300), (700, 330), (780, 470), (700, 700), (512, 880), (324, 700), (244, 470), (324, 330)], 0.9)
    a.region("berry", berry, RED)
    for i, (x, y) in enumerate([(360, 420), (660, 420), (320, 560), (700, 560), (420, 700), (600, 700), (512, 800)]):
        a.region(f"seed{i}", ellipse(x, y, 12, 18), SUN)
    a.region("calyx", union(*[rotate(blob([(512, 320), (470, 280), (512, 200), (554, 280)]), ang, 512, 320) for ang in (-70, -35, 0, 35, 70)]), GREEN)
    a.region("stem", thick([(512, 220), (530, 140)], 22), OLIVE)
    _face(a, 512, 500, 120, 20)
    a.anchor("table", "table_item", 512, 880)
    return a


def bread():
    a = Art("food.bread")
    a.part("body", pivot=(512, 820), motion="squash", amp=6)
    loaf = union(circle(380, 450, 160), circle(644, 450, 160), circle(512, 400, 180), rrect(220, 450, 804, 780, 60))
    a.region("crust", loaf, BROWN)
    a.region("crumb", union(circle(400, 470, 120), circle(624, 470, 120), circle(512, 430, 140), rrect(260, 470, 764, 740, 40)), TAN)
    for i, (x, y) in enumerate([(380, 380), (600, 420), (480, 600)]):
        a.region(f"hole{i}", ellipse(x, y, 22, 16), SAND)
    _face(a, 512, 560, 130, 20)
    a.anchor("table", "table_item", 512, 800)
    return a


def cake():
    a = Art("food.cake")
    a.part("body", pivot=(512, 880))
    a.region("plate", ellipse(512, 860, 330, 60), SKY)
    a.region("layer_bottom", rrect(220, 600, 804, 840, 40), PINK)
    a.region("filling", rect(224, 680, 800, 720), CREAM)
    a.region("layer_top", rrect(270, 420, 754, 610, 40), PINK)
    a.region("icing", union(rrect(260, 400, 764, 470, 30), *[circle(300 + k * 70, 470, 30) for k in range(7)]), WHITE)
    a.region("icing2", union(rrect(214, 590, 810, 640, 24), *[circle(250 + k * 80, 640, 28) for k in range(8)]), WHITE)
    a.region("cherry", circle(512, 340, 50), RED)
    a.region("cherry_stem", thick([(512, 300), (550, 230)], 10), GREEN)
    for i, x in enumerate((340, 680)):
        a.region(f"berry{i}", circle(x, 390, 28), RED)
    a.anchor("table", "table_item", 512, 860)
    return a


def watermelon():
    a = Art("food.watermelon")
    a.part("body", pivot=(512, 760), motion="rock", amp=6)
    a.region("plate", ellipse(512, 860, 420, 60), SKY)
    a.region("rind", inter(circle(512, 380, 440), rect(60, 380, 964, 840)), GREEN)
    a.region("white", inter(circle(512, 380, 400), rect(100, 380, 924, 800)), MINT)
    a.region("flesh", inter(circle(512, 380, 370), rect(130, 380, 894, 770)), RED)
    for i, (x, y) in enumerate([(340, 470), (450, 560), (580, 560), (690, 470), (512, 660), (400, 450), (620, 440)]):
        a.ink(blob([(x, y - 16), (x + 10, y + 4), (x, y + 18), (x - 10, y + 4)]))
    a.eyes(512, 470, 120, 20, lids=True)
    a.smile(512, 520, 60, 0.6, 7)
    a.anchor("table", "table_item", 512, 800)
    return a


def broccoli():
    a = Art("food.broccoli")
    a.part("body", pivot=(512, 900), motion="sway", amp=4)
    a.region("stalk", union(poly([(440, 520), (584, 520), (560, 880), (464, 880)], 30), thick([(470, 600), (380, 520)], 40), thick([(560, 600), (650, 520)], 40)), LEAF)
    for i, (x, y, r) in enumerate([(340, 450, 120), (512, 360, 160), (690, 450, 120), (420, 300, 90), (610, 300, 90)]):
        a.region(f"floret{i}", scallop(x, y, r * 0.7, r * 0.6, 9, r * 0.32), GREEN if i % 2 else OLIVE)
    _face(a, 512, 640, 70, 14)
    a.anchor("table", "table_item", 512, 900)
    return a


def soup():
    a = Art("food.soup")
    a.part("steam", pivot=(512, 280), motion="rise", amp=16)
    for i, x in enumerate((420, 512, 604)):
        a.region(f"steam{i}", thick([(x, 400), (x - 30, 330), (x + 20, 270), (x - 10, 200)], 26), WHITE)
    a.part("body", pivot=(512, 860))
    a.region("saucer", ellipse(512, 840, 330, 60), SKY)
    a.region("bowl", union(inter(circle(512, 520, 300), rect(200, 520, 830, 830)), rrect(220, 790, 804, 840, 20)), TURQ)
    a.region("rim", ellipse(512, 520, 300, 60), WHITE)
    a.region("soup", ellipse(512, 520, 260, 44), ORANGE)
    for i, (x, y) in enumerate([(420, 520), (560, 510), (640, 530)]):
        a.region(f"bit{i}", circle(x, y, 18), LEAF if i % 2 else SUN)
    a.region("spoon", union(thick([(700, 470), (850, 330)], 26), ellipse(690, 490, 50, 30, -40)), GREY)
    for i, x in enumerate((340, 512, 684)):
        a.region(f"dot{i}", circle(x, 680, 26), WHITE)
    a.anchor("table", "table_item", 512, 860)
    return a


def sandwich():
    a = Art("food.sandwich")
    a.part("body", pivot=(512, 820), motion="squash", amp=6)
    tri = lambda dy: poly([(200, 700 + dy), (824, 700 + dy), (512, 260 + dy)], 50)
    a.region("bread_bottom", tri(80), SAND)
    a.region("lettuce", union(tri(40), *[circle(240 + k * 80, 740, 34) for k in range(8)]), LEAF)
    a.region("tomato", inter(tri(20), rect(150, 660, 900, 720)), RED)
    a.region("cheese", union(inter(tri(0), rect(150, 600, 900, 690)), poly([(600, 680), (680, 680), (640, 760)], 10)), YELLOW)
    a.region("bread_top", diff(tri(-20), rect(150, 620, 900, 900)), SAND)
    a.region("crust", diff(tri(-20), tri(-20 + 30), rect(150, 620, 900, 900)), TAN)
    _face(a, 512, 470, 110, 18)
    a.anchor("table", "table_item", 512, 800)
    return a


def dumpling():
    a = Art("food.dumpling")
    a.part("steamer", pivot=(512, 880))
    a.region("basket", rrect(180, 700, 844, 880, 40), TAN)
    for k in range(3):
        a.line([(200, 740 + k * 40), (824, 740 + k * 40)], 5)
    a.part("body", pivot=(512, 700), motion="squash", amp=8)
    bun = union(ellipse(512, 560, 280, 180), circle(512, 400, 110))
    a.region("bun", bun, CREAM)
    a.region("top", circle(512, 340, 50), WHITE)
    for k in range(7):
        ang = 200 + k * 20
        a.line([(512 + 40 * math.cos(math.radians(ang)), 350 + 30 * math.sin(math.radians(ang))), (512 + 220 * math.cos(math.radians(ang)), 560 + 120 * math.sin(math.radians(ang)))], 5)
    a.region("dot", circle(512, 470, 18), RED)
    a.eyes(512, 600, 160, 18)
    a.smile(512, 640, 50, 0.6, 6)
    a.cheeks(512, 630, 260, 18)
    a.anchor("table", "table_item", 512, 880)
    return a


def fruit_bowl():
    a = Art("food.fruit_bowl")
    a.part("body", pivot=(512, 880))
    a.region("grapes", union(*[circle(x, y, 40) for x, y in [(300, 520), (360, 480), (340, 560), (400, 540), (280, 600)]]), PURPLE)
    a.region("orange", circle(512, 470, 110), ORANGE)
    a.region("pear", union(circle(700, 520, 90), circle(690, 420, 60)), LEAF)
    a.region("pear_stem", thick([(690, 360), (700, 310)], 14), BROWN)
    a.region("banana", thick([(380, 440), (480, 380), (620, 380)], 50), YELLOW)
    a.region("bowl", union(inter(circle(512, 560, 320), rect(180, 560, 850, 880)), rrect(170, 540, 854, 600, 30)), CORAL)
    a.region("foot", rrect(420, 840, 604, 900, 20), CORAL)
    for i, x in enumerate((300, 420, 540, 660)):
        a.region(f"dot{i}", circle(x + 30, 700, 22), WHITE)
    a.anchor("table", "table_item", 512, 880)
    return a


ALL = [apple, banana, carrot, strawberry, bread, cake, watermelon, broccoli, soup, sandwich, dumpling, fruit_bowl]
