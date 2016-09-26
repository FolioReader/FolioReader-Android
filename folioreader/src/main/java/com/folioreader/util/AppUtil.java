package com.folioreader.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import com.folioreader.R;
import com.folioreader.activity.FolioActivity;
import com.folioreader.database.BookModelTable;
import com.folioreader.model.BookModel;
import com.folioreader.model.SmilElements;
import com.folioreader.smil.AudioElement;
import com.folioreader.smil.SmilFile;
import com.folioreader.smil.TextElement;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.type.TypeReference;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;


/**
 * Created by mahavir on 5/7/16.
 */
public class AppUtil {

    private static final ObjectMapper jsonMapper;
    private static String FILE_NAME ;
    private static final String SMIL_ELEMENTS = "smil_elements";
    public static  String mFolderName=null;

    static {
        jsonMapper = new ObjectMapper();
        jsonMapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copy", text);
        clipboard.setPrimaryClip(clip);
    }

    public static void share(Context context, String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.send_to)));
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static Map<String, String> stringToJsonMap(String string) {
        ArrayList<HashMap<String, String>> map = new ArrayList<HashMap<String, String>>();
        try {
            map = jsonMapper.readValue(string, new TypeReference<ArrayList<HashMap<String, String>>>() {
            });
        } catch (Exception e) {
            map = null;
        }
        return map.get(0);
    }

    public static String formatDate(Date hightlightDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy | HH:mm");
        String date = simpleDateFormat.format(hightlightDate);
        return date;
    }

    public static void setBackColorToTextView(UnderlinedTextView textView, String type) {
        Context context = textView.getContext();
        if (type.equals("highlight-yellow")) {
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.yellow));
            textView.setUnderlineWidth(0.0f);
        } else if (type.equals("highlight-green")) {
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
            textView.setUnderlineWidth(0.0f);
        } else if (type.equals("highlight-blue")) {
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
            textView.setUnderlineWidth(0.0f);
        } else if (type.equals("highlight-pink")) {
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.pink));
            textView.setUnderlineWidth(0.0f);
        } else if (type.equals("highlight-underline")) {
            textView.setUnderLineColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            textView.setUnderlineWidth(2.0f);
        }
    }

    public static int parseTimeToLong(String time) {
        double temp = 0.0;
        Log.d("time", "time to parse" + time);
        Map<String, String> timeFormats = new HashMap<>();
        timeFormats.put("HH:mm:ss.SSS", "^\\d{1,2}:\\d{2}:\\d{2}\\.\\d{1,3}$");
        timeFormats.put("HH:mm:ss", "^\\d{1,2}:\\d{2}:\\d{2}$");
        timeFormats.put("mm:ss.SSS", "^\\d{1,2}:\\d{2}\\.\\d{1,3}$");
        timeFormats.put("mm:ss", "^\\d{1,2}:\\d{2}$");
        timeFormats.put("ss.SSS", "^\\d{1,2}\\.\\d{1,3}$");

        Set keys = timeFormats.keySet();
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String value = (String) timeFormats.get(key);
            String p = value;
            Pattern pattern = Pattern.compile(p);
            Matcher matcher = pattern.matcher(time);
            if (matcher.matches()) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(key);
                try {
                    Date date = simpleDateFormat.parse(time);
                    simpleDateFormat = new SimpleDateFormat("ss.SSS");
                    double sec = Double.valueOf(simpleDateFormat.format(date));

                    simpleDateFormat = new SimpleDateFormat("mm");
                    double mm = Double.valueOf(simpleDateFormat.format(date));

                    simpleDateFormat = new SimpleDateFormat("HH");
                    double HH = Double.valueOf(simpleDateFormat.format(date));
                    temp = (sec * 1000) + ((mm * 60) * 1000) + ((HH * 60 * 60) * 1000);
                    return (int) temp;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return (int) temp;
    }


    public static Book saveEpubFile(Bundle bundleData, final Context context) {
        String assestPath;
        FolioActivity.Epub_Source_Type sourceType;
        int rawId;
        String fileName;
        InputStream epubInputStream = null;
        Book book = null;
        boolean isFileAvailable, isFolderAvalable = false;
        try {
            isFolderAvalable = isFolderAvailable(bundleData, context);

            if (!isFolderAvalable) {
                sourceType = (FolioActivity.Epub_Source_Type) bundleData.getSerializable(FolioActivity.INTENT_EPUB_SOURCE_TYPE);
                FILE_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/" + mFolderName + "/" + mFolderName + ".epub";
                if (sourceType.equals(FolioActivity.Epub_Source_Type.RAW)) {
                    rawId = bundleData.getInt(FolioActivity.INTENT_EPUB_SOURCE_PATH, 0);
                    Resources res = context.getResources();
                    epubInputStream = res.openRawResource(rawId);
//                  fileName=res.getResourceName(rawId);
                    fileName = res.getResourceEntryName(rawId);
                    isFileAvailable = saveTempEpubFile(epubInputStream);
                } else if (sourceType.equals(FolioActivity.Epub_Source_Type.ASSESTS)) {
                    assestPath = bundleData.getString(FolioActivity.INTENT_EPUB_SOURCE_PATH);
                    AssetManager assetManager = context.getAssets();
                    epubInputStream = assetManager.open(assestPath);
                    isFileAvailable = saveTempEpubFile(epubInputStream);
                } else {
                    FILE_NAME = bundleData.getString(FolioActivity.INTENT_EPUB_SOURCE_PATH);
                    isFileAvailable = saveTempEpubFile(null);
                }

                new EpubManipulator(FILE_NAME, mFolderName, context);
                book = saveBookToDb(FILE_NAME, context);

            } else {
                BookModel bookModel = BookModelTable.getAllRecords(context);
                if (bookModel != null) {
                    book = bookModel.getBook();
                } else {
                    book = saveBookToDb(FILE_NAME, context);
                }
            }
            return book;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return book;
    }

    private static boolean isFolderAvailable(Bundle bundleData,Context context) {
        FolioActivity.Epub_Source_Type sourceType = (FolioActivity.Epub_Source_Type) bundleData.getSerializable(FolioActivity.INTENT_EPUB_SOURCE_TYPE);
        String folderName;
        File file = null;
        boolean isFileExist = false;

    if (sourceType.equals(FolioActivity.Epub_Source_Type.RAW)) {
        int rawId = bundleData.getInt(FolioActivity.INTENT_EPUB_SOURCE_PATH, 0);
        Resources res = context.getResources();
        mFolderName = res.getResourceEntryName(rawId);
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/" + mFolderName);
        if (file.isFile()) isFileExist = true;
    } else if (sourceType.equals(FolioActivity.Epub_Source_Type.ASSESTS)) {
        String fileName = bundleData.getString(FolioActivity.INTENT_EPUB_SOURCE_PATH);
        int fileMaxIndex = fileName.length();
        mFolderName = fileName.substring(0, fileMaxIndex - 5);
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/" + mFolderName);
        if (file.isFile()) isFileExist = true;
    } else {
        String fileName = bundleData.getString(FolioActivity.INTENT_EPUB_SOURCE_PATH);
        String temp[] = fileName.split("/");
        fileName = temp[temp.length - 1];
        int fileMaxIndex = fileName.length();
        mFolderName = fileName.substring(0, fileMaxIndex - 5);
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/" + mFolderName);
        if (file.isFile()) {
            isFileExist = true;
        } else {
            FILE_NAME = bundleData.getString(FolioActivity.INTENT_EPUB_SOURCE_PATH);
        }
    }
    return isFileExist;
}


    public static boolean compareUrl(String ur1, String ur2) {
        String s[] = ur1.split("//");
        String s1[] = ur2.split("/");
        ur1 = s[s.length - 1];
        ur2 = s1[s1.length - 1];
        return ur1.equalsIgnoreCase(ur2);
    }

    public static Book saveBookToDb(String fileName, Context context) {
        FileInputStream fs = null;
        Book book = null;
        try {
            fs = new FileInputStream(fileName);
            book = (new EpubReader()).readEpub(fs);
            fs = null;

            BookModel bookModel = new BookModel();
            book.setCoverImage(null);
            book.setResources(null);
            bookModel.setBook(book);
            BookModelTable.createEntryInTableIfNotExist(context, bookModel);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return book;
    }

    public static SmilFile createSmilJson(Context context) {
        SmilFile smilFile = null;
        List<AudioElement> audioElementArrayList;
        List<TextElement> textElementList;

        try {
            File f = null;
            File[] paths;
            // create new file
            f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/temp/OEBPS/text");
            // create new filename filter
            FilenameFilter fileNameFilter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    if (name.lastIndexOf('.') > 0) {
                        // get last index for '.' char
                        int lastIndex = name.lastIndexOf('.');
                        // get extension
                        String str = name.substring(lastIndex);
                        // match path name extension
                        if (str.equals(".smil")) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            // returns pathnames for files and directory
            paths = f.listFiles(fileNameFilter);
            if (paths != null) {
                smilFile = new SmilFile();
                smilFile.load(paths[0].getPath());
                audioElementArrayList = smilFile.getAudioSegments();
                textElementList = smilFile.getTextSegments();
                SmilElements smilElement = new SmilElements(audioElementArrayList, textElementList);
                String smilElemets = jsonMapper.writeValueAsString(smilElement);
                SharedPreferenceUtil.putSharedPreferencesString(context, SMIL_ELEMENTS, smilElemets);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return smilFile;

    }

    public static SmilElements retrieveAndParseSmilJSON(Context context) {
        String smilElmentsJson = SharedPreferenceUtil.getSharedPreferencesString(context, SMIL_ELEMENTS, null);
        SmilElements smilElements = null;
        if (smilElmentsJson != null) {
            try {
                smilElements = jsonMapper.readValue(smilElmentsJson, SmilElements.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return smilElements;
    }


    public static Boolean saveTempEpubFile(InputStream inputStream) {
        OutputStream outputStream = null;
        File file = new File(FILE_NAME);
        try {
            if (!file.exists()) {
                File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/"+mFolderName);
                folder.mkdirs();

                outputStream = new FileOutputStream(new File(FILE_NAME));
                int read = 0;
                byte[] bytes = new byte[inputStream.available()];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } else {
                return true;
            }
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}







