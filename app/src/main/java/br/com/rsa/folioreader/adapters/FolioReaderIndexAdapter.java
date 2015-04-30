package br.com.rsa.folioreader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.rsa.folioreader.R;
import nl.siegmann.epublib.domain.TOCReference;

/**
 * Created by rodrigo.almeida on 28/04/15.
 */
public class FolioReaderIndexAdapter extends BaseAdapter {

    private List<TOCReference> list;
    private Context context;

    public FolioReaderIndexAdapter(Context context, List<TOCReference> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public TOCReference getItem(int position) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.folioreader_menudrawer_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.lblindex.setText(getItem(position).getTitle());

        return convertView;
    }

    private class ViewHolder {

        public ViewHolder(View view){
            lblindex = (TextView) view.findViewById(R.id.folioreader_textview_itemmenu);
        }

        TextView lblindex;
    }
}
