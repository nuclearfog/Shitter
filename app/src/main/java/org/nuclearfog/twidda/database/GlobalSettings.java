package org.nuclearfog.twidda.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class GlobalSettings {

    private static GlobalSettings ourInstance;

    private SharedPreferences settings;

    private SimpleDateFormat sdf;

    private int background_color;
    private int font_color;
    private int highlight_color;
    private int tweet_color;

    private boolean loadImage;
    private boolean loadAnswer;
    private boolean loggedIn;
    private boolean customWorldId;
    private int row;

    private int woeId;
    private int woeIdPos;

    private String key1, key2;
    private long userId;

    private GlobalSettings(Context context) {
        settings = context.getSharedPreferences("settings", 0);
        woeId = settings.getInt("world_id", 1);
        customWorldId = settings.getBoolean("custom_woeId", false);
        woeIdPos = settings.getInt("world_id_pos", 0);
        background_color = settings.getInt("background_color", 0xff0f114a);
        highlight_color = settings.getInt("highlight_color", 0xffff00ff);
        font_color = settings.getInt("font_color", 0xffffffff);
        tweet_color = settings.getInt("tweet_color", 0xff19aae8);
        row = settings.getInt("preload", 20);
        loadImage = settings.getBoolean("image_load", true);
        loadAnswer = settings.getBoolean("answer_load", true);
        loggedIn = settings.getBoolean("login", false);
        key1 = settings.getString("key1", "");
        key2 = settings.getString("key2", "");
        userId = settings.getLong("userID", -1L);
        sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.getDefault());
    }

    /**
     * Get Singleton instance
     *
     * @param context Application Context needed for Shared preferences
     * @return instance of this class
     */
    public static GlobalSettings getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new GlobalSettings(context);
        }
        return ourInstance;
    }

    /**
     * Get get background color
     *
     * @return color value
     */
    public int getBackgroundColor() {
        return background_color;
    }

    /**
     * Set background color
     *
     * @param color background color value
     */
    public void setBackgroundColor(int color) {
        background_color = color;
        Editor edit = settings.edit();
        edit.putInt("background_color", color);
        edit.apply();
    }

    /**
     * get font color
     *
     * @return font color value
     */
    public int getFontColor() {
        return font_color;
    }

    /**
     * set font color
     *
     * @param color font color value
     */
    public void setFontColor(int color) {
        font_color = color;
        Editor edit = settings.edit();
        edit.putInt("font_color", color);
        edit.apply();
    }

    /**
     * get highlight color
     *
     * @return highlight color value
     */
    public int getHighlightColor() {
        return highlight_color;
    }

    /**
     * set highlight color
     *
     * @param color highlight color value
     */
    public void setHighlightColor(int color) {
        highlight_color = color;
        Editor edit = settings.edit();
        edit.putInt("highlight_color", color);
        edit.apply();
    }

    /**
     * get message window color
     *
     * @return color value
     */
    public int getPopupColor() {
        return tweet_color;
    }

    /**
     * set message window color
     *
     * @param color color value
     */
    public void setPopupColor(int color) {
        tweet_color = color;
        Editor edit = settings.edit();
        edit.putInt("tweet_color", color);
        edit.apply();
    }

    /**
     * image loading enabled
     *
     * @return true if enabled
     */
    public boolean getImageLoad() {
        return loadImage;
    }

    /**
     * enable/disable image load
     *
     * @param image true if enabled
     */
    public void setImageLoad(boolean image) {
        loadImage = image;
        Editor edit = settings.edit();
        edit.putBoolean("image_load", image);
        edit.apply();
    }

    /**
     * answer loading enabled
     *
     * @return if answer loading is enabled
     */
    public boolean getAnswerLoad() {
        return loadAnswer;
    }

    /**
     * enable/disable answer loading
     *
     * @param loadAnswer true if enabled
     */
    public void setAnswerLoad(boolean loadAnswer) {
        this.loadAnswer = loadAnswer;
        Editor edit = settings.edit();
        edit.putBoolean("answer_load", loadAnswer);
        edit.apply();
    }

    /**
     * get World ID for trends
     *
     * @return World ID
     */
    public int getWoeId() {
        return woeId;
    }

    /**
     * set World ID for trends
     *
     * @param id World ID
     */
    public void setWoeId(long id) {
        woeId = (int) id;
        Editor edit = settings.edit();
        edit.putInt("world_id", (int) id);
        edit.apply();
    }

    /**
     * return position of the world id dropdown list
     *
     * @return position
     */
    public int getWoeIdSelection() {
        return woeIdPos;
    }

    /**
     * set last position of the dropdown list
     *
     * @param pos position of the last selection
     */
    public void setWoeIdSelection(int pos) {
        woeIdPos = pos;
        Editor edit = settings.edit();
        edit.putInt("world_id_pos", pos);
        edit.apply();
    }

    /**
     * Check if custom World ID is set
     *
     * @return if custom world ID is set
     */
    public boolean getCustomWidSet() {
        return customWorldId;
    }

    /**
     * Set custom World ID
     *
     * @param customWoeId true if Custom world ID is set
     */
    public void setCustomWidSet(boolean customWoeId) {
        customWorldId = customWoeId;
        Editor edit = settings.edit();
        edit.putBoolean("custom_woeId", customWoeId);
        edit.apply();
    }

    /**
     * get loading limit of tweets/users
     *
     * @return max numbers of tweets/users should be loaded
     */
    public int getRowLimit() {
        return row;
    }

    /**
     * set limit of tweets/users
     *
     * @param limit max numbers of tweets/users
     */
    public void setRowLimit(int limit) {
        row = limit;
        Editor edit = settings.edit();
        edit.putInt("preload", limit);
        edit.apply();
    }

    /**
     * Check if current user is logged in
     *
     * @return true if current user is logged in
     */
    public boolean getLogin() {
        return loggedIn;
    }

    /**
     * get Access tokens
     *
     * @return access tokens
     */
    public String[] getKeys() {
        String[] out = new String[2];
        out[0] = key1;
        out[1] = key2;
        return out;
    }

    /**
     * get current users ID
     *
     * @return User ID
     */
    public long getUserId() {
        return userId;
    }

    /**
     * get Datetime Formatter for the current location
     *
     * @return Datetime Formatter
     */
    public SimpleDateFormat getDateFormatter() {
        return sdf;
    }

    /**
     * Set Access tokens and user ID
     *
     * @param key1   1st access token
     * @param key2   2nd access token
     * @param userId User ID
     */
    public void setConnection(String key1, String key2, Long userId) {
        loggedIn = true;
        this.key1 = key1;
        this.key2 = key2;
        this.userId = userId;
        Editor e = settings.edit();
        e.putBoolean("login", true);
        e.putLong("userID", userId);
        e.putString("key1", key1);
        e.putString("key2", key2);
        e.apply();
    }

    /**
     * Remove all user content from Shared Preferences
     */
    public void logout() {
        loggedIn = false;
        Editor e = settings.edit();
        e.putBoolean("login", false);
        e.remove("userID").remove("key1").remove("key2").remove("custom_woeId")
                .remove("image_load").remove("preload").remove("world_id_pos")
                .remove("world_id").remove("tweet_color").remove("highlight_color")
                .remove("highlight_color").remove("font_color").remove("background_color");
        e.apply();
    }
}