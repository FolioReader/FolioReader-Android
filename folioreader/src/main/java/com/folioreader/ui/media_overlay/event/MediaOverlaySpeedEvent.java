package com.folioreader.ui.media_overlay.event;

/**
 * @author gautam chibde on 14/6/17.
 */

public class MediaOverlaySpeedEvent {

    public enum Speed {
        HALF, ONE, ONE_HALF, TWO,
    }

    private Speed speed;

    public MediaOverlaySpeedEvent(Speed speed) {
        this.speed = speed;
    }

    public Speed getSpeed() {
        return speed;
    }
}
