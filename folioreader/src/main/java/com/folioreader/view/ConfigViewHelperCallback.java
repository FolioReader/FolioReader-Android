/*
* Copyright (C) 2016 Pedro Paulo de Amorim
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.folioreader.view;

import android.support.v4.widget.ViewDragHelper;
import android.view.View;

public class ConfigViewHelperCallback extends ViewDragHelper.Callback {

    private int mDragState = 0;
    private int mDragOffset = 0;
    private ConfigView mConfigView;

    /**
     * The constructor get the instance of ConfigView
     *
     * @param mConfigView provide the instance of ConfigView
     */
    public ConfigViewHelperCallback(ConfigView mConfigView) {
        this.mConfigView = mConfigView;
    }

    /**
     * Check if view on focus is the ConfigView
     *
     * @param child     return the view on focus
     * @param pointerId return the id of view
     * @return if the child on focus is equals the ConfigView
     */
    @Override
    public boolean tryCaptureView(View child, int pointerId) {
        return child.equals(mConfigView.getContainer());
    }

    /**
     * Return the value of slide based
     * on top and height of the element
     *
     * @param child return the view on focus
     * @param top   return the top size of ConfigView
     * @param dy    return the scroll on y-axis
     * @return the offset of slide
     */
    @Override
    public int clampViewPositionVertical(View child, int top, int dy) {
        return Math.min(Math.max(top, mConfigView.getPaddingTop()),
                mConfigView.getContainer().getHeight());
    }

    /**
     * Return the max value of view that can slide
     * based on #clampViewPositionVertical
     *
     * @param child return the view on focus
     * @return max vertical distance that view on focus can slide
     */
    @Override
    public int getViewVerticalDragRange(View child) {
        return mConfigView != null ? (int) mConfigView.getVerticalDragRange() : 0;
    }

    /**
     * Verify if container is dragging or idle and
     * check mDragOffset is bigger than dragRange,
     * if true, set the visible to gone.
     *
     * @param state return the touch state of view
     */
    @Override
    public void onViewDragStateChanged(int state) {
        if (state == mDragState) {
            return;
        }
        if ((mDragState == ViewDragHelper.STATE_DRAGGING
                || mDragState == ViewDragHelper.STATE_SETTLING)
                && state == ViewDragHelper.STATE_IDLE
                && (mDragOffset == mConfigView.getVerticalDragRange())) {
            mConfigView.hideView();
        }
        mDragState = state;
    }

    /**
     * Override method used notify the drag value
     * based on position and dragRange
     *
     * @param left position.
     * @param top  position.
     * @param dx   change in X position from the last call.
     * @param dy   change in Y position from the last call.
     */
    @Override
    public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
        super.onViewPositionChanged(changedView, left, top, dx, dy);
        mDragOffset = Math.abs(top);
        float fractionScreen = (float) mDragOffset / mConfigView.getVerticalDragRange();
        mConfigView.onViewPositionChanged(fractionScreen >= 1 ? 1 : fractionScreen);
    }

    /**
     * This is called only the touch on container is released.
     *
     * @param releasedChild return the view on focus
     * @param xVel          return the speed of X animation
     * @param yVel          return the speed of Y animation
     */
    @Override
    public void onViewReleased(View releasedChild, float xVel, float yVel) {
        super.onViewReleased(releasedChild, xVel, yVel);
        if (mConfigView.isDragViewAboveTheLimit()) {
            mConfigView.moveOffScreen();
        } else {
            mConfigView.moveToOriginalPosition();
        }
    }

}