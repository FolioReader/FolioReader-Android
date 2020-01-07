package com.folioreader.ui.base;

import android.content.Context;

import com.folioreader.model.dictionary.VietnameseDictionary;

public interface VietnameseDictionaryCallback extends BaseMvpView {
    void onVietnameseDictionaryDataReceived(VietnameseDictionary vietnameseDictionary);
    Context getContext_();
}
