# Asset licences

| Asset group | Location | Source | Licence / rights |
|-|-|-|-|
| 168 coloring templates, 21 props, 36 stickers, 44 character parts, Milo (6 expressions), 7 world backdrops, prompt illustrations, thumbnails, contact sheets | `app/src/main/assets/content/**`, `docs/contact_sheets` | Original vector art written as code in `tools/art` for this project; no tracing, stock or third-party art. Milo is an original design (spec §3.4). | Project-owned (owner of this repository). |
| Stories, prompts, labels (EN/VI) | `tools/content`, `assets/content/locales` | Written for this project. | Project-owned. |
| Sound effects (36) and music loops (3) | `app/src/main/assets/audio` | Synthesised from code by `tools/audio/synth.py` (no samples). | Project-owned. |
| Nunito font (Regular, SemiBold, Bold, ExtraBold) | `app/src/main/res/font` | The Nunito Project Authors | SIL Open Font License 1.1 – `licenses/Nunito-OFL.txt`. Bundled, not sold separately. |
| Voice-over | – | Not produced (KNOWN_ISSUES K-03) | – |

Build tools (skia-python, numpy, Pillow, ffmpeg) are used only at build time and are not shipped.
