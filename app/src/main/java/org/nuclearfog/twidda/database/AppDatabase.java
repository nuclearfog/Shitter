package org.nuclearfog.twidda.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.lists.MessageList;
import org.nuclearfog.twidda.backend.model.Message;
import org.nuclearfog.twidda.backend.model.Trend;
import org.nuclearfog.twidda.backend.model.Tweet;
import org.nuclearfog.twidda.backend.model.User;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static org.nuclearfog.twidda.database.DatabaseAdapter.ANSWER_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.FavoriteTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.HOME_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.MENTION_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.MESSAGE_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.MessageTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.SINGLE_TWEET_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.STATUS_EXIST_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.TREND_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.TWEETFLAG_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.TrendTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.TweetTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.USERFAVORIT_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.USERFLAG_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.USERTWEET_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.USER_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.UserTable;

/**
 * Connection Class to SQLite Database of the app
 * All tweet, user and message information are stored here
 *
 * @author nuclearfog
 */
public class AppDatabase {

    // Tweet flags
    private static final int FAV_MASK = 1;          //  tweet is favored by user
    private static final int RTW_MASK = 1 << 1;     //  tweet is retweeted by user
    private static final int HOM_MASK = 1 << 2;     //  tweet is from home timeline
    private static final int MEN_MASK = 1 << 3;     //  tweet is from mention timeline
    private static final int UTW_MASK = 1 << 4;     //  tweet is from an users timeline
    private static final int RPL_MASK = 1 << 5;     //  tweet is from a reply timeline

    // Media content flags
    private static final int MEDIA_IMAGE_MASK = 1 << 6; // tweet contains images
    private static final int MEDIA_VIDEO_MASK = 2 << 6; // tweet contains a video
    private static final int MEDIA_ANGIF_MASK = 3 << 6; // tweet contains an animation
    private static final int MEDIA_SENS_MASK = 1 << 8;  // tweet contains sensitive media

    // user flags
    private static final int VER_MASK = 1;          //  user is verified
    private static final int LCK_MASK = 1 << 1;     //  user is private
    private static final int FRQ_MASK = 1 << 2;     //  a follow request is pending
    private static final int EXCL_USR = 1 << 3;     //  user excluded from mention timeline
    private static final int DEF_IMG = 1 << 4;      //  user has a default profile image

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
    private static final String TWEET_SELECT = TweetTable.TABLE + "." + TweetTable.ID + "=?";

    /**
     * select user from user table matching user ID
     */
    private static final String USER_SELECT = UserTable.TABLE + "." + UserTable.ID + "=?";

    /**
     * limit of database entries
     */
    private final int limit;

    /**
     * ID of the current user
     */
    private final long homeId;

    private DatabaseAdapter dataHelper;

