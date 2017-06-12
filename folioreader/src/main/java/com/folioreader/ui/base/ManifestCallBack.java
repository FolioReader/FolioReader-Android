package com.folioreader.ui.base;

import org.readium.r2_streamer.model.publication.EpubPublication;

/**
 * @author by gautam chibde on 12/6/17.
 */

public interface ManifestCallBack extends BaseMvpView {

    void onReceivePublication(EpubPublication publication);
}
