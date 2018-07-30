package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.support.annotation.NonNull;

import org.nuclearfog.twidda.backend.listitems.Trend;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.backend.listitems.TwitterUser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import twitter4j.IDs;
import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Relationship;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UploadedMedia;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Stores Twitter Object
 */
public class TwitterEngine {

    private final String TWITTER_CONSUMER_KEY = "0EKRHWYcakpCkl8Lr4OcBFMZb";
    private final String TWITTER_CONSUMER_SECRET = "xxx";

    private static TwitterEngine mTwitter;
    private long twitterID = -1L;
    private Twitter twitter;
    private GlobalSettings settings;
    private RequestToken reqToken;
    private boolean login;
    private int load;


    /**
     * Singleton Constructor
     * @param context Current Activity's Context
     * @see #getInstance
     */
    private TwitterEngine(Context context) {
        settings = GlobalSettings.getInstance(context);
        login = settings.getLogin();
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
        builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
        Configuration configuration = builder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        twitter = factory.getInstance();
    }


    /**
     * Request Registration Website
     * @return Link to App Registration
     * @throws TwitterException if internet connection is unavailable
     */
    public String request() throws TwitterException {
        if(reqToken == null)
            reqToken = twitter.getOAuthRequestToken();
        return reqToken.getAuthenticationURL();
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
        builder.setTweetModeExtended(true);
        AccessToken token = new AccessToken(key1,key2);
        twitter = new TwitterFactory( builder.build() ).getInstance(token);
        login = true;
    }


    /**
     * store current user's name & id
     * @param key1 AccessToken
     * @param key2 AccessToken Secret
     * @throws TwitterException if twitter isn't initialized yet.
     */
    private void saveCurrentUser(String key1, String key2) throws TwitterException {
        twitterID = twitter.getId();
        settings.setConnection(key1, key2, twitterID);
    }


    /**
     * recall Keys from Shared-Preferences
     * & initialize Twitter
     */
    private void init() {
        if( login ) {
            String keys[] = settings.getKeys();
            initKeys(keys[0],keys[1]);
        }
        twitterID = settings.getUserId();
    }


    /**
     * set amount of tweets to be loaded
     */
    private void setLoad() {
        load = settings.getRowLimit();
    }


    /**
     * @return if Twitter4J is registered
     */
    public boolean loggedIn() {
        return login;
    }


    /**
     * Get Home Timeline
     * @param page current page
     * @param lastId Tweet ID of the earliest Tweet
     * @return List of Tweets
     * @throws TwitterException if access is unavailable
     */
    public List<Tweet> getHome(int page, long lastId) throws TwitterException {
        List<Status> homeTweets = twitter.getHomeTimeline(new Paging(page,load,lastId));
        return convertStatusList(homeTweets);
    }


    /**
     * Get Mention Tweets
     * @param page current page
     * @param id ID of the earliest Tweet
     * @return List of Mention Tweets
     * @throws TwitterException if access is unavailable
     */
    public List<Tweet> getMention(int page, long id) throws TwitterException {
        List<Status> mentions = twitter.getMentionsTimeline(new Paging(page,/*load*/5,id));
        return convertStatusList(mentions);
    }


    /**
     * Get Tweet search result
     * @param search Search String
     * @param id Since ID
     * @return List of Tweets
     * @throws TwitterException if acces is unavailable
     */
    public List<Tweet> searchTweets(String search, long id) throws TwitterException {
        Query q = new Query();
        q.setQuery(search+" +exclude:retweets");
        q.setCount(load);
        q.setSinceId(id);
        QueryResult result = twitter.search(q);
        List<Status> results = result.getTweets();
        return convertStatusList(results);
    }


    /**
     * Get Trending Hashtags
     * @param woeId Yahoo World ID
     * @return Trend Resource
     * @throws TwitterException if access is unavailable
     */
    public List<Trend> getTrends(int woeId) throws TwitterException {
        List<Trend> result = new ArrayList<>();
        twitter4j.Trend trends[] = twitter.getPlaceTrends(woeId).getTrends();

        for(int i = 0 ; i < trends.length ; i++) {
            Trend item = new Trend(i+1, trends[i].getName(), trends[i].getURL());
            result.add(item);
        }
        return result;
    }


    /**
     * Get User search result
     * @param search Search String
     * @return List of Users
     * @throws TwitterException if access is unavailable
     */
    public List<TwitterUser> searchUsers(String search) throws TwitterException {
        return convertUserList(twitter.searchUsers(search, -1));
    }


    /**
     * Get User Tweets
     * @param userId User ID
     * @param page current page
     * @return List of User Tweets
     * @throws TwitterException if access is unavailable
     */
    public List<Tweet> getUserTweets(long userId, long page, long id) throws TwitterException {
        List<Status> result = twitter.getUserTimeline(userId, new Paging((int)page,load, id));
        return convertStatusList(result);
    }


