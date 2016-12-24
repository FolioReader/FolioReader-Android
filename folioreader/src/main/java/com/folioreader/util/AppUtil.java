package com.folioreader.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import com.folioreader.R;
import com.folioreader.activity.FolioActivity;
import com.folioreader.model.BookModel;
import com.folioreader.model.SmilElements;
import com.folioreader.smil.AudioElement;
import com.folioreader.smil.SmilFile;
import com.folioreader.smil.TextElement;

/*import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;*/
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import static com.folioreader.Constants.BOOK_STATE;
import static com.folioreader.Constants.BOOK_TITLE;
import static com.folioreader.Constants.VIEWPAGER_POSITION;
import static com.folioreader.Constants.WEBVIEW_SCROLL_POSITION;
import static com.folioreader.util.FileUtil.saveTempEpubFile;
import static com.folioreader.util.SharedPreferenceUtil.getSharedPreferencesString;


/**
 * Created by mahavir on 5/7/16.
 */
public class AppUtil {

   /*private static final ObjectMapper jsonMapper;*/
    private static String mFileName;
    private static final String SMIL_ELEMENTS = "smil_elements";
    //private static String mFolderName = null;
    private static final String TAG = AppUtil.class.getSimpleName();
    private static String FOLIO_READER_ROOT="folioreader";

    private static enum FileType {
        OPS,
        OEBPS
    }

     /*static {
        jsonMapper = new ObjectMapper();
        try {
            jsonMapper
                    .configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }*/

    public static Map<String, String> stringToJsonMap(String string) {
       /* ArrayList<HashMap<String, String>> map = new ArrayList<HashMap<String, String>>();
        try {
            map = jsonMapper.readValue(string,
                    new TypeReference<ArrayList<HashMap<String, String>>>() {
                    });
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            return null;
        }
        return map.get(0);*/
        HashMap<String, String> map=new HashMap<>();
        try {
            JSONArray jsonArray=new JSONArray(string);
            JSONObject jObject = jsonArray.getJSONObject(0);
            Iterator<?> keys = jObject.keys();

                keys.hasNext();
                String key = (String)keys.next();
                String value = jObject.getString(key);
                map.put(key, value);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return map;

    }

    public static String formatDate(Date hightlightDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy | HH:mm");
        String date = simpleDateFormat.format(hightlightDate);
        return date;
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
                    double hh = Double.valueOf(simpleDateFormat.format(date));
                    temp = (sec * 1000) + ((mm * 60) * 1000) + ((hh * 60 * 60) * 1000);
                    return (int) temp;
                } catch (ParseException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }
        return (int) temp;
    }



