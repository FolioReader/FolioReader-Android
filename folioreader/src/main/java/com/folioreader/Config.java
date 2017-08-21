package com.folioreader;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mahavir on 4/12/16.
 */
public class Config implements Parcelable {
    public static final String INTENT_CONFIG = "config";
    private static Config mConfig;
    private int font;
    private int fontSize;
    private boolean nightMode;
    private int themeColor;

    public Config(int font, int fontSize, boolean nightMode, int iconcolor) {
        this.font = font;
        this.fontSize = fontSize;
        this.nightMode = nightMode;
        this.themeColor = iconcolor;
    }

    private Config(ConfigBuilder configBuilder) {
        font = configBuilder.font;
        fontSize = configBuilder.fontSize;
        nightMode = configBuilder.nightMode;
        themeColor = configBuilder.themeColor;
        mConfig = this;
    }

    private Config() {
        fontSize = 2;
        font = 3;
        nightMode = false;
        themeColor = R.color.app_green;
    }

    private Config(Parcel in) {
        readFromParcel(in);
    }

    public static Config getConfig() {
        if (mConfig == null) mConfig = new Config();

        return mConfig;
    }

    public static void setConfig(Config config) {
        mConfig = config;
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
    }

    private void readFromParcel(Parcel in) {
        font = in.readInt();
        fontSize = in.readInt();
        nightMode = in.readInt() == 1;
        themeColor = in.readInt();
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
        private int font = 3;
        private int fontSize = 2;
        private boolean nightMode = false;
        private int themeColor = R.color.app_green;

        public ConfigBuilder font(int font) {
            this.font = font;
            return this;
        }

        public ConfigBuilder fontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public ConfigBuilder nightmode(boolean nightMode) {
            this.nightMode = nightMode;
            return this;
        }

        public ConfigBuilder themeColor(int themeColor) {
            this.themeColor = themeColor;
            return this;
        }

        public Config build() {
            return new Config(this);
        }
    }
}


