package com.folioreader.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Created by mahavir on 3/31/16.
 */
public class CustomLinearLayoutManager extends LinearLayoutManager {
    private boolean mIsScrollEnabled = true;

    public CustomLinearLayoutManager(Context context) {
        super(context);
    }

    public void setScrollEnabled(boolean flag) {
        this.mIsScrollEnabled = flag;
    }

    @Override
    public boolean canScrollVertically() {
        return mIsScrollEnabled && super.canScrollVertically();
    }
}