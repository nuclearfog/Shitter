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
    private Context context;

    public TweetDatabase(Context context) {
        dataHelper = AppDatabase.getInstance(context);
        tweetlist = new ArrayList<>();
        this.context = context;
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
                    " WHERE user.userID = "+id+" ORDER BY tweetID DESC";
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
                tweetlist.add(tweet);
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tweetlist;
    }


    public Tweet getTweet(long tweetId) {
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


    private Tweet getTweet(Cursor cursor) {
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
            embeddedTweet = getTweet(retweetId);
        return new Tweet(tweetId,retweet,favorit,user,tweettext,time,replyname,null,
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
        index = cursor.getColumnIndex("fullpb");
        String fullpb = cursor.getString(index);
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
        return new TwitterUser(userId, username,screenname,profileImg,fullpb,bio,
                location,isVerified,locked,link,banner,createdAt,following,follower);
    }


    private void storeStatus(Tweet tweet, SQLiteDatabase db, long retweetID) {
        ContentValues status = new ContentValues();
        ContentValues user   = new ContentValues();

        status.put("tweetID", tweet.tweetID);
        status.put("time", tweet.time);
        status.put("tweet", tweet.tweet);
        status.put("retweet", tweet.retweet);
        status.put("favorite", tweet.favorit);
        status.put("retweetID", retweetID);
        status.put("source", tweet.source);
        status.put("replyID", tweet.replyID);
        status.put("replyname", tweet.replyName);
        status.put("retweeted",tweet.retweeted);
        status.put("favorized", tweet.favorized);
        TwitterUser mUser = tweet.user;
        user.put("userID", mUser.userID);
        user.put("username", mUser.username);
        user.put("scrname", mUser.screenname.substring(1));
        user.put("pbLink", mUser.profileImg);
        user.put("fullpb", mUser.fullpb);
        user.put("verify", mUser.isVerified);
        user.put("locked", mUser.isLocked);
        user.put("bio", mUser.bio);
        user.put("link", mUser.link);
        user.put("location", mUser.location);
        user.put("banner", mUser.bannerImg);
        user.put("createdAt", mUser.created);
        user.put("following", mUser.following);
        user.put("follower", mUser.follower);

        db.insertWithOnConflict("tweet",null, status,SQLiteDatabase.CONFLICT_REPLACE);
        db.insertWithOnConflict("user",null, user,SQLiteDatabase.CONFLICT_REPLACE);
    }


    public void removeStatus(long id) {
        SQLiteDatabase db = AppDatabase.getInstance(context).getWritableDatabase();
        db.delete("tweet", "tweetID"+"="+id, null);
        db.close();
    }
}