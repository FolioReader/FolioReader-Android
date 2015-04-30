package br.com.rsa.folioreader.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.rsa.folioreader.R;
import br.com.rsa.folioreader.customviews.FolioReaderWebView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FoliorRaderViewPagerFragment extends Fragment {


    public FoliorRaderViewPagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folior_rader_view_pager, container, false);
        path = getArguments().getString("path");
        webView = (FolioReaderWebView) view.findViewById(R.id.folioreader_webview);

        if (savedInstanceState != null)
            webView.restoreState(savedInstanceState);
        else
            webView.loadUrl(path);

        return view;
    }

    private String path;
    private FolioReaderWebView webView;
}
