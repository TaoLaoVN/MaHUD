# MaHUD

Android CPU monitor app (Kotlin, Jetpack Compose, Clean Architecture).

## GitHub release updates

The app checks for updates from [GitHub Releases](https://github.com/TaoLaoVN/MaHUD/releases) in **Settings → App update**.

### Publishing a new release

1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Build a release APK:
   ```bash
   ./gradlew :app:assembleRelease
   ```
3. Create a GitHub release with tag `v{versionName}` (example: `v0.2.0`).
4. Attach the APK file (name must end with `.apk`).
5. Optional: include `versionCode: 2` in the release notes for explicit version-code comparison.

Users can check, download, and install updates directly from the app settings screen.
