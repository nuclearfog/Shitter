package org.nuclearfog.twidda.engine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.nuclearfog.twidda.DataBase.AppDatabase;
import org.nuclearfog.twidda.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import twitter4j.Status;
import twitter4j.User;

public class TweetDatabase
{
    private AppDatabase dataHelper;
    private List<String> user,tweet,noRT,noFav,noAns,date, pbLink;
    private List<Long> userId, tweetId;
    private List<Status> stats;
    private Context c;
    private Date now;
    private int size = 0;

    /**
     * Store Data
     * @param stats   Twitter Home
     */
    public TweetDatabase(List<Status> stats, Context c) {
        this.stats=stats;
        dataHelper = AppDatabase.getInstance(c);
        initArray();
        fillArray();
        store();
        this.c=c;
    }

    /**
     * Read Data
     * @param c MainActivity Context
     */
    public TweetDatabase(Context c) {
        dataHelper = AppDatabase.getInstance(c);
        initArray();
        load();
        this.c=c;
    }

    private void store() {
        SQLiteDatabase data = dataHelper.getWritableDatabase();
        ContentValues usr = new ContentValues();
        ContentValues tl  = new ContentValues();

        Status stat;
        User user;
        for(int pos = 0;pos < stats.size(); pos++) {
            // USER
            usr.put("userID", getUserID(pos));
            usr.put("username", getUsername(pos));
            usr.put("pbLink", getPbImg(pos));
            // TWEET
            tl.put("userID", getUserID(pos));
            tl.put("tweetID", getTweetId(pos));
            tl.put("time", getDate(pos));
            tl.put("tweet", getTweet(pos));
            tl.put("retweet", getRetweet(pos));
            tl.put("favorite", getFavorite(pos));
        }
        data.insert("user",null, usr);
        data.insert("tweet",null, tl);


    }

    private void load() {
        SQLiteDatabase data = dataHelper.getReadableDatabase();//TODO


        Cursor cursor = data.rawQuery("SELECT * FROM tweet INNER JOIN user ON user.userID = tweet.userID",null);
        cursor.moveToFirst();
        while( cursor.moveToNext() ) {
            int index;
            index = cursor.getColumnIndex("time"); // time
            date.add( cursor.getString(index) );

            index = cursor.getColumnIndex("tweet"); // tweet
            tweet.add( cursor.getString(index) );

            index = cursor.getColumnIndex("retweet"); // retweet
            noRT.add( cursor.getString(index) );

            index = cursor.getColumnIndex("favorite"); // favorite
            noFav.add( cursor.getString(index) );

            index = cursor.getColumnIndex("answers"); // answers
            noAns.add( cursor.getString(index) );

            index = cursor.getColumnIndex("answers"); // user
            user.add(cursor.getString(index) );

            index = cursor.getColumnIndex("pbLink"); // user
            pbLink.add(cursor.getString(index) );

            size++;
        }
        data.close();
    }

    /**
     * Save Data
     * @param usr Username
     * @param tw Tweet
     * @param rt Number Retweets
     * @param fav Number Favorites
     * @param time Tweet Time
     * @param ans Number Answers
     */
    public void setTweet(String usr,String tw,String rt,String fav,String time,String ans) {
        user.add(usr);
        tweet.add(tw);
        noRT.add(rt);
        noFav.add(fav);
        noAns.add(ans);
        date.add(time);
    }

    public long getUserID(int pos){return userId.get(pos);}
    public long getTweetId(int pos){return tweetId.get(pos);}
    public String getUsername(int pos){return user.get(pos);}
    public String getTweet(int pos){return tweet.get(pos);}
    public String getRetweet(int pos){return noRT.get(pos);}
    public String getFavorite(int pos){return noFav.get(pos);}
    public String getDate(int pos){return date.get(pos);}
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

    private void initArray() {
        now    = new Date();
        user   = new ArrayList<>();
        tweet  = new ArrayList<>();
        noRT   = new ArrayList<>();
        noFav  = new ArrayList<>();
        noAns  = new ArrayList<>();
        date   = new ArrayList<>();
        userId = new ArrayList<>();
        pbLink = new ArrayList<>();
        tweetId= new ArrayList<>();
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
            date.add( getTweetTime(stat.getCreatedAt()) );
            String name = stat.getUser().getScreenName();
            String twUsr = stat.getUser().getName();
            user.add( name +" @"+twUsr );
            userId.add(stat.getUser().getId());
            pbLink.add(stat.getUser().getProfileImageURL());
            tweetId.add(stat.getId());
        }
    }
}