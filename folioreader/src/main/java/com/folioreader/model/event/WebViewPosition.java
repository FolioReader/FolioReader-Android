package com.folioreader.model.event;

/**
 * Created by PC on 12/24/2016.
 */

public class WebViewPosition {
    private int webviewPos;
    private String href;
    private String highlightId;

    public WebViewPosition(int webviewPos, String href, String highlightId) {
        this.webviewPos = webviewPos;
        this.href = href;
        this.highlightId = highlightId;
    }

    public int getWebviewPos() {
        return webviewPos;
    }

    public void setWebviewPos(int webviewPos) {
        this.webviewPos = webviewPos;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getHighlightId() {
        return highlightId;
    }

    public void setHighlightId(String highlightId) {
        this.highlightId = highlightId;
    }




}
