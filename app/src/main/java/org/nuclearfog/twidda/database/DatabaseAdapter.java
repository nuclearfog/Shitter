package org.nuclearfog.twidda.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.nuclearfog.twidda.backend.GlobalSettings;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.backend.listitems.TwitterUser;

import java.util.ArrayList;
import java.util.List;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

public class DatabaseAdapter {

    private final int LIMIT = 100;
    private final int favoritedmask = 1;
    private final int retweetedmask = 1 << 1;
    private final int hometlmask = 1 << 2;
    private final int mentionmask = 1 << 3;
    private final int usertweetmask = 1 << 4;
    private final int replymask = 1 << 5;

    private AppDatabase dataHelper;
    private long homeId;

    public DatabaseAdapter(Context context) {
        dataHelper = AppDatabase.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        homeId = settings.getUserId();
    }

    /**
     * Nutzer speichern
     * @param user Nutzer Information
     */
    public void storeUser(TwitterUser user) {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        storeUser(user, db);
        db.close();
    }

    /**
     * Home Timeline speichern
     * @param home Tweet Liste
     */
    public void storeHomeTimeline(List<Tweet> home) {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        for (int pos = 0; pos < home.size(); pos++) {
            Tweet tweet = home.get(pos);
            storeStatus(tweet, hometlmask, db);
        }
        db.close();
    }

    /**
     * Erwähnungen speichern
     * @param mentions Tweet Liste
     */
    public void storeMentions(List<Tweet> mentions) {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        for (int pos = 0; pos < mentions.size(); pos++) {
            Tweet tweet = mentions.get(pos);
            storeStatus(tweet, mentionmask, db);
        }
        db.close();
    }

    /**
     * Nutzer Tweets speichern
     * @param stats Tweet Liste
     */
    public void storeUserTweets(List<Tweet> stats) {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        for(int pos = 0; pos < stats.size(); pos++) {
            Tweet tweet = stats.get(pos);
            storeStatus(tweet,usertweetmask,db);
        }
        db.close();
    }

    /**
     * Nutzer Favoriten Speichern
     * @param fav Tweet Liste
     * @param ownerId User ID
     */
    public void storeUserFavs(List<Tweet> fav, long ownerId){
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        for(int pos = 0; pos < fav.size(); pos++) {
            Tweet tweet = fav.get(pos);
            storeStatus(tweet,0,db);
            ContentValues favTable = new ContentValues();
            favTable.put("tweetID", tweet.tweetID);
            favTable.put("ownerID", ownerId);
            db.insertWithOnConflict("favorit",null,favTable,CONFLICT_IGNORE);
        }
        db.close();
    }

    /**
     * Tweet Antworten speicher
     * @param replies Tweet Antworten Liste
     */
    public void storeReplies(List<Tweet> replies) {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        for(int pos = 0; pos < replies.size(); pos++) {
            Tweet tweet = replies.get(pos);
            storeStatus(tweet,replymask,db);
        }
        db.close();
    }

    /**
     * Speichere Tweet in Favoriten Tabelle
     * @param tweet Tweet
     */
    public void storeFavorite(Tweet tweet) {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        storeStatus(tweet, favoritedmask, db);
        db.close();
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
        TwitterUser user = null;
        String query = "SELECT * FROM user WHERE userID=" + userId + " LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst())
            user = getUser(cursor);
        cursor.close();
        db.close();
        return user;
    }

