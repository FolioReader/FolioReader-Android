package com.folioreader.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.folioreader.R;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import nl.siegmann.epublib.domain.SpineReference;

/**
 * Created by mobisys2 on 3/30/2016.
 */
public class SpineReferenceAdapter extends RecyclerView.Adapter<SpineReferenceAdapter.ViewHolder> {
    List<SpineReference> mSpineReferences;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public WebView spineContentView;

        public ViewHolder(View v) {
            super(v);
            spineContentView = (WebView) v.findViewById(R.id.content);
        }
    }

    public SpineReferenceAdapter(List<SpineReference> spineReferences) {
        mSpineReferences = spineReferences;
    }

    public String reader(int position) {
        Reader reader = null;
        try {
            reader = mSpineReferences.get(position).getResource().getReader();

            StringBuilder builder = new StringBuilder();
            int numChars;
            char[] cbuf = new char[2048];
            while ((numChars = reader.read(cbuf)) >= 0) {
                builder.append(cbuf, 0, numChars);
            }
            String content = builder.toString();
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_content, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.spineContentView.loadData(reader(position), "text/html; charset=UTF-8", "UTF-8");
    }

    @Override
    public int getItemCount() {
        return mSpineReferences.size();
    }

}
