package com.folioreader.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.folioreader.R;
import com.folioreader.view.StyleableTextView;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.io.Reader;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            map = jsonMapper.readValue(string, new TypeReference<ArrayList<HashMap<String, String>>>() {});
        } catch (Exception e) {
            map = null;
        }
        return map.get(0);
    }

    public static String formatDate(Date hightlightDate){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MMM dd, yyyy | HH:mm");
        String date=simpleDateFormat.format(hightlightDate);
        return date;
    }

    public static void setBackColorToTextView(UnderlinedTextView textView,String type){
        Context context=textView.getContext();
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
            textView.setUnderLineColor(ContextCompat.getColor(context,android.R.color.holo_red_dark));
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

    public static int parseTimeToLong(String time){

        double temp=0.0;

        Map<String,String> timeFormats=new HashMap<>();
        timeFormats.put("HH:mm:ss.SSS","^\\d{1,2}:\\d{2}:\\d{2}\\.\\d{1,3}$");
        timeFormats.put("HH:mm:ss","^\\d{1,2}:\\d{2}:\\d{2}$");
        timeFormats.put("mm:ss.SSS","^\\d{1,2}:\\d{2}\\.\\d{1,3}$");
        timeFormats.put("mm:ss","^\\d{1,2}:\\d{2}$");
        timeFormats.put("ss.SSS","^\\d{1,2}\\.\\d{1,3}$");

        Set keys = timeFormats.keySet();
        /*Collection c = timeFormats.values();
        Iterator<Map.Entry<String,String>> itr = c.iterator();*/
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String value = (String) timeFormats.get(key);
            String p=value;
            Pattern pattern = Pattern.compile(p);
            Matcher matcher=pattern.matcher(time);
            if(matcher.matches()){
               SimpleDateFormat simpleDateFormat=new SimpleDateFormat(key);
                try {
                    Date date=simpleDateFormat.parse(time);
                    simpleDateFormat =new SimpleDateFormat("ss.SSS");
                    double sec=Double.valueOf(simpleDateFormat.format(date));

                    simpleDateFormat =new SimpleDateFormat("mm");
                    double mm=Double.valueOf(simpleDateFormat.format(date));

                    simpleDateFormat =new SimpleDateFormat("HH");
                    double HH=Double.valueOf(simpleDateFormat.format(date));
                    temp=sec +(mm*60)+ (HH*60*60);
                    return (int) temp;


                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }


        return (int) temp;
    }


    }

