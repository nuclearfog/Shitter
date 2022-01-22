package org.nuclearfog.twidda.database;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static org.nuclearfog.twidda.database.DatabaseAdapter.FavoriteTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.MessageTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.TrendTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.TweetRegisterTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.TweetTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.UserRegisterTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.UserTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.lists.Directmessages;
import org.nuclearfog.twidda.model.DirectMessage;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.model.Tweet;
import org.nuclearfog.twidda.model.User;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * SQLite database class to store and load tweets, messages, trends and user information
 *
 * @author nuclearfog
 */
public class AppDatabase {

    // Tweet status bits
    static final int FAV_MASK = 1;          //  tweet is favored by user
    static final int RTW_MASK = 1 << 1;     //  tweet is retweeted by user
    static final int HOM_MASK = 1 << 2;     //  tweet is from home timeline
    static final int MEN_MASK = 1 << 3;     //  tweet is from mention timeline
    static final int UTW_MASK = 1 << 4;     //  tweet is from an users timeline
    static final int RPL_MASK = 1 << 5;     //  tweet is from a reply timeline
    static final int MEDIA_IMAGE_MASK = 1 << 6; // tweet contains images
    static final int MEDIA_VIDEO_MASK = 2 << 6; // tweet contains a video
    static final int MEDIA_ANGIF_MASK = 3 << 6; // tweet contains an animation
    static final int MEDIA_SENS_MASK = 1 << 8;  // tweet contains sensitive media

    // user status bits
    static final int VER_MASK = 1;          //  user is verified
    static final int LCK_MASK = 1 << 1;     //  user is private
    static final int FRQ_MASK = 1 << 2;     //  a follow request is pending
    static final int EXCL_USR = 1 << 3;     //  user excluded from mention timeline
    static final int DEF_IMG = 1 << 4;      //  user has a default profile image

    /**
     * query to create tweet table with user and register columns
     */
    private static final String TWEET_TABLE = TweetTable.NAME
            + " INNER JOIN " + UserTable.NAME
            + " ON " + TweetTable.NAME + "." + TweetTable.USER + "=" + UserTable.NAME + "." + UserTable.ID
            + " INNER JOIN " + UserRegisterTable.NAME
            + " ON " + TweetTable.NAME + "." + TweetTable.USER + "=" + UserRegisterTable.NAME + "." + UserRegisterTable.ID
            + " INNER JOIN " + TweetRegisterTable.NAME
            + " ON " + TweetTable.NAME + "." + TweetTable.ID + "=" + TweetRegisterTable.NAME + "." + TweetRegisterTable.ID;

    /**
     * query to get user information
     */
    private static final String USER_TABLE = "SELECT * FROM " + UserTable.NAME
            + " INNER JOIN " + UserRegisterTable.NAME
            + " ON " + UserTable.NAME + "." + UserTable.ID + "=" + UserRegisterTable.NAME + "." + UserRegisterTable.ID
            + " WHERE " + UserTable.NAME + "." + UserTable.ID + "=? LIMIT 1";

    /**
     * SQL query to get home timeline tweets
     */
    static final String HOME_QUERY = "SELECT * FROM " + TWEET_TABLE
            + " WHERE " + TweetRegisterTable.NAME + "." + TweetRegisterTable.REGISTER + "&" + HOM_MASK + " IS NOT 0"
            + " AND " + TweetRegisterTable.NAME + "." + TweetRegisterTable.OWNER + "=?"
            + " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
            + " ORDER BY " + TweetTable.ID
            + " DESC LIMIT ?";

    /**
     * SQL query to get mention timeline
     */
    static final String MENTION_QUERY = "SELECT * FROM " + TWEET_TABLE
            + " WHERE " + TweetRegisterTable.NAME + "." + TweetRegisterTable.REGISTER + "&" + MEN_MASK + " IS NOT 0"
            + " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.REGISTER + "&" + EXCL_USR + " IS 0"
            + " AND " + TweetRegisterTable.NAME + "." + TweetRegisterTable.OWNER + "=?"
            + " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
            + " ORDER BY " + TweetTable.ID
            + " DESC LIMIT ?";

