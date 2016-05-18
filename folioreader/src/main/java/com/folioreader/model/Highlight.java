package com.folioreader.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by mahavir on 5/12/16.
 */
public class Highlight implements Parcelable {
    public static enum HighlighStyle {
        Yellow,
        Green,
        Blue,
        Pink,
        Underline;

        /**
         Return HighlightStyle for CSS class.
         */
        public static HighlighStyle styleForClass(String className) {
            switch (className) {
                case "highlight-yellow":
                    return Yellow;
                case "highlight-green":
                    return Green;
                case "highlight-blue":
                    return Blue;
                case "highlight-pink":
                    return Pink;
                case "highlight-underline":
                    return Underline;
                default:
                    return Yellow;
            }
        }

        /**
         Return CSS class for HighlightStyle.
         */
        public static String classForStyle(HighlighStyle style) {
            switch (style){
                case Yellow:
                    return "highlight-yellow";
                case Green:
                    return "highlight-green";
                case Blue:
                    return "highlight-blue";
                case Pink:
                    return "highlight-pink";
                case Underline:
                    return "highlight-underline";
                default:
                    return "highlight-yellow";

            }
        }

        static String colorForStyle(HighlighStyle style, boolean nightMode) {
            switch (style){
                case Yellow:
                    return "#FFEB6B";
                case Green:
                    return "#C0ED72";
                case Blue:
                    return "#ADD8FF";
                case Pink:
                    return "#FFB0CA";
                case Underline:
                    return "#F02814";
                default:
                    return "#FFEB6B";
            }
        }
    }

    private String bookId;
    private String content;
    private String contentPost;
    private String contentPre;
    private Date date;
    private String highlightId;
    private int page;
    private int type;

    public Highlight() {
    }

    public Highlight(String bookId, String content, String contentPost, String contentPre, Date date, String highlightId, int page, int type) {
        this.bookId = bookId;
        this.content = content;
        this.contentPost = contentPost;
        this.contentPre = contentPre;
        this.date = date;
        this.highlightId = highlightId;
        this.page = page;
        this.type = type;
    }

    protected Highlight(Parcel in) {
        readFromParcel(in);
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

    public String getContentPost() {
        return contentPost;
    }

    public void setContentPost(String contentPost) {
        this.contentPost = contentPost;
    }

    public String getContentPre() {
        return contentPre;
    }

    public void setContentPre(String contentPre) {
        this.contentPre = contentPre;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getHighlightId() {
        return highlightId;
    }

    public void setHighlightId(String highlightId) {
        this.highlightId = highlightId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Highlight)) return false;

        Highlight highlight = (Highlight) o;

        if (page != highlight.page) return false;
        if (type != highlight.type) return false;
        if (bookId != null ? !bookId.equals(highlight.bookId) : highlight.bookId != null)
            return false;
        if (content != null ? !content.equals(highlight.content) : highlight.content != null)
            return false;
        if (contentPost != null ? !contentPost.equals(highlight.contentPost) : highlight.contentPost != null)
            return false;
        if (contentPre != null ? !contentPre.equals(highlight.contentPre) : highlight.contentPre != null)
            return false;
        if (date != null ? !date.equals(highlight.date) : highlight.date != null) return false;
        return highlightId != null ? highlightId.equals(highlight.highlightId) : highlight.highlightId == null;

    }

    @Override
    public int hashCode() {
        int result = bookId != null ? bookId.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (contentPost != null ? contentPost.hashCode() : 0);
        result = 31 * result + (contentPre != null ? contentPre.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (highlightId != null ? highlightId.hashCode() : 0);
        result = 31 * result + page;
        result = 31 * result + type;
        return result;
    }

    @Override
    public String toString() {
        return "Highlight{" +
                "bookId='" + bookId + '\'' +
                ", content='" + content + '\'' +
                ", contentPost='" + contentPost + '\'' +
                ", contentPre='" + contentPre + '\'' +
                ", date=" + date +
                ", highlightId='" + highlightId + '\'' +
                ", page=" + page +
                ", type=" + type +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookId);
        dest.writeString(content);
        dest.writeString(contentPost);
        dest.writeString(contentPre);
        dest.writeSerializable(date);
        dest.writeString(highlightId);
        dest.writeInt(page);
        dest.writeInt(type);
    }

    private void readFromParcel(Parcel in){
        bookId = in.readString();
        content = in.readString();
        contentPost = in.readString();
        contentPre = in.readString();
        date = (Date) in.readSerializable();
        highlightId = in.readString();
        page = in.readInt();
        type = in.readInt();
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
