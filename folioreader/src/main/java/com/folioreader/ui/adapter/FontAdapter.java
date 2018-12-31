/*
 * Copyright (C) 2016 Pedro Paulo de Amorim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.folioreader.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.folioreader.Font;
import com.folioreader.R;

import java.util.ArrayList;

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.ViewHolder> {

    private ArrayList<Font> mFonts = null;

    @Override
    public FontAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_font, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.mName.setText(mFonts.get(i).getName());
    }

    @Override
    public int getItemCount() {
        return mFonts != null ? mFonts.size() : 0;
    }

    public void setFonts(ArrayList<Font> mFonts) {
        this.mFonts = mFonts;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mName;

        public ViewHolder(View v) {
            super(v);
            mName = (TextView) v.findViewById(R.id.name);
        }
    }
}
