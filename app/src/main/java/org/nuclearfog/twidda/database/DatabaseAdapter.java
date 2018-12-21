package org.nuclearfog.twidda.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.backend.items.Trend;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.TwitterUser;

import java.util.ArrayList;
import java.util.List;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

public class DatabaseAdapter {

    public static final int LIMIT = 100;    //  DATABASE ENTRY LIMIT

    private static final int FAV_MASK = 1;         //  FAVORITE MASK
    private static final int RTW_MASK = 1 << 1;    //  RETWEET MASK
    private static final int HOM_MASK = 1 << 2;    //  HOME TWEET MASK
    private static final int MEN_MASK = 1 << 3;    //  MENTION MASK
    private static final int UTW_MASK = 1 << 4;    //  USER TWEETS
    private static final int RPL_MASK = 1 << 5;    //  TWEET ANSWERS

    private static final int VER_MASK = 1;         //  USER VERIFIED MASK
    private static final int LCK_MASK = 1 << 1;    //  USER LOCKED MASK

    private AppDatabase dataHelper;
    private long homeId;

    public DatabaseAdapter(Context context) {
        dataHelper = AppDatabase.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        homeId = settings.getUserId();
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
        for (Tweet tweet : home) {
            storeStatus(tweet, HOM_MASK, db);
        }
        commit(db);
    }

