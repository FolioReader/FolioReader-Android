package br.com.rsa.folioreader.entities;

import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;

/**
 * Created by rodrigo.almeida on 30/04/15.
 */
public class BookDecompressed {
    private List<String> urlResources;
    private Book book;

    public BookDecompressed() {
        urlResources = new ArrayList<>();
    }

    public void setUrlResources(List<String> urlResources) {
        this.urlResources = urlResources;
    }

    public void setUrlResources(String urlResource) {
        this.urlResources.add(urlResource);
    }

    public List<String> getUrlResources() {
        return urlResources;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Book getBook() {
        return book;
    }
}
