# RankMate V5.4 — FULL Android Studio Source

This is a **complete native Android Studio project** (not merely a Capacitor starter).
The RankMate V5.4 HTML/CSS/JavaScript ranking application is embedded under
`app/src/main/assets/index.html`. The native Java host is in
`app/src/main/java/com/rankmate/dashboard/MainActivity.java`.

## What works
- Imports `.xlsx` from Android's file picker (single and multi-file selection).
- Persistent per-week localStorage over an app-internal HTTPS-like origin.
- Weekly ranking, overall ranking, AM/AC selection, AVG% tie-breaker, multi-sheet XLSX.
- PNG/CSV/JSON/XLSX exported to **Downloads/RankMate** using Android MediaStore.
- `Bagikan gambar` opens the Android share sheet with a saved PNG file.
- 3D Folder default theme, TaskMate themes and all embedded web UI logic.
- No Capacitor dependency, no npm install, no external Java Android library.

## Build APK using Android Studio
1. Unzip this package into any folder.
2. Open **Android Studio** > **Open** > select the unzipped project folder
   containing `settings.gradle` and `app/`.
3. Install the requested **Android SDK 35** from SDK Manager if prompted.
4. Choose **Gradle JDK 17** (or Android Studio's embedded JDK 17/21).
5. Allow Gradle sync/download (internet is required the first time).
6. Run **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
7. APK output: `app/build/outputs/apk/debug/app-debug.apk`.

For command-line users, run `./gradlew assembleDebug` on macOS/Linux or
`gradlew.bat assembleDebug` on Windows. The included bootstrap wrapper
locates or downloads Gradle 8.9. You can convert it to the official Gradle
wrapper once Gradle is available by running `./gradlew wrapper --gradle-version 8.9`.

## Environment and limitations
- Minimum Android version: **Android 10 (API 29)**.
- Android Studio + SDK 35 + internet for first Gradle sync are necessary.
- App data are stored **locally**, not automatically synced across phones.
- Updating an installed app with the same package ID and signature retains
  local data; uninstalling or installing an APK with a different signature can
  remove or isolate local data. Export a JSON backup first.
- The tool environment used to package this source does not have Android SDK
  or Gradle distribution. **APK compilation has not been verified here**.
- Debug APK is suitable for testing. To publish, configure a release signing key.

## Project outline
```
RankMate_V5_4_Android_Full_Source/
├─ settings.gradle
├─ build.gradle
├─ app/
│  ├─ build.gradle
│  └─ src/main/
│     ├─ AndroidManifest.xml
│     ├─ java/com/rankmate/dashboard/MainActivity.java
│     ├─ assets/index.html
│     ├─ assets/native-bridge.js
│     └─ res/... (app name, colors, icon)
├─ gradle/wrapper/gradle-wrapper.properties
├─ gradle/wrapper/gradle-wrapper.jar
├─ gradlew / gradlew.bat
└─ README.md
```

## Reliability details
- JS generated Blob files (including JSZip `.xlsx`) are intercepted and streamed
  to native Android in fixed-size base64 chunks to avoid large single bridge calls.
- PNG share path uses native Android bridge rather than relying on unsupported
  browser Web Share APIs.
- HTML is loaded via `https://appassets.androidplatform.net/index.html`,
  intercepted to bundled assets, so browser localStorage has a stable origin.
- No storage permission is needed on Android 10+ for saving own Downloads files.
