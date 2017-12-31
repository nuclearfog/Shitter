package org.nuclearfog.twidda.Backend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

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
public class TwitterResource {
    private static TwitterResource mTwitter;
    private Twitter twitter;
    private Context context;
    private SharedPreferences settings;
    private RequestToken reqToken;
    private final String TWITTER_CONSUMER_KEY = "GrylGIgQK3cDjo9mSTBqF1vwf";
    private final String TWITTER_CONSUMER_SECRET = "pgaWUlDVS5b7Q6VJQDgBzHKw0mIxJIX0UQBcT1oFJEivsCl5OV";


    /**
     * Singleton Constructor
     * @param context Current Activity's Context
     * @see #getInstance
     */
    private TwitterResource(Context context) {
        settings = context.getSharedPreferences("settings", 0);
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
        builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
        Configuration configuration = builder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        twitter = factory.getInstance();
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
        String key1 = accessToken.getToken();
        String key2 = accessToken.getTokenSecret();
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
     * Singleton
     * @param context Main Thread Context
     * @return TwitterResource Instance
     */
    public static TwitterResource getInstance(Context context) {
        if(mTwitter == null){
            mTwitter = new TwitterResource(context);
        }
        return mTwitter;
    }
}