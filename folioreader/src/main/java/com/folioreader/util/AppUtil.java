package com.folioreader.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import com.folioreader.R;
import com.folioreader.activity.FolioActivity;
import com.folioreader.database.BookModelTable;
import com.folioreader.model.BookModel;


import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;


/**
 * Created by mahavir on 5/7/16.
 */
public class AppUtil {

    private static final ObjectMapper jsonMapper;

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


    public static String readerSmil(Reader reader) {
       /* if (mSpineReferenceHtmls.get(position) != null) {
            return mSpineReferenceHtmls.get(position);
        } else {*/
        try {
               /* Reader reader = mSpineReferences.get(position).getResource().getReader();*/

            StringBuilder builder = new StringBuilder();
            int numChars;
            char[] cbuf = new char[2048];
            while ((numChars = reader.read(cbuf)) >= 0) {
                builder.append(cbuf, 0, numChars);
            }
            String content = builder.toString();
              /*  mSpineReferenceHtmls.set(position, content);*/
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
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
        /*Collection c = timeFormats.values();
        Iterator<Map.Entry<String,String>> itr = c.iterator();*/
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
                    Log.d("time as outpu", "time as output" + temp);
                    return (int) temp;


                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }


        return (int) temp;
    }


    public static void saveFile(InputStream inputStream) {
        {

            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/audio" + ".mp3");
            Log.d("file exixts", file.exists() + "");
            Log.d("file isfile", file.isFile() + "");


            if (!file.canRead()) {
                File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader");
                folder.mkdirs();
                OutputStream outputStream = null;
                Log.d("not exixts", "mp3 file not avalable");

                try {
                    // read this file into InputStream
                    //inputStream = new FileInputStream("/Users/mkyong/Downloads/holder.js");

                    // write the inputStream to a FileOutputStream
                    outputStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/audio" + ".mp3"));

                    int read = 0;
                    byte[] bytes = new byte[inputStream.available()];

                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }

                    System.out.println("Done!");

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                            // outputStream.flush();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
    }


    public static void saveEpubFile(InputStream inputStream, final Context context) {
        {
            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/epubfile" + ".epub";
            File file = new File(fileName);
            Log.d("file exixts", file.exists() + "");
            Log.d("file isfile", file.isFile() + "");


            if (!file.exists()) {
                file=null;
                File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader");
                folder.mkdirs();
                folder=null;
                OutputStream outputStream = null;
                Log.d("not exixts", "mp3 file not avalable");

                try {
                    // read this file into InputStream
                    //inputStream = new FileInputStream("/Users/mkyong/Downloads/holder.js");

                    // write the inputStream to a FileOutputStream
                    outputStream = new FileOutputStream(new File(fileName));

                    int read = 0;
                    byte[] bytes = new byte[inputStream.available()];

                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }

                    System.out.println("Done!");
                    new EpubManipulator(fileName, "temp", context);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                            // outputStream.flush();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    FileInputStream fs = null;
                    Book book = null;
                      try {
                        fs = new FileInputStream(fileName);
                        book = (new EpubReader()).readEpub(fs);
                          fs=null;

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
                    final Book book1 = book;
                    ((FolioActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (book1 != null) {
                                ArrayList<TOCReference> tocReferenceArrayList = (ArrayList<TOCReference>) book1.getTableOfContents().getTocReferences();
                                ((FolioActivity) context).configRecyclerViews(tocReferenceArrayList, book1, (ArrayList<SpineReference>) book1.getSpine().getSpineReferences());
                            }
                        }
                    });

                }
            } else {
                //FileInputStream fs = null;
                //try {
                    /*fs = new FileInputStream(fileName);
                    final Book book = (new EpubReader()).readEpub(fs);*/
                    ((FolioActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BookModel bookModel=BookModelTable.getAllRecords(context);
                        Book book = BookModelTable.getAllRecords(context).getBook();
                        if (book != null) {
                            ArrayList<TOCReference> tocReferenceArrayList = (ArrayList<TOCReference>) book.getTableOfContents().getTocReferences();
                            ((FolioActivity) context).configRecyclerViews(tocReferenceArrayList, book, (ArrayList<SpineReference>) book.getSpine().getSpineReferences());
                        }
                    }
                });

             /*   } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

            }
        }
    }

    public static void copyAssets(Context context) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        String[] fontFile = null;
        try {
            files = assetManager.list("");
            fontFile = assetManager.list(files[6]);
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            File folder;
            try {
                in = assetManager.open(filename);
                //File outFile = new File(context.getExternalFilesDir(null), filename);
                File outFile = null;
                if (filename.contains(".js")) {
                    folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/temp/OEBPS/javascript");
                    folder.mkdir();
                    outFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/temp/OEBPS/javascript", filename);
                    // outFile.mkdir();
                } else if (filename.contains(".css")) {
                    folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/temp/OEBPS/CSS");
                    folder.mkdir();
                    outFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/temp/OEBPS/CSS", filename);
                } else if (filename.contains(".ttf") || filename.contains(".otf")) {
                    folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/temp/OEBPS/FONTS");
                    folder.mkdir();
                    outFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/temp/OEBPS/FONTS", filename);
                }
                if (outFile != null) {
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                }
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static  boolean compareUrl(String ur1,String ur2){
        /*String urlS="file:///storage/emulated/0/folioreader/temp/OEBPS//colophon.xhtml";
        String url="Text/colophon.xhtml";*/
        String s[]=ur1.split("//");
        String s1[]=ur2.split("/");
        ur1=s[s.length-1];
        ur2=s1[s1.length-1];
        return ur1.equalsIgnoreCase(ur2);
    }
}