    /**
     * SQL query to get tweets of an user
     */
    static final String USERTWEET_QUERY = "SELECT * FROM " + TWEET_TABLE
            + " WHERE " + TweetRegisterTable.NAME + "." + TweetRegisterTable.REGISTER + "&" + UTW_MASK + " IS NOT 0"
            + " AND " + TweetRegisterTable.NAME + "." + TweetRegisterTable.OWNER + "=?"
            + " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
            + " AND " + TweetTable.NAME + "." + TweetTable.USER + "=?"
            + " ORDER BY " + TweetTable.ID
            + " DESC LIMIT ?";

    /**
     * SQL query to get tweets favored by an user
     */
    static final String USERFAVORIT_QUERY = "SELECT * FROM " + TWEET_TABLE
            + " INNER JOIN " + FavoriteTable.NAME
            + " ON " + TweetTable.NAME + "." + TweetTable.ID + "=" + FavoriteTable.NAME + "." + FavoriteTable.TWEETID
            + " WHERE " + FavoriteTable.NAME + "." + FavoriteTable.FAVORITEDBY + "=?"
            + " AND " + TweetRegisterTable.NAME + "." + TweetRegisterTable.OWNER + "=?"
            + " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
            + " ORDER BY " + TweetTable.ID
            + " DESC LIMIT ?";

    /**
     * SQL query to get a single tweet specified by an ID
     */
    static final String SINGLE_TWEET_QUERY = "SELECT * FROM " + TWEET_TABLE
            + " WHERE " + TweetTable.NAME + "." + TweetTable.ID + "=?"
            + " AND " + TweetRegisterTable.NAME + "." + TweetRegisterTable.OWNER + "=?"
            + " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
            + " LIMIT 1";

    /**
     * SQL query to get replies of a tweet specified by a tweet ID
     */
    static final String ANSWER_QUERY = "SELECT * FROM " + TWEET_TABLE
            + " WHERE " + TweetTable.NAME + "." + TweetTable.REPLYTWEET + "=?"
            + " AND " + TweetRegisterTable.NAME + "." + TweetRegisterTable.OWNER + "=?"
            + " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
            + " AND " + TweetRegisterTable.NAME + "." + TweetRegisterTable.REGISTER + "&" + RPL_MASK + " IS NOT 0"
            + " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.REGISTER + "&" + EXCL_USR + " IS 0"
            + " ORDER BY " + TweetTable.ID + " DESC LIMIT ?";

    /**
     * SQL query to get current user's messages
     */
    static final String MESSAGE_QUERY = "SELECT * FROM " + MessageTable.NAME
            + " WHERE " + MessageTable.FROM + "=? OR " + MessageTable.TO + "=?"
            + " ORDER BY " + MessageTable.SINCE + " DESC LIMIT ?";

    /**
     * select tweet entries from favorite table matching tweet ID
     * this tweet can be favored by multiple users
     */
    private static final String FAVORITE_SELECT_TWEET = FavoriteTable.TWEETID + "=?";

    /**
     * select all tweets from favorite table favored by given user
     */
    private static final String FAVORITE_SELECT_OWNER = FavoriteTable.FAVORITEDBY + "=?";

    /**
     * select specific tweet from favorite table
     */
    private static final String FAVORITE_SELECT = FAVORITE_SELECT_TWEET + " AND " + FAVORITE_SELECT_OWNER;

    /**
     * select message from message table with ID
     */
    private static final String MESSAGE_SELECT = MessageTable.ID + "=?";

    /**
     * select trends from trend table with given world ID
     */
    private static final String TREND_SELECT = TrendTable.ID + "=?";

    /**
     * select tweet from tweet table matching tweet ID
     */
    private static final String TWEET_SELECT = TweetTable.NAME + "." + TweetTable.ID + "=?";

    /**
     * select user from user table matching user ID
     */
    private static final String USER_SELECT = UserTable.NAME + "." + UserTable.ID + "=?";

    /**
     * selection to get tweet register
     */
    private static final String TWEET_REG_SELECT = TweetRegisterTable.ID + "=? AND " + TweetRegisterTable.OWNER + "=?";

    /**
     * selection to get user register
     */
    private static final String USER_REG_SELECT = UserRegisterTable.ID + "=? AND " + UserRegisterTable.OWNER + "=?";

