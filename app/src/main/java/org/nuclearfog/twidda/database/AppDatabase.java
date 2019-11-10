package org.nuclearfog.twidda.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.TwitterUser;

import java.util.LinkedList;
import java.util.List;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

public class AppDatabase {

    private static final int FAV_MASK = 1;          //  FAVORITE MASK
    private static final int RTW_MASK = 1 << 1;     //  RETWEET MASK
    private static final int HOM_MASK = 1 << 2;     //  HOME TWEET MASK
    private static final int MEN_MASK = 1 << 3;     //  MENTION MASK
    private static final int UTW_MASK = 1 << 4;     //  USER TWEETS
    private static final int RPL_MASK = 1 << 5;     //  TWEET ANSWERS

    private static final int VER_MASK = 1;          //  USER VERIFIED MASK
    private static final int LCK_MASK = 1 << 1;     //  USER LOCKED MASK
    private static final int FRQ_MASK = 1 << 2;     //  USER REQUEST FOLLOW
    private static final int EXCL_USR = 1 << 3;     //  EXCLUDE USERS TWEETS

    private final int limit;       //  DATABASE ENTRY limit
    private final long homeId;

    private DatabaseAdapter dataHelper;

    public AppDatabase(Context context) {
        dataHelper = DatabaseAdapter.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        homeId = settings.getUserId();
        limit = settings.getRowLimit();
    }

