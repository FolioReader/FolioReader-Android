package br.com.rsa.folioreader.contracts;

import br.com.rsa.folioreader.adapter.FolioReaderAdapter;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;

/**
 * Created by rodrigo.almeida on 08/04/15.
 */
public interface IFolioReader {
    /*
    public int getCurrentPage();
    public WebView getCurrentWebView();
    public int getPagesCount();
    public void gotoPage(int pageIndex);
    public void gotoLastPage();
    public void gotoFirstPage();
    public void setHighlight();
    */

    public FolioReaderAdapter getAdapter();
    public VerticalViewPager getPager();
    public VerticalViewPager setAdapter(FolioReaderAdapter folioReaderAdapter);
}
