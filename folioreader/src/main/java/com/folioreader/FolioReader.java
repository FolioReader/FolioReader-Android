package com.folioreader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.ReadPosition;
import com.folioreader.model.sqlite.DbAdapter;
import com.folioreader.ui.base.OnSaveHighlight;
import com.folioreader.ui.base.SaveReceivedHighlightTask;
import com.folioreader.ui.folio.activity.ContentHighlightActivity;
import com.folioreader.ui.folio.activity.FolioActivity;
import com.folioreader.ui.folio.activity.SearchActivity;
import com.folioreader.util.OnHighlightListener;
import com.folioreader.util.ReadPositionListener;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by avez raj on 9/13/2017.
 */

public class FolioReader implements Application.ActivityLifecycleCallbacks {

    @SuppressLint("StaticFieldLeak")
    private static FolioReader singleton = null;
    private static final String LOG_TAG = FolioReader.class.getSimpleName();
    public static final String INTENT_BOOK_ID = "book_id";
    private Context context;
    private Config config;
    private boolean overrideConfig;
    private OnHighlightListener onHighlightListener;
    private ReadPositionListener readPositionListener;
    private ReadPosition readPosition;
    public static final String ACTION_SAVE_READ_POSITION = "com.folioreader.action.SAVE_READ_POSITION";
    public static final String ACTION_CLOSE_FOLIOREADER = "com.folioreader.action.CLOSE_FOLIOREADER";
    public static final String EXTRA_READ_POSITION = "com.folioreader.extra.READ_POSITION";

    //TODO -> Check for memory leak
    public static WeakReference<Activity> folioActivity = null;
    public static WeakReference<Activity> contentHighlightActivity = null;
    public static WeakReference<Activity> searchActivity = null;

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
            if (readPositionListener != null)
                readPositionListener.saveReadPosition(readPosition);
        }
    };

    public static FolioReader get() {

        if (singleton == null) {
            synchronized (FolioReader.class) {
                if (singleton == null) {
                    if (AppContext.get() == null) {
                        throw new IllegalArgumentException("-> context == null");
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
        intent.putExtra(INTENT_BOOK_ID, bookId);
        context.startActivity(intent);
        return singleton;
    }

    public FolioReader openBook(int rawId, int port, String bookId) {
        Intent intent = getIntentFromUrl(null, rawId);
        intent.putExtra(Config.INTENT_PORT, port);
        intent.putExtra(INTENT_BOOK_ID, bookId);
        context.startActivity(intent);
        return singleton;
    }

    public FolioReader openBook(String assetOrSdcardPath, String bookId) {
        Intent intent = getIntentFromUrl(assetOrSdcardPath, 0);
        intent.putExtra(INTENT_BOOK_ID, bookId);
        context.startActivity(intent);
        return singleton;
    }

    private Intent getIntentFromUrl(String assetOrSdcardPath, int rawId) {

        Intent intent = new Intent(context, FolioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.EXTRA_OVERRIDE_CONFIG, overrideConfig);
        intent.putExtra(FolioActivity.EXTRA_READ_POSITION, readPosition);

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

    public static synchronized void close() {

        if (singleton != null) {
            Intent intent = new Intent(FolioReader.ACTION_CLOSE_FOLIOREADER);
            LocalBroadcastManager.getInstance(singleton.context).sendBroadcast(intent);
        }
    }

    private void unregisterListeners() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(highlightReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(readPositionReceiver);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof FolioActivity) {
            folioActivity = new WeakReference<>(activity);
        } else if (activity instanceof ContentHighlightActivity) {
            contentHighlightActivity = new WeakReference<>(activity);
        } else if (activity instanceof SearchActivity) {
            searchActivity = new WeakReference<>(activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(LOG_TAG, "-> onActivityStopped -> " + activity.getClass().getSimpleName());

        if (activity instanceof FolioActivity) {
            folioActivity = null;
        } else if (activity instanceof ContentHighlightActivity) {
            contentHighlightActivity = null;
        } else if (activity instanceof SearchActivity) {
            searchActivity = null;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity instanceof FolioActivity) {
            folioActivity = null;
        } else if (activity instanceof ContentHighlightActivity) {
            contentHighlightActivity = null;
        } else if (activity instanceof SearchActivity) {
            searchActivity = null;
        }
    }
}
