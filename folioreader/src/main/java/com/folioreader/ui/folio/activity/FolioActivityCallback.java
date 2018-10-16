package com.folioreader.ui.folio.activity;

import android.graphics.Rect;

import com.folioreader.Config;
import com.folioreader.model.DisplayUnit;
import com.folioreader.model.ReadPosition;

import java.lang.ref.WeakReference;

public interface FolioActivityCallback {

    int getCurrentChapterIndex();

    ReadPosition getEntryReadPosition();

    boolean goToChapter(String href);

    Config.Direction getDirection();

    void onDirectionChange(Config.Direction newDirection);

    void storeLastReadPosition(ReadPosition lastReadPosition);

    void toggleSystemUI();

    void setDayMode();

    void setNightMode();

    int getTopDistraction(final DisplayUnit unit);

    int getBottomDistraction(final DisplayUnit unit);

    Rect getViewportRect(final DisplayUnit unit);

    WeakReference<FolioActivity> getActivity();
}
