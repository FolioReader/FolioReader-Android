package com.folioreader.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.folioreader.R;
import com.folioreader.model.dictionary.VietnameseDictionaryResult;
import com.folioreader.ui.base.VietnameseDictionaryCallback;

import java.util.List;

public class VietnameseDictionaryAdapter extends RecyclerView.Adapter<VietnameseDictionaryAdapter.ViewHolder> {
    Context mContext;

    private List<VietnameseDictionaryResult> resultList;

    public VietnameseDictionaryAdapter(Context mContext, VietnameseDictionaryCallback callback) {
        this.mContext = mContext;
       // this.resultList = resultList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_dictionary, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VietnameseDictionaryResult result = resultList.get(position);
        holder.example.setText(result.getExample());
        holder.word.setText(result.getWord());
        holder.define.setText(result.getDefine());
    }

    public void setResultList(List<VietnameseDictionaryResult> resultList) {
        this.resultList = resultList;
    }

    public List<VietnameseDictionaryResult> getResultList() {
        return resultList;
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView word;
        private TextView define;
        private TextView example;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            word = itemView.findViewById(R.id.tv_word);
            define = itemView.findViewById(R.id.tv_definition);
            example = itemView.findViewById(R.id.tv_examples);
            //config view item
        }
    }

    public void clear() {
        resultList.clear();
        notifyItemRangeRemoved(0, resultList.size());
    }
}
