package com.folioreader.view;

import android.view.View;

import android.support.v4.widget.ViewDragHelper;

public class ConfigViewHelperCallback extends ViewDragHelper.Callback {

  private ConfigView configView;

  /**
   * The constructor get the instance of ConfigView
   *
   * @param configView provide the instance of ConfigView
   */
  public ConfigViewHelperCallback(ConfigView configView) {
    this.configView = configView;
  }

  /**
   * Check if view on focus is the ConfigView
   *
   * @param child return the view on focus
   * @param pointerId return the id of view
   * @return if the child on focus is equals the ConfigView
   */
  @Override public boolean tryCaptureView(View child, int pointerId) {
    return child.equals(configView.getContainer());
  }

  /**
   * Return the value of slide based
   * on top and height of the element
   *
   * @param child return the view on focus
   * @param top return the top size of ConfigView
   * @param dy return the scroll on y-axis
   * @return the offset of slide
   */
  @Override public int clampViewPositionVertical(View child, int top, int dy) {
    return Math.min(Math.max(top, configView.getPaddingTop()),
        configView.getContainer().getHeight());
  }

  /**
   * Return the max value of view that can slide
   * based on #clampViewPositionVertical
   *
   * @param child return the view on focus
   * @return max vertical distance that view on focus can slide
   */
  @Override public int getViewVerticalDragRange(View child) {
    return configView != null ? (int) configView.getVerticalDragRange() : 0;
  }

}