package com.folioreader.util;

import android.content.Context;
import android.content.Intent;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.ui.folio.activity.FolioActivity;

/**
 * Created by avez raj on 9/13/2017.
 */

public class FolioReader {
    public static final String INTENT_BOOK_ID = "book_id";

    public void openBook(Context context, String assetOrSdcardPath) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0, context);
        context.startActivity(intent);
    }

    public void openBook(Context context, int rawId) {
        Intent intent = getIntentFromUrl(null, rawId, context);
        context.startActivity(intent);
    }

    public void openBook(Context context, String assetOrSdcardPath, Config config) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0, context);
        intent.putExtra(Config.INTENT_CONFIG, config);
        context.startActivity(intent);
    }

    public void openBook(Context context, int rawId, Config config) {
        Intent intent = getIntentFromUrl(null, rawId, context);
        intent.putExtra(Config.INTENT_CONFIG, config);
        context.startActivity(intent);
    }

    public void openBook(Context context, String assetOrSdcardPath, Config config, int port) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0, context);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        context.startActivity(intent);
    }

    public void openBook(Context context, int rawId, Config config, int port) {
        Intent intent = getIntentFromUrl(null, rawId, context);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        context.startActivity(intent);
    }

    public void openBook(Context context, String assetOrSdcardPath, Config config, int port, String bookId) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0, context);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        intent.putExtra(INTENT_BOOK_ID, bookId);
        context.startActivity(intent);
    }

    public void openBook(Context context, int rawId, Config config, int port, String bookId) {
        Intent intent = getIntentFromUrl(null, rawId, context);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        intent.putExtra(INTENT_BOOK_ID, bookId);
        context.startActivity(intent);
    }

    private Intent getIntentFromUrl(String assetsUrl, int rawId, Context context) {
        Intent intent = new Intent(context, FolioActivity.class);
        if (rawId != 0) {
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, rawId);
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_TYPE, FolioActivity.EpubSourceType.RAW);
        } else if(assetsUrl.contains(Constants.STORAGE)) {
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, assetsUrl);
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_TYPE, FolioActivity.EpubSourceType.SD_CARD);
        } else {
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, assetsUrl);
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_TYPE, FolioActivity.EpubSourceType.ASSETS);
        }
        return intent;
    }
}
