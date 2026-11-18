# Data and privacy

Status: design + implemented storage layer. App-level items marked *(app)* are to be verified when `:app` exists.

## What is stored, where
| Data | Where | Notes |
|-|-|-|
| Child profiles (local UUID, avatar, optional local alias set by a parent, skill mode, locale) | `files/library/profiles/<id>/profile.json` | No real names required; no photos, no audio. |
| Artwork, scenes, characters, books, story progress, colour-lab swatches | `files/library/profiles/<id>/docs/<doc>/rev/*` | Immutable revisions with SHA-256; pointer `current.json`. |
| Deleted items | `files/library/trash/` | Kept 7 days; parent can restore or delete forever. |
| Parent settings, gate state, timer, entitlements cache | separate DataStore *(app)* | Never part of the document store, therefore never in backups. |
| Thumbnails / toy textures / export staging | cache dir *(app)* | Derived; safe to delete. |

## Network, SDKs, permissions
- No analytics, ads, crash-reporting or tracking SDKs. No network code in any module.
- `offlineFull` declares **no INTERNET permission** *(app)*. `store` adds only what Play Billing needs.
- No camera, microphone, location or contacts permissions (candles are blown by tapping).

## Backup
- In-app backup creates a `.colora.zip` the parent saves through the system file picker (SAF) behind the parent gate.
- Contents: manifest + profiles + documents only. PIN, gate tokens, entitlements, purchase tokens, logs are excluded by construction (they are not in the library).
- Import validates everything in staging and adds data as new profiles by default; it never grants purchases.
- Android Auto Backup / device-transfer rules *(app)*: library and settings excluded from cloud backup so nothing is uploaded silently; device-to-device transfer decision to be documented with the rules XML.

## Deletion
- Delete profile (parent gate, explicit scope, backup offered first) removes its folder and trash.
- "Delete all data" in the parent area *(app)* removes library, settings and caches.
