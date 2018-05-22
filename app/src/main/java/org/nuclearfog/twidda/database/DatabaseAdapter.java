package org.nuclearfog.twidda.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.backend.listitems.TwitterUser;

import java.util.ArrayList;
import java.util.List;

public class DatabaseAdapter {

    public static final int FAVT  = 1;
    public static final int TWEET = 2;
    public static final int HOME  = 3;
    public static final int MENT  = 4;
    public static final int ANS   = 5;
    private AppDatabase dataHelper;

    /**
     * Public Cunstructor
     * @param context Activity Context
     */
    public DatabaseAdapter(Context context) {
        dataHelper = AppDatabase.getInstance(context);
    }


    /**
     * Store Tweet List
     * @param stats List of Tweets
     * @param mode store in extra table
     * @param id Owner ID of favorite table
     */
    public void store(final List<Tweet> stats, final int mode, final long id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dataHelper.getWritableDatabase();

                ContentValues home  = new ContentValues();
                ContentValues fav   = new ContentValues();
                ContentValues ment  = new ContentValues();

                for(int pos = 0; pos < stats.size(); pos++) {
                    Tweet tweet = stats.get(pos);
                    storeStatus(tweet,db);

                    if(mode != TWEET) {
                        if(mode == HOME) {
                            home.put("tweetID", tweet.tweetID);
                            db.insertWithOnConflict("timeline",null,home,SQLiteDatabase.CONFLICT_REPLACE);
                        } else if(mode == FAVT) {
                            fav.put("tweetID", tweet.tweetID);
                            fav.put("userID", id);
                            db.insertWithOnConflict("favorit",null,fav,SQLiteDatabase.CONFLICT_REPLACE);
                        } else if(mode == MENT) {
                            ment.put("tweetID", tweet.tweetID);
                            db.insertWithOnConflict("mention",null,ment,SQLiteDatabase.CONFLICT_REPLACE);
                        }
                    }
                }
            }
        }).start();
    }


    /**
     * Load Tweet list from Table
     * @param mode select table
     * @param id User ID for user tweets and favorites
     * @return List of Tweets
     */
    public List<Tweet> load(int mode, long id) {
        List<Tweet> tweetList = new ArrayList<>();
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        String SQL_GET_HOME="";
        int limit = 0;
        if(mode == HOME) {
            SQL_GET_HOME = "SELECT * FROM tweet " +
                    "INNER JOIN timeline ON timeline.tweetID = tweet.tweetID " +
                    "INNER JOIN user ON tweet.userID = user.userID ORDER BY tweetID DESC";
        }
        else if(mode == MENT) {
            SQL_GET_HOME = "SELECT * FROM tweet " +
                    "INNER JOIN mention ON mention.tweetID = tweet.tweetID " +
                    "INNER JOIN user ON tweet.userID = user.userID ORDER BY tweetID DESC";
        }
        else if(mode == TWEET) {
            SQL_GET_HOME = "SELECT * FROM tweet " +
                    "INNER JOIN user ON tweet.userID = user.userID"+
                    " WHERE user.userID = "+id+" ORDER BY tweetID DESC";
        }
        else if(mode == FAVT) {
            SQL_GET_HOME = "SELECT * FROM tweet " +
                    "INNER JOIN favorit ON favorit.tweetID = tweet.tweetID " +
                    "INNER JOIN user ON tweet.userID = user.userID " +
                    "WHERE favorit.userID = "+id+" ORDER BY tweetID DESC";
        }
        else if(mode == ANS) {
            SQL_GET_HOME = "SELECT * FROM tweet " +
                    "INNER JOIN user ON tweet.userID = user.userID"+
                    " WHERE tweet.replyID = "+id+" ORDER BY tweetID DESC";
        }
        Cursor cursor = db.rawQuery(SQL_GET_HOME,null);
        if(cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while(cursor.moveToNext() && limit++ < 200);
        }
        cursor.close();
        return tweetList;
    }


    /**
     * get single Tweet
     * @param tweetId ID of tweet
     * @return Tweet or null if not found
     */
    public Tweet getStatus(long tweetId) {
        SQLiteDatabase search = dataHelper.getReadableDatabase();
        Tweet result = null;
        String query = "SELECT * FROM tweet " +
                "INNER JOIN user ON user.userID = tweet.userID " +
                "WHERE tweet.tweetID == " + tweetId;
        Cursor cursor = search.rawQuery(query,null);
        if(cursor.moveToFirst())
            result = getStatus(cursor);
        cursor.close();
        return result;
    }


    /**
     * get single User
     * @param userId User ID
     * @return User or null if not found
     */
    public TwitterUser getUser(long userId) {
        SQLiteDatabase search = dataHelper.getReadableDatabase();
        TwitterUser user = null;
        String query = "SELECT * FROM user WHERE userID ="+ userId;
        Cursor cursor = search.rawQuery(query, null);
        if(cursor.moveToFirst())
            user = getUser(cursor);
        cursor.close();
        return user;
    }


    /**
     * Store single Tweet
     * @param tweet Tweet to be stored
     */
    public void storeStatus(final Tweet tweet) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                storeStatus(tweet, dataHelper.getWritableDatabase());
            }
        }).start();
    }

    /**
     * Store single User
     * @param user Twitteruser to be stored
     */
    public void storeUser(final TwitterUser user) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                storeUser(user, dataHelper.getWritableDatabase());
            }
        }).start();
    }


    /**
     * Delete Status
     * @param id Status id
     */
    public void removeStatus(final long id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dataHelper.getWritableDatabase();
                db.delete("tweet", "tweetID="+id, null);
            }
        }).start();
    }

    /**
     * Check if Tweet exists in Database
     * @param id Tweet ID
     * @return true if found
     */
    public boolean containStatus(long id) {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        String query = "SELECT EXISTS(SELECT tweetID FROM tweet WHERE tweetID="+id+" LIMIT 1);";
        Cursor c = db.rawQuery(query,null);
        c.moveToFirst();
        boolean found = c.getInt(0) == 1;
        c.close();
        return found;
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
        index = cursor.getColumnIndex("retweeted");
        boolean retweeted = cursor.getInt(index) == 1;
        index = cursor.getColumnIndex("favorized");
        boolean favorized = cursor.getInt(index) == 1;
        index = cursor.getColumnIndex("replyname");
        String replyname = cursor.getString(index);
        index = cursor.getColumnIndex("replyID");
        long replyStatusId = cursor.getLong(index);
        index = cursor.getColumnIndex("source");
        String source = cursor.getString(index);
        TwitterUser user = getUser(cursor);
        Tweet embeddedTweet = null;
        if(retweetId > 0)
            embeddedTweet = getStatus(retweetId);
        return new Tweet(tweetId,retweet,favorit,user,tweettext,time,replyname,null/*TODO*/,
                source,replyStatusId,embeddedTweet,retweeted,favorized);
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


    private void storeStatus(Tweet tweet, SQLiteDatabase db) {
        ContentValues status = new ContentValues();
        Tweet rtStat = tweet.embedded;
        long rtId = -1;

        if(rtStat != null) {
            storeStatus(rtStat, db);
            rtId = rtStat.tweetID;
        }
        TwitterUser mUser = tweet.user;
        storeUser(mUser,db);
        status.put("tweetID", tweet.tweetID);
        status.put("userID", mUser.userID);
        status.put("time", tweet.time);
        status.put("tweet", tweet.tweet);
        status.put("retweet", tweet.retweet);
        status.put("favorite", tweet.favorit);
        status.put("retweetID", rtId);
        status.put("source", tweet.source);
        status.put("replyID", tweet.replyID);
        status.put("replyname", tweet.replyName);
        status.put("retweeted",tweet.retweeted);
        status.put("favorized", tweet.favorized);
        db.insertWithOnConflict("tweet",null, status,SQLiteDatabase.CONFLICT_REPLACE);
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
        db.insertWithOnConflict("user",null, userColumn,SQLiteDatabase.CONFLICT_REPLACE);
    }
}