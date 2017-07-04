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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.model.dictionary.Dictionary;
import com.folioreader.ui.base.DictionaryCallBack;
import com.folioreader.ui.base.DictionaryTask;
import com.folioreader.ui.folio.adapter.DictionaryAdapter;

import java.io.IOException;

/**
 * @author gautam chibde on 4/7/17.
 */

public class DictionaryFragment extends DialogFragment implements DictionaryCallBack {

    private String word;

    private MediaPlayer mediaPlayer;
    private RecyclerView dictResults;
    private TextView noNetwork, dictionary, wikipedia;
    private ProgressBar progressBar;
    private Button googleSearch;

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
        loadDictionary();
    }

    private void loadDictionary() {
        dictionary.setSelected(true);
        wikipedia.setSelected(false);
        DictionaryTask task = new DictionaryTask(this);
        String baseUrl = Constants.DICTIONARY_BASE_URL + word.trim();
        task.execute(baseUrl);
    }

    private void loadWikipedia() {
        dictionary.setSelected(false);
        wikipedia.setSelected(true);
    }

    @Override
    public void onError() {
        noNetwork.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDictionaryDataReceived(Dictionary dictionary) {
        progressBar.setVisibility(View.GONE);
        if (dictionary.getResults().isEmpty()) {
            noNetwork.setVisibility(View.VISIBLE);
            googleSearch.setVisibility(View.VISIBLE);
            noNetwork.setText("Word not found");
        } else {
            dictResults.setAdapter(new DictionaryAdapter(dictionary.getResults(), getActivity(), this));
        }
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
                e.printStackTrace();
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
