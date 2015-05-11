package br.com.rsa.folioreader.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.rsa.folioreader.R;
import br.com.rsa.folioreader.configuration.Configuration;
import br.com.rsa.folioreader.customviews.FolioReaderWebView;
import br.com.rsa.folioreader.utils.FolioReaderUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class FoliorRaderViewPagerFragment extends Fragment {


    public FoliorRaderViewPagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folior_rader_view_pager, container, false);
        path = getArguments().getString(Configuration.PATH_DECOMPRESSED);
        baseURL = getArguments().getString(Configuration.BASE_URL);

        String pathCSS = "file:///android_asset/style.css";

        webView = (FolioReaderWebView) view.findViewById(R.id.folioreader_webview);

        String data = FolioReaderUtils.getStringFromFile(path);

        String cssTag = "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + pathCSS + "\">";
        String toInject = "\n" + cssTag + " \n</head>";

        data = data.replace("</head>", toInject);

        webView.loadDataWithBaseURL(baseURL, data, "text/html", "UTF-8", null);

        return view;
    }

    private String path;
    private String baseURL;
    private FolioReaderWebView webView;
}
