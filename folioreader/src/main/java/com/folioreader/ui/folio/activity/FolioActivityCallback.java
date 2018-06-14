package com.folioreader.ui.folio.activity;

import com.folioreader.model.ReadPosition;
import com.folioreader.view.DirectionalViewpager;

public interface FolioActivityCallback {

    int getChapterPosition();

    void setPagerToPosition(String href);

    ReadPosition getEntryReadPosition();

    void goToChapter(String href);

    DirectionalViewpager.Direction getDirection();

    void onDirectionChange(DirectionalViewpager.Direction newDirection);

    void storeLastReadPosition(ReadPosition lastReadPosition);
}
