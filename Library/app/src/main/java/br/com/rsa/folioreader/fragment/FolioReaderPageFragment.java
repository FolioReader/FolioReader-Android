package br.com.rsa.folioreader.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.rsa.folioreader.FolioReaderWebView;
import br.com.rsa.folioreader.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FolioReaderPageFragment extends Fragment {

    private String urlToLoad;
    private FolioReaderWebView webView;

    public FolioReaderPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_folio_reader_page, container, false);
        this.urlToLoad = getArguments().getString("url");
        webView = (FolioReaderWebView) view.findViewById(R.id.webView);

        if (savedInstanceState != null)
            webView.restoreState(savedInstanceState);
        else
            webView.loadData(urlToLoad, "text/html", "UTF-8");

        webView.setCurrentIndex(getArguments().getInt("currentIndex"));
        webView.setTotalIndex(getArguments().getInt("maxIndex"));

        return view;
    }
}
