package com.folioreader.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.model.Highlight;
import com.folioreader.ui.folio.activity.FolioActivity;

/**
 * Created by avez raj on 9/13/2017.
 */

public class FolioReader {
    public static final String INTENT_BOOK_ID = "book_id";
    private Context context;

    private OnHighlightCreateListener onHighlightCreateListener;

    public FolioReader(Context context) {
        this.context = context;
        LocalBroadcastManager.getInstance(context).registerReceiver(highlightReceiver,
                new IntentFilter(Highlight.BROADCAST_EVENT));
    }

    private BroadcastReceiver highlightReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Highlight highlight = intent.getParcelableExtra(Highlight.INTENT);
            Highlight.HighLightAction action = (Highlight.HighLightAction)
                    intent.getSerializableExtra(Highlight.HighLightAction.class.getName());
            if (onHighlightCreateListener != null && highlight != null && action != null) {
                onHighlightCreateListener.onCreateHighlight(highlight, action);
            }
        }
    };

    public void openBook(String assetOrSdcardPath) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        context.startActivity(intent);
    }

    public void openBook(int rawId) {
        Intent intent = getIntentFromUrl(null, rawId);
        context.startActivity(intent);
    }

    public void openBook(String assetOrSdcardPath, Config config) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(Config.INTENT_CONFIG, config);
        context.startActivity(intent);
    }

    public void openBook(int rawId, Config config) {
        Intent intent = getIntentFromUrl(null, rawId);
        intent.putExtra(Config.INTENT_CONFIG, config);
        context.startActivity(intent);
    }

    public void openBook(String assetOrSdcardPath, Config config, int port) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        context.startActivity(intent);
    }

    public void openBook(int rawId, Config config, int port) {
        Intent intent = getIntentFromUrl(null, rawId);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        context.startActivity(intent);
    }

    public void openBook(String assetOrSdcardPath, Config config, int port, String bookId) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        intent.putExtra(INTENT_BOOK_ID, bookId);
        context.startActivity(intent);
    }

    public void openBook(Context context, int rawId, Config config, int port, String bookId) {
        Intent intent = getIntentFromUrl(null, rawId);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        intent.putExtra(INTENT_BOOK_ID, bookId);
        context.startActivity(intent);
    }

    private Intent getIntentFromUrl(String assetOrSdcardPath, int rawId) {
        Intent intent = new Intent(context, FolioActivity.class);
        if (rawId != 0) {
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, rawId);
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_TYPE, FolioActivity.EpubSourceType.RAW);
        } else if (assetOrSdcardPath.contains(Constants.ASSET)) {
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, assetOrSdcardPath);
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_TYPE, FolioActivity.EpubSourceType.ASSETS);
        } else {
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, assetOrSdcardPath);
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_TYPE, FolioActivity.EpubSourceType.SD_CARD);
        }
        return intent;
    }

    public void setOnHighlightCreateListener(OnHighlightCreateListener onHighlightCreateListener) {
        this.onHighlightCreateListener = onHighlightCreateListener;
    }

    public void unSubscribe() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(highlightReceiver);
    }
}
