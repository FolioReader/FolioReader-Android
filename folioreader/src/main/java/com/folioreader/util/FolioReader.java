package com.folioreader.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.ReadPosition;
import com.folioreader.model.sqlite.DbAdapter;
import com.folioreader.ui.base.OnSaveHighlight;
import com.folioreader.ui.base.SaveReceivedHighlightTask;
import com.folioreader.ui.folio.activity.FolioActivity;

import java.util.List;

/**
 * Created by avez raj on 9/13/2017.
 */

public class FolioReader {

    public static final String INTENT_BOOK_ID = "book_id";
    private Context context;
    private OnHighlightListener onHighlightListener;
    private ReadPositionCallback readPositionCallback;
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
            if (readPositionCallback != null )
                readPositionCallback.saveReadPosition(readPosition);
        }
    };

    public FolioReader(Context context) {
        this.context = context;
        new DbAdapter(context);
        LocalBroadcastManager.getInstance(context).registerReceiver(highlightReceiver,
                new IntentFilter(HighlightImpl.BROADCAST_EVENT));
        LocalBroadcastManager.getInstance(context).registerReceiver(readPositionReceiver,
                new IntentFilter(ACTION_SAVE_READ_POSITION));
    }

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

    public void openBook(int rawId, Config config, int port, String bookId) {
        Intent intent = getIntentFromUrl(null, rawId);
        intent.putExtra(Config.INTENT_CONFIG, config);
        intent.putExtra(Config.INTENT_PORT, port);
        intent.putExtra(INTENT_BOOK_ID, bookId);
        context.startActivity(intent);
    }

    private Intent getIntentFromUrl(String assetOrSdcardPath, int rawId) {

        Intent intent = new Intent(context, FolioActivity.class);
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

    public void registerHighlightListener(OnHighlightListener onHighlightListener) {
        this.onHighlightListener = onHighlightListener;
    }

    public void unregisterHighlightListener() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(highlightReceiver);
        this.onHighlightListener = null;
    }

    public void setReadPositionCallback(ReadPositionCallback readPositionCallback) {
        this.readPositionCallback = readPositionCallback;
    }

    public void removeReadPositionCallback() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(readPositionReceiver);
        readPositionCallback = null;
    }

    public void setReadPosition(ReadPosition readPosition) {
        this.readPosition = readPosition;
    }

    public void saveReceivedHighLights(List<HighLight> highlights, OnSaveHighlight onSaveHighlight) {
        new SaveReceivedHighlightTask(onSaveHighlight, highlights).execute();
    }
}
