package com.folioreader.mediaoverlay;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.folioreader.Constants;
import com.folioreader.model.event.MediaOverlayPlayPauseEvent;
import com.folioreader.model.event.MediaOverlaySpeedEvent;
import com.folioreader.model.media_overlay.OverlayItems;
import com.folioreader.util.UiUtil;
import org.readium.r2.shared.Clip;
import org.readium.r2.shared.MediaOverlays;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * @author gautam chibde on 21/6/17.
 */

public class MediaController {

    private static final String TAG = MediaController.class.getSimpleName();

    public enum MediaType {
        TTS, SMIL,
    }

    private MediaType mediaType;
    private MediaControllerCallbacks callbacks;
    private Context context;

    //**********************************//
    //          MEDIA OVERLAY           //
    //**********************************//
    private MediaOverlays mediaOverlays;
    private List<OverlayItems> mediaItems = new ArrayList<>();
    private int mediaItemPosition = 0;
    private MediaPlayer mediaPlayer;

    private Clip currentClip;

    private boolean isMediaPlayerReady;
    private Handler mediaHandler;

    //*********************************//
    //              TTS                //
    //*********************************//
    private TextToSpeech mTextToSpeech;
    private boolean mIsSpeaking = false;

    public MediaController(Context context, MediaType mediaType, MediaControllerCallbacks callbacks) {
        this.mediaType = mediaType;
        this.callbacks = callbacks;
        this.context = context;
    }

    private Runnable mHighlightTask = new Runnable() {
        @Override
        public void run() {
            int currentPosition = mediaPlayer.getCurrentPosition();
            if (mediaPlayer.getDuration() != currentPosition) {
                if (mediaItemPosition < mediaItems.size()) {
                    //int end = (int) currentClip.end * 1000;
                    int end = (int) (currentClip.getEnd() * 1000);
                    if (currentPosition > end) {
                        mediaItemPosition++;
                        currentClip = mediaOverlays.clip(mediaItems.get(mediaItemPosition).getId());
                        if (currentClip != null) {
                            callbacks.highLightText(mediaItems.get(mediaItemPosition).getId());
                        } else {
                            mediaItemPosition++;
                        }
                    }
                    mediaHandler.postDelayed(mHighlightTask, 10);
                } else {
                    mediaHandler.removeCallbacks(mHighlightTask);
                }
            }
        }
    };

    public void resetMediaPosition() {
        mediaItemPosition = 0;
    }

    public void setSMILItems(List<OverlayItems> overlayItems) {
        this.mediaItems = overlayItems;
    }

    public void next() {
        mediaItemPosition++;
    }

    public void setTextToSpeech(final Context context) {
        mTextToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.UK);
                    mTextToSpeech.setSpeechRate(0.70f);
                }

                mTextToSpeech.setOnUtteranceCompletedListener(
                        new TextToSpeech.OnUtteranceCompletedListener() {
                            @Override
                            public void onUtteranceCompleted(String utteranceId) {
                                ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mIsSpeaking) {
                                            callbacks.highLightTTS();
                                        }
                                    }
                                });
                            }
                        });
            }
        });
    }

    public void setUpMediaPlayer(MediaOverlays mediaOverlays, String path, String mBookTitle) {
        this.mediaOverlays = mediaOverlays;
        mediaHandler = new Handler();
        try {
            mediaItemPosition = 0;
            String uri = Constants.DEFAULT_STREAMER_URL + mBookTitle + path;
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(uri);
            mediaPlayer.prepare();
            isMediaPlayerReady = true;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void setSpeed(MediaOverlaySpeedEvent.Speed speed) {
        switch (speed) {
            case HALF:
                setPlaybackSpeed(0.5f);
                break;
            case ONE:
                setPlaybackSpeed(1.0f);
                break;
            case ONE_HALF:
                setPlaybackSpeed(1.5f);
                break;
            case TWO:
                setPlaybackSpeed(2.0f);
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void setPlaybackSpeed(float speed) {
        if (mediaType == MediaType.SMIL) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            }
        } else {
            mTextToSpeech.setSpeechRate(speed);
        }
    }

    public void stateChanged(MediaOverlayPlayPauseEvent event) {
        if (event.isStateChanged()) {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
            if (mTextToSpeech != null && mTextToSpeech.isSpeaking()) {
                mTextToSpeech.stop();
            }
        } else {
            if (event.isPlay()) {
                UiUtil.keepScreenAwake(true, context);
            } else {
                UiUtil.keepScreenAwake(false, context);
            }
            if (mediaType == MediaType.SMIL) {
                playSMIL();
            } else {
                if (mTextToSpeech.isSpeaking()) {
                    mTextToSpeech.stop();
                    mIsSpeaking = false;
                    callbacks.resetCurrentIndex();
                } else {
                    mIsSpeaking = true;
                    callbacks.highLightTTS();
                }
            }
        }
    }

    private void playSMIL() {
        if (mediaPlayer != null && isMediaPlayerReady) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                currentClip = mediaOverlays.clip(mediaItems.get(mediaItemPosition).getId());
                if (currentClip != null) {
                    mediaPlayer.start();
                    mediaHandler.post(mHighlightTask);
                } else {
                    mediaItemPosition++;
                    mediaPlayer.start();
                    mediaHandler.post(mHighlightTask);
                }
            }
        }
    }

    public void speakAudio(String sentence) {
        if (mediaType == MediaType.TTS) {
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "stringId");
            mTextToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, params);
        }
    }

    public void stop() {
        if (mTextToSpeech != null) {
            if (mTextToSpeech.isSpeaking()) {
                mTextToSpeech.stop();
            }
            mTextToSpeech.shutdown();
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            mediaHandler.removeCallbacks(mHighlightTask);
        }
    }
}
