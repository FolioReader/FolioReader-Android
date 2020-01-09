# Change Log

## 0.5.4

- Search API from codetoart/r2-streamer-kotlin now use window.find() instead of Rangy solution.

## 0.5.3

- FolioReader migrated to AndroidX. You will have to migrate your project to AndroidX. Read more about it here - [Migrating to AndroidX](https://developer.android.com/jetpack/androidx/migrate)
- Search API calls made asynchronous.

### Bugs Fixed

- FolioReader/FolioReader-Android#190
- FolioReader/FolioReader-Android#276
- FolioReader/FolioReader-Android#239
- FolioReader/FolioReader-Android#308
- FolioReader/FolioReader-Android#284
- FolioReader/FolioReader-Android#298

## 0.5.2 (Unreleased)

- Developer now don't need to add network_security_config.xml explicitly.
- Android Min SDK raised to API 21 as Chrome is stuck on version 30 on API 19.
- ReadPosition replaced with ReadLocator using CFI.
- TTS feature temporarily disabled, see KNOWN_ISSUES.md for more information.
- SearchQueryResults and SearchItem replaced with SearchLocator using CFI.

## 0.5.1

- r2-streamer-java replaced with r2-streamer-kotlin and r2-shared-kotlin
- WebViewMarker replaced with Native text selection popup.
- Media Overlay feature temporarily unavailable, see KNOWN_ISSUES.md for more information.

## 0.4.1

- Developer now don't need to specify permissions and FolioActivity tag in AndroidManifest.xml