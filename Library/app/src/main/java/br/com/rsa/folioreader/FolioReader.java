package br.com.rsa.folioreader;

import android.content.Context;

import java.io.FileInputStream;
import java.util.UUID;

import br.com.rsa.folioreader.contracts.IFolioReader;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

/**
 * Created by rodrigo.almeida on 09/04/15.
 */
public class FolioReader implements IFolioReader {

    private Book book;
    private FileInputStream fileInputStream;
    private Context context;
    private UUID identity;
    private String localDecompressed = context.getExternalFilesDir(null) + identity.toString();

    public FolioReader(String pathEPub, Context context) {
        try {
            this.fileInputStream = new FileInputStream(pathEPub);
            this.book = (new EpubReader()).readEpub(fileInputStream);
        } catch (Exception e) { }
        this.context = context;
        identity = UUID.randomUUID();
    }

    @Override
    public Book getBook() {
        return this.book;
    }
}
