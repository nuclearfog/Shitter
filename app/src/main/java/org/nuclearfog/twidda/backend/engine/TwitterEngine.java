package org.nuclearfog.twidda.backend.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.holder.ListHolder;
import org.nuclearfog.twidda.backend.holder.TweetHolder;
import org.nuclearfog.twidda.backend.lists.MessageList;
import org.nuclearfog.twidda.backend.lists.UserList;
import org.nuclearfog.twidda.backend.lists.UserLists;
import org.nuclearfog.twidda.backend.model.Message;
import org.nuclearfog.twidda.backend.model.Relation;
import org.nuclearfog.twidda.backend.model.Trend;
import org.nuclearfog.twidda.backend.model.TrendLocation;
import org.nuclearfog.twidda.backend.model.Tweet;
import org.nuclearfog.twidda.backend.model.TwitterList;
import org.nuclearfog.twidda.backend.model.User;
import org.nuclearfog.twidda.database.AccountDatabase;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import io.michaelrocks.paranoid.Obfuscate;
import twitter4j.DirectMessage;
import twitter4j.DirectMessageList;
import twitter4j.GeoLocation;
import twitter4j.IDs;
import twitter4j.Location;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UploadedMedia;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Backend for the Twitter API. All app actions are managed here.
 *
 * @author nuclearfog
 */
@Obfuscate
public class TwitterEngine {

    private static final TwitterEngine mTwitter = new TwitterEngine();

    @Nullable
    private RequestToken reqToken;
    private GlobalSettings settings;
    private AccountDatabase accountDB;
    private Twitter twitter;

    private boolean isInitialized = false;


    private TwitterEngine() {
    }

    /**
     * Initialize Twitter4J instance
     */
    private void initTwitter(AccessToken aToken) {
        TLSSocketFactory.getSupportTLSifNeeded();
        ConfigurationBuilder builder = new ConfigurationBuilder();
        // set API keys
        if (settings.isCustomApiSet()) {
            builder.setOAuthConsumerKey(settings.getConsumerKey());
            builder.setOAuthConsumerSecret(settings.getConsumerSecret());
        } else {
            builder.setOAuthConsumerKey("");
            builder.setOAuthConsumerSecret("");
        }
        // Twitter4J has its own proxy settings
        if (settings.isProxyEnabled()) {
            builder.setHttpProxyHost(settings.getProxyHost());
            builder.setHttpProxyPort(settings.getProxyPortNumber());
            if (settings.isProxyAuthSet()) {
                builder.setHttpProxyUser(settings.getProxyUser());
                builder.setHttpProxyPassword(settings.getProxyPass());
            }
        }
        // init proxy connection
        ProxySetup.setConnection(settings);
        // init Twitter instance
        TwitterFactory factory = new TwitterFactory(builder.build());
        if (aToken != null) {
            twitter = factory.getInstance(aToken);
        } else {
            twitter = factory.getInstance();
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
            mTwitter.isInitialized = true;
            // initialize database and settings
            mTwitter.settings = GlobalSettings.getInstance(context);
            mTwitter.accountDB = AccountDatabase.getInstance(context);
            // check if already logged in
            if (mTwitter.settings.isLoggedIn()) {
                // init login access
                String[] keys = mTwitter.settings.getCurrentUserAccessToken();
                AccessToken token = new AccessToken(keys[0], keys[1]);
                mTwitter.initTwitter(token);
            } else {
                // init empty session
                mTwitter.initTwitter(null);
            }
        }
        return mTwitter;
    }

    /**
     * get singleton instance with empty session
     *
     * @return TwitterEngine Instance
     */
    public static TwitterEngine getEmptyInstance(Context context) {
        // initialize storage
        mTwitter.settings = GlobalSettings.getInstance(context);
        mTwitter.accountDB = AccountDatabase.getInstance(context);
        // init empty session
        mTwitter.isInitialized = false;
        mTwitter.initTwitter(null);
        return mTwitter;
    }

    /**
     * reset Twitter state
     */
    public static void resetTwitter() {
        mTwitter.isInitialized = false;
    }

    /**
     * Request Registration Website
     *
     * @return Link to App Registration
     * @throws EngineException if internet connection is unavailable
     */
    public String request() throws EngineException {
        try {
            if (reqToken == null) {
                reqToken = twitter.getOAuthRequestToken();
            }
        } catch (Exception err) {
            throw new EngineException(err);
        }
        return reqToken.getAuthenticationURL();
    }

