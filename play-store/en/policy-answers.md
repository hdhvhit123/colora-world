# Play Console declarations: suggested answers (English)

These are drafts from what the code does today. The developer submits them and is responsible for them;
nothing here has been submitted or reviewed by Google. Check the current Play Console wording before answering.

## Data safety

- Does your app collect or share any of the required user data types? **No.**
  - No network code in the offline edition (no INTERNET permission). No analytics, ads, crash or tracking SDKs.
  - Files leave the device only when an adult exports or shares them through the system picker; per Google's
    definitions that is user-initiated transfer, not collection.
- Is all user data encrypted in transit? Not applicable (no data is transmitted).
- Do you provide a way for users to request deletion? In-app: parent area, "Delete all data"; or uninstall.
- Store edition: Google Play Billing handles purchases; the app itself does not collect payment information.

## Target audience and content

- Target age groups: **5 and under** and **6–8** (the app is designed for pre-readers with an adult nearby).
  Choosing these puts the app under the **Families policy**: no ads SDKs, no personal data collection,
  adult check before external links, purchases and settings. The app meets these by design; confirm against
  the current policy text before submitting.
- Appeals to children: Yes.
- Ads: **No ads.**

## Content rating (IARC questionnaire)

- Category: All other app types (or "Game" if the console steers you there).
- Violence, fear, sexuality, language, controlled substances, gambling: **None.**
- User interaction / user-generated content shared with others: **No** (no chat, no social features, nothing
  is posted online). Users can export their own drawings to a file.
- Shares location: No. Digital purchases: Store edition only (Yes); offline edition: No.
- Expected result: the lowest rating in every region (e.g. PEGI 3, ESRB Everyone).

## App access

- No login. All features are available without credentials. The adult check is a hold-and-type-the-number
  prompt (or a PIN the tester sets), so reviewers do not need any instructions beyond that.

## Permissions

- Offline edition: no dangerous permissions; AD_ID is explicitly removed from the merged manifest.
- Store edition: only `com.android.vending.BILLING` (from Play Billing).

## Still needed from the developer (blocked here)

- Play Console account, app creation, signing key / Play App Signing upload.
- Public URL for `privacy-policy.html` with the developer name, contact email and date filled in.
- Contact email for the listing.
- Billing products (if the store edition is published) created and activated in Play Console.
