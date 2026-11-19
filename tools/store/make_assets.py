#!/usr/bin/env python3
"""Builds the English Google Play store graphics from real app captures.

Inputs (made by the Robolectric tests, which render the real app UI):
  play-store/raw/icon_512.png                      StoreIconTest (the adaptive launcher icon layers)
  play-store/raw/{phone,tablet7,tablet10}/en/*.png StoreShots*Test

Outputs (play-store/en/):
  icon-512.png                 512x512, 32-bit PNG, full-bleed square (Play applies the mask)
  feature-graphic-1024x500.png 1024x500, 24-bit PNG, no alpha
  phone/NN_name.png            1080x1920 captioned screenshots
  tablet-7in/NN_name.png       1920x1200 captioned screenshots
  tablet-10in/NN_name.png      1920x1200 captioned screenshots

Every pixel of app imagery comes from the captured app; this script only adds the caption band,
a soft frame and the feature-graphic layout. Run:  python3 tools/store/make_assets.py
"""
from __future__ import annotations

import pathlib
import sys

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = pathlib.Path(__file__).resolve().parents[2]
RAW = ROOT / "play-store" / "raw"
OUT = ROOT / "play-store" / "en"
FONT = ROOT / "app/src/main/res/font"

# brand tokens (MASTER_SPEC §3.2)
INK = (0x24, 0x32, 0x4B)
CREAM = (0xFF, 0xF8, 0xEF)
CORAL = (0xFF, 0x7A, 0x6B)
TURQ = (0x3F, 0xC8, 0xBE)
BUTTER = (0xFF, 0xD9, 0x66)
SKY = (0x8B, 0xD4, 0xF6)
LILAC = (0xB9, 0xA7, 0xF2)
MINT = (0x9F, 0xE0, 0xA8)

# order, file stem, caption, band colour
SHOTS = [
    ("01_home", "A whole world to colour", CORAL),
    ("02_studio", "Fill, crayon, marker and watercolour", TURQ),
    ("03_toy", "Your picture comes to life", BUTTER),
    ("04_world", "Play with your toys in 7 worlds", SKY),
    ("05_stories", "28 gentle picture stories", LILAC),
    ("06_book", "Make your own picture book", CORAL),
    ("07_friend", "Invent a friend, then colour it", MINT),
    ("08_lab", "Mix brand-new colours", BUTTER),
]


