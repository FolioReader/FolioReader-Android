package com.folioreader.adapter;

import com.folioreader.R;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import nl.siegmann.epublib.domain.TOCReference;

/**
 * Created by Mahavir on 3/29/16.
 */
public class TOCAdapter extends RecyclerView.Adapter<TOCAdapter.ViewHolder> {
    private List<TOCReference> mTOCReferences;
    private boolean isNightMode;
    private int selectedChapterPosition;
    private ChapterSelectionCallBack chapterSelectionCallBack;
    private Context context;

    public interface ChapterSelectionCallBack {
        public void onChapterSelect(int position);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tocTitleView;

        public ViewHolder(View v) {
            super(v);
            tocTitleView = (TextView) v.findViewById(R.id.chapter);
        }
    }

    public TOCAdapter(List<TOCReference> tocReferences, Context context) {
        mTOCReferences = tocReferences;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chapter, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.tocTitleView.setText(mTOCReferences.get(position).getTitle());
        if (!(selectedChapterPosition == position)) {
            if (isNightMode) {
                holder.tocTitleView.setTextColor(Color.WHITE);
            } else {
                holder.tocTitleView.setTextColor(Color.BLACK);
            }
        } else {
            holder.tocTitleView.setTextColor(Color.GREEN);
        }

        holder.tocTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chapterSelectionCallBack = ((ChapterSelectionCallBack) context);
                chapterSelectionCallBack.onChapterSelect(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTOCReferences.size();
    }

    public void setNightMode(boolean nightMode) {
        isNightMode = nightMode;
    }

    public void setSelectedChapterPosition(int position) {
        selectedChapterPosition = position;
    }

}
