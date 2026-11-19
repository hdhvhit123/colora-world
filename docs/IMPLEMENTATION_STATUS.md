# Implementation status

Legend: **done** (implemented and verified by the evidence listed) · **done, device-unverified** (implemented, built and exercised in Robolectric, never run on a phone) · **partial** · **blocked** (external, see KNOWN_ISSUES).
Last update: 2026-11-18, session 2, branch `claude/project-thread-yx92gq`. Evidence commands: `docs/QA_REPORT.md`.

## Milestone view
| Milestone | State |
|-|-|
| Content for FULL (14 topics / 168 templates / 7 worlds / 28 stories / 30 prompts / 44 parts / 36 stickers / 21 props / Milo / palettes / EN-VI) | **done** – `validateContent` → `CONTENT OK (FULL)` |
| Engine + storage + backup (pure Kotlin) | **done** – 54 JVM tests |
| Android app, offlineFull flavor (every screen of spec §5–§14) | **done, device-unverified** – builds, 6 Robolectric UI/export tests per flavor, 46 real screenshots |
| Vertical slice: pick → colour → save → toy keeps strokes → place in world → restart keeps everything | **done** – `VerticalSliceTest` drives the real UI with taps and a crayon stroke, then reopens through a fresh `Library` + recovery and asserts the toy replays exactly the saved commands |
| Debug APKs | **done** – `app-offlineFull-debug.apk` (no INTERNET permission), `app-store-debug.apk` |
| Device QA, performance, TalkBack, MP4/PDF files opened | **blocked** – no KVM/emulator here (K-02, K-08, K-09) |
| Store billing / release | **blocked** – owner's Play Console, products, signing (K-05, K-10) |

## Requirement matrix (spec §23.1)
| ID | Requirement | Status | Evidence / gap |
|-|-|-|-|
| REQ-01 | Consistent, beautiful art | done, device-unverified | One design system (`ui/theme`, hand-drawn icon set, Nunito, Milo); screenshots `screenshots/p_*`, `l_*`, `t_*` |
| REQ-02 | Accurate fill, no leaks | done | Hit-test tests on all 168 templates; studio fill by real taps in `VerticalSliceTest`; strokes clipped per visible region |
| REQ-03 | Strokes preserved in toys | done | Toy textures baked from the same command log per part; `06_toy_reveal.png`; command equality asserted after restart |
| REQ-04 | Reliable saving | done (JVM + Robolectric) | Atomic revisions + recovery tests; 500 ms debounced autosave queue, flush on background/back/exit; restart test |
| REQ-05 | Cross-theme world | done, device-unverified | `SceneEditorTest`; world screen with play/decorate, drag, attach to anchors, tray, undo/redo, day/night; `p_world.png` |
| REQ-06 | FULL content | **done** | validator + similarity report |
| REQ-07 | Builder + colour lab | done, device-unverified | `p_builder.png`, `p_lab_mixed.png` (red+yellow → "Orange", labelled in EN/VI) |
| REQ-08 | Book + export PNG/PDF/MP4 | PNG **done**; PDF/MP4 implemented, **device-unverified** | `ExporterTest` writes and re-reads PNG (picture, outline, scene) and renders every book page; PDF test skipped under Robolectric; MP4 needs a device (K-09) |
| REQ-09 | Parent gate + timer | done, device-unverified | hold 2 s + number in words (or PIN, salted SHA-256, lockout); foreground-only budget with warning and rest screen; parent session expires after 2 min |
| REQ-10 | Offline | done | offlineFull manifest has no INTERNET permission (`aapt2 dump permissions`); all content/audio/fonts bundled |
| REQ-11 | Accessibility | partial | content descriptions on all controls, swatches named, 56 dp+ targets, reduced motion, studio accessibility actions; TalkBack walk-through not done (device) |
| REQ-12 | EN/VI | done, device-unverified | UI strings `ui/I18n.kt` (EN/VI parity), content locales, language switch in parent area |
| REQ-13 | Performance | not verified | needs device (K-08). Design: compiled paths, cached paint layer, per-part toy textures (72 MB LRU), no per-frame parsing |
| REQ-14 | Real purchases | blocked | real Play Billing adapter in `store` flavor (signature check, pending never grants, acknowledge); needs products + key (K-10) |
| REQ-15 | No unintended data collection | done | no SDKs, no analytics, `allowBackup=false`, data-extraction rules, AD_ID removed; backup excludes PIN and purchases |
| REQ-16 | Continuable handover | done | README, DECISIONS (ADR-001…019), this file, QA_REPORT, KNOWN_ISSUES, CHECKPOINT |

## Feature checklist (spec §5–§14)
| Area | Status | Where |
|-|-|-|
| First run, home, topics (14 + blank page), template library with levels, locks (store), favourites, continue/again | done | `LibraryScreens.kt` |
| Studio: fill, crayon, marker, watercolour, rainbow, pattern, eraser, stickers, clear region, reset page, undo/redo, 1–4× zoom, palettes + "My colours", skill-based tool sets, autosave badge, portrait/landscape | done | `StudioScreen.kt`, `StudioController.kt`, `render/BrushPainter.kt` |
| Toy reveal, 15 motion presets, toy sounds | done | `RevealScreen`, `render/ToyRig.kt`, `audio/AudioEngine.kt` |
| 7 worlds, parallax, play/decorate, drag, attach/ride/plant/water/sit, tray, undo/redo, day/night, "use new colours" | done | `WorldScreen.kt`, `WorldController.kt` |
| 28 stories with steps, skip, progress, rewards | done | `PlayScreens.kt` (Stories), `WorldController` story runtime |
| 30 ideas with shuffle, free drawing | done | `PlayScreens.kt` (Ideas) |
| Collection: pictures, friends, scenes, books; continue/play/duplicate/favourite/export/bin | done | `PlayScreens.kt` |
| Character builder: 6 bodies, 38 parts, none, size clamp, region painting, try moving, undo | done | `CreateScreens.kt` |
| Colour lab: R/Y/B/white drops, named result, try on paper, keep (24 max) | done | `CreateScreens.kt` |
| Child's book: pages (one/two/scene layouts), add, reorder, duplicate, remove with undo, cover colour, reader, PDF | done (PDF device-unverified) | `CreateScreens.kt`, `render/BookRenderer.kt` |
| Export PNG / outline PNG / PDF A4+Letter / MP4 portrait+landscape, progress, cancel, verify, share | done (PDF/MP4 device-unverified) | `export/Exporter.kt`, `ParentScreens.kt` (Export) |
| Profiles (up to 5, avatars, switch, delete with confirm) | done | `ParentScreens.kt` |
| Parent area: skill, language, music/effects, mute, reduced motion, vibration, play time, stats, backup/restore (SAF, verified), bin restore/delete, PIN, extra content, off-screen ideas, privacy, content check, take-turns reminder, delete all | done | `ParentScreens.kt` |
| Audio: SFX, toy sets, 3 music loops with focus handling | done (not listened to on device, K-06) | `AudioEngine.kt` |
| Voice-over EN/VI | blocked | K-03 |