    /**
     * Get User Favs
     * @param userId User ID
     * @param page current page
     * @return List of User Favs
     * @throws TwitterException if access is unavailable
     */
    public List<Tweet> getUserFavs(long userId, long page, long id) throws TwitterException {
        List<Status> favorits = twitter.getFavorites(userId,new Paging((int)page,load,id));
        return convertStatusList(favorits);
    }


    /**
     * Get User Context
     * @param id User ID
     * @return User Object
     * @throws TwitterException if Access is unavailable
     */
    public TwitterUser getUser(long id) throws TwitterException {
        return getUser(twitter.showUser(id));
    }


    /**
     * Efficient Access of Connection Information
     * @param id User ID compared with Home ID
     * @return array of connection states Index 0: Following, 1: Follow, 2: blocked
     * @throws TwitterException if Connection is unavailable
     */
    public boolean[] getConnection(long id) throws TwitterException {
        Relationship connect = twitter.showFriendship(twitterID,id);
        boolean connection[] = new boolean[3];
        connection[0] = connect.isSourceFollowingTarget();
        connection[1] = connect.isTargetFollowingSource();
        connection[2] = connect.isSourceBlockingTarget();
        return connection;
    }


    /**
     *  Switch following User
     *  @param userId User ID
     *  @param action using action
     *  @throws TwitterException if Access is unavailable
     */
    public void followAction(long userId, boolean action) throws TwitterException {
        if(action) {
            twitter.createFriendship(userId);
        } else {
            twitter.destroyFriendship(userId);
        }
    }


    /**
     * Switch blocking User
     * @param userId User ID
     * @param action using action
     * @throws TwitterException if Access is unavailable
     */
    public void blockAction(long userId, boolean action) throws TwitterException {
        if(action){
            twitter.createBlock(userId);
        } else {
            twitter.destroyBlock(userId);
        }
    }


    /**
     * get Following User List
     * @param id User ID
     * @return List of Following User
     * @throws TwitterException if Access is unavailable
     */
    public List<TwitterUser> getFollowing(long id, long cursor) throws TwitterException {
        IDs userIDs = twitter.getFriendsIDs(id,cursor,load);
        return convertUserList(twitter.lookupUsers(userIDs.getIDs()));
    }


    /**
     * get Follower
     * @param id User ID
     * @return List of Follower
     * @throws TwitterException if Access is unavailable
     */
    public List<TwitterUser> getFollower(long id, long cursor) throws TwitterException {
        IDs userIDs = twitter.getFollowersIDs(id,cursor,load);
        return convertUserList(twitter.lookupUsers(userIDs.getIDs()));
    }


    /**
     * Send Tweet
     * @param text Tweet Text
     * @param reply In reply to tweet ID
     * @throws TwitterException if Access is unavailable
     */
    public void sendStatus(String text, long reply) throws TwitterException {
        StatusUpdate mStatus = new StatusUpdate(text);
        if(reply > 0)
            mStatus.setInReplyToStatusId(reply);
        twitter.tweets().updateStatus(mStatus);
    }


    /**
     * Send Tweet
     * @param text Tweet Text
     * @param reply In reply to tweet ID
     * @param path Path to the Media File
     * @throws TwitterException if Access is unavailable
     * @throws NullPointerException if file path is wrong
     */
    public void sendStatus(String text, long reply,@NonNull String[] path) throws TwitterException, NullPointerException {
        UploadedMedia media;
        int count = path.length;
        long[] mIDs = new long[count];
        StatusUpdate mStatus = new StatusUpdate(text);

        if(reply > 0) {
            mStatus.setInReplyToStatusId(reply);
        }
        for(int i = 0 ; i < count; i++) {
            String current = path[i];
            media = twitter.uploadMedia(new File(current));
            mIDs[i] = media.getMediaId();
        }
        mStatus.setMediaIds(mIDs);
        twitter.tweets().updateStatus(mStatus);
    }


    /**
     * Get Tweet
     * @param id Tweet ID
     * @return Tweet Object
     * @throws TwitterException if Access is unavailable
     */
    public Tweet getStatus(long id) throws TwitterException {
        Status status = twitter.showStatus(id);
        Status retweet = status.getRetweetedStatus(); // Bug getretweetedstatus does not have a currentUserRetweetId
        if(retweet != null ) {
            retweet = twitter.showStatus(retweet.getId()); // reload full retweet
            Tweet embedded = getTweet(retweet,null);
            return getTweet(status,embedded);
        } else {
            return getTweet(status,null);
        }
    }


    /**
     * Get Answer Tweets
     * @param name screen name of receiver
     * @param tweetId tweet ID
     * @param sinceId last tweet
     * @return List of Answers
     * @throws TwitterException if Access is unavailable
     */
    public List<Tweet> getAnswers(String name, long tweetId, long sinceId) throws TwitterException {
        List<Status> answers = new ArrayList<>();
        Query query = new Query("to:"+name+" since_id:"+sinceId+" -filter:retweets");
        query.setCount(load);
        QueryResult result = twitter.search(query);
        List<twitter4j.Status> stats = result.getTweets();
        for(twitter4j.Status reply : stats) {
            if(reply.getInReplyToStatusId() == tweetId) {
                answers.add(reply);
            }
        }
        return convertStatusList(answers);
    }


