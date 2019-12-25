package com.folioreader.ui.base;

import android.content.Context;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;

/**
 * @author gautam chibde on 14/6/17.
 */

public final class HtmlUtil {

    /**
     * Function modifies input html string by adding extra css,js and font information.
     *
     * @param context     Activity Context
     * @param htmlContent input html raw data
     * @return modified raw html string
     */
    public static String getHtmlContent(Context context, String htmlContent, Config config) {

        String cssPath =
                String.format(context.getString(R.string.css_tag), "file:///android_asset/css/Style.css");

        String fontFamily = null;
        String fontStyle = null;

        switch (config.getFont()) {
            case Constants.FONT_ARVO:
                fontFamily = "arvo";
                fontStyle = "sans-serif";
                break;
            case Constants.FONT_LATO:
                fontFamily = "lato";
                fontStyle = "serif";
                break;
            case Constants.FONT_LORA:
                fontFamily = "lora";
                fontStyle = "serif";
                break;
            case Constants.FONT_UBUNTU:
                fontFamily = "ubuntu";
                fontStyle = "sans-serif";
                break;
            default:
                // default or internal epub fonts will be used
                break;
        }

        if (fontFamily != null) {
            String[] textElements = {"p", "span", "h1", "h2", "h3", "h4", "h5", "h6"};
            String fontFamilyStyle = "{font-family:\"{font family}\", {font style} !important;}"
                    .replace("{font family}", fontFamily)
                    .replace("{font style}", fontStyle);

            cssPath = cssPath + "\n<style>\n";
            for (String e : textElements) {
                cssPath = cssPath + "    " + e + " " + fontFamilyStyle + "\n";
            }
            cssPath = cssPath + "</style>";
        }

        String jsPath = String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/jsface.min.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/jquery-3.1.1.min.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-core.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-highlighter.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-classapplier.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-serializer.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/Bridge.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangefix.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/readium-cfi.umd.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag_method_call),
                "setMediaOverlayStyleColors('#C0ED72','#C0ED72')") + "\n";

        jsPath = jsPath
                + "<meta name=\"viewport\" content=\"height=device-height, user-scalable=no\" />";

        String toInject = "\n" + cssPath + "\n" + jsPath + "\n</head>";
        htmlContent = htmlContent.replace("</head>", toInject);

        String classes = "";
        switch (config.getFont()) {
            case Constants.FONT_ARVO:
                classes = "arvo";
                break;
            case Constants.FONT_LATO:
                classes = "lato";
                break;
            case Constants.FONT_LORA:
                classes = "lora";
                break;
            case Constants.FONT_UBUNTU:
                classes = "ubuntu";
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

        htmlContent = htmlContent.replace("<html", "<html class=\""+ classes + "\"" +
                " onclick=\"onClickHtml()\"");
        return htmlContent;
    }
}