def font(weight: str, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(str(FONT / f"nunito_{weight}.ttf"), size)


def rounded(im: Image.Image, r: int) -> Image.Image:
    m = Image.new("L", im.size, 0)
    ImageDraw.Draw(m).rounded_rectangle((0, 0, im.width - 1, im.height - 1), r, fill=255)
    out = im.convert("RGBA")
    out.putalpha(m)
    return out


def paste_with_shadow(canvas: Image.Image, im: Image.Image, xy: tuple[int, int], r: int, border: int) -> None:
    x, y = xy
    sh = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    ImageDraw.Draw(sh).rounded_rectangle((x - border, y - border + 14, x + im.width + border, y + im.height + border + 14), r + border, fill=(36, 50, 75, 70))
    canvas.alpha_composite(sh.filter(ImageFilter.GaussianBlur(18)))
    ImageDraw.Draw(canvas).rounded_rectangle((x - border, y - border, x + im.width + border, y + im.height + border), r + border, fill=(255, 255, 255, 255))
    canvas.alpha_composite(rounded(im, r), (x, y))


def fit_text(draw: ImageDraw.ImageDraw, text: str, max_w: int, size: int, weight: str = "extrabold") -> tuple[ImageFont.FreeTypeFont, list[str]]:
    """One line if it fits at >= 80 % of the target size, otherwise two balanced lines (no orphan words)."""
    for s in range(size, int(size * 0.8) - 1, -2):
        f = font(weight, s)
        if draw.textlength(text, font=f) <= max_w:
            return f, [text]
    words = text.split()
    for s in range(size, 30, -2):
        f = font(weight, s)
        best = min((max(draw.textlength(" ".join(words[:i]), font=f), draw.textlength(" ".join(words[i:]), font=f)), i) for i in range(1, len(words)))
        if best[0] <= max_w:
            return f, [" ".join(words[:best[1]]), " ".join(words[best[1]:])]
    raise SystemExit(f"caption does not fit: {text}")


def caption_band(canvas: Image.Image, text: str, color, band_h: int, pad: int, size: int) -> None:
    d = ImageDraw.Draw(canvas)
    d.rectangle((0, 0, canvas.width, band_h), fill=color + (255,))
    # soft hill edge under the band, echoing the app's world ground line
    d.ellipse((-canvas.width * 0.2, band_h - 60, canvas.width * 1.2, band_h + 60), fill=color + (255,))
    f, lines = fit_text(d, text, canvas.width - 2 * pad, size)
    lh = int(f.size * 1.18)
    y = (band_h - lh * len(lines)) // 2 + 6
    for l in lines:
        w = d.textlength(l, font=f)
        d.text(((canvas.width - w) / 2, y), l, font=f, fill=INK)
        y += lh


def phone(name: str, text: str, color) -> Image.Image:
    W, H, band = 1080, 1920, 330
    c = Image.new("RGBA", (W, H), CREAM + (255,))
    caption_band(c, text, color, band, 70, 78)
    raw = Image.open(RAW / "phone" / "en" / f"{name}.png").convert("RGB")
    avail_h = H - band - 40 - 60
    k = min((W - 180) / raw.width, avail_h / raw.height)
    im = raw.resize((round(raw.width * k), round(raw.height * k)), Image.LANCZOS)
    paste_with_shadow(c, im, ((W - im.width) // 2, band + 40), 44, 12)
    return c.convert("RGB")


def tablet(device: str, name: str, text: str, color) -> Image.Image:
    W, H, band = 1920, 1200, 190
    c = Image.new("RGBA", (W, H), CREAM + (255,))
    caption_band(c, text, color, band, 120, 74)
    raw = Image.open(RAW / device / "en" / f"{name}.png").convert("RGB")
    avail_h = H - band - 40 - 40
    k = min((W - 200) / raw.width, avail_h / raw.height)
    im = raw.resize((round(raw.width * k), round(raw.height * k)), Image.LANCZOS)
    paste_with_shadow(c, im, ((W - im.width) // 2, band + 34), 30, 10)
    return c.convert("RGB")


def feature_graphic() -> Image.Image:
    W, H = 1024, 500
    c = Image.new("RGBA", (W, H), CREAM + (255,))
    world = Image.open(RAW / "tablet10" / "en" / "04_world.png").convert("RGB")
    # play area only: below the top buttons, left of the "My toys" button
    crop = world.crop((470, 95, 1569, 1200))   # car, dinosaur, friend, pond and trees
    right_w = 590
    crop = crop.resize((right_w, round(crop.height * right_w / crop.width)), Image.LANCZOS)
    crop = crop.crop((0, crop.height - H, right_w, crop.height)) if crop.height > H else crop
    c.paste(crop, (W - right_w, 0))
    # soft cream fade from the text panel into the world
    fw = 230
    fade = Image.new("L", (fw, H))
    for x in range(fw):
        t = x / (fw - 1)
        ImageDraw.Draw(fade).line((x, 0, x, H), fill=int(255 * (1 - t * t * (3 - 2 * t))))  # smoothstep
    c.paste(Image.new("RGBA", (fw, H), CREAM + (255,)), (W - right_w, 0), fade)
    icon = Image.open(RAW / "icon_512.png").convert("RGBA").resize((150, 150), Image.LANCZOS)
    c.alpha_composite(rounded(icon, 34), (56, 64))
    d = ImageDraw.Draw(c)
    d.text((54, 236), "Colora World", font=font("extrabold", 60), fill=INK)
    tf = font("bold", 27)
    y = 324
    for line in ("Colour, play and make stories.", "Offline. No ads. No accounts."):
        d.text((58, y), line, font=tf, fill=INK)
        y += 40
    return c.convert("RGB")


def main() -> int:
    missing = [p for p in [RAW / "icon_512.png"] + [RAW / dv / "en" / f"{n}.png" for dv in ("phone", "tablet7", "tablet10") for n, _, _ in SHOTS] if not p.exists()]
    if missing:
        print("missing captures (run the StoreIconTest and StoreShots*Test tests first):", *missing, sep="\n  ")
        return 1
    OUT.mkdir(parents=True, exist_ok=True)
    icon = Image.open(RAW / "icon_512.png").convert("RGBA")
    assert icon.size == (512, 512)
    icon.save(OUT / "icon-512.png")
    fg = feature_graphic()
    assert fg.size == (1024, 500) and fg.mode == "RGB"
    fg.save(OUT / "feature-graphic-1024x500.png")
    for sub, fn in (("phone", lambda n, t, col: phone(n, t, col)),
                    ("tablet-7in", lambda n, t, col: tablet("tablet7", n, t, col)),
                    ("tablet-10in", lambda n, t, col: tablet("tablet10", n, t, col))):
        (OUT / sub).mkdir(exist_ok=True)
        for n, t, col in SHOTS:
            im = fn(n, t, col)
            im.save(OUT / sub / f"{n}.png", optimize=True)
    print("wrote", OUT)
    return 0


if __name__ == "__main__":
    sys.exit(main())