    /**
     * Retweet Action
     * @param tweet Tweet
     * @throws TwitterException if Access is unavailable
     */
    public void retweet(Tweet tweet) throws TwitterException {
        if(tweet.retweeted) {
            if(tweet.retweetId > tweet.tweetID)
                deleteTweet(tweet.retweetId);
            else
                deleteTweet(tweet.tweetID);
        } else {
            twitter.retweetStatus(tweet.tweetID);
        }
    }


    /**
     * Favorite Action
     * @param tweet Tweet
     * @throws TwitterException if Access is unavailable
     */
    public void favorite(Tweet tweet) throws TwitterException {
        if(tweet.favorized) {
            twitter.destroyFavorite(tweet.tweetID);
        } else {
            twitter.createFavorite(tweet.tweetID);
        }
    }


    /**
     * Get User who retweeted a Tweet
     * @param tweetID Tweet ID
     * @param cursor List Cursor
     * @return List of users or empty list if no match
     * @throws TwitterException if Access is unavailable
     */
    public List<TwitterUser> getRetweeter(long tweetID, long cursor) throws TwitterException {
        Tweet embeddedStat = getStatus(tweetID).embedded;
        if(embeddedStat != null)
            tweetID = embeddedStat.tweetID;
        long[] userIds = twitter.getRetweeterIds(tweetID,load,cursor).getIDs();
        if(userIds.length == 0) {
            return new ArrayList<>();
        } else {
            return convertUserList(twitter.lookupUsers(userIds));
        }
    }


    /**
     * convert #twitter4j.User to TwitterUser List
     * @param users Twitter4J user List
     * @return TwitterUser
     */
    private List<TwitterUser> convertUserList(List<User> users) {
        List <TwitterUser> result = new ArrayList<>();
        if(users.isEmpty())
            return result;
            for (User user : users) {
                try {
                    TwitterUser item = getUser(user);
                    result.add(item);
                } catch (Exception err) {
                    // Bug in Twitter4J caused by 'withheld accounts'
                    // because of empty profile image URL
                }
            }
        return result;
    }



    /**
     * convert #twitter4j.Status to Tweet List
     * @param statuses Twitter4J status List
     * @return TwitterStatus
     */
    private List<Tweet>convertStatusList(List<Status> statuses) {
        List<Tweet> result = new ArrayList<>();
        if(statuses.isEmpty())
            return result;

        for(Status status : statuses) {
            try {
                Status embedded = status.getRetweetedStatus();
                if(embedded != null) {
                    Tweet retweet = getTweet(embedded,null);
                    Tweet tweet = getTweet(status, retweet);
                    result.add(tweet);
                } else {
                    Tweet tweet = getTweet(status,null);
                    result.add(tweet);
                }
            } catch (Exception err) {
                // Bug in Twitter4J caused by 'withheld accounts'
                // because of empty profile image URL
            }
        }
        return result;
    }


    /**
     * @param status twitter4j.Status
     * @param retweetedStat embedded Status
     * @return Tweet item
     */
    private Tweet getTweet(Status status,Tweet retweetedStat){
        TwitterUser user = getUser(status.getUser());
        int retweet, favorite;
        if(retweetedStat != null) {
            retweet = retweetedStat.retweet;
            favorite = retweetedStat.favorit;
        } else {
            retweet = status.getRetweetCount();
            favorite = status.getFavoriteCount();
        }
        return new Tweet(status.getId(),retweet,favorite,user,status.getText(),
                status.getCreatedAt().getTime(),status.getInReplyToScreenName(),
                getMediaLinks(status),status.getSource(),status.getInReplyToStatusId(),
                retweetedStat,status.getCurrentUserRetweetId(),status.isRetweeted(),status.isFavorited());
    }

    /**
     * @param user Twitter4J User
     * @return User item
     */
    private TwitterUser getUser(User user) {
        return new TwitterUser(user.getId(),user.getName(),user.getScreenName(),
                user.getOriginalProfileImageURL(),user.getDescription(),user.getLocation(),user.isVerified(),
                user.isProtected(),user.getURL(),user.getProfileBannerURL(),user.getCreatedAt().getTime(),
                user.getFriendsCount(),user.getFollowersCount());
    }


    /**
     * @param status Twitter4J status
     * @return Array of Medialinks
     */
    private String[] getMediaLinks(Status status) {
        MediaEntity[] mediaEntities = status.getMediaEntities();
        String medialinks[] = new String[mediaEntities.length];
        byte i = 0;
        for(MediaEntity media : mediaEntities) {
            medialinks[i++] = media.getMediaURLHttps();
        }
        return medialinks;
    }


    /**
     * @param id Tweet ID
     * @throws TwitterException if Access is unavailable
     */
    public void deleteTweet(long id) throws TwitterException {
        twitter.destroyStatus(id);
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
        mTwitter.setLoad();
        return mTwitter;
    }
}