package com.folioreader.fragments;

import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.view.ObservableWebView;
import com.folioreader.view.VerticalSeekbar;

import org.w3c.dom.Text;

/**
 * Created by mahavir on 4/2/16.
 */
public class FolioPageFragment extends Fragment  {
    public static final String KEY_FRAGMENT_FOLIO_POSITION = "com.folioreader.fragments.FolioPageFragment.POSITION";

    private View mRootView;
    private VerticalSeekbar mScrollSeekbar;

    public static FolioPageFragment newInstance(int position) {
        FolioPageFragment fragment = new FolioPageFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_FRAGMENT_FOLIO_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public static interface FolioPageFragmentCallback {
        public String getChapterHtmlContent(int position);
        public void hideOrshowToolBar();
        public  void hideToolBarIfVisible();
    }

    private int mPosition = -1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_FRAGMENT_FOLIO_POSITION)) {
            mPosition = savedInstanceState.getInt(KEY_FRAGMENT_FOLIO_POSITION);
        } else {
            mPosition = getArguments().getInt(KEY_FRAGMENT_FOLIO_POSITION);
        }

        String htmlContent = getHtmlContent();

        mRootView = View.inflate(getActivity(), R.layout.folio_page_fragment, null);
        mScrollSeekbar = (VerticalSeekbar)mRootView.findViewById(R.id.scrollSeekbar);
        mScrollSeekbar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.app_green), PorterDuff.Mode.SRC_IN);

        ObservableWebView webView = (ObservableWebView) mRootView.findViewById(R.id.contentWebView);
        final Boolean[] mMoveOccured = new Boolean[1];
        final float[] mDownPosX = new float[1];
        final float[] mDownPosY = new float[1];
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final float MOVE_THRESHOLD_DP = 20 * getResources().getDisplayMetrics().density;

                final int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mMoveOccured[0] = false;
                        mDownPosX[0] = event.getX();
                        mDownPosY[0] = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!mMoveOccured[0]) {
                            //Toast.makeText(v.getContext(), "Webview pressed", Toast.LENGTH_SHORT).show();

                            ((FolioPageFragmentCallback)getActivity()).hideOrshowToolBar();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getX() - mDownPosX[0]) > MOVE_THRESHOLD_DP || Math.abs(event.getY() - mDownPosY[0]) > MOVE_THRESHOLD_DP) {
                            Log.d("**********", "Webview Scroll");
                            ((FolioPageFragmentCallback)getActivity()).hideToolBarIfVisible();
                            mMoveOccured[0] = true;
                        }
                        break;
                }
                return false;
            }
        });
        webView.setVerticalScrollBarEnabled(false);
        webView.setScrollListener(new ObservableWebView.ScrollListener() {
            @Override
            public void onScrollChange(float percent) {
                mScrollSeekbar.setProgressAndThumb((int)percent);
            }
        });
        webView.loadDataWithBaseURL(null, htmlContent, "text/html; charset=UTF-8", "UTF-8", null);

        return mRootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_FRAGMENT_FOLIO_POSITION, mPosition);
    }

    public void reload(){
        WebView webView = (WebView) mRootView.findViewById(R.id.contentWebView);
        String htmlContent = getHtmlContent();
        webView.loadDataWithBaseURL(null, htmlContent, "text/html; charset=UTF-8", "UTF-8", null);
    }

    private String getHtmlContent(){
        String htmlContent = "???";
        if (getActivity() instanceof FolioPageFragmentCallback && mPosition != -1){
            htmlContent = ((FolioPageFragmentCallback)getActivity()).getChapterHtmlContent(mPosition);
        }

        String cssPath = String.format("<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\">", "file:///android_asset/Style.css");
        String jsPath = String.format("<script type=\"text/javascript\" src=\"%s\"></script>", "file:///android_asset/Bridge.js");
        String toInject = "\n"+cssPath+"\n"+jsPath+"\n</head>";
        htmlContent = htmlContent.replace("</head>", toInject);

        String classes = "";
        Config config = Config.getConfig();
        switch (config.getFont()) {
            case 0:
                classes="andada";
                break;
            case 1:
                classes="lato";
                break;
            case 2:
                classes="lora";
                break;
            case 3:
                classes="raleway";
                break;
            default:
                break;
        }

        if (config.isNightMode()){
            classes += " nightMode";
        }

        switch (config.getFontSize()){
            case 0:
                classes += " textSizeOne";
                break;
            case 1:
                classes += " textSizeTwo";
                break;
            case 2:
                classes += " textSizeThree";
                break;
            case 3:
                classes += " textSizeFour";
                break;
            case 4:
                classes += " textSizeFive";
                break;
            default:
                break;
        }

        htmlContent = htmlContent.replace("<html ", "<html class=\""+classes+"\" ");
        return htmlContent;
    }
}
