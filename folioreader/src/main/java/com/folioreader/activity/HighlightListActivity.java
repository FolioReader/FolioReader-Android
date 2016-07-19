package com.folioreader.activity;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.database.HighlightTable;
import com.folioreader.model.Highlight;
import com.folioreader.util.AppUtil;
import com.folioreader.util.UnderlinedTextView;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class HighlightListActivity extends AppCompatActivity {
    private static final String HIGHLIGHT_ITEM = "highlight_item";
    private static final String ITEM_DELETED = "item_deleted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highlight_list);
        initViews();
    }

    private void initViews() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initList();
    }

    private void initList() {
        if (Config.getConfig().isNightMode()) {
            ((RelativeLayout) findViewById(R.id.main)).setBackgroundColor(ContextCompat.getColor(HighlightListActivity.this, R.color.black));
            ((TextView) findViewById(R.id.lbl_center)).setTextColor(ContextCompat.getColor(HighlightListActivity.this, R.color.white));
            ((Toolbar) findViewById(R.id.toolbar)).setBackgroundColor(ContextCompat.getColor(HighlightListActivity.this, R.color.black));
            ((View) findViewById(R.id.view)).setBackgroundColor(ContextCompat.getColor(HighlightListActivity.this, R.color.white));
            ((ListView) findViewById(R.id.list_highligts)).setDivider(new ColorDrawable(ContextCompat.getColor(HighlightListActivity.this, R.color.white)));
            ((ListView) findViewById(R.id.list_highligts)).setDividerHeight(1);
        }
        ArrayList<Highlight> highlightArrayList = (ArrayList<Highlight>) HighlightTable.getAllRecords(HighlightListActivity.this);
        HightlightAdpater hightlightAdpater = new HightlightAdpater(HighlightListActivity.this, 0, highlightArrayList);
        ListView highlightListview = (ListView) findViewById(R.id.list_highligts);
        highlightListview.setAdapter(hightlightAdpater);
    }


    private class HightlightAdpater extends ArrayAdapter<Highlight> {
        private LayoutInflater mInflater;

        private class ViewHolder {
            public UnderlinedTextView txtHightlightText;
            public TextView txtHightLightTime;
            public ImageView delete;

            public ViewHolder(View row) {
                txtHightlightText = (UnderlinedTextView) row.findViewById(R.id.txt_hightlight_text);
                txtHightLightTime = (TextView) row.findViewById(R.id.txt_hightlight_time);
                delete = (ImageView) row.findViewById(R.id.delete);
            }
        }

        public HightlightAdpater(Context context, int textViewResourceId, ArrayList<Highlight> objects) {
            super(context, textViewResourceId, objects);
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder = null;
            if (row == null) {
                row = mInflater.inflate(R.layout.row_highlight, null);
                holder = new ViewHolder(row);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            final Highlight rowItem = getItem(position);
            holder.txtHightlightText.setText(rowItem.getContent().trim());
            holder.txtHightLightTime.setText(AppUtil.formatDate(rowItem.getDate()));
            AppUtil.setBackColorToTextView(holder.txtHightlightText, rowItem.getType());
            row.findViewById(R.id.main_data).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra(HIGHLIGHT_ITEM, rowItem);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });

            if (Config.getConfig().isNightMode()) {
                holder.txtHightlightText.setTextColor(ContextCompat.getColor(HighlightListActivity.this, R.color.white));
                holder.txtHightLightTime.setTextColor(ContextCompat.getColor(HighlightListActivity.this, R.color.white));
            }

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HighlightTable.remove(rowItem.getHighlightId(), HighlightListActivity.this);
                    Intent intent = new Intent();
                    intent.putExtra(ITEM_DELETED, true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
            return row;
        }
    }
}
