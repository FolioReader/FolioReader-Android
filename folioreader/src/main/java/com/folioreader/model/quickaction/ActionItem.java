package com.folioreader.model.quickaction;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Action item, displayed as menu with mIcon and text.
 *
 * @author Lorensius. W. L. T <lorenz@londatiga.net>
 *
 *         Contributors:
 *         - Kevin Peck <kevinwpeck@gmail.com>
 */
public class ActionItem {
    private Drawable mIcon;
    private Bitmap mThumb;
    private String mTitle;
    private int mActionId = -1;
    private boolean mSelected;
    private boolean mSticky;

    /**
     * Constructor
     *
     * @param mActionId Action id for case statements
     * @param mTitle    Title
     * @param mIcon     Icon to use
     */
    public ActionItem(int mActionId, String mTitle, Drawable mIcon) {
        this.mTitle = mTitle;
        this.mIcon = mIcon;
        this.mActionId = mActionId;
    }

    /**
     * Constructor
     */
    public ActionItem() {
        this(-1, null, null);
    }

    /**
     * Constructor
     *
     * @param mActionId Action id of the item
     * @param mTitle    Text to show for the item
     */
    public ActionItem(int mActionId, String mTitle) {
        this(mActionId, mTitle, null);
    }

    /**
     * Constructor
     *
     * @param mIcon {@link Drawable} action mIcon
     */
    public ActionItem(Drawable mIcon) {
        this(-1, null, mIcon);
    }

    /**
     * Constructor
     *
     * @param mActionId Action ID of item
     * @param mIcon     {@link Drawable} action mIcon
     */
    public ActionItem(int mActionId, Drawable mIcon) {
        this(mActionId, null, mIcon);
    }

    /**
     * Set action mTitle
     *
     * @param mTitle action mTitle
     */
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    /**
     * Get action mTitle
     *
     * @return action mTitle
     */
    public String getTitle() {
        return this.mTitle;
    }

    /**
     * Set action mIcon
     *
     * @param mIcon {@link Drawable} action mIcon
     */
    public void setIcon(Drawable mIcon) {
        this.mIcon = mIcon;
    }

    /**
     * Get action mIcon
     *
     * @return {@link Drawable} action mIcon
     */
    public Drawable getIcon() {
        return this.mIcon;
    }

    /**
     * Set action id
     *
     * @param mActionId Action id for this action
     */
    public void setActionId(int mActionId) {
        this.mActionId = mActionId;
    }

    /**
     * @return Our action id
     */
    public int getActionId() {
        return mActionId;
    }

    /**
     * Set mSticky status of button
     *
     * @param mSticky true for mSticky, pop up sends event but does not disappear
     */
    public void setSticky(boolean mSticky) {
        this.mSticky = mSticky;
    }

    /**
     * @return true if button is mSticky, menu stays visible after press
     */
    public boolean isSticky() {
        return mSticky;
    }

    /**
     * Set mSelected flag;
     *
     * @param mSelected Flag to indicate the item is mSelected
     */
    public void setSelected(boolean mSelected) {
        this.mSelected = mSelected;
    }

    /**
     * Check if item is mSelected
     *
     * @return true or false
     */
    public boolean isSelected() {
        return this.mSelected;
    }

    /**
     * Set mThumb
     *
     * @param mThumb Thumb image
     */
    public void setThumb(Bitmap mThumb) {
        this.mThumb = mThumb;
    }

    /**
     * Get mThumb image
     *
     * @return Thumb image
     */
    public Bitmap getThumb() {
        return this.mThumb;
    }
}