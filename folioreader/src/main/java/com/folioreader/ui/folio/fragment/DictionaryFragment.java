package com.folioreader.ui.folio.fragment;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.model.dictionary.Dictionary;
import com.folioreader.model.dictionary.Wikipedia;
import com.folioreader.ui.base.DictionaryCallBack;
import com.folioreader.ui.base.DictionaryTask;
import com.folioreader.ui.base.WikipediaCallBack;
import com.folioreader.ui.base.WikipediaTask;
import com.folioreader.ui.folio.adapter.DictionaryAdapter;

import java.io.IOException;

/**
 * @author gautam chibde on 4/7/17.
 */

public class DictionaryFragment extends DialogFragment implements DictionaryCallBack, WikipediaCallBack {

    private static final String TAG = "DictionaryFragment";

    private String word;

    private MediaPlayer mediaPlayer;
    private RecyclerView dictResults;
    private TextView noNetwork, dictionary, wikipedia, wikiWord, def;
    private ProgressBar progressBar;
    private Button googleSearch;
    private LinearLayout wikiLayout;
    private WebView wikiWebView;
    private DictionaryAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
        word = getArguments().getString(Constants.SELECTED_WORD);
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_dictionary, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noNetwork = (TextView) view.findViewById(R.id.no_network);
        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        dictResults = (RecyclerView) view.findViewById(R.id.rv_dict_results);

        googleSearch = (Button) view.findViewById(R.id.btn_google_search);
        dictionary = (TextView) view.findViewById(R.id.btn_dictionary);
        wikipedia = (TextView) view.findViewById(R.id.btn_wikipedia);

        wikiLayout = (LinearLayout) view.findViewById(R.id.ll_wiki);
        wikiWord = (TextView) view.findViewById(R.id.tv_word);
        def = (TextView) view.findViewById(R.id.tv_def);
        wikiWebView = (WebView) view.findViewById(R.id.wv_wiki);
        wikiWebView.getSettings().setLoadsImagesAutomatically(true);
        wikiWebView.setWebViewClient(new WebViewClient());
        wikiWebView.getSettings().setJavaScriptEnabled(true);
        wikiWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        dictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDictionary();
            }
        });

        wikipedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadWikipedia();
            }
        });

        googleSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, word);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        dictResults.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new DictionaryAdapter(getActivity(), this);
        loadDictionary();
    }

    private void loadDictionary() {
        if(noNetwork.getVisibility() == View.VISIBLE || googleSearch.getVisibility() == View.VISIBLE) {
            noNetwork.setVisibility(View.GONE);
            googleSearch.setVisibility(View.GONE);
        }
        wikiWebView.loadUrl("about:blank");
        mAdapter.clear();
        dictionary.setSelected(true);
        wikipedia.setSelected(false);
        wikiLayout.setVisibility(View.GONE);
        dictResults.setVisibility(View.VISIBLE);
        DictionaryTask task = new DictionaryTask(this);
        String baseUrl = Constants.DICTIONARY_BASE_URL + word.trim();
        task.execute(baseUrl);
    }

    private void loadWikipedia() {
        if(noNetwork.getVisibility() == View.VISIBLE || googleSearch.getVisibility() == View.VISIBLE) {
            noNetwork.setVisibility(View.GONE);
            googleSearch.setVisibility(View.GONE);
        }
        wikiWebView.loadUrl("about:blank");
        mAdapter.clear();
        wikiLayout.setVisibility(View.VISIBLE);
        dictResults.setVisibility(View.GONE);
        dictionary.setSelected(false);
        wikipedia.setSelected(true);
        WikipediaTask task = new WikipediaTask(this);
        task.execute(Constants.WIKIPEDIA_API_URL + word.trim());
    }

    @Override
    public void onError() {
        noNetwork.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        noNetwork.setText("offline");
        googleSearch.setVisibility(View.GONE);
    }

    @Override
    public void onDictionaryDataReceived(Dictionary dictionary) {
        progressBar.setVisibility(View.GONE);
        if (dictionary.getResults().isEmpty()) {
            noNetwork.setVisibility(View.VISIBLE);
            googleSearch.setVisibility(View.VISIBLE);
            noNetwork.setText("Word not found");
        } else {
            mAdapter.setResults(dictionary.getResults());
            dictResults.setAdapter(mAdapter);
        }
    }

    @Override
    @SuppressWarnings("PMD.InefficientEmptyStringCheck")
    public void onWikipediaDataReceived(Wikipedia wikipedia) {
        wikiWord.setText(wikipedia.getWord());
        if (wikipedia.getDefinition().trim().isEmpty()) {
            def.setVisibility(View.GONE);
        } else {
            String definition = "\"" +
                    wikipedia.getDefinition() +
                    "\"";
            def.setText(definition);
        }
        wikiWebView.loadUrl(wikipedia.getLink());
    }

    //TODO
    @Override
    public void playMedia(String url) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                Log.e(TAG, "playMedia failed", e);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
