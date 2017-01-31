package com.folioreader.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.util.AppUtil;

import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;

import static com.folioreader.Constants.CHAPTER_SELECTED;
import static com.folioreader.Constants.SELECTED_CHAPTER_POSITION;
import static com.folioreader.Constants.TYPE;


public class ContentsFragment extends Fragment {
    private static String BOOK_PATH = "book_path";
    private View mRootView;
    private Context mContext;
    private List<TOCReference> mTocReferences;
    private List<SpineReference> mSpineReferences;
    private int mSelectedChapterPosition;
    private boolean mIsNightMode;

    public static ContentsFragment newInstance(String bookPath, int selectedChapterPosition) {
        ContentsFragment contentsFragment = new ContentsFragment();
        Bundle args = new Bundle();
        args.putString(BOOK_PATH, bookPath);
        args.putInt(SELECTED_CHAPTER_POSITION, selectedChapterPosition);
        contentsFragment.setArguments(args);
        return contentsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_contents, container, false);
        mContext = getActivity();
        mIsNightMode = Config.getConfig().isNightMode();
        if (mIsNightMode) {
            mRootView.findViewById(R.id.recycler_view_menu).
                    setBackgroundColor(ContextCompat.getColor(mContext,
                            R.color.black));
        }
        configRecyclerViews();
        return mRootView;
    }


    public void configRecyclerViews() {
        String bookPath = getArguments().getString(BOOK_PATH);

        if (!TextUtils.isEmpty(bookPath)) {
            Book book = AppUtil.saveBookToDb(bookPath);
            mSelectedChapterPosition
                    = getArguments().getInt(SELECTED_CHAPTER_POSITION);
            mTocReferences = book.getTableOfContents().getTocReferences();
            mSpineReferences
                    = book.getSpine().getSpineReferences();
            RecyclerView recyclerView
                    = (RecyclerView) mRootView.findViewById(R.id.recycler_view_menu);
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            if (mTocReferences != null) {
                TOCAdapter tocAdapter = new TOCAdapter(mTocReferences, mSelectedChapterPosition);
                recyclerView.setAdapter(tocAdapter);
            }
        }
    }


    public class TOCAdapter extends RecyclerView.Adapter<TOCAdapter.ViewHolder> {
        private List<TOCReference> mTOCReferences;
        private int mSelectedChapterPosition;


        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView tocTitleView;
            public View line;

            public ViewHolder(View v) {
                super(v);
                tocTitleView = (TextView) v.findViewById(R.id.chapter);
                line = v.findViewById(R.id.line1);
            }
        }

        public TOCAdapter(List<TOCReference> tocReferences, int selectedChapterPosition) {
            mTOCReferences = tocReferences;
            mSelectedChapterPosition = selectedChapterPosition;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_chapter, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.tocTitleView.setText(mTOCReferences.get(position).getTitle());

            if(mSelectedChapterPosition!=-1) {
                if (!(mSelectedChapterPosition == position)) {
                    if (mIsNightMode) {
                        holder.tocTitleView.setTextColor(Color.WHITE);
                        holder.line.setBackgroundColor(Color.WHITE);
                    } else {
                        holder.tocTitleView.setTextColor(Color.BLACK);
                        holder.line.setBackgroundColor(Color.BLACK);
                    }
                } else {
                    holder.tocTitleView.setTextColor(Color.GREEN);
                    if (mIsNightMode) {
                        holder.line.setBackgroundColor(Color.WHITE);
                    }
                }
            }

            holder.tocTitleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = mSpineReferences.get(position).getResource().getId();
                    for (int i = 0; i < mSpineReferences.size(); i++) {
                        if (mSpineReferences.get(i).getResource().getId().equals(title)) {
                            mSelectedChapterPosition = i;
                            Intent intent = new Intent();
                            intent.putExtra(SELECTED_CHAPTER_POSITION, mSelectedChapterPosition);
                            intent.putExtra(TYPE, CHAPTER_SELECTED);
                            getActivity().setResult(Activity.RESULT_OK, intent);
                            getActivity().finish();
                            return;
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mTOCReferences.size();
        }
    }

}