package com.folioreader.ui.fragment;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.model.dictionary.EnglishDictionary;
import com.folioreader.model.dictionary.VietnameseDictionary;
import com.folioreader.model.dictionary.Wikipedia;
import com.folioreader.ui.adapter.EnglishDictionaryAdapter;
import com.folioreader.ui.adapter.VietnameseDictionaryAdapter;
import com.folioreader.ui.base.EnglishDictionaryCallBack;
import com.folioreader.ui.base.EnglishDictionaryTask;
import com.folioreader.ui.base.VietnameseDictionaryCallback;
import com.folioreader.ui.base.VietnameseDictionaryTask;
import com.folioreader.ui.base.WikipediaCallBack;
import com.folioreader.ui.base.WikipediaTask;
import com.folioreader.util.AppUtil;
import com.folioreader.util.UiUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * @author gautam chibde on 4/7/17.
 */

public class DictionaryFragment extends DialogFragment
        implements EnglishDictionaryCallBack, WikipediaCallBack, VietnameseDictionaryCallback {

    private static final String TAG = "DictionaryFragment";
    View view;
    private String word;
    private MediaPlayer mediaPlayer;
    private RecyclerView recyclerView_dictResults;
    private TextView noNetwork, dictionary, wikipedia, wikiWord, def;
    private ProgressBar progressBar;
    private Button googleSearch;
    private LinearLayout wikiLayout;
    private WebView wikiWebView;
    private EnglishDictionaryAdapter en_English_dictionaryAdapter;
    private VietnameseDictionaryAdapter vi_dictionaryAdapter;
    private VietnameseDictionaryTask vi_dictionary_task;
    private ImageView imageViewClose;

    private EnglishDictionaryTask en_dictionary_task;
    private WikipediaTask wiki_task;

    private Spinner dictionarySpinner;
    private String[] nationList_English = {"English", "Vietnamese"};
    private String[] nationList_VietNamese = {"Tiếng Anh", "Tiếng Việt"};
    private String[] nationList_Russian = {"Английский", "вьетнамский"};
    private String[] nationList_Portuguese = {"Inglês", "vietnamita"};
    private String[] nationList_Czech = {"Angličtina", "vietnamština"};


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
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
        view = inflater.inflate(R.layout.layout_dictionary, container);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpView();

    }

    private void setUpView() {
        dictionarySpinner = (Spinner) view.findViewById(R.id.spn_dictionary_language);
        ArrayAdapter<String> spinnerArrayAdapter;
        if (getDeviceLanguage().equals("vi")) {
            spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, nationList_VietNamese);
        } else if (getDeviceLanguage().equals("ru")) {
            spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, nationList_Russian);
        } else if (getDeviceLanguage().equals("pt")) {
            spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, nationList_Portuguese);
        } else if (getDeviceLanguage().equals("cs")) {
            spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, nationList_Czech);
        } else {
            spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, nationList_English);
        }

        dictionarySpinner.setAdapter(spinnerArrayAdapter);
        if (getDeviceLanguage().equals("vi")) {
            dictionarySpinner.setSelection(1);
        } else {
            dictionarySpinner.setSelection(0);
        }

        noNetwork = (TextView) view.findViewById(R.id.no_network);
        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        recyclerView_dictResults = (RecyclerView) view.findViewById(R.id.rv_dict_results);

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
                dictionarySpinner.setVisibility(View.VISIBLE);
                loadEnglishDictionary();
            }
        });

        wikipedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadWikipedia();
                dictionarySpinner.setVisibility(View.GONE);
            }
        });

        dictionarySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    loadVietnameseDictionary();
                } else {
                    loadEnglishDictionary();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

        imageViewClose = view.findViewById(R.id.btn_close);
        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        recyclerView_dictResults.setLayoutManager(new LinearLayoutManager(getActivity()));
        en_English_dictionaryAdapter = new EnglishDictionaryAdapter(getContext(), this);
        vi_dictionaryAdapter = new VietnameseDictionaryAdapter(getContext(), this);

        configureTheme(view);

    }

    private void configureTheme(View view) {

        Config config = AppUtil.getSavedConfig(getContext());
        assert config != null;
        assert getContext() != null;
        final int themeColor = config.getThemeColor();

        UiUtil.setColorIntToDrawable(themeColor, imageViewClose.getDrawable());
        LinearLayout layoutHeader = view.findViewById(R.id.layout_header);
        layoutHeader.setBackgroundDrawable(UiUtil.getShapeDrawable(themeColor));
        UiUtil.setColorIntToDrawable(themeColor, progressBar.getIndeterminateDrawable());
        UiUtil.setShapeColor(googleSearch, themeColor);

        if (config.isNightMode()) {
            view.findViewById(R.id.toolbar).setBackgroundColor(Color.BLACK);
            view.findViewById(R.id.contentView).setBackgroundColor(Color.BLACK);
            dictionary.setBackgroundDrawable(UiUtil.createStateDrawable(themeColor, Color.BLACK));
            wikipedia.setBackgroundDrawable(UiUtil.createStateDrawable(themeColor, Color.BLACK));
            dictionary.setTextColor(UiUtil.getColorList(Color.BLACK, themeColor));
            wikipedia.setTextColor(UiUtil.getColorList(Color.BLACK, themeColor));
            int nightTextColor = ContextCompat.getColor(getContext(), R.color.night_text_color);
            wikiWord.setTextColor(nightTextColor);
            wikiWord.setBackgroundColor(Color.BLACK);
            def.setTextColor(nightTextColor);
            def.setBackgroundColor(Color.BLACK);
            noNetwork.setTextColor(nightTextColor);

        } else {
            view.findViewById(R.id.contentView).setBackgroundColor(Color.WHITE);
            dictionary.setTextColor(UiUtil.getColorList(Color.WHITE, themeColor));
            wikipedia.setTextColor(UiUtil.getColorList(Color.WHITE, themeColor));
            dictionary.setBackgroundDrawable(UiUtil.createStateDrawable(themeColor, Color.WHITE));
            wikipedia.setBackgroundDrawable(UiUtil.createStateDrawable(themeColor, Color.WHITE));
            wikiWord.setBackgroundColor(Color.WHITE);
            def.setBackgroundColor(Color.WHITE);
            googleSearch.setTextColor(Color.WHITE);
        }
    }

    private void loadEnglishDictionary() {
        if (noNetwork.getVisibility() == View.VISIBLE || googleSearch.getVisibility() == View.VISIBLE) {
            noNetwork.setVisibility(View.GONE);
            googleSearch.setVisibility(View.GONE);
        }
        wikiWebView.loadUrl("about:blank");
        en_English_dictionaryAdapter.clear();
        dictionary.setSelected(true);
        wikipedia.setSelected(false);
        wikiLayout.setVisibility(View.GONE);
        recyclerView_dictResults.setVisibility(View.VISIBLE);
        en_dictionary_task = new EnglishDictionaryTask(this);
        String urlString = null;
        try {
            urlString = Constants.DICTIONARY_BASE_URL + URLEncoder.encode(word, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "-> loadDictionary", e);
        }
        en_dictionary_task.execute(urlString);
    }

    private void loadVietnameseDictionary() {
        if (noNetwork.getVisibility() == View.VISIBLE || googleSearch.getVisibility() == View.VISIBLE) {
            noNetwork.setVisibility(View.GONE);
            googleSearch.setVisibility(View.GONE);
        }
        // code here
        en_English_dictionaryAdapter.clear();
        dictionary.setSelected(true);
        wikipedia.setSelected(false);
        recyclerView_dictResults.setVisibility(View.GONE);
        recyclerView_dictResults.setVisibility(View.VISIBLE);

        if (wikiLayout.getVisibility() == View.VISIBLE)
            wikiLayout.setVisibility(View.GONE);
        recyclerView_dictResults.setVisibility(View.VISIBLE);
        vi_dictionary_task = new VietnameseDictionaryTask(this);
        vi_dictionary_task.execute(word);
    }

    private void loadWikipedia() {
        if (noNetwork.getVisibility() == View.VISIBLE || googleSearch.getVisibility() == View.VISIBLE) {
            noNetwork.setVisibility(View.GONE);
            googleSearch.setVisibility(View.GONE);
        }
        wikiWebView.loadUrl("about:blank");
        en_English_dictionaryAdapter.clear();
        wikiLayout.setVisibility(View.VISIBLE);
        recyclerView_dictResults.setVisibility(View.GONE);
        dictionary.setSelected(false);
        wikipedia.setSelected(true);
        wiki_task = new WikipediaTask(this);
        String urlString = null;
        try {
            urlString = Constants.WIKIPEDIA_API_URL + URLEncoder.encode(word, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "-> loadWikipedia", e);
        }
        wiki_task.execute(urlString);
    }

    @Override
    public void onError() {
        noNetwork.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        noNetwork.setText(R.string.offline);
        googleSearch.setVisibility(View.GONE);
    }


    @Override
    public void onVietnameseDictionaryDataReceived(VietnameseDictionary vietnameseDictionary) {
        progressBar.setVisibility(View.GONE);
        if (vietnameseDictionary.getResultsList().isEmpty()) {
            noNetwork.setVisibility(View.VISIBLE);
            googleSearch.setVisibility(View.VISIBLE);
            noNetwork.setText(R.string.word_not_found);
        } else {
            vi_dictionaryAdapter.setResultList(vietnameseDictionary.getResultsList());
            recyclerView_dictResults.setAdapter(vi_dictionaryAdapter);
        }
    }

    @Override
    public void onEnglishDictionaryDataReceived(EnglishDictionary englishDictionary){
        progressBar.setVisibility(View.GONE);
        if (englishDictionary.getResultsList().isEmpty()) {
            noNetwork.setVisibility(View.VISIBLE);
            googleSearch.setVisibility(View.VISIBLE);
            noNetwork.setText(R.string.word_not_found);
        } else {
            en_English_dictionaryAdapter.setResults(englishDictionary.getResultsList());
            recyclerView_dictResults.setAdapter(en_English_dictionaryAdapter);
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

    private String getDeviceLanguage() {
        Locale locale = getContext().getResources().getConfiguration().locale;
        //return locale; //return vi_VN
        return locale.getLanguage(); //return vi
    }


    @Override
    public Context getContext_() {
        return getContext();
    }
}
