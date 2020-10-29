package org.nuclearfog.twidda.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.items.TrendLocation;

import static android.content.Context.MODE_PRIVATE;
import static android.graphics.Typeface.DEFAULT;
import static android.graphics.Typeface.MONOSPACE;
import static android.graphics.Typeface.NORMAL;
import static android.graphics.Typeface.SERIF;

/**
 * This class manages app settings
 */
public class GlobalSettings {

    /**
     * link suffix for low resolution profile images
     */
    public static final String PROFILE_IMG_LOW_RES = "_mini";

    /**
     * link suffix for high resolution profile images
     */
    public static final String PROFILE_IMG_HIGH_RES = "_bigger";

    /**
     * link suffix for low resolution banner images
     */
    public static final String BANNER_IMG_LOW_RES = "/300x100";

    /**
     * link suffix for standard banner image resolution
     */
    public static final String BANNER_IMG_MID_RES = "/600x200";

    /**
     * link suffix for high resolution banner images
     */
    public static final String BANNER_IMG_HIGH_RES = "/1500x500";

    /**
     * custom font families from android system
     */
    public static final Typeface[] FONTS = {DEFAULT, MONOSPACE, SERIF, Typeface.create("sans-serif-thin", NORMAL)};

    /**
     * names of the font types {@link #FONTS}
     */
    public static final String[] FONT_NAMES = {"Default", "Monospace", "Serif", "sans-serif-thin"};

    // Setting names stored in SharedPreference
    private static final String BACKGROUND_COLOR = "background_color";
    private static final String HIGHLIGHT_COLOR = "highlight_color";
    private static final String FONT_COLOR = "font_color";
    private static final String POPUP_COLOR = "tweet_color";
    private static final String INDEX_FONT = "index_font";
    private static final String LIST_SIZE = "preload";
    private static final String IMAGE_LOAD = "image_load";
    private static final String IMAGE_QUALITY = "image_hq";
    private static final String ANSWER_LOAD = "answer_load";
    private static final String LOGGED_IN = "login";
    private static final String AUTH_KEY1 = "key1";
    private static final String AUTH_KEY2 = "key2";
    private static final String USER_ID = "userID";
    private static final String PROXY_SET = "proxy_enabled";
    private static final String AUTH_SET = "proxy_auth_set";
    private static final String PROXY_ADDR = "proxy_addr";
    private static final String PROXY_PORT = "proxy_port";
    private static final String PROXY_USER = "proxy_user";
    private static final String PROXY_PASS = "proxy_pass";
    private static final String TREND_LOC = "location";
    private static final String TREND_ID = "world_id";
    private static final String NAME = "settings";

    // Default App settings
    @IntRange(from = 0, to = 3)
    private static final int DEFAULT_FONT_INDEX = 0;
    @IntRange(from = 0, to = 100)
    private static final int DEFAULT_LIST_SIZE = 20;
    private static final int DEFAULT_BACKGROUND_COLOR = 0xff0f114a;
    private static final int DEFAULT_HIGHLIGHT_COLOR = 0xffff00ff;
    private static final int DEFAULT_FONT_COLOR = 0xffffffff;
    private static final int DEFAULT_POPUP_COLOR = 0xff19aae8;
    private static final int DEFAULT_LOCATION_WOEID = 1;
    private static final String DEFAULT_LOCATION_NAME = "Worldwide";
    private static final boolean DEFAULT_DATA_USAGE = true;

    private static final GlobalSettings ourInstance = new GlobalSettings();

    private SharedPreferences settings;
    private TrendLocation location;
    private String key1, key2;
    private boolean loadImage;
    private boolean hqImages;
    private boolean loadAnswer;
    private boolean loggedIn;
    private boolean isProxyEnabled;
    private boolean isProxyAuthSet;
    private int indexFont;
    private int background_color;
    private int font_color;
    private int highlight_color;
    private int popup_color;
    private int listSize;
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
    public static GlobalSettings getInstance(@NonNull Context context) {
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
        edit.putInt(BACKGROUND_COLOR, color);
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
        edit.putInt(FONT_COLOR, color);
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
        edit.putInt(HIGHLIGHT_COLOR, color);
        edit.apply();
    }

