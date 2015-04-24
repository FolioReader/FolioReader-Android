package br.com.rsa.folioreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import br.com.rsa.folioreader.adapter.FolioReaderAdapter;
import br.com.rsa.folioreader.contracts.IFolioReader;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;

/**
 * Created by rodrigo.almeida on 09/04/15.
 */
public class FolioReader implements IFolioReader {

    private Context context;
    private VerticalViewPager pager;
    private FolioReaderAdapter adapter;

    public FolioReader(Context context) {
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.folioreader_vertical_view_pager, null);
        pager = (VerticalViewPager) view.findViewById(R.id.folio_reader_vertical_view_pager);
    }

    @Override
    public VerticalViewPager getPager() {
        return this.pager;
    }

    @Override
    public VerticalViewPager setAdapter(FolioReaderAdapter folioReaderAdapter) {
        if (folioReaderAdapter != null)
            pager.setAdapter(folioReaderAdapter);

        adapter = folioReaderAdapter;
        return pager;
    }

    @Override
    public FolioReaderAdapter getAdapter() {
        return this.adapter;
    }
}
