package com.folioreader.model.event;

/**
 * Created by PC on 12/24/2016.
 */

public class WebViewPosition {
    private int webviewPos;
    private String href;

    public WebViewPosition(int webviewPos, String href) {
        this.webviewPos = webviewPos;
        this.href = href;
    }

    public int getWebviewPos() {
        return webviewPos;
    }

    public String getHref() {
        return href;
    }

    @Override
    public String toString() {
        return "WebViewPosition{" +
                "webviewPos=" + webviewPos +
                ", href='" + href + '\'' +
                '}';
    }
}
