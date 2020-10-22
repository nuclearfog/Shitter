package org.nuclearfog.twidda.backend.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.backend.holder.ListHolder;
import org.nuclearfog.twidda.backend.holder.MessageHolder;
import org.nuclearfog.twidda.backend.holder.TweetHolder;
import org.nuclearfog.twidda.backend.holder.TwitterUserList;
import org.nuclearfog.twidda.backend.holder.UserHolder;
import org.nuclearfog.twidda.backend.holder.UserListList;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.backend.items.TrendLocation;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.TwitterList;
import org.nuclearfog.twidda.backend.items.TwitterTrend;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.items.UserRelation;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.GeoLocation;
import twitter4j.IDs;
import twitter4j.Location;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
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

/**
 * Backend for twitter API.
 */
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
        if (settings.isProxyEnabled()) {
            builder.setHttpProxyHost(settings.getProxyHost());
            builder.setHttpProxyPort(settings.getProxyPortNumber());
            if (settings.isProxyAuthSet()) {
                builder.setHttpProxyUser(settings.getProxyUser());
                builder.setHttpProxyPassword(settings.getProxyPass());
            }
        }
        TwitterFactory factory = new TwitterFactory(builder.build());
        if (aToken != null) {
            twitter = factory.getInstance(aToken);
        } else {
            twitter = factory.getInstance();
        }
        ProxySetup.setConnection(settings);
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
    public String request() throws EngineException {
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
    public void initialize(String twitterPin) throws EngineException {
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
                throw new EngineException(EngineException.InternalErrorType.TOKENNOTSET);
            }
        } catch (TwitterException err) {
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
        } catch (TwitterException err) {
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
        } catch (TwitterException err) {
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
    public List<TwitterTrend> getTrends(int woeId) throws EngineException {
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
    public List<TrendLocation> getLocations() throws EngineException {
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
    public TwitterUserList searchUsers(String search, long cursor) throws EngineException {
        try {
            int currentPage = 1;
            if (cursor > 0)
                currentPage = (int) cursor;
            long prevPage = currentPage - 1;
            long nextPage = currentPage + 1;
            List<TwitterUser> users = convertUserList(twitter.searchUsers(search, currentPage));
            if (users.size() < 20)
                nextPage = 0;
            TwitterUserList result = new TwitterUserList(prevPage, nextPage);
            result.addAll(users);
            return result;
        } catch (TwitterException err) {
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
        } catch (TwitterException err) {
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
        } catch (TwitterException err) {
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
        } catch (TwitterException err) {
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
    public TwitterUser getUser(long userId) throws EngineException {
        try {
            return new TwitterUser(twitter.showUser(userId));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Get User Context
     *
     * @param username screen name of the user
     * @return User Object
     * @throws EngineException if Access is unavailable
     */
    public TwitterUser getUser(String username) throws EngineException {
        try {
            return new TwitterUser(twitter.showUser(username));
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
    public TwitterUser getCurrentUser() throws EngineException {
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
    public UserRelation getConnection(long userId) throws EngineException {
        try {
            return new UserRelation(twitter.showFriendship(twitterID, userId));
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * Efficient Access of Connection Information
     *
     * @param username screen name of the user
     * @return User Properties
     * @throws EngineException if Connection is unavailable
     */
    public UserRelation getConnection(String username) throws EngineException {
        try {
            return new UserRelation(twitter.showFriendship(twitter.getScreenName(), username));
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
    public TwitterUser followUser(long userID) throws EngineException {
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
    public TwitterUser unfollowUser(long userID) throws EngineException {
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
    public TwitterUser blockUser(long UserID) throws EngineException {
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
    public TwitterUser unblockUser(long UserID) throws EngineException {
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
    public TwitterUser muteUser(long UserID) throws EngineException {
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
    public TwitterUser unmuteUser(long UserID) throws EngineException {
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
     * @return List of Following User with cursors
     * @throws EngineException if Access is unavailable
     */
    public TwitterUserList getFollowing(long userId, long cursor) throws EngineException {
        try {
            int load = settings.getListSize();
            IDs userIDs = twitter.getFriendsIDs(userId, cursor, load);
            long[] ids = userIDs.getIDs();
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = userIDs.getNextCursor();
            TwitterUserList result = new TwitterUserList(prevCursor, nextCursor);
            if (ids.length > 0) {
                result.addAll(convertUserList(twitter.lookupUsers(ids)));
            }
            return result;
        } catch (TwitterException err) {
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
    public TwitterUserList getFollower(long userId, long cursor) throws EngineException {
        try {
            int load = settings.getListSize();
            IDs userIDs = twitter.getFollowersIDs(userId, cursor, load);
            long[] ids = userIDs.getIDs();
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = userIDs.getNextCursor();
            TwitterUserList result = new TwitterUserList(prevCursor, nextCursor);
            if (ids.length > 0) {
                result.addAll(convertUserList(twitter.lookupUsers(ids)));
            }
            return result;
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
    public void uploadStatus(TweetHolder tweet) throws EngineException {
        try {
            StatusUpdate mStatus = new StatusUpdate(tweet.getText());
            if (tweet.isReply())
                mStatus.setInReplyToStatusId(tweet.getReplyId());
            if (tweet.hasLocation())
                mStatus.setLocation(new GeoLocation(tweet.getLatitude(), tweet.getLongitude()));
            if (tweet.getMediaType() == TweetHolder.MediaType.IMAGE) {
                long[] ids = uploadImages(tweet.getMediaPaths());
                mStatus.setMediaIds(ids);
            } else if (tweet.getMediaType() == TweetHolder.MediaType.VIDEO) {
                long id = uploadVideo(tweet.getMediaPath());
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
    public Tweet getStatus(long tweetId) throws EngineException {
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
     * @param sinceId ID of the last tweet reply
     * @param maxId   ID of the earliest tweet reply
     * @return List of Answers
     * @throws EngineException if Access is unavailable
     */
    public List<Tweet> getAnswers(String name, long tweetId, long sinceId, long maxId) throws EngineException {
        try {
            int load = settings.getListSize();
            List<Status> answers = new LinkedList<>();
            Query query = new Query("to:" + name + " +exclude:retweets");
            query.setCount(load);
            if (sinceId > 0)
                query.setSinceId(sinceId);
            else
                query.setSinceId(tweetId);
            if (maxId > 1)
                query.setMaxId(maxId - 1);
            query.setResultType(Query.RECENT);
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
    public Tweet retweet(long tweetId) throws EngineException {
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
    public Tweet favorite(long tweetId) throws EngineException {
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
    public Tweet deleteTweet(long tweetId) throws EngineException {
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
    public TwitterUserList getRetweeter(long tweetID, long cursor) throws EngineException {
        try {
            int load = settings.getListSize();
            IDs userIDs = twitter.getRetweeterIds(tweetID, load, cursor);
            long[] ids = userIDs.getIDs();
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = userIDs.getNextCursor(); // fixme next cursor always zero
            TwitterUserList result = new TwitterUserList(prevCursor, nextCursor);
            if (ids.length > 0) {
                result.addAll(convertUserList(twitter.lookupUsers(ids)));
            }
            return result;
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
    public List<Message> getMessages() throws EngineException {
        try {
            int load = settings.getListSize();
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
    public void sendMessage(MessageHolder messageHolder) throws EngineException {
        try {
            long id = twitter.showUser(messageHolder.getUsername()).getId();
            if (messageHolder.hasMedia()) {
                long[] mediaId = uploadImages(messageHolder.getMediaPath());
                twitter.sendDirectMessage(id, messageHolder.getMessage(), mediaId[0]);
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
    public void deleteMessage(long id) throws EngineException {
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
    public TwitterUser updateProfile(UserHolder userHolder) throws EngineException {
        try {
            if (userHolder.hasProfileImage()) {
                File profileImage = new File(userHolder.getProfileImage());
                twitter.updateProfileImage(profileImage);
            }
            if (userHolder.hasProfileBanner()) {
                File profileBanner = new File(userHolder.getProfileBanner());
                twitter.updateProfileBanner(profileBanner);
            }
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
     * get user list
     *
     * @param userId id of the list owner
     * @param cursor list cursor to set the start point
     * @return list information
     * @throws EngineException if access is unavailable
     */
    public UserListList getUserList(long userId, long cursor) throws EngineException {
        try {
            List<UserList> lists = twitter.getUserLists(userId);
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = 0;
            UserListList result = new UserListList(0, 0); // todo add paging system
            for (UserList list : lists)
                result.add(new TwitterList(list, twitterID));
            return result;
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }


    /**
     * get user list
     *
     * @param username id of the list owner
     * @param cursor   list cursor to set the start point
     * @return list information
     * @throws EngineException if access is unavailable
     */
    public UserListList getUserList(String username, long cursor) throws EngineException {
        try {
            List<UserList> lists = twitter.getUserLists(username);
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = 0;
            UserListList result = new UserListList(prevCursor, nextCursor); // todo add paging system
            for (UserList list : lists)
                result.add(new TwitterList(list, twitterID));
            return result;
        } catch (TwitterException err) {
            throw new EngineException(err);
        }
    }

    /**
     * get the lists the user has been added to
     *
     * @param userId ID of the user
     * @param cursor list cursor
     * @return a list of user lists
     * @throws EngineException if access is unavailable
     */
    public UserListList getUserListMemberships(long userId, long cursor) throws EngineException {
        try {
            int count = settings.getListSize();
            PagableResponseList<UserList> lists = twitter.getUserListMemberships(userId, count, cursor);
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = lists.getNextCursor();
            UserListList result = new UserListList(prevCursor, nextCursor);
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
    public TwitterList followUserList(long listId) throws EngineException {
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
    public TwitterList deleteUserList(long listId) throws EngineException {
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
    public TwitterUserList getListFollower(long listId, long cursor) throws EngineException {
        try {
            PagableResponseList<User> followerList = twitter.getUserListSubscribers(listId, cursor);
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = followerList.getNextCursor();
            TwitterUserList result = new TwitterUserList(prevCursor, nextCursor);
            result.addAll(convertUserList(followerList));
            return result;
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
    public TwitterUserList getListMember(long listId, long cursor) throws EngineException {
        try {
            PagableResponseList<User> users = twitter.getUserListMembers(listId, cursor);
            long prevCursor = cursor > 0 ? cursor : 0;
            long nextCursor = users.getNextCursor();
            TwitterUserList result = new TwitterUserList(prevCursor, nextCursor);
            result.addAll(convertUserList(users));
            return result;
        } catch (TwitterException err) {
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
        } catch (TwitterException err) {
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
     * creates an user list
     *
     * @param list holder for list information
     * @throws EngineException if access is unavailable
     */
    public void updateUserList(ListHolder list) throws EngineException {
        try {
            if (list.exists()) {
                twitter.updateUserList(list.getId(), list.getTitle(), list.isPublic(), list.getDescription());
            } else {
                twitter.createUserList(list.getTitle(), list.isPublic(), list.getDescription());
            }
        } catch (TwitterException err) {
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
        } catch (TwitterException err) {
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
            throw new EngineException(EngineException.InternalErrorType.FILENOTFOUND);
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
            throw new EngineException(EngineException.InternalErrorType.FILENOTFOUND);
        }
    }
}