    /**
     * initialize database
     */
    public AppDatabase(Context context) {
        dataHelper = DatabaseAdapter.getInstance(context);
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
        db.delete(TrendTable.TABLE, TREND_SELECT, args);
        for (Trend trend : trends) {
            ContentValues trendColumn = new ContentValues(4);
            trendColumn.put(TrendTable.ID, woeId);
            trendColumn.put(TrendTable.VOL, trend.getRange());
            trendColumn.put(TrendTable.TREND, trend.getName());
            trendColumn.put(TrendTable.INDEX, trend.getRank());
            db.insert(TrendTable.TABLE, null, trendColumn);
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
    public void storeMessage(List<Message> messages) {
        SQLiteDatabase db = getDbWrite();
        for (Message message : messages)
            storeMessage(message, db);
        commit(db);
    }

    /**
     * load home timeline
     *
     * @return tweet list
     */
    public List<Tweet> getHomeTimeline() {
        String[] args = {Integer.toString(HOM_MASK), Integer.toString(limit)};

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
        String[] args = {Integer.toString(MEN_MASK), Integer.toString(EXCL_USR), Integer.toString(limit)};

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
        String[] args = {Integer.toString(UTW_MASK), Long.toString(userID), Integer.toString(limit)};

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
        String[] args = {Long.toString(ownerID), Integer.toString(limit)};

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
        String[] args = {Long.toString(tweetId)};

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
        String[] args = {Long.toString(tweetId), Integer.toString(RPL_MASK),
                Integer.toString(EXCL_USR), Integer.toString(limit)};

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
        db.delete(TweetTable.TABLE, TWEET_SELECT, args);
        db.delete(FavoriteTable.TABLE, FAVORITE_SELECT_TWEET, args);
        commit(db);
    }

    /**
     * remove status from favorites
     *
     * @param tweet Tweet to remove from the favorites
     */
    public void removeFavorite(Tweet tweet) {
        long tweetId = tweet.getId();
        if (tweet.getEmbeddedTweet() != null)
            tweetId = tweet.getEmbeddedTweet().getId();
        String[] delArgs = {Long.toString(tweetId), Long.toString(homeId)};
        String[] updateArgs = {Long.toString(tweetId)};

        SQLiteDatabase db = getDbWrite();
        int flags = getTweetFlags(db, tweetId);
        flags &= ~FAV_MASK;
        ContentValues status = new ContentValues(1);
        status.put(TweetTable.REGISTER, flags);
        db.delete(FavoriteTable.TABLE, FAVORITE_SELECT, delArgs);
        db.update(TweetTable.TABLE, status, TWEET_SELECT, updateArgs);
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
        db.delete(MessageTable.TABLE, MESSAGE_SELECT, messageId);
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
        Cursor cursor = db.rawQuery(TREND_QUERY, args);

        List<Trend> trends = new LinkedList<>();
        if (cursor.moveToFirst()) {
            // get indexes first
            int idxName = cursor.getColumnIndexOrThrow(TrendTable.TREND);
            int idxVol = cursor.getColumnIndexOrThrow(TrendTable.VOL);
            int idxPos = cursor.getColumnIndexOrThrow(TrendTable.INDEX);
            do {
                // show trend
                String trendName = cursor.getString(idxName);
                int vol = cursor.getInt(idxVol);
                int pos = cursor.getInt(idxPos);
                trends.add(new Trend(trendName, vol, pos));
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
    public MessageList getMessages() {
        String[] args = {Integer.toString(limit)};
        // TODO get next cursor from database
        MessageList result = new MessageList(null, null);
        SQLiteDatabase db = getDbRead();
        Cursor cursor = db.rawQuery(MESSAGE_QUERY, args);
        if (cursor.moveToFirst()) {
            // get indexes
            int idxSender = cursor.getColumnIndexOrThrow(MessageTable.SENDER);
            int idxReceiver = cursor.getColumnIndexOrThrow(MessageTable.RECEIVER);
            int idxMessage = cursor.getColumnIndexOrThrow(MessageTable.MESSAGE);
            int idxTime = cursor.getColumnIndexOrThrow(MessageTable.SINCE);
            int idxId = cursor.getColumnIndexOrThrow(MessageTable.SINCE);
            do {
                // fetch message information
                long senderID = cursor.getLong(idxSender);
                long receiverID = cursor.getLong(idxReceiver);
                String message = cursor.getString(idxMessage);
                long time = cursor.getLong(idxTime);
                long messageId = cursor.getLong(idxId);
                // show message
                User sender = getUser(senderID, db);
                User receiver = getUser(receiverID, db);
                result.add(new Message(messageId, sender, receiver, time, message));
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
        String[] args = {Long.toString(id)};

        SQLiteDatabase db = getDbWrite();
        int flags = getUserFlags(db, id);
        if (mute)
            flags |= EXCL_USR;
        else
            flags &= ~EXCL_USR;

        ContentValues userColumn = new ContentValues(1);
        userColumn.put(UserTable.REGISTER, flags);
        db.update(UserTable.TABLE, userColumn, USER_SELECT, args);
        commit(db);
    }

    /**
     * get tweet information from database
     *
     * @param cursor cursor containing tweet informations
     * @return tweet instance
     */
    private Tweet getStatus(Cursor cursor) {
        long time = cursor.getLong(cursor.getColumnIndexOrThrow(TweetTable.SINCE));
        String tweettext = cursor.getString(cursor.getColumnIndexOrThrow(TweetTable.TWEET));
        int retweet = cursor.getInt(cursor.getColumnIndexOrThrow(TweetTable.RETWEET));
        int favorit = cursor.getInt(cursor.getColumnIndexOrThrow(TweetTable.FAVORITE));
        long tweetId = cursor.getLong(cursor.getColumnIndexOrThrow(TweetTable.ID));
        long retweetId = cursor.getLong(cursor.getColumnIndexOrThrow(TweetTable.RETWEETID));
        String replyname = cursor.getString(cursor.getColumnIndexOrThrow(TweetTable.REPLYNAME));
        long replyStatusId = cursor.getLong(cursor.getColumnIndexOrThrow(TweetTable.REPLYTWEET));
        long retweeterId = cursor.getLong(cursor.getColumnIndexOrThrow(TweetTable.RETWEETUSER));
        String source = cursor.getString(cursor.getColumnIndexOrThrow(TweetTable.SOURCE));
        String medialinks = cursor.getString(cursor.getColumnIndexOrThrow(TweetTable.MEDIA));
        String place = cursor.getString(cursor.getColumnIndexOrThrow(TweetTable.PLACE));
        String geo = cursor.getString(cursor.getColumnIndexOrThrow(TweetTable.COORDINATE));
        long replyUserId = cursor.getLong(cursor.getColumnIndexOrThrow(TweetTable.REPLYUSER));
        int statusregister = cursor.getInt(cursor.getColumnIndexOrThrow(TweetTable.REGISTER));
        boolean favorited = (statusregister & FAV_MASK) != 0;
        boolean retweeted = (statusregister & RTW_MASK) != 0;
        boolean sensitive = (statusregister & MEDIA_SENS_MASK) != 0;
        String[] medias = parseMedia(medialinks);
        // get media type
        Tweet.MediaType mediaType = Tweet.MediaType.NONE;
        if ((statusregister & MEDIA_ANGIF_MASK) == MEDIA_ANGIF_MASK)
            mediaType = Tweet.MediaType.GIF;
        else if ((statusregister & MEDIA_IMAGE_MASK) == MEDIA_IMAGE_MASK)
            mediaType = Tweet.MediaType.IMAGE;
        else if ((statusregister & MEDIA_VIDEO_MASK) == MEDIA_VIDEO_MASK)
            mediaType = Tweet.MediaType.VIDEO;
        User user = getUser(cursor);
        Tweet embeddedTweet = null;
        if (retweetId > 1)
            embeddedTweet = getStatus(retweetId);
        return new Tweet(tweetId, retweet, favorit, user, tweettext, time, replyname, replyUserId, medias,
                mediaType, source, replyStatusId, embeddedTweet, retweeterId, retweeted, favorited, sensitive, place, geo);
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
        Cursor cursor = db.rawQuery(USER_QUERY, args);

        User user = null;
        if (cursor.moveToFirst())
            user = getUser(cursor);
        cursor.close();
        return user;
    }

    /**
     * get user information from database
     *
     * @param cursor cursor containing user data
     * @return user instance
     */
    private User getUser(Cursor cursor) {
        long userId = cursor.getLong(cursor.getColumnIndexOrThrow(UserTable.ID));
        String username = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.USERNAME));
        String screenname = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.SCREENNAME));
        int userRegister = cursor.getInt(cursor.getColumnIndexOrThrow(UserTable.REGISTER));
        String profileImg = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.IMAGE));
        String bio = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.DESCRIPTION));
        String link = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.LINK));
        String location = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.LOCATION));
        String banner = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.BANNER));
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(UserTable.SINCE));
        int following = cursor.getInt(cursor.getColumnIndexOrThrow(UserTable.FRIENDS));
        int follower = cursor.getInt(cursor.getColumnIndexOrThrow(UserTable.FOLLOWER));
        int tCount = cursor.getInt(cursor.getColumnIndexOrThrow(UserTable.TWEETS));
        int fCount = cursor.getInt(cursor.getColumnIndexOrThrow(UserTable.FAVORS));
        boolean isCurrentUser = homeId == userId;
        boolean isVerified = (userRegister & VER_MASK) != 0;
        boolean isLocked = (userRegister & LCK_MASK) != 0;
        boolean isReq = (userRegister & FRQ_MASK) != 0;
        boolean defaultImg = (userRegister & DEF_IMG) != 0;
        return new User(userId, username, screenname, profileImg, bio, location, isCurrentUser, isVerified,
                isLocked, isReq, defaultImg, link, banner, createdAt, following, follower, tCount, fCount);
    }

    /**
     * store user information into database
     *
     * @param user user information
     * @param db   SQLITE DB
     * @param mode SQLITE mode {@link SQLiteDatabase#CONFLICT_IGNORE} or {@link SQLiteDatabase#CONFLICT_REPLACE}
     */
    private void storeUser(User user, SQLiteDatabase db, int mode) {
        ContentValues userColumn = new ContentValues(14);
        int flags = getUserFlags(db, user.getId());
        if (user.isVerified())
            flags |= VER_MASK;
        else
            flags &= ~VER_MASK;
        if (user.isLocked())
            flags |= LCK_MASK;
        else
            flags &= ~LCK_MASK;
        if (user.followRequested())
            flags |= FRQ_MASK;
        else
            flags &= ~FRQ_MASK;
        if (user.hasDefaultProfileImage())
            flags |= DEF_IMG;
        else
            flags &= ~DEF_IMG;

        userColumn.put(UserTable.ID, user.getId());
        userColumn.put(UserTable.USERNAME, user.getUsername());
        userColumn.put(UserTable.SCREENNAME, user.getScreenname());
        userColumn.put(UserTable.IMAGE, user.getImageLink());
        userColumn.put(UserTable.REGISTER, flags);
        userColumn.put(UserTable.DESCRIPTION, user.getBio());
        userColumn.put(UserTable.LINK, user.getLink());
        userColumn.put(UserTable.LOCATION, user.getLocation());
        userColumn.put(UserTable.BANNER, user.getBannerLink());
        userColumn.put(UserTable.SINCE, user.getCreatedAt());
        userColumn.put(UserTable.FRIENDS, user.getFollowing());
        userColumn.put(UserTable.FOLLOWER, user.getFollower());
        userColumn.put(UserTable.TWEETS, user.getTweetCount());
        userColumn.put(UserTable.FAVORS, user.getFavorCount());

        db.insertWithOnConflict(UserTable.TABLE, null, userColumn, mode);
    }


    /**
     * save tweet into database
     *
     * @param tweet          Tweet information
     * @param statusRegister predefined status register or zero if there isn't one
     * @param db             SQLite database
     */
    private void storeStatus(Tweet tweet, int statusRegister, SQLiteDatabase db) {
        ContentValues status = new ContentValues(17);
        User user = tweet.getUser();
        Tweet rtStat = tweet.getEmbeddedTweet();
        long rtId = -1L;

        if (rtStat != null) {
            storeStatus(rtStat, 0, db);
            rtId = rtStat.getId();
        }
        statusRegister |= getTweetFlags(db, tweet.getId());
        if (tweet.favored()) {
            statusRegister |= FAV_MASK;
        } else {
            statusRegister &= ~FAV_MASK;
        }
        if (tweet.retweeted()) {
            statusRegister |= RTW_MASK;
        } else {
            statusRegister &= ~RTW_MASK;
        }
        if (tweet.containsSensitiveMedia()) {
            statusRegister |= MEDIA_SENS_MASK;
        } else {
            statusRegister &= ~MEDIA_SENS_MASK;
        }
        switch (tweet.getMediaType()) {
            case IMAGE:
                statusRegister |= MEDIA_IMAGE_MASK;
                break;

            case VIDEO:
                statusRegister |= MEDIA_VIDEO_MASK;
                break;

            case GIF:
                statusRegister |= MEDIA_ANGIF_MASK;
                break;
        }
        status.put(TweetTable.MEDIA, getMediaLinks(tweet));
        status.put(TweetTable.REGISTER, statusRegister);
        status.put(TweetTable.ID, tweet.getId());
        status.put(TweetTable.USER, user.getId());
        status.put(TweetTable.SINCE, tweet.getTime());
        status.put(TweetTable.TWEET, tweet.getTweet());
        status.put(TweetTable.RETWEETID, rtId);
        status.put(TweetTable.SOURCE, tweet.getSource());
        status.put(TweetTable.REPLYTWEET, tweet.getReplyId());
        status.put(TweetTable.RETWEET, tweet.getRetweetCount());
        status.put(TweetTable.FAVORITE, tweet.getFavoriteCount());
        status.put(TweetTable.RETWEETUSER, tweet.getMyRetweetId());
        status.put(TweetTable.REPLYUSER, tweet.getReplyUserId());
        status.put(TweetTable.PLACE, tweet.getLocationName());
        status.put(TweetTable.COORDINATE, tweet.getLocationCoordinates());
        status.put(TweetTable.REPLYUSER, tweet.getReplyUserId());
        status.put(TweetTable.REPLYNAME, tweet.getReplyName());
        storeUser(user, db, CONFLICT_IGNORE);
        db.insertWithOnConflict(TweetTable.TABLE, null, status, CONFLICT_REPLACE);
    }

    /**
     * updates existing tweet
     *
     * @param tweet update of the tweet
     * @param db    database instance
     */
    private void updateStatus(Tweet tweet, SQLiteDatabase db) {
        String[] tweetIdArg = {Long.toString(tweet.getId())};
        String[] userIdArg = {Long.toString(tweet.getUser().getId())};

        ContentValues statColumn = new ContentValues(7);
        ContentValues userColumn = new ContentValues(9);
        int flags = getTweetFlags(db, tweet.getId());
        if (tweet.retweeted())
            flags |= RTW_MASK;
        else
            flags &= ~RTW_MASK;
        if (tweet.favored())
            flags |= FAV_MASK;
        else
            flags &= ~FAV_MASK;
        statColumn.put(TweetTable.TWEET, tweet.getTweet());
        statColumn.put(TweetTable.RETWEET, tweet.getRetweetCount());
        statColumn.put(TweetTable.FAVORITE, tweet.getFavoriteCount());
        statColumn.put(TweetTable.RETWEETUSER, tweet.getMyRetweetId());
        statColumn.put(TweetTable.REPLYNAME, tweet.getReplyName());
        statColumn.put(TweetTable.REGISTER, flags);
        statColumn.put(TweetTable.MEDIA, getMediaLinks(tweet));

        User user = tweet.getUser();
        userColumn.put(UserTable.USERNAME, user.getUsername());
        userColumn.put(UserTable.SCREENNAME, user.getScreenname());
        userColumn.put(UserTable.IMAGE, user.getImageLink());
        userColumn.put(UserTable.DESCRIPTION, user.getBio());
        userColumn.put(UserTable.LINK, user.getLink());
        userColumn.put(UserTable.LOCATION, user.getLocation());
        userColumn.put(UserTable.BANNER, user.getBannerLink());
        userColumn.put(UserTable.FRIENDS, user.getFollowing());
        userColumn.put(UserTable.FOLLOWER, user.getFollower());

        db.update(TweetTable.TABLE, statColumn, TWEET_SELECT, tweetIdArg);
        db.update(UserTable.TABLE, userColumn, USER_SELECT, userIdArg);
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
        db.insertWithOnConflict(FavoriteTable.TABLE, null, favTable, CONFLICT_REPLACE);
    }

    /**
     * clear old favorites from table
     *
     * @param db     database instance
     * @param userId ID of the favorite list owner
     */
    private void removeOldFavorites(SQLiteDatabase db, long userId) {
        String[] delArgs = {Long.toString(userId)};
        db.delete(FavoriteTable.TABLE, FAVORITE_SELECT_OWNER, delArgs);
    }

    /**
     * store direct message
     *
     * @param message direct message information
     * @param db      database instance
     */
    private void storeMessage(Message message, SQLiteDatabase db) {
        ContentValues messageColumn = new ContentValues(5);
        messageColumn.put(MessageTable.ID, message.getId());
        messageColumn.put(MessageTable.SINCE, message.getTime());
        messageColumn.put(MessageTable.SENDER, message.getSender().getId());
        messageColumn.put(MessageTable.RECEIVER, message.getReceiver().getId());
        messageColumn.put(MessageTable.MESSAGE, message.getText());
        storeUser(message.getSender(), db, CONFLICT_IGNORE);
        storeUser(message.getReceiver(), db, CONFLICT_IGNORE);
        db.insertWithOnConflict(MessageTable.TABLE, null, messageColumn, CONFLICT_IGNORE);
    }

    /**
     * get status register of a tweet or zero if tweet was not found
     *
     * @param db      database instance
     * @param tweetID ID of the tweet
     * @return tweet flags
     */
    private int getTweetFlags(SQLiteDatabase db, long tweetID) {
        String[] args = {Long.toString(tweetID)};

        Cursor c = db.rawQuery(TWEETFLAG_QUERY, args);
        int result = 0;
        if (c.moveToFirst()) {
            int pos = c.getColumnIndexOrThrow(TweetTable.REGISTER);
            result = c.getInt(pos);
        }
        c.close();
        return result;
    }

    /**
     * get flags of a twitter user or zero if user was not found
     *
     * @param db     database instance
     * @param userID ID of the user
     * @return user flags
     */
    private int getUserFlags(SQLiteDatabase db, long userID) {
        String[] args = {Long.toString(userID)};

        Cursor c = db.rawQuery(USERFLAG_QUERY, args);
        int result = 0;
        if (c.moveToFirst()) {
            int pos = c.getColumnIndexOrThrow(UserTable.REGISTER);
            result = c.getInt(pos);
        }
        c.close();
        return result;
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

        Cursor c = db.rawQuery(STATUS_EXIST_QUERY, args);
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
        return dataHelper.getDatabase();
    }

    /**
     * GET SQLite instance for writing database
     *
     * @return SQLite instance
     */
    private synchronized SQLiteDatabase getDbWrite() {
        SQLiteDatabase db = dataHelper.getDatabase();
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
     * Parse string where media links are separated by ' ; '
     *
     * @param media tweet media link
     * @return array of media links
     */
    private String[] parseMedia(String media) {
        Pattern splitter = Pattern.compile(";");
        return splitter.split(media);
    }

    /**
     * create string where media links are separated by ' ; '
     *
     * @param tweet tweet information
     * @return String of media links
     */
    private String getMediaLinks(Tweet tweet) {
        StringBuilder media = new StringBuilder();
        for (String link : tweet.getMediaLinks())
            media.append(link).append(";");
        return media.toString();
    }
}