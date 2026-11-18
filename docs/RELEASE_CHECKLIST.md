# Release checklist (owner actions marked ★)

Nothing has been published. Do not publish without the owner's explicit request.

## Build and signing
- [ ] `:app` builds from README with pinned, verified versions (blocked until an SDK session, K-01).
- [ ] ★ Create an upload key and keep it outside git (Play App Signing recommended). No keystore or password in this repo (`.gitignore` blocks `*.jks`, `*.keystore`).
- [ ] Release build minified/shrunk; verify no debug/fake-billing code in `offlineFull`/`store` release.

## Play Console (★ owner account required)
- [ ] App created; Families policy / Designed for Families questionnaire; target age groups.
- [ ] Data safety form (matches docs/DATA_AND_PRIVACY.md: no data collected/shared).
- [ ] Content rating questionnaire.
- [ ] Store listing EN/VI, screenshots from real devices, privacy policy URL.
- [ ] In-app products for `pack_nature_plus`, `pack_everyday_plus`, `pack_imagination_plus` (store flavor) and license testers.
- [ ] Internal testing track → closed testing → production.

## Billing (store flavor)
- [ ] Play Billing adapter shows purchase UI only after product details load; pending purchases never grant; acknowledgement; restore on start; refunds/revocations handled; offline uses cached verified grant.
- [ ] Tested with license testers on a real device.

## Policy and quality
- [ ] No ads/analytics/tracking SDKs; permissions review (offlineFull has no INTERNET).
- [ ] Parent gate on every external/purchase/export/settings exit.
- [ ] EN/VI complete, Vietnamese diacritics not clipped at 200% font scale.
- [ ] TalkBack pass, reduced motion, accessibility labels.
- [ ] QA_REPORT updated with device results and performance numbers.
