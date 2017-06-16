package com.folioreader.model.event;

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