    /**
     * column projection for user register
     */
    private static final String[] USER_REG_COLUMN = {UserRegisterTable.REGISTER};

    /**
     * column projection for tweet register
     */
    private static final String[] TWEET_REG_COLUMN = {TweetRegisterTable.REGISTER};

    /**
     * default order for trend rows
     */
    private static final String TREND_ORDER = TrendTable.INDEX + " ASC";

    /**
     * limit for accessing a single row
     */
    private static final String SINGLE_ITEM = "1";

    /**
     * limit of database entries
     */
    private final int limit;

    /**
     * ID of the current user
     */
    private final long homeId;

    /**
     * adapter for the database backend
     */
    private DatabaseAdapter adapter;

    /**
     * @param context activity context
     */
    public AppDatabase(Context context) {
        adapter = DatabaseAdapter.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        homeId = settings.getCurrentUserId();
        limit = settings.getListSize();
    }

    /**
     * Store user information
     *
     * @param user Twitter user
     */
    public void storeUser(User user) {
        SQLiteDatabase db = getDbWrite();
        storeUser(user, db, CONFLICT_REPLACE);
        commit(db);
    }

    /**
     * store home timeline
     *
     * @param home tweet from home timeline
     */
    public void storeHomeTimeline(List<Tweet> home) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : home)
            storeStatus(tweet, HOM_MASK, db);
        commit(db);
    }

    /**
     * store mentions
     *
     * @param mentions tweets
     */
    public void storeMentions(List<Tweet> mentions) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : mentions)
            storeStatus(tweet, MEN_MASK, db);
        commit(db);
    }

    /**
     * store user timeline
     *
     * @param stats user timeline
     */
    public void storeUserTweets(List<Tweet> stats) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : stats)
            storeStatus(tweet, UTW_MASK, db);
        commit(db);
    }

    /**
     * store user favors
     *
     * @param fav     tweet favored by user
     * @param ownerId user ID
     */
    public void storeUserFavs(List<Tweet> fav, long ownerId) {
        SQLiteDatabase db = getDbWrite();
        removeOldFavorites(db, ownerId);
        for (Tweet tweet : fav) {
            storeStatus(tweet, 0, db);
            storeFavorite(tweet.getId(), ownerId, db);
        }
        commit(db);
    }

    /**
     * store replies of a tweet
     *
     * @param replies tweet replies
     */
    public void storeReplies(List<Tweet> replies) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : replies)
            storeStatus(tweet, RPL_MASK, db);
        commit(db);
    }

    /**
     * store location specific trends
     *
     * @param trends List of Trends
     * @param woeId  Yahoo World ID
     */
    public void storeTrends(List<Trend> trends, int woeId) {
        String[] args = {Integer.toString(woeId)};
        SQLiteDatabase db = getDbWrite();
        db.delete(TrendTable.NAME, TREND_SELECT, args);
        for (Trend trend : trends) {
            ContentValues trendColumn = new ContentValues(4);
            trendColumn.put(TrendTable.ID, woeId);
            trendColumn.put(TrendTable.VOL, trend.getPopularity());
            trendColumn.put(TrendTable.TREND, trend.getName());
            trendColumn.put(TrendTable.INDEX, trend.getRank());
            db.insert(TrendTable.NAME, null, trendColumn);
        }
        commit(db);
    }

    /**
     * store tweet ID of a favored tweet by the current user
     *
     * @param tweet favored tweet
     */
    public void storeFavorite(Tweet tweet) {
        if (tweet.getEmbeddedTweet() != null)
            tweet = tweet.getEmbeddedTweet();
        SQLiteDatabase db = getDbWrite();
        storeStatus(tweet, 0, db);
        storeFavorite(tweet.getId(), homeId, db);
        commit(db);
    }

    /**
     * store direct messages
     *
     * @param messages list of direct messages
     */
    public void storeMessage(List<DirectMessage> messages) {
        SQLiteDatabase db = getDbWrite();
        for (DirectMessage message : messages)
            storeMessage(message, db);
        commit(db);
    }

    /**
     * load home timeline
     *
     * @return tweet list
     */
    public List<Tweet> getHomeTimeline() {
        String homeStr = Long.toString(homeId);
        String[] args = {homeStr, homeStr, Integer.toString(limit)};

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();
        Cursor cursor = db.rawQuery(HOME_QUERY, args);
        if (cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tweetList;
    }

    /**
     * load mentions
     *
     * @return tweet list
     */
    public List<Tweet> getMentions() {
        String homeStr = Long.toString(homeId);
        String[] args = {homeStr, homeStr, Integer.toString(limit)};

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();
        Cursor cursor = db.rawQuery(MENTION_QUERY, args);
        if (cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tweetList;
    }

    /**
     * load user timeline
     *
     * @param userID user ID
     * @return Tweet list of user tweets
     */
    public List<Tweet> getUserTweets(long userID) {
        String homeStr = Long.toString(homeId);
        String[] args = {homeStr, homeStr, Long.toString(userID), Integer.toString(limit)};

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();
        Cursor cursor = db.rawQuery(USERTWEET_QUERY, args);
        if (cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tweetList;
    }

    /**
     * load user favored tweets
     *
     * @param ownerID user ID
     * @return favored tweets by user
     */
    public List<Tweet> getUserFavorites(long ownerID) {
        String homeStr = Long.toString(homeId);
        String[] args = {Long.toString(ownerID), homeStr, homeStr, Integer.toString(limit)};

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();
        Cursor cursor = db.rawQuery(USERFAVORIT_QUERY, args);
        if (cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tweetList;
    }

    /**
     * get user information
     *
     * @param userId ID of user
     * @return user information or null if not found
     */
    @Nullable
    public User getUser(long userId) {
        SQLiteDatabase db = getDbRead();
        return getUser(userId, db);
    }

    /**
     * load status
     *
     * @param tweetId tweet ID
     * @return tweet or null if not found
     */
    @Nullable
    public Tweet getStatus(long tweetId) {
        String homeStr = Long.toString(homeId);
        String[] args = {Long.toString(tweetId), homeStr, homeStr};

        SQLiteDatabase db = getDbRead();
        Tweet result = null;
        Cursor cursor = db.rawQuery(SINGLE_TWEET_QUERY, args);
        if (cursor.moveToFirst())
            result = getStatus(cursor);
        cursor.close();
        return result;
    }

    /**
     * get tweet answers
     *
     * @param tweetId Tweet ID
     * @return list of tweet answers
     */
    public List<Tweet> getAnswers(long tweetId) {
        String homeStr = Long.toString(homeId);
        String[] args = {Long.toString(tweetId), homeStr, homeStr, Integer.toString(limit)};

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();
        Cursor cursor = db.rawQuery(ANSWER_QUERY, args);
        if (cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tweetList;
    }

    /**
     * update status and author information
     *
     * @param tweet Tweet
     */
    public void updateStatus(Tweet tweet) {
        SQLiteDatabase db = getDbWrite();
        updateStatus(tweet, db);
        if (tweet.getEmbeddedTweet() != null)
            updateStatus(tweet.getEmbeddedTweet(), db);
        commit(db);
    }

    /**
     * remove status
     *
     * @param tweetId Tweet ID
     */
    public void removeStatus(long tweetId) {
        String[] args = {Long.toString(tweetId)};

        SQLiteDatabase db = getDbWrite();
        db.delete(TweetTable.NAME, TWEET_SELECT, args);
        db.delete(FavoriteTable.NAME, FAVORITE_SELECT_TWEET, args);
        commit(db);
    }

    /**
     * remove status from favorites
     *
     * @param tweet Tweet to remove from the favorites
     */
    public void removeFavorite(Tweet tweet) {
        String[] delArgs = {Long.toString(tweet.getId()), Long.toString(homeId)};

        if (tweet.getEmbeddedTweet() != null) {
            tweet = tweet.getEmbeddedTweet();
        }
        SQLiteDatabase db = getDbWrite();
        // get tweet register
        int register = getTweetRegister(db, tweet.getId());
        register &= ~FAV_MASK; // unset favorite flag
        // update database
        setTweetRegister(db, tweet, register);
        db.delete(FavoriteTable.NAME, FAVORITE_SELECT, delArgs);
        commit(db);
    }

    /**
     * Delete Direct Message
     *
     * @param id Direct Message ID
     */
    public void deleteMessage(long id) {
        String[] messageId = {Long.toString(id)};

        SQLiteDatabase db = getDbWrite();
        db.delete(MessageTable.NAME, MESSAGE_SELECT, messageId);
        commit(db);
    }

    /**
     * Load trend List
     *
     * @param woeId Yahoo World ID
     * @return list of trends
     */
    public List<Trend> getTrends(int woeId) {
        String[] args = {Integer.toString(woeId)};
        SQLiteDatabase db = getDbRead();
        Cursor cursor = db.query(TrendTable.NAME, null, TREND_SELECT, args, null, null, TREND_ORDER);
        List<Trend> trends = new LinkedList<>();
        if (cursor.moveToFirst()) {
            do {
                trends.add(new TrendImpl(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return trends;
    }

    /**
     * load direct messages
     *
     * @return list of direct messages
     */
    public Directmessages getMessages() {
        String homeIdStr = Long.toString(homeId);
        String[] args = {homeIdStr, homeIdStr, Integer.toString(limit)};
        Directmessages result = new Directmessages(null, null);
        SQLiteDatabase db = getDbRead();
        Map<Long, User> userCache = new TreeMap<>();
        Cursor cursor = db.rawQuery(MESSAGE_QUERY, args);
        if (cursor.moveToFirst()) {
            do {
                User sender, receiver;
                DirectMessageImpl message = new DirectMessageImpl(cursor);
                if (userCache.containsKey(message.getSenderId())) {
                    sender = userCache.get(message.getSenderId());
                } else {
                    sender = getUser(message.getSenderId());
                    userCache.put(message.getSenderId(), sender);
                }
                if (userCache.containsKey(message.getReceiverId())) {
                    receiver = userCache.get(message.getReceiverId());
                } else {
                    receiver = getUser(message.getReceiverId());
                    userCache.put(message.getReceiverId(), receiver);
                }
                if (sender != null && receiver != null) {
                    message.setSender(sender);
                    message.setReceiver(receiver);
                    result.add(message);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    /**
     * check if tweet exists in database
     *
     * @param id Tweet ID
     * @return true if found
     */
    public boolean containStatus(long id) {
        SQLiteDatabase db = getDbRead();
        return containStatus(id, db);
    }

    /**
     * remove user from mention results
     *
     * @param id   user ID
     * @param mute true remove user tweets from mention results
     */
    public void muteUser(long id, boolean mute) {
        SQLiteDatabase db = getDbWrite();
        int register = getUserRegister(db, id);
        if (mute) {
            register |= EXCL_USR;
        } else {
            register &= ~EXCL_USR;
        }
        setUserRegister(db, id, register);
        commit(db);
    }

    /**
     * get tweet information from database
     *
     * @param cursor cursor containing tweet informations
     * @return tweet instance
     */
    private Tweet getStatus(Cursor cursor) {
        TweetImpl result = new TweetImpl(cursor, homeId);
        // check if there is an embedded tweet
        if (result.getEmbeddedTweetId() > 1)
            result.addEmbeddedTweet(getStatus(result.getEmbeddedTweetId()));
        return result;
    }

    /**
     * get user information from table
     *
     * @param userId Id of the user
     * @param db     SQLITE DB
     * @return user instance
     */
    @Nullable
    private User getUser(long userId, SQLiteDatabase db) {
        String[] args = {Long.toString(userId)};
        Cursor cursor = db.rawQuery(USER_TABLE, args);

        User user = null;
        if (cursor.moveToFirst())
            user = new UserImpl(cursor, homeId);
        cursor.close();
        return user;
    }

    /**
     * store user information into database
     *
     * @param user user information
     * @param db   SQLITE DB
     * @param mode SQLITE mode {@link SQLiteDatabase#CONFLICT_IGNORE} or {@link SQLiteDatabase#CONFLICT_REPLACE}
     */
    private void storeUser(User user, SQLiteDatabase db, int mode) {
        int register = getUserRegister(db, user.getId());
        if (user.isVerified())
            register |= VER_MASK;
        else
            register &= ~VER_MASK;
        if (user.isProtected())
            register |= LCK_MASK;
        else
            register &= ~LCK_MASK;
        if (user.followRequested())
            register |= FRQ_MASK;
        else
            register &= ~FRQ_MASK;
        if (user.hasDefaultProfileImage())
            register |= DEF_IMG;
        else
            register &= ~DEF_IMG;

        ContentValues userColumn = new ContentValues(13);
        userColumn.put(UserTable.ID, user.getId());
        userColumn.put(UserTable.USERNAME, user.getUsername());
        userColumn.put(UserTable.SCREENNAME, user.getScreenname());
        userColumn.put(UserTable.IMAGE, user.getImageUrl());
        userColumn.put(UserTable.DESCRIPTION, user.getDescription());
        userColumn.put(UserTable.LINK, user.getProfileUrl());
        userColumn.put(UserTable.LOCATION, user.getLocation());
        userColumn.put(UserTable.BANNER, user.getBannerUrl());
        userColumn.put(UserTable.SINCE, user.getCreatedAt());
        userColumn.put(UserTable.FRIENDS, user.getFollowing());
        userColumn.put(UserTable.FOLLOWER, user.getFollower());
        userColumn.put(UserTable.TWEETS, user.getTweetCount());
        userColumn.put(UserTable.FAVORS, user.getFavoriteCount());

        db.insertWithOnConflict(UserTable.NAME, null, userColumn, mode);
        setUserRegister(db, user.getId(), register);
    }


    /**
     * save tweet into database
     *
     * @param tweet          Tweet information
     * @param statusRegister predefined status register or zero if there isn't one
     * @param db             SQLite database
     */
    private void storeStatus(Tweet tweet, int statusRegister, SQLiteDatabase db) {
        User user = tweet.getAuthor();
        Tweet rtStat = tweet.getEmbeddedTweet();
        long rtId = -1L;
        if (rtStat != null) {
            storeStatus(rtStat, 0, db);
            rtId = rtStat.getId();
        }
        statusRegister |= getTweetRegister(db, tweet.getId());
        if (tweet.isFavorited()) {
            statusRegister |= FAV_MASK;
        } else {
            statusRegister &= ~FAV_MASK;
        }
        if (tweet.isRetweeted()) {
            statusRegister |= RTW_MASK;
        } else {
            statusRegister &= ~RTW_MASK;
        }
        if (tweet.isSensitive()) {
            statusRegister |= MEDIA_SENS_MASK;
        } else {
            statusRegister &= ~MEDIA_SENS_MASK;
        }
        if (Tweet.MEDIA_PHOTO.equals(tweet.getMediaType())) {
            statusRegister |= MEDIA_IMAGE_MASK;
        } else if (Tweet.MEDIA_VIDEO.equals(tweet.getMediaType())) {
            statusRegister |= MEDIA_VIDEO_MASK;
        } else if (Tweet.MEDIA_GIF.equals(tweet.getMediaType())) {
            statusRegister |= MEDIA_ANGIF_MASK;
        }
        ContentValues status = new ContentValues(16);
        status.put(TweetTable.MEDIA, getMediaLinks(tweet));
        status.put(TweetTable.ID, tweet.getId());
        status.put(TweetTable.USER, user.getId());
        status.put(TweetTable.SINCE, tweet.getTimestamp());
        status.put(TweetTable.TWEET, tweet.getText());
        status.put(TweetTable.EMBEDDED, rtId);
        status.put(TweetTable.SOURCE, tweet.getSource());
        status.put(TweetTable.REPLYTWEET, tweet.getReplyId());
        status.put(TweetTable.RETWEET, tweet.getRetweetCount());
        status.put(TweetTable.FAVORITE, tweet.getFavoriteCount());
        status.put(TweetTable.REPLYUSER, tweet.getReplyUserId());
        status.put(TweetTable.PLACE, tweet.getLocationName());
        status.put(TweetTable.COORDINATE, tweet.getLocationCoordinates());
        status.put(TweetTable.REPLYUSER, tweet.getReplyUserId());
        status.put(TweetTable.REPLYNAME, tweet.getReplyName());

        db.insertWithOnConflict(TweetTable.NAME, null, status, CONFLICT_REPLACE);

        storeUser(user, db, CONFLICT_IGNORE);
        setTweetRegister(db, tweet, statusRegister);
    }

    /**
     * updates existing tweet
     *
     * @param tweet update of the tweet
     * @param db    database instance
     */
    private void updateStatus(Tweet tweet, SQLiteDatabase db) {
        String[] tweetIdArg = {Long.toString(tweet.getId())};
        String[] userIdArg = {Long.toString(tweet.getAuthor().getId())};

        User user = tweet.getAuthor();
        int register = getTweetRegister(db, tweet.getId());
        if (tweet.isRetweeted())
            register |= RTW_MASK;
        else
            register &= ~RTW_MASK;
        if (tweet.isFavorited())
            register |= FAV_MASK;
        else
            register &= ~FAV_MASK;

        ContentValues tweetColumn = new ContentValues(6);
        tweetColumn.put(TweetTable.TWEET, tweet.getText());
        tweetColumn.put(TweetTable.RETWEET, tweet.getRetweetCount());
        tweetColumn.put(TweetTable.FAVORITE, tweet.getFavoriteCount());
        tweetColumn.put(TweetTable.REPLYNAME, tweet.getReplyName());
        tweetColumn.put(TweetTable.MEDIA, getMediaLinks(tweet));

        ContentValues userColumn = new ContentValues(9);
        userColumn.put(UserTable.USERNAME, user.getUsername());
        userColumn.put(UserTable.SCREENNAME, user.getScreenname());
        userColumn.put(UserTable.IMAGE, user.getImageUrl());
        userColumn.put(UserTable.DESCRIPTION, user.getDescription());
        userColumn.put(UserTable.LINK, user.getProfileUrl());
        userColumn.put(UserTable.LOCATION, user.getLocation());
        userColumn.put(UserTable.BANNER, user.getBannerUrl());
        userColumn.put(UserTable.FRIENDS, user.getFollowing());
        userColumn.put(UserTable.FOLLOWER, user.getFollower());

        db.update(TweetTable.NAME, tweetColumn, TWEET_SELECT, tweetIdArg);
        db.update(UserTable.NAME, userColumn, USER_SELECT, userIdArg);
        setTweetRegister(db, tweet, register);
    }

    /**
     * Store Tweet into favorite table of a user
     *
     * @param tweetId ID of the favored tweet
     * @param ownerId ID of the favorite list owner
     * @param db      database instance
     */
    private void storeFavorite(long tweetId, long ownerId, SQLiteDatabase db) {
        ContentValues favTable = new ContentValues(2);
        favTable.put(FavoriteTable.TWEETID, tweetId);
        favTable.put(FavoriteTable.FAVORITEDBY, ownerId);
        db.insertWithOnConflict(FavoriteTable.NAME, null, favTable, CONFLICT_REPLACE);
    }

    /**
     * clear old favorites from table
     *
     * @param db     database instance
     * @param userId ID of the favorite list owner
     */
    private void removeOldFavorites(SQLiteDatabase db, long userId) {
        String[] delArgs = {Long.toString(userId)};
        db.delete(FavoriteTable.NAME, FAVORITE_SELECT_OWNER, delArgs);
    }

    /**
     * store direct message
     *
     * @param message direct message information
     * @param db      database instance
     */
    private void storeMessage(DirectMessage message, SQLiteDatabase db) {
        // store message information
        ContentValues messageColumn = new ContentValues(6);
        messageColumn.put(MessageTable.ID, message.getId());
        messageColumn.put(MessageTable.SINCE, message.getTimestamp());
        messageColumn.put(MessageTable.FROM, message.getSender().getId());
        messageColumn.put(MessageTable.TO, message.getReceiver().getId());
        messageColumn.put(MessageTable.MESSAGE, message.getText());
        if (message.getMedia() != null)
            messageColumn.put(MessageTable.MEDIA, message.getMedia().toString());
        db.insertWithOnConflict(MessageTable.NAME, null, messageColumn, CONFLICT_IGNORE);
        // store user information
        storeUser(message.getSender(), db, CONFLICT_IGNORE);
        storeUser(message.getReceiver(), db, CONFLICT_IGNORE);
    }

    /**
     * get status register of a tweet or zero if tweet was not found
     *
     * @param db      database instance
     * @param tweetID ID of the tweet
     * @return tweet register
     */
    private int getTweetRegister(SQLiteDatabase db, long tweetID) {
        String[] args = {Long.toString(tweetID), Long.toString(homeId)};

        Cursor c = db.query(TweetRegisterTable.NAME, TWEET_REG_COLUMN, TWEET_REG_SELECT, args, null, null, null, SINGLE_ITEM);
        int result = 0;
        if (c.moveToFirst())
            result = c.getInt(0);
        c.close();
        return result;
    }

    /**
     * set status register of a tweet. if an entry exists, update it
     *
     * @param db       database instance
     * @param tweet    Tweet
     * @param register tweet register
     */
    public void setTweetRegister(SQLiteDatabase db, Tweet tweet, int register) {
        String[] args = {Long.toString(tweet.getId()), Long.toString(homeId)};

        ContentValues values = new ContentValues(3);
        values.put(TweetRegisterTable.ID, tweet.getId());
        values.put(TweetRegisterTable.OWNER, homeId);
        values.put(TweetRegisterTable.REGISTER, register);
        values.put(TweetRegisterTable.RETWEETUSER, tweet.getMyRetweetId());

        int cnt = db.update(TweetRegisterTable.NAME, values, TWEET_REG_SELECT, args);
        if (cnt == 0) {
            // create new entry if there isn't one
            db.insert(TweetRegisterTable.NAME, null, values);
        }
    }

    /**
     * get user register or zero if not found
     *
     * @param db     database instance
     * @param userID ID of the user
     * @return user flags
     */
    private int getUserRegister(SQLiteDatabase db, long userID) {
        String[] args = {Long.toString(userID), Long.toString(homeId)};

        Cursor c = db.query(UserRegisterTable.NAME, USER_REG_COLUMN, USER_REG_SELECT, args, null, null, null, SINGLE_ITEM);
        int result = 0;
        if (c.moveToFirst())
            result = c.getInt(0);
        c.close();
        return result;
    }

    /**
     * set user register. If entry exists, update it.
     *
     * @param db       database instance
     * @param id       User ID
     * @param register tweet register
     */
    public void setUserRegister(SQLiteDatabase db, long id, int register) {
        String[] args = {Long.toString(id), Long.toString(homeId)};

        ContentValues values = new ContentValues(3);
        values.put(UserRegisterTable.ID, id);
        values.put(UserRegisterTable.OWNER, homeId);
        values.put(UserRegisterTable.REGISTER, register);

        int cnt = db.update(UserRegisterTable.NAME, values, USER_REG_SELECT, args);
        if (cnt == 0) {
            // create new entry if there isn't one
            db.insert(UserRegisterTable.NAME, null, values);
        }
    }

    /**
     * check if tweet exists in database
     *
     * @param id Tweet ID
     * @param db database instance
     * @return true if found
     */
    private boolean containStatus(long id, SQLiteDatabase db) {
        String[] args = {Long.toString(id)};

        Cursor c = db.query(TweetTable.NAME, null, TWEET_SELECT, args, null, null, SINGLE_ITEM);
        boolean result = c.moveToFirst();
        c.close();
        return result;
    }

    /**
     * Get SQLite instance for reading database
     *
     * @return SQLite instance
     */
    private synchronized SQLiteDatabase getDbRead() {
        return adapter.getDatabase();
    }

    /**
     * GET SQLite instance for writing database
     *
     * @return SQLite instance
     */
    private synchronized SQLiteDatabase getDbWrite() {
        SQLiteDatabase db = adapter.getDatabase();
        db.beginTransaction();
        return db;
    }

    /**
     * Commit changes and close Database
     *
     * @param db database instance
     */
    private synchronized void commit(SQLiteDatabase db) {
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * create string where media links are separated by ' ; '
     *
     * @param tweet tweet information
     * @return String of media links
     */
    private String getMediaLinks(Tweet tweet) {
        StringBuilder media = new StringBuilder();
        for (Uri link : tweet.getMediaLinks())
            media.append(link.toString()).append(";");
        return media.toString();
    }
}