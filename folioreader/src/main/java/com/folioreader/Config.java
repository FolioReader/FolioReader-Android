package com.folioreader;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mahavir on 4/12/16.
 */
public class Config implements Parcelable {

    private static final String LOG_TAG = Config.class.getSimpleName();
    public static final String INTENT_CONFIG = "config";
    public static final String EXTRA_OVERRIDE_CONFIG = "com.folioreader.extra.OVERRIDE_CONFIG";
    public static final String CONFIG_FONT = "font";
    public static final String CONFIG_FONT_SIZE = "font_size";
    public static final String CONFIG_IS_NIGHTMODE = "is_night_mode";
    public static final String CONFIG_IS_THEMECOLOR = "theme_color";
    public static final String CONFIG_IS_TTS = "is_tts";
    public static final String CONFIG_ALLOWED_DIRECTION = "allowed_direction";
    public static final String CONFIG_DIRECTION = "direction";
    public static final String INTENT_PORT = "port";
    private int font = 3;
    private int fontSize = 2;
    private boolean nightMode;
    private int themeColor = R.color.app_green;
    private boolean showTts = true;
    private AllowedDirection allowedDirection = AllowedDirection.ONLY_VERTICAL;
    private Direction direction = Direction.VERTICAL;

    public enum AllowedDirection {
        ONLY_VERTICAL, ONLY_HORIZONTAL, VERTICAL_AND_HORIZONTAL
    }

    public enum Direction {
        VERTICAL, HORIZONTAL
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(font);
        dest.writeInt(fontSize);
        dest.writeByte((byte) (nightMode ? 1 : 0));
        dest.writeInt(themeColor);
        dest.writeByte((byte) (showTts ? 1 : 0));
        dest.writeString(allowedDirection.toString());
        dest.writeString(direction.toString());
    }

    protected Config(Parcel in) {
        font = in.readInt();
        fontSize = in.readInt();
        nightMode = in.readByte() != 0;
        themeColor = in.readInt();
        showTts = in.readByte() != 0;
        allowedDirection = getAllowedDirectionFromString(LOG_TAG, in.readString());
        direction = getDirectionFromString(LOG_TAG, in.readString());
    }

    public Config() {
    }

    public Config(int font, int fontSize, boolean nightMode, int themeColor, boolean showTts,
                  AllowedDirection allowedDirection, Direction direction) {
        this.font = font;
        this.fontSize = fontSize;
        this.nightMode = nightMode;
        this.themeColor = themeColor;
        this.showTts = showTts;
        setAllowedDirection(allowedDirection);
        setDirection(direction);
    }

    public Config(JSONObject jsonObject) {
        font = jsonObject.optInt(CONFIG_FONT);
        fontSize = jsonObject.optInt(CONFIG_FONT_SIZE);
        nightMode = jsonObject.optBoolean(CONFIG_IS_NIGHTMODE);
        themeColor = jsonObject.optInt(CONFIG_IS_THEMECOLOR);
        showTts = jsonObject.optBoolean(CONFIG_IS_TTS);
        allowedDirection = getAllowedDirectionFromString(LOG_TAG,
                jsonObject.optString(CONFIG_ALLOWED_DIRECTION));
        direction = getDirectionFromString(LOG_TAG, jsonObject.optString(CONFIG_DIRECTION));
    }

    public static Direction getDirectionFromString(String LOG_TAG, String directionString) {

        switch (directionString) {
            case "VERTICAL":
                return Direction.VERTICAL;
            case "HORIZONTAL":
                return Direction.HORIZONTAL;
            default:
                Log.w(LOG_TAG, "-> Illegal argument directionString = " + directionString
                        + ", defaulting direction to " + Direction.VERTICAL.toString());
                return Direction.VERTICAL;
        }
    }

    public static AllowedDirection getAllowedDirectionFromString(String LOG_TAG,
                                                                 String allowedDirectionString) {

        switch (allowedDirectionString) {
            case "ONLY_VERTICAL":
                return AllowedDirection.ONLY_VERTICAL;
            case "ONLY_HORIZONTAL":
                return AllowedDirection.ONLY_HORIZONTAL;
            case "VERTICAL_AND_HORIZONTAL":
                return AllowedDirection.VERTICAL_AND_HORIZONTAL;
            default:
                Log.w(LOG_TAG, "-> Illegal argument allowedDirectionString = " + allowedDirectionString
                        + ", defaulting direction to " + AllowedDirection.ONLY_VERTICAL.toString());
                return AllowedDirection.ONLY_VERTICAL;
        }
    }

    public int getFont() {
        return font;
    }

    public Config setFont(int font) {
        this.font = font;
        return this;
    }

    public int getFontSize() {
        return fontSize;
    }

    public Config setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public boolean isNightMode() {
        return nightMode;
    }

    public Config setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
        return this;
    }

    public int getThemeColor() {
        return themeColor;
    }

    public Config setThemeColor(int themeColor) {
        this.themeColor = themeColor;
        return this;
    }

    public boolean isShowTts() {
        return showTts;
    }

    public Config setShowTts(boolean showTts) {
        this.showTts = showTts;
        return this;
    }

    public Direction getDirection() {
        return direction;
    }

    public Config setDirection(Direction direction) {

        if (allowedDirection == AllowedDirection.ONLY_VERTICAL && direction != Direction.VERTICAL) {
            this.direction = Direction.VERTICAL;

        } else if (allowedDirection == AllowedDirection.ONLY_HORIZONTAL && direction != Direction.HORIZONTAL) {
            this.direction = Direction.HORIZONTAL;

        } else {
            this.allowedDirection = AllowedDirection.VERTICAL_AND_HORIZONTAL;
            this.direction = direction;
        }

        return this;
    }

    public AllowedDirection getAllowedDirection() {
        return allowedDirection;
    }

    public Config setAllowedDirection(AllowedDirection allowedDirection) {

        this.allowedDirection = allowedDirection;

        if (allowedDirection == null) {
            this.allowedDirection = AllowedDirection.VERTICAL_AND_HORIZONTAL;

        } else if (allowedDirection == AllowedDirection.ONLY_VERTICAL && direction != Direction.VERTICAL) {
            direction = Direction.VERTICAL;
            Log.w(LOG_TAG, "-> Allowed direction is " + allowedDirection.toString()
                    + " so defaulting direction to " + direction.toString());

        } else if (allowedDirection == AllowedDirection.ONLY_HORIZONTAL && direction != Direction.HORIZONTAL) {
            direction = Direction.HORIZONTAL;
            Log.w(LOG_TAG, "-> Allowed direction is " + allowedDirection.toString()
                    + " so defaulting direction to " + direction.toString());
        }

        return this;
    }

    @Override
    public String toString() {
        return "Config{" +
                "font=" + font +
                ", fontSize=" + fontSize +
                ", nightMode=" + nightMode +
                ", themeColor=" + themeColor +
                ", showTts=" + showTts +
                ", allowedDirection=" + allowedDirection +
                ", direction=" + direction +
                '}';
    }
}


