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
package com.folioreader.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.folioreader.Font;
import com.folioreader.R;
import java.util.ArrayList;

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.ViewHolder> {

  private ArrayList<Font> fonts = null;

  @Override public FontAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
    return new ViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.adapter_font, parent, false));
  }

  @Override public void onBindViewHolder(ViewHolder viewHolder, int i) {
    viewHolder.name.setText(fonts.get(i).getName());
  }

  @Override public int getItemCount() {
    return fonts != null ? fonts.size() : 0;
  }

  public void setFonts(ArrayList<Font> fonts) {
    this.fonts = fonts;
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    private TextView name;

    public ViewHolder(View v) {
      super(v);
      name = (TextView) v.findViewById(R.id.name);
    }
  }

}
