package com.folioreader.ui.folio.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.folioreader.R;
import com.folioreader.model.Highlight;
import com.folioreader.util.AppUtil;
import com.folioreader.view.UnderlinedTextView;

import java.util.List;

/**
 * @author gautam chibde on 16/6/17.
 */

public class HightlightAdapter extends RecyclerView.Adapter<HightlightAdapter.HighlightHolder> {
    private List<Highlight> highlights;
    private HighLightAdapterCallback callback;

    public HightlightAdapter(List<Highlight> highlights, HighLightAdapterCallback callback) {
        this.highlights = highlights;
        this.callback = callback;
    }

    @Override
    public HighlightHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HighlightHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_highlight, parent, false));
    }

    @Override
    public void onBindViewHolder(HighlightHolder holder, final int position) {
        holder.content.setText(getItem(position).getContent());
        holder.date.setText(AppUtil.formatDate(getItem(position).getDate()));
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.activityForResults(getItem(position));
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.deleteHighlight(getItem(position).getHighlightId());
                highlights.remove(position);
                notifyItemRemoved(position);
            }
        });
        holder.editNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.editHighlight(getItem(position));
            }
        });
    }

    private Highlight getItem(int position) {
        return highlights.get(position);
    }

    @Override
    public int getItemCount() {
        return highlights.size();
    }

    static class HighlightHolder extends RecyclerView.ViewHolder {
        public UnderlinedTextView content;
        public ImageView delete;
        public ImageView editNote;
        TextView date;
        LinearLayout container;

        public HighlightHolder(View itemView) {
            super(itemView);
            container = (LinearLayout) itemView.findViewById(R.id.container);
            content = (UnderlinedTextView) itemView.findViewById(R.id.utv_highlight_content);
            delete = (ImageView) itemView.findViewById(R.id.iv_delete);
            editNote = (ImageView) itemView.findViewById(R.id.iv_edit_note);
            date = (TextView) itemView.findViewById(R.id.tv_highlight_date);
        }
    }

    public interface HighLightAdapterCallback {
        void activityForResults(Highlight highlight);

        void deleteHighlight(String id);

        void editHighlight(Highlight highlight);
    }
}