    /**
     * store mentions
     *
     * @param mentions tweets
     */
    public void storeMentions(List<Tweet> mentions) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : mentions) {
            storeStatus(tweet, MEN_MASK, db);
        }
        commit(db);
    }

    /**
     * store user timeline
     *
     * @param stats user timeline
     */
    public void storeUserTweets(List<Tweet> stats) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : stats) {
            storeStatus(tweet, UTW_MASK, db);
        }
        commit(db);
    }

    /**
     * store user favors
     *
     * @param fav tweet favored by user
     * @param ownerId user ID
     */
    public void storeUserFavs(List<Tweet> fav, long ownerId) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : fav) {
            storeStatus(tweet, 0, db);
            ContentValues favTable = new ContentValues();
            favTable.put("tweetID", tweet.getId());
            favTable.put("ownerID", ownerId);
            db.insertWithOnConflict("favorit", null, favTable, CONFLICT_IGNORE);
        }
        commit(db);
    }

    /**
     * store replies of a tweet
     *
     * @param replies tweet replies
     */
    public void storeReplies(final List<Tweet> replies) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : replies) {
            storeStatus(tweet, RPL_MASK, db);
        }
        commit(db);
    }

    /**
     * store location specific trends
     *
     * @param trends List of Trends
     * @param woeId  Yahoo World ID
     */
    public void storeTrends(final List<Trend> trends, int woeId) {
        SQLiteDatabase db = getDbWrite();
        String query = "DELETE FROM trend WHERE woeID=" + woeId;
        db.execSQL(query);
        for (Trend trend : trends) {
            storeTrends(trend, woeId, db);
        }
        commit(db);
    }

    /**
     * store tweet ID of a favored tweet by the current user
     *
     * @param tweetID Tweet ID
     */
    public void storeFavorite(long tweetID) {
        SQLiteDatabase db = getDbWrite();

        ContentValues favTable = new ContentValues();
        ContentValues status = new ContentValues();

        int register = getStatRegister(db, tweetID);
        register |= FAV_MASK;

        favTable.put("tweetID", tweetID);
        favTable.put("ownerID", homeId);
        status.put("statusregister", register);

        db.insertWithOnConflict("favorit", null, favTable, CONFLICT_IGNORE);
        db.update("tweet", status, "tweet.tweetID=" + tweetID, null);
        commit(db);
    }

    /**
     * store direct messages
     *
     * @param messages list of direct messages
     */
    public void storeMessage(List<Message> messages) {
        SQLiteDatabase db = getDbWrite();
        for (Message message : messages) {
            storeMessage(message, db);
        }
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
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        TwitterUser result = getUser(userId, db);
        db.close();
        return result;
    }

    /**
     * load home timeline
     *
     * @return tweet list
     */
    public List<Tweet> getHomeTimeline() {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        List<Tweet> tweetList = new ArrayList<>();
        String SQL_GET_HOME = "SELECT * FROM tweet " +
                "INNER JOIN user ON tweet.userID=user.userID " +
                "WHERE statusregister&" + HOM_MASK + ">0 " +
                "ORDER BY tweetID DESC LIMIT " + LIMIT;
        Cursor cursor = db.rawQuery(SQL_GET_HOME, null);
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
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        List<Tweet> tweetList = new ArrayList<>();
        String SQL_GET_HOME = "SELECT * FROM tweet " +
                "INNER JOIN user ON tweet.userID=user.userID " +
                "WHERE statusregister&" + MEN_MASK + ">0 " +
                "ORDER BY tweetID DESC LIMIT " + LIMIT;
        Cursor cursor = db.rawQuery(SQL_GET_HOME, null);
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
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        List<Tweet> tweetList = new ArrayList<>();
        String SQL_GET_HOME = "SELECT * FROM tweet " +
                "INNER JOIN user ON tweet.userID = user.userID " +
                "WHERE statusregister&" + UTW_MASK + ">0 " +
                "AND user.userID =" + userID +
                " ORDER BY tweetID DESC LIMIT " + LIMIT;

        Cursor cursor = db.rawQuery(SQL_GET_HOME, null);

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
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        List<Tweet> tweetList = new ArrayList<>();
        String SQL_GET_HOME = "SELECT * FROM tweet " +
                "INNER JOIN favorit on tweet.tweetID = favorit.tweetID " +
                "INNER JOIN user ON tweet.userID = user.userID " +
                "WHERE favorit.ownerID =" + ownerID +
                " ORDER BY tweetID DESC LIMIT " + LIMIT;
        Cursor cursor = db.rawQuery(SQL_GET_HOME, null);
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
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        Tweet result = null;
        String query = "SELECT * FROM tweet " +
                "INNER JOIN user ON user.userID = tweet.userID " +
                "WHERE tweet.tweetID==" + tweetId + " LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
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
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        List<Tweet> tweetList = new ArrayList<>();
        String SQL_GET_HOME = "SELECT * FROM tweet " +
                "INNER JOIN user ON tweet.userID = user.userID " +
                "WHERE tweet.replyID=" + tweetId + " AND statusregister&" + RPL_MASK + ">0 " +
                "ORDER BY tweetID DESC LIMIT " + LIMIT;
        Cursor cursor = db.rawQuery(SQL_GET_HOME, null);
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
        ContentValues statColumn = new ContentValues();
        ContentValues userColumn = new ContentValues();
        int register = getStatRegister(db, tweet.getId());
        if (tweet.retweeted())
            register |= RTW_MASK;
        else
            register &= ~RTW_MASK;
        if (tweet.favorized())
            register |= FAV_MASK;
        else
            register &= ~FAV_MASK;
        statColumn.put("retweet", tweet.getRetweetCount());
        statColumn.put("favorite", tweet.getFavorCount());
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

        db.update("tweet", statColumn, "tweet.tweetID=" + tweet.getId(), null);
        db.update("user", userColumn, "user.userID=" + user.getId(), null);
        commit(db);
    }

    /**
     * remove status
     *
     * @param id Tweet ID
     */
    public void removeStatus(long id) {
        SQLiteDatabase db = getDbWrite();
        db.delete("tweet", "tweetID=" + id, null);
        db.delete("favorit", "tweetID=" + id + " AND ownerID=" + homeId, null);
        commit(db);
    }

    /**
     * remove status from favorites
     *
     * @param tweetId tweet ID
     */
    public void removeFavorite(long tweetId) {
        SQLiteDatabase db = getDbWrite();
        int register = getStatRegister(db, tweetId);
        register &= ~FAV_MASK;
        ContentValues status = new ContentValues();
        status.put("statusregister", register);
        db.delete("favorit", "tweetID=" + tweetId + " AND ownerID=" + homeId, null);
        db.update("tweet", status, "tweet.tweetID=" + tweetId, null);
        commit(db);
    }

    /**
     * Delete Direct Message
     *
     * @param id Direct Message ID
     */
    public void deleteDm(long id) {
        SQLiteDatabase db = getDbWrite();
        db.delete("message", "messageID=" + id, null);
        commit(db);
    }

    /**
     * Load trend List
     *
     * @param woeId Yahoo World ID
     * @return list of trends
     */
    public List<Trend> getTrends(int woeId) {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        List<Trend> trends = new ArrayList<>();
        String query = "SELECT * FROM trend WHERE woeID=" + woeId + " ORDER BY trendpos ASC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                int index = cursor.getColumnIndex("trendpos");
                int position = cursor.getInt(index);
                index = cursor.getColumnIndex("trendname");
                String name = cursor.getString(index);
                trends.add(new Trend(position, name));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return trends;
    }

    /**
     * load direct messages
     *
     * @return list of direct messages
     */
    public List<Message> getMessages() {
        List<Message> result = new ArrayList<>();
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        String query = "SELECT * FROM message ORDER BY messageID DESC LIMIT " + LIMIT;
        Cursor cursor = db.rawQuery(query, null);
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
        db.close();
        return result;
    }

    /**
     * check if tweet exists in database
     *
     * @param id Tweet ID
     * @return true if found
     */
    public boolean containStatus(long id) {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        String query = "SELECT tweetID FROM tweet WHERE tweetID=" + id + " LIMIT 1;";
        Cursor c = db.rawQuery(query, null);
        boolean result = c.moveToFirst();
        c.close();
        db.close();
        return result;
    }


    private Tweet getStatus(Cursor cursor) {
        int index;
        index = cursor.getColumnIndex("time");
        long time = cursor.getLong(index);
        index = cursor.getColumnIndex("tweet");
        String tweettext = cursor.getString(index);
        index = cursor.getColumnIndex("retweet");
        int retweet = cursor.getInt(index);
        index = cursor.getColumnIndex("favorite");
        int favorit = cursor.getInt(index);
        index = cursor.getColumnIndex("tweetID");
        long tweetId = cursor.getLong(index);
        index = cursor.getColumnIndex("retweetID");
        long retweetId = cursor.getLong(index);
        index = cursor.getColumnIndex("replyname");
        String replyname = cursor.getString(index);
        index = cursor.getColumnIndex("replyID");
        long replyStatusId = cursor.getLong(index);
        index = cursor.getColumnIndex("retweeterID");
        long retweeterId = cursor.getLong(index);
        index = cursor.getColumnIndex("source");
        String source = cursor.getString(index);
        index = cursor.getColumnIndex("media");
        String medialinks = cursor.getString(index);
        index = cursor.getColumnIndex("replyUserID");
        long replyUserId = cursor.getLong(index);
        index = cursor.getColumnIndex("statusregister");
        int statusregister = cursor.getInt(index);
        boolean favorited = (statusregister & FAV_MASK) > 0;
        boolean retweeted = (statusregister & RTW_MASK) > 0;

        String[] medias = parseMedia(medialinks);

        TwitterUser user = getUser(cursor);
        Tweet embeddedTweet = null;
        if (retweetId > 1)
            embeddedTweet = getStatus(retweetId);
        return new Tweet(tweetId, retweet, favorit, user, tweettext, time, replyname, replyUserId, medias,
                source, replyStatusId, embeddedTweet, retweeterId, retweeted, favorited);
    }


    private TwitterUser getUser(long userId, SQLiteDatabase db) {
        TwitterUser user = null;
        String query = "SELECT * FROM user WHERE userID=" + userId + " LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst())
            user = getUser(cursor);
        cursor.close();
        return user;
    }


    private TwitterUser getUser(Cursor cursor) {
        int index = cursor.getColumnIndex("userID");
        long userId = cursor.getLong(index);
        index = cursor.getColumnIndex("username");
        String username = cursor.getString(index);
        index = cursor.getColumnIndex("scrname");
        String screenname = cursor.getString(index);
        index = cursor.getColumnIndex("userregister");
        int userRegister = cursor.getInt(index);
        index = cursor.getColumnIndex("pbLink");
        String profileImg = cursor.getString(index);
        index = cursor.getColumnIndex("bio");
        String bio = cursor.getString(index);
        index = cursor.getColumnIndex("link");
        String link = cursor.getString(index);
        index = cursor.getColumnIndex("location");
        String location = cursor.getString(index);
        index = cursor.getColumnIndex("banner");
        String banner = cursor.getString(index);
        index = cursor.getColumnIndex("createdAt");
        long createdAt = cursor.getLong(index);
        index = cursor.getColumnIndex("following");
        int following = cursor.getInt(index);
        index = cursor.getColumnIndex("follower");
        int follower = cursor.getInt(index);

        boolean isVerified = (userRegister & VER_MASK) > 0;
        boolean isLocked = (userRegister & LCK_MASK) > 0;
        return new TwitterUser(userId, username, screenname, profileImg, bio,
                location, isVerified, isLocked, link, banner, createdAt, following, follower);
    }


    private void storeUser(TwitterUser user, SQLiteDatabase db, int mode) {
        ContentValues userColumn = new ContentValues();
        int userRegister = 0;
        if (user.isVerified())
            userRegister |= VER_MASK;
        if (user.isLocked())
            userRegister |= LCK_MASK;
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
        db.insertWithOnConflict("user", null, userColumn, mode);
    }


    private void storeStatus(Tweet tweet, int statusRegister, SQLiteDatabase db) {
        ContentValues status = new ContentValues();
        TwitterUser user = tweet.getUser();
        Tweet rtStat = tweet.getEmbeddedTweet();
        long rtId = 1L;

        if (rtStat != null) {
            storeStatus(rtStat, 0, db);
            rtId = rtStat.getId();
        }

        statusRegister |= getStatRegister(db, tweet.getId());
        if (tweet.favorized()) {
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
        status.put("tweet", tweet.getText());
        status.put("retweetID", rtId);
        status.put("source", tweet.getSource());
        status.put("replyID", tweet.getReplyId());
        status.put("replyname", tweet.getReplyName());
        status.put("retweet", tweet.getRetweetCount());
        status.put("favorite", tweet.getFavorCount());
        status.put("retweeterID", tweet.getMyRetweetId());
        status.put("replyUserID", tweet.getReplyUserId());
        storeUser(user, db, CONFLICT_IGNORE);
        db.insertWithOnConflict("tweet", null, status, CONFLICT_REPLACE);
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


    private void storeTrends(Trend trend, int woeId, SQLiteDatabase db) {
        ContentValues trendColumn = new ContentValues();
        trendColumn.put("woeID", woeId);
        trendColumn.put("trendpos", trend.getPosition());
        trendColumn.put("trendname", trend.getName());
        db.insertWithOnConflict("trend", null, trendColumn, CONFLICT_REPLACE);
    }


    private int getStatRegister(SQLiteDatabase db, long tweetID) {
        String query = "SELECT statusregister FROM tweet WHERE tweetID=" + tweetID + " LIMIT 1;";
        Cursor c = db.rawQuery(query, null);
        int result = 0;
        if (c.moveToFirst()) {
            int pos = c.getColumnIndex("statusregister");
            result = c.getInt(pos);
        }
        c.close();
        return result;
    }


    private synchronized SQLiteDatabase getDbWrite() {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        db.beginTransaction();
        return db;
    }


    private synchronized void commit(SQLiteDatabase db) {
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }


    private String[] parseMedia(String media) {
        int index;
        List<String> links = new ArrayList<>();
        do {
            index = media.indexOf(';');
            if (index > 0 && index < media.length()) {
                links.add(media.substring(0, index));
                media = media.substring(index + 1);
            }
        } while (index > 0);
        String[] result = new String[links.size()];
        return links.toArray(result);
    }
}