package com.folioreader.ui.base;

import com.folioreader.model.dictionary.Dictionary;

/**
 * @author gautam chibde on 4/7/17.
 */

public interface DictionaryCallBack extends BaseMvpView {

    void onDictionaryDataReceived(Dictionary dictionary);

    //TODO
    void playMedia(String url);
}
