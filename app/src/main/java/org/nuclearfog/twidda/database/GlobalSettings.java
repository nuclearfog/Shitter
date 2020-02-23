package org.nuclearfog.twidda.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;

import org.nuclearfog.twidda.backend.items.TrendLocation;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import static android.content.Context.MODE_PRIVATE;

public final class GlobalSettings {

    public static final Typeface[] fonts = {Typeface.DEFAULT, Typeface.MONOSPACE,
            Typeface.SERIF, Typeface.create("sans-serif-thin", Typeface.NORMAL)};
    public static final String[] fontnames = {"Default", "Monospace", "Serif", "sans-serif-thin"};

    private static final String NAME = "settings";
    private static final GlobalSettings ourInstance = new GlobalSettings();

    private SharedPreferences settings;
    private TrendLocation location;
    private String key1, key2;
    private boolean loadImage;
    private boolean loadAnswer;
    private boolean loggedIn;
    private int indexFont;
    private int background_color;
    private int font_color;
    private int highlight_color;
    private int tweet_color;
    private int row;
    private long userId;

    private String proxyHost, proxyPort;
    private String proxyUser, proxyPass;

    private GlobalSettings() {
    }

    /**
     * Get Singleton instance
     *
     * @param context Application Context needed for Shared preferences
     * @return instance of this class
     */
    public static GlobalSettings getInstance(Context context) {
        if (ourInstance.settings == null) {
            ourInstance.settings = context.getSharedPreferences(NAME, MODE_PRIVATE);
            ourInstance.initialize();
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
     * image load loading enabled
     *
     * @return true if enabled
     */
    public boolean getImageLoad() {
        return loadImage;
    }

    /**
     * enable/disable image load load
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
     * @return true if enabled
     */
    public boolean getAnswerLoad() {
        return loadAnswer;
    }

    /**
     * enable/disable answer load load
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
     * get selected location information
     *
     * @return saved location information
     */
    public TrendLocation getTrendLocation() {
        return location;
    }

    /**
     * set selected location information
     *
     * @param location location information
     */
    public void setTrendLocation(TrendLocation location) {
        this.location = location;
        Editor edit = settings.edit();
        edit.putInt("world_id", location.getWoeId());
        edit.putString("location", location.getName());
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
     * return font type
     *
     * @return font family
     */
    public Typeface getFontFace() {
        return fonts[indexFont];
    }

    /**
     * get font position
     *
     * @return font index
     */
    public int getFont() {
        return indexFont;
    }

    /**
     * set font type
     *
     * @param index index of font type in array
     */
    public void setFont(int index) {
        indexFont = index;
        Editor edit = settings.edit();
        edit.putInt("index_font", index);
        edit.apply();
    }

    /**
     * get proxy address
     *
     * @return proxy address
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * set proxy address
     *
     * @param proxyHost address of proxy
     * @param proxyPort port of proxy
     */
    public void setProxyServer(String proxyHost, String proxyPort) {
        Editor edit = settings.edit();
        if (proxyHost.trim().isEmpty()) {
            this.proxyHost = "";
            this.proxyPort = "";
            edit.putString("proxy_addr", "");
            edit.putString("proxy_port", "");
        } else {
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            edit.putString("proxy_addr", proxyHost);
            edit.putString("proxy_port", proxyPort);
        }
        edit.apply();
    }

    /**
     * get proxy port
     *
     * @return proxy port string
     */
    public String getProxyPort() {
        return proxyPort;
    }

    /**
     * get proxy user login
     *
     * @return username
     */
    public String getProxyUser() {
        return proxyUser;
    }

    /**
     * set proxy user login
     *
     * @param proxyUser proxy username
     * @param proxyPass proxy password
     */
    public void setProxyLogin(String proxyUser, String proxyPass) {
        Editor edit = settings.edit();
        if (proxyUser.trim().isEmpty() || proxyHost.trim().isEmpty()) {
            this.proxyUser = "";
            this.proxyPass = "";
            edit.putString("proxy_user", "");
            edit.putString("proxy_pass", "");
        } else {
            this.proxyUser = proxyUser;
            this.proxyPass = proxyPass;
            edit.putString("proxy_user", proxyUser);
            edit.putString("proxy_pass", proxyPass);
        }
        edit.apply();
    }

    /**
     * get proxy password
     *
     * @return login password
     */
    public String getProxyPass() {
        return proxyPass;
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
     * set JAVA VM proxy
     */
    public void configureProxy() {
        try {
            if (proxyHost.trim().isEmpty()) {
                System.clearProperty("https.proxyHost");
            } else {
                System.setProperty("https.proxyHost", proxyHost);
            }
            if (proxyPort.trim().isEmpty()) {
                System.clearProperty("https.proxyPort");
            } else {
                System.setProperty("https.proxyPort", proxyPort);
            }
            if (proxyUser.trim().isEmpty()) {
                System.clearProperty("https.proxyUser");
            }
            if (proxyPass.trim().isEmpty()) {
                System.clearProperty("https.proxyPassword");
            }
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    System.setProperty("https.proxyUser", proxyUser);
                    System.setProperty("https.proxyPassword", proxyPass);
                    return new PasswordAuthentication(proxyUser, proxyPass.toCharArray());
                }
            });
        } catch (SecurityException sErr) {
            sErr.printStackTrace();
        }
    }

    /**
     * Remove all user content from Shared Preferences
     */
    public void logout() {
        settings.edit().clear().apply();
        initialize();
    }

    /**
     * Init setting values
     */
    private void initialize() {
        background_color = settings.getInt("background_color", 0xff0f114a);
        highlight_color = settings.getInt("highlight_color", 0xffff00ff);
        font_color = settings.getInt("font_color", 0xffffffff);
        tweet_color = settings.getInt("tweet_color", 0xff19aae8);
        indexFont = settings.getInt("index_font", 0);
        row = settings.getInt("preload", 20);
        loadImage = settings.getBoolean("image_load", true);
        loadAnswer = settings.getBoolean("answer_load", true);
        loggedIn = settings.getBoolean("login", false);
        key1 = settings.getString("key1", "");
        key2 = settings.getString("key2", "");
        userId = settings.getLong("userID", -1L);
        proxyHost = settings.getString("proxy_addr", "");
        proxyPort = settings.getString("proxy_port", "");
        proxyUser = settings.getString("proxy_user", "");
        proxyPass = settings.getString("proxy_pass", "");
        String place = settings.getString("location", "Worldwide");
        int woeId = settings.getInt("world_id", 1);
        location = new TrendLocation(place, woeId);
        configureProxy();
    }
}