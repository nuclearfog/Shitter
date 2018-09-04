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
        loggedIn = settings.getBoolean("login", false);
        key1 = settings.getString("key1", "");
        key2 = settings.getString("key2", "");
        userId = settings.getLong("userID", -1L);
        sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.GERMANY); // TODO editable date format
    }

    public static GlobalSettings getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new GlobalSettings(context);
        }
        return ourInstance;
    }

    public int getBackgroundColor() {
        return background_color;
    }

    public void setBackgroundColor(int color) {
        Editor edit = settings.edit();
        edit.putInt("background_color", color);
        background_color = color;
        edit.apply();
    }

    public int getFontColor() {
        return font_color;
    }

    public void setFontColor(int color) {
        Editor edit = settings.edit();
        edit.putInt("font_color", color);
        font_color = color;
        edit.apply();
    }

    public int getHighlightColor() {
        return highlight_color;
    }

    public void setHighlightColor(int color) {
        Editor edit = settings.edit();
        edit.putInt("highlight_color", color);
        highlight_color = color;
        edit.apply();
    }

    public int getTweetColor() {
        return tweet_color;
    }

    public void setTweetColor(int color) {
        Editor edit = settings.edit();
        edit.putInt("tweet_color", color);
        tweet_color = color;
        edit.apply();
    }

    public boolean loadImages() {
        return loadImage;
    }

    public int getWoeId() {
        return woeId;
    }

    public void setWoeId(long id) {
        Editor edit = settings.edit();
        edit.putInt("world_id", (int) id);
        woeId = (int) id;
        edit.apply();
    }

    public boolean customWoeIdset() {
        return customWorldId;
    }

    public int getWoeIdSelection() {
        return woeIdPos;
    }

    public void setWoeIdSelection(int pos) {
        Editor edit = settings.edit();
        edit.putInt("world_id_pos", pos);
        woeIdPos = pos;
        edit.apply();
    }

    public int getRowLimit() {
        return row;
    }

    public void setRowLimit(int limit) {
        Editor edit = settings.edit();
        edit.putInt("preload", limit);
        row = limit;
        edit.apply();
    }

    public boolean getLogin() {
        return loggedIn;
    }

    public String[] getKeys() {
        String out[] = new String[2];
        out[0] = key1;
        out[1] = key2;
        return out;
    }

    public long getUserId() {
        return userId;
    }

    public SimpleDateFormat getDateFormatter() {
        return sdf;
    }

    public void setImageLoad(boolean image) {
        Editor edit = settings.edit();
        edit.putBoolean("image_load", image);
        loadImage = image;
        edit.apply();
    }

    public void setCustomWoeId(boolean customWoeId) {
        Editor edit = settings.edit();
        edit.putBoolean("custom_woeId", customWoeId);
        customWorldId = customWoeId;
        edit.apply();
    }

    public void setConnection(String key1, String key2, Long userId) {
        Editor e = settings.edit();
        loggedIn = true;
        this.key1 = key1;
        this.key2 = key2;
        this.userId = userId;
        e.putBoolean("login", true);
        e.putLong("userID", userId);
        e.putString("key1", key1);
        e.putString("key2", key2);
        e.apply();
    }
}