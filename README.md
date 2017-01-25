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
- [x] TTS - Text to Speech Support
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
Add following dependency to your app build.gradle
``` java
compile 'com.folioreader:folioreader:0.2.5'
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

### Author
[**Heberti Almeida**](https://github.com/hebertialmeida)

- Follow me on **Twitter**: [**@hebertialmeida**](https://twitter.com/hebertialmeida)
- Contact me on **LinkedIn**: [**hebertialmeida**](http://linkedin.com/in/hebertialmeida)

[**CodeToArt Technology**](https://github.com/codetoart)

- Follow us on **Twitter**: [**@codetoart**](https://twitter.com/codetoart)
- Contact us on **Website**: [**codetoart**](http://www.codetoart.com)

## Donations

**This project needs you!** If you would like to support this project's further development, the creator of this project or the continuous maintenance of this project, **feel free to donate**. Your donation is highly appreciated. Thank you!

**PayPal**

 - [**Donate 5 $**] (https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=hebertialmeida%40gmail%2ecom&lc=US&item_name=FolioReader%20Libraries&amount=5%2e00&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted): Thank's for creating this project, here's a tea (or some juice) for you!
 - [**Donate 10 $**] (https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=hebertialmeida%40gmail%2ecom&lc=US&item_name=FolioReader%20Libraries&amount=10%2e00&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted): Wow, I am stunned. Let me take you to the movies!
 - [**Donate 15 $**] (https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=hebertialmeida%40gmail%2ecom&lc=US&item_name=FolioReader%20Libraries&amount=15%2e00&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted): I really appreciate your work, let's grab some lunch! 
 - [**Donate 25 $**] (https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=hebertialmeida%40gmail%2ecom&lc=US&item_name=FolioReader%20Libraries&amount=25%2e00&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted): That's some awesome stuff you did right there, dinner is on me!
 - [**Donate 50 $**] (https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=hebertialmeida%40gmail%2ecom&lc=US&item_name=FolioReader%20Libraries&amount=50%2e00&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted): I really really want to support this project, great job!
 - [**Donate 100 $**] (https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=hebertialmeida%40gmail%2ecom&lc=US&item_name=FolioReader%20Libraries&amount=100%2e00&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted): You are the man! This project saved me hours (if not days) of struggle and hard work, simply awesome!
 - Of course, you can also [**choose what you want to donate**](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=hebertialmeida%40gmail%2ecom&lc=US&item_name=FolioReader%20Libraries&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted), all donations are awesome!

## License
FolioReaderKit is available under the BSD license. See the [LICENSE](https://github.com/FolioReader/FolioReader-Android/blob/master/License.md) file.

