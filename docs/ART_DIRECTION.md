# Art direction

**Premium interactive picture book, gentle 2D depth.** Round, friendly shapes; one confident ink line; warm paper; soft pastel worlds that never compete with the child's colours.

## Line system (1024 design units)
| Element | Width | Colour |
|-|-|-|
| Part silhouette | 13 | inkNavy `#24324B` |
| Region outline | 9 | inkNavy |
| Facial/detail lines | 6–10 | inkNavy |
| World far / mid / near outlines | 0–5 / 7 / 9 (2048-wide scene) | inkNavy |
Eyes are ink ovals with a white catch-light; cheeks are a translucent blush (`#FF9C93` @60%). Every character has its own silhouette (no shared body with swapped ears).

## UI tokens (spec §3.2)
bgCream `#FFF8EF` · paperWhite `#FFFFFF` · inkNavy `#24324B` · brandCoral `#FF786D` · brandTurquoise `#42C8BD` · sunshine `#FFD66B` · lavender `#B8A0F3` · skyBlue `#8BD4F6` · leafGreen `#9DDC86` · softRose `#FFE6E0` · focusDark `#34548D`. Text is inkNavy on pastel; no small white text on yellow/coral/turquoise.

## Type
Nunito (OFL): titles 24–32sp ExtraBold, labels 16–20sp Bold, parent area 14–18sp Regular/SemiBold. Respect font scaling; allow 40% text expansion.

## Children's palettes
Separate from UI colours: `bright_basic`, `warm`, `cool`, `pastel`, `rainbow`, `earth`, `nature`, `night`, `skin` (10 skin tones) – `assets/content/palettes.json`. White swatches get an outline.

## Worlds
Sky gradient + far (no outline, desaturated) / mid / near layers with parallax 0.15 / 0.45 / 1.0. Contact shadows under toys; no large blurs.

## Mascot
Milo, a small soft paint drop with two eyes and short arms; six expressions (greet, curious, happy, guide, listening, resting). Appears on home, first suggestion and story end only; never covers the canvas.

## Review sheets
`docs/contact_sheets/`: one per topic (outline row + sample-colour row), `props.png`, `stickers.png`, `characters.png` (48 random builds), `milo.png`, `worlds.png` (anchors in red), `prompts.png`.
