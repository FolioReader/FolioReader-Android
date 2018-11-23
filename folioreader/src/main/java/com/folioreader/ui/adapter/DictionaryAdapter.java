package com.folioreader.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.model.dictionary.*;
import com.folioreader.ui.base.DictionaryCallBack;
import com.folioreader.util.AppUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gautam chibde on 4/7/17.
 */

public class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.DictionaryHolder> {

    private List<DictionaryResults> results;
    private Context context;
    private DictionaryCallBack callBack;
    private static Config config;

    public DictionaryAdapter(Context context, DictionaryCallBack callBack) {
        this.results = new ArrayList<>();
        this.context = context;
        this.callBack = callBack;
        config = AppUtil.getSavedConfig(context);
    }

    @Override
    public DictionaryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DictionaryHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dictionary, parent, false));
    }

    @Override
    @SuppressWarnings("PMD.InefficientEmptyStringCheck")
    public void onBindViewHolder(DictionaryHolder holder, int position) {
        final DictionaryResults res = results.get(position);
        if (res.getPartOfSpeech() != null) {
            int wordLength = res.getHeadword().length();
            SpannableString spannableString = new SpannableString(res.getHeadword() + " - " + res.getPartOfSpeech());
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, wordLength, 0);
            spannableString.setSpan(new StyleSpan(Typeface.ITALIC), wordLength + 2, spannableString.length(), 0);
            holder.name.setText(spannableString);
        } else {
            holder.name.setTypeface(Typeface.DEFAULT_BOLD);
            holder.name.setText(res.getHeadword());
        }
        StringBuilder def = new StringBuilder();
        StringBuilder exp = new StringBuilder();

        if (res.getSenses() != null) {
            for (Senses senses : res.getSenses()) {
                if (senses.getDefinition() != null) {
                    for (String s : senses.getDefinition()) {
                        def.append("\u2022 ").append(s).append('\n');
                    }
                }
            }

            for (Senses senses : res.getSenses()) {
                if (senses.getExamples() != null) {
                    for (Example s : senses.getExamples()) {
                        exp.append("\u2022 ").append(s.getText()).append('\n');
                    }
                }
            }
        }
        if (!def.toString().trim().isEmpty()) {
            def.insert(0, "Definition\n");
            holder.definition.setText(def.toString());
        } else {
            holder.definition.setVisibility(View.GONE);
        }

        if (!exp.toString().trim().isEmpty()) {
            exp.insert(0, "Example\n");
            holder.example.setText(exp.toString());
        } else {
            holder.example.setVisibility(View.GONE);
        }
//        if (res.getPronunciations() != null) {
//            final String url = getAudioUrl(res.getPronunciations());
//            if (url == null) {
//                holder.sound.setVisibility(View.GONE);
//            }
//        }

//        holder.sound.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("DictionaryAdapter", "clicked");
//                if (res.getPronunciations() != null) {
//                    final String url = getAudioUrl(res.getPronunciations());
//                    callBack.playMedia(url);
//                }
//            }
//        });
    }

    private String getAudioUrl(List<Pronunciations> pronunciations) {
        if (!pronunciations.isEmpty()
                && pronunciations.get(0).getAudio() != null
                && !pronunciations.get(0).getAudio().isEmpty()) {
            Audio audio = pronunciations.get(0).getAudio().get(0);
            if (audio.getUrl() != null) {
                return audio.getUrl();
            }
        }
        return null;
    }

    public void setResults(List<DictionaryResults> resultsList) {
        if (resultsList != null && !resultsList.isEmpty()) {
            results.addAll(resultsList);
            notifyDataSetChanged();
        }
    }

    public void clear() {
        results.clear();
        notifyItemRangeRemoved(0, results.size());
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class DictionaryHolder extends RecyclerView.ViewHolder {
        private TextView name, definition, example;
        //TODO private ImageButton sound;

        public DictionaryHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.tv_word);
            //sound = (ImageButton) itemView.findViewById(R.id.ib_speak);
            definition = (TextView) itemView.findViewById(R.id.tv_definition);
            example = (TextView) itemView.findViewById(R.id.tv_examples);
            View rootView = itemView.findViewById(R.id.rootView);

            if (config.isNightMode()) {
                rootView.setBackgroundColor(Color.BLACK);
                int nightTextColor = ContextCompat.getColor(itemView.getContext(),
                        R.color.night_text_color);
                name.setTextColor(nightTextColor);
                definition.setTextColor(nightTextColor);
                example.setTextColor(nightTextColor);
            } else {
                rootView.setBackgroundColor(Color.WHITE);
            }
        }
    }
}
