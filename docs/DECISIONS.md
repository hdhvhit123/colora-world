# Architecture decisions (ADR log)

Each entry: context → decision → consequences. Newest last. Dates are session dates.

## ADR-001 Repository state and preservation (2026-11-18)
**Context.** The repository `colora-world` was created empty by the owner; the only input was the master spec.
**Decision.** Start a fresh Gradle (Kotlin DSL) project. The spec is kept verbatim at `docs/MASTER_SPEC.md`. No user files existed, so nothing was overwritten.

## ADR-002 Build stack and pinned versions
**Context.** Spec §15.1 requires stable, pinned versions verified against official sources. In the first working session the cloud environment blocked Google hosts (`dl.google.com`, `maven.google.com`), so the Android Gradle Plugin, AndroidX and Compose artifacts could not be resolved or verified there.
**Decision.**
- Gradle wrapper **8.14.3** (committed), Kotlin **2.3.21**, kotlinx.serialization **1.10.0**, coroutines **1.10.2**, JUnit **4.13.2**, JVM target 17. These resolve from Maven Central and are verified by the JVM build.
- Android entries in `gradle/libs.versions.toml` (AGP, Compose BOM, Room, KSP, DataStore, Play Billing, Robolectric…) are **marked UNVERIFIED** until an SDK-enabled session resolves them; that session must confirm each against the official release notes and adjust before writing app code.
- `:app` is included only when an Android SDK is detected (`ANDROID_HOME`, `ANDROID_SDK_ROOT` or `local.properties sdk.dir`), so the pure-JVM modules always build and test.
- `google()` repository is content-filtered to `com.android.*`, `com.google.*`, `androidx.*` groups so the JVM build never depends on it.
**Consequences.** Engine, storage, validator and content pipeline are fully built and tested now; the Android app build is an explicit blocker (see KNOWN_ISSUES K-01) and needs the environment `Colora World (Android build)` that allows Google Maven.

## ADR-003 Module layout (merged modules)
**Context.** Spec §15.2 lists ~18 modules and allows merging to reduce build overhead while keeping boundaries.
**Decision.**
| Module | Covers spec modules |
|-|-|
| `core:engine` (pure Kotlin/JVM) | core:model, core:drawing (commands, geometry, hit-test, brushes), core:world (scene graph, attachment rules, waypoint mover), stories runtime, color lab, character rules, prompts resolver, session budget |
| `core:storage` (pure Kotlin/JVM) | core:data file layer: atomic revisions, recovery, trash, backup export/import |
| `content-tools` (JVM) | validators (`validateContent`) + the Python asset compiler in `tools/` |
| `app` (Android) | design system, renderer Views, features, Room/DataStore wiring, audio, export, parent area, billing (store flavor only) |
Pure-JVM cores keep the risky logic (hit-test, paint replay, attachments, backup safety) unit-testable without an emulator.

## ADR-004 Vector content compiled from code, single renderer contract
**Context.** 168 original templates + props/stickers/parts/worlds must be consistent, distinct and licensable; no stock art.
**Decision.** All art is authored as Python code (`tools/art`, skia-python path ops) in a 1024×1024 design space and compiled to JSON with per-part regions (visible, non-overlapping within a part), rig parts with pivots/motion, details, anchors and capabilities. `tools/art/render.py` is the reference renderer that the Android `ArtRenderer` mirrors (fill → paint → region outline 9 → part silhouette 13 → details). Thumbnails, contact sheets, prompt illustrations and golden images come from this renderer.
**Consequences.** Every asset is reproducible from source (`python3 tools/art/build_content.py`). Duplicate detection is automated (`content/reports/similarity.json`, rule: silhouette IoU ≥ 0.9 AND line-art IoU ≥ 0.6 = violation; currently 0).

## ADR-005 Paint document = command log (no flattened bitmaps as source of truth)
**Decision.** Artwork = ordered `PaintCommand` log (fill region, stroke with seed + points in template units, stickers, clear region/page, paper style) with an undo cursor (≥50 steps; default 60) and periodic replay checkpoints. Bitmaps (thumbnails, toy textures, exports) are derived caches keyed by template version + revision + renderer version.
**Consequences.** Toys keep the child's exact strokes because the toy renderer replays the same commands clipped per part (REQ-03); exports are deterministic.

