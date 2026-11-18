# Implementation status

Legend: **done** (implemented and verified by the evidence listed) · **partial** · **not started** · **blocked** (external dependency, see KNOWN_ISSUES).
Last update: 2026-11-18, commit on `main` (see `git log`). Evidence commands are listed in `docs/QA_REPORT.md`.

## Milestone view
| Milestone | State |
|-|-|
| Content for FULL (14 topics / 168 templates / 7 worlds / 28 stories / 30 prompts / 44 parts / 36 stickers / 21 props / mascot / palettes / EN-VI content text) | **done** – `validateContent FULL` passes |
| Pure-Kotlin engine (hit-test, paint log, brushes, scene graph + attachments, stories, colour lab, character rules, prompts, session budget) | **done** for logic; Android rendering not started |
| Storage + backup (atomic revisions, recovery, trash, `.colora.zip`) | **done** at file layer; Room index/SAF wiring not started |
| Original audio (SFX, toy sets, 3 loops) | **done**; voice **blocked** |
| Android app (Compose UI, renderer Views, features, export, parent area, flavors) | **not started – blocked** on Android SDK/Google Maven in this environment (K-01) |
| Debug APK, real screenshots, device QA, performance | **blocked** by the above |
| Store billing / release | **blocked** (Play Console, signing – external, K-05) |

## Requirement matrix (spec §23.1)
| ID | Requirement | Status | Evidence / gap |
|-|-|-|-|
| REQ-01 | Consistent, beautiful art | partial | Style system + all art done: `docs/contact_sheets/*.png` (14 topics, props, stickers, characters, milo, worlds, prompts). UI screens not built yet. |
| REQ-02 | Accurate fill, no leaks | partial | Visible-region geometry (no overlap within a part); `TemplateGeometryTest` checks every region of all 168 templates is tappable and resolves to itself or a region drawn above. Clip/zoom fixtures on device pending. |
| REQ-03 | Strokes preserved in toys | partial | Command-log model (ADR-005) + per-part rig; toy renderer and golden images pending app. |
| REQ-04 | Reliable saving | partial | `DocumentStoreTest`: immutable revisions, corrupt-revision fallback, crash-before-pointer recovery, missing pointer rebuild, 200 concurrent commits without lost updates, GC keeps referenced, trash 7 days. Kill/restart on device pending. |
| REQ-05 | Cross-theme world | partial | `SceneEditorTest`: rabbit rides car and follows, incompatible/occupied anchors rejected, single parent, no cycles, rocket returns with passenger, store detaches children, 20+ undo, scale clamp, elapsed-time mover. Validator proves every ATTACH story step has a free compatible pair. UI pending. |
| REQ-06 | FULL content | **done** | `./gradlew :content-tools:validateContent` → `CONTENT OK (FULL)`; similarity report 0 violations; contact sheets. |
| REQ-07 | Builder + colour lab | partial | 6 bodies + 38 parts, socket scale clamps computed for 228 combos (0 unfit); colour lab tests. Builder UI pending. |
| REQ-08 | Book + export PNG/PDF/MP4 | not started | Needs Android APIs (PdfDocument, MediaCodec). |
| REQ-09 | Parent gate + timer | partial | `SessionBudget` tests (foreground-only, restart restore, warn once, extend). Gate UI pending. |
| REQ-10 | Offline | partial | All content/audio/fonts bundled in `app/src/main/assets` + `res/font`; no network code anywhere. Airplane-mode test pending APK. |
| REQ-11 | Accessibility | not started | Requires UI + TalkBack test on device. |
| REQ-12 | EN/VI | partial | 435 content keys in `assets/content/locales/{en,vi}.json`, validator enforces parity and non-empty; Nunito has full Vietnamese glyphs. UI strings pending app. |
| REQ-13 | Performance | not started | Needs device. |
| REQ-14 | Real purchases | blocked | Play Console/products (K-05). No fake purchase code exists. |
| REQ-15 | No unintended data collection | partial | No SDKs/network code; backup excludes entitlements/PIN by construction (tested). Manifest review pending app. |
| REQ-16 | Continuable handover | **done** for current scope | README, this file, DECISIONS, CONTENT_PIPELINE, KNOWN_ISSUES, CHECKPOINT. |

## Feature checklist (spec §5–§14)
| Area | Status | Notes |
|-|-|-|
| 14 topics × 12 templates, tiers (6 base + 6 expansion per topic) | done | `catalog.json`, thumbnails, rig, anchors, capabilities, presets |
| Coloring tools: fill, crayon, marker, watercolor, rainbow, pattern, eraser, stickers, undo/redo | partial | Command model, seeded rainbow, stroke validation done; Android brush rendering pending |
| Toy from artwork + 15 interaction presets | partial | Presets assigned to all templates; preset SFX done; animation runtime pending |
| 7 worlds, 3 layers, ≥5 anchors, zones | done (content) | `worlds/*.json`; world View pending |
| Play/Decorate modes, undo ≥20 | partial | Engine done; UI pending |
| 28 stories with free paths | done (content + runtime) | Story runtime tests; UI pending |
| 30 prompts with illustrations, resolver | done (content + resolver) | UI pending |
| Character builder ≥44 parts | done (content + rules) | UI pending |
| Colour lab | partial | Mixer + tests; UI pending |
| Child's book 4–12 pages | not started | |
| PNG/PDF/MP4 export | not started | |
| Local profiles | partial | `ChildProfile` + per-profile storage; UI pending |
| Parent area, gate, timer, settings | partial | Budget logic; rest pending |
| Backup/restore | done (file layer) | 17 backup tests incl. zip-slip, symlink, bomb, digest, rollback |
| Audio: SFX/music | done | 36 SFX + 3 loops (53–69 s), manifest with peak/RMS; no clipping |
| Voice EN/VI | blocked | K-03 |
| EN/VI UI | partial | content text done |
| Milo mascot (6 expressions) | done (art) | |
| Flavors offlineFull/store, billing adapter | not started | store billing blocked externally |
