package com.folioreader.util;

import com.folioreader.model.Highlight;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.siegmann.epublib.domain.Book;

/**
 * Created by priyank on 5/12/16.
 */
public class HighlightUtil {
    public static final int mHighlightRange = 30;

    public static Highlight matchHighlight(String html, String highlightId, Book book) {
        String contentPre = "";
        String contentPost = "";
        Highlight highlight = null;
        try {
            String pattern = "<highlight id=\\\"" + highlightId + "\" onclick=\".*?\" class=\"(.*?)\">((.|\\s)(.*)?)</highlight>";
            Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(html);
            if (matcher.find()) {
                contentPre = html.substring(matcher.start() - mHighlightRange, matcher.start());
                contentPost = html.substring(matcher.end(), matcher.end() + mHighlightRange);
                if (contentPre != null && contentPre.contains(">")) {
                    Matcher preMatcher = Pattern.compile("((?=[^>]*$)(.|\\s)*$)", Pattern.CASE_INSENSITIVE).matcher(contentPre);
                    if (preMatcher.find()) {
                        String searchString = contentPre.substring(contentPre.lastIndexOf(">"), contentPre.length());
                        if (searchString.length() > 0 && contentPre.contains(searchString))
                            contentPre = searchString;
                    }
                }

                if (contentPost != null && contentPost.contains("<")) {
                    Matcher postMatcher = Pattern.compile("^((.|\\s)*?)(?=<)", Pattern.CASE_INSENSITIVE).matcher(contentPost);
                    if (postMatcher.find()) {
                        String searchString = contentPost.substring(contentPost.indexOf("<"), contentPost.length());
                        if (searchString.length() > 0 && contentPost.contains(searchString))
                            contentPost = searchString;
                    }
                }
            }

            highlight = new Highlight();
            highlight.setContentPre(contentPre);
            highlight.setType(matcher.group(1));
            highlight.setContentPost(contentPost);
            highlight.setHighlightId(highlightId);
            highlight.setContent(matcher.group(2));
            highlight.setBookId(book.getTitle());
            highlight.setDate(Calendar.getInstance().getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return highlight;
    }
}
