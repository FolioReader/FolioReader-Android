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

public interface TextSelectionControlListener {
    void jsError(String error);
    void jsLog(String message);
    void startSelectionMode();
    void endSelectionMode();

    /**
     * Tells the listener to show the context menu for the given range and selected text.
     * The bounds parameter contains a json string representing the selection bounds in the form 
     * { 'left': leftPoint, 'top': topPoint, 'right': rightPoint, 'bottom': bottomPoint }
     * @param range
     * @param text
     * @param handleBounds
     * @param isReallyChanged
     */
    void selectionChanged(String range, String text, String handleBounds, boolean isReallyChanged);

    void setContentWidth(float contentWidth);
}