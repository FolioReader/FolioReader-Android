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
