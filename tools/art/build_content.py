"""Builds the Colora World content pack from the vector sources.

Outputs (all generated from real bytes, never hand-edited):
  app/src/main/assets/content/templates/<id>.json   vector regions/rig/anchors
  app/src/main/assets/content/thumbs/<id>.webp      256px line-art thumbnails
  app/src/main/assets/content/catalog.json          topics + template index
  docs/contact_sheets/<topic>.png                   outline + sample paint review
  content/reports/similarity.json                   silhouette near-duplicate scan
Run:  python3 tools/art/build_content.py
"""
from __future__ import annotations
import hashlib, importlib, json, os, sys
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
sys.path.insert(0, HERE)
import numpy as np
import skia
from PIL import Image
from render import draw_art, PAPER

TOPICS = ["animals", "flowers", "nature", "family", "machines", "vehicles", "ocean", "dinosaurs",
          "space", "food", "home_city", "fantasy", "jobs", "celebrations"]
WORLDS = ["garden", "town", "ocean", "dinosaur", "space", "fantasy", "celebration"]
PACK_OF_TOPIC = {
    "animals": "pack_nature_plus", "flowers": "pack_nature_plus", "nature": "pack_nature_plus", "ocean": "pack_nature_plus", "dinosaurs": "pack_nature_plus",
    "family": "pack_everyday_plus", "machines": "pack_everyday_plus", "vehicles": "pack_everyday_plus", "food": "pack_everyday_plus", "home_city": "pack_everyday_plus", "jobs": "pack_everyday_plus",
    "space": "pack_imagination_plus", "fantasy": "pack_imagination_plus", "celebrations": "pack_imagination_plus",
}
TOPIC_PRESET = {"animals": "pet_walk", "flowers": "flower_bloom", "nature": "nature_react", "family": "family_wave",
                "machines": "machine_work", "vehicles": "vehicle_ride", "ocean": "ocean_swim", "dinosaurs": "dino_play",
                "space": "space_explore", "food": "food_serve", "home_city": "home_decor", "fantasy": "fantasy_magic",
                "jobs": "family_wave", "celebrations": "celebration"}
TOPIC_CAPS = {"animals": ["passenger", "carryable"], "flowers": ["plantable", "waterable", "carryable"],
              "nature": ["carryable"], "family": ["passenger", "seatable"], "machines": ["carrier"],
              "vehicles": ["rideable"], "ocean": ["aquatic", "carryable"], "dinosaurs": ["passenger"],
              "space": ["carryable"], "food": ["table_item", "carryable"], "home_city": ["decor"],
              "fantasy": ["carryable"], "jobs": ["passenger", "seatable"], "celebrations": ["table_item", "carryable"]}
