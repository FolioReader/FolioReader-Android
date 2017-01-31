package com.folioreader.util;

import android.util.Log;

import com.folioreader.model.Highlight;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by priyank on 5/12/16.
 */
public class HighlightUtil {
    public static final int mHighlightRange = 30;
    private static final String TAG = HighlightUtil.class.getSimpleName();

    public static Highlight matchHighlight(String html, String highlightId, String bookTitle, int pageNo) {
        String contentPre = "";
        String contentPost = "";
        Highlight highlight = null;
        try {
            String pattern = "<highlight id=\"" + highlightId
                    + "\" onclick=\".*?\" class=\"(.*?)\">(.*?)</highlight>";
            Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL).matcher(html);
            if (matcher.find()) {
                contentPre = html.substring(matcher.start() - mHighlightRange, matcher.start());
                contentPost = html.substring(matcher.end(), matcher.end() + mHighlightRange);
                if (contentPre != null && contentPre.contains(">")) {
                    Matcher preMatcher = Pattern.compile("((?=[^>]*$)(.|\\s)*$)",
                            Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(contentPre);
                    if (preMatcher.find()) {
                        String searchString =
                                contentPre.substring(contentPre.lastIndexOf('>') + 1,
                                        contentPre.length());
                        contentPre = searchString;
                    }
                }
                if (contentPost != null && contentPost.contains("<")) {
                    Matcher postMatcher = Pattern.compile("^((.|\\s)*?)(?=<)",
                            Pattern.CASE_INSENSITIVE
                            | Pattern.DOTALL).matcher(contentPost);
                    if (postMatcher.find()) {
                        String searchString = contentPost.substring(0, contentPost.indexOf('<'));
                        contentPost = searchString;
                    }
                }
                highlight = new Highlight();
                highlight.setContentPre(contentPre);
                highlight.setType(matcher.group(1));
                highlight.setContentPost(contentPost);
                highlight.setHighlightId(highlightId);
                highlight.setContent(matcher.group(2));
                highlight.setBookId(bookTitle);
                highlight.setPage(pageNo);
                highlight.setDate(Calendar.getInstance().getTime());
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return highlight;
    }
}
