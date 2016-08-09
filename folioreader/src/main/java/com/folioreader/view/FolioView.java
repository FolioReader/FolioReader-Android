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

import com.folioreader.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class FolioView extends FrameLayout implements View.OnClickListener {

    private View shadowView;

    private FolioViewCallback folioViewCallback;

    public FolioView(Context context) {
        this(context, null);
    }

    public FolioView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FolioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Bind the attributes of the view and config
     * the DragView with these params.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            inflateView();
        }
    }

    @Override
    public void onClick(View view) {
        folioViewCallback.onShadowClick();
    }

    public void setFolioViewCallback(FolioViewCallback folioViewCallback) {
        this.folioViewCallback = folioViewCallback;
    }

    private void inflateView() {
        inflate(getContext(), R.layout.view_folio, this);
        shadowView = findViewById(R.id.shadow);
        configClickListener();
    }

    private void configClickListener() {
        shadowView.setOnClickListener(this);
    }

    public void updateShadowAlpha(float alpha) {
        float invertedShadow = 1 - alpha;
        shadowView.setAlpha(invertedShadow);
        if (invertedShadow == 0.0) {
            shadowView.setVisibility(GONE);
        }
    }

    public void resetView() {
        shadowView.setVisibility(VISIBLE);
    }

}
