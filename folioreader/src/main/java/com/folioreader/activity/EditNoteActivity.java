package com.folioreader.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;

import com.folioreader.R;
import com.folioreader.database.HighlightTable;
import com.folioreader.model.Highlight;

public class EditNoteActivity extends AppCompatActivity {
    private static final String HIGHLIGHT_ITEM = "highlight_item";
    private EditText mEdit;
    private Highlight highlight_item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note);

        initViews();
    }

    private void initViews() {
        mEdit = (EditText)findViewById(R.id.edit_text_note);

        Intent intent = getIntent();
        highlight_item = intent.getParcelableExtra(HIGHLIGHT_ITEM);
        String currentNote = highlight_item.getNote();
        if(currentNote != null) {
            mEdit.setText(currentNote);
        }

        findViewById(R.id.save_note_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editedNote = mEdit.getText().toString();
                if(editedNote.length() > 0){
                    highlight_item.setNote(editedNote);
                    HighlightTable.save(getApplicationContext(), highlight_item);

                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                }

                finish();
            }
        });
    }
}