    /**
     * Get account access keys, store them and initialize Twitter login
     *
     * @param twitterPin PIN from the twitter login page, after successful login
     * @throws EngineException if pin is false or request token is null
     */
    public void initialize(String twitterPin) throws EngineException {
        try {
            // check if corresponding request key is valid
            if (reqToken != null) {
                // get login keys
                AccessToken accessToken = twitter.getOAuthAccessToken(reqToken, twitterPin);
                String key1 = accessToken.getToken();
                String key2 = accessToken.getTokenSecret();
                // init twitter login
                initTwitter(new AccessToken(key1, key2));
                // save login to storage and database
                settings.setConnection(key1, key2, twitter.getId());
                accountDB.setLogin(twitter.getId(), key1, key2);
                // request token is not needed anymore
                reqToken = null;
            } else {
                // request token does not exist, open login page first
                throw new EngineException(EngineException.InternalErrorType.TOKENNOTSET);
            }
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Get Home Timeline
     *
     * @param sinceId id of the earliest tweet
     * @param maxId   ID of the oldest tweet
     * @return List of Tweets
     * @throws EngineException if access is unavailable
     */
    public List<Tweet> getHome(long sinceId, long maxId) throws EngineException {
        try {
            Paging paging = new Paging();
            paging.setCount(settings.getListSize());
            if (sinceId > 0)
                paging.setSinceId(sinceId);
            if (maxId > 1)
                paging.setMaxId(maxId - 1);
            List<Status> homeTweets = twitter.getHomeTimeline(paging);
            return convertStatusList(homeTweets);
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Get Mention Tweets
     *
     * @param sinceId id of the earliest tweet
     * @param maxId   ID of the oldest tweet
     * @return List of Mention Tweets
     * @throws EngineException if access is unavailable
     */
    public List<Tweet> getMention(long sinceId, long maxId) throws EngineException {
        try {
            Paging paging = new Paging();
            paging.setCount(settings.getListSize());
            if (sinceId > 0)
                paging.setSinceId(sinceId);
            if (maxId > 1)
                paging.setMaxId(maxId - 1);
            List<Status> mentions = twitter.getMentionsTimeline(paging);
            return convertStatusList(mentions);
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Get Tweet search result
     *
     * @param search  Search String
     * @param sinceId id of the earliest tweet
     * @param maxId   ID of the oldest tweet
     * @return List of Tweets
     * @throws EngineException if access is unavailable
     */
    public List<Tweet> searchTweets(String search, long sinceId, long maxId) throws EngineException {
        try {
            int load = settings.getListSize();
            Query q = new Query();
            q.setQuery(search + " +exclude:retweets");
            q.setCount(load);
            if (sinceId > 0)
                q.setSinceId(sinceId);
            if (maxId > 1)
                q.setMaxId(maxId - 1);
            QueryResult result = twitter.search(q);
            List<Status> results = result.getTweets();
            return convertStatusList(results);
        } catch (Exception err) {
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
    public List<Trend> getTrends(int woeId) throws EngineException {
        try {
            int index = 1;
            List<Trend> result = new LinkedList<>();
            twitter4j.Trend[] trends = twitter.getPlaceTrends(woeId).getTrends();
            for (twitter4j.Trend trend : trends)
                result.add(new Trend(trend, index++));
            return result;
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * get available locations
     *
     * @return list of locations
     * @throws EngineException if access is unavailable
     */
    public List<TrendLocation> getLocations() throws EngineException {
        try {
            List<TrendLocation> result = new LinkedList<>();
            List<Location> locations = twitter.getAvailableTrends();
            for (Location location : locations)
                result.add(new TrendLocation(location));
            return result;
        } catch (Exception err) {
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
    public UserList searchUsers(String search, long cursor) throws EngineException {
        try {
            int currentPage = 1;
            if (cursor > 0)
                currentPage = (int) cursor;
            long prevPage = currentPage - 1;
            long nextPage = currentPage + 1;
            List<User> users = convertUserList(twitter.searchUsers(search, currentPage));
            if (users.size() < 20)
                nextPage = 0;
            UserList result = new UserList(prevPage, nextPage);
            result.addAll(users);
            return result;
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Get User Tweets
     *
     * @param userId  User ID
     * @param sinceId id of the earliest tweet
     * @param maxId   ID of the oldest tweet
     * @return List of User Tweets
     * @throws EngineException if access is unavailable
     */
    public List<Tweet> getUserTweets(long userId, long sinceId, long maxId) throws EngineException {
        try {
            Paging paging = new Paging();
            paging.setCount(settings.getListSize());
            if (sinceId > 0)
                paging.setSinceId(sinceId);
            if (maxId > 1)
                paging.setMaxId(maxId - 1);
            return convertStatusList(twitter.getUserTimeline(userId, paging));
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Get User Tweets
     *
     * @param username screen name of the user
     * @param sinceId  id of the earliest tweet
     * @param maxId    ID of the oldest tweet
     * @return List of User Tweets
     * @throws EngineException if access is unavailable
     */
    public List<Tweet> getUserTweets(String username, long sinceId, long maxId) throws EngineException {
        try {
            Paging paging = new Paging();
            paging.setCount(settings.getListSize());
            if (sinceId > 0)
                paging.setSinceId(sinceId);
            if (maxId > 0)
                paging.setMaxId(maxId - 1);
            return convertStatusList(twitter.getUserTimeline(username, paging));
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Get User Favs
     *
     * @param userId  User ID
     * @param sinceId id of the earliest tweet
     * @param maxId   ID of the oldest tweet
     * @return List of User Favs
     * @throws EngineException if access is unavailable
     */
    public List<Tweet> getUserFavs(long userId, long sinceId, long maxId) throws EngineException {
        try {
            Paging paging = new Paging();
            paging.setCount(settings.getListSize());
            if (sinceId > 0)
                paging.setSinceId(sinceId);
            if (maxId > 1)
                paging.setMaxId(maxId - 1);
            return convertStatusList(twitter.getFavorites(userId, paging));
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Get User Favs
     *
     * @param username screen name of the user
     * @param sinceId  id of the earliest tweet
     * @param maxId    ID of the oldest tweet
     * @return List of User Favs
     * @throws EngineException if access is unavailable
     */
    public List<Tweet> getUserFavs(String username, long sinceId, long maxId) throws EngineException {
        try {
            Paging paging = new Paging();
            paging.setCount(settings.getListSize());
            if (sinceId > 0)
                paging.setSinceId(sinceId);
            if (maxId > 0)
                paging.setMaxId(maxId - 1);
            List<Status> tweets = twitter.getFavorites(username, paging);
            return convertStatusList(tweets);
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Get User
     *
     * @param userId User ID
     * @return User Object
     * @throws EngineException if Access is unavailable
     */
    public User getUser(long userId) throws EngineException {
        try {
            return new User(twitter.showUser(userId), twitter.getId());
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * get a list of users
     *
     * @param users user IDs
     * @return list of users
     * @throws EngineException if Access is unavailable
     */
    public List<User> getUsers(long[] users) throws EngineException {
        try {
            // todo add paging system
            return convertUserList(twitter.lookupUsers(users));
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Get User
     *
     * @param username screen name of the user
     * @return User Object
     * @throws EngineException if Access is unavailable
     */
    public User getUser(String username) throws EngineException {
        try {
            return new User(twitter.showUser(username), twitter.getId());
        } catch (Exception err) {
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
    public Relation getConnection(long userId) throws EngineException {
        try {
            return new Relation(twitter.showFriendship(twitter.getId(), userId));
        } catch (Exception err) {
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
    public User followUser(long userID) throws EngineException {
        try {
            return new User(twitter.createFriendship(userID), twitter.getId());
        } catch (Exception err) {
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
    public User unfollowUser(long userID) throws EngineException {
        try {
            return new User(twitter.destroyFriendship(userID), twitter.getId());
        } catch (Exception err) {
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
    public User blockUser(long UserID) throws EngineException {
        try {
            return new User(twitter.createBlock(UserID), twitter.getId());
        } catch (Exception err) {
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
    public User unblockUser(long UserID) throws EngineException {
        try {
            return new User(twitter.destroyBlock(UserID), twitter.getId());
        } catch (Exception err) {
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
    public User muteUser(long UserID) throws EngineException {
        try {
            return new User(twitter.createMute(UserID), twitter.getId());
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Un-mute Twitter user
     *
     * @param UserID User ID
     * @return Twitter User
     * @throws EngineException if Access is unavailable
     */
    public User unmuteUser(long UserID) throws EngineException {
        try {
            return new User(twitter.destroyMute(UserID), twitter.getId());
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * get Following User List
     *
     * @param userId User ID
     * @return List of Following User with cursors
     * @throws EngineException if Access is unavailable
     */
    public UserList getFollowing(long userId, long cursor) throws EngineException {
        try {
            int load = settings.getListSize();
            IDs userIDs = twitter.getFriendsIDs(userId, cursor, load);
            long[] ids = userIDs.getIDs();
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = userIDs.getNextCursor();
            UserList result = new UserList(prevCursor, nextCursor);
            if (ids.length > 0) {
                result.addAll(convertUserList(twitter.lookupUsers(ids)));
            }
            return result;
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * get Follower
     *
     * @param userId User ID
     * @return List of Follower with cursors attached
     * @throws EngineException if Access is unavailable
     */
    public UserList getFollower(long userId, long cursor) throws EngineException {
        try {
            int load = settings.getListSize();
            IDs userIDs = twitter.getFollowersIDs(userId, cursor, load);
            long[] ids = userIDs.getIDs();
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = userIDs.getNextCursor();
            UserList result = new UserList(prevCursor, nextCursor);
            if (ids.length > 0) {
                result.addAll(convertUserList(twitter.lookupUsers(ids)));
            }
            return result;
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * send tweet
     *
     * @param tweet Tweet holder
     * @throws EngineException if twitter service is unavailable
     */
    public void uploadStatus(TweetHolder tweet, long[] mediaIds) throws EngineException {
        try {
            StatusUpdate mStatus = new StatusUpdate(tweet.getText());
            if (tweet.isReply())
                mStatus.setInReplyToStatusId(tweet.getReplyId());
            if (tweet.hasLocation())
                mStatus.setLocation(new GeoLocation(tweet.getLatitude(), tweet.getLongitude()));
            if (mediaIds.length > 0)
                mStatus.setMediaIds(mediaIds);
            twitter.updateStatus(mStatus);
        } catch (Exception err) {
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
    public Tweet getStatus(long tweetId) throws EngineException {
        try {
            Status tweet = twitter.showStatus(tweetId);
            return new Tweet(tweet, twitter.getId());
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Get replies to a specific tweet
     *
     * @param name    screen name of receiver
     * @param tweetId tweet ID
     * @param sinceId ID of the last tweet reply
     * @param maxId   ID of the earliest tweet reply
     * @return List of tweet answers
     * @throws EngineException if Access is unavailable
     */
    public List<Tweet> getReplies(String name, long tweetId, long sinceId, long maxId) throws EngineException {
        try {
            int load = settings.getListSize();
            List<Status> answers = new LinkedList<>();
            Query query = new Query("to:" + name + " +exclude:retweets");
            query.setCount(100); // get max count of replies
            if (sinceId > 0)
                query.setSinceId(sinceId);  //
            else
                query.setSinceId(tweetId);  // only search for Tweets created after this Tweet
            if (maxId > 1)
                query.setMaxId(maxId - 1);
            query.setResultType(Query.RECENT);
            QueryResult result = twitter.search(query);
            List<Status> stats = result.getTweets();
            for (int i = 0; i < stats.size() && i < load; i++) {
                Status reply = stats.get(i);
                if (reply.getInReplyToStatusId() == tweetId) {
                    answers.add(reply);
                }
            }
            return convertStatusList(answers);
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }


    /**
     * Retweet Type
     *
     * @param tweetId Tweet ID
     * @param retweet true to retweet this tweet
     * @return updated tweet
     * @throws EngineException if Access is unavailable
     */
    public Tweet retweet(long tweetId, boolean retweet) throws EngineException {
        try {
            Status tweet = twitter.showStatus(tweetId);
            Status embedded = tweet.getRetweetedStatus();
            int retweetCount = tweet.getRetweetCount();
            int favoriteCount = tweet.getFavoriteCount();
            if (embedded != null) {
                tweetId = embedded.getId();
                retweetCount = embedded.getRetweetCount();
                favoriteCount = embedded.getFavoriteCount();
            }
            if (retweet) {
                twitter.retweetStatus(tweetId);
                retweetCount++;
            } else {
                twitter.unRetweetStatus(tweetId);
                if (retweetCount > 0)
                    retweetCount--;
            }
            return new Tweet(tweet, twitter.getId(), tweet.getCurrentUserRetweetId(), retweetCount,
                    retweet, favoriteCount, tweet.isFavorited());
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }


    /**
     * favorite Tweet
     *
     * @param tweetId  Tweet ID
     * @param favorite true to favorite this tweet
     * @return updated tweet
     * @throws EngineException if Access is unavailable
     */
    public Tweet favorite(long tweetId, boolean favorite) throws EngineException {
        try {
            Status tweet = twitter.showStatus(tweetId);
            Status embedded = tweet.getRetweetedStatus();
            int retweetCount = tweet.getRetweetCount();
            int favoriteCount = tweet.getFavoriteCount();
            if (embedded != null) {
                tweetId = embedded.getId();
                retweetCount = embedded.getRetweetCount();
                favoriteCount = embedded.getFavoriteCount();
            }
            if (favorite) {
                twitter.createFavorite(tweetId);
                favoriteCount++;
            } else {
                twitter.destroyFavorite(tweetId);
                if (favoriteCount > 0)
                    favoriteCount--;
            }
            return new Tweet(tweet, twitter.getId(), tweet.getCurrentUserRetweetId(),
                    retweetCount, tweet.isRetweeted(), favoriteCount, favorite);
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }


    /**
     * delete tweet
     *
     * @param tweetId Tweet ID
     * @return removed tweet
     * @throws EngineException if Access is unavailable
     */
    public Tweet deleteTweet(long tweetId) throws EngineException {
        try {
            // Twitter API returns removed tweet with false information
            // so get the tweet first before delete
            Tweet tweet = new Tweet(twitter.showStatus(tweetId), twitter.getId());
            twitter.destroyStatus(tweetId);
            return tweet;
        } catch (Exception err) {
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
    public UserList getRetweeter(long tweetID, long cursor) throws EngineException {
        try {
            int load = settings.getListSize();
            IDs userIDs = twitter.getRetweeterIds(tweetID, load, cursor);
            long[] ids = userIDs.getIDs();
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = userIDs.getNextCursor(); // fixme next cursor always zero
            UserList result = new UserList(prevCursor, nextCursor);
            if (ids.length > 0) {
                result.addAll(convertUserList(twitter.lookupUsers(ids)));
            }
            return result;
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }


    /**
     * get list of Direct Messages
     *
     * @param cursor list cursor
     * @return list of messages
     * @throws EngineException if access is unavailable
     */
    public MessageList getMessages(@Nullable String cursor) throws EngineException {
        try {
            DirectMessageList dmList;
            int load = settings.getListSize();
            if (cursor != null) {
                dmList = twitter.getDirectMessages(load, cursor);
            } else {
                dmList = twitter.getDirectMessages(load);
            }
            MessageList result = new MessageList(cursor, dmList.getNextCursor());
            HashMap<Long, User> userMap = new HashMap<Long, User>();

            for (DirectMessage dm : dmList) {
                try {
                    // get sender of the message
                    User sender;
                    if (userMap.containsKey(dm.getSenderId())) {
                        // recycle user information
                        sender = userMap.get(dm.getSenderId());
                    } else {
                        // download new user information
                        sender = getUser(dm.getSenderId());
                        userMap.put(dm.getSenderId(), sender);

                    }
                    // get receiver of the message
                    User receiver;
                    if (userMap.containsKey(dm.getRecipientId())) {
                        // recycle user information
                        receiver = userMap.get(dm.getRecipientId());
                    } else {
                        // download new user information
                        receiver = getUser(dm.getRecipientId());
                        userMap.put(dm.getRecipientId(), receiver);
                    }
                    // build message and add to list
                    Message message = new Message(dm, sender, receiver);
                    result.add(message);
                } catch (EngineException err) {
                    // ignore messages from suspended/deleted users
                }
            }
            return result;
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * send direct message to an user
     *
     * @param username screen name of the user
     * @param message  message text
     * @param mediaId  media ID referenced by Twitter
     * @throws EngineException if access is unavailable
     */
    public void sendDirectMessage(String username, String message, long mediaId) throws EngineException {
        try {
            if (mediaId > 0) {
                long userId = twitter.showUser(username).getId();
                twitter.sendDirectMessage(userId, message, mediaId);
            } else {
                twitter.sendDirectMessage(username, message);
            }
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Delete Direct Message
     *
     * @param id Message ID
     * @throws EngineException if Access is unavailable or message not found
     */
    public void deleteMessage(long id) throws EngineException {
        try {
            twitter.destroyDirectMessage(id);
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * update current users profile
     *
     * @param name       new username
     * @param url        new profile link or empty if none
     * @param loc        new location name or empty if none
     * @param bio        new bio description or empty if none
     * @param profileImg local path to the profile image or null
     * @param bannerImg  local path to the banner image or null
     * @return updated user information
     * @throws EngineException if access is unavailable
     */
    public User updateProfile(String name, String url, String loc, String bio, String profileImg, String bannerImg) throws EngineException {
        try {
            if (profileImg != null && !profileImg.isEmpty())
                twitter.updateProfileImage(new File(profileImg));
            if (bannerImg != null && !bannerImg.isEmpty())
                twitter.updateProfileBanner(new File(bannerImg));
            twitter4j.User user = twitter.updateProfile(name, url, loc, bio);
            return new User(user, twitter.getId());
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * get user list
     *
     * @param userId id of the list owner
     * @param cursor list cursor to set the start point
     * @return list information
     * @throws EngineException if access is unavailable
     */
    public UserLists getUserList(long userId, String username, long cursor) throws EngineException {
        try {
            List<twitter4j.UserList> lists;
            if (userId > 0)
                lists = twitter.getUserLists(userId);
            else
                lists = twitter.getUserLists(username);
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = 0;
            UserLists result = new UserLists(prevCursor, nextCursor); // todo add paging system
            for (twitter4j.UserList list : lists)
                result.add(new TwitterList(list, twitter.getId()));
            return result;
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * get the lists the user has been added to
     *
     * @param userId   ID of the user
     * @param username alternative to userId if id is '0'
     * @param cursor   list cursor
     * @return a list of user lists
     * @throws EngineException if access is unavailable
     */
    public UserLists getUserListMemberships(long userId, String username, long cursor) throws EngineException {
        try {
            int count = settings.getListSize();
            PagableResponseList<twitter4j.UserList> lists;
            if (userId > 0)
                lists = twitter.getUserListMemberships(userId, count, cursor);
            else
                lists = twitter.getUserListMemberships(username, count, cursor);
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = lists.getNextCursor();
            UserLists result = new UserLists(prevCursor, nextCursor);
            for (twitter4j.UserList list : lists)
                result.add(new TwitterList(list, twitter.getId()));
            return result;
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * load user list information
     *
     * @param listId ID of the userlist
     * @return list information
     * @throws EngineException if access is unavailable
     */
    public TwitterList loadUserList(long listId) throws EngineException {
        try {
            return new TwitterList(twitter.showUserList(listId), twitter.getId());
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Follow action for twitter list
     *
     * @param listId ID of the list
     * @param follow ID of the list
     * @return List information
     * @throws EngineException if access is unavailable
     */
    public TwitterList followUserList(long listId, boolean follow) throws EngineException {
        try {
            twitter4j.UserList list;
            if (follow) {
                list = twitter.createUserListSubscription(listId);
            } else {
                list = twitter.destroyUserListSubscription(listId);
            }
            return new TwitterList(list, twitter.getId(), follow);
        } catch (Exception err) {
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
    public TwitterList deleteUserList(long listId) throws EngineException {
        try {
            return new TwitterList(twitter.destroyUserList(listId), twitter.getId());
        } catch (Exception err) {
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
    public UserList getListFollower(long listId, long cursor) throws EngineException {
        try {
            PagableResponseList<twitter4j.User> followerList = twitter.getUserListSubscribers(listId, cursor);
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = followerList.getNextCursor();
            UserList result = new UserList(prevCursor, nextCursor);
            result.addAll(convertUserList(followerList));
            return result;
        } catch (Exception err) {
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
    public UserList getListMember(long listId, long cursor) throws EngineException {
        try {
            PagableResponseList<twitter4j.User> users = twitter.getUserListMembers(listId, cursor);
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = users.getNextCursor();
            UserList result = new UserList(prevCursor, nextCursor);
            result.addAll(convertUserList(users));
            return result;
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * get tweets of a lists
     *
     * @param listId  ID of the list
     * @param sinceId id of the earliest tweet
     * @param maxId   ID of the oldest tweet
     * @return list of tweets
     * @throws EngineException if access is unavailable
     */
    public List<Tweet> getListTweets(long listId, long sinceId, long maxId) throws EngineException {
        try {
            Paging paging = new Paging();
            paging.setCount(settings.getListSize());
            if (sinceId > 0)
                paging.setSinceId(sinceId);
            if (maxId > 1)
                paging.setMaxId(maxId - 1);
            return convertStatusList(twitter.getUserListStatuses(listId, paging));
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * download image from Twitter
     *
     * @param link link of the image
     * @return bitmap image
     * @throws EngineException if image loading failed
     */
    @Nullable
    public Bitmap getImage(String link) throws EngineException {
        try {
            URL url = new URL(link);
            InputStream stream = url.openConnection().getInputStream();
            return BitmapFactory.decodeStream(stream);
        } catch (IOException err) {
            throw new EngineException(EngineException.InternalErrorType.BITMAP_FAILURE);
        }
    }

    /**
     * upload image to twitter and return unique media ID
     *
     * @param path path to the media file
     * @return media ID
     * @throws EngineException if twitter service is unavailable or media not found
     */
    public long uploadImage(@NonNull String path) throws EngineException {
        try {
            File file = new File(path);
            UploadedMedia media = twitter.uploadMedia(file.getName(), new FileInputStream(file));
            return media.getMediaId();
        } catch (FileNotFoundException err) {
            throw new EngineException(EngineException.InternalErrorType.FILENOTFOUND);
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * Upload video to twitter and return unique media ID
     *
     * @param path path to the media file
     * @return media ID
     * @throws EngineException if twitter service is unavailable or media not found
     */
    public long uploadVideo(@NonNull String path) throws EngineException {
        try {
            File file = new File(path);
            UploadedMedia media = twitter.uploadMediaChunked(file.getName(), new FileInputStream(file));
            return media.getMediaId();
        } catch (FileNotFoundException err) {
            throw new EngineException(EngineException.InternalErrorType.FILENOTFOUND);
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * creates an user list
     *
     * @param list holder for list information
     * @throws EngineException if access is unavailable
     */
    public TwitterList updateUserList(ListHolder list) throws EngineException {
        try {
            twitter4j.UserList result;
            if (list.exists()) {
                result = twitter.updateUserList(list.getId(), list.getTitle(), list.isPublic(), list.getDescription());
            } else {
                result = twitter.createUserList(list.getTitle(), list.isPublic(), list.getDescription());
            }
            return new TwitterList(result, twitter.getId());
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * adds user  to an user list
     *
     * @param listId    I of the list
     * @param usernames screen names of the users
     * @throws EngineException if access is unavailable
     */
    public void addUserToList(long listId, String[] usernames) throws EngineException {
        try {
            twitter.createUserListMembers(listId, usernames);
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * removes an user from user list
     *
     * @param listId   I of the list
     * @param username screen names of an user
     * @throws EngineException if access is unavailable
     */
    public void delUserFromList(long listId, String username) throws EngineException {
        try {
            twitter.destroyUserListMember(listId, username);
        } catch (Exception err) {
            throw new EngineException(err);
        }
    }

    /**
     * convert #twitter4j.User to User List
     *
     * @param users Twitter4J user List
     * @return User
     */
    private List<User> convertUserList(List<twitter4j.User> users) throws TwitterException {
        ArrayList<User> result = new ArrayList<>();
        result.ensureCapacity(users.size());
        for (twitter4j.User user : users) {
            User item = new User(user, twitter.getId());
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
    private List<Tweet> convertStatusList(List<Status> statuses) throws TwitterException {
        ArrayList<Tweet> result = new ArrayList<>();
        result.ensureCapacity(statuses.size());
        for (Status status : statuses)
            result.add(new Tweet(status, twitter.getId()));
        return result;
    }
}