"""28 stories (spec §10). Each step: (id, event, subject, target, action, count, en, vi).
Matchers: dict(t=[templates], f=[free fallbacks], c=capability)."""

def M(t=(), f=(), c=None):
    return {"templates": list(t), "fallbacks": list(f), "capability": c}

FAM = ["family.caregiver", "family.mother", "family.father", "family.grandparent"]
FOOD = M(["food.apple", "food.banana", "food.bread", "food.cake", "food.strawberry", "food.carrot"], ["prop.food_plate"], "table_item")
FLOWER = M(["flowers.sunflower", "flowers.rose", "flowers.lotus", "flowers.tulip", "flowers.daisy", "flowers.hibiscus"], [], "plantable")
PASS = M([], [], "passenger")
TABLE = M(["home_city.table"], ["prop.table"], None)

S = []
def story(sid, world, en, vi, reward, icons, steps):
    S.append(dict(id=sid, world=world, en=en, vi=vi, reward=reward, icons=icons, steps=steps))

def st(sid, ev, subj, en, vi, target=None, action=None, count=1):
    return dict(id=sid, event=ev, subject=subj, target=target, action=action, count=count, en=en, vi=vi)

story("ST-01", "garden", "Picnic with Bunny", "Dã ngoại cùng thỏ", "book_page", ["animals.rabbit", "vehicles.car"], [
    st("ride", "ATTACH", M(["animals.rabbit"], [], "passenger"), "Help Bunny into the car.", "Cho thỏ lên xe nhé.", M(["vehicles.car"], [], "rideable")),
    st("arrive", "PLACE", M(["family.picnic"], ["prop.picnic_mat"]), "Lay out a picnic spot.", "Trải chỗ ngồi dã ngoại.",),
    st("food", "ATTACH", FOOD, "Put two snacks on the picnic.", "Đặt hai món ăn lên chỗ dã ngoại.", M(["family.picnic"], ["prop.picnic_mat", "prop.basket"]), count=2),
])
story("ST-02", "garden", "New Friends in the Garden", "Bạn mới trong vườn", "sticker:sticker.nature.flower", ["animals.cat", "animals.dog"], [
    st("cat", "PLACE", M(["animals.cat"], [], "passenger"), "Bring a kitty to the garden.", "Đưa một bạn mèo vào vườn."),
    st("dog", "PLACE", M(["animals.dog"], [], "passenger"), "Now a puppy friend!", "Thêm một bạn cún nữa!"),
    st("plant", "INTERACT", FLOWER, "Plant a flower or a tree.", "Trồng một bông hoa hoặc cái cây.", action="plant"),
    st("hello", "INTERACT", PASS, "Tap the friends to say hello.", "Chạm vào các bạn để chào nhau.", action="tap", count=2),
])
story("ST-03", "garden", "Rainbow Flower Garden", "Vườn hoa cầu vồng", "book_page", ["flowers.tulip", "flowers.sunflower"], [
    st("plant", "INTERACT", FLOWER, "Plant three flowers.", "Trồng ba bông hoa.", action="plant", count=3),
    st("water", "INTERACT", M([], ["prop.watering_can"], "plantable"), "Water each flower.", "Tưới nước cho từng bông.", action="water", count=3),
])
story("ST-04", "celebration", "Flowers for Someone Special", "Bó hoa tặng người thân", "book_page", ["flowers.rose", "family.caregiver"], [
    st("pick", "ATTACH", FLOWER, "Put three flowers in the wrap.", "Cắm ba bông hoa vào giấy gói.", M([], ["prop.bouquet_wrap"], "bouquet"), count=3),
    st("family", "PLACE", M(FAM, [], None), "Who will get the flowers?", "Bó hoa dành tặng ai nhỉ?"),
    st("give", "ATTACH", M([], ["prop.bouquet_wrap"], "bouquet"), "Give the bouquet.", "Trao bó hoa nào.", M(["family.caregiver"], [])),
])
story("ST-05", "garden", "A Day of Clouds and Sun", "Một ngày có mây và nắng", "book_page", ["nature.sun", "nature.cloud"], [
    st("tree", "PLACE", M(["nature.tree"], [], "plantable"), "Place a tree.", "Đặt một cái cây."),
    st("sky", "PLACE", M(["nature.sun", "nature.cloud"], [], "sky"), "Add a sun and a cloud.", "Thêm mặt trời và mây.", count=2),
    st("mood", "MOOD", M(), "Choose how the sky feels.", "Chọn bầu trời hôm nay.", action="any"),
    st("save", "INTERACT", M([], [], None), "Save your scene.", "Lưu cảnh của con.", action="save"),
])
story("ST-06", "garden", "A Bridge over the Pond", "Cây cầu qua hồ nhỏ", "sticker:sticker.weather.raindrop", ["nature.pond", "home_city.bridge"], [
    st("pond", "PLACE", M(["nature.pond"], [], "water_body"), "Find the pond.", "Tìm cái hồ nhỏ."),
    st("bridge", "PLACE", M(["home_city.bridge"], ["prop.bridge"], "bridge"), "Put a bridge over it.", "Đặt cây cầu bắc qua hồ."),
    st("cross", "MOVE", PASS, "Walk a friend across.", "Dẫn một bạn đi qua cầu.", action="cross"),
])
story("ST-07", "town", "Reading Together", "Cùng nhau đọc sách", "book_page", ["family.reading", "home_city.chair"], [
    st("chair", "PLACE", M(["home_city.chair"], ["prop.chair"], "seat_furniture"), "Bring a comfy chair.", "Mang một chiếc ghế êm."),
    st("sit", "ATTACH", M(["family.reading", "family.caregiver"], [], "seatable"), "Sit down to read.", "Ngồi xuống đọc sách nào.", M(["home_city.chair"], ["prop.chair"], "seat_furniture")),
    st("book", "INTERACT", M([], ["prop.book"], "readable"), "Pick a book cover.", "Chọn bìa cuốn sách.", action="open"),
])
story("ST-08", "town", "Family Dinner", "Bữa ăn gia đình", "book_page", ["home_city.table", "food.bread"], [
    st("table", "PLACE", TABLE, "Set up the table.", "Kê bàn ăn."),
    st("food", "ATTACH", FOOD, "Put food on the table.", "Bày món ăn lên bàn.", TABLE, count=2),
    st("sit", "ATTACH", M(FAM + ["family.child", "family.sibling"], [], "seatable"), "Two family members sit together.", "Hai người thân ngồi cùng nhau.", TABLE, count=2),
])
story("ST-09", "garden", "Digger Builds a Garden", "Máy xúc xây vườn", "sticker:sticker.nature.clover", ["machines.excavator"], [
    st("dig", "PLACE", M(["machines.excavator"], [], "carrier"), "Bring the digger.", "Gọi máy xúc tới."),
    st("carry", "ATTACH", M([], ["prop.soil_block"], "soil"), "Carry three soil blocks.", "Chở ba khối đất.", M(["machines.excavator"], [], "carrier"), count=3),
    st("plant", "INTERACT", FLOWER, "Plant a flower in the new bed.", "Trồng hoa vào luống mới.", action="plant"),
])
story("ST-10", "town", "Robot Tidies Up", "Robot giúp xếp đồ", "sticker:sticker.sparkle.star", ["machines.robot"], [
    st("robot", "PLACE", M(["machines.robot"], [], "carrier"), "Switch on the robot.", "Bật robot lên."),
    st("tidy", "ATTACH", M([], ["prop.book", "prop.basket", "prop.gift_toy", "prop.leaf"], "carryable"), "Robot carries three things.", "Robot mang ba món đồ.", M(["machines.robot"], [], "carrier"), count=3),
])
story("ST-11", "town", "Happy Bus Ride", "Chuyến xe buýt vui", "book_page", ["vehicles.bus"], [
    st("bus", "PLACE", M(["vehicles.bus"], [], "rideable"), "Here comes the bus!", "Xe buýt tới rồi!"),
    st("board", "ATTACH", PASS, "Two friends get on.", "Hai bạn lên xe.", M(["vehicles.bus"], [], "rideable"), count=2),
    st("park", "MOVE", M(["vehicles.bus"], [], "rideable"), "Drive to the park.", "Lái tới công viên.", action="arrive"),
])
story("ST-12", "town", "Train through Town", "Tàu hỏa qua khu phố", "book_page", ["vehicles.train", "home_city.house"], [
    st("house", "PLACE", M(["home_city.house"], ["prop.house"], "door"), "Build a house by the track.", "Dựng một ngôi nhà cạnh đường ray."),
    st("train", "PLACE", M(["vehicles.train"], [], "rideable"), "Bring the train.", "Đưa tàu hỏa tới."),
    st("stops", "MOVE", M(["vehicles.train"], [], "rideable"), "Stop at two stations.", "Dừng ở hai ga.", action="waypoint", count=2),
    st("loop", "MOVE", M(["vehicles.train"], [], "rideable"), "Go round the loop!", "Chạy một vòng nào!", action="loop"),
])
story("ST-13", "ocean", "Undersea Garden", "Khu vườn dưới biển", "book_page", ["ocean.coral", "ocean.clownfish"], [
    st("coral", "PLACE", M(["ocean.coral"], [], None), "Grow some coral.", "Trồng rạn san hô."),
    st("fish", "PLACE", M(["ocean.clownfish", "ocean.turtle"], [], "aquatic"), "Add a fish and a turtle.", "Thêm cá và rùa.", count=2),
    st("swim", "INTERACT", M([], [], "aquatic"), "Tap to make them swim.", "Chạm để các bạn bơi.", action="swim"),
])
story("ST-14", "ocean", "Submarine Explorer", "Tàu ngầm khám phá", "sticker:sticker.friends.fish", ["ocean.submarine"], [
    st("sub", "PLACE", M(["ocean.submarine"], ["vehicles.boat", "prop.exploration_window"], "rideable"), "Launch the explorer.", "Thả tàu khám phá xuống nước."),
    st("route", "MOVE", M(["ocean.submarine"], ["vehicles.boat", "prop.exploration_window"], "rideable"), "Visit three places.", "Ghé thăm ba nơi.", action="waypoint", count=3),
    st("look", "INTERACT", M([], [], "aquatic"), "Look at three sea friends.", "Ngắm ba bạn dưới biển.", action="tap", count=3),
])
story("ST-15", "dinosaur", "Dinosaur Park", "Công viên khủng long", "book_page", ["dinosaurs.triceratops", "nature.tree"], [
    st("dinos", "PLACE", M(["dinosaurs.triceratops", "dinosaurs.stegosaurus", "dinosaurs.brachiosaurus"], [], "passenger"), "Bring two dinosaurs.", "Đưa hai bạn khủng long tới.", count=2),
    st("tree", "INTERACT", M(["nature.tree"], [], "plantable"), "Plant a tree.", "Trồng một cái cây.", action="plant"),
    st("lake", "MOVE", M([], [], "passenger"), "Walk to the lake.", "Đi tới hồ nước.", action="arrive"),
])
story("ST-16", "dinosaur", "Little Footprints", "Theo dấu chân nhỏ", "sticker:sticker.nature.acorn", ["dinosaurs.triceratops"], [
    st("dino", "PLACE", M(["dinosaurs.triceratops"], [], "passenger"), "Meet Triceratops.", "Gặp bạn Ba Sừng."),
    st("follow", "MOVE", M(["dinosaurs.triceratops"], [], "passenger"), "Follow three footprints.", "Đi theo ba dấu chân.", action="waypoint", count=3),
    st("leaf", "PLACE", M(["nature.leaf"], ["prop.leaf"], None), "Find the big leaf!", "Tìm thấy chiếc lá to!"),
])
story("ST-17", "space", "Rocket Trip", "Chuyến bay tên lửa", "book_page", ["space.rocket", "space.astronaut"], [
    st("board", "ATTACH", M(["space.astronaut"], [], "passenger"), "Astronaut climbs aboard.", "Phi hành gia lên tàu.", M(["space.rocket"], [], "rideable")),
    st("fly", "MOVE", M(["space.rocket"], [], "rideable"), "Fly to the planet.", "Bay tới hành tinh.", action="arrive"),
    st("home", "MOVE", M(["space.rocket"], [], "rideable"), "Fly back home.", "Bay về nhà.", action="return"),
])
story("ST-18", "space", "A New Planet", "Hành tinh mới", "book_page", ["space.planet", "machines.robot"], [
    st("planet", "PLACE", M(["space.planet"], [], "sky"), "Place a planet.", "Đặt một hành tinh."),
    st("star", "PLACE", M(["space.star"], [], "sky"), "Add a star.", "Thêm một ngôi sao."),
    st("robot", "PLACE", M(["machines.robot"], [], "carrier"), "Send a robot explorer.", "Gửi robot đi khám phá."),
    st("save", "INTERACT", M([], [], None), "Save your planet.", "Lưu hành tinh của con.", action="save"),
])
story("ST-19", "town", "Fruit Stall", "Quầy trái cây", "sticker:sticker.treats.cherries", ["food.apple", "food.banana"], [
    st("stall", "PLACE", M([], ["prop.stall"], "surface"), "Open the fruit stall.", "Mở quầy trái cây."),
    st("fruit", "ATTACH", M(["food.apple", "food.banana", "food.strawberry"], ["prop.food_plate"], "table_item"), "Line up three fruits.", "Xếp ba loại quả.", M([], ["prop.stall"], "surface"), count=3),
    st("basket", "ATTACH", M([], ["prop.basket"], "carryable"), "Hand over a basket.", "Trao giỏ cho khách.", M([], [], "passenger")),
])
story("ST-20", "celebration", "A Little Party", "Bữa tiệc nhỏ", "book_page", ["food.cake"], [
    st("cake", "ATTACH", M(["food.cake", "celebrations.birthday_cake"], [], "table_item"), "Put the cake on the table.", "Đặt bánh lên bàn.", TABLE),
    st("food", "ATTACH", FOOD, "Add two more dishes.", "Thêm hai món nữa.", TABLE, count=2),
    st("guests", "INTERACT", PASS, "Greet the guests.", "Chào đón khách.", action="tap", count=2),
])
story("ST-21", "town", "My Little Room", "Căn phòng bạn nhỏ", "book_page", ["home_city.bed", "family.child"], [
    st("bed", "PLACE", M(["home_city.bed"], [], "seat_furniture"), "Put in a bed.", "Đặt chiếc giường."),
    st("chair", "PLACE", M(["home_city.chair"], ["prop.chair"], "seat_furniture"), "And a chair.", "Thêm cái ghế."),
    st("child", "ATTACH", M(["family.child"], [], "seatable"), "Tuck the child in.", "Cho bạn nhỏ vào chỗ.", M(["home_city.bed", "home_city.chair"], ["prop.chair"], "seat_furniture")),
])
story("ST-22", "town", "Street with a Park", "Khu phố có công viên", "book_page", ["home_city.house", "home_city.park"], [
    st("house", "PLACE", M(["home_city.house"], ["prop.house"], "door"), "Place a house.", "Đặt ngôi nhà."),
    st("park", "PLACE", M(["home_city.park"], ["prop.park"], None), "Make a park.", "Làm công viên."),
    st("road", "PLACE", M([], ["prop.path"], "path"), "Join them with a path.", "Nối bằng con đường."),
    st("drive", "MOVE", M(["vehicles.car"], [], "rideable"), "Drive along!", "Lái xe chạy nào!", action="arrive"),
])
story("ST-23", "fantasy", "Dragon Finds a Playground", "Bạn rồng tìm nơi chơi", "book_page", ["fantasy.dragon", "fantasy.castle"], [
    st("castle", "PLACE", M(["fantasy.castle"], [], "door"), "Build a castle.", "Dựng lâu đài."),
    st("dragon", "PLACE", M(["fantasy.dragon"], [], "flying"), "Here's Dragon!", "Bạn rồng đây rồi!"),
    st("fly", "MOVE", M(["fantasy.dragon"], [], "flying"), "Fly to three places.", "Bay tới ba nơi.", action="waypoint", count=3),
])
story("ST-24", "fantasy", "Fairy Cottage", "Ngôi nhà cổ tích", "sticker:sticker.sparkle.twinkle", ["fantasy.fairy_house"], [
    st("house", "PLACE", M(["fantasy.fairy_house"], ["prop.house"], "door"), "Place the cottage.", "Đặt ngôi nhà nhỏ."),
    st("flowers", "INTERACT", FLOWER, "Plant two flowers.", "Trồng hai bông hoa.", action="plant", count=2),
    st("lamp", "INTERACT", M(["home_city.lamp"], ["prop.lamp"], "light"), "Light the lamp.", "Thắp đèn lên.", action="light"),
])
story("ST-25", "garden", "The Gardener", "Người làm vườn", "book_page", ["jobs.gardener", "machines.tractor"], [
    st("tractor", "ATTACH", M(["jobs.gardener"], [], "passenger"), "Gardener drives the tractor.", "Bác làm vườn lái máy kéo.", M(["machines.tractor"], [], "rideable")),
    st("pot", "ATTACH", M([], ["prop.plant_pot"], "plant_target"), "Carry a flower pot.", "Chở chậu hoa.", M(["machines.tractor"], [], "carrier")),
    st("plant", "INTERACT", FLOWER, "Plant two flowers.", "Trồng hai bông hoa.", action="plant", count=2),
])
story("ST-26", "town", "Chef Sets the Table", "Đầu bếp chuẩn bị bàn", "book_page", ["jobs.chef", "home_city.table"], [
    st("chef", "PLACE", M(["jobs.chef"], [], "seatable"), "Call the chef.", "Mời bác đầu bếp."),
    st("table", "PLACE", TABLE, "Set up the table.", "Kê bàn."),
    st("food", "ATTACH", FOOD, "Serve three dishes.", "Bày ba món ăn.", TABLE, count=3),
    st("greet", "INTERACT", PASS, "Welcome the guests.", "Chào khách tới.", action="tap"),
])
story("ST-27", "celebration", "Colourful Birthday", "Sinh nhật sắc màu", "book_page", ["celebrations.birthday_cake", "celebrations.gift"], [
    st("decorate", "PLACE", M(["celebrations.balloons", "celebrations.bunting"], [], "hangable"), "Hang balloons and bunting.", "Treo bóng bay và cờ.", count=2),
    st("cake", "ATTACH", M(["celebrations.birthday_cake"], ["prop.food_plate"], "table_item"), "Bring the cake.", "Mang bánh sinh nhật ra.", TABLE),
    st("candles", "INTERACT", M(["celebrations.birthday_cake"], ["prop.food_plate"], "table_item"), "Tap to blow the candles.", "Chạm để thổi nến.", action="blow"),
    st("gift", "INTERACT", M(["celebrations.gift"], ["prop.gift_toy"], "openable"), "Open the present!", "Mở quà nào!", action="open"),
])
story("ST-28", "celebration", "A Card for Family", "Thiệp cho gia đình", "book_page", ["celebrations.greeting_card", "family.caregiver"], [
    st("card", "PLACE", M(["celebrations.greeting_card"], [], "openable"), "Pick your card.", "Chọn tấm thiệp."),
    st("decorate", "INTERACT", M(["celebrations.greeting_card"], [], "openable"), "Decorate it with stickers.", "Trang trí bằng sticker.", action="sticker"),
    st("give", "ATTACH", M(["celebrations.greeting_card"], [], "carryable"), "Give the card.", "Trao tấm thiệp.", M(["family.caregiver"] + FAM, [], None)),
    st("save", "INTERACT", M([], [], None), "Save it to your book.", "Lưu vào sách của con.", action="save"),
])
assert len(S) == 28
