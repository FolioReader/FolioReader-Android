package br.com.rsa.folioreader.entities;

import br.com.rsa.folioreader.enummerators.ScrollDirection;

/**
 * Created by rodrigo.almeida on 15/04/15.
 */
public class FingerPairDirection {
    private ScrollDirection x;
    private ScrollDirection y;

    public FingerPairDirection() {
        this(ScrollDirection.None, ScrollDirection.None);
    }

    public FingerPairDirection(ScrollDirection x, ScrollDirection y) {
        this.x = x;
        this.y = y;
    }

    public void setX(ScrollDirection x) {
        this.x = x;
    }

    public void setY(ScrollDirection y) {
        this.y = y;
    }

    public ScrollDirection getX() {
        return x;
    }

    public ScrollDirection getY() {
        return y;
    }
}
