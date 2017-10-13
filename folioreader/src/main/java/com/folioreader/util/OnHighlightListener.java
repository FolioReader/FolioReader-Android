package com.folioreader.util;

import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;

/**
 * Interface to convey highlight events.
 *
 * @author gautam chibde on 26/9/17.
 */

public interface OnHighlightListener {

    /**
     * This method will be invoked when a highlight is created, deleted or modified.
     *
     * @param highlight meta-data for created highlight {@link HighlightImpl}.
     * @param type      type of event e.g new,edit or delete {@link com.folioreader.model.HighlightImpl.HighLightAction}.
     */
    void onHighlight(HighLight highlight, HighLight.HighLightAction type);
}
