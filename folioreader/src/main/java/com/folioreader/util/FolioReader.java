package com.folioreader.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.sqlite.DbAdapter;
import com.folioreader.ui.base.OnSaveHighlight;
import com.folioreader.ui.base.SaveReceivedHighlightTask;
import com.folioreader.ui.folio.activity.FolioActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by avez raj on 9/13/2017.
 */

public class FolioReader {
    public static final String INTENT_BOOK_ID = "book_id";
    private Context context;

    private List<HighlightImpl> h = new ArrayList<>();

    private OnHighlightListener onHighlightListener;

    public FolioReader(Context context) {
        this.context = context;
        new DbAdapter(context);
        LocalBroadcastManager.getInstance(context).registerReceiver(highlightReceiver,
                new IntentFilter(HighlightImpl.BROADCAST_EVENT));
    }

    private BroadcastReceiver highlightReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HighlightImpl highlightImpl = intent.getParcelableExtra(HighlightImpl.INTENT);
            h.add(highlightImpl);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Log.i("json", objectMapper.writeValueAsString(h));
            } catch (Exception e) {
                e.printStackTrace();
            }


            HighLight.HighLightAction action = (HighLight.HighLightAction)
                    intent.getSerializableExtra(HighLight.HighLightAction.class.getName());
            if (onHighlightListener != null && highlightImpl != null && action != null) {
                onHighlightListener.onHighlight(highlightImpl, action);
            }
        }
    };

    public void openBook(String assetOrSdcardPath) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        context.startActivity(intent);
    }

    public void openBook(final String assetOrSdcardPath,
                         List<HighLight> highlights) {
        saveReceivedHighLights(new OnSaveHighlight() {
            @Override
            public void onFinished() {
                openBook(assetOrSdcardPath);
            }
        }, highlights);
    }


    public void openBook(int rawId) {
        Intent intent = getIntentFromUrl(null, rawId);
        context.startActivity(intent);
    }

    public void openBook(final int rawId,
                         List<HighLight> highlights) {
        saveReceivedHighLights(new OnSaveHighlight() {
            @Override
            public void onFinished() {
                openBook(rawId);
            }
        }, highlights);
    }

    public void openBook(String assetOrSdcardPath, Config config) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(Config.INTENT_CONFIG, config);
        context.startActivity(intent);
    }

    public void openBook(final String assetOrSdcardPath,
                         final Config config,
                         List<HighLight> highlights) {
        saveReceivedHighLights(new OnSaveHighlight() {
            @Override
            public void onFinished() {
                openBook(assetOrSdcardPath, config);
            }
        }, highlights);
    }

    public void openBook(int rawId, Config config) {
        Intent intent = getIntentFromUrl(null, rawId);
        intent.putExtra(Config.INTENT_CONFIG, config);
        context.startActivity(intent);
    }

    public void openBook(final int rawId,
                         final Config config,
                         List<HighLight> highlights) {
        saveReceivedHighLights(new OnSaveHighlight() {
            @Override
            public void onFinished() {
                openBook(rawId, config);
            }
        }, highlights);
    }

    public void openBook(String assetOrSdcardPath, Config config, int port) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        context.startActivity(intent);
    }

    public void openBook(final String assetOrSdcardPath,
                         final Config config,
                         final int port,
                         List<HighLight> highlights) {
        saveReceivedHighLights(new OnSaveHighlight() {
            @Override
            public void onFinished() {
                openBook(assetOrSdcardPath, config, port);
            }
        }, highlights);
    }

    public void openBook(int rawId, Config config, int port) {
        Intent intent = getIntentFromUrl(null, rawId);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        context.startActivity(intent);
    }

    public void openBook(final int rawId,
                         final Config config,
                         final int port, List<HighLight> highlights) {
        saveReceivedHighLights(new OnSaveHighlight() {
            @Override
            public void onFinished() {
                openBook(rawId, config, port);
            }
        }, highlights);
    }

    public void openBook(String assetOrSdcardPath, Config config, int port, String bookId) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        intent.putExtra(INTENT_BOOK_ID, bookId);
        context.startActivity(intent);
    }

    public void openBook(final String assetOrSdcardPath,
                         final Config config,
                         final int port,
                         final String bookId,
                         List<HighLight> highlights) {
        saveReceivedHighLights(new OnSaveHighlight() {
            @Override
            public void onFinished() {
                openBook(assetOrSdcardPath, config, port, bookId);
            }
        }, highlights);
    }

    public void openBook(int rawId, Config config, int port, String bookId) {
        Intent intent = getIntentFromUrl(null, rawId);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        intent.putExtra(INTENT_BOOK_ID, bookId);
        context.startActivity(intent);
    }

    public void openBook(final int rawId,
                         final Config config,
                         final int port,
                         final String bookId,
                         List<HighLight> highlights) {
        saveReceivedHighLights(new OnSaveHighlight() {
            @Override
            public void onFinished() {
                openBook(rawId, config, port, bookId);
            }
        }, highlights);
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

    public void registerHighlightListener(OnHighlightListener onHighlightListener) {
        this.onHighlightListener = onHighlightListener;
    }

    public void unregisterHighlightListener() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(highlightReceiver);
        this.onHighlightListener = null;
    }

    private void saveReceivedHighLights(OnSaveHighlight onSaveHighlight,
                                        List<HighLight> highlights) {
        new SaveReceivedHighlightTask(onSaveHighlight, highlights).execute();
    }
}
