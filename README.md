![FolioReader logo](https://raw.githubusercontent.com/FolioReader/FolioReaderKit/assets/folioreader.png)

[![Build Status](https://api.travis-ci.org/FolioReader/FolioReader-Android.svg?branch=master)](https://travis-ci.org/FolioReader/FolioReader-Android)

FolioReader-Android is an ePub reader and parser framework written in Java.

### Features

- [x] Custom Fonts
- [x] Custom Text Size
- [x] Themes / Day mode / Night mode
- [x] Text Highlighting
- [x] List / Edit / Delete Highlights
- [x] Handle Internal and External Links
- [x] Portrait / Landscape
- [x] Reading Time Left / Pages left
- [ ] In-App Dictionary
- [x] Media Overlays (Sync text rendering with audio playback)
- [ ] TTS - Text to Speech Support
- [ ] Parse epub cover image
- [ ] PDF support
- [ ] Book Search
- [x] Add Notes to a Highlight
- [ ] Better Documentation

## Demo
##### Custom Fonts
![Custom fonts](https://cloud.githubusercontent.com/assets/1277242/19012915/0661c7b2-87e0-11e6-81d6-8c71051e1074.gif)
##### Day and Night Mode
![Day night mode](https://cloud.githubusercontent.com/assets/1277242/19012914/f42059c4-87df-11e6-97f8-29e61a79e8aa.gif)
##### Text Highlighting 
![Highlight](https://cloud.githubusercontent.com/assets/1277242/19012904/c2700c3a-87df-11e6-97ed-507765b3ddf0.gif)
##### Media Overlays 
![Media Overlay](https://cloud.githubusercontent.com/assets/1277242/19012908/d61f3ce2-87df-11e6-8652-d72b6a1ad9a3.gif)

### Gradle
Add following to Project's build.gradle
``` java
maven {
     url  "http://dl.bintray.com/mobisystech/maven"
}
```
Add following dependency to your app build.gradle
``` java
compile 'com.folioreader:folioreader:0.1'
```

### Usage

To use FolioReader, you need to call FolioReaderActivity with following parameters:
1. INTENT_EPUB_SOURCE_TYPE - your epub can come from raw or assets folder or from SD card. Use enum FolioActivity.EpubSourceType.
2. INTENT_EPUB_SOURCE_PATH - assets/SD card path of the epub file or raw ID of epub file if epub file is in raw folder

Reading from assets folder
```java
Intent intent = new Intent(HomeActivity.this, FolioActivity.class);
intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_TYPE, FolioActivity.EpubSourceType.ASSESTS);
intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, "epub/The Silver Chair.epub");
startActivity(intent);
```

Reading from raw folder of resources
```java
Intent intent = new Intent(HomeActivity.this, FolioActivity.class);
intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_TYPE, FolioActivity.EpubSourceType.RAW);
intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, R.raw.adventures);
startActivity(intent);
```

For reading from SD card, just retrieve absolute path of epub file and pass that in INTENT_EPUB_SOURCE_PATH.

### Credits
1. <a href="https://github.com/daimajia/AndroidSwipeLayout">SwipeLayout</a>
2. <a href="http://ormlite.com/">ORMLite</a>
3. <a href="https://github.com/julianharty/new-android-daisy-reader">SMIL parsing</a>
