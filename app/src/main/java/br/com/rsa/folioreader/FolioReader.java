package br.com.rsa.folioreader;

import android.content.Context;
import android.content.Intent;

import java.io.FileInputStream;
import java.io.IOException;

import br.com.rsa.folioreader.activities.FolioReaderActivity;
import br.com.rsa.folioreader.configuration.Configuration;
import br.com.rsa.folioreader.contracts.IFolioReader;
import br.com.rsa.folioreader.entities.BookDecompressed;
import br.com.rsa.folioreader.utils.FolioReaderUtils;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;

/**
 * Created by rodrigo.almeida on 28/04/15.
 */
public class FolioReader implements IFolioReader {

    private Context context;
    private FileInputStream fileInputStream;
    private Book book;
    private BookDecompressed bookDecompressed;

    public FolioReader(Context context) {
        this.context = context;
        bookDecompressed = new BookDecompressed();
    }

    @Override
    public void openBook(String ePubpath) {
        try {
            this.fileInputStream = new FileInputStream(ePubpath);

            String folderDec = FolioReaderUtils.getPathePubDec(context) + FolioReaderUtils.getFilename(ePubpath) + "/";

            if (!FolioReaderUtils.isDecompressed(context, ePubpath))
                FolioReaderUtils.unzipEPub(ePubpath, folderDec);

            this.book = new EpubReader().readEpub(fileInputStream);
            bookDecompressed.setBook(book);
            String opfPath = FolioReaderUtils.getPathOPF(folderDec);

            for (SpineReference s : book.getSpine().getSpineReferences()) {
                bookDecompressed.setUrlResources("file://" + folderDec + opfPath + "/" + s.getResource().getHref());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Configuration.setData("key-book", bookDecompressed);
        Intent intent = new Intent(context, FolioReaderActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
