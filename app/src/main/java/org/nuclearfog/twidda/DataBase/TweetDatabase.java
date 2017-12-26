package org.nuclearfog.twidda.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import twitter4j.Status;
import twitter4j.User;

public class TweetDatabase {
    public static final int HOME_TL = 0;
    public static final int FAV_TL  = 1;
    public static final int USER_TL = 2;

    private AppDatabase dataHelper;
    private List<String> user,tweet,noRT,noFav,noAns,pbLink;
    private List<Long> userId,tweetId,timeMillis;
    private List<Status> stats;
    private Context c;
    private int size = 0;
    private int mode = 0;
    private long userid = 0;
    private SharedPreferences settings;

    /**
     * Store & Read Data
     * @param stats   Twitter Home
     */
    public TweetDatabase(List<Status> stats, Context c, int mode,long userid) {
        this.stats=stats;
        this.c=c;
        this.userid = userid;
        this.mode=mode;
        dataHelper = AppDatabase.getInstance(c);
        settings = c.getSharedPreferences("settings", 0);
        initArray();
        store();
        load();
    }

    /**
     * Read Data
     * @param c MainActivity Context
     */
    public TweetDatabase(Context c, int mode, long userid) {
        this.c=c;
        this.userid=userid;
        this.mode=mode;
        dataHelper = AppDatabase.getInstance(c);
        settings = c.getSharedPreferences("settings", 0);
        initArray();
        load();
    }

    private void store() {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        ContentValues user  = new ContentValues();
        ContentValues tweet = new ContentValues();
        ContentValues home  = new ContentValues();
        ContentValues fav   = new ContentValues();

        for(int pos = 0; pos < stats.size(); pos++) {

            Status stat = stats.get(pos);
            User usr = stat.getUser();

            user.put("userID",usr.getId());
            user.put("username", usr.getName());
            user.put("pbLink", usr.getProfileImageURL());
            user.put("banner", usr.getProfileBannerURL());
            user.put("bio",usr.getDescription());
            user.put("location",usr.getLocation());
            user.put("link",usr.getURL());

            tweet.put("userID", usr.getId());  // this one does not work TODO
            tweet.put("tweetID", stat.getId());
            tweet.put("time", stat.getCreatedAt().getTime());
            tweet.put("tweet", stat.getText());
            tweet.put("retweet", stat.getRetweetCount());
            tweet.put("favorite", stat.getFavoriteCount());
            tweet.put("answers", 0);

            home.put("tweetID", stat.getId());

            fav.put("ownerID", userid);

            db.insertWithOnConflict("user",null, user,SQLiteDatabase.CONFLICT_IGNORE);
            db.insertWithOnConflict("tweet",null, tweet,SQLiteDatabase.CONFLICT_IGNORE);

            if(mode!=USER_TL) {
                if(mode == HOME_TL) {
                    db.insertWithOnConflict("timeline",null,home,SQLiteDatabase.CONFLICT_IGNORE);
                }
                else if(mode == FAV_TL) {
                    db.insertWithOnConflict("favorit",null,home,SQLiteDatabase.CONFLICT_IGNORE);
                    db.insertWithOnConflict("favorit",null,fav,SQLiteDatabase.CONFLICT_IGNORE);
                }
            }
        }
        db.close();
    }

    private void load() {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        int index;
        size = 0;
        String SQL_GET_HOME=" ";

        if(mode==HOME_TL) {
            SQL_GET_HOME = "SELECT * FROM timeline " +
                    "INNER JOIN tweet ON timeline.tweetID = tweet.tweetID " +
                    "INNER JOIN user ON tweet.userID=user.userID ORDER BY time DESC";
        } else if(mode==FAV_TL) {
            SQL_GET_HOME = "SELECT * FROM favorit " +
                    "INNER JOIN tweet ON favorit.tweetID = tweet.tweetID " +
                    "INNER JOIN user ON tweet.userID=user.userID " +
                    /*"WHERE favorit.ownerID = "+userid+*/" ORDER BY tweet.time DESC";
        } else if(mode==USER_TL) {
            SQL_GET_HOME = "SELECT * FROM user INNER JOIN tweet ON user.userID = tweet.userID " +
                    "WHERE user.userID = "+userid+" ORDER BY tweet.time DESC";
        }

        Cursor cursor = db.rawQuery(SQL_GET_HOME,null);

        if(cursor.moveToFirst()) {
            do {
                index = cursor.getColumnIndex("time"); // time
                timeMillis.add(cursor.getLong(index));
                index = cursor.getColumnIndex("tweet"); // tweet
                tweet.add( cursor.getString(index) );
                index = cursor.getColumnIndex("retweet"); // retweet
                noRT.add( cursor.getString(index) );
                index = cursor.getColumnIndex("favorite"); // fav
                noFav.add( cursor.getString(index) );
                index = cursor.getColumnIndex("answers"); // answers
                noAns.add(cursor.getString(index));
                index = cursor.getColumnIndex("username"); // user
                user.add(cursor.getString(index) );
                index = cursor.getColumnIndex("pbLink"); // image
                pbLink.add(cursor.getString(index) );
                index = cursor.getColumnIndex("userID"); // UserID
                userId.add(cursor.getLong(index) );long integ = cursor.getLong(index);
                index = cursor.getColumnIndex("tweetID"); // tweetID
                tweetId.add(cursor.getLong(index) );
                size++;
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    public int getSize() {
        return size;
    }
    public long getUserID(int pos){return userId.get(pos);}
    public long getTweetId(int pos){return tweetId.get(pos);}
    public long getTime(int pos){return timeMillis.get(pos);}
    public String getUsername(int pos){return user.get(pos);}
    public String getTweet(int pos){return tweet.get(pos);}
    public String getRetweet(int pos){return noRT.get(pos);}
    public String getFavorite(int pos){return noFav.get(pos);}
    public String getDate(int pos){return timeToString(getTime(pos));}
    public String getAnswer(int pos){return noAns.get(pos);}
    public String getPbImg (int pos){return pbLink.get(pos);}
    public boolean loadImages(){
        return settings.getBoolean("image_load", false);
    }

    /**
     * Convert Time to String
     * @param mills Tweet Time
     * @return Formatted String
     */
    private String timeToString(long mills) {
        Calendar now = Calendar.getInstance();
        long diff = now.getTimeInMillis() - mills;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;

        if(weeks > 0)
            return "vor "+weeks+" w";
        if(days > 0)
            return "vor "+days+" d";
        if(hours > 0)
            return "vor "+hours+" h";
        if(minutes > 0)
            return "vor "+minutes+" m";
        else
            return "vor "+seconds+" s";
    }

    private void initArray() {
        user    = new ArrayList<>();
        tweet   = new ArrayList<>();
        noRT    = new ArrayList<>();
        noFav   = new ArrayList<>();
        noAns   = new ArrayList<>();
        userId  = new ArrayList<>();
        pbLink  = new ArrayList<>();
        tweetId = new ArrayList<>();
        timeMillis = new ArrayList<>();
    }
}