package com.folioreader.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.model.HighlightImpl;
import com.folioreader.ui.view.UnderlinedTextView;
import com.folioreader.util.AppUtil;
import com.folioreader.util.UiUtil;

import java.util.List;

/**
 * @author gautam chibde on 16/6/17.
 */

public class HighlightAdapter extends RecyclerView.Adapter<HighlightAdapter.HighlightHolder> {
    private List<HighlightImpl> highlights;
    private HighLightAdapterCallback callback;
    private Context context;
    private Config config;

    public HighlightAdapter(Context context, List<HighlightImpl> highlights, HighLightAdapterCallback callback, Config config) {
        this.context = context;
        this.highlights = highlights;
        this.callback = callback;
        this.config = config;
    }

    @Override
    public HighlightHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HighlightHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_highlight, parent, false));
    }

    @Override
    public void onBindViewHolder(final HighlightHolder holder, final int position) {

        holder.container.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holder.container.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
                    }
                });
            }
        }, 10);

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
                callback.deleteHighlight(getItem(position).getId());
                highlights.remove(position);
                notifyDataSetChanged();

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
        }, 30);
        if (config.isNightMode()) {
            holder.container.setBackgroundColor(ContextCompat.getColor(context,
                    R.color.black));
            holder.note.setTextColor(ContextCompat.getColor(context,
                    R.color.white));
            holder.date.setTextColor(ContextCompat.getColor(context,
                    R.color.white));
            holder.content.setTextColor(ContextCompat.getColor(context,
                    R.color.white));
        } else {
            holder.container.setBackgroundColor(ContextCompat.getColor(context,
                    R.color.white));
            holder.note.setTextColor(ContextCompat.getColor(context,
                    R.color.black));
            holder.date.setTextColor(ContextCompat.getColor(context,
                    R.color.black));
            holder.content.setTextColor(ContextCompat.getColor(context,
                    R.color.black));
        }
    }

    private HighlightImpl getItem(int position) {
        return highlights.get(position);
    }

    @Override
    public int getItemCount() {
        return highlights.size();
    }

    public void editNote(String note, int position) {
        highlights.get(position).setNote(note);
        notifyDataSetChanged();
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
        void onItemClick(HighlightImpl highlightImpl);

        void deleteHighlight(int id);

        void editNote(HighlightImpl highlightImpl, int position);
    }
}
