package org.nuclearfog.twidda.Engine;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import org.nuclearfog.twidda.DataBase.AppDatabase;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Stores Twitter Object
 * NOT RECOMMENDED FOR MAIN-THREAD!
 */
public class TwitterStore {

    private static TwitterStore mTwitter;
    private Twitter twitter;
    private Context context;
    private AppDatabase mData;
    private SharedPreferences settings;
    private RequestToken reqToken;
    private final String TWITTER_CONSUMER_KEY = "GrylGIgQK3cDjo9mSTBqF1vwf";
    private final String TWITTER_CONSUMER_SECRET = "pgaWUlDVS5b7Q6VJQDgBzHKw0mIxJIX0UQBcT1oFJEivsCl5OV";


    /**
     * Singleton Constructor
     * @param context Current Activity's Context
     * @see #getInstance
     */
    private TwitterStore(Context context) {
        settings = context.getSharedPreferences("settings", 0);
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
        builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
        Configuration configuration = builder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        twitter = factory.getInstance();
        mData = AppDatabase.getInstance(context);
        this.context = context;
    }


    /**
     * get RequestToken and Open Twitter Registration Website
     * @throws TwitterException if Connection is unavailable
     */
    public void request() throws TwitterException {
        reqToken = twitter.getOAuthRequestToken();
        String redirectURL = reqToken.getAuthenticationURL();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(redirectURL));
        context.startActivity(i);
    }


    /**
     * Get Access-Token, store and initialize Twitter
     * @param twitterPin PIN for accessing account
     * @throws TwitterException if pin is false
     * @throws NullPointerException if Request-Token is not set
     * @see #initKeys(String, String)
     */
    public void initialize(String twitterPin) throws TwitterException, NullPointerException{
        if(reqToken == null) throw new NullPointerException("empty request token");
        AccessToken accessToken = twitter.getOAuthAccessToken(reqToken,twitterPin);
        long userId = accessToken.getUserId();
        String username = accessToken.getScreenName();
        String key1 = accessToken.getToken();
        String key2 = accessToken.getTokenSecret();
        store(username, userId,key1,key2);
        initKeys(key1, key2);
        saveCurrentUser(key1, key2);
    }


    /**
     * Initialize Twitter with Accesstoken
     * @param key1 AccessToken
     * @param key2 AccessToken Secret
     */
    private void initKeys(String key1, String key2) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
        builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
        AccessToken token = new AccessToken(key1,key2);
        twitter = new TwitterFactory( builder.build() ).getInstance(token);
    }

    /**
     * store current user's name & id
     * @param key1 AccessToken
     * @param key2 AccessToken Secret
     * @throws TwitterException if twitter isn't initialized yet.
     */
    private void saveCurrentUser(String key1, String key2) throws TwitterException {
        SharedPreferences.Editor e = settings.edit();
        e.putBoolean("login", true);
        e.putLong("userID", twitter.getId());
        e.putString("username", twitter.getScreenName());
        e.putString("key1", key1);
        e.putString("key2", key2);
        e.apply();
    }


    /**
     * get Twitter object
     * @return Twitter Object
     */
    public Twitter getTwitter(){
        init();
        return twitter;
    }


    /**
     * recall Keys from Shared-Preferences
     * & initialize Twitter
     */
    public void init(){
        String key1,key2;
        if( settings.getBoolean("login", false) ) {
            key1 = settings.getString("key1", " ");
            key2 = settings.getString("key2", " ");
            initKeys(key1,key2);
        }
    }


    /**
     * Store user id + keys into sqlite database
     * @param username Screen name of User
     * @param userId unique User ID
     * @param key1 First Key of Access-token
     * @param key2 Second Key of Access-Token
     */
    private void store(String username, long userId, String key1, String key2){
        SQLiteDatabase db = mData.getWritableDatabase();
        ContentValues storeValues = new ContentValues();
        ContentValues usrValues = new ContentValues();
        usrValues.put("username", username);
        usrValues.put("userID", userId);
        storeValues.put("userID", userId);
        storeValues.put("key1", key1);
        storeValues.put("key2", key2);
        db.insertWithOnConflict("user",null,usrValues,SQLiteDatabase.CONFLICT_IGNORE);
        db.insertWithOnConflict("keys",null,storeValues,SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }


    /**
     * Singleton
     * @param context Main Thread Context
     * @return TwitterStore Instance
     */
    public static TwitterStore getInstance(Context context) {
        if(mTwitter == null){
            mTwitter = new TwitterStore(context);
        }
        return mTwitter;
    }
}