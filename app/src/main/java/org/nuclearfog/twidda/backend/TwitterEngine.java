package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.backend.items.Trend;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import twitter4j.DirectMessage;
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

public class TwitterEngine {

    private static TwitterEngine mTwitter;
    private final String TWITTER_CONSUMER_KEY = BuildConfig.API_KEY_1;
    private final String TWITTER_CONSUMER_SECRET = BuildConfig.API_KEY_2;

    private String redirectionUrl;
    private long twitterID;
    private Twitter twitter;
    private GlobalSettings settings;
    private RequestToken reqToken;
    private int load;


    /**
     * Singleton Constructor
     */
    private TwitterEngine(Context context) {
        settings = GlobalSettings.getInstance(context);
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
        builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
        Configuration configuration = builder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        twitter = factory.getInstance();

        if (settings.getLogin()) {
            String keys[] = settings.getKeys();
            initKeys(keys[0], keys[1]);
            twitterID = settings.getUserId();
        }
    }


    /**
     * Singleton
     *
     * @param context Main Thread Context
     * @return TwitterEngine Instance
     */
    public static TwitterEngine getInstance(Context context) {
        if (mTwitter == null) {
            mTwitter = new TwitterEngine(context);
        }
        mTwitter.setLoad();
        return mTwitter;
    }


    public static void destroyInstance() {
        mTwitter = null;
    }


    /**
     * Request Registration Website
     *
     * @return Link to App Registration
     * @throws TwitterException if internet connection is unavailable
     */
    public String request() throws TwitterException {
        if (reqToken == null) {
            reqToken = twitter.getOAuthRequestToken();
            redirectionUrl = reqToken.getAuthenticationURL();
        }
        return redirectionUrl;
    }


    /**
     * Get Access-Token, store and initialize Twitter
     *
     * @param twitterPin PIN for accessing account
     * @throws TwitterException if pin is false
     * @see #initKeys(String, String)
     */
    public void initialize(String twitterPin) throws TwitterException {
        if (reqToken != null) {
            AccessToken accessToken = twitter.getOAuthAccessToken(reqToken, twitterPin);
            String key1 = accessToken.getToken();
            String key2 = accessToken.getTokenSecret();
            initKeys(key1, key2);
            twitterID = twitter.getId();
            settings.setConnection(key1, key2, twitterID);
        }
    }