    /**
     * Store user information
     *
     * @param user Twitter user
     */
    public void storeUser(TwitterUser user) {
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
        for (Tweet tweet : fav) {
            storeStatus(tweet, 0, db);
            storeFavorite(tweet, ownerId, db);
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
    public void storeTrends(List<String> trends, int woeId) {
        final String[] ARGS = new String[]{Integer.toString(woeId)};

        SQLiteDatabase db = getDbWrite();
        db.delete("trend", "woeid=?", ARGS);
        for (String trend : trends) {
            ContentValues trendColumn = new ContentValues();
            trendColumn.put("woeID", woeId);
            trendColumn.put("trendname", trend);
            db.insertWithOnConflict("trend", null, trendColumn, CONFLICT_REPLACE);
        }
        commit(db);
    }

    /**
     * store tweet ID of a favored tweet by the current user
     *
     * @param tweet favored tweet
     */
    public void storeFavorite(Tweet tweet) {
        SQLiteDatabase db = getDbWrite();
        long tweetID = tweet.getId();

        if (!containsFavor(homeId, tweetID, db)) {
            storeStatus(tweet, 0, db);
            storeFavorite(tweet, homeId, db);
        }
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
     * get user information
     *
     * @param userId ID of user
     * @return user information or null if not found
     */
    @Nullable
    public TwitterUser getUser(long userId) {
        SQLiteDatabase db = getDbRead();
        return getUser(userId, db);
    }

    /**
     * load home timeline
     *
     * @return tweet list
     */
    public List<Tweet> getHomeTimeline() {
        final String[] ARGS = new String[]{Integer.toString(HOM_MASK), Integer.toString(limit)};
        final String QUERY = "SELECT * FROM tweet INNER JOIN user ON tweet.userID=user.userID " +
                "WHERE statusregister&? IS NOT 0 ORDER BY tweetID DESC LIMIT ?";

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();

        Cursor cursor = db.rawQuery(QUERY, ARGS);
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
        final String[] ARGS = new String[]{Integer.toString(MEN_MASK), Integer.toString(EXCL_USR), Integer.toString(limit)};
        final String QUERY = "SELECT * FROM tweet INNER JOIN user ON tweet.userID=user.userID " +
                "WHERE statusregister&? IS NOT 0 AND userregister&? IS 0 ORDER BY tweetID DESC LIMIT ?";

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();

        Cursor cursor = db.rawQuery(QUERY, ARGS);
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
        final String[] ARGS = new String[]{Integer.toString(UTW_MASK), Long.toString(userID), Integer.toString(limit)};
        final String QUERY = "SELECT * FROM tweet INNER JOIN user ON tweet.userID=user.userID " +
                "WHERE statusregister&? IS NOT 0 AND user.userID=? ORDER BY tweetID DESC LIMIT ?";

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();

        Cursor cursor = db.rawQuery(QUERY, ARGS);
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
    public List<Tweet> getUserFavs(long ownerID) {
        final String[] ARGS = new String[]{Long.toString(ownerID), Integer.toString(limit)};
        final String QUERY = "SELECT * FROM tweet INNER JOIN favorit on tweet.tweetID=favorit.tweetID " +
                "INNER JOIN user ON tweet.userID=user.userID WHERE favorit.ownerID=? ORDER BY tweetID DESC LIMIT ?";

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();

        Cursor cursor = db.rawQuery(QUERY, ARGS);
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
     * load status
     *
     * @param tweetId tweet ID
     * @return tweet or null if not found
     */
    @Nullable
    public Tweet getStatus(long tweetId) {
        final String[] ARGS = new String[]{Long.toString(tweetId)};
        final String QUERY = "SELECT * FROM tweet INNER JOIN user " +
                "ON user.userID = tweet.userID WHERE tweet.tweetID=? LIMIT 1";

        SQLiteDatabase db = getDbRead();
        Tweet result = null;

        Cursor cursor = db.rawQuery(QUERY, ARGS);
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
        final String[] ARGS = new String[]{Long.toString(tweetId), Integer.toString(RPL_MASK),
                Integer.toString(EXCL_USR), Integer.toString(limit)};
        final String QUERY = "SELECT * FROM tweet INNER JOIN user ON tweet.userID=user.userID " +
                "WHERE tweet.replyID=? AND statusregister&? IS NOT 0 AND userregister&? IS 0 ORDER BY tweetID DESC LIMIT ?";

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();

        Cursor cursor = db.rawQuery(QUERY, ARGS);
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
        final String[] tweetIdArg = {Long.toString(tweet.getId())};
        final String[] userIdArg = {Long.toString(tweet.getUser().getId())};

        SQLiteDatabase db = getDbWrite();
        ContentValues statColumn = new ContentValues();
        ContentValues userColumn = new ContentValues();
        int register = getTweetStatus(db, tweet.getId());
        if (tweet.retweeted())
            register |= RTW_MASK;
        else
            register &= ~RTW_MASK;
        if (tweet.favored())
            register |= FAV_MASK;
        else
            register &= ~FAV_MASK;
        statColumn.put("retweet", tweet.getRetweetCount());
        statColumn.put("favorite", tweet.getFavorCount());
        statColumn.put("retweeterID", tweet.getMyRetweetId());
        statColumn.put("replyname", tweet.getReplyName());
        statColumn.put("statusregister", register);

        TwitterUser user = tweet.getUser();
        userColumn.put("username", user.getUsername());
        userColumn.put("scrname", user.getScreenname());
        userColumn.put("pbLink", user.getImageLink());
        userColumn.put("bio", user.getBio());
        userColumn.put("link", user.getLink());
        userColumn.put("location", user.getLocation());
        userColumn.put("banner", user.getBannerLink());
        userColumn.put("following", user.getFollowing());
        userColumn.put("follower", user.getFollower());

        db.update("tweet", statColumn, "tweet.tweetID=?", tweetIdArg);
        db.update("user", userColumn, "user.userID=?", userIdArg);
        commit(db);
    }

    /**
     * remove status
     *
     * @param tweetId Tweet ID
     */
    public void removeStatus(long tweetId) {
        final String[] args = {Long.toString(tweetId)};

        SQLiteDatabase db = getDbWrite();
        db.delete("tweet", "tweetID=?", args);
        db.delete("favorit", "tweetID=?", args);
        commit(db);
    }

    /**
     * remove status containing a retweet
     *
     * @param tweetId tweet ID of retweet
     */
    public void removeRetweet(long tweetId) {
        Tweet tweet = getStatus(tweetId);
        if (tweet != null) {
            final String[] args = {Long.toString(tweet.getMyRetweetId())};
            SQLiteDatabase db = getDbWrite();
            db.delete("tweet", "tweetID=?", args);
            commit(db);
        }
    }

    /**
     * remove status from favorites
     *
     * @param tweetId tweet ID
     */
    public void removeFavorite(long tweetId) {
        final String[] delArgs = {Long.toString(tweetId), Long.toString(homeId)};
        final String[] updateArgs = {Long.toString(tweetId)};

        SQLiteDatabase db = getDbWrite();
        int register = getTweetStatus(db, tweetId);
        register &= ~FAV_MASK;
        ContentValues status = new ContentValues();
        status.put("statusregister", register);
        db.delete("favorit", "tweetID=? AND ownerID=?", delArgs);
        db.update("tweet", status, "tweet.tweetID=?", updateArgs);
        commit(db);
    }

    /**
     * Delete Direct Message
     *
     * @param id Direct Message ID
     */
    public void deleteDm(long id) {
        final String[] messageId = {Long.toString(id)};

        SQLiteDatabase db = getDbWrite();
        db.delete("message", "messageID=?", messageId);
        commit(db);
    }

    /**
     * Load trend List
     *
     * @param woeId Yahoo World ID
     * @return list of trends
     */
    public List<String> getTrends(int woeId) {
        final String[] ARGS = new String[]{Integer.toString(woeId)};
        final String QUERY = "SELECT * FROM trend WHERE woeID=? ORDER BY trendpos ASC";

        List<String> trends = new LinkedList<>();
        SQLiteDatabase db = getDbRead();
        Cursor cursor = db.rawQuery(QUERY, ARGS);
        if (cursor.moveToFirst()) {
            do {
                int index = cursor.getColumnIndex("trendname");
                String trendName = cursor.getString(index);
                trends.add(trendName);
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
    public List<Message> getMessages() {
        final String[] ARGS = new String[]{Integer.toString(limit)};
        final String QUERY = "SELECT * FROM message ORDER BY messageID DESC LIMIT ?";

        List<Message> result = new LinkedList<>();
        SQLiteDatabase db = getDbRead();
        Cursor cursor = db.rawQuery(QUERY, ARGS);
        if (cursor.moveToFirst()) {
            do {
                int index = cursor.getColumnIndex("senderID");
                long senderID = cursor.getLong(index);
                index = cursor.getColumnIndex("receiverID");
                long receiverID = cursor.getLong(index);
                index = cursor.getColumnIndex("message");
                String message = cursor.getString(index);
                index = cursor.getColumnIndex("time");
                long time = cursor.getLong(index);
                index = cursor.getColumnIndex("messageID");
                long messageId = cursor.getLong(index);

                TwitterUser sender = getUser(senderID, db);
                TwitterUser receiver = getUser(receiverID, db);

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
        final String[] ARGS = new String[]{Long.toString(id)};

        SQLiteDatabase db = getDbWrite();
        int userRegister = getUserStatus(db, id);
        if (mute)
            userRegister |= EXCL_USR;
        else
            userRegister &= ~EXCL_USR;

        ContentValues userColumn = new ContentValues();
        userColumn.put("userregister", userRegister);
        db.update("user", userColumn, "user.userID=?", ARGS);
        commit(db);
    }


    private Tweet getStatus(Cursor cursor) {
        long time = cursor.getLong(cursor.getColumnIndex("time"));
        String tweettext = cursor.getString(cursor.getColumnIndex("tweet"));
        int retweet = cursor.getInt(cursor.getColumnIndex("retweet"));
        int favorit = cursor.getInt(cursor.getColumnIndex("favorite"));
        long tweetId = cursor.getLong(cursor.getColumnIndex("tweetID"));
        long retweetId = cursor.getLong(cursor.getColumnIndex("retweetID"));
        String replyname = cursor.getString(cursor.getColumnIndex("replyname"));
        long replyStatusId = cursor.getLong(cursor.getColumnIndex("replyID"));
        long retweeterId = cursor.getLong(cursor.getColumnIndex("retweeterID"));
        String source = cursor.getString(cursor.getColumnIndex("source"));
        String medialinks = cursor.getString(cursor.getColumnIndex("media"));
        String place = cursor.getString(cursor.getColumnIndex("place"));
        String geo = cursor.getString(cursor.getColumnIndex("geo"));
        long replyUserId = cursor.getLong(cursor.getColumnIndex("replyUserID"));
        int statusregister = cursor.getInt(cursor.getColumnIndex("statusregister"));
        boolean favorited = (statusregister & FAV_MASK) > 0;
        boolean retweeted = (statusregister & RTW_MASK) > 0;
        String[] medias = parseMedia(medialinks);
        TwitterUser user = getUser(cursor);
        Tweet embeddedTweet = null;
        if (retweetId > 1)
            embeddedTweet = getStatus(retweetId);
        return new Tweet(tweetId, retweet, favorit, user, tweettext, time, replyname, replyUserId, medias,
                source, replyStatusId, embeddedTweet, retweeterId, retweeted, favorited, place, geo);
    }


    @Nullable
    private TwitterUser getUser(long userId, SQLiteDatabase db) {
        final String[] ARGS = new String[]{Long.toString(userId)};
        final String QUERY = "SELECT * FROM user WHERE userID=? LIMIT 1";

        TwitterUser user = null;
        Cursor cursor = db.rawQuery(QUERY, ARGS);
        if (cursor.moveToFirst())
            user = getUser(cursor);
        cursor.close();
        return user;
    }


    private TwitterUser getUser(Cursor cursor) {
        long userId = cursor.getLong(cursor.getColumnIndex("userID"));
        String username = cursor.getString(cursor.getColumnIndex("username"));
        String screenname = cursor.getString(cursor.getColumnIndex("scrname"));
        int userRegister = cursor.getInt(cursor.getColumnIndex("userregister"));
        String profileImg = cursor.getString(cursor.getColumnIndex("pbLink"));
        String bio = cursor.getString(cursor.getColumnIndex("bio"));
        String link = cursor.getString(cursor.getColumnIndex("link"));
        String location = cursor.getString(cursor.getColumnIndex("location"));
        String banner = cursor.getString(cursor.getColumnIndex("banner"));
        long createdAt = cursor.getLong(cursor.getColumnIndex("createdAt"));
        int following = cursor.getInt(cursor.getColumnIndex("following"));
        int follower = cursor.getInt(cursor.getColumnIndex("follower"));
        int tCount = cursor.getInt(cursor.getColumnIndex("tweetCount"));
        int fCount = cursor.getInt(cursor.getColumnIndex("favorCount"));
        boolean isVerified = (userRegister & VER_MASK) > 0;
        boolean isLocked = (userRegister & LCK_MASK) > 0;
        boolean isReq = (userRegister & FRQ_MASK) > 0;
        return new TwitterUser(userId, username, screenname, profileImg, bio, location, isVerified,
                isLocked, isReq, link, banner, createdAt, following, follower, tCount, fCount);
    }


    private void storeUser(TwitterUser user, SQLiteDatabase db, int mode) {
        ContentValues userColumn = new ContentValues();
        int userRegister = getUserStatus(db, user.getId());
        if (user.isVerified())
            userRegister |= VER_MASK;
        else
            userRegister &= ~VER_MASK;
        if (user.isLocked())
            userRegister |= LCK_MASK;
        else
            userRegister &= ~LCK_MASK;
        if (user.followRequested())
            userRegister |= FRQ_MASK;
        else
            userRegister &= ~FRQ_MASK;

        userColumn.put("userID", user.getId());
        userColumn.put("username", user.getUsername());
        userColumn.put("scrname", user.getScreenname());
        userColumn.put("pbLink", user.getImageLink());
        userColumn.put("userregister", userRegister);
        userColumn.put("bio", user.getBio());
        userColumn.put("link", user.getLink());
        userColumn.put("location", user.getLocation());
        userColumn.put("banner", user.getBannerLink());
        userColumn.put("createdAt", user.getCreatedAt());
        userColumn.put("following", user.getFollowing());
        userColumn.put("follower", user.getFollower());
        userColumn.put("tweetCount", user.getTweetCount());
        userColumn.put("favorCount", user.getFavorCount());

        db.insertWithOnConflict("user", null, userColumn, mode);
    }


    private void storeStatus(Tweet tweet, int statusRegister, SQLiteDatabase db) {
        ContentValues status = new ContentValues();
        TwitterUser user = tweet.getUser();
        Tweet rtStat = tweet.getEmbeddedTweet();
        long rtId = -1L;

        if (rtStat != null) {
            storeStatus(rtStat, 0, db);
            rtId = rtStat.getId();
        }

        statusRegister |= getTweetStatus(db, tweet.getId());
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

        StringBuilder media = new StringBuilder();
        for (String link : tweet.getMediaLinks()) {
            media.append(link);
            media.append(";");
        }
        status.put("media", media.toString());
        status.put("statusregister", statusRegister);
        status.put("tweetID", tweet.getId());
        status.put("userID", user.getId());
        status.put("time", tweet.getTime());
        status.put("tweet", tweet.getTweet());
        status.put("retweetID", rtId);
        status.put("source", tweet.getSource());
        status.put("replyID", tweet.getReplyId());
        status.put("retweet", tweet.getRetweetCount());
        status.put("favorite", tweet.getFavorCount());
        status.put("retweeterID", tweet.getMyRetweetId());
        status.put("replyUserID", tweet.getReplyUserId());
        status.put("place", tweet.getLocationName());
        status.put("geo", tweet.getLocationCoordinates());
        status.put("replyUserID", tweet.getReplyUserId());
        if (tweet.getReplyUserId() > 0)
            status.put("replyname", tweet.getReplyName());
        storeUser(user, db, CONFLICT_IGNORE);
        db.insertWithOnConflict("tweet", null, status, CONFLICT_REPLACE);
    }


    private void storeFavorite(Tweet tweet, long ownerId, SQLiteDatabase db) {
        if (!containsFavor(ownerId, tweet.getId(), db)) {
            ContentValues favTable = new ContentValues();
            favTable.put("tweetID", tweet.getId());
            favTable.put("ownerID", ownerId);
            db.insertWithOnConflict("favorit", null, favTable, CONFLICT_IGNORE);
        }
    }


    private void storeMessage(Message message, SQLiteDatabase db) {
        ContentValues messageColumn = new ContentValues();
        messageColumn.put("messageID", message.getId());
        messageColumn.put("time", message.getTime());
        messageColumn.put("senderID", message.getSender().getId());
        messageColumn.put("receiverID", message.getReceiver().getId());
        messageColumn.put("message", message.getText());
        storeUser(message.getSender(), db, CONFLICT_IGNORE);
        storeUser(message.getReceiver(), db, CONFLICT_IGNORE);
        db.insertWithOnConflict("message", null, messageColumn, CONFLICT_IGNORE);
    }


    private int getTweetStatus(SQLiteDatabase db, long tweetID) {
        final String[] ARGS = new String[]{Long.toString(tweetID)};
        final String QUERY = "SELECT statusregister FROM tweet WHERE tweetID=? LIMIT 1;";

        Cursor c = db.rawQuery(QUERY, ARGS);
        int result = 0;
        if (c.moveToFirst()) {
            int pos = c.getColumnIndex("statusregister");
            result = c.getInt(pos);
        }
        c.close();
        return result;
    }


    private int getUserStatus(SQLiteDatabase db, long userID) {
        final String[] ARGS = new String[]{Long.toString(userID)};
        final String QUERY = "SELECT userregister FROM user WHERE userID=? LIMIT 1;";

        Cursor c = db.rawQuery(QUERY, ARGS);
        int result = 0;
        if (c.moveToFirst()) {
            int pos = c.getColumnIndex("userregister");
            result = c.getInt(pos);
        }
        c.close();
        return result;
    }


    /**
     * check if tweet exists in database
     *
     * @param id Tweet ID
     * @param db opened database
     * @return true if found
     */
    private boolean containStatus(long id, SQLiteDatabase db) {
        final String[] ARGS = new String[]{Long.toString(id)};
        final String QUERY = "SELECT tweetID FROM tweet WHERE tweetID=? LIMIT 1;";

        Cursor c = db.rawQuery(QUERY, ARGS);
        boolean result = c.moveToFirst();
        c.close();
        return result;
    }


    /**
     * check if Tweet already exists in favorite table
     *
     * @param userId  user ID of the owner
     * @param tweetId tweet ID
     * @param db      database for read
     * @return true if tweet found
     */
    private boolean containsFavor(long userId, long tweetId, SQLiteDatabase db) {
        final String[] ARGS = new String[]{Long.toString(userId), Long.toString(tweetId)};
        final String QUERY = "SELECT tweetID FROM favorit WHERE ownerID=? AND tweetID=? LIMIT 1;";

        Cursor c = db.rawQuery(QUERY, ARGS);
        boolean result = c.moveToFirst();
        c.close();
        return result;
    }


    private synchronized SQLiteDatabase getDbRead() {
        return dataHelper.getDatabase();
    }


    private synchronized SQLiteDatabase getDbWrite() {
        SQLiteDatabase db = dataHelper.getDatabase();
        db.beginTransaction();
        return db;
    }


    private synchronized void commit(SQLiteDatabase db) {
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    private String[] parseMedia(String media) {
        int index;
        List<String> links = new LinkedList<>();
        do {
            index = media.indexOf(';');
            if (index > 0 && index < media.length()) {
                links.add(media.substring(0, index));
                media = media.substring(index + 1);
            }
        } while (index > 0);
        return links.toArray(new String[0]);
    }
}