    /**
     * get message window color
     *
     * @return color value
     */
    public int getPopupColor() {
        return popup_color;
    }

    /**
     * set message window color
     *
     * @param color color value
     */
    public void setPopupColor(int color) {
        popup_color = color;

        Editor edit = settings.edit();
        edit.putInt(POPUP_COLOR, color);
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
     * @param enable true if enabled
     */
    public void setImageLoad(boolean enable) {
        loadImage = enable;

        Editor edit = settings.edit();
        edit.putBoolean(IMAGE_LOAD, enable);
        edit.apply();
    }

    /**
     * sets image quality
     *
     * @return true if thumbnails should be in high resolution
     */
    public boolean getImageQuality() {
        return hqImages;
    }

    /**
     * returns the twitter image suffix depending on the resolution
     *
     * @return suffix string
     */
    public String getImageSuffix() {
        if (hqImages)
            return PROFILE_IMG_HIGH_RES;
        return PROFILE_IMG_LOW_RES;
    }

    /**
     * returns the suffix for the banner image link
     *
     * @return suffix string
     */
    public String getBannerSuffix() {
        if (hqImages)
            return BANNER_IMG_MID_RES;
        return BANNER_IMG_LOW_RES;
    }

    /**
     * sets the image quality
     *
     * @param enable true if small thumbnails should be in high resolution
     */
    public void setHighQualityImage(boolean enable) {
        hqImages = enable;
        Editor edit = settings.edit();
        edit.putBoolean(IMAGE_QUALITY, enable);
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
        edit.putBoolean(ANSWER_LOAD, loadAnswer);
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
        edit.putInt(TREND_ID, location.getWoeId());
        edit.putString(TREND_LOC, location.getName());
        edit.apply();
    }


    /**
     * get loading limit of tweets/users
     *
     * @return max numbers of tweets/users should be loaded
     */
    public int getListSize() {
        return listSize;
    }

    /**
     * set limit of tweets/users
     *
     * @param listSize max numbers of tweets/users
     */
    public void setListSize(int listSize) {
        this.listSize = listSize;

        Editor edit = settings.edit();
        edit.putInt(LIST_SIZE, listSize);
        edit.apply();
    }

    /**
     * return font type
     *
     * @return font family
     */
    public Typeface getFontFace() {
        return FONTS[indexFont];
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
     * @param indexFont index of font type in array
     */
    public void setFont(int indexFont) {
        this.indexFont = indexFont;

        Editor edit = settings.edit();
        edit.putInt(INDEX_FONT, indexFont);
        edit.apply();
    }

    /**
     * set proxy address
     *
     * @param proxyHost address of proxy
     * @param proxyPort port of proxy
     */
    public void setProxyServer(String proxyHost, String proxyPort, String proxyUser, String proxyPass) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;

        Editor edit = settings.edit();
        edit.putString(PROXY_ADDR, proxyHost);
        edit.putString(PROXY_PORT, proxyPort);
        edit.putString(PROXY_USER, proxyUser);
        edit.putString(PROXY_PASS, proxyPass);
        edit.apply();
    }

    /**
     * Remove Proxy settings
     */
    public void clearProxyServer() {
        isProxyEnabled = false;
        isProxyAuthSet = false;
        this.proxyHost = "";
        this.proxyPort = "";
        this.proxyUser = "";
        this.proxyPass = "";

        Editor edit = settings.edit();
        edit.putBoolean(PROXY_SET, false);
        edit.putBoolean(AUTH_SET, false);
        edit.putString(PROXY_ADDR, "");
        edit.putString(PROXY_PORT, "");
        edit.putString(PROXY_USER, "");
        edit.putString(PROXY_PASS, "");
        edit.apply();
    }

    /**
     * set proxy server connection enabled
     *
     * @param enable true if proxy connection is set
     */
    public void setProxyEnabled(boolean enable) {
        isProxyEnabled = enable;

        Editor edit = settings.edit();
        edit.putBoolean(PROXY_SET, enable);
        edit.apply();
    }

    /**
     * set proxy authentication enabled
     *
     * @param enable true if proxy auth is enabled
     */
    public void setProxyAuthSet(boolean enable) {
        isProxyAuthSet = enable;

        Editor edit = settings.edit();
        edit.putBoolean(AUTH_SET, enable);
        edit.apply();
    }

    /**
     * get proxy address
     *
     * @return proxy address
     */
    public String getProxyHost() {
        if (isProxyEnabled)
            return proxyHost;
        return "";
    }

    /**
     * get proxy port
     *
     * @return proxy port string
     */
    public String getProxyPort() {
        if (isProxyEnabled)
            return proxyPort;
        return "";
    }

    /**
     * get proxy port
     *
     * @return proxy port integer
     */
    public int getProxyPortNumber() {
        if (isProxyEnabled && !proxyPort.isEmpty() && proxyPort.length() < 6)
            return Integer.parseInt(proxyPort);
        return 0;
    }

    /**
     * get proxy user login
     *
     * @return username
     */
    public String getProxyUser() {
        if (isProxyAuthSet) {
            return proxyUser;
        }
        return "";
    }

    /**
     * get proxy password
     *
     * @return login password
     */
    public String getProxyPass() {
        if (isProxyAuthSet) {
            return proxyPass;
        }
        return "";
    }

    /**
     * check if proxy connection is set
     *
     * @return true if proxy is set
     */
    public boolean isProxyEnabled() {
        return isProxyEnabled;
    }

    /**
     * check kif proxy authentication is set
     *
     * @return true if user auth is set
     */
    public boolean isProxyAuthSet() {
        return isProxyAuthSet;
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
        e.putBoolean(LOGGED_IN, true);
        e.putLong(USER_ID, userId);
        e.putString(AUTH_KEY1, key1);
        e.putString(AUTH_KEY2, key2);
        e.apply();
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
        background_color = settings.getInt(BACKGROUND_COLOR, DEFAULT_BACKGROUND_COLOR);
        highlight_color = settings.getInt(HIGHLIGHT_COLOR, DEFAULT_HIGHLIGHT_COLOR);
        font_color = settings.getInt(FONT_COLOR, DEFAULT_FONT_COLOR);
        popup_color = settings.getInt(POPUP_COLOR, DEFAULT_POPUP_COLOR);
        indexFont = settings.getInt(INDEX_FONT, DEFAULT_FONT_INDEX);
        listSize = settings.getInt(LIST_SIZE, DEFAULT_LIST_SIZE);
        loadImage = settings.getBoolean(IMAGE_LOAD, DEFAULT_DATA_USAGE);
        loadAnswer = settings.getBoolean(ANSWER_LOAD, DEFAULT_DATA_USAGE);
        hqImages = settings.getBoolean(IMAGE_QUALITY, DEFAULT_DATA_USAGE);
        loggedIn = settings.getBoolean(LOGGED_IN, false);
        key1 = settings.getString(AUTH_KEY1, "");
        key2 = settings.getString(AUTH_KEY2, "");
        userId = settings.getLong(USER_ID, 0);
        isProxyEnabled = settings.getBoolean(PROXY_SET, false);
        isProxyAuthSet = settings.getBoolean(AUTH_SET, false);
        proxyHost = settings.getString(PROXY_ADDR, "");
        proxyPort = settings.getString(PROXY_PORT, "");
        proxyUser = settings.getString(PROXY_USER, "");
        proxyPass = settings.getString(PROXY_PASS, "");
        String place = settings.getString(TREND_LOC, DEFAULT_LOCATION_NAME);
        int woeId = settings.getInt(TREND_ID, DEFAULT_LOCATION_WOEID);
        location = new TrendLocation(place, woeId);
    }
}