    public static Book saveEpubFile(final Context context, FolioActivity.EpubSourceType epubSourceType, String epubFilePath, int epubRawId, String epubFileName) {
        String filePath;
        InputStream epubInputStream;
        Book book = null;
        boolean isFolderAvalable;
        try {
            isFolderAvalable = isFolderAvailable(epubFileName);
            filePath = getFolioEpubFilePath(epubSourceType, epubFilePath, epubFileName);

            if (!isFolderAvalable) {
                if (epubSourceType.equals(FolioActivity.EpubSourceType.RAW)) {
                    epubInputStream = context.getResources().openRawResource(epubRawId);
                    saveTempEpubFile(filePath, epubFileName, epubInputStream);
                } else if (epubSourceType.equals(FolioActivity.EpubSourceType.ASSESTS)) {
                    AssetManager assetManager = context.getAssets();
                    epubInputStream = assetManager.open(epubFilePath);
                    saveTempEpubFile(filePath, epubFileName, epubInputStream);
                } else {
                    filePath = epubFilePath;
                }

                EpubManipulator epubManipulator= new EpubManipulator(filePath, epubFileName, context);
                //book = saveBookToDb(filePath, epubFileName, context);
                book = epubManipulator.getEpubBook();

            } else {

                FileInputStream fileInputStream = new FileInputStream(filePath);
                book = (new EpubReader()).readEpub(fileInputStream);
               /* BookModel bookModel = BookModelTable.getBookFromName(context, epubFileName);
                if (bookModel != null) {
                    book = bookModel.getBook();
                } else {
                    book = saveBookToDb(filePath, epubFileName, context);
                }*/
            }
            return book;
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return book;
    }

    public static String getFolioEpubFolderPath(String epubFileName) {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + FOLIO_READER_ROOT + "/" + epubFileName;
    }

    public static String getFolioEpubFilePath(FolioActivity.EpubSourceType sourceType, String epubFilePath, String epubFileName) {
        if (FolioActivity.EpubSourceType.SD_CARD.equals(sourceType)) {
            return epubFilePath;
        } else {
            return getFolioEpubFolderPath(epubFileName) + "/" + epubFileName + ".epub";
        }
    }

    private static boolean isFolderAvailable(String epubFileName) {
        File file = new File(getFolioEpubFolderPath(epubFileName));
        return file.isDirectory();
    }

    public static String getEpubFilename(Context context, FolioActivity.EpubSourceType epubSourceType,
                                         String epubFilePath, int epubRawId) {
        String epubFileName;
        if (epubSourceType.equals(FolioActivity.EpubSourceType.RAW)) {
            Resources res = context.getResources();
            epubFileName = res.getResourceEntryName(epubRawId);
        } else {
            String[] temp = epubFilePath.split("/");
            epubFileName = temp[temp.length - 1];
            int fileMaxIndex = epubFileName.length();
            epubFileName = epubFileName.substring(0, fileMaxIndex - 5);
        }

        return epubFileName;
    }


    public static boolean compareUrl(String ur1, String ur2) {
        String[] s = ur1.split("//");
        String[] s1 = ur2.split("/");
        ur1 = s[s.length - 1];
        ur2 = s1[s1.length - 1];
        return ur1.equalsIgnoreCase(ur2);
    }

    public static Book saveBookToDb(String epubFilePath, String epubFileName, Context context) {
        FileInputStream fs = null;
        Book book = null;
        try {
            fs = new FileInputStream(epubFilePath);
            book = (new EpubReader()).readEpub(fs);
            fs = null;

            BookModel bookModel = new BookModel();
           /* book.setCoverImage(null);
            book.setResources(null);*/
           /* bookModel.setBook(book);
            bookModel.setBookName(epubFileName);*/
            //BookModelTable.createEntryInTableIfNotExist(context, bookModel);

        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
        return book;
    }

    public static SmilFile createSmilJson(Context context, String epubFileName) {
        SmilFile smilFile = null;
        List<AudioElement> audioElementArrayList;
        List<TextElement> textElementList;
        String epubFolderPath = FileUtil.getFolioEpubFolderPath(epubFileName);

        try {
            File f = null;
            File[] paths;
            // create new file
            f = new File(epubFolderPath + "/OEBPS/text");
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
            if (paths != null && paths.length > 0) {
                smilFile = new SmilFile();
                smilFile.load(paths[0].getPath());
                audioElementArrayList = smilFile.getAudioSegments();
                textElementList = smilFile.getTextSegments();
                /*SmilElements smilElement = new SmilElements(audioElementArrayList, textElementList);
                String smilElemets = jsonMapper.writeValueAsString(smilElement);
                SharedPreferenceUtil.putSharedPreferencesString(context, epubFileName, smilElemets);*/
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            Log.d(TAG, e.getMessage());
        }
        return smilFile;

    }

    /*public static SmilElements retrieveAndParseSmilJSON(Context context, String epubFileName) {
        String smilElmentsJson =
                getSharedPreferencesString(context, epubFileName, null);
        SmilElements smilElements = null;
        if (smilElmentsJson != null) {
            try {
                smilElements = jsonMapper.readValue(smilElmentsJson, SmilElements.class);
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }

        return smilElements;
    }*/

    public static String getPathOPF(String unzipDir, Context context) {
        String mPathOPF = "";
        try {
            // get the OPF path, directly from container.xml

            BufferedReader br
                    = new BufferedReader(new InputStreamReader(new FileInputStream(unzipDir
                    + "/META-INF/container.xml"), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                //if (line.indexOf(getS(R.string.full_path)) > -1)
                if (line.contains(context.getString(R.string.full_path))) {
                    int start = line.indexOf(context.getString((R.string.full_path)));
                    //int start2 = line.indexOf("\"", start);
                    int start2 = line.indexOf('\"', start);
                    int stop2 = line.indexOf('\"', start2 + 1);
                    if (start2 > -1 && stop2 > start2) {
                        mPathOPF = line.substring(start2 + 1, stop2).trim();
                        break;
                    }
                }
            }
            br.close();

            // in case the OPF file is in the root directory
            if (!mPathOPF.contains("/")) {
                mPathOPF = AppUtil.getTypeOfOPF(unzipDir);
            }

            // remove the OPF file name and the preceding '/'
            int last = mPathOPF.lastIndexOf('/');
            if (last > -1) {
                mPathOPF = mPathOPF.substring(0, last);
            }

            return mPathOPF;
        } catch (NullPointerException | IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return mPathOPF;
    }

    public static boolean checkOPFInRootDirectory(String unzipDir, Context context) {
        String mPathOPF = "";
        boolean status = false;
        try {
            // get the OPF path, directly from container.xml
           /* BufferedReader br = new BufferedReader(new FileReader(unzipDir
                    + "/META-INF/container.xml"));*/
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(new FileInputStream(unzipDir
                    + "/META-INF/container.xml"), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                //if (line.indexOf(getS(R.string.full_path)) > -1)
                if (line.contains(context.getString(R.string.full_path))) {
                    int start = line.indexOf(context.getString((R.string.full_path)));
                    //int start2 = line.indexOf("\"", start);
                    int start2 = line.indexOf('\"', start);
                    int stop2 = line.indexOf('\"', start2 + 1);
                    if (start2 > -1 && stop2 > start2) {
                        mPathOPF = line.substring(start2 + 1, stop2).trim();
                        break;
                    }
                }
            }
            br.close();

            // check the OPF file is in the root directory
            if (!mPathOPF.contains("/")) {
                status = true;
            } else {
                status = false;
            }


        } catch (NullPointerException | IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return status;
    }

    private static String getTypeOfOPF(String unzipDir) {
        File folder = new File(unzipDir);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isDirectory()) {
                FileType type = FileType.valueOf(listOfFiles[i].getName());
                if (type.equals(FileType.OPS))
                    return type.name();
                if (type.equals(FileType.OEBPS))
                    return type.name();
            }
        }
        return "";
    }

    public static void saveBookState(Context context, Book book, int folioPageViewPagerPosition, int webViewScrollPosition) {
        SharedPreferenceUtil.removeSharedPreferencesKey(context, book.getTitle() + BOOK_STATE);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BOOK_TITLE, book.getTitle());
            obj.put(WEBVIEW_SCROLL_POSITION, webViewScrollPosition);
            obj.put(VIEWPAGER_POSITION, folioPageViewPagerPosition);
            SharedPreferenceUtil.
                    putSharedPreferencesString(
                    context, book.getTitle() + BOOK_STATE, obj.toString());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static boolean checkPreviousBookStateExist(Context context, Book book) {
        String json
                = getSharedPreferencesString(
                context, book.getTitle() + BOOK_STATE,
                null);
        if (json != null) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                String bookTitle = jsonObject.getString(BOOK_TITLE);
                if (bookTitle.equals(book.getTitle()))
                    return true;
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
        }
        return false;
    }

    public static int getPreviousBookStatePosition(Context context, Book book) {
        String json
                = getSharedPreferencesString(context,
                book.getTitle() + BOOK_STATE,
                null);
        if (json != null) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                return jsonObject.getInt(VIEWPAGER_POSITION);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                return 0;
            }
        }
        return 0;
    }

    public static int getPreviousBookStateWebViewPosition(Context context, Book book) {
        String json = getSharedPreferencesString(context, book.getTitle() + BOOK_STATE, null);
        if (json != null) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                return jsonObject.getInt(WEBVIEW_SCROLL_POSITION);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                return 0;
            }
        }
        return 0;
    }

}







