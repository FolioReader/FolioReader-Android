package br.com.rsa.folioreader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import br.com.rsa.folioreader.FolioReaderWebView;
import br.com.rsa.folioreader.R;
import br.com.rsa.folioreader.configuration.Constants;

/**
 * Created by rodrigo.almeida on 16/04/15.
 */
public class FolioReaderListAdapter extends RecyclerView.Adapter<FolioReaderListAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<String> list = Collections.emptyList();

    public FolioReaderListAdapter(Context context, List<String> list){
        inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.cell_book, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        String url = list.get(i);
        viewHolder.webView.loadData(Constants.content, "text/html", "UTF-8");
        //viewHolder.webView.loadUrl(url);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        FolioReaderWebView webView;

        public ViewHolder(View itemView) {
            super(itemView);
            webView = (FolioReaderWebView) itemView.findViewById(R.id.webViewCell);
        }
    }
}