OVERRIDE = {
    "animals.bird": ("bird_fly", ["flying", "passenger", "carryable"]),
    "animals.butterfly": ("bird_fly", ["flying", "carryable"]),
    "animals.elephant": ("pet_walk", ["passenger", "rideable"]),
    "animals.horse": ("pet_walk", ["passenger", "rideable"]),
    "nature.tree": ("nature_react", ["plantable", "waterable"]),
    "nature.sun": ("nature_react", ["sky", "mood_day"]),
    "nature.cloud": ("nature_react", ["sky", "mood_cloudy"]),
    "nature.rain": ("nature_react", ["sky", "mood_rain"]),
    "nature.moon": ("nature_react", ["sky", "mood_night"]),
    "nature.rainbow": ("nature_react", ["sky", "mood_rainbow"]),
    "nature.mountain": ("nature_react", ["decor"]),
    "nature.pond": ("nature_react", ["water_body", "decor"]),
    "nature.waterfall": ("nature_react", ["water_body", "decor"]),
    "nature.island": ("nature_react", ["water_body", "decor"]),
    "nature.mushroom": ("nature_react", ["plantable", "carryable"]),
    "nature.leaf": ("nature_react", ["carryable"]),
    "family.picnic": ("home_decor", ["surface", "decor"]),
    "machines.robot": ("robot_dance", ["passenger", "carrier", "carryable"]),
    "machines.windmill": ("machine_work", ["decor"]),
    "machines.conveyor": ("machine_work", ["carrier", "decor"]),
    "machines.tractor": ("machine_work", ["carrier", "rideable"]),
    "machines.submarine_robot": ("ocean_swim", ["aquatic", "carrier", "rideable"]),
    "vehicles.airplane": ("vehicle_ride", ["rideable", "flying"]),
    "vehicles.helicopter": ("vehicle_ride", ["rideable", "flying"]),
    "vehicles.hot_air_balloon": ("vehicle_ride", ["rideable", "flying"]),
    "vehicles.boat": ("vehicle_ride", ["rideable", "aquatic"]),
    "vehicles.sailboat": ("vehicle_ride", ["rideable", "aquatic"]),
    "ocean.coral": ("home_decor", ["aquatic", "decor"]),
    "ocean.seashell": ("home_decor", ["aquatic", "carryable"]),
    "ocean.starfish": ("ocean_swim", ["aquatic", "carryable"]),
    "ocean.turtle": ("ocean_swim", ["aquatic", "rideable"]),
    "ocean.whale": ("ocean_swim", ["aquatic", "rideable"]),
    "ocean.dolphin": ("ocean_swim", ["aquatic", "rideable"]),
    "ocean.submarine": ("vehicle_ride", ["aquatic", "rideable"]),
    "dinosaurs.brachiosaurus": ("dino_play", ["passenger", "rideable"]),
    "dinosaurs.egg": ("home_decor", ["carryable"]),
    "dinosaurs.fossil": ("home_decor", ["carryable", "decor"]),
    "space.rocket": ("space_explore", ["rideable", "flying"]),
    "space.astronaut": ("family_wave", ["passenger", "seatable"]),
    "space.moon_rover": ("vehicle_ride", ["rideable"]),
    "space.alien_friend": ("dino_play", ["passenger"]),
    "space.planet": ("space_explore", ["sky", "decor"]),
    "space.ringed_planet": ("space_explore", ["sky", "decor"]),
    "space.star": ("space_explore", ["sky", "carryable"]),
    "space.galaxy": ("space_explore", ["sky", "decor"]),
    "space.comet": ("space_explore", ["sky", "flying"]),
    "space.station": ("space_explore", ["decor", "flying"]),
    "space.satellite": ("space_explore", ["flying", "carryable"]),
    "space.telescope": ("home_decor", ["decor", "carryable"]),
    "home_city.house": ("home_decor", ["decor", "door", "light"]),
    "home_city.bed": ("home_decor", ["seat_furniture", "decor"]),
    "home_city.chair": ("home_decor", ["seat_furniture", "carryable"]),
    "home_city.table": ("home_decor", ["surface", "carryable"]),
    "home_city.lamp": ("home_decor", ["light", "carryable"]),
    "home_city.park": ("home_decor", ["seat_furniture", "decor"]),
    "home_city.bridge": ("home_decor", ["decor", "bridge"]),
    "home_city.fountain": ("home_decor", ["water_body", "decor"]),
    "fantasy.dragon": ("fantasy_magic", ["flying", "rideable", "passenger"]),
    "fantasy.unicorn": ("pet_walk", ["rideable", "passenger"]),
    "fantasy.magic_carpet": ("fantasy_magic", ["flying", "rideable"]),
    "fantasy.flying_cat": ("bird_fly", ["flying", "passenger"]),
    "fantasy.friendly_monster": ("dino_play", ["passenger"]),
    "fantasy.castle": ("home_decor", ["decor", "door"]),
    "fantasy.fairy_house": ("home_decor", ["decor", "door", "light"]),
    "fantasy.cloud_house": ("fantasy_magic", ["decor", "flying"]),
    "fantasy.magic_tree": ("nature_react", ["plantable", "waterable"]),
    "fantasy.story_gate": ("fantasy_magic", ["decor"]),
    "celebrations.birthday_cake": ("celebration", ["table_item", "candles"]),
    "celebrations.gift": ("celebration", ["table_item", "carryable", "openable"]),
    "celebrations.greeting_card": ("celebration", ["carryable", "openable"]),
    "celebrations.bunting": ("celebration", ["decor", "hangable"]),
    "celebrations.festival_lights": ("celebration", ["decor", "hangable", "light"]),
    "celebrations.family_banner": ("celebration", ["decor", "hangable"]),
    "celebrations.paper_lantern": ("celebration", ["hangable", "light", "carryable"]),
    "celebrations.lantern_festival": ("celebration", ["light", "carryable"]),
    "celebrations.winter_tree": ("celebration", ["plantable", "decor", "light"]),
    "celebrations.kite_festival": ("celebration", ["flying", "carryable"]),
    "celebrations.spring_blossom": ("celebration", ["table_item", "decor"]),
}