    /**
     * Lade Home Timeline
     * @return Tweet Liste
     */
    public List<Tweet> getHomeTimeline() {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        List<Tweet> tweetList = new ArrayList<>();
        String SQL_GET_HOME = "SELECT * FROM tweet " +
                "INNER JOIN user ON tweet.userID=user.userID " +
                "WHERE statusregister&"+hometlmask+">0 "+
                "ORDER BY tweetID DESC LIMIT "+LIMIT;
        Cursor cursor = db.rawQuery(SQL_GET_HOME,null);
        if(cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tweetList;
    }

    /**
     * Erwähnungen laden
     * @return Tweet Liste
     */
    public List<Tweet> getMentions() {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        List<Tweet> tweetList = new ArrayList<>();
        String SQL_GET_HOME = "SELECT * FROM tweet " +
                "INNER JOIN user ON tweet.userID=user.userID " +
                "WHERE statusregister&"+mentionmask+">0 "+
                "ORDER BY tweetID DESC LIMIT "+LIMIT;
        Cursor cursor = db.rawQuery(SQL_GET_HOME,null);
        if(cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tweetList;
    }

    /**
     * Tweet Liste eines Nutzers
     * @param userID Nutzer ID
     * @return Tweet Liste des Users
     */
    public List<Tweet> getUserTweets(long userID) {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        List<Tweet> tweetList = new ArrayList<>();
        String SQL_GET_HOME = "SELECT * FROM tweet "+
                "INNER JOIN user ON tweet.userID = user.userID " +
                "WHERE statusregister&"+usertweetmask+">0 "+
                "AND user.userID ="+userID+" ORDER BY tweetID DESC LIMIT "+LIMIT;

        Cursor cursor = db.rawQuery(SQL_GET_HOME,null);

        if(cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tweetList;
    }

    /**
     * Lade Favorisierte Tweets eines Nutzers
     * @param userID Nutzer ID
     * @return Favoriten des Nutzers
     */
    public List<Tweet> getUserFavs(long userID) {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        List<Tweet> tweetList = new ArrayList<>();
        String SQL_GET_HOME = "SELECT * FROM tweet " +
                "INNER JOIN user ON tweet.userID = user.userID " +
                "INNER JOIN favorit on tweet.tweetID = favorit.tweetID " +
                "WHERE favorit.ownerID =" + userID + " ORDER BY tweetID DESC LIMIT "+LIMIT;
        Cursor cursor = db.rawQuery(SQL_GET_HOME,null);
        if(cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tweetList;
    }

    /**
     * Lade Tweet
     * @param tweetId Tweet ID
     * @return Gefundener Tweet oder NULL falls nicht vorhanden
     */
    @Nullable
    public Tweet getStatus(long tweetId) {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        Tweet result = null;
        String query = "SELECT * FROM tweet " +
                "INNER JOIN user ON user.userID = tweet.userID " +
                "WHERE tweet.tweetID=="+tweetId+" LIMIT 1";
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.moveToFirst())
            result = getStatus(cursor);
        cursor.close();
        db.close();
        return result;
    }

    /**
     * Lade Antworten
     * @param tweetId Tweet ID
     * @return Antworten zur Tweet ID
     */
    public List<Tweet> getAnswers(long tweetId) {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        List<Tweet> tweetList = new ArrayList<>();
        String SQL_GET_HOME = "SELECT * FROM tweet " +
                "INNER JOIN user ON tweet.userID = user.userID " +
                "WHERE tweet.replyID=" + tweetId + " AND statusregister&" + replymask + ">0 " +
                "ORDER BY tweetID DESC LIMIT " + LIMIT;
        Cursor cursor = db.rawQuery(SQL_GET_HOME, null);
        if (cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tweetList;
    }

    /**
     * Aktualisiere Status
     * @param tweet Tweet
     */
    public void updateStatus(Tweet tweet) {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        ContentValues status = new ContentValues();
        int register = getStatRegister(db,tweet.tweetID);
        if (tweet.retweeted)
            register |= retweetedmask;
        else
            register &= ~retweetedmask;

        if (tweet.favorized)
            register |= favoritedmask;
        else
            register &= ~favoritedmask;

        status.put("retweet",tweet.retweet);
        status.put("favorite",tweet.favorit);
        status.put("statusregister",register);
        db.update("tweet",status,"tweet.tweetID="+tweet.tweetID,null);
        db.close();
    }

    /**
     * Lösche Tweet
     * @param id Tweet ID
     */
    public void removeStatus(long id) {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        db.delete("tweet","tweetID="+id,null);
        db.close();
    }

    /**
     * Entferne Tweet aus der Favoriten Tabelle
     *
     * @param tweetId ID des tweets
     */
    public void removeFavorite(long tweetId) {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        db.delete("favorit", "tweetID=" + tweetId + " AND ownerID=" + homeId, null);

        int register = getStatRegister(db, tweetId);
        register &= ~favoritedmask;

        ContentValues status = new ContentValues();
        status.put("statusregister", register);
        db.update("tweet", status, "tweet.tweetID=" + tweetId, null);
        db.close();
    }

    /**
     * Suche Tweet in Datenbank
     * @param id Tweet ID
     * @return True falls gefunden, ansonsten False
     */
    public boolean containStatus(long id) {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        String query = "SELECT EXISTS(SELECT tweetID FROM tweet WHERE tweetID="+id+" LIMIT 1);";
        Cursor c = db.rawQuery(query,null);
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
        boolean favorited = (statusregister & 1) == favoritedmask;
        boolean retweeted = (statusregister & 2) == retweetedmask;

        String[] medias = parseMedia(medialinks);

        TwitterUser user = getUser(cursor);
        Tweet embeddedTweet = null;
        if(retweetId > 1)
            embeddedTweet = getStatus(retweetId);
        return new Tweet(tweetId,retweet,favorit,user,tweettext,time,replyname, replyUserId,medias,
                source,replyStatusId,embeddedTweet,retweeterId,retweeted,favorited);
    }


    private TwitterUser getUser(Cursor cursor){
        int index = cursor.getColumnIndex("userID");
        long userId = cursor.getLong(index);
        index = cursor.getColumnIndex("username");
        String username = cursor.getString(index);
        index = cursor.getColumnIndex("scrname");
        String screenname = cursor.getString(index);
        index = cursor.getColumnIndex("verify");
        boolean isVerified = cursor.getInt(index) == 1;
        index = cursor.getColumnIndex("locked");
        boolean locked = cursor.getInt(index) == 1;
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
        return new TwitterUser(userId, username,screenname,profileImg,bio,
                location,isVerified,locked,link,banner,createdAt,following,follower);
    }


    private void storeStatus(Tweet tweet, int newStatusRegister, SQLiteDatabase db) {
        ContentValues status = new ContentValues();
        TwitterUser mUser = tweet.user;
        Tweet rtStat = tweet.embedded;
        long rtId = 1L;

        if(rtStat != null) {
            storeStatus(rtStat,0, db);
            rtId = rtStat.tweetID;
        }

        storeUser(mUser,db);
        status.put("tweetID", tweet.tweetID);
        status.put("userID", mUser.userID);
        status.put("time", tweet.time);
        status.put("tweet", tweet.tweet);
        status.put("retweetID", rtId);
        status.put("source", tweet.source);
        status.put("replyID", tweet.replyID);
        status.put("replyname", tweet.replyName);
        status.put("retweet", tweet.retweet);
        status.put("favorite", tweet.favorit);
        status.put("retweeterID", tweet.retweetId);
        status.put("replyUserID", tweet.replyUserId);
        String[] medialinks = tweet.media;
        StringBuilder media = new StringBuilder();
        for(String link : medialinks) {
            media.append(link);
            media.append(";");
        }
        status.put("media",media.toString());
        int statusregister = getStatRegister(db,tweet.tweetID);
        statusregister |= newStatusRegister;
        if (tweet.favorized)
            statusregister |= favoritedmask;
        else
            statusregister &= ~favoritedmask;

        if (tweet.retweeted)
            statusregister |= retweetedmask;
        else
            statusregister &= ~retweetedmask;

        status.put("statusregister", statusregister);
        db.insertWithOnConflict("tweet", null, status, CONFLICT_REPLACE);
    }


    private int getStatRegister(SQLiteDatabase db, long tweetID) {
        String query = "SELECT statusregister FROM tweet WHERE tweetID="+tweetID+" LIMIT 1;";
        Cursor c = db.rawQuery(query,null);
        int result = 0;
        if(c.moveToFirst()) {
            int pos = c.getColumnIndex("statusregister");
            result = c.getInt(pos);
        }
        c.close();
        return result;
    }


    private void storeUser(TwitterUser user, SQLiteDatabase db) {
        ContentValues userColumn   = new ContentValues();
        userColumn.put("userID", user.userID);
        userColumn.put("username", user.username);
        userColumn.put("scrname", user.screenname.substring(1));
        userColumn.put("pbLink", user.profileImg);
        userColumn.put("verify", user.isVerified);
        userColumn.put("locked", user.isLocked);
        userColumn.put("bio", user.bio);
        userColumn.put("link", user.link);
        userColumn.put("location", user.location);
        userColumn.put("banner", user.bannerImg);
        userColumn.put("createdAt", user.created);
        userColumn.put("following", user.following);
        userColumn.put("follower", user.follower);
        db.insertWithOnConflict("user",null, userColumn, CONFLICT_REPLACE);
    }


    private String[] parseMedia(String media) {
        int index;
        List<String> links = new ArrayList<>();
        do {
            index = media.indexOf(';');
            if(index > 0 && index < media.length()) {
                links.add(media.substring(0,index));
                media = media.substring(index+1);
            }
        } while(index > 0);
        String[] result = new String[links.size()];
        return links.toArray(result);
    }
}