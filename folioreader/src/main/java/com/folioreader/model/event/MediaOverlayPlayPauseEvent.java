package com.folioreader.model.event;

/**
 * @author gautam chibde on 13/6/17.
 */

public class MediaOverlayPlayPauseEvent {
    private boolean play;
    private String href;
    private boolean stateChanged;

    public MediaOverlayPlayPauseEvent(String href, boolean play, boolean stateChanged) {
        this.href = href;
        this.play = play;
        this.stateChanged = stateChanged;
    }

    public String getHref() {
        return href;
    }

    public boolean isPlay() {
        return play;
    }

    public boolean isStateChanged() {
        return stateChanged;
    }
}
