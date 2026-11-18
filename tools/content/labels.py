"""EN/VI labels for content keys (templates, topics, worlds, props, stickers, parts, palettes)."""
TOPICS = {"animals": ("Animals", "Động vật"), "flowers": ("Flowers", "Hoa"), "nature": ("Nature", "Thiên nhiên"),
          "family": ("Family", "Gia đình"), "machines": ("Machines", "Máy móc"), "vehicles": ("Vehicles", "Phương tiện"),
          "ocean": ("Ocean", "Đại dương"), "dinosaurs": ("Dinosaurs", "Khủng long"), "space": ("Space", "Vũ trụ"),
          "food": ("Food", "Đồ ăn"), "home_city": ("Home & City", "Nhà và thành phố"), "fantasy": ("Fantasy", "Tưởng tượng"),
          "jobs": ("Jobs", "Nghề nghiệp"), "celebrations": ("Celebrations", "Lễ hội và dịp vui")}
T = """
animals.cat Cat|Mèo
animals.dog Dog|Cún
animals.rabbit Rabbit|Thỏ
animals.elephant Elephant|Voi
animals.giraffe Giraffe|Hươu cao cổ
animals.bird Bird|Chim
animals.lion Lion|Sư tử
animals.panda Panda|Gấu trúc
animals.monkey Monkey|Khỉ
animals.sheep Sheep|Cừu
animals.horse Horse|Ngựa
animals.butterfly Butterfly|Bướm
flowers.sunflower Sunflower|Hoa hướng dương
flowers.rose Rose|Hoa hồng
flowers.lotus Lotus|Hoa sen
flowers.tulip Tulip|Hoa tulip
flowers.daisy Daisy|Hoa cúc
flowers.hibiscus Hibiscus|Hoa dâm bụt
flowers.lily Lily|Hoa loa kèn
flowers.orchid Orchid|Hoa lan
flowers.lavender Lavender|Hoa oải hương
flowers.daffodil Daffodil|Hoa thủy tiên vàng
flowers.marigold Marigold|Hoa vạn thọ
flowers.hydrangea Hydrangea|Hoa cẩm tú cầu
nature.tree Tree|Cái cây
nature.sun Sun|Mặt trời
nature.cloud Cloud|Đám mây
nature.rainbow Rainbow|Cầu vồng
nature.mountain Mountain|Ngọn núi
nature.pond Pond|Hồ nhỏ
nature.waterfall Waterfall|Thác nước
nature.mushroom Mushroom|Cây nấm
nature.leaf Leaf|Chiếc lá
nature.moon Moon|Mặt trăng
nature.rain Rain|Cơn mưa
nature.island Island|Hòn đảo
family.child Child|Bạn nhỏ
family.caregiver Caregiver|Người chăm sóc
family.mother Mum|Mẹ
family.father Dad|Bố
family.grandparent Grandparent|Ông bà
family.sibling Sibling|Anh chị em
family.reading Reading together|Cùng đọc sách
family.cooking Cooking together|Cùng nấu ăn
family.gardening Gardening together|Cùng làm vườn
family.picnic Picnic|Dã ngoại
family.petcare Pet care|Chăm thú cưng
family.celebrate Celebrating|Cùng chúc mừng
machines.excavator Digger|Máy xúc
machines.crane Crane|Cần cẩu
machines.tractor Tractor|Máy kéo
machines.robot Robot|Robot
machines.bulldozer Bulldozer|Xe ủi
machines.cement_mixer Cement mixer|Xe trộn bê tông
machines.road_roller Road roller|Xe lu
machines.forklift Forklift|Xe nâng
machines.harvester Harvester|Máy gặt
machines.windmill Windmill|Cối xay gió
machines.conveyor Conveyor belt|Băng chuyền
machines.submarine_robot Sea robot|Robot lặn
vehicles.car Car|Ô tô
vehicles.train Train|Tàu hỏa
vehicles.airplane Aeroplane|Máy bay
vehicles.bicycle Bicycle|Xe đạp
vehicles.bus Bus|Xe buýt
vehicles.boat Boat|Thuyền
vehicles.tram Tram|Tàu điện
vehicles.hot_air_balloon Hot-air balloon|Khinh khí cầu
vehicles.helicopter Helicopter|Trực thăng
vehicles.sailboat Sailboat|Thuyền buồm
vehicles.scooter Scooter|Xe trượt scooter
vehicles.camper Camper van|Xe cắm trại
ocean.clownfish Clownfish|Cá hề
ocean.turtle Sea turtle|Rùa biển
ocean.dolphin Dolphin|Cá heo
ocean.octopus Octopus|Bạch tuộc
ocean.seahorse Seahorse|Cá ngựa
ocean.coral Coral|San hô
ocean.whale Whale|Cá voi
ocean.starfish Starfish|Sao biển
ocean.crab Crab|Con cua
ocean.jellyfish Jellyfish|Sứa
ocean.seashell Seashell|Vỏ sò
ocean.submarine Submarine|Tàu ngầm
dinosaurs.triceratops Triceratops|Khủng long Ba Sừng
dinosaurs.stegosaurus Stegosaurus|Khủng long Phiến Sừng
dinosaurs.brachiosaurus Brachiosaurus|Khủng long Cổ Dài
dinosaurs.tyrannosaurus Tyrannosaurus|Khủng long Bạo Chúa
dinosaurs.ankylosaurus Ankylosaurus|Khủng long Giáp
dinosaurs.parasaurolophus Parasaurolophus|Khủng long Mào Ống
dinosaurs.diplodocus Diplodocus|Khủng long Đuôi Dài
dinosaurs.spinosaurus Spinosaurus|Khủng long Gai Lưng
dinosaurs.iguanodon Iguanodon|Khủng long Ngón Cái
dinosaurs.pachycephalosaurus Pachycephalosaurus|Khủng long Đầu Dày
dinosaurs.egg Dinosaur egg|Trứng khủng long
dinosaurs.fossil Fossil|Hóa thạch
space.rocket Rocket|Tên lửa
space.planet Planet|Hành tinh
space.astronaut Astronaut|Phi hành gia
space.satellite Satellite|Vệ tinh
space.moon_rover Moon rover|Xe tự hành mặt trăng
space.star Star|Ngôi sao
space.station Space station|Trạm vũ trụ
space.comet Comet|Sao chổi
space.ringed_planet Ringed planet|Hành tinh có vành
space.telescope Telescope|Kính thiên văn
space.alien_friend Space friend|Bạn ngoài hành tinh
space.galaxy Galaxy|Thiên hà
food.apple Apple|Quả táo
food.banana Banana|Quả chuối
food.carrot Carrot|Củ cà rốt
food.strawberry Strawberry|Quả dâu tây
food.bread Bread|Bánh mì
food.cake Cake|Bánh ngọt
food.watermelon Watermelon|Dưa hấu
food.broccoli Broccoli|Bông cải xanh
food.soup Soup|Bát canh
food.sandwich Sandwich|Bánh kẹp
food.dumpling Dumplings|Bánh bao
food.fruit_bowl Fruit bowl|Đĩa trái cây
home_city.house House|Ngôi nhà
home_city.school School|Trường học
home_city.park Park|Công viên
home_city.bed Bed|Chiếc giường
home_city.table Table|Cái bàn
home_city.chair Chair|Cái ghế
home_city.bridge Bridge|Cây cầu
home_city.library Library|Thư viện
home_city.shop Shop|Cửa hàng
home_city.lamp Street lamp|Cột đèn
home_city.playground Playground|Sân chơi
home_city.fountain Fountain|Đài phun nước
fantasy.dragon Dragon|Rồng
fantasy.unicorn Unicorn|Kỳ lân
fantasy.castle Castle|Lâu đài
fantasy.fairy_house Fairy house|Nhà tiên
fantasy.magic_tree Magic tree|Cây phép
fantasy.friendly_monster Friendly monster|Quái vật thân thiện
fantasy.flying_cat Flying cat|Mèo biết bay
fantasy.cloud_house Cloud house|Nhà trên mây
fantasy.magic_carpet Magic carpet|Thảm bay
fantasy.crystal Crystal|Pha lê
fantasy.wand Magic wand|Đũa phép
fantasy.story_gate Story gate|Cổng truyện cổ tích
jobs.doctor Doctor|Bác sĩ
jobs.chef Chef|Đầu bếp
jobs.gardener Gardener|Người làm vườn
jobs.firefighter Firefighter|Lính cứu hỏa
jobs.teacher Teacher|Cô giáo
jobs.builder Builder|Thợ xây
jobs.artist Artist|Họa sĩ
jobs.astronaut Astronaut|Phi hành gia
jobs.farmer Farmer|Nông dân
jobs.veterinarian Vet|Bác sĩ thú y
jobs.scientist Scientist|Nhà khoa học
jobs.musician Musician|Nhạc công
celebrations.birthday_cake Birthday cake|Bánh sinh nhật
celebrations.balloons Balloons|Bóng bay
celebrations.gift Present|Hộp quà
celebrations.bunting Bunting|Dây cờ
celebrations.greeting_card Greeting card|Thiệp chúc mừng
celebrations.paper_lantern Paper lantern|Đèn lồng giấy
celebrations.spring_blossom Spring blossom|Cành hoa xuân
celebrations.lantern_festival Lantern festival|Rước đèn
celebrations.winter_tree Winter tree|Cây mùa đông
celebrations.festival_lights Festival lights|Đèn lễ hội
celebrations.kite_festival Kite|Con diều
celebrations.family_banner Family banner|Băng rôn gia đình
"""
TEMPLATES = {l.split(" ", 1)[0]: tuple(l.split(" ", 1)[1].split("|")) for l in T.strip().splitlines()}
assert len(TEMPLATES) == 168
WORLDS = {"garden": ("Garden", "Khu vườn"), "town": ("Town", "Khu phố"), "ocean": ("Ocean", "Đại dương"),
          "dinosaur": ("Dinosaur Valley", "Thung lũng khủng long"), "space": ("Space", "Vũ trụ"),
          "fantasy": ("Fantasy Land", "Xứ sở tưởng tượng"), "celebration": ("Party Yard", "Sân tiệc")}
