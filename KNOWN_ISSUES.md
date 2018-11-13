# Known Issues

This is a list of major known issues. For the latest list of all issues see the
[Github Issues page](https://github.com/FolioReader/FolioReader-Android/issues)

## Media Overlay

From version 0.5.1, Media Overlay would not work as the streamer implementation is not done yet in r2-streamer-kotlin.

## TTS

From version 0.5.2, TTS is disabled.
In previous versions, sentences were formed by changing the DOM structure.
To implement TTS without changing DOM would require NLP to extract sentences.
As of now, no major reading systems other than Google Play Books implements TTS bug free.