package com.folioreader.quickaction;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Action item, displayed as menu with icon and text.
 *
 * @author Lorensius. W. L. T <lorenz@londatiga.net>
 *
 *         Contributors:
 *         - Kevin Peck <kevinwpeck@gmail.com>
 */
public class ActionItem {
    private Drawable icon;
    private Bitmap thumb;
    private String title;
    private int actionId = -1;
    private boolean selected;
    private boolean sticky;

    /**
     * Constructor
     *
     * @param actionId Action id for case statements
     * @param title    Title
     * @param icon     Icon to use
     */
    public ActionItem(int actionId, String title, Drawable icon) {
        this.title = title;
        this.icon = icon;
        this.actionId = actionId;
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
     * @param actionId Action id of the item
     * @param title    Text to show for the item
     */
    public ActionItem(int actionId, String title) {
        this(actionId, title, null);
    }

    /**
     * Constructor
     *
     * @param icon {@link Drawable} action icon
     */
    public ActionItem(Drawable icon) {
        this(-1, null, icon);
    }

    /**
     * Constructor
     *
     * @param actionId Action ID of item
     * @param icon     {@link Drawable} action icon
     */
    public ActionItem(int actionId, Drawable icon) {
        this(actionId, null, icon);
    }

    /**
     * Set action title
     *
     * @param title action title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get action title
     *
     * @return action title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set action icon
     *
     * @param icon {@link Drawable} action icon
     */
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    /**
     * Get action icon
     *
     * @return {@link Drawable} action icon
     */
    public Drawable getIcon() {
        return this.icon;
    }

    /**
     * Set action id
     *
     * @param actionId Action id for this action
     */
    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    /**
     * @return Our action id
     */
    public int getActionId() {
        return actionId;
    }

    /**
     * Set sticky status of button
     *
     * @param sticky true for sticky, pop up sends event but does not disappear
     */
    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    /**
     * @return true if button is sticky, menu stays visible after press
     */
    public boolean isSticky() {
        return sticky;
    }

    /**
     * Set selected flag;
     *
     * @param selected Flag to indicate the item is selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Check if item is selected
     *
     * @return true or false
     */
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * Set thumb
     *
     * @param thumb Thumb image
     */
    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    /**
     * Get thumb image
     *
     * @return Thumb image
     */
    public Bitmap getThumb() {
        return this.thumb;
    }
}