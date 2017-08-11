package com.folioreader.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.folioreader.R;

import java.util.Date;

/**
 * Created by mahavir on 5/12/16.
 */

public class Highlight implements Parcelable {

    public static final String LOCAL_DB_HIGHLIGHT_CONTENT = "content";
    public static final String LOCAL_DB_HIGHLIGHT_CONTENT_PRE = "contentPre";
    public static final String LOCAL_DB_HIGHLIGHT_CONTENT_POST = "contentPost";
    public static final String LOCAL_DB_HIGHLIGHT_BOOK_ID = "bookId";
    public static final String LOCAL_DB_HIGHLIGHT_PAGE = "page";
    public static final String LOCAL_DB_HIGHLIGHT_ID = "highlightId";
    public static final String LOCAL_DB_HIGHLIGHT_TYPE = "type";
    public static final String MEDIA_OVERLAY_STYLE = "epub-media-overlay-playing";

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
         * Return CSS class for HighlightStyle.
         */
        public static String classForStyle(HighlightStyle style) {
            switch (style) {
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


    private int id;
    private String bookId;
    private String content;
    private String contentPost;
    private String contentPre;
    private Date date;
    private String highlightId;
    private int page;
    private String type;
    private int currentPagerPostion;
    private int currentWebviewScrollPos;
    private String note;

    public Highlight() {
    }

    public Highlight(int id, String bookId, String content, String contentPost,
                     String contentPre, Date date, String highlightId, int page,
                     String type, String note) {
        this.id = id;
        this.bookId = bookId;
        this.content = content;
        this.contentPost = contentPost;
        this.contentPre = contentPre;
        this.date = date;
        this.highlightId = highlightId;
        this.page = page;
        this.type = type;
        this.note = note;
    }

    public Highlight(String bookId, String content, String contentPost, String contentPre, Date date, String highlightId, int page, String type, int currentPagerPostion, int currentWebviewScrollPos, String note) {
        this.bookId = bookId;
        this.content = content;
        this.contentPost = contentPost;
        this.contentPre = contentPre;
        this.date = date;
        this.highlightId = highlightId;
        this.page = page;
        this.type = type;
        this.currentPagerPostion = currentPagerPostion;
        this.currentWebviewScrollPos = currentWebviewScrollPos;
        this.note = note;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCurrentPagerPostion() {
        return currentPagerPostion;
    }

    public void setCurrentPagerPostion(int currentPagerPostion) {
        this.currentPagerPostion = currentPagerPostion;
    }

    public int getCurrentWebviewScrollPos() {
        return currentWebviewScrollPos;
    }

    public void setCurrentWebviewScrollPos(int currentWebviewScrollPos) {
        this.currentWebviewScrollPos = currentWebviewScrollPos;
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
        if (page != highlight.page) return false;
        if (bookId != null ? !bookId.equals(highlight.bookId) : highlight.bookId != null)
            return false;
        if (content != null ? !content.equals(highlight.content) : highlight.content != null)
            return false;
        if (contentPost != null ? !contentPost.equals(highlight.contentPost) :
                highlight.contentPost != null)
            return false;
        if (contentPre != null ? !contentPre.equals(highlight.contentPre) :
                highlight.contentPre != null)
            return false;
        if (date != null ? !date.equals(highlight.date) : highlight.date != null) return false;
        if (highlightId != null ? !
                highlightId.equals(highlight.highlightId) : highlight.highlightId != null)
            return false;
        return type != null ? type.equals(highlight.type) : highlight.type == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (bookId != null ? bookId.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (contentPost != null ? contentPost.hashCode() : 0);
        result = 31 * result + (contentPre != null ? contentPre.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (highlightId != null ? highlightId.hashCode() : 0);
        result = 31 * result + page;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Highlight{" +
                "id=" + id +
                ", bookId='" + bookId + '\'' +
                ", content='" + content + '\'' +
                ", contentPost='" + contentPost + '\'' +
                ", contentPre='" + contentPre + '\'' +
                ", date=" + date +
                ", highlightId='" + highlightId + '\'' +
                ", page=" + page +
                ", type='" + type + '\'' +
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
        dest.writeString(content);
        dest.writeString(contentPost);
        dest.writeString(contentPre);
        dest.writeSerializable(date);
        dest.writeString(highlightId);
        dest.writeInt(page);
        dest.writeString(type);
        dest.writeInt(currentPagerPostion);
        dest.writeInt(currentWebviewScrollPos);
        dest.writeString(note);
    }

    private void readFromParcel(Parcel in) {
        id = in.readInt();
        bookId = in.readString();
        content = in.readString();
        contentPost = in.readString();
        contentPre = in.readString();
        date = (Date) in.readSerializable();
        highlightId = in.readString();
        page = in.readInt();
        type = in.readString();
        currentPagerPostion = in.readInt();
        currentWebviewScrollPos = in.readInt();
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
