package com.folioreader.ui.base;

import com.folioreader.model.dictionary.EnglishDictionary;

import java.util.Dictionary;

/**
 * @author gautam chibde on 4/7/17.
 */

public interface EnglishDictionaryCallBack extends BaseMvpView {

    void onEnglishDictionaryDataReceived(EnglishDictionary dictionary);

    //TODO
    void playMedia(String url);
}
