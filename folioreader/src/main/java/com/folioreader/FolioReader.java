package com.folioreader;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.locators.ReadLocator;
import com.folioreader.model.sqlite.DbAdapter;
import com.folioreader.ui.base.OnSaveHighlight;
import com.folioreader.ui.base.SaveReceivedHighlightTask;
import com.folioreader.ui.folio.activity.FolioActivity;
import com.folioreader.util.OnHighlightListener;
import com.folioreader.util.ReadLocatorListener;

import java.util.List;

/**
 * Created by avez raj on 9/13/2017.
 */

public class FolioReader {

    @SuppressLint("StaticFieldLeak")
    private static FolioReader singleton = null;

    public static final String EXTRA_BOOK_ID = "com.folioreader.extra.BOOK_ID";
    public static final String EXTRA_READ_LOCATOR = "com.folioreader.extra.READ_LOCATOR";
    public static final String ACTION_SAVE_READ_LOCATOR = "com.folioreader.action.SAVE_READ_LOCATOR";
    public static final String ACTION_CLOSE_FOLIOREADER = "com.folioreader.action.CLOSE_FOLIOREADER";
    public static final String ACTION_FOLIOREADER_CLOSED = "com.folioreader.action.FOLIOREADER_CLOSED";

    private Context context;
    private Config config;
    private boolean overrideConfig;
    private OnHighlightListener onHighlightListener;
    private ReadLocatorListener readLocatorListener;
    private OnClosedListener onClosedListener;
    private ReadLocator readLocator;

    public interface OnClosedListener {
        /**
         * You may call {@link FolioReader#clear()} in this method, if you wouldn't require to open
         * an epub again from the current activity.
         * Or you may call {@link FolioReader#stop()} in this method, if you wouldn't require to open
         * an epub again from your application.
         */
        void onFolioReaderClosed();
    }

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

    private BroadcastReceiver readLocatorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ReadLocator readLocator =
                    (ReadLocator) intent.getSerializableExtra(FolioReader.EXTRA_READ_LOCATOR);
            if (readLocatorListener != null)
                readLocatorListener.saveReadLocator(readLocator);
        }
    };

    private BroadcastReceiver closedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (onClosedListener != null)
                onClosedListener.onFolioReaderClosed();
        }
    };

    public static FolioReader get() {

        if (singleton == null) {
            synchronized (FolioReader.class) {
                if (singleton == null) {
                    if (AppContext.get() == null) {
                        throw new IllegalStateException("-> context == null");
                    }
                    singleton = new FolioReader(AppContext.get());
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

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.registerReceiver(highlightReceiver,
                new IntentFilter(HighlightImpl.BROADCAST_EVENT));
        localBroadcastManager.registerReceiver(readLocatorReceiver,
                new IntentFilter(ACTION_SAVE_READ_LOCATOR));
        localBroadcastManager.registerReceiver(closedReceiver,
                new IntentFilter(ACTION_FOLIOREADER_CLOSED));
    }

    public FolioReader openEncryptedBook(String assetOrSdcardPath, String key) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(FolioActivity.EXTRA_BOOK_KEY, key);
        context.startActivity(intent);
        return singleton;
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

    public FolioReader openBook(String assetOrSdcardPath, int port) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(Config.INTENT_PORT, port);
        context.startActivity(intent);
        return singleton;
    }

    public FolioReader openBook(int rawId, int port) {
        Intent intent = getIntentFromUrl(null, rawId);
        intent.putExtra(Config.INTENT_PORT, port);
        context.startActivity(intent);
        return singleton;
    }

    public FolioReader openBook(String assetOrSdcardPath, int port, String bookId) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(Config.INTENT_PORT, port);
        intent.putExtra(EXTRA_BOOK_ID, bookId);
        context.startActivity(intent);
        return singleton;
    }

    public FolioReader openBook(int rawId, int port, String bookId) {
        Intent intent = getIntentFromUrl(null, rawId);
        intent.putExtra(Config.INTENT_PORT, port);
        intent.putExtra(EXTRA_BOOK_ID, bookId);
        context.startActivity(intent);
        return singleton;
    }

    public FolioReader openBook(String assetOrSdcardPath, String bookId) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(EXTRA_BOOK_ID, bookId);
        context.startActivity(intent);
        return singleton;
    }

    private Intent getIntentFromUrl(String assetOrSdcardPath, int rawId) {

        Intent intent = new Intent(context, FolioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.EXTRA_OVERRIDE_CONFIG, overrideConfig);
        intent.putExtra(FolioActivity.EXTRA_READ_LOCATOR, (Parcelable) readLocator);

        if (rawId != 0) {
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, rawId);
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_TYPE,
                    FolioActivity.EpubSourceType.RAW);
        } else if (assetOrSdcardPath.contains(Constants.ASSET)) {
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, assetOrSdcardPath);
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_TYPE,
                    FolioActivity.EpubSourceType.ASSETS);
        } else {
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, assetOrSdcardPath);
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_TYPE,
                    FolioActivity.EpubSourceType.SD_CARD);
        }

        return intent;
    }

    /**
     * Pass your configuration and choose to override it every time or just for first execution.
     *
     * @param config         custom configuration.
     * @param overrideConfig true will override the config, false will use either this
     *                       config if it is null in application context or will fetch previously
     *                       saved one while execution.
     */
    public FolioReader setConfig(Config config, boolean overrideConfig) {
        this.config = config;
        this.overrideConfig = overrideConfig;
        return singleton;
    }

    public FolioReader setOnHighlightListener(OnHighlightListener onHighlightListener) {
        this.onHighlightListener = onHighlightListener;
        return singleton;
    }

    public FolioReader setReadLocatorListener(ReadLocatorListener readLocatorListener) {
        this.readLocatorListener = readLocatorListener;
        return singleton;
    }

    public FolioReader setOnClosedListener(OnClosedListener onClosedListener) {
        this.onClosedListener = onClosedListener;
        return singleton;
    }

    public FolioReader setReadLocator(ReadLocator readLocator) {
        this.readLocator = readLocator;
        return singleton;
    }

    public void saveReceivedHighLights(List<HighLight> highlights,
                                       OnSaveHighlight onSaveHighlight) {
        new SaveReceivedHighlightTask(onSaveHighlight, highlights).execute();
    }

    /**
     * Closes all the activities related to FolioReader.
     * After closing all the activities of FolioReader, callback can be received in
     * {@link OnClosedListener#onFolioReaderClosed()} if implemented.
     * Developer is still bound to call {@link #clear()} or {@link #stop()}
     * for clean up if required.
     */
    public void close() {
        Intent intent = new Intent(FolioReader.ACTION_CLOSE_FOLIOREADER);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Nullifies readLocator and listeners.
     * This method ideally should be used in onDestroy() of Activity or Fragment.
     * Use this method if you want to use FolioReader singleton instance again in the application,
     * else use {@link #stop()} which destruct the FolioReader singleton instance.
     */
    public static synchronized void clear() {

        if (singleton != null) {
            singleton.readLocator = null;
            singleton.onHighlightListener = null;
            singleton.readLocatorListener = null;
            singleton.onClosedListener = null;
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
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.unregisterReceiver(highlightReceiver);
        localBroadcastManager.unregisterReceiver(readLocatorReceiver);
        localBroadcastManager.unregisterReceiver(closedReceiver);
    }
}
