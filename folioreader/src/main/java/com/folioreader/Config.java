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
    public static final String INTENT_PORT = "port";
    private int font;
    private int fontSize;
    private boolean nightMode;
    private int themeColor;
    private boolean showTts;

    public Config(int font, int fontSize, boolean nightMode, int iconcolor, boolean showTts) {
        this.font = font;
        this.fontSize = fontSize;
        this.nightMode = nightMode;
        this.themeColor = iconcolor;
        this.showTts = showTts;
    }

    private Config(ConfigBuilder configBuilder) {
        font = configBuilder.mFont;
        fontSize = configBuilder.mFontSize;
        nightMode = configBuilder.mNightMode;
        themeColor = configBuilder.mThemeColor;
        showTts = configBuilder.mShowTts;
    }

    public Config(JSONObject jsonObject) {
        font = jsonObject.optInt(CONFIG_FONT);
        fontSize = jsonObject.optInt(CONFIG_FONT_SIZE);
        nightMode = jsonObject.optBoolean(CONFIG_IS_NIGHTMODE);
        themeColor = jsonObject.optInt(CONFIG_IS_THEMECOLOR);
        showTts = jsonObject.optBoolean(CONFIG_IS_TTS);
    }

    private Config() {
        fontSize = 2;
        font = 3;
        nightMode = false;
        themeColor = R.color.app_green;
        showTts = true;
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

    @Override

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Config)) return false;

        Config config = (Config) o;

        return font == config.font && fontSize == config.fontSize && nightMode == config.nightMode;
    }

    @Override
    public int hashCode() {
        int result = font;
        result = 31 * result
                + fontSize;
        result = 31 * result
                + (nightMode ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Config{"
                + "font="
                + font
                + ", fontSize=" + fontSize
                + ", nightMode=" + nightMode
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
    }

    private void readFromParcel(Parcel in) {
        font = in.readInt();
        fontSize = in.readInt();
        nightMode = in.readInt() == 1;
        themeColor = in.readInt();
        showTts = in.readInt() == 1;
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


        public Config build() {
            return new Config(this);
        }
    }
}


