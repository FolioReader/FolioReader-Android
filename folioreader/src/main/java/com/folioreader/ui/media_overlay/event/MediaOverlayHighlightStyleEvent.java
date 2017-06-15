package com.folioreader.ui.media_overlay.event;

/**
 * @author gautam chibde on 14/6/17.
 */

public class MediaOverlayHighlightStyleEvent {
    private String style;

    public MediaOverlayHighlightStyleEvent(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }
}
