# QA report

Only checks that were actually run are listed. **Nothing has been run on a physical Android device or emulator** (no KVM, see K-02/K-08). The Android UI was exercised through Robolectric with real Skia rendering.

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

## Android build and app tests (session 2)
Commands (all run, all green on 2026-11-18):
```
ANDROID_HOME=/opt/android-sdk ./gradlew assembleOfflineFullDebug assembleStoreDebug
./gradlew :core:engine:test :core:storage:test :content-tools:test :content-tools:validateContent
./gradlew :app:testOfflineFullDebugUnitTest :app:testStoreDebugUnitTest
```
| Suite (per flavor) | Tests | Result | What it proves |
|-|-|-|-|
| `VerticalSliceTest` | 1 | pass | Real taps choose colours and fill 7 regions of the cat, a real crayon drag; saved log has ≥5 fills + the stroke; reveal, world picker, world with the toy; a fresh `Library` + `recover()` (simulated process death) still has the scene with 1 toy whose revision equals the saved commands |
| `AllScreensTest` / `…LandscapeTest` / `…TabletTest` | 3 | pass | Every screen composes and renders with realistic saved work (3 coloured pictures, a character, a 4-toy scene, a 4-page book) on 411×891 dp, 891×411 dp and 1280×800 dp; lab mixing by real clicks |
| `ExporterTest.pngAndPdfAreRealFiles` | 1 | pass | PNG picture 1024 px, outline PNG, scene PNG 1600×800 written and decoded back; cover + 4 book pages rendered |
| `ExporterTest.pdfOnDevice` | 1 | **skipped** | `PdfDocument` has no Robolectric backend (`assumeTrue`); runs on a device |
| **App total** | 12 (6 × 2 flavors) | 10 pass, 2 skipped | |

APK checks: `aapt2 dump permissions` → offlineFull requests **no** INTERNET/network permission; store requests BILLING + INTERNET (Play Billing). targetSdk 36, minSdk 26.

### Screenshots (real renders of the app, `screenshots/`)
- Vertical slice: `01_home` … `10_world_reopened`.
- Portrait phone `p_*`: first_run, home, topics, templates, studio, world, stories, ideas, collection, book, lab, lab_mixed, builder, parent_gate, parent, export, rest.
- Landscape phone `l_*` and tablet `t_*`: home, studio, world, collection, book, lab, builder.
- Export output: `export_picture`, `export_outline`, `export_scene`, `export_book_cover`, `export_book_page1…4`.

Visual fixes made after reviewing them: collection tabs scroll, parent profile list loads on the main thread, readable switch states, named paint pots in the lab, book pages scale with screen height, crisp vector hero on tablets.

## Device checklist (not yet run – needs a phone/tablet)
1. Install `app-offlineFull-debug.apk`, airplane mode on, cold start < 3 s.
2. Colour a picture with every tool; rotate; kill the app from recents mid-stroke; reopen → nothing lost.
3. Toy reveal and world at 60 fps on a low-end phone; drag/attach toys; day/night.
4. Export PNG, PDF (A4) of a book and an MP4 of a scene; open each file in another app.
5. Backup → delete all data → restore → profiles come back as new profiles.
6. TalkBack pass over home, studio, world; Vietnamese language pass.
7. Sound levels and music loop seam (K-04, K-06).

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
Contact sheets in `docs/contact_sheets/` were reviewed at thumbnail size. Device screenshots, TalkBack, performance, offline cold start on a device, PDF/MP4 files and billing: **not run** (see device checklist).
