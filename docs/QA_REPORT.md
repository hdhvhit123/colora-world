# QA report

Only checks that were actually run are listed. Nothing here was run on an Android device or emulator yet (see KNOWN_ISSUES K-01/K-02).

## Environment of the runs below
Linux cloud container, OpenJDK 21 (Kotlin JVM target 17), Gradle 8.14.3 wrapper, Kotlin 2.3.21, Python 3 with skia-python. Date: 2026-11-18.

## Automated tests (JVM) – all passing
Command: `./gradlew :core:engine:test :core:storage:test :content-tools:test --no-parallel`

| Suite | Tests | Result | What it covers |
|-|-|-|-|
| `TemplateGeometryTest` | 4 | pass | topmost-region hit-test on the cat, head-over-body part priority, tolerance snapping, every region of all 168 templates has area and an interior tap resolves to itself or something drawn above it |
| `PaintDocumentTest` | 9 | pass | undo/redo order incl. fill after stroke, redo branch dropped, ≥50 undo with bounded history, checkpoint replay = full replay, JSON round-trip of all command types, clear-region, eraser + refill, seeded rainbow determinism, malformed stroke rejected |
| `SceneEditorTest` | 8 | pass | rabbit rides car and follows, incompatible/occupied anchors rejected, single parent / no cycles, rocket round trip keeps passenger, storing parent detaches children, idempotent add + 25-step undo, scale clamp 0.6–1.6, elapsed-time waypoint mover without teleport |
| `SmallSystemsTest` | 6 | pass | colour-lab pairs (orange/green/purple/brown/pink), story idempotence + skip, session budget foreground-only/restart/warn-once/extend, unlimited budget, deterministic never-locked suggestions |
| `DocumentStoreTest` | 10 | pass | immutable revisions, corrupt revision fallback + recovery, crash between revision and pointer, missing pointer rebuild, 200 concurrent commits (no lost update, linear parent chain), GC keeps referenced/current/latest, trash restore/purge after 7 days, favourite kept, id traversal rejected, 3 MB payload |
| `BackupServiceTest` | 17 | pass | export→import into a clean library, double import with id remap + reference rewrite, zip-slip, absolute path, backslash path, entitlement file, script file, digest mismatch, missing/unlisted entry, entry for unlisted profile, too many entries, zip-bomb ratio, oversized entry, corrupt revision meta, not-a-zip, symlink entry; every failure leaves the library byte-identical |
| `ContentValidatorTest` | 5 | pass | shipped content valid for FULL; tampered file, missing VI text, story without free path and stray file are each detected |
| **Total** | **59** | **59 pass** | |

## Content validation
`./gradlew :content-tools:validateContent` →
```
ok    index: 602 files verified
ok    templates 168, props 21, stickers 36, worlds 7, character parts 44, stories 28, prompts 30, locale keys 435
CONTENT OK (FULL)
```
Similarity scan (`content/reports/similarity.json`): 0 violations; closest pair vehicles.bus / vehicles.camper (silhouette IoU 0.93 but line-art IoU 0.51 – different drawings).

## Audio build report
`python3 tools/audio/synth.py` → 39 clips (12 UI, 24 toy/animal/action, 3 loops: 65.5 s, 68.6 s, 53.3 s), 1.0 MiB total, no clipping, no over-long effects. Not yet listened to on a device (K-06).

## Visual review
Contact sheets in `docs/contact_sheets/` were reviewed at thumbnail size. Screens, device screenshots, TalkBack, performance, offline cold start, export and billing: **not run** (no APK yet).