    /**
     * Initialize Twitter with Accesstoken
     *
     * @param key1 AccessToken
     * @param key2 AccessToken Secret
     */
    private void initKeys(String key1, String key2) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
        builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
        builder.setTweetModeExtended(true);
        AccessToken token = new AccessToken(key1, key2);
        twitter = new TwitterFactory(builder.build()).getInstance(token);
    }


    /**
     * set amount of tweets to be loaded
     */
    private void setLoad() {
        load = settings.getRowLimit();
    }


    /**
     * Get Home Timeline
     *
     * @param page   current page
     * @param lastId Tweet ID of the earliest Tweet
     * @return List of Tweets
     * @throws TwitterException if access is unavailable
     */
    public List<Tweet> getHome(int page, long lastId) throws TwitterException {
        List<Status> homeTweets = twitter.getHomeTimeline(new Paging(page, load, lastId));
        return convertStatusList(homeTweets);
    }


    /**
     * Get Mention Tweets
     *
     * @param page current page
     * @param id   ID of the earliest Tweet
     * @return List of Mention Tweets
     * @throws TwitterException if access is unavailable
     */
    public List<Tweet> getMention(int page, long id) throws TwitterException {
        List<Status> mentions = twitter.getMentionsTimeline(new Paging(page,/*load*/5, id));
        return convertStatusList(mentions);
    }


    /**
     * Get Tweet search result
     *
     * @param search Search String
     * @param id     Since ID
     * @return List of Tweets
     * @throws TwitterException if acces is unavailable
     */
    public List<Tweet> searchTweets(String search, long id) throws TwitterException {
        Query q = new Query();
        q.setQuery(search + " +exclude:retweets");
        q.setCount(load);
        q.setSinceId(id);
        QueryResult result = twitter.search(q);
        List<Status> results = result.getTweets();
        return convertStatusList(results);
    }


    /**
     * Get Trending Hashtags
     *
     * @param woeId Yahoo World ID
     * @return Trend Resource
     * @throws TwitterException if access is unavailable
     */
    public List<Trend> getTrends(int woeId) throws TwitterException {
        List<Trend> result = new ArrayList<>();
        twitter4j.Trend trends[] = twitter.getPlaceTrends(woeId).getTrends();

        for (int i = 0; i < trends.length; i++) {
            Trend item = new Trend(i + 1, trends[i].getName());
            result.add(item);
        }
        return result;
    }


    /**
     * Get User search result
     *
     * @param search Search String
     * @return List of Users
     * @throws TwitterException if access is unavailable
     */
    public List<TwitterUser> searchUsers(String search) throws TwitterException {
        return convertUserList(twitter.searchUsers(search, -1));
    }


    /**
     * Get User Tweets
     *
     * @param userId  User ID
     * @param sinceId minimum tweet ID
     * @param page    current page
     * @return List of User Tweets
     * @throws TwitterException if access is unavailable
     */
    public List<Tweet> getUserTweets(long userId, long sinceId, long page) throws TwitterException {
        List<Status> result = twitter.getUserTimeline(userId, new Paging((int) page, load, sinceId));
        return convertStatusList(result);
    }


    /**
     * Get User Favs
     *
     * @param userId  User ID
     * @param sinceId minimum tweet ID
     * @param page    current page
     * @return List of User Favs
     * @throws TwitterException if access is unavailable
     */
    public List<Tweet> getUserFavs(long userId, long sinceId, long page) throws TwitterException {
        List<Status> favorits = twitter.getFavorites(userId, new Paging((int) page, load, sinceId));
        return convertStatusList(favorits);
    }


    /**
     * Get User Context
     *
     * @param id User ID
     * @return User Object
     * @throws TwitterException if Access is unavailable
     */
    public TwitterUser getUser(long id) throws TwitterException {
        return getUser(twitter.showUser(id));
    }


    /**
     * Efficient Access of Connection Information
     *
     * @param id User ID compared with Home ID
     * @return array of connection states Index 0: Following, 1: Follow, 2: blocked 3: muted 4: canDM
     * @throws TwitterException if Connection is unavailable
     */
    public boolean[] getConnection(long id) throws TwitterException {
        Relationship connect = twitter.showFriendship(twitterID, id);
        boolean connection[] = new boolean[5];
        connection[0] = connect.isSourceFollowingTarget();
        connection[1] = connect.isTargetFollowingSource();
        connection[2] = connect.isSourceBlockingTarget();
        connection[3] = connect.isSourceMutingTarget();
        connection[4] = connect.canSourceDm();
        return connection;
    }


    /**
     * Switch following User
     *
     * @param userId User ID
     * @param action using action
     * @return updated user information
     * @throws TwitterException if Access is unavailable
     */
    public TwitterUser followAction(long userId, boolean action) throws TwitterException {
        User user;
        if (action)
            user = twitter.createFriendship(userId);
        else
            user = twitter.destroyFriendship(userId);
        return getUser(user);
    }


    /**
     * Switch blocking User
     *
     * @param userId User ID
     * @param action using action
     * @return updated user information
     * @throws TwitterException if Access is unavailable
     */
    public TwitterUser blockAction(long userId, boolean action) throws TwitterException {
        User user;
        if (action)
            user = twitter.createBlock(userId);
        else
            user = twitter.destroyBlock(userId);
        return getUser(user);
    }


    /**
     * Switch muting User
     *
     * @param userId User ID
     * @param action using action
     * @return updated user information
     * @throws TwitterException if Access is unavailable
     */
    public TwitterUser muteAction(long userId, boolean action) throws TwitterException {
        User user;
        if (action)
            user = twitter.createMute(userId);
        else
            user = twitter.destroyMute(userId);
        return getUser(user);
    }


    /**
     * get Following User List
     *
     * @param id User ID
     * @return List of Following User
     * @throws TwitterException if Access is unavailable
     */
    public List<TwitterUser> getFollowing(long id, long cursor) throws TwitterException {
        IDs userIDs = twitter.getFriendsIDs(id, cursor, load);
        return convertUserList(twitter.lookupUsers(userIDs.getIDs()));
    }


    /**
     * get Follower
     *
     * @param id User ID
     * @return List of Follower
     * @throws TwitterException if Access is unavailable
     */
    public List<TwitterUser> getFollower(long id, long cursor) throws TwitterException {
        IDs userIDs = twitter.getFollowersIDs(id, cursor, load);
        return convertUserList(twitter.lookupUsers(userIDs.getIDs()));
    }


    /**
     * Send Tweet
     *
     * @param text  Tweet Text
     * @param reply In reply to tweet ID
     * @param path  Path to the Media File
     * @throws TwitterException if Access is unavailable
     */
    public void sendStatus(String text, long reply, @Nullable String[] path) throws TwitterException {
        StatusUpdate mStatus = new StatusUpdate(text);

        if (reply > 0)
            mStatus.setInReplyToStatusId(reply);

        if (path != null) {
            final int count = path.length;
            long[] mIDs = new long[count];
            for (int i = 0; i < count; i++) {
                String current = path[i];
                UploadedMedia media = twitter.uploadMedia(new File(current));
                mIDs[i] = media.getMediaId();
            }
            mStatus.setMediaIds(mIDs);
        }
        twitter.tweets().updateStatus(mStatus);
    }


    /**
     * Get Tweet
     *
     * @param id Tweet ID
     * @return Tweet Object
     * @throws TwitterException if Access is unavailable
     */
    public Tweet getStatus(long id) throws TwitterException {
        Status status = twitter.showStatus(id);
        Status retweet = status.getRetweetedStatus();
        if (retweet != null) {
            retweet = twitter.showStatus(retweet.getId());
            Tweet embedded = getTweet(retweet, null);
            return getTweet(status, embedded);
        } else {
            return getTweet(status, null);
        }
    }


    /**
     * Get Answer Tweets
     *
     * @param name    screen name of receiver
     * @param tweetId tweet ID
     * @param sinceId last tweet
     * @return List of Answers
     * @throws TwitterException if Access is unavailable
     */
    public List<Tweet> getAnswers(String name, long tweetId, long sinceId) throws TwitterException {
        List<Status> answers = new ArrayList<>();
        Query query = new Query("to:" + name + " since_id:" + sinceId + " -filter:retweets");
        query.setCount(load);
        QueryResult result = twitter.search(query);

        List<twitter4j.Status> stats = result.getTweets();
        for (twitter4j.Status reply : stats) {
            if (reply.getInReplyToStatusId() == tweetId) {
                answers.add(reply);
            }
        }
        return convertStatusList(answers);
    }


    /**
     * Retweet Action
     *
     * @param tweetId Tweet ID
     * @throws TwitterException if Access is unavailable
     */
    public Tweet retweet(long tweetId) throws TwitterException {
        Status tweet = twitter.showStatus(tweetId);

        if (tweet.isRetweeted())
            tweet = twitter.unRetweetStatus(tweet.getId());
        else
            tweet = twitter.retweetStatus(tweet.getId());

        if (tweet.getRetweetedStatus() == null)
            return getTweet(tweet, null);
        else
            return getTweet(tweet, getTweet(tweet.getRetweetedStatus(), null));
    }


    /**
     * Favorite Action
     *
     * @param tweetId Tweet ID
     * @throws TwitterException if Access is unavailable
     */
    public Tweet favorite(long tweetId) throws TwitterException {
        Status tweet = twitter.showStatus(tweetId);

        if (tweet.isFavorited())
            tweet = twitter.destroyFavorite(tweet.getId());
        else
            tweet = twitter.createFavorite(tweet.getId());

        if (tweet.getRetweetedStatus() == null)
            return getTweet(tweet, null);
        else
            return getTweet(tweet, getTweet(tweet.getRetweetedStatus(), null));
    }


    /**
     * @param id Tweet ID
     * @throws TwitterException if Access is unavailable
     */
    public void deleteTweet(long id) throws TwitterException {
        twitter.destroyStatus(id);
    }


    /**
     * Get User who retweeted a Tweet
     *
     * @param tweetID Tweet ID
     * @param cursor  List Cursor
     * @return List of users or empty list if no match
     * @throws TwitterException if Access is unavailable
     */
    public List<TwitterUser> getRetweeter(long tweetID, long cursor) throws TwitterException {
        Tweet embeddedStat = getStatus(tweetID).getEmbeddedTweet();
        if (embeddedStat != null)
            tweetID = embeddedStat.getId();
        long[] userIds = twitter.getRetweeterIds(tweetID, load, cursor).getIDs();
        if (userIds.length == 0) {
            return new ArrayList<>();
        } else {
            return convertUserList(twitter.lookupUsers(userIds));
        }
    }


    /**
     * get list of Direct Messages
     *
     * @return DM List
     * @throws TwitterException if access is unavailable
     */
    public List<Message> getMessages() throws TwitterException {
        List<DirectMessage> dmList = twitter.getDirectMessages(load);
        List<Message> result = new ArrayList<>();
        for (DirectMessage dm : dmList) {
            result.add(getMessage(dm));
        }
        return result;
    }


    /**
     * Send direct message
     *
     * @param username receiver name
     * @param msg      Message Text
     * @param path     media path
     * @throws TwitterException if access is unavailable
     */
    public void sendMessage(String username, String msg, @Nullable String path) throws TwitterException {
        long id = twitter.showUser(username).getId();
        if (path != null && !path.trim().isEmpty()) {
            UploadedMedia media = twitter.uploadMedia(new File(path));
            long mediaId = media.getMediaId();
            twitter.sendDirectMessage(id, msg, mediaId);
        } else {
            twitter.sendDirectMessage(id, msg);
        }
    }


    /**
     * Delete Direct Message
     *
     * @param id Message ID
     * @throws TwitterException if Access is unavailable
     */
    public void deleteMessage(long id) throws TwitterException {
        twitter.destroyDirectMessage(id);
    }


    /**
     * convert #twitter4j.User to TwitterUser List
     *
     * @param users Twitter4J user List
     * @return TwitterUser
     */
    private List<TwitterUser> convertUserList(List<User> users) {
        List<TwitterUser> result = new ArrayList<>();

        for (User user : users) {
            TwitterUser item = getUser(user);
            result.add(item);
        }
        return result;
    }


    /**
     * convert #twitter4j.Status to Tweet List
     *
     * @param statuses Twitter4J status List
     * @return TwitterStatus
     */
    private List<Tweet> convertStatusList(List<Status> statuses) {
        List<Tweet> result = new ArrayList<>();

        for (Status status : statuses) {
            Status embedded = status.getRetweetedStatus();
            if (embedded != null) {
                Tweet retweet = getTweet(embedded, null);
                Tweet tweet = getTweet(status, retweet);
                result.add(tweet);
            } else {
                Tweet tweet = getTweet(status, null);
                result.add(tweet);
            }
        }
        return result;
    }


    /**
     * @param status        twitter4j.Status
     * @param retweetedStat embedded Status
     * @return Tweet item
     */
    private Tweet getTweet(@NonNull Status status, @Nullable Tweet retweetedStat) {
        TwitterUser user = getUser(status.getUser());
        int retweet = status.getRetweetCount();
        int favorite = status.getFavoriteCount();
        if (retweetedStat != null) {
            retweet = retweetedStat.getRetweetCount();
            favorite = retweetedStat.getFavorCount();
        }
        String api = status.getSource();
        api = api.substring(api.indexOf('>') + 1);
        api = api.substring(0, api.indexOf('<'));

        return new Tweet(status.getId(), retweet, favorite, user, status.getText(),
                status.getCreatedAt().getTime(), status.getInReplyToScreenName(), status.getInReplyToUserId(),
                getMediaLinks(status), api, status.getInReplyToStatusId(),
                retweetedStat, status.getCurrentUserRetweetId(), status.isRetweeted(), status.isFavorited());
    }


    /**
     * @param user Twitter4J User
     * @return User item
     */
    private TwitterUser getUser(User user) {
        return new TwitterUser(user);
    }


    /**
     * @param dm Twitter4J directmessage
     * @return dm item
     */
    private Message getMessage(DirectMessage dm) throws TwitterException {
        TwitterUser sender, receiver;
        sender = getUser(twitter.showUser(dm.getSenderId()));

        if (dm.getSenderId() != dm.getRecipientId()) {
            receiver = getUser(twitter.showUser(dm.getRecipientId()));
        } else {
            receiver = sender;
        }
        long time = dm.getCreatedAt().getTime();

        return new Message(dm.getId(), sender, receiver, time, dm.getText());
    }


    /**
     * @param status Twitter4J status
     * @return Array of Medialinks
     */
    private String[] getMediaLinks(Status status) {
        MediaEntity[] mediaEntities = status.getMediaEntities();
        String medialinks[] = new String[mediaEntities.length];
        byte i = 0;
        for (MediaEntity media : mediaEntities) {
            medialinks[i++] = media.getMediaURLHttps();
        }
        return medialinks;
    }
}