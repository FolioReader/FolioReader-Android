package com.folioreader.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.webkit.JavascriptInterface;
import android.widget.ProgressBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.util.AppUtil;
import com.folioreader.util.UiUtil;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class LoadingView extends ConstraintLayout {

    private ProgressBar progressBar;
    private int maxVisibleDuration = -1;
    private Handler handler;

    private Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private static final String LOG_TAG = LoadingView.class.getSimpleName();

    public LoadingView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

        LayoutInflater.from(context).inflate(R.layout.view_loading, this);

        if (isInEditMode())
            return;

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.LoadingView,
                0, 0);

        maxVisibleDuration = typedArray.getInt(R.styleable.LoadingView_maxVisibleDuration, -1);

        handler = new Handler();
        progressBar = findViewById(R.id.progressBar);

        setClickable(true);
        setFocusable(true);

        updateTheme();

        if (getVisibility() == VISIBLE)
            show();
    }

    public void updateTheme() {

        Config config = AppUtil.getSavedConfig(getContext());
        if (config == null)
            config = new Config();
        UiUtil.setColorIntToDrawable(config.getThemeColor(), progressBar.getIndeterminateDrawable());
        if (config.isNightMode()) {
            setBackgroundColor(ContextCompat.getColor(getContext(), R.color.night_background_color));
        } else {
            setBackgroundColor(ContextCompat.getColor(getContext(), R.color.day_background_color));
        }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void show() {
        //Log.d(LOG_TAG, "-> show");

        handler.removeCallbacks(hideRunnable);
        handler.post(new Runnable() {
            @Override
            public void run() {
                setVisibility(VISIBLE);
            }
        });

        if (maxVisibleDuration > -1)
            handler.postDelayed(hideRunnable, maxVisibleDuration);
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void hide() {
        //Log.d(LOG_TAG, "-> hide");

        handler.removeCallbacks(hideRunnable);
        handler.post(new Runnable() {
            @Override
            public void run() {
                setVisibility(INVISIBLE);
            }
        });
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void visible() {
        //Log.d(LOG_TAG, "-> visible");
        handler.post(new Runnable() {
            @Override
            public void run() {
                setVisibility(VISIBLE);
            }
        });
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void invisible() {
        //Log.d(LOG_TAG, "-> invisible");
        handler.post(new Runnable() {
            @Override
            public void run() {
                setVisibility(INVISIBLE);
            }
        });
    }

    public int getMaxVisibleDuration() {
        return maxVisibleDuration;
    }

    public void setMaxVisibleDuration(int maxVisibleDuration) {
        this.maxVisibleDuration = maxVisibleDuration;
    }
}
