package com.folioreader.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * This data structure holds information about an individual highlight.
 *
 * @author mahavir on 5/12/16.
 */

public class Highlight implements Parcelable {

    public static final String INTENT = Highlight.class.getName();
    public static final String BROADCAST_EVENT = "highlight_broadcast_event";

    /**
     * Database id
     */
    private int id;
    /**
     * Book id.
     */
    private String bookId;
    /**
     * Highlighted text content text content.
     */
    private String content;
    /**
     * Date time when highlight is created (format:- MMM dd, yyyy | HH:mm).
     */
    private Date date;
    /**
     * Field defines the color of the highlight.
     */
    private String type;
    /**
     * Page index in the book taken from Epub spine reference.
     */
    private int pageNumber;
    /**
     * href of the page from the Epub spine list.
     */
    private String pageId;
    /**
     * <p> Contains highlight meta data in terms of rangy format.</p>
     * <strong>format </strong>:- start$end$id$class$containerId.
     * <p>for reference, look here: <a href="https://github.com/timdown/rangy">rangy</a>.</p>
     */
    private String rangy;
    /**
     * Note linked to the highlight (optional)
     */
    private String note;

    public enum HighLightAction {
        NEW, DELETE, MODIFY
    }

    public enum HighlightStyle {
        Yellow,
        Green,
        Blue,
        Pink,
        Underline,
        TextColor,
        DottetUnderline,
        Normal;

        /**
         * Return CSS class for HighlightStyle.
         */
        public static String classForStyle(HighlightStyle style) {
            switch (style) {
                case Yellow:
                    return "yellow";
                case Green:
                    return "green";
                case Blue:
                    return "blue";
                case Pink:
                    return "pink";
                case Underline:
                    return "underline";
                case DottetUnderline:
                    return "mediaOverlayStyle1";
                case TextColor:
                    return "mediaOverlayStyle2";
                default:
                    return "mediaOverlayStyle0";

            }
        }
    }

    public Highlight(int id, String bookId, String content, Date date, String type,
                     int pageNumber, String pageId,
                     String rangy, String note) {
        this.id = id;
        this.bookId = bookId;
        this.content = content;
        this.date = date;
        this.type = type;
        this.pageNumber = pageNumber;
        this.pageId = pageId;
        this.rangy = rangy;
        this.note = note;
    }

    public Highlight() {
    }

    protected Highlight(Parcel in) {
        readFromParcel(in);
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

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getRangy() {
        return rangy;
    }

    public void setRangy(String rangy) {
        this.rangy = rangy;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Highlight highlight = (Highlight) o;

        return id == highlight.id
                && (bookId != null ? bookId.equals(highlight.bookId) : highlight.bookId == null
                && (content != null ? content.equals(highlight.content) : highlight.content == null
                && (date != null ? date.equals(highlight.date) : highlight.date == null
                && (type != null ? type.equals(highlight.type) : highlight.type == null))));
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (bookId != null ? bookId.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Highlight{" +
                "id=" + id +
                ", bookId='" + bookId + '\'' +
                ", content='" + content + '\'' +
                ", date=" + date +
                ", type='" + type + '\'' +
                ", pageNumber=" + pageNumber +
                ", pageId='" + pageId + '\'' +
                ", rangy='" + rangy + '\'' +
                ", note='" + note + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(bookId);
        dest.writeString(pageId);
        dest.writeString(rangy);
        dest.writeString(content);
        dest.writeSerializable(date);
        dest.writeString(type);
        dest.writeInt(pageNumber);
        dest.writeString(note);
    }

    private void readFromParcel(Parcel in) {
        id = in.readInt();
        bookId = in.readString();
        pageId = in.readString();
        rangy = in.readString();
        content = in.readString();
        date = (Date) in.readSerializable();
        type = in.readString();
        pageNumber = in.readInt();
        note = in.readString();
    }

    public static final Creator<Highlight> CREATOR = new Creator<Highlight>() {
        @Override
        public Highlight createFromParcel(Parcel in) {
            return new Highlight(in);
        }

        @Override
        public Highlight[] newArray(int size) {
            return new Highlight[size];
        }
    };
}
