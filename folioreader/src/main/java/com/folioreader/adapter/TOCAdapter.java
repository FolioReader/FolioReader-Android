package com.folioreader.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.folioreader.R;

import java.util.List;

import nl.siegmann.epublib.domain.TOCReference;

/**
 * Created by Mahavir on 3/29/16.
 */
public class TOCAdapter extends RecyclerView.Adapter<TOCAdapter.ViewHolder> {
    private List<TOCReference> mTOCReferences;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tocTitleView;

        public ViewHolder(View v) {
            super(v);
            tocTitleView = (TextView) v.findViewById(R.id.chapter);
        }
    }

    public TOCAdapter(List<TOCReference> tocReferences) {
        mTOCReferences = tocReferences;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chapter, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tocTitleView.setText(mTOCReferences.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return mTOCReferences.size();
    }

}
