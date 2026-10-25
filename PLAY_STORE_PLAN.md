# Play Store launch plan

Status: **on hold**. Assessed 2026-10-19; come back to this before publishing.

## 1. Technical (Claude can do this)
- [ ] Create an upload keystore and release signing config (keep the key and its passwords backed up somewhere safe)
- [ ] Enable R8 minify and resource shrinking for release, and add keep rules if needed
- [ ] Raise `targetSdk`/`compileSdk` to the level Play currently requires (probably 36), then re-test
- [ ] Bump version to `1.0.0` (versionCode 1)
- [ ] Build `./gradlew bundleRelease` and install/test the release build on a device

## 2. Play Console paperwork (owner submits; Claude can draft)
- [ ] Privacy policy URL (needed because of camera and microphone; all data stays on device)
- [ ] Data safety form: no data collected or shared
- [ ] Foreground service declaration for `specialUse` (floating teleprompter over the camera app), with a short demo video
- [ ] Mention the "Display over other apps" floating prompter in the listing
- [ ] Store listing: 512×512 icon, 1024×500 feature graphic, phone screenshots, short and full description
- [ ] Content rating questionnaire
- [ ] If the developer account is personal and created after Nov 2023: closed test with **12+ testers for 14 days** before production. This takes the longest, so start it early.

## 3. Testing still needed
- [ ] In-app recording (record a clip, check it saves to Movies/SuperPrompter)
- [ ] External mic (DJI Mic Mini via the USB-C receiver)
- [ ] Other devices: Samsung camera app, older Android versions (minSdk 26 = Android 8), tablets
- [ ] Replace the placeholder launcher icon with a designed one

## Ideas for later
- Bluetooth remote / volume-key control of scrolling
- Mic indicator badge (which mic will be recorded)
- Move the in-app prompter's reading line higher, like the floating one
