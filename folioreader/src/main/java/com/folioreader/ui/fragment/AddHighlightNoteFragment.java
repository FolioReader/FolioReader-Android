package com.folioreader.ui.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.folioreader.R;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.event.MediaOverlayHighlightStyleEvent;

public class AddHighlightNoteFragment extends DialogFragment {
    private View view;
    private MediaOverlayHighlightStyleEvent.Style style;
    private String selectedText;
    private String note;

    public AddHighlightNoteFragment(String selectedText) {
        this.selectedText = selectedText;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_edit_notes, container, false);
        setUpView();
        return view;
    }

    private void setUpView() {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button btn_SaveNote = view.findViewById(R.id.btn_save_note);
        Button btn_DeleteNote = view.findViewById(R.id.btn_delete_note);
        ImageButton btn_EditNote_Red = view.findViewById(R.id.btn_edit_note_red);
        ImageButton btn_EditNote_Orange = view.findViewById(R.id.btn_edit_note_orange);
        ImageButton btn_EditNote_Blue = view.findViewById(R.id.btn_edit_note_blue);
        ImageButton btn_EditNote_Green = view.findViewById(R.id.btn_edit_note_green);
        //HighlightImpl highlightImpl =
    }

//    public void editNote(final HighlightImpl highlightImpl, final int position) {
//        // Update here
//        final Dialog dialog = new Dialog(getActivity(), R.style.DialogCustomTheme);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.dialog_edit_notes);
//
//        if (highlightImpl.getTmpColorLabel().equals("red")) {
//            ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_red_blur);
//        }
//        if (highlightImpl.getTmpColorLabel().equals("orange")) {
//            ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_orange_blur);
//        }
//        if (highlightImpl.getTmpColorLabel().equals("blue")) {
//            ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_blue_blur);
//        }
//        if (highlightImpl.getTmpColorLabel().equals("green")) {
//            ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_green_blur);
//        }
//
//        dialog.show();
//
//        String noteText = highlightImpl.getNote();
//        ((EditText) dialog.findViewById(R.id.edit_note)).setText(noteText);
//
//        Button btn_DeleteNote = dialog.findViewById(R.id.btn_delete_note);
//        ImageButton btn_EditNote_Red = dialog.findViewById(R.id.btn_edit_note_red);
//        ImageButton btn_EditNote_Orange = dialog.findViewById(R.id.btn_edit_note_orange);
//        ImageButton btn_EditNote_Blue = dialog.findViewById(R.id.btn_edit_note_blue);
//        ImageButton btn_EditNote_Green = dialog.findViewById(R.id.btn_edit_note_green);
//
//        dialog.findViewById(R.id.btn_save_note).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String note =
//                        ((EditText) dialog.findViewById(R.id.edit_note)).getText().toString();
//                if (!TextUtils.isEmpty(note)) {
//                    highlightImpl.setNote(note);
//                    if (HighLightTable.updateHighlight(highlightImpl)) {
//                        HighlightUtil.sendHighlightBroadcastEvent(
//                                HighlightFragment.this.getActivity().getApplicationContext(),
//                                highlightImpl,
//                                HighLight.HighLightAction.MODIFY);
//                        adapter.editNote(note, position);
//                    }
//                    dialog.dismiss();
//                } else {
//                    Toast.makeText(getActivity(),
//                            getString(R.string.please_enter_note),
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        btn_DeleteNote.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                adapter.deleteNote(position);
//                Toast.makeText(getActivity(), R.string.you_have_deleted_note, Toast.LENGTH_SHORT).show();
//                dialog.dismiss();
//            }
//        });
//
//        btn_EditNote_Red.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_red_blur);
//
//                highlightImpl.setType("highlight_red");
//                highlightImpl.setRangy(HighLightTable.updateRangy(highlightImpl.getRangy(), highlightImpl.getType()));
//
//                if (HighLightTable.updateHighlight(highlightImpl)) {
//                    HighlightUtil.sendHighlightBroadcastEvent(
//                            HighlightFragment.this.getActivity().getApplicationContext(),
//                            highlightImpl,
//                            HighLight.HighLightAction.MODIFY);
//                }
//                EventBus.getDefault().post(new UpdateHighlightEvent());
//                adapter.changeColorNote("highlight_red", position);
//            }
//        });
//
//        btn_EditNote_Orange.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_orange_blur);
//
//                highlightImpl.setType("highlight_orange");
//                highlightImpl.setRangy(HighLightTable.updateRangy(highlightImpl.getRangy(), highlightImpl.getType()));
//
//                if (HighLightTable.updateHighlight(highlightImpl)) {
//                    HighlightUtil.sendHighlightBroadcastEvent(
//                            HighlightFragment.this.getActivity().getApplicationContext(),
//                            highlightImpl,
//                            HighLight.HighLightAction.MODIFY);
//                }
//                EventBus.getDefault().post(new UpdateHighlightEvent());
//                adapter.changeColorNote("highlight_orange", position);
//            }
//        });
//
//        btn_EditNote_Blue.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_blue_blur);
//
//                highlightImpl.setType("highlight_blue");
//                highlightImpl.setRangy(HighLightTable.updateRangy(highlightImpl.getRangy(), highlightImpl.getType()));
//
//                if (HighLightTable.updateHighlight(highlightImpl)) {
//                    HighlightUtil.sendHighlightBroadcastEvent(
//                            HighlightFragment.this.getActivity().getApplicationContext(),
//                            highlightImpl,
//                            HighLight.HighLightAction.MODIFY);
//                }
//                EventBus.getDefault().post(new UpdateHighlightEvent());
//                adapter.changeColorNote("highlight_blue", position);
//            }
//        });
//
//        btn_EditNote_Green.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_green_blur);
//
//                highlightImpl.setType("highlight_green");
//                highlightImpl.setRangy(HighLightTable.updateRangy(highlightImpl.getRangy(), highlightImpl.getType()));
//
//                if (HighLightTable.updateHighlight(highlightImpl)) {
//                    HighlightUtil.sendHighlightBroadcastEvent(
//                            HighlightFragment.this.getActivity().getApplicationContext(),
//                            highlightImpl,
//                            HighLight.HighLightAction.MODIFY);
//                }
//                EventBus.getDefault().post(new UpdateHighlightEvent());
//                adapter.changeColorNote("highlight_green", position);
//            }
//        });
//
//    }
}
