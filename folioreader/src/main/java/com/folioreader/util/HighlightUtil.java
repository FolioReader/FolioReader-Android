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
        String contentPre;
        String contentPost;
        Highlight highlight = null;
        try {
            String pattern = "<highlight id=\"" + highlightId
                    + "\" onclick=\".*?\" class=\"(.*?)\">(.*?)</highlight>";
            Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL).matcher(html);
            if (matcher.find()) {
                contentPre = html.substring(matcher.start() - mHighlightRange, matcher.start());
                contentPost = html.substring(matcher.end(), matcher.end() + mHighlightRange);
                if (contentPre.contains(">")) {
                    Matcher preMatcher = Pattern.compile("((?=[^>]*$)(.|\\s)*$)",
                            Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(contentPre);
                    if (preMatcher.find()) {
                        contentPre = contentPre.substring(contentPre.lastIndexOf('>') + 1,
                                contentPre.length());
                    }
                }
                if (contentPost.contains("<")) {
                    Matcher postMatcher = Pattern.compile("^((.|\\s)*?)(?=<)",
                            Pattern.CASE_INSENSITIVE
                            | Pattern.DOTALL).matcher(contentPost);
                    if (postMatcher.find()) {
                        contentPost = contentPost.substring(0, contentPost.indexOf('<'));
                    }
                }

                String content = matcher.group(2);
                content = removeSentenceSpam(content);
                contentPost = removeSentenceSpam(contentPost);
                contentPre = removeSentenceSpam(contentPre);

                highlight = new Highlight();
                highlight.setContentPre(contentPre);
                highlight.setType(matcher.group(1));
                highlight.setContentPost(contentPost);
                highlight.setHighlightId(highlightId);
                highlight.setContent(content);
                highlight.setBookId(bookTitle);
                highlight.setPage(pageNo);
                highlight.setDate(Calendar.getInstance().getTime());
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return highlight;
    }

    private static String removeSentenceSpam(String html) {
        String pattern = "<span class=\"sentence\">((.|\\s)*?)</span>";
        Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE
                | Pattern.DOTALL).matcher(html);

        while (matcher.find()){
            String rangeWithoutSpan = matcher.group(1);
            String rangeWithSpan = matcher.group(0);

            html = html.replace(rangeWithSpan, rangeWithoutSpan);
        }
        return html;
    }
}
