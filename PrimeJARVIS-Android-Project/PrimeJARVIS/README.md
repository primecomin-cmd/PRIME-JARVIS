# Prime JARVIS — Android voice assistant starter

Starter Android app for explicit tap-to-speak commands in Hindi/English. Includes launching YouTube/WhatsApp/Chrome, opening Settings/Wi-Fi settings, flashlight toggle, and spoken responses.

## Build APK using a phone (GitHub Actions)
1. Sign in to GitHub in your mobile browser and create a new repository named `PrimeJARVIS`.
2. Upload the contents of this folder to the repository, preserving folder structure (including `.github/workflows/build-apk.yml`).
3. Open the repository's **Actions** tab. If prompted, enable workflows.
4. Select **Build Prime JARVIS APK** → **Run workflow**. Wait for the run to finish successfully.
5. Open that completed run and download the artifact named `Prime-JARVIS-debug-APK`. Extract the ZIP to get `app-debug.apk`.
6. Open the APK on your phone and allow installation from that source if Android asks. Only install APKs you built or trust.

## Build with Android Studio
Open this folder as a project and run the `assembleDebug` Gradle task. The APK output is `app/build/outputs/apk/debug/app-debug.apk`.

## Current limits
- Speech recognition is started when you tap the microphone; it is not an always-on hotword service.
- Android's speech recognition service may need internet and may send audio to the selected recognition provider.
- Commands are intentionally limited to listed actions. It does not bypass Android security, silently send messages, or control every third-party app.
- This is a debug starter build, not a Play Store release. Review permissions and test before relying on it.
