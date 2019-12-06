package com.folioreader.ui.fragment.HighlightNoteFragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.folioreader.R;

public class BookReadingHighlightNoteFrm extends Fragment implements HighlightNoteFrmContract.HighlightNoteView, View.OnClickListener {

    private HighlightNotePresenter presenter;
    // private Note note;
    private int color_index;
    private Context context;
    private View view;
    private ImageButton imgBtn_red;
    private ImageButton imgBtn_green;
    private ImageButton imgBtn_blue;
    private ImageButton imgBtn_yellow;
    private EditText editText_Note;
    private Button btn_DeleteNote;
    private Button btn_SaveNote;
    private Button btn_verify_delete_note;
    private Button btn_no_verify_detete_note;

    AlertDialog dialog;

    public BookReadingHighlightNoteFrm(Context context) {
        this.context = context;
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_highlight_note, container, false);
        color_index = 1;
        // note = new Note("", "", color_index);
        presenter = new HighlightNotePresenter(this);

        setUpView();
        return view;
    }

    private void setUpView() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
        View mView = getLayoutInflater().inflate(R.layout.fragment_verify_delete_note_frm, null);
        mBuilder.setView(mView);
        dialog = mBuilder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        btn_verify_delete_note = mView.findViewById(R.id.verify_delete_btn);
        btn_no_verify_detete_note = mView.findViewById(R.id.no_verify_delete_btn);

        imgBtn_green = view.findViewById(R.id.btn_highlight_frm_green);
        imgBtn_yellow = view.findViewById(R.id.btn_highlight_frm_yellow);
        imgBtn_red = view.findViewById(R.id.btn_highlight_frm_red);
        imgBtn_blue = view.findViewById(R.id.btn_highlight_frm_blue);
        editText_Note = view.findViewById(R.id.edittext_highlight_frm);
        btn_DeleteNote = view.findViewById(R.id.note_delete_btn);
        btn_SaveNote = view.findViewById(R.id.note_save_btn);
        btn_SaveNote.setOnClickListener(this);
        btn_DeleteNote.setOnClickListener(this);
        btn_verify_delete_note.setOnClickListener(this);
        btn_no_verify_detete_note.setOnClickListener(this);

        presenter.showNote();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.note_save_btn) {
            //  presenter.saveNote(note, color_index, editText_Note.getText().toString());
        } else if (view.getId() == R.id.note_delete_btn) {
            presenter.negativeToVerifyFrm();
        } else if (view.getId() == R.id.verify_delete_btn) {
            //   presenter.deleteNote(note);
        } else if (view.getId() == R.id.btn_highlight_frm_red) {
            color_index = 1;
        } else if (view.getId() == R.id.btn_highlight_frm_yellow) {
            color_index = 2;
        } else if (view.getId() == R.id.btn_highlight_frm_blue) {
            color_index = 3;

        } else if (view.getId() == R.id.btn_highlight_frm_green) {
            color_index = 4;

        }
    }

    @Override
    public void killDialogOnView() {
        dialog.dismiss();
    }

//  @Override
//  public void showNoteToView(Note note) {
//    editText_Note.setText(note.getText());
//   }

    @Override
    public void negativeToVerifyFrm() {
        dialog.show();
    }

    //  @Override
    // public void deleteNoteOnView(Note note) {
    // delete highlight in text
//}

}
