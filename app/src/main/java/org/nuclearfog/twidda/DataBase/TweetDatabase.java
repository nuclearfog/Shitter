package org.nuclearfog.twidda.DataBase;

import org.nuclearfog.twidda.DataBase.AppDatabase;
import org.nuclearfog.twidda.R;

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
    public static final int USER_TL = 1;

    private AppDatabase dataHelper;
    private List<String> user,tweet,noRT,noFav,noAns,pbLink;
    private List<Long> userId,tweetId,timeMillis;
    private List<Status> stats;
    private Context c;
    private int size = 0;
    private int mode = 0;
    private SharedPreferences settings;

    /**
     * Store & Read Data
     * @param stats   Twitter Home
     */
    public TweetDatabase(List<Status> stats, Context c, int mode) {
        this.stats=stats;
        this.c=c;
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
    public TweetDatabase(Context c, int mode) {
        this.c=c;
        this.mode=mode;
        dataHelper = AppDatabase.getInstance(c);
        settings = c.getSharedPreferences("settings", 0);
        initArray();
        load();
    }

    private void store() {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        ContentValues usr = new ContentValues();
        ContentValues tl  = new ContentValues();
        String tweetTable;

        if(mode==HOME_TL)
            tweetTable = "tweet";  //Switch between Timeline and Home Tweets
        else
            tweetTable = "hometweet";

        for(int pos = 0; pos < stats.size(); pos++) {
            // USER
            Status stat = stats.get(pos);
            User user = stat.getUser();

            usr.put("userID",user.getId());
            usr.put("username", user.getName());
            usr.put("pbLink", user.getProfileImageURL());
            // TWEET
            tl.put("userID", user.getId());
            tl.put("tweetID", stat.getId());
            tl.put("time", stat.getCreatedAt().getTime());
            tl.put("tweet", stat.getText());
            tl.put("retweet", stat.getRetweetCount());
            tl.put("favorite", stat.getFavoriteCount());
            db.insertWithOnConflict("user",null, usr,SQLiteDatabase.CONFLICT_IGNORE);
            db.insertWithOnConflict(tweetTable,null, tl,SQLiteDatabase.CONFLICT_REPLACE);
        }
        db.close();
    }

    private void load() {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        int index;
        size = 0;
        String SQL_GET_HOME;

        if(mode==HOME_TL)
            SQL_GET_HOME = c.getString(R.string.SQL_HOME_TL); // Home Tineline
        else
            SQL_GET_HOME = c.getString(R.string.SQL_USER_TL); // User Timeline

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
                noAns.add( cursor.getString(index) );
                index = cursor.getColumnIndex("username"); // user
                user.add(cursor.getString(index) );
                index = cursor.getColumnIndex("pbLink"); // image
                pbLink.add(cursor.getString(index) );
                index = cursor.getColumnIndex("userID"); // UserID
                userId.add(cursor.getLong(index) );
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