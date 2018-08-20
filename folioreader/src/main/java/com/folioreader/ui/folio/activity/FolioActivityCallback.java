package com.folioreader.ui.folio.activity;

import com.folioreader.Config;
import com.folioreader.model.ReadPosition;

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

    int getTopDistraction();

    int getBottomDistraction();
}
