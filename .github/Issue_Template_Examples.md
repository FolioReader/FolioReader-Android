### Example No 1 -

Issue / Feature - Issue <br/>
FolioReader version - 0.3.7 <br/>
FolioReader Stock / Modified - Stock <br/> 
Android SDK - 4.4.4<br/>
Mobile / Tablet / Emulator Info - Samsung Galaxy S3<br/> 
Crash / Error - Crash<br/>

Steps to reproduce / Describe in detail - <br/>

I'm using library version 0.3.7
When trying to open epub from my Activity, eg -

```
FolioReader folioReader = FolioReader.getInstance(getApplicationContext());
folioReader.openBook(R.raw.barrett); 
```

Getting this error -
(While i'm opening the file from my activity)

```
android.util.AndroidRuntimeException: Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
        at android.app.ContextImpl.startActivity(ContextImpl.java:672)
        at android.app.ContextImpl.startActivity(ContextImpl.java:659)
        at android.content.ContextWrapper.startActivity(ContextWrapper.java:331)
        at com.folioreader.FolioReader.openBook(FolioReader.java:97)
        at com.devs.ncert.activity.MainActivity$1.onClick(MainActivity.java:35)
```

### Example No 2 -

Issue / Feature - Feature <br/>
FolioReader version - 0.3.9 <br/>
FolioReader Stock / Modified - <br/> 
Android SDK - <br/>
Mobile / Tablet / Emulator Info - <br/> 
Crash / Error - <br/>

Steps to reproduce / Describe in detail - <br/>

Hey guys! I need Horizontal scrolling pagination reading mode. <br/> 
Is it possible to make this feature available?