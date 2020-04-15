package org.nuclearfog.twidda.backend;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.backend.items.MessageHolder;
import org.nuclearfog.twidda.backend.items.TrendLocation;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.TweetHolder;
import org.nuclearfog.twidda.backend.items.TwitterList;
import org.nuclearfog.twidda.backend.items.TwitterTrend;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.items.UserHolder;
import org.nuclearfog.twidda.backend.items.UserProperties;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.LinkedList;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.GeoLocation;
import twitter4j.IDs;
import twitter4j.Location;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Trend;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UploadedMedia;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterEngine {

    private static final TwitterEngine mTwitter = new TwitterEngine();

    private Twitter twitter;
    private long twitterID;
    private boolean isInitialized = false;
    private GlobalSettings settings;
    @Nullable
    private RequestToken reqToken;
    @Nullable
    private AccessToken aToken;


    private TwitterEngine() {
    }


    /**
     * Initialize Twitter4J instance
     */
    private void initTwitter() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(BuildConfig.API_KEY_1);
        builder.setOAuthConsumerSecret(BuildConfig.API_KEY_2);
        // Twitter4J has its own proxy settings
        if (settings.isProxyServerSet()) {
            builder.setHttpProxyHost(settings.getProxyHost());
            builder.setHttpProxyPort(Integer.parseInt(settings.getProxyPort()));
            if (settings.isProxyLoginSet()) {
                builder.setHttpProxyUser(settings.getProxyUser());
                builder.setHttpProxyPassword(settings.getProxyPass());
            }
        }
        TwitterFactory factory = new TwitterFactory(builder.build());
        if (aToken != null)
            twitter = factory.getInstance(aToken);
        else
            twitter = factory.getInstance();
        initJVMProxy();
    }

    /**
     * Initialize App proxy
     */
    private void initJVMProxy() {
        try {
            if (settings.isProxyServerSet()) {
                System.setProperty("https.proxyHost", settings.getProxyHost());
                System.setProperty("https.proxyPort", settings.getProxyPort());
                if (settings.isProxyLoginSet()) {
                    System.setProperty("https.proxyUser", settings.getProxyUser());
                    System.setProperty("https.proxyPassword", settings.getProxyPass());
                    Authenticator.setDefault(new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(settings.getProxyUser(), settings.getProxyPass().toCharArray());
                        }
                    });
                }
            } else {
                System.clearProperty("https.proxyHost");
                System.clearProperty("https.proxyPort");
                System.clearProperty("https.proxyUser");
                System.clearProperty("https.proxyPassword");
            }
        } catch (SecurityException sErr) {
            sErr.printStackTrace();
        }
    }


    /**
     * get singleton instance
     *
     * @param context Main Thread Context
     * @return TwitterEngine Instance
     */
    public static TwitterEngine getInstance(Context context) {
        if (!mTwitter.isInitialized) {
            mTwitter.settings = GlobalSettings.getInstance(context);
            if (mTwitter.settings.getLogin()) {
                String[] keys = mTwitter.settings.getKeys();
                mTwitter.aToken = new AccessToken(keys[0], keys[1]);
                mTwitter.twitterID = mTwitter.settings.getUserId();
            }
            mTwitter.initTwitter();
            mTwitter.isInitialized = true;
        }
        return mTwitter;
    }


    /**
     * reset Twitter state
     */
    public static void resetTwitter() {
        mTwitter.isInitialized = false;
        mTwitter.reqToken = null;   // Destroy connections
        mTwitter.aToken = null;     //
    }


    /**
     * Request Registration Website
     *
     * @return Link to App Registration
     * @throws EngineException if internet connection is unavailable
     */
    String request() throws EngineException {
        try {
            if (reqToken == null)
                reqToken = twitter.getOAuthRequestToken();
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
        return reqToken.getAuthenticationURL();
    }


    /**
     * Get Access-Token, store and initialize Twitter
     *
     * @param twitterPin PIN for accessing account
     * @throws EngineException if pin is false or request token is null
     */
    void initialize(String twitterPin) throws EngineException {
        try {
            if (reqToken != null) {
                AccessToken accessToken = twitter.getOAuthAccessToken(reqToken, twitterPin);
                String key1 = accessToken.getToken();
                String key2 = accessToken.getTokenSecret();
                aToken = new AccessToken(key1, key2);
                initTwitter();
                twitterID = twitter.getId();
                settings.setConnection(key1, key2, twitterID);
            } else {
                throw new EngineException(EngineException.TOKENNOTSET);
            }
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Get Home Timeline
     *
     * @param page   current page
     * @param lastId Tweet ID of the earliest Tweet
     * @return List of Tweets
     * @throws EngineException if access is unavailable
     */
    List<Tweet> getHome(int page, long lastId) throws EngineException {
        try {
            int load = settings.getRowLimit();
            List<Status> homeTweets = twitter.getHomeTimeline(new Paging(page, load, lastId));
            return convertStatusList(homeTweets);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Get Mention Tweets
     *
     * @param page current page
     * @param id   ID of the earliest Tweet
     * @return List of Mention Tweets
     * @throws EngineException if access is unavailable
     */
    List<Tweet> getMention(int page, long id) throws EngineException {
        try {
            int load = settings.getRowLimit();
            List<Status> mentions = twitter.getMentionsTimeline(new Paging(page, load, id));
            return convertStatusList(mentions);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Get Tweet search result
     *
     * @param search Search String
     * @param id     Since ID
     * @return List of Tweets
     * @throws EngineException if acces is unavailable
     */
    List<Tweet> searchTweets(String search, long id) throws EngineException {
        try {
            int load = settings.getRowLimit();
            Query q = new Query();
            q.setQuery(search + " +exclude:retweets");
            q.setCount(load);
            q.setSinceId(id);
            QueryResult result = twitter.search(q);
            List<Status> results = result.getTweets();
            return convertStatusList(results);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Get Trending Hashtags
     *
     * @param woeId Yahoo World ID
     * @return Trend Resource
     * @throws EngineException if access is unavailable
     */
    List<TwitterTrend> getTrends(int woeId) throws EngineException {
        try {
            int index = 1;
            List<TwitterTrend> result = new LinkedList<>();
            Trend[] trends = twitter.getPlaceTrends(woeId).getTrends();
            for (Trend trend : trends)
                result.add(new TwitterTrend(trend, index++));
            return result;
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * get available locations
     *
     * @return list of locations
     * @throws EngineException if access is unavailable
     */
    List<TrendLocation> getLocations() throws EngineException {
        try {
            List<TrendLocation> result = new LinkedList<>();
            List<Location> locations = twitter.getAvailableTrends();
            for (Location location : locations)
                result.add(new TrendLocation(location));
            return result;
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Get User search result
     *
     * @param search Search String
     * @return List of Users
     * @throws EngineException if access is unavailable
     */
    List<TwitterUser> searchUsers(String search) throws EngineException {
        try {
            return convertUserList(twitter.searchUsers(search, -1));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Get User Tweets
     *
     * @param userId  User ID
     * @param sinceId minimum tweet ID
     * @param page    current page
     * @return List of User Tweets
     * @throws EngineException if access is unavailable
     */
    List<Tweet> getUserTweets(long userId, long sinceId, int page) throws EngineException {
        try {
            int load = settings.getRowLimit();
            Paging paging = new Paging(page, load, sinceId);
            return convertStatusList(twitter.getUserTimeline(userId, paging));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Get User Favs
     *
     * @param userId  User ID
     * @param page    current page
     * @return List of User Favs
     * @throws EngineException if access is unavailable
     */
    List<Tweet> getUserFavs(long userId, int page) throws EngineException {
        try {
            int load = settings.getRowLimit();
            Paging paging = new Paging(page, load);
            List<Status> favorits = twitter.getFavorites(userId, paging);
            return convertStatusList(favorits);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Get User Context
     *
     * @param userId User ID
     * @return User Object
     * @throws EngineException if Access is unavailable
     */
    TwitterUser getUser(long userId) throws EngineException {
        try {
            return new TwitterUser(twitter.showUser(userId));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Get current user
     *
     * @return curent user
     * @throws EngineException if Access is unavailable
     */
    TwitterUser getCurrentUser() throws EngineException {
        try {
            return new TwitterUser(twitter.showUser(twitterID));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Efficient Access of Connection Information
     *
     * @param userId User ID compared with Home ID
     * @return User Properties
     * @throws EngineException if Connection is unavailable
     */
    UserProperties getConnection(long userId) throws EngineException {
        try {
            return new UserProperties(twitter.showFriendship(twitterID, userId));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Follow Twitter user
     *
     * @param userID User ID
     * @return Twitter User
     * @throws EngineException if Access is unavailable
     */
    TwitterUser followUser(long userID) throws EngineException {
        try {
            return new TwitterUser(twitter.createFriendship(userID));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Unfollow Twitter user
     *
     * @param userID User ID
     * @return Twitter User
     * @throws EngineException if Access is unavailable
     */
    TwitterUser unfollowUser(long userID) throws EngineException {
        try {
            return new TwitterUser(twitter.destroyFriendship(userID));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Block Twitter user
     *
     * @param UserID User ID
     * @return Twitter User
     * @throws EngineException if Access is unavailable
     */
    TwitterUser blockUser(long UserID) throws EngineException {
        try {
            return new TwitterUser(twitter.createBlock(UserID));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Unblock Twitter user
     *
     * @param UserID User ID
     * @return Twitter User
     * @throws EngineException if Access is unavailable
     */
    TwitterUser unblockUser(long UserID) throws EngineException {
        try {
            return new TwitterUser(twitter.destroyBlock(UserID));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Mute Twitter user
     *
     * @param UserID User ID
     * @return Twitter User
     * @throws EngineException if Access is unavailable
     */
    TwitterUser muteUser(long UserID) throws EngineException {
        try {
            return new TwitterUser(twitter.createMute(UserID));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Unmute Twitter user
     *
     * @param UserID User ID
     * @return Twitter User
     * @throws EngineException if Access is unavailable
     */
    TwitterUser unmuteUser(long UserID) throws EngineException {
        try {
            return new TwitterUser(twitter.destroyMute(UserID));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * get Following User List
     *
     * @param userId User ID
     * @return List of Following User
     * @throws EngineException if Access is unavailable
     */
    List<TwitterUser> getFollowing(long userId) throws EngineException {
        try {
            int load = settings.getRowLimit();
            IDs userIDs = twitter.getFriendsIDs(userId, -1, load);
            long[] ids = userIDs.getIDs();
            if (ids.length == 0)
                return new LinkedList<>();
            return convertUserList(twitter.lookupUsers(ids));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * get Follower
     *
     * @param userId User ID
     * @return List of Follower
     * @throws EngineException if Access is unavailable
     */
    List<TwitterUser> getFollower(long userId) throws EngineException {
        try {
            int load = settings.getRowLimit();
            IDs userIDs = twitter.getFollowersIDs(userId, -1, load);
            long[] ids = userIDs.getIDs();
            if (ids.length == 0)
                return new LinkedList<>();
            return convertUserList(twitter.lookupUsers(userIDs.getIDs()));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * send tweet
     *
     * @param tweet Tweet holder
     * @throws EngineException if twitter service is unavailable or media was not found
     */
    void uploadStatus(TweetHolder tweet) throws EngineException {
        try {
            StatusUpdate mStatus = new StatusUpdate(tweet.getText());
            if (tweet.isReply())
                mStatus.setInReplyToStatusId(tweet.getReplyId());
            if (tweet.hasLocation())
                mStatus.setLocation(new GeoLocation(tweet.getLatitude(), tweet.getLongitude()));
            if (tweet.hasImages()) {
                long[] ids = uploadImages(tweet.getImageLink());
                mStatus.setMediaIds(ids);
            } else if (tweet.hasVideo()) {
                long id = uploadVideo(tweet.getVideoLink());
                mStatus.setMediaIds(id);
            }
            twitter.updateStatus(mStatus);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Get Tweet
     *
     * @param tweetId Tweet ID
     * @return Tweet Object
     * @throws EngineException if Access is unavailable
     */
    Tweet getStatus(long tweetId) throws EngineException {
        try {
            Status tweet = twitter.showStatus(tweetId);
            return new Tweet(tweet);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Get Answer Tweets
     *
     * @param name    screen name of receiver
     * @param tweetId tweet ID
     * @param sinceId last tweet
     * @return List of Answers
     * @throws EngineException if Access is unavailable
     */
    List<Tweet> getAnswers(String name, long tweetId, long sinceId) throws EngineException {
        try {
            int load = settings.getRowLimit();
            List<Status> answers = new LinkedList<>();
            Query query = new Query("to:" + name + " since_id:" + sinceId + " +exclude:retweets");
            query.setCount(load);
            QueryResult result = twitter.search(query);
            List<Status> stats = result.getTweets();
            for (Status reply : stats) {
                if (reply.getInReplyToStatusId() == tweetId) {
                    answers.add(reply);
                }
            }
            return convertStatusList(answers);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Retweet Action
     *
     * @param tweetId Tweet ID
     * @throws EngineException if Access is unavailable
     */
    Tweet retweet(long tweetId) throws EngineException {
        try {
            Status tweet = twitter.showStatus(tweetId);
            boolean retweeted = tweet.isRetweeted();
            boolean favorited = tweet.isFavorited();
            int retweetCount = tweet.getRetweetCount();
            int favoritCount = tweet.getFavoriteCount();

            if (tweet.isRetweeted()) {
                twitter.unRetweetStatus(tweet.getId());
                if (retweetCount > 0)
                    retweetCount--;
            } else {
                twitter.retweetStatus(tweet.getId());
                retweetCount++;
            }
            return new Tweet(tweet, retweetCount, !retweeted, favoritCount, favorited);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Favorite Action
     *
     * @param tweetId Tweet ID
     * @throws EngineException if Access is unavailable
     */
    Tweet favorite(long tweetId) throws EngineException {
        try {
            Status tweet = twitter.showStatus(tweetId);
            boolean retweeted = tweet.isRetweeted();
            boolean favorited = tweet.isFavorited();
            int retweetCount = tweet.getRetweetCount();
            int favoritCount = tweet.getFavoriteCount();

            if (tweet.isFavorited()) {
                tweet = twitter.destroyFavorite(tweet.getId());
                if (favoritCount > 0)
                    favoritCount--;
            } else {
                tweet = twitter.createFavorite(tweet.getId());
                favoritCount++;
            }
            return new Tweet(tweet, retweetCount, retweeted, favoritCount, !favorited);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * @param tweetId Tweet ID
     * @return dummy tweet
     * @throws EngineException if Access is unavailable
     */
    Tweet deleteTweet(long tweetId) throws EngineException {
        try {
            return new Tweet(twitter.destroyStatus(tweetId));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Get User who retweeted a Tweet
     *
     * @param tweetID Tweet ID
     * @return List of users or empty list if no match
     * @throws EngineException if Access is unavailable
     */
    List<TwitterUser> getRetweeter(long tweetID) throws EngineException {
        try {
            int load = settings.getRowLimit();
            Tweet embeddedStat = getStatus(tweetID).getEmbeddedTweet();
            if (embeddedStat != null)
                tweetID = embeddedStat.getId();
            long[] userIds = twitter.getRetweeterIds(tweetID, load, -1).getIDs();
            if (userIds.length == 0)
                return new LinkedList<>();
            return convertUserList(twitter.lookupUsers(userIds));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * get list of Direct Messages
     *
     * @return DM List
     * @throws EngineException if access is unavailable
     */
    List<Message> getMessages() throws EngineException {
        try {
            int load = settings.getRowLimit();
            List<DirectMessage> dmList = twitter.getDirectMessages(load);
            List<Message> result = new LinkedList<>();
            for (DirectMessage dm : dmList) {
                result.add(getMessage(dm));
            }
            return result;
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Send direct message
     *
     * @param messageHolder message informations
     * @throws EngineException if access is unavailable
     */
    void sendMessage(MessageHolder messageHolder) throws EngineException {
        try {
            long id = twitter.showUser(messageHolder.getUsername()).getId();
            if (messageHolder.hasMedia()) {
                UploadedMedia media = twitter.uploadMedia(new File(messageHolder.getMediaPath()));
                long mediaId = media.getMediaId();
                twitter.sendDirectMessage(id, messageHolder.getMessage(), mediaId);
            } else {
                twitter.sendDirectMessage(id, messageHolder.getMessage());
            }
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Delete Direct Message
     *
     * @param id Message ID
     * @throws EngineException if Access is unavailable or message not found
     */
    void deleteMessage(long id) throws EngineException {
        try {
            twitter.destroyDirectMessage(id);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Update user profile
     *
     * @param userHolder User data
     * @return updated user profile
     * @throws EngineException if Access is unavailable
     */
    TwitterUser updateProfile(UserHolder userHolder) throws EngineException {
        try {
            String username = userHolder.getName();
            String user_link = userHolder.getLink();
            String user_loc = userHolder.getLocation();
            String user_bio = userHolder.getBio();
            User user = twitter.updateProfile(username, user_link, user_loc, user_bio);
            return new TwitterUser(user);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Update user profile image_add
     *
     * @param path image path
     * @throws EngineException if access is unavailable
     */
    void updateProfileImage(String path) throws EngineException {
        try {
            File image = new File(path);
            twitter.updateProfileImage(image);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * get user list
     *
     * @param userId id of the list owner
     * @return list information
     * @throws EngineException if access is unavailable
     */
    List<TwitterList> getUserList(long userId) throws EngineException {
        try {
            List<TwitterList> result = new LinkedList<>();
            ResponseList<UserList> lists = twitter.getUserLists(userId);
            for (UserList list : lists)
                result.add(new TwitterList(list, twitterID));
            return result;
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Follow action for twitter list
     *
     * @param listId ID of the list
     * @return List information
     * @throws EngineException if access is unavailable
     */
    TwitterList followUserList(long listId) throws EngineException {
        try {
            UserList list = twitter.showUserList(listId);
            if (list.isFollowing()) {
                twitter.destroyUserListSubscription(listId);
                return new TwitterList(list, twitterID, false);
            } else {
                twitter.createUserListSubscription(listId);
                return new TwitterList(list, twitterID, true);
            }
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }

    /**
     * Delete User list
     *
     * @param listId ID of the list
     * @return List information
     * @throws EngineException if access is unavailable
     */
    TwitterList deleteUserList(long listId) throws EngineException {
        try {
            return new TwitterList(twitter.destroyUserList(listId), twitterID);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }

    /**
     * Get subscriber of a user list
     *
     * @param listId ID of the list
     * @return list of users following the list
     * @throws EngineException if access is unavailable
     */
    List<TwitterUser> getListFollower(long listId) throws EngineException {
        try {
            return convertUserList(twitter.getUserListSubscribers(listId, -1));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }

    /**
     * Get member of a list
     *
     * @param listId ID of the list
     * @return list of users
     * @throws EngineException if access is unavailable
     */
    List<TwitterUser> getListMember(long listId) throws EngineException {
        try {
            return convertUserList(twitter.getUserListMembers(listId, -1));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * get tweets of a lists
     *
     * @param listId  ID of the list
     * @param sinceId Id of the recent tweet
     * @param page    tweet page
     * @return list of tweets
     * @throws EngineException if access is unavailable
     */
    List<Tweet> getListTweets(long listId, long sinceId, int page) throws EngineException {
        try {
            int load = settings.getRowLimit();
            Paging paging = new Paging(page, load, sinceId);
            return convertStatusList(twitter.getUserListStatuses(listId, paging));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
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
     * @throws EngineException if Access is unavailable
     */
    private Message getMessage(DirectMessage dm) throws EngineException {
        try {
            User sender = twitter.showUser(dm.getSenderId());
            User receiver = twitter.showUser(dm.getRecipientId());
            return new Message(dm, sender, receiver);
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Upload image to twitter and return unique media IDs
     *
     * @param paths Image Paths
     * @return Media ID array
     * @throws EngineException if twitter service is unavailable or media not found
     */
    private long[] uploadImages(String[] paths) throws EngineException {
        try {
            long[] ids = new long[paths.length];
            int i = 0;
            for (String path : paths) {
                File file = new File(path);
                UploadedMedia media = twitter.uploadMedia(file.getName(), new FileInputStream(file));
                ids[i++] = media.getMediaId();
            }
            return ids;
        } catch (TwitterException err) {
            throw new EngineException(err);
        } catch (FileNotFoundException err) {
            throw new EngineException(EngineException.FILENOTFOUND);
        }
    }


    /**
     * Upload video or gif to twitter and return unique media ID
     *
     * @param path path of video or gif
     * @return media ID
     * @throws EngineException if twitter service is unavailable or media not found
     */
    private long uploadVideo(String path) throws EngineException {
        try {
            File file = new File(path);
            UploadedMedia media = twitter.uploadMediaChunked(file.getName(), new FileInputStream(file));
            return media.getMediaId();
        } catch (TwitterException err) {
            throw new EngineException(err);
        } catch (FileNotFoundException err) {
            throw new EngineException(EngineException.FILENOTFOUND);
        }
    }


    /**
     * Internal Exception
     */
    public static class EngineException extends Exception {

        private static final int FILENOTFOUND = 600;
        private static final int TOKENNOTSET = 601;

        @StringRes
        private int messageResource;
        private boolean hardFault = false;
        private boolean statusNotFound = false;

        /**
         * Constructor for Twitter4J errors
         *
         * @param error Twitter4J Exception
         */
        private EngineException(TwitterException error) {
            super(error);
            switch (error.getErrorCode()) {
                case 88:
                case 420:   //
                case 429:   // Rate limit exceeded!
                    messageResource = R.string.error_limit_exceeded;
                    break;

                case 17:
                case 50:    // USER not found
                case 63:    // USER suspended
                    messageResource = R.string.error_user_not_found;
                    statusNotFound = true;
                    hardFault = true;
                    break;

                case 32:
                    messageResource = R.string.error_request_token;
                    break;

                case 34:    //
                case 144:   // TWEET not found
                    messageResource = R.string.error_not_found;
                    statusNotFound = true;
                    hardFault = true;
                    break;

                case 150:
                    messageResource = R.string.error_send_dm;
                    break;

                case 136:
                case 179:
                    messageResource = R.string.info_not_authorized;
                    hardFault = true;
                    break;

                case 186:
                    messageResource = R.string.error_status_too_long;
                    break;

                case 187:
                    messageResource = R.string.error_duplicate_status;
                    break;

                case 349:
                    messageResource = R.string.error_dm_send;
                    break;

                case 354:
                    messageResource = R.string.error_dm_length;
                    break;

                case 89:
                    messageResource = R.string.error_accesstoken;
                    break;

                default:
                    if (error.getStatusCode() == 401)
                        messageResource = R.string.info_not_authorized;
                    else
                        messageResource = R.string.error_connection_failed;
                    break;
            }
        }

        /**
         * Constructor for non Twitter4J errors
         *
         * @param errorCode custom error code
         */
        private EngineException(int errorCode) {
            switch (errorCode) {
                case FILENOTFOUND:
                    messageResource = R.string.error_media_not_found;
                    break;

                case TOKENNOTSET:
                    messageResource = R.string.error_token_not_set;
                    break;

                default:
                    messageResource = R.string.info_error;
                    break;
            }
        }

        /**
         * get String resource of error message
         *
         * @return string recource for
         */
        @StringRes
        int getMessageResource() {
            return messageResource;
        }

        /**
         * return if activity should closed
         *
         * @return true if hard fault
         */
        boolean isHardFault() {
            return hardFault;
        }

        /**
         * return if tweet or author was not found
         *
         * @return true if author or tweet not found
         */
        boolean statusNotFound() {
            return statusNotFound;
        }
    }
}