## ADR-006 Storage: pointer file is the commit point
**Context.** Spec §17.1 asks for temp → fsync → atomic rename → metadata commit with a journal between file commit and Room commit.
**Decision.** Documents live as files: immutable `rev/<id>.bin` + `rev/<id>.meta.json` (SHA-256, sequence, parent) and a `current.json` pointer written last via temp+fsync+rename+dir-fsync. The pointer write *is* the commit; there is no second transactional store to keep in sync. Room (in the app) only holds derived indexes that are rebuilt from pointers at startup if they disagree. Per-document writes are serialised; recovery removes temp files, repairs broken/old pointers to the newest valid revision, and purges trash older than 7 days.
**Consequences.** No window where Room and files disagree in a harmful way; recovery is a pure file scan (tested with simulated crashes).

## ADR-007 Backup format `.colora.zip`
**Decision.** Manifest (format, schema, content version, profiles, per-entry size + SHA-256) + exact library layout. Import: parent gate + SAF, central-directory scan (symlinks via Unix mode bits), strict path allow-list (no traversal/absolute/backslash/foreign files), streamed byte counting with entry/total/ratio limits, full digest + structure validation in staging, ID remapping on collision with JSON reference rewriting, import **as new profiles** by default, single atomic directory move per profile, rollback on failure. Entitlements, PIN and settings are never in the store so they can never be exported or imported.

## ADR-008 Capability-driven world interactions
**Decision.** Logic only reads capability tags (`rideable`, `passenger`, `plantable`, `waterable`, `seatable`, `table_item`, `flying`, `aquatic`, `carryable` + `carrier`, `surface`, `seat_furniture`, `sky`, `water_body`, `bouquet`, …) and anchor types (`passenger`, `seat`, `carry`, `table_item`, `plant`, `water`, `bouquet`). `AttachRules` maps anchor type → accepted capabilities. No template names in interaction code. The validator proves each story step has a free subject/target pair with a compatible anchor.

## ADR-009 Stories and prompts are data
**Decision.** 28 stories and 30 prompts are authored in `tools/content/*.py` and compiled to JSON; step events PLACE/ATTACH/INTERACT/MOOD/MOVE are idempotent per object (+ detail for waypoints). Free fallbacks required by spec (ST-01, 06, 07, 14, 16) are declared and validated. Prompts resolve to accessible content or their alternate wording; never to a locked screen.

## ADR-010 Character builder sockets and scale clamps
**Decision.** Bodies declare sockets (ear_l/ear_r mirrored, tail, wing_l/wing_r mirrored, hat, decor) with position, scale, rotation and z (behind/front). The build computes, for every body × part × socket, the largest part scale that keeps the composite inside the builder stage and stores it in `characters.json.scaleLimits`; the app clamps to it. Combinations that cannot fit at min scale fail the build (currently 0; 5 clamped).

## ADR-011 Fonts
**Decision.** Nunito (SIL OFL 1.1) Regular/SemiBold/Bold/ExtraBold bundled under `app/src/main/res/font`, licence in `licenses/Nunito-OFL.txt`. Verified to contain all Vietnamese precomposed glyphs. No runtime font download.

## ADR-012 Audio is original and synthesised
**Decision.** All SFX and the three music loops are synthesised by `tools/audio/synth.py` (numpy + ffmpeg/libvorbis), so the project owns them outright. The build report records duration, peak/RMS and clipping. Voice-over is **blocked** (no licensed EN/VI voice talent or TTS service); the app will show text for every line and may offer device TTS as an optional fallback only, as the spec allows (see KNOWN_ISSUES K-03).

## ADR-013 Flavors
**Decision.** `offlineFull` (default handover: everything unlocked, no billing, no INTERNET permission) and `store` (base + expansions, real Play Billing adapter only when product details load; never a fake purchase in release). Packs: `pack_nature_plus`, `pack_everyday_plus`, `pack_imagination_plus`; first 6 templates of each topic are base.