def difficulty(n):
    return "simple" if n <= 8 else ("medium" if n <= 18 else "detailed")


def silhouette_mask(art, px=96):
    surf = skia.Surface(px, px)
    c = surf.getCanvas(); c.clear(skia.ColorWHITE)
    b = art["contentBounds"]; w = b[2] - b[0]; h = b[3] - b[1]; s = (px - 8) / max(w, h)
    c.translate(4 + (px - 8 - w * s) / 2, 4 + (px - 8 - h * s) / 2); c.scale(s, s); c.translate(-b[0], -b[1])
    from colora_art import from_svg
    for p in art["parts"]:
        c.drawPath(from_svg(p["silhouette"]), skia.Paint(Color=skia.ColorBLACK, AntiAlias=False))
    arr = np.array(surf.makeImageSnapshot().toarray())[:, :, 0]
    return arr < 128


def ink_mask(art, px=96):
    """Dilated line-art mask: compares interior design, not just outline."""
    surf = skia.Surface(px, px)
    c = surf.getCanvas(); c.clear(skia.ColorWHITE)
    b = art["contentBounds"]; w = b[2] - b[0]; h = b[3] - b[1]; s = (px - 8) / max(w, h)
    c.translate(4 + (px - 8 - w * s) / 2, 4 + (px - 8 - h * s) / 2); c.scale(s, s); c.translate(-b[0], -b[1])
    draw_art(c, art)
    g = np.array(surf.makeImageSnapshot().toarray())[:, :, 0] < 140
    d = g.copy()
    for dx in (-1, 0, 1):
        for dy in (-1, 0, 1):
            d |= np.roll(np.roll(g, dx, 0), dy, 1)
    return d


