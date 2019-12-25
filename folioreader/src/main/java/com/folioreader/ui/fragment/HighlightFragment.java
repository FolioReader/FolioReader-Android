package com.folioreader.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.media.Image;
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

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.FolioReader;
import com.folioreader.R;
import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.event.UpdateHighlightEvent;
import com.folioreader.model.sqlite.HighLightTable;
import com.folioreader.ui.adapter.HighlightAdapter;
import com.folioreader.util.AppUtil;
import com.folioreader.util.HighlightUtil;

import org.greenrobot.eventbus.EventBus;

public class HighlightFragment extends Fragment implements HighlightAdapter.HighLightAdapterCallback {
    private static final String HIGHLIGHT_ITEM = "highlight_item";
    private View mRootView;
    private HighlightAdapter adapter;
    public static HighlightFragment instance;
    private String mBookId;
    private final String LOG_TAG = "HIGHLIGHT_FRM";
    public static HighlightFragment newInstance(String bookId, String epubTitle) {
        HighlightFragment highlightFragment = new HighlightFragment();
        Bundle args = new Bundle();
        args.putString(FolioReader.EXTRA_BOOK_ID, bookId);
        args.putString(Constants.BOOK_TITLE, epubTitle);
        highlightFragment.setArguments(args);
        instance = highlightFragment;
        return highlightFragment;
    }

    public static HighlightFragment getInstance()
    {
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_highlight_list, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView highlightsView = (RecyclerView) mRootView.findViewById(R.id.rv_highlights);
        Config config = AppUtil.getSavedConfig(getActivity());
        mBookId = getArguments().getString(FolioReader.EXTRA_BOOK_ID);

        if (config.isNightMode()) {
            mRootView.findViewById(R.id.rv_highlights).
                    setBackgroundColor(ContextCompat.getColor(getActivity(),
                            R.color.black));
        }
        highlightsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        highlightsView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        adapter = new HighlightAdapter(getActivity(), HighLightTable.getAllHighlights(mBookId), this, config);
        highlightsView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(HighlightImpl highlightImpl) {
        Intent intent = new Intent();
        intent.putExtra(HIGHLIGHT_ITEM, highlightImpl);
        intent.putExtra(Constants.TYPE, Constants.HIGHLIGHT_SELECTED);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void deleteHighlight(int id) {
        if (HighLightTable.deleteHighlight(id)) {
            EventBus.getDefault().post(new UpdateHighlightEvent());
        }
    }

    @Override
    public void editNote(final HighlightImpl highlightImpl, final int position) {
        // Update here


        final Dialog dialog = new Dialog(getActivity(), R.style.DialogCustomTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_notes);

        if (highlightImpl.getTmpColorLabel().equals("red")) {
            ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_red_blur);
        }
        if (highlightImpl.getTmpColorLabel().equals("orange")) {
            ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_orange_blur);
        }
        if (highlightImpl.getTmpColorLabel().equals("blue")) {
            ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_blue_blur);
        }
        if (highlightImpl.getTmpColorLabel().equals("green")) {
            ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_green_blur);
        }

        dialog.show();

        String noteText = highlightImpl.getNote();
        ((EditText) dialog.findViewById(R.id.edit_note)).setText(noteText);

        Button btn_DeleteNote = dialog.findViewById(R.id.btn_delete_note);
        ImageButton btn_EditNote_Red = dialog.findViewById(R.id.btn_edit_note_red);
        ImageButton btn_EditNote_Orange = dialog.findViewById(R.id.btn_edit_note_orange);
        ImageButton btn_EditNote_Blue = dialog.findViewById(R.id.btn_edit_note_blue);
        ImageButton btn_EditNote_Green = dialog.findViewById(R.id.btn_edit_note_green);

        dialog.findViewById(R.id.btn_save_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String note =
                        ((EditText) dialog.findViewById(R.id.edit_note)).getText().toString();
                if (!TextUtils.isEmpty(note)) {
                    highlightImpl.setNote(note);
                    if (HighLightTable.updateHighlight(highlightImpl)) {
                        HighlightUtil.sendHighlightBroadcastEvent(
                                HighlightFragment.this.getActivity().getApplicationContext(),
                                highlightImpl,
                                HighLight.HighLightAction.MODIFY);
                        adapter.editNote(note, position);
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(getActivity(),
                            getString(R.string.please_enter_note),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_DeleteNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.deleteNote(position);
                Toast.makeText(getActivity(), R.string.you_have_deleted_note, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        btn_EditNote_Red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_red_blur);

                highlightImpl.setType("highlight_red");
                highlightImpl.setRangy(HighLightTable.updateRangy(highlightImpl.getRangy(), highlightImpl.getType()));

                if (HighLightTable.updateHighlight(highlightImpl)) {
                    HighlightUtil.sendHighlightBroadcastEvent(
                            HighlightFragment.this.getActivity().getApplicationContext(),
                            highlightImpl,
                            HighLight.HighLightAction.MODIFY);
                }
                EventBus.getDefault().post(new UpdateHighlightEvent());
                adapter.changeColorNote("highlight_red", position);
            }
        });

        btn_EditNote_Orange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_orange_blur);

                highlightImpl.setType("highlight_orange");
                highlightImpl.setRangy(HighLightTable.updateRangy(highlightImpl.getRangy(), highlightImpl.getType()));

                if (HighLightTable.updateHighlight(highlightImpl)) {
                    HighlightUtil.sendHighlightBroadcastEvent(
                            HighlightFragment.this.getActivity().getApplicationContext(),
                            highlightImpl,
                            HighLight.HighLightAction.MODIFY);
                }
                EventBus.getDefault().post(new UpdateHighlightEvent());
                adapter.changeColorNote("highlight_orange", position);
            }
        });

        btn_EditNote_Blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_blue_blur);

                highlightImpl.setType("highlight_blue");
                highlightImpl.setRangy(HighLightTable.updateRangy(highlightImpl.getRangy(), highlightImpl.getType()));

                if (HighLightTable.updateHighlight(highlightImpl)) {
                    HighlightUtil.sendHighlightBroadcastEvent(
                            HighlightFragment.this.getActivity().getApplicationContext(),
                            highlightImpl,
                            HighLight.HighLightAction.MODIFY);
                }
                EventBus.getDefault().post(new UpdateHighlightEvent());
                adapter.changeColorNote("highlight_blue", position);
            }
        });

        btn_EditNote_Green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageView) dialog.findViewById(R.id.edit_note_background)).setImageResource(R.drawable.note_edittext_background_green_blur);

                highlightImpl.setType("highlight_green");
                highlightImpl.setRangy(HighLightTable.updateRangy(highlightImpl.getRangy(), highlightImpl.getType()));

                if (HighLightTable.updateHighlight(highlightImpl)) {
                    HighlightUtil.sendHighlightBroadcastEvent(
                            HighlightFragment.this.getActivity().getApplicationContext(),
                            highlightImpl,
                            HighLight.HighLightAction.MODIFY);
                }
                EventBus.getDefault().post(new UpdateHighlightEvent());
                adapter.changeColorNote("highlight_green", position);
            }
        });

    }
}


