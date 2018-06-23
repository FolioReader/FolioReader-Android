package com.folioreader;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Created by mahavir on 4/12/16.
 */
public class Config implements Parcelable {
    public static final String INTENT_CONFIG = "config";
    public static final String CONFIG_FONT = "font";
    public static final String CONFIG_FONT_SIZE = "font_size";
    public static final String CONFIG_IS_NIGHTMODE = "is_night_mode";
    public static final String CONFIG_IS_THEMECOLOR = "theme_color";
    public static final String CONFIG_IS_TTS = "is_tts";
    public static final String CONFIG_IS_ACTION_COPY = "is_action_copy";
    public static final String CONFIG_IS_ACTION_SHARE = "is_action_share";
    public static final String CONFIG_IS_ACTION_DEFINE = "is_action_define";
    public static final String CONFIG_IS_ACTION_HIGHLIGHT = "is_action_highlight";
    public static final String INTENT_PORT = "port";
    private int font;
    private int fontSize;
    private boolean nightMode;
    private int themeColor;
    private boolean showTts;
    private boolean actionCopy;
    private boolean actionShare;
    private boolean actionDefine;
    private boolean actionHighlight;

    public Config(int font, int fontSize, boolean nightMode, int themeColor, boolean showTts, boolean actionCopy, boolean actionShare, boolean actionDefine, boolean actionHighlight) {
        this.font = font;
        this.fontSize = fontSize;
        this.nightMode = nightMode;
        this.themeColor = themeColor;
        this.showTts = showTts;
        this.actionCopy = actionCopy;
        this.actionShare = actionShare;
        this.actionDefine = actionDefine;
        this.actionHighlight = actionHighlight;
    }

    private Config(ConfigBuilder configBuilder) {
        font = configBuilder.mFont;
        fontSize = configBuilder.mFontSize;
        nightMode = configBuilder.mNightMode;
        themeColor = configBuilder.mThemeColor;
        showTts = configBuilder.mShowTts;
        actionCopy = configBuilder.mActionCopy;
        actionShare = configBuilder.mActionShare;
        actionDefine = configBuilder.mActionDefine;
        actionHighlight = configBuilder.mActionHighlight;
    }

    public Config(JSONObject jsonObject) {
        font = jsonObject.optInt(CONFIG_FONT);
        fontSize = jsonObject.optInt(CONFIG_FONT_SIZE);
        nightMode = jsonObject.optBoolean(CONFIG_IS_NIGHTMODE);
        themeColor = jsonObject.optInt(CONFIG_IS_THEMECOLOR);
        showTts = jsonObject.optBoolean(CONFIG_IS_TTS);
        actionCopy = jsonObject.optBoolean(CONFIG_IS_ACTION_COPY);
        actionShare = jsonObject.optBoolean(CONFIG_IS_ACTION_SHARE);
        actionDefine = jsonObject.optBoolean(CONFIG_IS_ACTION_DEFINE);
        actionHighlight = jsonObject.optBoolean(CONFIG_IS_ACTION_HIGHLIGHT);
    }

    private Config() {
        fontSize = 2;
        font = 3;
        nightMode = false;
        themeColor = R.color.app_green;
        showTts = true;
        actionCopy = true;
        actionShare = true;
        actionDefine = true;
        actionHighlight = true;
    }

    private Config(Parcel in) {
        readFromParcel(in);
    }

    public int getFont() {
        return font;
    }

    public void setFont(int font) {
        this.font = font;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public boolean isNightMode() {
        return nightMode;
    }

    public void setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
    }

    public int getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(int themeColor) {
        this.themeColor = themeColor;
    }

    public boolean isShowTts() {
        return showTts;
    }

    public void setShowTts(boolean showTts) {
        this.showTts = showTts;
    }

    public boolean isActionCopy() {
        return actionCopy;
    }

    public void setActionCopy(boolean actionCopy) {
        this.actionCopy = actionCopy;
    }

    public boolean isActionShare() {
        return actionShare;
    }

    public void setActionShare(boolean actionShare) {
        this.actionShare = actionShare;
    }

    public boolean isActionDefine() {
        return actionDefine;
    }

    public void setActionDefine(boolean actionDefine) {
        this.actionDefine = actionDefine;
    }

    public boolean isActionHighlight() {
        return actionHighlight;
    }

    public void setActionHighlight(boolean actionHighlight) {
        this.actionHighlight = actionHighlight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Config)) {
            return false;
        }

        Config config = (Config) o;

        return font == config.font && fontSize == config.fontSize && nightMode == config.nightMode;
    }

    @Override
    public int hashCode() {
        int result = font;
        result = 31 * result + fontSize;
        result = 31 * result + (nightMode ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Config{"
                + "font=" + font
                + ", fontSize=" + fontSize
                + ", nightMode=" + nightMode
                + ", themeColor=" + themeColor
                + ", showTts=" + showTts
                + ", actionCopy=" + actionCopy
                + ", actionShare=" + actionShare
                + ", actionDefine=" + actionDefine
                + ", actionHighlight=" + actionHighlight
                + '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(font);
        dest.writeInt(fontSize);
        dest.writeInt(nightMode ? 1 : 0);
        dest.writeInt(themeColor);
        dest.writeInt(showTts ? 1 : 0);
        dest.writeInt(actionCopy ? 1 : 0);
        dest.writeInt(actionShare ? 1 : 0);
        dest.writeInt(actionDefine ? 1 : 0);
        dest.writeInt(actionHighlight ? 1 : 0);
    }

    private void readFromParcel(Parcel in) {
        font = in.readInt();
        fontSize = in.readInt();
        nightMode = in.readInt() == 1;
        themeColor = in.readInt();
        showTts = in.readInt() == 1;
        actionCopy = in.readInt() == 1;
        actionShare = in.readInt() == 1;
        actionDefine = in.readInt() == 1;
        actionHighlight = in.readInt() == 1;
    }

    public static final Creator<Config> CREATOR = new Creator<Config>() {
        @Override
        public Config createFromParcel(Parcel in) {
            return new Config(in);
        }

        @Override
        public Config[] newArray(int size) {
            return new Config[size];
        }
    };

    public static class ConfigBuilder {
        private int mFont = 3;
        private int mFontSize = 2;
        private boolean mNightMode = false;
        private int mThemeColor = R.color.app_green;
        private boolean mShowTts = true;
        private boolean mActionCopy = true;
        private boolean mActionShare = true;
        private boolean mActionDefine = true;
        private boolean mActionHighlight = true;

        public ConfigBuilder font(int font) {
            mFont = font;
            return this;
        }

        public ConfigBuilder fontSize(int fontSize) {
            mFontSize = fontSize;
            return this;
        }

        public ConfigBuilder nightmode(boolean nightMode) {
            mNightMode = nightMode;
            return this;
        }

        public ConfigBuilder themeColor(int themeColor) {
            mThemeColor = themeColor;
            return this;
        }

        public ConfigBuilder setShowTts(boolean showTts) {
            mShowTts = showTts;
            return this;
        }

        public ConfigBuilder allowCopy(boolean allowCopy) {
            mActionCopy = allowCopy;
            return this;
        }

        public ConfigBuilder allowShare(boolean allowShare) {
            mActionShare = allowShare;
            return this;
        }

        public ConfigBuilder allowDefine(boolean allowDefine) {
            mActionDefine = allowDefine;
            return this;
        }

        public ConfigBuilder allowHighlight(boolean allowHighlight) {
            mActionHighlight = allowHighlight;
            return this;
        }


        public Config build() {
            return new Config(this);
        }
    }
}


