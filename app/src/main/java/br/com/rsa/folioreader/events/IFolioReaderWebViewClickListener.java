package br.com.rsa.folioreader.events;

import android.view.View;

/**
 * Created by rodrigo.almeida on 11/05/15.
 */
public interface IFolioReaderWebViewClickListener {
    public void onWebViewLinkClick(View view, String url);
}