## ADR-014 Colour lab model
**Decision.** Art-directed RYB cube interpolation (after Gossett & Chen 2004) with normalised channel strength and white tinting; documented as illustrative, not a pigment simulation. Tests pin red+yellow→orange, yellow+blue→green, red+blue→purple, all three→brown, tint of red→pink.

## ADR-002b Verified Android stack (session 2, 2026-11-18)
**Context.** The second session had an Android SDK (cmdline-tools, platform 36, build-tools 36.0.0) and could reach Google Maven.
**Decision.** Pinned and verified by real builds: Gradle 8.14.3, AGP 8.13.2, Kotlin 2.3.21, Compose BOM 2025.12.01, activity-compose 1.11.0, lifecycle 2.9.4, core-ktx 1.17.0, Play Billing 8.0.0 (store flavor only), Robolectric 4.16, compileSdk/targetSdk 36, minSdk 26. Large Maven Central artifacts redirected to a blocked mirror, so `settings.gradle.kts` lists Google's Maven Central mirror (`maven-central.storage-download.googleapis.com`) first; Robolectric's runtime jars use the same mirror (`robolectric.dependency.repo.url`).
**Consequences.** `assembleOfflineFullDebug` and `assembleStoreDebug` build; the UNVERIFIED marks from ADR-002 are resolved.

## ADR-015 No Room, no DataStore, no Navigation library
**Context.** ADR-006 makes the file store the source of truth; a Room index would be a second copy that can disagree.
**Decision.** Libraries are listed by scanning the store (small per-profile directories); settings use SharedPreferences behind a `StateFlow`; navigation is a small typed back stack (`AppState`). Removed Room, KSP, DataStore and Navigation from the catalog.
**Consequences.** Fewer moving parts and no migration code; listing cost grows with item count (fine for hundreds of items per child, re-evaluate beyond that).

## ADR-016 Eraser removes strokes only
**Decision.** `FillRegion` no longer hides erased strokes; the eraser stroke stays in the log and is drawn as DST_OUT over strokes only. Refilling a region never brings erased strokes back, and the fill colour is not erased (children expect the eraser to rub out crayon, not the paint bucket). Test `fillAfterEraserKeepsErasedStrokesErased`.

## ADR-017 Screenshots and UI tests through Robolectric native graphics
**Context.** No KVM in the cloud container, so no emulator.
**Decision.** UI tests run the real `MainActivity` under Robolectric 4.16 with `graphicsMode=NATIVE` (real Skia rendering); screenshots are taken with `decorView.draw()` into `screenshots/`. The Compose clock is paused (`autoAdvance=false`) because the world and toys animate forever.
**Consequences.** Real pixels of the real app for every screen in portrait phone, landscape phone and tablet. Not covered: `PdfDocument` (no native backend in Robolectric; the PDF test is skipped there, `assumeTrue`), MediaCodec (MP4), TalkBack, real touch latency and frame timing. These need a device (K-02).

## ADR-018 Export pipeline
**Decision.** `Exporter` writes every file to `cache/exports` first, verifies it by reading it back (PNG bounds, `%PDF-` header, MP4 track duration via `MediaExtractor`), then copies it to the SAF destination the parent picked; a failed or cancelled export deletes the half-written document. PNG 2048 or 1024 px with paper or transparent background; outline PNG; PDF A4/Letter via `PdfDocument` (books: cover + pages, pictures: one page with half-inch margins); MP4 H.264 720×1280 or 1280×720, 24 fps, 8 s, YUV420 flexible `Image` input so presentation times are exact (frame/24 s). Scenes pan slowly across the world with toys animating; pictures and characters dance on paper. No codec → the button is disabled with a message.

## ADR-019 Book pages reference immutable revisions
**Decision.** Book items store `art:<doc>:<rev>` / `chr:<doc>:<rev>` / `scene:<doc>` refs; pages render from the referenced revision so later colouring does not change a finished book. Scene pages crop to fill the page, centred on the placed toys.
