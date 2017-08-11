package com.folioreader.ui.tableofcontents.presenter;

import com.folioreader.model.TOCLinkWrapper;
import com.folioreader.ui.base.BaseMvpView;

import java.util.ArrayList;

/**
 * @author gautam chibde on 8/6/17.
 */

public interface TOCMvpView extends BaseMvpView {

    void onLoadTOC(ArrayList<TOCLinkWrapper> tocLinkWrapperList);
}
