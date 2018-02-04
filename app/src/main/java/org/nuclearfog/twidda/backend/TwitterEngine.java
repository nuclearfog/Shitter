package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Stores Twitter Object
 * NOT RECOMMENDED FOR MAIN-THREAD!
 */
public class TwitterEngine {

    private final String TWITTER_CONSUMER_KEY = "GrylGIgQK3cDjo9mSTBqF1vwf";
    private final String TWITTER_CONSUMER_SECRET = "pgaWUlDVS5b7Q6VJQDgBzHKw0mIxJIX0UQBcT1oFJEivsCl5OV";

    private static TwitterEngine mTwitter;
    private Twitter twitter;
    private Context context;
    private SharedPreferences settings;
    private RequestToken reqToken;
    private int load;
    private int location;


    /**
     * Singleton Constructor
     * @param context Current Activity's Context
     * @see #getInstance
     */
    private TwitterEngine(Context context) {
        settings = context.getSharedPreferences("settings", 0);
        location = settings.getInt("woeid",23424829); // Germany WOEID
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
    public void initialize(String twitterPin) throws TwitterException, NullPointerException {
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
     * recall Keys from Shared-Preferences
     * & initialize Twitter
     */
    private void init() {
        String key1,key2;
        if( settings.getBoolean("login", false) ) {
            key1 = settings.getString("key1", " ");
            key2 = settings.getString("key2", " ");
            initKeys(key1,key2);
        }
    }

    /**
     * Get Home Timeline
     * @param page current page
     * @param lastId Tweet ID of the earliest Tweet
     * @return List of Tweets
     * @throws TwitterException if access is unavailable
     */
    public List<Status> getHome(int page, long lastId) throws TwitterException {
        return twitter.getHomeTimeline(new Paging(page,load,lastId));
    }

    /**
     * Get Trending Hashtags
     * @return Trend Resource
     * @throws TwitterException if access is unavailable
     */
    public Trends getTrends() throws TwitterException {
        return twitter.getPlaceTrends(location);
    }

    /**
     * Get Mention Tweets
     * @param page current page
     * @param id ID of the earliest Tweet
     * @return List of Mention Tweets
     * @throws TwitterException if access is unavailable
     */
    public List<Status> getMention(int page, long id) throws TwitterException {
        return twitter.getMentionsTimeline(new Paging(page,load,id));
    }


    /**
     * Get Tweet search result
     * @param search Search String
     * @return List of Tweets
     * @throws TwitterException if acces is unavailable
     */
    public List<Status> searchTweets(String search, long id) throws TwitterException {
        Query q = new Query();
        q.setQuery(search+" +exclude:retweets");
        q.setCount(load);
        q.setSinceId(id);
        QueryResult result = twitter.search(q);
        return result.getTweets();
    }

    /**
     * Get User search result
     * @param search Search String
     * @return List of Users
     * @throws TwitterException if access is unavailable
     */
    public List<User> searchUsers(String search) throws TwitterException {
        return twitter.searchUsers(search, -1);
    }

    /**
     * Get User Tweets
     * @param userId User ID
     * @param page current page
     * @return List of User Tweets
     * @throws TwitterException if access is unavailable
     */
    public List<Status> getUserTweets(long userId, long page, long id) throws TwitterException {
        return twitter.getUserTimeline(userId, new Paging((int)page,load, id));
    }

    /**
     * Get User Favs
     * @param userId User ID
     * @param page current page
     * @return List of User Favs
     * @throws TwitterException if access is unavailable
     */
    public List<Status> getUserFavs(long userId, long page, long id) throws TwitterException {
        return twitter.getFavorites(userId,new Paging((int)page,load,id));
    }

    /**
     * Get User Context
     * @param id User ID
     * @return User Object
     * @throws TwitterException if Access is unavailable
     */
    public User getUser(long id) throws TwitterException {
        return twitter.showUser(id);
    }

    /**
     * Get Connection between Home and another User
     * @param id User ID
     * @param following mode following = true , follower = false
     * @return result
     * @throws TwitterException if Access is unavailable
     */
    public boolean getConnection(long id,boolean following) throws TwitterException {
        if(following)
            return twitter.showFriendship(twitter.getId(),id).isSourceFollowingTarget();
        else
            return twitter.showFriendship(twitter.getId(),id).isTargetFollowingSource();
    }

    /**
     * Get Block Status
     * @param id User ID
     * @return if target is blocked
     * @throws TwitterException if Access is unavailable
     */
    public boolean getBlocked(long id) throws TwitterException {
        return twitter.showFriendship(twitter.getId(),id).isSourceMutingTarget();
    }


    /**
     *  Switch following User
     *  @param id Uder ID
     *  @return follow status
     *  @throws TwitterException if Access is unavailable
     */
    public boolean toggleFollow(long id) throws TwitterException {
        if(getConnection(id,false)) {
            twitter.destroyFriendship(id);
            return false;
        } else {
            twitter.createFriendship(id);
            return true;
        }
    }

    /**
     * Switch blocking User
     * @param id User ID
     * @return Block Status
     * @throws TwitterException if Access is unavailable
     */
    public boolean toggleBlock(long id) throws TwitterException {
        if(getBlocked(id)){
            twitter.destroyBlock(id);
            return false;
        } else {
            twitter.createBlock(id);
            return true;
        }
    }


    /**
     * get Following User List
     * @param id User ID
     * @return List of Following User
     * @throws TwitterException if Access is unavailable
     */
    public List<User> getFollowing(long id) throws TwitterException {
        return twitter.getFriendsList(id,-1L);
    }

    /**
     * get Follower
     * @param id User ID
     * @return List of Follower
     * @throws TwitterException if Access is unavailable
     */
    public List<User> getFollower(long id) throws TwitterException {
        return twitter.getFollowersList(id,-1L);
    }

    /**
     * Send Tweet
     * @param text Tweet Text
     * @param reply In reply to tweet ID
     * @param path Path to the Media File
     * @throws TwitterException if Access is unavailable
     * @throws NullPointerException if file path is wrong
     */
    public void sendStatus(String text, long reply, String path) throws TwitterException, NullPointerException {
        StatusUpdate mStatus = new StatusUpdate(text);
        if(reply > 0)
            mStatus.setInReplyToStatusId(reply);
        if(!path.isEmpty()) {
            mStatus.setMedia(new File(path));
        }
        twitter.tweets().updateStatus(mStatus);
    }

    /**
     * Get Tweet
     * @param id Tweet ID
     * @return Tweet Object
     * @throws TwitterException if Access is unavailable
     */
    public Status getStatus(long id) throws TwitterException {
        twitter4j.Status currentTweet = twitter.showStatus(id);
        twitter4j.Status retweetedStat = currentTweet.getRetweetedStatus();
        if(retweetedStat != null) {
            currentTweet = retweetedStat;
        }
        return currentTweet;
    }

    /**
     * Get Answer Tweets
     * @param name name of receiver
     * @param id tweet ID
     * @return List of Answers
     * @throws TwitterException if Access is unavailable
     */
    public List<Status> getAnswers(String name, long id) throws TwitterException {
        List<Status> answers = new ArrayList<>();
        Query query = new Query("to:"+name+" since_id:"+id+" -filter:retweets");
        query.setCount(load);

        QueryResult result = twitter.search(query);
        List<twitter4j.Status> stats = result.getTweets();

        for(twitter4j.Status reply : stats) {
            if(reply.getInReplyToStatusId() == id) {
                answers.add(reply);
            }
        }
        return answers;
    }

    /**
     * Retweet Action
     * @param id Tweet ID
     * @param active current retweet Status
     * @throws TwitterException if Access is unavailable
     */
    public void retweet(long id, boolean active) throws TwitterException {
        if(!active) {
            twitter.retweetStatus(id);
        }
    }

    /**
     * Favorite Action
     * @param id Tweet ID
     * @param active current Favorite Status
     * @throws TwitterException if Access is unavailable
     */
    public void favorite(long id, boolean active) throws TwitterException {
        if(!active){
            twitter.createFavorite(id);
        }
        else{
            twitter.destroyFavorite(id);
        }
    }

    /**
     * check if User ID is home ID
     * @param id User ID
     * @return result
     * @throws TwitterException if Access is unavailable
     */
    public boolean isHome(long id) throws TwitterException {
        return twitter.getId() == id;
    }

    /**
     * Singleton
     * @param context Main Thread Context
     * @return TwitterEngine Instance
     */
    public static TwitterEngine getInstance(Context context) {
        if(mTwitter == null) {
            mTwitter = new TwitterEngine(context);
            mTwitter.init();
        }
        mTwitter.load = mTwitter.settings.getInt("preload", 10);
        return mTwitter;
    }
}