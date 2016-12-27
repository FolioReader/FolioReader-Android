package com.folioreader.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.model.Highlight;
import com.folioreader.util.AppUtil;
import com.folioreader.util.UiUtil;
import com.folioreader.view.UnderlinedTextView;

import java.util.ArrayList;

public class HighlightListActivity extends AppCompatActivity {
    private static final String HIGHLIGHT_ITEM = "highlight_item";
    private static final String ITEM_DELETED = "item_deleted";

    //private static final int REQUEST_CODE = 1;
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
                Intent intent = new Intent();
                intent.putExtra(ITEM_DELETED, true);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        initList();
    }

    private void initList() {
        if (Config.getConfig().isNightMode()) {
            ((RelativeLayout) findViewById(R.id.main))
                    .setBackgroundColor(ContextCompat.getColor(HighlightListActivity.this,
                            R.color.black));
            ((TextView) findViewById(R.id.lbl_center))
                    .setTextColor(ContextCompat.getColor(HighlightListActivity.this,
                            R.color.white));
            ((Toolbar) findViewById(R.id.toolbar))
                    .setBackgroundColor(ContextCompat.getColor(HighlightListActivity.this,
                            R.color.black));
            ((View) findViewById(R.id.view))
                    .setBackgroundColor(ContextCompat.getColor(HighlightListActivity.this,
                            R.color.white));
            ((ListView) findViewById(R.id.list_highligts))
                    .setDivider(new ColorDrawable(ContextCompat.
                            getColor(HighlightListActivity.this, R.color.white)));
            ((ListView) findViewById(R.id.list_highligts)).setDividerHeight(1);
        }

       /* HightlightAdpater hightlightAdpater =
                new HightlightAdpater(HighlightListActivity.this, 0,
                        (ArrayList<Highlight>) HighlightTable
                                .getAllRecords(HighlightListActivity.this));
        ListView highlightListview = (ListView) findViewById(R.id.list_highligts);
        highlightListview.setAdapter(hightlightAdpater);*/
    }


    private class HightlightAdpater extends ArrayAdapter<Highlight> {
        private LayoutInflater mInflater;

        private class ViewHolder {
            public UnderlinedTextView txtHightlightText;
            public TextView txtHightLightTime;
            public TextView txtHightLightNote;
            public ImageView delete;
            public ImageView editNote;
            public RelativeLayout dataRelativeLayout;
            public LinearLayout swipeLinearlayout;

            public ViewHolder(View row) {
                txtHightlightText = (UnderlinedTextView) row.findViewById(R.id.txt_hightlight_text);
                txtHightLightTime = (TextView) row.findViewById(R.id.txt_hightlight_time);
                txtHightLightNote = (TextView) row.findViewById(R.id.txt_hightlight_note);
                delete = (ImageView) row.findViewById(R.id.delete);
                editNote = (ImageView) row.findViewById(R.id.edit_note);
                dataRelativeLayout = (RelativeLayout) row.findViewById(R.id.main_data);
                swipeLinearlayout = (LinearLayout) row.findViewById(R.id.swipe_linear_layout);
            }
        }

        public HightlightAdpater(Context context,
                                 int textViewResourceId, ArrayList<Highlight> objects) {
            super(context, textViewResourceId, objects);
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
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
            final ViewHolder holder1 = holder;
            holder.dataRelativeLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final int height = holder1.dataRelativeLayout.getHeight();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewGroup.LayoutParams params =
                                    holder1.swipeLinearlayout.getLayoutParams();
                            params.height = height;
                            holder1.swipeLinearlayout.setLayoutParams(params);
                        }
                    });
                }
            }, 20);


            String editedNote = rowItem.getNote();
            if (editedNote != null && editedNote.length() > 0) {
                holder.txtHightLightNote.setText(editedNote);
            }

            UiUtil.setBackColorToTextView(holder.txtHightlightText,
                    rowItem.getType());
            row.findViewById(R.id.txt_hightlight_text)
                        .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.putExtra(HIGHLIGHT_ITEM, rowItem);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                        });

            row.findViewById(R.id.txt_hightlight_note)
                    .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showEditNoteDailog(getItem(position));
                            }
                    });

            if (Config.getConfig().isNightMode()) {
                holder.txtHightlightText
                        .setTextColor(ContextCompat.getColor(HighlightListActivity.this,
                                R.color.white));
                holder.txtHightLightNote
                        .setTextColor(ContextCompat.getColor(HighlightListActivity.this,
                                R.color.white));
                holder.txtHightLightTime
                        .setTextColor(ContextCompat.getColor(HighlightListActivity.this,
                                R.color.white));
            }

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //HighlightTable.remove(rowItem.getHighlightId(), HighlightListActivity.this);
                    Intent intent = new Intent();
                    intent.putExtra(ITEM_DELETED, true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
            holder.editNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditNoteDailog(getItem(position));
                }
            });
            return row;
        }
    }


    private void showEditNoteDailog(final Highlight highlightItem) {
        final Dialog dailog = new Dialog(HighlightListActivity.this, R.style.DialogCustomTheme);
        dailog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dailog.setContentView(R.layout.dialog_edit_notes);
        dailog.show();
        String noteText = highlightItem.getNote();
        ((EditText) dailog.findViewById(R.id.edit_note)).setText(noteText);

        dailog.findViewById(R.id.btn_save_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String note =
                        ((EditText) dailog.findViewById(R.id.edit_note)).getText().toString();
                if (note != null && (!TextUtils.isEmpty(note))) {
                    highlightItem.setNote(note);
                    //HighlightTable.save(getApplicationContext(), highlightItem);
                    dailog.dismiss();
                    initViews();
                } else {
                    Toast.makeText(HighlightListActivity.this,
                            getString(R.string.please_enter_note),
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }


}
