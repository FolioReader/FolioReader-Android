package com.folioreader.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.folioreader.R;

/**
 * Created by mahavir on 4/2/16.
 */
public class FolioPageFragment extends Fragment {
    public static final String KEY_FRAGMENT_FOLIO_POSITION = "com.folioreader.fragments.FolioPageFragment.POSITION";
    public static FolioPageFragment newInstance(int position) {
        FolioPageFragment fragment = new FolioPageFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_FRAGMENT_FOLIO_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public static interface FolioPageFragmentCallback {
        public String getChapterHtmlContent(int position);
    }

    private int mPosition = -1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_FRAGMENT_FOLIO_POSITION)) {
            mPosition = savedInstanceState.getInt(KEY_FRAGMENT_FOLIO_POSITION);
        } else {
            mPosition = getArguments().getInt(KEY_FRAGMENT_FOLIO_POSITION);
        }

        String htmlContent = "???";
        if (getActivity() instanceof FolioPageFragmentCallback && mPosition != -1){
            htmlContent = ((FolioPageFragmentCallback)getActivity()).getChapterHtmlContent(mPosition);
        }

        View rootView = View.inflate(getActivity(), R.layout.folio_page_fragment, null);
        WebView webView = (WebView) rootView.findViewById(R.id.contentWebView);
        webView.loadData(htmlContent, "text/html; charset=UTF-8", "UTF-8");

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_FRAGMENT_FOLIO_POSITION, mPosition);
    }
}
