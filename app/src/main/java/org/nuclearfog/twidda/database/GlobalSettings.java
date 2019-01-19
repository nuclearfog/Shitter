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
        background_color = color;
        Editor edit = settings.edit();
        edit.putInt("background_color", color);
        edit.apply();
    }

    public int getFontColor() {
        return font_color;
    }

    public void setFontColor(int color) {
        font_color = color;
        Editor edit = settings.edit();
        edit.putInt("font_color", color);
        edit.apply();
    }

    public int getHighlightColor() {
        return highlight_color;
    }

    public void setHighlightColor(int color) {
        highlight_color = color;
        Editor edit = settings.edit();
        edit.putInt("highlight_color", color);
        edit.apply();
    }

    public int getTweetColor() {
        return tweet_color;
    }

    public void setTweetColor(int color) {
        tweet_color = color;
        Editor edit = settings.edit();
        edit.putInt("tweet_color", color);
        edit.apply();
    }

    public boolean getImageLoad() {
        return loadImage;
    }

    public void setImageLoad(boolean image) {
        loadImage = image;
        Editor edit = settings.edit();
        edit.putBoolean("image_load", image);
        edit.apply();
    }

    public boolean getAnswerLoad() {
        return loadAnswer;
    }

    public void setAnswerLoad(boolean loadAnswer) {
        this.loadAnswer = loadAnswer;
        Editor edit = settings.edit();
        edit.putBoolean("answer_load", loadAnswer);
        edit.apply();
    }

    public int getWoeId() {
        return woeId;
    }

    public void setWoeId(long id) {
        woeId = (int) id;
        Editor edit = settings.edit();
        edit.putInt("world_id", (int) id);
        edit.apply();
    }

    public int getWoeIdSelection() {
        return woeIdPos;
    }

    public void setWoeIdSelection(int pos) {
        woeIdPos = pos;
        Editor edit = settings.edit();
        edit.putInt("world_id_pos", pos);
        edit.apply();
    }

    public boolean getCustomWidSet() {
        return customWorldId;
    }

    public void setCustomWidSet(boolean customWoeId) {
        customWorldId = customWoeId;
        Editor edit = settings.edit();
        edit.putBoolean("custom_woeId", customWoeId);
        edit.apply();
    }

    public int getRowLimit() {
        return row;
    }

    public void setRowLimit(int limit) {
        row = limit;
        Editor edit = settings.edit();
        edit.putInt("preload", limit);
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