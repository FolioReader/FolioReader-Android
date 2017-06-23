package com.folioreader.ui.folio.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.model.Highlight;
import com.folioreader.model.sqlite.HighLightTable;
import com.folioreader.ui.folio.activity.FolioActivity;
import com.folioreader.ui.folio.adapter.HightlightAdapter;

public class HighlightFragment extends Fragment implements HightlightAdapter.HighLightAdapterCallback {
    private static final String HIGHLIGHT_ITEM = "highlight_item";
    private View mRootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_highlight_list, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView highlightsView = (RecyclerView) mRootView.findViewById(R.id.rv_highlights);
        if (Config.getConfig().isNightMode()) {
            mRootView.findViewById(R.id.main)
                    .setBackgroundColor(ContextCompat.getColor(getActivity(),
                            R.color.black));
        }

        highlightsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        HightlightAdapter adapter = new HightlightAdapter(getActivity(), HighLightTable.getAllHighlights(FolioActivity.EPUB_TITLE), this);
        highlightsView.setAdapter(adapter);
    }

    @Override
    public void activityForResults(Highlight highlight) {
        Intent intent = new Intent();
        intent.putExtra(HIGHLIGHT_ITEM, highlight);
        intent.putExtra(Constants.TYPE, Constants.HIGHLIGHT_SELECTED);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void deleteHighlight(String id) {
        HighLightTable.deleteHighlight(id);
    }

    @Override
    public void editHighlight(Highlight highlight) {

    }
}


