package com.folioreader.model.event;

/**
 * Created by Shrikant on 7/28/2017.
 */

public class AnchorIdEvent {
    private String href;

    public AnchorIdEvent() {
    }

    public AnchorIdEvent(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }
}