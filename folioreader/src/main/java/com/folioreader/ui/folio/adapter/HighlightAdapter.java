package com.folioreader.ui.folio.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.model.Highlight;
import com.folioreader.util.AppUtil;
import com.folioreader.util.UiUtil;
import com.folioreader.view.UnderlinedTextView;

import java.util.List;

/**
 * @author gautam chibde on 16/6/17.
 */

public class HighlightAdapter extends RecyclerView.Adapter<HighlightAdapter.HighlightHolder> {
    private List<Highlight> highlights;
    private HighLightAdapterCallback callback;
    private Context context;

    public HighlightAdapter(Context context, List<Highlight> highlights, HighLightAdapterCallback callback) {
        this.context = context;
        this.highlights = highlights;
        this.callback = callback;
    }

    @Override
    public HighlightHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HighlightHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_highlight, parent, false));
    }

    @Override
    public void onBindViewHolder(final HighlightHolder holder, final int position) {
        holder.content.setText(Html.fromHtml(getItem(position).getContent()));
        UiUtil.setBackColorToTextView(holder.content,
                getItem(position).getType());
        holder.date.setText(AppUtil.formatDate(getItem(position).getDate()));
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onItemClick(getItem(position));
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
                callback.editNote(getItem(position), position);
            }
        });
        if (getItem(position).getNote() != null) {
            if (getItem(position).getNote().isEmpty()) {
                holder.note.setVisibility(View.GONE);
            } else {
                holder.note.setVisibility(View.VISIBLE);
                holder.note.setText(getItem(position).getNote());
            }
        } else {
            holder.note.setVisibility(View.GONE);
        }
        holder.container.postDelayed(new Runnable() {
            @Override
            public void run() {
                final int height = holder.container.getHeight();
                ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewGroup.LayoutParams params =
                                holder.swipeLinearLayout.getLayoutParams();
                        params.height = height;
                        holder.swipeLinearLayout.setLayoutParams(params);
                    }
                });
            }
        }, 20);
        if (Config.getConfig().isNightMode()) {
            holder.container.setBackgroundColor(ContextCompat.getColor(context,
                    R.color.black));
            holder.note.setTextColor(ContextCompat.getColor(context,
                    R.color.white));
            holder.date.setTextColor(ContextCompat.getColor(context,
                    R.color.white));
        } else {
            holder.container.setBackgroundColor(ContextCompat.getColor(context,
                    R.color.white));
            holder.note.setTextColor(ContextCompat.getColor(context,
                    R.color.black));
            holder.date.setTextColor(ContextCompat.getColor(context,
                    R.color.black));
        }
    }

    private Highlight getItem(int position) {
        return highlights.get(position);
    }

    @Override
    public int getItemCount() {
        return highlights.size();
    }

    public void editNote(String note, int position) {
        highlights.get(position).setNote(note);
        notifyItemChanged(position);
    }

    static class HighlightHolder extends RecyclerView.ViewHolder {
        private UnderlinedTextView content;
        private ImageView delete, editNote;
        private TextView date;
        private RelativeLayout container;
        private TextView note;
        private LinearLayout swipeLinearLayout;

        HighlightHolder(View itemView) {
            super(itemView);
            container = (RelativeLayout) itemView.findViewById(R.id.container);
            swipeLinearLayout = (LinearLayout) itemView.findViewById(R.id.swipe_linear_layout);
            content = (UnderlinedTextView) itemView.findViewById(R.id.utv_highlight_content);
            delete = (ImageView) itemView.findViewById(R.id.iv_delete);
            editNote = (ImageView) itemView.findViewById(R.id.iv_edit_note);
            date = (TextView) itemView.findViewById(R.id.tv_highlight_date);
            note = (TextView) itemView.findViewById(R.id.tv_note);
        }
    }

    public interface HighLightAdapterCallback {
        void onItemClick(Highlight highlight);

        void deleteHighlight(String id);

        void editNote(Highlight highlight, int position);
    }
}
