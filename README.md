# Colora World

An offline, ad-free Android colouring and play app for young children in an interactive picture-book style: colour a picture, watch it become a toy that keeps every stroke, place it in a living world, play stories, build characters, mix colours and make a book. English and Vietnamese.

Full product spec: [`docs/MASTER_SPEC.md`](docs/MASTER_SPEC.md). Current state: [`docs/IMPLEMENTATION_STATUS.md`](docs/IMPLEMENTATION_STATUS.md). Where to continue: [`docs/CHECKPOINT.md`](docs/CHECKPOINT.md).

## What is in the repository today
| Path | Contents |
|-|-|
| `core/engine` | Pure Kotlin engine: template geometry + hit-test, paint command log with undo, brushes, scene graph with capability-based attachments, waypoint mover, story runtime + free-path resolver, colour lab, character rules, prompt resolver, session budget |
| `core/storage` | Atomic document store (immutable revisions, crash recovery, trash) and `.colora.zip` backup export/import |
| `content-tools` | `validateContent` – validates the whole content pack |
| `tools/art`, `tools/content` | Source of every picture, prop, sticker, character part, world, story, prompt and label (Python + skia) |
| `tools/audio` | Synthesiser for all sound effects and music loops |
| `app/src/main/assets` | Generated content pack (168 templates, 7 worlds, 28 stories, 30 prompts, 44 character parts, 36 stickers, 21 props, Milo, palettes, EN/VI text, SHA-256 index) and audio |
| `app/src/main/res/font` | Nunito (OFL) |
| `docs/contact_sheets` | Review sheets for every asset group |

| `app` | Android app (Jetpack Compose + custom Canvas renderers): studio, toys, worlds, stories, ideas, collection, book, colour lab, character builder, parent area, export, backup; flavors `offlineFull` and `store` |
| `screenshots/` | Real renders of every screen (Robolectric, native graphics) and export samples |

## Build the Android app
Requirements: JDK 17+ (21 tested), Android SDK with platform 36 and build-tools 36.0.0. Put `sdk.dir=/path/to/sdk` in `local.properties` (or set `ANDROID_HOME`).
```
./gradlew assembleOfflineFullDebug        # app/build/outputs/apk/offlineFull/debug/app-offlineFull-debug.apk
./gradlew assembleStoreDebug              # Play Billing flavor (needs -Pcolora.playLicenseKey=... to grant purchases)
./gradlew :app:testOfflineFullDebugUnitTest   # Robolectric UI + export tests, writes screenshots/
adb install -r app/build/outputs/apk/offlineFull/debug/app-offlineFull-debug.apk
```

## Build and test (JVM parts)
Requirements: JDK 17+ (21 tested). The Gradle wrapper downloads Gradle 8.14.3.
```
./gradlew :core:engine:test :core:storage:test :content-tools:test
./gradlew :content-tools:validateContent
```
`:app` is included automatically when an Android SDK is found (`ANDROID_HOME`, `ANDROID_SDK_ROOT` or `sdk.dir` in `local.properties`).

## Regenerating content
See [`docs/CONTENT_PIPELINE.md`](docs/CONTENT_PIPELINE.md):
```
pip install skia-python numpy pillow fonttools
python3 tools/art/build_content.py     # art, catalog, extras, locales, index
python3 tools/audio/synth.py           # sound effects + music
```

## Flavors
- `offlineFull` – everything unlocked, no billing, no INTERNET permission (handover default).
- `store` – base content free, three expansion packs through Google Play Billing (needs the owner's Play Console setup).

## Documents
[DECISIONS](docs/DECISIONS.md) · [IMPLEMENTATION_STATUS](docs/IMPLEMENTATION_STATUS.md) · [ART_DIRECTION](docs/ART_DIRECTION.md) · [CONTENT_PIPELINE](docs/CONTENT_PIPELINE.md) · [ASSET_LICENSES](docs/ASSET_LICENSES.md) · [DATA_AND_PRIVACY](docs/DATA_AND_PRIVACY.md) · [QA_REPORT](docs/QA_REPORT.md) · [RELEASE_CHECKLIST](docs/RELEASE_CHECKLIST.md) · [KNOWN_ISSUES](docs/KNOWN_ISSUES.md) · [CHECKPOINT](docs/CHECKPOINT.md)
