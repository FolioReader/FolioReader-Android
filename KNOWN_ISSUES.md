# Known Issues

This is a list of major known issues. For the latest list of all issues see the
[Github Issues page](https://github.com/FolioReader/FolioReader-Android/issues)

## Errors on API 21 and 22 with Webview lower than version 44

FolioReader might not work properly on API 21 and 22 Emulator images.
As these images have Android System Webview lower than version 44.
Devices or Emulator of API 21 and 22 with Google Play Store will be
able to run FolioReader without any error after updating Android System
Webview to version 44+

## Media Overlay

From version 0.5.1, Media Overlay would not work as the streamer implementation is not done yet in r2-streamer-kotlin.

## TTS

From version 0.5.2, TTS is disabled.
In previous versions, sentences were formed by changing the DOM structure.
To implement TTS without changing DOM would require NLP to extract sentences.
As of now, no major reading systems other than Google Play Books implements TTS bug free.