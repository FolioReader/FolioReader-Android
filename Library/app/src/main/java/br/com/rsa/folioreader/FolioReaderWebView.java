package br.com.rsa.folioreader;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by rodrigo.almeida on 10/04/15.
 */
public class FolioReaderWebView extends WebView {

    public FolioReaderWebView(Context context) {
        super(context);
        init(context);
    }

    public FolioReaderWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.getSettings().setJavaScriptEnabled(true);
        this.setWebViewClient(new WebViewClient() {
        });
        setVerticalScrollbarOverlay(true);
    }

    @Override
    public int getContentHeight() {
        return this.computeVerticalScrollRange() - this.getHeight();
    }
}
