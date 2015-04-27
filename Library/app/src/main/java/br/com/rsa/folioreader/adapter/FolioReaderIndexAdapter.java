package br.com.rsa.folioreader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.rsa.folioreader.R;

/**
 * Created by rodrigo.almeida on 27/04/15.
 */
public class FolioReaderIndexAdapter extends BaseAdapter {
    Context context;
    List<String> list;

    public FolioReaderIndexAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
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
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.cell_index, null);
            viewHolder.lblIndex = (TextView) convertView.findViewById(R.id.lblCaptionIndex);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.lblIndex.setText(position + 1 + ". " + getItem(position).toString());

        return convertView;
    }

    static class ViewHolder {
        TextView lblIndex;
    }
}