PROPS = {"soil_block": ("Soil block", "Khối đất"), "watering_can": ("Watering can", "Bình tưới"), "plant_pot": ("Flower pot", "Chậu hoa"),
         "bouquet_wrap": ("Bouquet wrap", "Giấy gói hoa"), "footprint": ("Footprint", "Dấu chân"), "stall": ("Market stall", "Quầy hàng"),
         "basket": ("Basket", "Cái giỏ"), "lamp": ("Lamp", "Cây đèn"), "path": ("Path", "Lối đi"), "book": ("Book", "Cuốn sách"),
         "waypoint": ("Flag", "Lá cờ chỉ đường"), "gift_toy": ("Toy box", "Hộp đồ chơi"), "picnic_mat": ("Picnic mat", "Thảm dã ngoại"),
         "bridge": ("Little bridge", "Cây cầu nhỏ"), "exploration_window": ("Explorer bubble", "Bong bóng thám hiểm"), "leaf": ("Big leaf", "Chiếc lá to"),
         "chair": ("Chair", "Cái ghế"), "table": ("Table", "Cái bàn"), "house": ("Little house", "Ngôi nhà nhỏ"), "park": ("Park corner", "Góc công viên"),
         "food_plate": ("Plate of food", "Đĩa thức ăn")}
STICKER_GROUPS = {"sparkle": ("Sparkles", "Lấp lánh"), "nature": ("Nature", "Thiên nhiên"), "weather": ("Weather", "Thời tiết"),
                  "treats": ("Treats", "Đồ ngọt"), "party": ("Party", "Tiệc vui"), "friends": ("Friends", "Bạn bè")}