def main():
    out_t = os.path.join(ROOT, "app/src/main/assets/content/templates")
    out_th = os.path.join(ROOT, "app/src/main/assets/content/thumbs")
    out_cs = os.path.join(ROOT, "docs/contact_sheets")
    out_rep = os.path.join(ROOT, "content/reports")
    for d in (out_t, out_th, out_cs, out_rep):
        os.makedirs(d, exist_ok=True)
    catalog = {"schemaVersion": 1, "contentVersion": 1, "topics": [], "templates": []}
    masks = {}
    for topic in TOPICS:
        mod = importlib.import_module(f"subjects.{topic}")
        assert len(mod.ALL) == 12, (topic, len(mod.ALL))
        ids = []
        sheet = skia.Surface(256 * 6, 256 * 4)
        sc = sheet.getCanvas(); sc.clear(skia.Color(0xFF, 0xF8, 0xEF))
        for idx, fn in enumerate(mod.ALL):
            art_obj = fn()
            tid = art_obj.id
            assert tid.startswith(topic + "."), tid
            preset, caps = OVERRIDE.get(tid, (TOPIC_PRESET[topic], TOPIC_CAPS[topic]))
            tier = "base" if idx < 6 else "expansion"
            meta = {
                "contentVersion": 1, "topicId": topic, "labelKey": f"template.{tid}",
                "access": {"tier": tier, "packId": "base" if tier == "base" else PACK_OF_TOPIC[topic]},
                "interactionPreset": preset, "capabilities": caps,
                "audioSetId": f"sfx.{preset}", "compatibleWorlds": WORLDS,
            }
            art = art_obj.to_json(meta)
            n = len(art["regions"])
            art["difficulty"] = difficulty(n)
            path = os.path.join(out_t, f"{tid}.json")
            with open(path, "w") as f:
                json.dump(art, f, separators=(",", ":"))
            # thumbnail (line art the child will colour)
            surf = skia.Surface(256, 256); c = surf.getCanvas(); c.clear(PAPER)
            c.scale(256 / 1024, 256 / 1024); draw_art(c, art)
            img = Image.fromarray(surf.makeImageSnapshot().toarray()[:, :, [2, 1, 0, 3]] if False else surf.makeImageSnapshot().convert(alphaType=skia.kUnpremul_AlphaType, colorType=skia.kRGBA_8888_ColorType).toarray())
            img.convert("RGB").save(os.path.join(out_th, f"{tid}.webp"), "WEBP", quality=88, method=6)
            for k, samples in enumerate((False, True)):
                x = (idx % 6) * 256; y = ((idx // 6) * 2 + k) * 256
                sc.save(); sc.translate(x + 4, y + 4)
                sc.drawRect(skia.Rect(0, 0, 248, 248), skia.Paint(Color=PAPER))
                sc.scale(248 / 1024, 248 / 1024); draw_art(sc, art, use_samples=samples); sc.restore()
            masks[tid] = (silhouette_mask(art), ink_mask(art))
            catalog["templates"].append({"id": tid, "topicId": topic, "index": idx, "tier": art["access"]["tier"],
                                         "packId": art["access"]["packId"], "difficulty": art["difficulty"],
                                         "regions": n, "preset": preset, "capabilities": caps,
                                         "file": f"templates/{tid}.json", "thumb": f"thumbs/{tid}.webp"})
            ids.append(tid)
        sheet.makeImageSnapshot().save(os.path.join(out_cs, f"{topic}.png"), skia.kPNG)
        catalog["topics"].append({"id": topic, "labelKey": f"topic.{topic}", "templates": ids,
                                  "coverTemplate": ids[0], "pack": PACK_OF_TOPIC[topic]})
        print(topic, "ok")
    with open(os.path.join(ROOT, "app/src/main/assets/content/catalog.json"), "w") as f:
        json.dump(catalog, f, indent=1)
    # near-duplicate silhouette scan (IoU of normalised masks)
    keys = sorted(masks)
    worst = []
    for i in range(len(keys)):
        for j in range(i + 1, len(keys)):
            (sa, ia), (sb, ib) = masks[keys[i]], masks[keys[j]]
            iou = float((sa & sb).sum()) / float((sa | sb).sum() or 1)
            ink = float((ia & ib).sum()) / float((ia | ib).sum() or 1)
            worst.append((round(iou * 0.5 + ink * 0.5, 3), round(iou, 3), round(ink, 3), keys[i], keys[j]))
    worst.sort(reverse=True)
    rep = {"method": "score = mean(silhouette IoU, dilated line-art IoU) on 96px renders normalised to content bounds",
           "rule": "violation when silhouette IoU >= 0.9 AND line-art IoU >= 0.6 (same outline AND same inner drawing)",
           "top20": [{"score": w[0], "silhouetteIoU": w[1], "lineArtIoU": w[2], "a": w[3], "b": w[4]} for w in worst[:20]],
           "violations": [{"silhouetteIoU": w[1], "lineArtIoU": w[2], "a": w[3], "b": w[4]} for w in worst if w[1] >= 0.9 and w[2] >= 0.6]}
    with open(os.path.join(out_rep, "similarity.json"), "w") as f:
        json.dump(rep, f, indent=1)
    print("templates:", len(catalog["templates"]), "most similar:", rep["top20"][0], "violations:", len(rep["violations"]))


if __name__ == "__main__":
    main()
    if "--templates-only" not in sys.argv:
        import build_extras
        build_extras.main()
