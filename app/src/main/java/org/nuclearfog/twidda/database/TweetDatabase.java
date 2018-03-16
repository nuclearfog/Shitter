package org.nuclearfog.twidda.database;

import org.nuclearfog.twidda.backend.listitems.*;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class TweetDatabase {

    public static final int FAVT  = 1;
    public static final int TWEET = 2;
    public static final int HOME  = 3;
    public static final int MENT  = 4;

    private AppDatabase dataHelper;
    private List<Tweet> tweetlist;

    public TweetDatabase(Context context) {
        dataHelper = AppDatabase.getInstance(context);
        tweetlist = new ArrayList<>();
    }


    public void store(List<Tweet> stats, int mode, long id) {
        SQLiteDatabase db = dataHelper.getWritableDatabase();

        ContentValues home  = new ContentValues();
        ContentValues fav   = new ContentValues();
        ContentValues ment  = new ContentValues();

        for(int pos = 0; pos < stats.size(); pos++) {
            Tweet tweet = stats.get(pos);
            Tweet rtStat = tweet.embedded;

            if(rtStat != null) {
                storeStatus(rtStat, db,-1L);
                storeStatus(tweet, db, rtStat.tweetID);
            } else {
                storeStatus(tweet, db, -1L);
            }

            if(mode != TWEET) {
                if(mode == HOME) {
                    home.put("tweetID", tweet.tweetID);
                    db.insertWithOnConflict("timeline",null,home,SQLiteDatabase.CONFLICT_REPLACE);
                } else if(mode == FAVT) {
                    fav.put("tweetID", tweet.tweetID);
                    fav.put("userID", id);
                    db.insertWithOnConflict("favorit",null,fav,SQLiteDatabase.CONFLICT_REPLACE);
                } else if(mode == MENT) {
                    ment.put("mTweetID", tweet.tweetID);
                    db.insertWithOnConflict("timeline",null,ment,SQLiteDatabase.CONFLICT_IGNORE);
                }
            }
        }
        db.close();
    }


    public List<Tweet> load(int mode, long id) {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        String SQL_GET_HOME=" ";

        if(mode == HOME) {
            SQL_GET_HOME = "SELECT * FROM timeline " +
                    "INNER JOIN tweet ON timeline.tweetID = tweet.tweetID " +
                    "INNER JOIN user ON tweet.userID=user.userID ORDER BY tweetID DESC";
        } else if(mode == MENT) {
            SQL_GET_HOME = "SELECT * FROM timeline " +
                    "INNER JOIN tweet ON timeline.mTweetID = tweet.tweetID " +
                    "INNER JOIN user ON tweet.userID=user.userID ORDER BY tweetID ASC";
        }

        else if(mode == TWEET) {
            SQL_GET_HOME = "SELECT * FROM user " +
                    "INNER JOIN tweet ON tweet.userID = user.userID"+
                    " WHERE user.userID = "+id+ " ORDER BY tweetID DESC";
        } else if(mode == FAVT) {
            SQL_GET_HOME = "SELECT * FROM favorit " +
                    "INNER JOIN tweet ON favorit.tweetID = tweet.tweetID " +
                    "INNER JOIN user ON tweet.userID = user.userID " +
                    "WHERE favorit.userID = "+id+" ORDER BY tweetID DESC";
        }
        Cursor cursor = db.rawQuery(SQL_GET_HOME,null);
        if(cursor.moveToFirst()) {
            do {
                Tweet tweet = getTweet(cursor);
               /* if(tweet.embedded != null)
                    tweetlist.add(tweet.embedded);
                else*/
                    tweetlist.add(tweet);
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tweetlist;
    }


    private Tweet searchTweet(long tweetId) {
        SQLiteDatabase search = dataHelper.getReadableDatabase();
        Tweet result = null;
        String query = "SELECT * FROM tweet " +
                "INNER JOIN user ON user.userID = tweet.userID " +
                "WHERE tweet.tweetID == " + tweetId;
        Cursor cursor = search.rawQuery(query,null);
        if(cursor.moveToFirst())
            result = getTweet(cursor);
        cursor.close();
        return result;
    }


    private Tweet getTweet(Cursor cursor){
        int index = cursor.getColumnIndex("time");
        long time = cursor.getLong(index);
        index = cursor.getColumnIndex("tweet");
        String tweettext = cursor.getString(index);
        index = cursor.getColumnIndex("retweet");
        int retweet = cursor.getInt(index);
        index = cursor.getColumnIndex("favorite");
        int favorit = cursor.getInt(index);
        index = cursor.getColumnIndex("username");
        String username = cursor.getString(index);
        index = cursor.getColumnIndex("scrname");
        String screenname = cursor.getString(index);
        index = cursor.getColumnIndex("verify");
        boolean isVerified = cursor.getInt(index) == 1;
        index = cursor.getColumnIndex("pbLink");
        String profileImg = cursor.getString(index);
        index = cursor.getColumnIndex("userID");
        long userId = cursor.getLong(index);
        index = cursor.getColumnIndex("tweetID");
        long tweetId = cursor.getLong(index);
        index = cursor.getColumnIndex("retweetID");
        long retweetId = cursor.getLong(index);
        index = cursor.getColumnIndex("replyname");
        String replyname = cursor.getString(index);
        index = cursor.getColumnIndex("replyID");
        long replyStatusId = cursor.getLong(index);
        index = cursor.getColumnIndex("source");
        String source = cursor.getString(index);

        Tweet embeddedTweet = null;
        if(retweetId > 0) {
            embeddedTweet = searchTweet(retweetId);
        }
        return new Tweet(tweetId,userId,username,screenname,retweet,favorit,
                profileImg,tweettext, time, replyname, null, source, replyStatusId,
                isVerified,embeddedTweet,false,false);
    }


    private void storeStatus(Tweet tweet, SQLiteDatabase db, long retweetID) {
        ContentValues status = new ContentValues();
        ContentValues user   = new ContentValues();
        status.put("userID", tweet.userID);
        status.put("tweetID", tweet.tweetID);
        status.put("time", tweet.time);
        status.put("tweet", tweet.tweet);
        status.put("retweet", tweet.retweet);
        status.put("favorite", tweet.favorit);
        status.put("retweetID", retweetID);
        status.put("source", tweet.source);
        status.put("replyID", tweet.replyID);
        status.put("replyname", tweet.replyName);

        user.put("userID", tweet.userID);
        user.put("username", tweet.username);
        user.put("scrname", tweet.screenname);
        user.put("pbLink", tweet.profileImg);
        user.put("verify", tweet.verified);

        db.insertWithOnConflict("tweet",null, status,SQLiteDatabase.CONFLICT_REPLACE);
        db.insertWithOnConflict("user",null, user,SQLiteDatabase.CONFLICT_REPLACE);
    }


    public static void removeStatus(Context c, long id) {
        SQLiteDatabase db = AppDatabase.getInstance(c).getWritableDatabase();
        db.delete("tweet", "tweetID"+"="+id, null);
        db.close();
    }
}