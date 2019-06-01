package org.nuclearfog.twidda.backend;

import android.content.Context;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.backend.items.Trend;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.TweetHolder;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.IDs;
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
            String[] keys = settings.getKeys();
            initKeys(keys[0], keys[1]);
            twitterID = settings.getUserId();
        }
    }


    /**
     * Singleton, package-private
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
        List<Status> mentions = twitter.getMentionsTimeline(new Paging(page, load, id));
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
        List<Trend> result = new LinkedList<>();
        twitter4j.Trend[] trends = twitter.getPlaceTrends(woeId).getTrends();

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
     * @param userId User ID
     * @return User Object
     * @throws TwitterException if Access is unavailable
     */
    public TwitterUser getUser(long userId) throws TwitterException {
        return new TwitterUser(twitter.showUser(userId));
    }


    /**
     * Get current user
     *
     * @return curent user
     * @throws TwitterException if Access is unavailable
     */
    public TwitterUser getCurrentUser() throws TwitterException {
        return new TwitterUser(twitter.showUser(twitterID));
    }


    /**
     * Efficient Access of Connection Information
     *
     * @param userId User ID compared with Home ID
     * @return array of connection states Index 0: I follow user, 1: user follows me, 2: blocked 3: muted 4: canDM
     * @throws TwitterException if Connection is unavailable
     */
    public boolean[] getConnection(long userId) throws TwitterException {
        Relationship connect = twitter.showFriendship(twitterID, userId);
        boolean[] connection = new boolean[5];
        connection[0] = connect.isSourceFollowingTarget();
        connection[1] = connect.isTargetFollowingSource();
        connection[2] = connect.isSourceBlockingTarget();
        connection[3] = connect.isSourceMutingTarget();
        connection[4] = connect.canSourceDm();
        return connection;
    }


    /**
     * Follow Twitter user
     *
     * @param userID User ID
     * @return Twitter User
     * @throws TwitterException if Access is unavailable
     */
    public TwitterUser followUser(long userID) throws TwitterException {
        return new TwitterUser(twitter.createFriendship(userID));
    }


    /**
     * Unfollow Twitter user
     *
     * @param userID User ID
     * @return Twitter User
     * @throws TwitterException if Access is unavailable
     */
    public TwitterUser unfollowUser(long userID) throws TwitterException {
        return new TwitterUser(twitter.destroyFriendship(userID));
    }


    /**
     * Block Twitter user
     *
     * @param UserID User ID
     * @return Twitter User
     * @throws TwitterException if Access is unavailable
     */
    public TwitterUser blockUser(long UserID) throws TwitterException {
        return new TwitterUser(twitter.createBlock(UserID));
    }


    /**
     * Unblock Twitter user
     *
     * @param UserID User ID
     * @return Twitter User
     * @throws TwitterException if Access is unavailable
     */
    public TwitterUser unblockUser(long UserID) throws TwitterException {
        return new TwitterUser(twitter.destroyBlock(UserID));
    }


    /**
     * Mute Twitter user
     *
     * @param UserID User ID
     * @return Twitter User
     * @throws TwitterException if Access is unavailable
     */
    public TwitterUser muteUser(long UserID) throws TwitterException {
        return new TwitterUser(twitter.createMute(UserID));
    }


    /**
     * Unmute Twitter user
     *
     * @param UserID User ID
     * @return Twitter User
     * @throws TwitterException if Access is unavailable
     */
    public TwitterUser unmuteUser(long UserID) throws TwitterException {
        return new TwitterUser(twitter.destroyMute(UserID));
    }


    /**
     * get Following User List
     *
     * @param userId User ID
     * @return List of Following User
     * @throws TwitterException if Access is unavailable
     */
    public List<TwitterUser> getFollowing(long userId, long cursor) throws TwitterException {
        IDs userIDs = twitter.getFriendsIDs(userId, cursor, load);
        long[] ids = userIDs.getIDs();
        if (ids.length == 0)
            return new LinkedList<>();
        return convertUserList(twitter.lookupUsers(ids));
    }


    /**
     * get Follower
     *
     * @param userId User ID
     * @return List of Follower
     * @throws TwitterException if Access is unavailable
     */
    public List<TwitterUser> getFollower(long userId, long cursor) throws TwitterException {
        IDs userIDs = twitter.getFollowersIDs(userId, cursor, load);
        long[] ids = userIDs.getIDs();
        if (ids.length == 0)
            return new LinkedList<>();
        return convertUserList(twitter.lookupUsers(userIDs.getIDs()));
    }


    /**
     * send tweet
     * @param tweet Tweet holder
     * @throws TwitterException if twitter service is unavailable
     * @throws FileNotFoundException if file was not found
     */
    public void uploadStatus(TweetHolder tweet) throws TwitterException, FileNotFoundException {
        StatusUpdate mStatus = new StatusUpdate(tweet.getText());
        if (tweet.isReply())
            mStatus.setInReplyToStatusId(tweet.getReplyId());
        if (tweet.hasImages()) {
            long[] ids = uploadImages(tweet.getImageLink());
            mStatus.setMediaIds(ids);
        } else if (tweet.hasVideo()) {
            long[] ids = uploadVideo(tweet.getVideoLink());
            mStatus.setMediaIds(ids);
        }
        twitter.updateStatus(mStatus);
    }


    /**
     * Get Tweet
     *
     * @param tweetId Tweet ID
     * @return Tweet Object
     * @throws TwitterException if Access is unavailable
     */
    public Tweet getStatus(long tweetId) throws TwitterException {
        Status tweet = twitter.showStatus(tweetId);
        return new Tweet(tweet);
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
        List<Status> answers = new LinkedList<>();
        Query query = new Query("to:" + name + " since_id:" + sinceId + " -filter:retweets");
        query.setCount(load);
        QueryResult result = twitter.search(query);

        List<Status> stats = result.getTweets();
        for (Status reply : stats) {
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
        if (tweet.isRetweeted()) {
            tweet = twitter.unRetweetStatus(tweet.getId());
            return new Tweet(tweet).removeRetweet();
        } else {
            tweet = twitter.retweetStatus(tweet.getId()).getRetweetedStatus();
            return new Tweet(tweet);
        }
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

        return new Tweet(tweet);
    }


    /**
     * @param tweetId Tweet ID
     * @throws TwitterException if Access is unavailable
     */
    public void deleteTweet(long tweetId) throws TwitterException {
        twitter.destroyStatus(tweetId);
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
        if (userIds.length == 0)
            return new LinkedList<>();
        return convertUserList(twitter.lookupUsers(userIds));
    }


    /**
     * get list of Direct Messages
     *
     * @return DM List
     * @throws TwitterException if access is unavailable
     */
    public List<Message> getMessages() throws TwitterException {
        List<DirectMessage> dmList = twitter.getDirectMessages(load);
        List<Message> result = new LinkedList<>();
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
     * Update user profile
     *
     * @param name     new Username
     * @param url      new link
     * @param location new location
     * @param bio      new bio
     * @return updated user profile
     * @throws TwitterException if Access is unavailable
     */
    public TwitterUser updateProfile(String name, String url, String location, String bio) throws TwitterException {
        User user = twitter.updateProfile(name, url, location, bio);
        return new TwitterUser(user);
    }


    /**
     * Update user profile image_add
     *
     * @param image image_add file
     * @throws TwitterException if Access is unavailable
     */
    public void updateProfileImage(File image) throws TwitterException {
        twitter.updateProfileImage(image);
    }


    /**
     * convert #twitter4j.User to TwitterUser List
     *
     * @param users Twitter4J user List
     * @return TwitterUser
     */
    private List<TwitterUser> convertUserList(List<User> users) {
        List<TwitterUser> result = new LinkedList<>();
        for (User user : users) {
            TwitterUser item = new TwitterUser(user);
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
        List<Tweet> result = new LinkedList<>();
        for (Status status : statuses)
            result.add(new Tweet(status));
        return result;
    }


    /**
     * @param dm Twitter4J directmessage
     * @return dm item
     */
    private Message getMessage(DirectMessage dm) throws TwitterException {
        User sender = twitter.showUser(dm.getSenderId());
        User receiver = twitter.showUser(dm.getRecipientId());
        return new Message(dm, sender, receiver);
    }


    /**
     * Upload image to twitter and return unique media IDs
     *
     * @param paths Image Paths
     * @return Media ID array
     * @throws TwitterException      if twitter service is unavailable
     * @throws FileNotFoundException if file was not found
     */
    private long[] uploadImages(String[] paths) throws TwitterException, FileNotFoundException {
        long[] ids = new long[paths.length];
        int i = 0;
        for (String path : paths) {
            File file = new File(path);
            UploadedMedia media = twitter.uploadMedia(file.getName(), new FileInputStream(file));
            ids[i++] = media.getMediaId();
        }
        return ids;
    }


    /**
     * Upload video or gif to twitter and return unique media ID
     *
     * @param path path of video or gif
     * @return media ID
     * @throws TwitterException      if twitter service is unavailable
     * @throws FileNotFoundException if file was not found
     */
    private long[] uploadVideo(String path) throws TwitterException, FileNotFoundException {
        File file = new File(path);
        UploadedMedia media = twitter.uploadMediaChunked(file.getName(), new FileInputStream(file));
        return new long[]{media.getMediaId()};
    }
}