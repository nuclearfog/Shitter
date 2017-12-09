package org.nuclearfog.twidda.engine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.nuclearfog.twidda.DataBase.AppDatabase;
import org.nuclearfog.twidda.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import twitter4j.Status;
import twitter4j.User;

public class TweetDatabase
{
    private AppDatabase dataHelper;
    private List<String> user,tweet,noRT,noFav,noAns, pbLink;
    private List<Long> userId, tweetId;
    private List<Date> newDate;
    private List<Status> stats;
    private Context c;
    private int size = 0;

    /**
     * Store Data
     * @param stats   Twitter Home
     */
    public TweetDatabase(List<Status> stats, Context c) {
        this.stats=stats;
        this.c=c;
        dataHelper = AppDatabase.getInstance(c);
        initArray();
        fillArray();
        store();
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
        Status stat;
        User user;
        for(int pos = 0; pos < getSize(); pos++) {
            // USER
            usr.put("userID", getUserID(pos));
            usr.put("username", getUsername(pos));
            usr.put("pbLink", getPbImg(pos));
            // TWEET
            tl.put("userID", getUserID(pos));
            tl.put("tweetID", getTweetId(pos));
            tl.put("time", dateToLong(newDate.get(pos)));
            tl.put("tweet", getTweet(pos));
            tl.put("retweet", getRetweet(pos));
            tl.put("favorite", getFavorite(pos));
            db.insertWithOnConflict("user",null, usr,SQLiteDatabase.CONFLICT_REPLACE);
            db.insertWithOnConflict("tweet",null, tl,SQLiteDatabase.CONFLICT_REPLACE);
        }
        db.close();
    }

    private void load() {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        int index;
        String SQL_GET_HOME = c.getString(R.string.SQL_HOME_TL);
        Cursor cursor = db.rawQuery(SQL_GET_HOME,null);
        cursor.moveToFirst();
        if(cursor.moveToFirst()) {
            do {
                index = cursor.getColumnIndex("time"); // time
                newDate.add(longToDate(cursor.getLong(index)));
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

    public long getUserID(int pos){return userId.get(pos);}
    public long getTweetId(int pos){return tweetId.get(pos);}
    public String getUsername(int pos){return user.get(pos);}
    public String getTweet(int pos){return tweet.get(pos);}
    public String getRetweet(int pos){return noRT.get(pos);}
    public String getFavorite(int pos){return noFav.get(pos);}
    public String getDate(int pos){return getTweetTime(newDate.get(pos));}
    public String getAnswer(int pos){return noAns.get(pos);}
    public String getPbImg (int pos){return pbLink.get(pos);}

    public int getSize() {
        if(stats != null) {
            return stats.size();
        } else {
            return size;
        }
    }

    private String getTweetTime(Date time) {
        Date now = new Date();
        int tweetHour = now.getHours() - time.getHours();
        int tweetMin  = now.getMinutes() - time.getMinutes();
        int tweetSec  = now.getSeconds() - time.getSeconds();
        if (tweetHour > 0)
            return "vor "+tweetHour+" h";
        else if(tweetMin > 0)
            return "vor "+tweetMin+" min";
        else
            return "vor "+tweetSec+" sec";
    }

    private Date longToDate(long mills) {
        return new Date(mills);
    }
    private long dateToLong(Date d) {
        return d.getTime();
    }

    private void initArray() {
        user   = new ArrayList<>();
        tweet  = new ArrayList<>();
        noRT   = new ArrayList<>();
        noFav  = new ArrayList<>();
        noAns  = new ArrayList<>();
        userId = new ArrayList<>();
        pbLink = new ArrayList<>();
        tweetId= new ArrayList<>();
        newDate= new ArrayList<>();
    }

    private void fillArray() {
        Status stat;
        for(short pos = 0; pos < getSize(); pos++) {
            stat = stats.get(pos);
            stat.getId();
            tweet.add( stat.getText() );
            noAns.add("test");
            noRT.add( Integer.toString(stat.getRetweetCount()) );
            noFav.add( Integer.toString(stat.getFavoriteCount()) );
            newDate.add( stat.getCreatedAt() );
            String name = stat.getUser().getScreenName();
            String twUsr = stat.getUser().getName();
            user.add( name +" @"+twUsr );
            userId.add(stat.getUser().getId());
            pbLink.add(stat.getUser().getProfileImageURL());
            tweetId.add(stat.getId());
        }
    }
}