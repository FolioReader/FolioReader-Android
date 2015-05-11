package br.com.rsa.folioreader.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by rodrigo.almeida on 28/04/15.
 */
public class FolioReaderWebView extends WebView {
    public FolioReaderWebView(Context context) {
        super(context);
        init();
    }

    public FolioReaderWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FolioReaderWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.getSettings().setJavaScriptEnabled(true);
        this.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
    }

    @Override
    public int getContentHeight() {
        return this.computeVerticalScrollRange() - this.getHeight();
    }
}
