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

    private final int FAV_MASK = 1;         //  FAVORITE MASK
    private final int RTW_MASK = 1 << 1;    //  RETWEET MASK
    private final int HOM_MASK = 1 << 2;    //  HOME TWEET MASK
    private final int MEN_MASK = 1 << 3;    //  MENTION MASK
    private final int UTW_MASK = 1 << 4;    //  USER TWEETS
    private final int RPL_MASK = 1 << 5;    //  TWEET ANSWERS

    private final int VER_MASK = 1;         //  USER VERIFIED MASK
    private final int LCK_MASK = 1 << 1;    //  USER LOCKED MASK


    private AppDatabase dataHelper;
    private long homeId;

    public DatabaseAdapter(Context context) {
        dataHelper = AppDatabase.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        homeId = settings.getUserId();
    }

    /**
     * Nutzer speichern
     *
     * @param user Nutzer Information
     */
    public void storeUser(TwitterUser user) {
        SQLiteDatabase db = getDbWrite();
        storeUser(user, db, CONFLICT_REPLACE);
        commit(db);
    }

    /**
     * Home Timeline speichern
     *
     * @param home Tweet Liste
     */
    public void storeHomeTimeline(List<Tweet> home) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : home) {
            storeStatus(tweet, HOM_MASK, db);
        }
        commit(db);
    }

    /**
     * Erwähnungen speichern
     *
     * @param mentions Tweet Liste
     */
    public void storeMentions(List<Tweet> mentions) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : mentions) {
            storeStatus(tweet, MEN_MASK, db);
        }
        commit(db);
    }

    /**
     * Nutzer Tweets speichern
     *
     * @param stats Tweet Liste
     */
    public void storeUserTweets(List<Tweet> stats) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : stats) {
            storeStatus(tweet, UTW_MASK, db);
        }
        commit(db);
    }

    /**
     * Nutzer Favoriten Speichern
     *
     * @param fav     Tweet Liste
     * @param ownerId User ID
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
     * Tweet Antworten speicher
     *
     * @param replies Tweet Antworten Liste
     */
    public void storeReplies(final List<Tweet> replies) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : replies) {
            storeStatus(tweet, RPL_MASK, db);
        }
        commit(db);
    }

    /**
     * Speichere Twitter Trends
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
     * Speichere Tweet in Favoriten Tabelle
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
     * Store currently sent tweet
     *
     * @param tweet new created tweet
     */
    public void storeTweet(Tweet tweet) {
        SQLiteDatabase db = getDbWrite();
        int mask = UTW_MASK | HOM_MASK;
        if (tweet.getReplyId() > 0)
            mask |= RPL_MASK;
        storeStatus(tweet, mask, db);
        commit(db);
    }

    /**
     * speicher direktnachrichten
     *
     * @param messages Direktnachrichten liste
     */
    public void storeMessage(List<Message> messages) {
        SQLiteDatabase db = getDbWrite();
        for (Message message : messages) {
            storeMessage(message, db);
        }
        commit(db);
    }

    /**
     * Lade Nutzer Information
     *
     * @param userId Nutzer ID
     * @return Nutzer Informationen oder NULL falls nicht vorhanden
     */
    @Nullable
    public TwitterUser getUser(long userId) {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        TwitterUser result = getUser(userId, db);
        db.close();
        return result;
    }

    /**
     * Lade Home Timeline
     *
     * @return Tweet Liste
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
     * Erwähnungen laden
     *
     * @return Tweet Liste
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
     * Tweet Liste eines Nutzers
     *
     * @param userID Nutzer ID
     * @return Tweet Liste des Users
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
     * Lade Favorisierte Tweets eines Nutzers
     *
     * @param ownerID Nutzer ID
     * @return Favoriten des Nutzers
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
     * Lade Tweet
     *
     * @param tweetId Tweet ID
     * @return Gefundener Tweet oder NULL falls nicht vorhanden
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
     * Lade Antworten
     *
     * @param tweetId Tweet ID
     * @return Antworten zur Tweet ID
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
     * Aktualisiere Status
     *
     * @param tweet Tweet
     */
    public void updateStatus(Tweet tweet) {
        SQLiteDatabase db = getDbWrite();
        ContentValues status = new ContentValues();
        int register = getStatRegister(db, tweet.getId());
        if (tweet.retweeted())
            register |= RTW_MASK;
        else
            register &= ~RTW_MASK;

        if (tweet.favorized())
            register |= FAV_MASK;
        else
            register &= ~FAV_MASK;
        status.put("retweet", tweet.getRetweetCount());
        status.put("favorite", tweet.getFavorCount());
        status.put("statusregister", register);
        db.update("tweet", status, "tweet.tweetID=" + tweet.getId(), null);
        commit(db);
    }

    /**
     * Lösche Tweet
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
     * Entferne Tweet aus der Favoriten Tabelle
     *
     * @param tweetId ID des tweets
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
     * Direkt nachrichten laden
     *
     * @return Liste Direktnachrichten
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
     * Suche Tweet in Datenbank
     *
     * @param id Tweet ID
     * @return True falls gefunden, ansonsten False
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


    private void commit(SQLiteDatabase db) {
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