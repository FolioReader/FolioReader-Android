package br.com.rsa.folioreader;

import android.content.Context;
import android.content.Intent;

import java.io.FileInputStream;
import java.io.IOException;

import br.com.rsa.folioreader.activities.FolioReaderActivity;
import br.com.rsa.folioreader.configuration.Configuration;
import br.com.rsa.folioreader.contracts.IFolioReader;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

/**
 * Created by rodrigo.almeida on 28/04/15.
 */
public class FolioReader implements IFolioReader {

    private Context context;
    private FileInputStream fileInputStream;
    private Book book;

    public FolioReader(Context context) {
        this.context = context;
    }

    @Override
    public void openBook(String ePubpath) {

        try {
            this.fileInputStream = new FileInputStream(ePubpath);
            this.book = new EpubReader().readEpub(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Configuration.setData("key-book", this.book);
        Intent intent = new Intent(context, FolioReaderActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
