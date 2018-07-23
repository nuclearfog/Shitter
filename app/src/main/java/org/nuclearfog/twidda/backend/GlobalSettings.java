package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class GlobalSettings {

    private static GlobalSettings ourInstance;

    private SharedPreferences settings;

    private int background_color;
    private int font_color;
    private int highlight_color;
    private int tweet_color;

    private boolean loadImage;
    private int row;

    private int woeid;



    private GlobalSettings(Context context) {
        settings = context.getSharedPreferences("settings",0);
        woeid = settings.getInt("world_id",23424829);
        background_color = settings.getInt("background_color", 0xff0f114a);
        highlight_color = settings.getInt("highlight_color", 0xffff00ff);
        font_color = settings.getInt("font_color", 0xffffffff);
        tweet_color = settings.getInt("tweet_color",0xff19aae8);
        row = settings.getInt("preload",20);
        loadImage = settings.getBoolean("image_load", true);
    }

    public int getBackgroundColor() {
        return background_color;
    }

    public int getFontColor() {
        return font_color;
    }

    public int getHighlightColor() {
        return highlight_color;
    }

    public int getTweetColor() {
        return tweet_color;
    }


    public boolean loadImages() {
        return loadImage;
    }

    public int getWoeId() {
        return woeid;
    }

    public int getRowLimit() {
        return row;
    }


    public void setBackgroundColor(int color) {
        Editor edit  = settings.edit();
        edit.putInt("background_color",color);
        background_color = color;
        edit.apply();
    }

    public void setFontColor(int color) {
        Editor edit  = settings.edit();
        edit.putInt("font_color",color);
        font_color = color;
        edit.apply();
    }

    public void setHighlightColor(int color) {
        Editor edit  = settings.edit();
        edit.putInt("highlight_color",color);
        highlight_color = color;
        edit.apply();
    }

    public void setTweetColor(int color) {
        Editor edit  = settings.edit();
        edit.putInt("tweet_color",color);
        tweet_color = color;
        edit.apply();
    }

    public void setImageLoad(boolean image) {
        Editor edit  = settings.edit();
        edit.putBoolean("image_load", image);
        loadImage = image;
        edit.apply();
    }

    public void setWoeId(int id) {
        Editor edit  = settings.edit();
        edit.putInt("world_id", id);
        woeid = id;
        edit.apply();
    }

    public void setRowLimit(int limit) {
        Editor edit  = settings.edit();
        edit.putInt("preload", limit);
        row = limit;
        edit.apply();
    }


    public static GlobalSettings getInstance(Context context) {
        if(ourInstance == null) {
            ourInstance = new GlobalSettings(context);
        }
        return ourInstance;
    }
}