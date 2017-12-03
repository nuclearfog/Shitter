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

public class TweetDatabase {
    private Date now;
    private List<String> user,tweet,noRT,noFav,noAns,date;
    private AppDatabase dataHelper;
    private List<Status> stats;
    private Context c;

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
        initArray();
        dataHelper = AppDatabase.getInstance(c);
        load();
        this.c=c;

    }

    private void store() {
        SQLiteDatabase data = dataHelper.getWritableDatabase();
        ContentValues usr = new ContentValues();
        ContentValues tl  = new ContentValues();

        Status stat;
        for(int pos = 0;pos < stats.size(); pos++)
        {
            stat = stats.get(pos);

            // USER
            usr.put("userID",stat.getUser().getId() );
            usr.put("username", stat.getUser().getScreenName());
            usr.put("pbLink",stat.getUser().getProfileImageURL());

            // TWEET
            tl.put("userID",stat.getUser().getId() );
            tl.put("tweetID", stat.getId());
            tl.put("time", stat.getCreatedAt().toString());
            tl.put("tweet", stat.getText());
            tl.put("retweet", stat.getRetweetCount());
            tl.put("favorite", stat.getFavoriteCount());
            tl.put("user", stat.getUser().getId());
        }
        data.insert("user",null, usr);
        data.insert("tweet",null, tl);
    }

    private void load() {
        SQLiteDatabase data = dataHelper.getReadableDatabase();
        String col_name[]= new String[] {c.getString(R.string.table_column)};
        //Cursor tw = data.query("tweet_table", col_name,  ); //TODO




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

    public String getUsername(int pos){return user.get(pos);}
    public String getTweet(int pos){return tweet.get(pos);}
    public String getRetweet(int pos){return noRT.get(pos);}
    public String getFavorite(int pos){return noFav.get(pos);}
    public String getDate(int pos){return date.get(pos);}
    public String getAnswer(int pos){return noAns.get(pos);}
    public int getSize(){return user.size();}

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
        now   = new Date();
        user  = new ArrayList<>();
        tweet = new ArrayList<>();
        noRT  = new ArrayList<>();
        noFav = new ArrayList<>();
        noAns = new ArrayList<>();
        date  = new ArrayList<>();
    }

    private void fillArray() {
        Status stat;
        for(short pos = 0; pos < getSize(); pos++) {
            stat = stats.get(pos);
            tweet.add( stat.getText() );
            noRT.add( Integer.toString(stat.getRetweetCount()) );
            noFav.add( Integer.toString(stat.getFavoriteCount()) );
            date.add( getTweetTime(stat.getCreatedAt()) );
            String name = stat.getUser().getScreenName();
            String twUsr = stat.getUser().getName();
            user.add( name +" @"+twUsr );
        }
    }
}