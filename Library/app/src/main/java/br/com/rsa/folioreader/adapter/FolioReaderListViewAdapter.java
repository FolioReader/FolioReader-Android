package br.com.rsa.folioreader.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import br.com.rsa.folioreader.FolioReaderWebView;
import br.com.rsa.folioreader.R;
import br.com.rsa.folioreader.configuration.Constants;

/**
 * Created by rodrigo.almeida on 16/04/15.
 */
public class FolioReaderListViewAdapter extends BaseAdapter {
    private List<String> list;
    private Context context;
    private FolioReaderWebView webView;

    public FolioReaderListViewAdapter(Context context, List<String> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.cell_book, null);
        }

        webView = (FolioReaderWebView) convertView.findViewById(R.id.webViewCell);
        webView.loadData(Constants.content, "text/html", "UTF-8");

        return convertView;
    }
}
