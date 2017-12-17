package org.nuclearfog.twidda.engine;

import org.nuclearfog.twidda.DataBase.AppDatabase;
import org.nuclearfog.twidda.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import twitter4j.Status;

public class TweetDatabase {
    private AppDatabase dataHelper;
    private List<String> user,tweet,noRT,noFav,noAns,pbLink;
    private List<Long> userId,tweetId,timeMillis;
    private List<Status> stats;
    private Context c;
    private int size = 0;

    /**
     * Store & Read Data
     * @param stats   Twitter Home
     */
    public TweetDatabase(List<Status> stats, Context c) {
        this.stats=stats;
        this.c=c;
        dataHelper = AppDatabase.getInstance(c);
        initArray();
        store();
        load();
    }

    /**
     * Read Data
     * @param c MainActivity Context
     */
    public TweetDatabase(Context c) {
        this.c=c;
        dataHelper = AppDatabase.getInstance(c);
        initArray();
        load();
    }

    private void store() {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        ContentValues usr = new ContentValues();
        ContentValues tl  = new ContentValues();

        for(int pos = 0; pos < getSize(); pos++) {
            // USER
            usr.put("userID", getUserID(pos));
            usr.put("username", getUsername(pos));
            usr.put("pbLink", getPbImg(pos));
            // TWEET
            tl.put("userID", getUserID(pos));
            tl.put("tweetID", getTweetId(pos));
            tl.put("time", getTime(pos));
            tl.put("tweet", getTweet(pos));
            tl.put("retweet", getRetweet(pos));
            tl.put("favorite", getFavorite(pos));
            db.insertWithOnConflict("user",null, usr,SQLiteDatabase.CONFLICT_IGNORE);
            db.insertWithOnConflict("tweet",null, tl,SQLiteDatabase.CONFLICT_REPLACE);
        }
        db.close();
    }

    private void load() {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        int index;
        String SQL_GET_HOME = c.getString(R.string.SQL_HOME_TL);
        Cursor cursor = db.rawQuery(SQL_GET_HOME,null);

        if(cursor.moveToFirst()) {
            do {
                index = cursor.getColumnIndex("time"); // time
                timeMillis.add(cursor.getLong(index));
                index = cursor.getColumnIndex("tweet"); // tweet
                tweet.add( cursor.getString(index) );
                index = cursor.getColumnIndex("retweet"); // retweet
                noRT.add( cursor.getString(index) );
                index = cursor.getColumnIndex("favorite"); // favorite
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
        if(stats != null) {
            return stats.size();
        } else {
            return size;
        }
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