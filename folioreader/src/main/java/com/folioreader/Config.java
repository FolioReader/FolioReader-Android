package com.folioreader;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import org.json.JSONObject;
import timber.log.Timber;

/**
 * Configuration class for FolioReader.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class Config implements Parcelable {
    public static final String INTENT_CONFIG = "config";
    public static final String EXTRA_OVERRIDE_CONFIG = "com.folioreader.extra.OVERRIDE_CONFIG";
    public static final String CONFIG_FONT = "font";
    public static final String CONFIG_FONT_SIZE = "font_size";
    public static final String CONFIG_IS_NIGHT_MODE = "is_night_mode";
    public static final String CONFIG_THEME_COLOR_INT = "theme_color_int";
    public static final String CONFIG_IS_TTS = "is_tts";
    public static final String CONFIG_ALLOWED_DIRECTION = "allowed_direction";
    public static final String CONFIG_DIRECTION = "direction";
    private static final AllowedDirection DEFAULT_ALLOWED_DIRECTION = AllowedDirection.ONLY_VERTICAL;
    private static final Direction DEFAULT_DIRECTION = Direction.VERTICAL;
    private static final int DEFAULT_THEME_COLOR_INT =
            ContextCompat.getColor(AppContext.get(), R.color.default_theme_accent_color);

    private int font = 3;
    private int fontSize = 2;
    private boolean nightMode;
    @ColorInt
    private int themeColor = DEFAULT_THEME_COLOR_INT;
    private boolean showTts = true;
    private AllowedDirection allowedDirection = DEFAULT_ALLOWED_DIRECTION;
    private Direction direction = DEFAULT_DIRECTION;

    /**
     * Reading modes available
     */
    public enum AllowedDirection {
        ONLY_VERTICAL, ONLY_HORIZONTAL, VERTICAL_AND_HORIZONTAL
    }

    /**
     * Reading directions
     */
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
        allowedDirection = getAllowedDirectionFromString(in.readString());
        direction = getDirectionFromString(in.readString());
    }

    public Config() {
    }

    public Config(JSONObject jsonObject) {
        font = jsonObject.optInt(CONFIG_FONT);
        fontSize = jsonObject.optInt(CONFIG_FONT_SIZE);
        nightMode = jsonObject.optBoolean(CONFIG_IS_NIGHT_MODE);
        themeColor = getValidColorInt(jsonObject.optInt(CONFIG_THEME_COLOR_INT));
        showTts = jsonObject.optBoolean(CONFIG_IS_TTS);
        allowedDirection = getAllowedDirectionFromString(
                jsonObject.optString(CONFIG_ALLOWED_DIRECTION));
        direction = getDirectionFromString(jsonObject.optString(CONFIG_DIRECTION));
    }

    public static Direction getDirectionFromString(@Nullable String directionString) {

        switch (directionString == null? "null" : directionString) {
            case "VERTICAL":
                return Direction.VERTICAL;
            case "HORIZONTAL":
                return Direction.HORIZONTAL;
            default:
                Timber.w("-> Illegal argument directionString = `%s`, defaulting direction to %s",
                        directionString, DEFAULT_DIRECTION);
                return DEFAULT_DIRECTION;
        }
    }

    public static AllowedDirection getAllowedDirectionFromString(@Nullable String allowedDirectionString) {

        switch (allowedDirectionString == null? "null" : allowedDirectionString) {
            case "ONLY_VERTICAL":
                return AllowedDirection.ONLY_VERTICAL;
            case "ONLY_HORIZONTAL":
                return AllowedDirection.ONLY_HORIZONTAL;
            case "VERTICAL_AND_HORIZONTAL":
                return AllowedDirection.VERTICAL_AND_HORIZONTAL;
            default:
                Timber.w("-> Illegal argument allowedDirectionString = `%s`, defaulting allowedDirection to %s",
                        allowedDirectionString, DEFAULT_ALLOWED_DIRECTION);
                return DEFAULT_ALLOWED_DIRECTION;
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

    @ColorInt
    private int getValidColorInt(@ColorInt int colorInt) {
        if (colorInt >= 0) {
            Timber.w("-> getValidColorInt -> Invalid argument colorInt = %d, Returning DEFAULT_THEME_COLOR_INT = %d",
                    colorInt, DEFAULT_THEME_COLOR_INT);
            return DEFAULT_THEME_COLOR_INT;
        }
        return colorInt;
    }

    @ColorInt
    public int getThemeColor() {
        return themeColor;
    }

    public Config setThemeColorRes(@ColorRes int colorResId) {
        try {
            this.themeColor = ContextCompat.getColor(AppContext.get(), colorResId);
        } catch (Resources.NotFoundException e) {
            Timber.w(e, "-> setThemeColorRes -> ");
            Timber.w("-> setThemeColorRes -> Defaulting themeColor to %d",
                    DEFAULT_THEME_COLOR_INT);
            this.themeColor = DEFAULT_THEME_COLOR_INT;
        }
        return this;
    }

    public Config setThemeColorInt(@ColorInt int colorInt) {
        this.themeColor = getValidColorInt(colorInt);
        return this;
    }

    public boolean isShowTts() {
        return showTts;
    }

    public Config setShowTts(boolean showTts) {
        this.showTts = showTts;
        return this;
    }

    public AllowedDirection getAllowedDirection() {
        return allowedDirection;
    }

    /**
     * Set reading direction mode options for users. This method should be called before
     * {@link #setDirection(Direction)} as it has higher preference.
     *
     * @param allowedDirection reading direction mode options for users. Setting to
     *                         {@link AllowedDirection#VERTICAL_AND_HORIZONTAL} users will have
     *                         choice to select the reading direction at runtime.
     */
    public Config setAllowedDirection(AllowedDirection allowedDirection) {

        this.allowedDirection = allowedDirection;

        if (allowedDirection == null) {
            this.allowedDirection = DEFAULT_ALLOWED_DIRECTION;
            direction = DEFAULT_DIRECTION;
            Timber.w("-> allowedDirection cannot be null, defaulting allowedDirection to %s and direction to %s",
                    DEFAULT_ALLOWED_DIRECTION, DEFAULT_DIRECTION);

        } else if (allowedDirection == AllowedDirection.ONLY_VERTICAL &&
                direction != Direction.VERTICAL) {
            direction = Direction.VERTICAL;
            Timber.w("-> allowedDirection is %s, defaulting direction to %s",
                    allowedDirection, direction);

        } else if (allowedDirection == AllowedDirection.ONLY_HORIZONTAL &&
                direction != Direction.HORIZONTAL) {
            direction = Direction.HORIZONTAL;
            Timber.w("-> allowedDirection is %s, defaulting direction to %s",
                    allowedDirection, direction);
        }

        return this;
    }

    public Direction getDirection() {
        return direction;
    }

    /**
     * Set reading direction. This method should be called after
     * {@link #setAllowedDirection(AllowedDirection)} as it has lower preference.
     *
     * @param direction reading direction.
     */
    public Config setDirection(Direction direction) {

        if (allowedDirection == AllowedDirection.VERTICAL_AND_HORIZONTAL && direction == null) {
            this.direction = DEFAULT_DIRECTION;
            Timber.w("-> direction cannot be `null` when allowedDirection is %s, defaulting direction to %s",
                    allowedDirection, this.direction);

        } else if (allowedDirection == AllowedDirection.ONLY_VERTICAL &&
                direction != Direction.VERTICAL) {
            this.direction = Direction.VERTICAL;
            Timber.w("-> direction cannot be `%s` when allowedDirection is %s, defaulting direction to %s",
                    direction, allowedDirection, this.direction);

        } else if (allowedDirection == AllowedDirection.ONLY_HORIZONTAL &&
                direction != Direction.HORIZONTAL) {
            this.direction = Direction.HORIZONTAL;
            Timber.w("-> direction cannot be `%s` when allowedDirection is %s, defaulting direction to %s",
                    direction, allowedDirection, this.direction);

        } else {
            this.direction = direction;
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


