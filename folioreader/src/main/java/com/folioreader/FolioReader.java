package com.folioreader;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.ReadPosition;
import com.folioreader.model.sqlite.DbAdapter;
import com.folioreader.ui.base.OnSaveHighlight;
import com.folioreader.ui.base.SaveReceivedHighlightTask;
import com.folioreader.ui.folio.activity.FolioActivity;
import com.folioreader.util.OnHighlightListener;
import com.folioreader.util.ReadPositionListener;

import java.util.List;

/**
 * Created by avez raj on 9/13/2017.
 */

public class FolioReader {

    @SuppressLint("StaticFieldLeak")
    private static FolioReader singleton = null;
    public static final String INTENT_BOOK_ID = "book_id";
    private Context context;
    private OnHighlightListener onHighlightListener;
    private ReadPositionListener readPositionListener;
    private ReadPosition readPosition;
    public static final String ACTION_SAVE_READ_POSITION = "com.folioreader.action.SAVE_READ_POSITION";
    public static final String EXTRA_READ_POSITION = "com.folioreader.extra.READ_POSITION";

    private BroadcastReceiver highlightReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HighlightImpl highlightImpl = intent.getParcelableExtra(HighlightImpl.INTENT);
            HighLight.HighLightAction action = (HighLight.HighLightAction)
                    intent.getSerializableExtra(HighLight.HighLightAction.class.getName());
            if (onHighlightListener != null && highlightImpl != null && action != null) {
                onHighlightListener.onHighlight(highlightImpl, action);
            }
        }
    };

    private BroadcastReceiver readPositionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ReadPosition readPosition =
                    intent.getParcelableExtra(FolioReader.EXTRA_READ_POSITION);
            if (readPositionListener != null )
                readPositionListener.saveReadPosition(readPosition);
        }
    };

    public static FolioReader getInstance(Context context) {

        if (singleton == null) {
            synchronized (FolioReader.class) {
                if (singleton == null) {
                    if (context == null) {
                        throw new IllegalArgumentException("-> context cannot be null");
                    }
                    singleton = new FolioReader(context.getApplicationContext());
                }
            }
        }
        return singleton;
    }

    private FolioReader() {
    }

    private FolioReader(Context context) {
        this.context = context;
        DbAdapter.initialize(context);
        LocalBroadcastManager.getInstance(context).registerReceiver(highlightReceiver,
                new IntentFilter(HighlightImpl.BROADCAST_EVENT));
        LocalBroadcastManager.getInstance(context).registerReceiver(readPositionReceiver,
                new IntentFilter(ACTION_SAVE_READ_POSITION));
    }

    public FolioReader openBook(String assetOrSdcardPath) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        context.startActivity(intent);
        return singleton;
    }

    public FolioReader openBook(int rawId) {
        Intent intent = getIntentFromUrl(null, rawId);
        context.startActivity(intent);
        return singleton;
    }

    public FolioReader openBook(String assetOrSdcardPath, Config config) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(Config.INTENT_CONFIG, config);
        context.startActivity(intent);
        return singleton;
    }

    public FolioReader openBook(int rawId, Config config) {
        Intent intent = getIntentFromUrl(null, rawId);
        intent.putExtra(Config.INTENT_CONFIG, config);
        context.startActivity(intent);
        return singleton;
    }

    public FolioReader openBook(String assetOrSdcardPath, Config config, int port) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        context.startActivity(intent);
        return singleton;
    }

    public FolioReader openBook(int rawId, Config config, int port) {
        Intent intent = getIntentFromUrl(null, rawId);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        context.startActivity(intent);
        return singleton;
    }

    public FolioReader openBook(String assetOrSdcardPath, Config config, int port, String bookId) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        intent.putExtra(INTENT_BOOK_ID, bookId);
        context.startActivity(intent);
        return singleton;
    }

    public FolioReader openBook(int rawId, Config config, int port, String bookId) {
        Intent intent = getIntentFromUrl(null, rawId);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        intent.putExtra(INTENT_BOOK_ID, bookId);
        context.startActivity(intent);
        return singleton;
    }

    public FolioReader openBook(String assetOrSdcardPath, Config config, String bookId) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(INTENT_BOOK_ID, bookId);
        context.startActivity(intent);
        return singleton;
    }

    private Intent getIntentFromUrl(String assetOrSdcardPath, int rawId) {

        Intent intent = new Intent(context, FolioActivity.class);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FolioActivity.EXTRA_READ_POSITION, (Parcelable) readPosition);

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

    public FolioReader setOnHighlightListener(OnHighlightListener onHighlightListener) {
        this.onHighlightListener = onHighlightListener;
        return singleton;
    }

    public FolioReader setReadPositionListener(ReadPositionListener readPositionListener) {
        this.readPositionListener = readPositionListener;
        return singleton;
    }

    public FolioReader setReadPosition(ReadPosition readPosition) {
        this.readPosition = readPosition;
        return singleton;
    }

    public void saveReceivedHighLights(List<HighLight> highlights, OnSaveHighlight onSaveHighlight) {
        new SaveReceivedHighlightTask(onSaveHighlight, highlights).execute();
    }

    /**
     * Nullifies readPosition and listeners.
     * This method ideally should be used in onDestroy() of Activity or Fragment.
     * Use this method if you want to use FolioReader singleton instance again in the application,
     * else use {@link #stop()} which destruct the FolioReader singleton instance.
     */
    public static synchronized void clear() {

        if (singleton != null) {
            singleton.readPosition = null;
            singleton.onHighlightListener = null;
            singleton.readPositionListener = null;
        }
    }

    /**
     * Destructs the FolioReader singleton instance.
     * Use this method only if you are sure that you won't need to use
     * FolioReader singleton instance again in application, else use {@link #clear()}.
     */
    public static synchronized void stop() {

        if (singleton != null) {
            DbAdapter.terminate();
            singleton.unregisterListeners();
            singleton = null;
        }
    }

    private void unregisterListeners() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(highlightReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(readPositionReceiver);
    }
}
