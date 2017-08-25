package com.folioreader.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.folioreader.R;

import java.util.Date;

/**
 * Created by mahavir on 5/12/16.
 */

public class Highlight implements Parcelable {

    private int id;
    private String bookId;
    private String content;
    private Date date;
    private String type;
    private int scrollPosition;
    private int pageNumber;
    private String pageId;
    private String rangy;
    private String note;

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
         * Return HighlightStyle for CSS class.
         */
        public static HighlightStyle styleForClass(String className) {
            switch (className) {
                case "yellow":
                    return Yellow;
                case "green":
                    return Green;
                case "blue":
                    return Blue;
                case "pink":
                    return Pink;
                case "underline":
                    return Underline;
                default:
                    return Yellow;
            }
        }

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

        public static int colorForStyle(HighlightStyle style, boolean nightMode) {
            switch (style) {
                case Yellow:
                    return R.color.yellow;
                case Green:
                    return R.color.green;
                case Blue:
                    return R.color.blue;
                case Pink:
                    return R.color.pink;
                case Underline:
                    return R.color.underline;
                default:
                    return R.color.yellow;
            }
        }
    }

    public Highlight(int id, String bookId, String content, Date date, String type,
                     int scrollPosition, int pageNumber, String pageId,
                     String rangy, String note) {
        this.id = id;
        this.bookId = bookId;
        this.content = content;
        this.date = date;
        this.type = type;
        this.scrollPosition = scrollPosition;
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

    public int getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(int scrollPosition) {
        this.scrollPosition = scrollPosition;
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

        if (id != highlight.id) return false;
        if (bookId != null ? !bookId.equals(highlight.bookId) : highlight.bookId != null)
            return false;
        if (content != null ? !content.equals(highlight.content) : highlight.content != null)
            return false;
        return date != null ? date.equals(highlight.date) : highlight.date == null && (type != null ? type.equals(highlight.type) : highlight.type == null);
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
                ", scrollPosition=" + scrollPosition +
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
        dest.writeInt(scrollPosition);
        dest.writeInt(pageNumber);
        dest.writeString(note);
    }

    private void readFromParcel(Parcel in) {
        id = in.readInt();
        pageId = in.readString();
        rangy = in.readString();
        bookId = in.readString();
        content = in.readString();
        date = (Date) in.readSerializable();
        type = in.readString();
        scrollPosition = in.readInt();
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
