package com.folioreader.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.adapter.TOCAdapter;

import java.util.ArrayList;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.TOCReference;


public class ContentsFragment extends Fragment {
    private View mRootView;
    private Context mContext;


    public static ContentsFragment newInstance(Book book,int selectedChapterPosition) {
        ContentsFragment contentsFragment = new ContentsFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BOOK, book);
        args.putInt(Constants.SELECTED_CHAPTER_POSITION,selectedChapterPosition);
        contentsFragment.setArguments(args);
        return contentsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView=inflater.inflate(R.layout.fragment_contents, container, false);
        mContext=getActivity();
        configRecyclerViews();
        return mRootView;
    }


    public void configRecyclerViews() {
        Book book= (Book) getArguments().getSerializable(com.folioreader.Constants.BOOK);
        int selectedChapterPosition=getArguments().getInt(Constants.SELECTED_CHAPTER_POSITION);
        ArrayList<TOCReference> tocReferences = (ArrayList<TOCReference>) book.getTableOfContents().getTocReferences();
        RecyclerView recyclerView=(RecyclerView) mRootView.findViewById(R.id.recycler_view_menu);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        if (tocReferences != null) {
            TOCAdapter tocAdapter = new TOCAdapter(tocReferences, mContext,selectedChapterPosition);
            recyclerView.setAdapter(tocAdapter);
        }
    }
}