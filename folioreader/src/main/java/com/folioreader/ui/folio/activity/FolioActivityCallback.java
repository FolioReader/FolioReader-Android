package com.folioreader.ui.folio.activity;

import com.folioreader.Config;
import com.folioreader.model.ReadPosition;

public interface FolioActivityCallback {

    int getChapterPosition();

    void setPagerToPosition(String href);

    ReadPosition getEntryReadPosition();

    void goToChapter(String href);

    Config.Direction getDirection();

    void onDirectionChange(Config.Direction newDirection);

    void storeLastReadPosition(ReadPosition lastReadPosition);
}
