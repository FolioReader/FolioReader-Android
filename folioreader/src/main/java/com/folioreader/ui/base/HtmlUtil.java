package com.folioreader.ui.base;

import android.content.Context;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.model.Highlight;
import com.folioreader.model.sqlite.HighLightTable;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author gautam chibde on 14/6/17.
 */

public final class HtmlUtil {

    /**
     * Function modifies input html string by adding extra css,js and font information.
     *
     * @param context     Activity Context
     * @param htmlContent input html raw data
     * @param mBookTitle  Epub book title
     * @return modified raw html string
     */
    public static String getHtmlContent(Context context, String htmlContent, String mBookTitle) {
        String cssPath =
                String.format(context.getString(R.string.css_tag), "file:///android_asset/Style.css");
        String jsPath =
                String.format(context.getString(R.string.script_tag),
                        "file:///android_asset/Bridge.js");
        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag),
                        "file:///android_asset/jquery-1.8.3.js");
        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag),
                        "file:///android_asset/jpntext.js");
        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag),
                        "file:///android_asset/rangy-core.js");
        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag),
                        "file:///android_asset/rangy-serializer.js");
        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag),
                        "file:///android_asset/android.selection.js");
        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag_method_call),
                        "setMediaOverlayStyleColors('#C0ED72','#C0ED72')");
        String toInject = "\n" + cssPath + "\n" + jsPath + "\n</head>";
        htmlContent = htmlContent.replace("</head>", toInject);

        String classes = "";
        Config config = Config.getConfig();
        switch (config.getFont()) {
            case Constants.FONT_ANDADA:
                classes = "andada";
                break;
            case Constants.FONT_LATO:
                classes = "lato";
                break;
            case Constants.FONT_LORA:
                classes = "lora";
                break;
            case Constants.FONT_RALEWAY:
                classes = "raleway";
                break;
            default:
                break;
        }

        if (config.isNightMode()) {
            classes += " nightMode";
        }

        switch (config.getFontSize()) {
            case 0:
                classes += " textSizeOne";
                break;
            case 1:
                classes += " textSizeTwo";
                break;
            case 2:
                classes += " textSizeThree";
                break;
            case 3:
                classes += " textSizeFour";
                break;
            case 4:
                classes += " textSizeFive";
                break;
            default:
                break;
        }

        htmlContent = htmlContent.replace("<html ", "<html class=\"" + classes + "\" ");
        ArrayList<Highlight> highlights = HighLightTable.getAllHighlights(mBookTitle);
        for (Highlight highlight : highlights) {
            String highlightStr = highlight.getContentPre() +
                    "<highlight id=\"" + highlight.getHighlightId() +
                    "\" onclick=\"callHighlightURL(this);\" class=\"" +
                    highlight.getType() + "\">" + highlight.getContent() + "</highlight>" + highlight.getContentPost();
            String searchStr = highlight.getContentPre() +
                    "" + highlight.getContent() + "" + highlight.getContentPost();
            htmlContent = htmlContent.replaceFirst(Pattern.quote(searchStr), highlightStr);
        }
        return htmlContent;
    }
}
