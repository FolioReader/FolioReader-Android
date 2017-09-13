package com.folioreader.util;

import android.content.Context;
import android.content.Intent;

import com.folioreader.Config;
import com.folioreader.ui.folio.activity.FolioActivity;

/**
 * Created by avez raj on 9/13/2017.
 */

public class FolioReader {
    public static final String INTENT_BOOK_ID = "book_id";

    public void openBook(String assetsUrl, int rawId, Context context) {
        Intent intent = getIntentFromUrl(assetsUrl,rawId,context);
        context.startActivity(intent);
    }

    public void openBook(String assetsUrl, int rawId ,Config config, int port, Context context) {
        Intent intent = getIntentFromUrl(assetsUrl,rawId,context);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        context.startActivity(intent);
    }

    public void openBook(String epubBookUrl, int rawId, String bookId, int port, Context context) {
        Intent intent = getIntentFromUrl(epubBookUrl,rawId,context);
        intent.putExtra(INTENT_BOOK_ID, bookId);
        intent.putExtra(Config.INTENT_PORT, port);
        context.startActivity(intent);

    }

    public void openBook(String epubBookUrl,int rawId, Config config, String bookId, int port, Context context) {
        Intent intent = getIntentFromUrl(epubBookUrl,rawId,context);
        intent.putExtra(Config.INTENT_CONFIG,config);
        intent.putExtra(INTENT_BOOK_ID, bookId);
        intent.putExtra(Config.INTENT_PORT, port);
        context.startActivity(intent);

    }

    private Intent getIntentFromUrl(String assetsUrl, int rawId,Context context) {
        Intent intent = new Intent(context, FolioActivity.class);
        if (rawId != 0) {
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, rawId);
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_TYPE, FolioActivity.EpubSourceType.RAW);
        } else {
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, assetsUrl);
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_TYPE, FolioActivity.EpubSourceType.ASSETS);
        }
        return intent;
    }
}
