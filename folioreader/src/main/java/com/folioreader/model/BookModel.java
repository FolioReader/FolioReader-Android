package com.folioreader.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import nl.siegmann.epublib.domain.Book;

/**
 * Created by mobisys on 8/11/2016.
 */

@DatabaseTable(tableName = "bookTable")
public class BookModel implements Serializable {
    @DatabaseField(generatedId=true)
    private int localId;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private Book book;
    @DatabaseField()
    private String bookName;

    public BookModel(){}

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
}
