package com.folioreader.model.sqlite;

/**
 * @author gautam chibde on 21/8/17.
 */

public class HighLightRangy {
    private int id;
    private String bookId;
    private String pageId;
    private String rangy;

    public HighLightRangy() {
    }

    public HighLightRangy(String bookId, String pageId, String rangy) {
        this.bookId = bookId;
        this.pageId = pageId;
        this.rangy = rangy;
    }

    public HighLightRangy(int id, String bookId, String pageId, String rangy) {
        this.id = id;
        this.bookId = bookId;
        this.pageId = pageId;
        this.rangy = rangy;
    }

    @Override
    public String toString() {
        return "HighLightRangy{" +
                "id=" + id +
                ", bookId='" + bookId + '\'' +
                ", pageId='" + pageId + '\'' +
                ", rangy='" + rangy + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getRangy() {
        return rangy;
    }

    public void setRangy(String rangy) {
        this.rangy = rangy;
    }
}