PART_KINDS = {"body": ("Body", "Thân"), "ear": ("Ears", "Tai"), "tail": ("Tail", "Đuôi"), "wing": ("Wings", "Cánh"), "hat": ("Hat", "Mũ"), "decor": ("Accessory", "Phụ kiện")}
PARTS = {
 "body.bean": ("Bean", "Hạt đậu"), "body.pear": ("Pear", "Quả lê"), "body.tall": ("Tall friend", "Bạn cao kều"), "body.boxy": ("Boxy bot", "Rô-bốt hộp"),
 "body.star": ("Star", "Ngôi sao"), "body.fluffy": ("Fluffy", "Bông xù"),
 "ear.cat": ("Cat ears", "Tai mèo"), "ear.bunny": ("Bunny ears", "Tai thỏ"), "ear.bear": ("Bear ears", "Tai gấu"), "ear.floppy": ("Floppy ears", "Tai cụp"),
 "ear.elephant": ("Elephant ears", "Tai voi"), "ear.mouse": ("Mouse ears", "Tai chuột"), "ear.fox": ("Fox ears", "Tai cáo"), "ear.antenna": ("Antennae", "Râu ăng-ten"),
 "tail.cat": ("Cat tail", "Đuôi mèo"), "tail.fox": ("Fox tail", "Đuôi cáo"), "tail.puff": ("Puff tail", "Đuôi bông"), "tail.dino": ("Dino tail", "Đuôi khủng long"),
 "tail.fin": ("Fish tail", "Đuôi cá"), "tail.lion": ("Lion tail", "Đuôi sư tử"), "tail.peacock": ("Peacock tail", "Đuôi công"), "tail.curly": ("Curly tail", "Đuôi xoắn"),
 "wing.feather": ("Feather wings", "Cánh lông vũ"), "wing.butterfly": ("Butterfly wings", "Cánh bướm"), "wing.bat": ("Bat wings", "Cánh dơi"),
 "wing.fairy": ("Fairy wings", "Cánh tiên"), "wing.dragon": ("Dragon wings", "Cánh rồng"), "wing.leaf": ("Leaf wings", "Cánh lá"),
 "hat.party": ("Party hat", "Mũ tiệc"), "hat.crown": ("Crown", "Vương miện"), "hat.top_hat": ("Top hat", "Mũ chóp cao"), "hat.beanie": ("Beanie", "Mũ len"),
 "hat.flower_crown": ("Flower crown", "Vòng hoa"), "hat.chef": ("Chef hat", "Mũ đầu bếp"), "hat.cap": ("Cap", "Mũ lưỡi trai"), "hat.wizard": ("Wizard hat", "Mũ phù thủy"),
 "decor.bow_tie": ("Bow tie", "Nơ cổ"), "decor.scarf": ("Scarf", "Khăn quàng"), "decor.beads": ("Beads", "Vòng hạt"), "decor.medal": ("Medal", "Huy chương"),
 "decor.bell_collar": ("Bell collar", "Vòng chuông"), "decor.heart_pocket": ("Heart pocket", "Túi trái tim"), "decor.lei": ("Flower necklace", "Vòng hoa cổ"), "decor.bandana": ("Bandana", "Khăn tam giác"),
}
PALETTES = {"bright_basic": ("Bright", "Tươi sáng"), "warm": ("Warm", "Màu ấm"), "cool": ("Cool", "Màu lạnh"), "pastel": ("Pastel", "Màu phấn"),
            "rainbow": ("Rainbow", "Cầu vồng"), "earth": ("Earth", "Đất và gỗ"), "nature": ("Nature", "Thiên nhiên"), "night": ("Night", "Ban đêm"), "skin": ("Skin tones", "Màu da")}
MILO = {"greet": ("Hi! I'm Milo.", "Chào bạn! Mình là Milo."), "curious": ("What shall we make?", "Mình cùng làm gì nhỉ?"),
        "happy": ("That looks lovely!", "Đẹp quá!"), "guide": ("Try this one!", "Thử cái này nhé!"),
        "listening": ("I'm listening.", "Mình đang nghe nè."), "resting": ("Time for a little rest.", "Nghỉ một chút nhé.")}
