# Content pipeline

Everything in `app/src/main/assets/content` and `app/src/main/assets/audio` is **generated** from source code in `tools/`. Never hand-edit generated files: the SHA-256 index will reject them.

## Requirements
Python 3.11+, `skia-python`, `numpy`, `Pillow`, `fonttools`; `ffmpeg` with libvorbis for audio. On Debian/Ubuntu `skia-python` needs `libegl1`.
```
pip install skia-python numpy pillow fonttools
```

## Commands
| Task | Command |
|-|-|
| Rebuild all art, catalog, thumbnails, contact sheets, similarity report, extras, stories, prompts, locales, index | `python3 tools/art/build_content.py` |
| Only templates | `python3 tools/art/build_content.py --templates-only` |
| Only extras (props, stickers, parts, Milo, worlds, stories, prompts, palettes, locales, index) | `python3 tools/art/build_extras.py` |
| Audio (SFX, toy sets, music loops, manifest) | `python3 tools/audio/synth.py` |
| Validate the pack | `./gradlew :content-tools:validateContent` (args: content dir, `VS`/`LAUNCH`/`FULL`) |
| Quick sheet of one topic while drawing | `cd tools/art && python3 sheet.py subjects.animals /tmp/animals.png` |

After changing anything under `tools/art` or `tools/content`, re-run the build (the integrity index is regenerated last) and then the validator.

## Adding a template
1. Pick the topic module `tools/art/subjects/<topic>.py`; each module exports `ALL` with exactly 12 functions (order = catalog order; first 6 are base tier).
2. Draw with the DSL (`colora_art.py`): `Art(id)` → `part(id, parent, pivot, motion, amp)` → `region(name, path, sample_colour)`; details with `line/ink/shine/blush`; `eyes/smile/cheeks` for faces; `anchor(id, type, x, y)` for interactions. Later regions in the same part cover earlier ones; the exported region is the visible piece.
3. Add capabilities/preset overrides in `build_content.py` (`OVERRIDE`) when the topic default is not right.
4. Add the EN/VI label in `tools/content/labels.py`.
5. Build, open `docs/contact_sheets/<topic>.png` (outline row + sample colour row) and check `content/reports/similarity.json` (`violations` must be empty; mirrors/recolours are not new templates).

## Adding a world
`tools/art/extras/worlds.py`: create `World(id, sky_top, sky_bottom, ground_y)`, add shapes to `far`/`mid`/`near`, zones (`soil`, `water`, `table`, `sky`, `path`, …) and ≥5 anchors. Add its label in `labels.py` and append to `ALL`.

## Adding a story
`tools/content/stories.py`: 2–5 steps (`PLACE`, `ATTACH`, `INTERACT`, `MOOD`, `MOVE`) with matchers `M(templates, fallbacks, capability)`. Every step must be finishable with base-tier templates or props; the validator proves this and also checks that an ATTACH step has a free subject/target pair with a compatible anchor (`AttachRules`). Texts EN/VI go in the step tuple.

## Adding a prompt
`tools/content/prompts.py`: suggested templates, optional alternate wording + templates for when the first are not accessible, palette and destination (`studio`, `world`, `character`, `album`, `lab`, `book`). Illustrations are rendered automatically.

## Voice (pending, see KNOWN_ISSUES K-03)
Planned layout: `assets/audio/voice/<en|vi>/<locale-key>.ogg` with `voice_manifest.json` mapping key → file, duration and licence. The audio build report must list missing keys, clips over 6 s and clipping.

## Integrity
`assets/content/index.json` lists every file with size + SHA-256. The validator fails on mismatches, missing files and unlisted files; the app will verify it on first start of each content version.
