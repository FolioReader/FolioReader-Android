package br.com.rsa.folioreader.utils;

import android.view.MotionEvent;

import br.com.rsa.folioreader.entities.FingerPairDirection;
import br.com.rsa.folioreader.enummerators.ScrollDirection;

/**
 * Created by rodrigo.almeida on 15/04/15.
 */
public class FingerUtils {
    private static float downXValue = 0;
    private static float downYValue = 0;

    public static FingerPairDirection getPairDirection(MotionEvent event) {
        FingerPairDirection fingerPairDirection = new FingerPairDirection();
        fingerPairDirection.setX(ScrollDirection.None);
        fingerPairDirection.setY(ScrollDirection.None);


        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                downXValue = event.getX();
                downYValue = event.getY();
                break;
            }

            case MotionEvent.ACTION_UP: {
                float currentX = event.getX();
                float currentY = event.getY();

                if (Math.abs(downXValue - currentX) > Math.abs(downYValue - currentY)) {
                    if (downXValue < currentX) {
                        fingerPairDirection.setX(ScrollDirection.Right);
                    }

                    if (downXValue > currentX) {
                        fingerPairDirection.setX(ScrollDirection.Left);
                    }

                } else {
                    if (downYValue < currentY) {
                        fingerPairDirection.setY(ScrollDirection.Down);
                    }
                    if (downYValue > currentY) {
                        fingerPairDirection.setY(ScrollDirection.Up);
                    }
                }
                break;
            }

        }

        return fingerPairDirection;
    }
}
