package com.folioreader.util;

import com.folioreader.model.Highlight;
import com.folioreader.model.sqlite.HighLightRangy;
import com.folioreader.model.sqlite.HighLightRangyTable;
import com.folioreader.model.sqlite.HighLightTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by priyank on 5/12/16.
 */
public class HighlightUtil {
    public static final int mHighlightRange = 30;
    private static final String TAG = HighlightUtil.class.getSimpleName();

    public static void createHighlightRangy(String content, String bookTitle, String pageId, int pageNo,int scrollPosition) {
        try {
            JSONObject jObject = new JSONObject(content);

            String rangy = jObject.getString("rangy");
            String textContent = jObject.getString("content");
            String color = jObject.getString("color");

            Highlight highlight = new Highlight();
            highlight.setContent(textContent);
            highlight.setType(color);
            highlight.setPageNumber(pageNo);
            highlight.setScrollPosition(scrollPosition);
            highlight.setBookId(bookTitle);
            highlight.setDate(Calendar.getInstance().getTime());
            HighLightTable.insertHighlight(highlight);

            HighLightRangy highLightRangy = new HighLightRangy();
            highLightRangy.setRangy(rangy);
            highLightRangy.setBookId(bookTitle);
            highLightRangy.setPageId(pageId);
            HighLightRangyTable.saveHighLight(highLightRangy);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String removeSentenceSpam(String html) {
        String pattern = "<span class=\"sentence\">((.|\\s)*?)</span>";
        Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE
                | Pattern.DOTALL).matcher(html);

        while (matcher.find()) {
            String rangeWithoutSpan = matcher.group(1);
            String rangeWithSpan = matcher.group(0);

            html = html.replace(rangeWithSpan, rangeWithoutSpan);
        }
        return html;
    }
}
