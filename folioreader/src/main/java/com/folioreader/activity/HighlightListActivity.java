package com.folioreader.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.folioreader.R;
import com.folioreader.database.HighlightTable;
import com.folioreader.model.Highlight;
import com.folioreader.util.AppUtil;
import com.folioreader.util.UnderlinedTextView;
import com.folioreader.view.StyleableTextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class HighlightListActivity extends AppCompatActivity {
    private static final String HIGHLIGHT_ITEM ="highlight_item" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highlight_list);
        initViews();
    }

    private void initViews() {
        if(getSupportActionBar()!=null){
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
        ArrayList<Highlight> highlightArrayList= (ArrayList<Highlight>) HighlightTable.getAllRecords(HighlightListActivity.this);
        HightlightAdpater hightlightAdpater=new HightlightAdpater(HighlightListActivity.this,0,highlightArrayList);
        ListView highlightListview= (ListView) findViewById(R.id.list_highligts);
        highlightListview.setAdapter(hightlightAdpater);
    }


    private class HightlightAdpater extends ArrayAdapter<Highlight> {
        private LayoutInflater mInflater;
        private class ViewHolder {
           public UnderlinedTextView txtHightlightText;
            public TextView txtHightLightTime;

            public ViewHolder(View row){
                txtHightlightText = (UnderlinedTextView) row.findViewById(R.id.txt_hightlight_text);
                txtHightLightTime = (TextView) row.findViewById(R.id.txt_hightlight_time);
            }
        }

        public HightlightAdpater(Context context, int textViewResourceId, ArrayList<Highlight> objects) {
            super(context, textViewResourceId, objects);
            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder = null;
            if(row == null){
                row = mInflater.inflate(R.layout.row_highlight, null);
                holder = new ViewHolder(row);
                row.setTag(holder);
            }
            else{
                holder = (ViewHolder)row.getTag();
            }
            final Highlight rowItem = getItem(position);
            holder.txtHightlightText.setText(rowItem.getContent().trim());
            holder.txtHightLightTime.setText(AppUtil.formatDate(rowItem.getDate()));
            AppUtil.setBackColorToTextView(holder.txtHightlightText,rowItem.getType());
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent();
                    intent.putExtra(HIGHLIGHT_ITEM,rowItem);
                    setResult(RESULT_OK,intent);
                    finish();
                }
            });
            return row;
        }
    }
}
