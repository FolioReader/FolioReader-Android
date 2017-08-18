/*
 * Copyright (C) 2012 - 2014 Brandon Tate, bossturbo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bossturban.webviewmarker;

import android.webkit.JavascriptInterface;

public class TextSelectionController {
    public static final String TAG = "TextSelectionController";
    public static final String INTERFACE_NAME = "TextSelection";

    private TextSelectionControlListener mListener;

    public TextSelectionController(TextSelectionControlListener listener) {
        mListener = listener;
    }

    @JavascriptInterface
    public void jsError(String error) {
        if (mListener != null) {
            mListener.jsError(error);
        }
    }
    @JavascriptInterface
    public void jsLog(String message) {
        if (mListener != null) {
            mListener.jsLog(message);
        }
    }
    @JavascriptInterface
    public void startSelectionMode() {
        if (mListener != null) {
            mListener.startSelectionMode();
        }
    }
    @JavascriptInterface
    public void endSelectionMode() {
        if (mListener != null) {
            mListener.endSelectionMode();
        }
    }
    @JavascriptInterface
    public void selectionChanged(String range, String text, String handleBounds, boolean isReallyChanged) {
        if (mListener != null) {
            mListener.selectionChanged(range, text, handleBounds, isReallyChanged);
        }
    }
    @JavascriptInterface
    public void setContentWidth(float contentWidth) {
        if (mListener != null) {
            mListener.setContentWidth(contentWidth);
        }
    }
}
