package org.nuclearfog.twidda.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import twitter4j.Status;
import twitter4j.User;

public class TweetDatabase {
    public static final int HOME_TL   = 0;    // GET HOME TIMELINE
    public static final int FAV_TL    = 1;    // GET FAVORITE TL
    public static final int USER_TL   = 2;    // GET USERS TWEET TL @userID
    public static final int GET_TWEET = 3;    // GET TWEET @ userID
    public static final int GET_MENT  = 4;    // GET MENTION TL

    private AppDatabase dataHelper;
    private List<String> user,scrname,tweet,pbLink;
    private List<Long> userId,tweetId,timeMillis;
    private List<Integer> noRT,noFav,noAns;
    private SharedPreferences settings;
    private boolean toggleImg;
    private int size = 0;
    private int mode = 0;
    private int limit;
    private long CurrentId = 0;

    /**
     * Store & Read Data
     * @param stats Twitter Status
     * @param context Current Activity's Context
     * @param mode which type of data should be stored
     * @param CurrentId current User ID
     * @see #HOME_TL#FAV_TL#USER_TL
     */
    public TweetDatabase(List<Status> stats, Context context, final int mode,long CurrentId) {
        this.CurrentId = CurrentId;
        this.mode = mode;
        initialize(context);
        store(stats);
        load();
    }

    /**
     * Read Data
     * @param context MainActivity Context
     * @param mode which type of data should be loaded
     * @param CurrentId current ID (USER OR TWEET)
     */
    public TweetDatabase(Context context, final int mode, long CurrentId) {
        this.CurrentId = CurrentId;
        this.mode = mode;
        initialize(context);
        load();
    }

    /**
     * this Constructor is used by twitter search
     * no need to store in SQLITE
     * @param stats SearchWindow Result Tweets
     */
    public TweetDatabase(List<Status> stats, Context context) {
        initialize(context);
        insert(stats);
    }

    /**
     * Add new Elements to the Lists and store into Database
     * @param stats List of Tweets
     */
    public void add(List<Status> stats) {
        store(stats);
        insertNew(stats);
    }

    /**
     * Add new Elements without storing
     * @param stats list of Tweets
     */
    public void addHot(List<Status> stats) {
        insertNew(stats);
    }

    private void store(List<Status> stats) {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        ContentValues user  = new ContentValues();
        ContentValues tweet = new ContentValues();
        ContentValues home  = new ContentValues();
        ContentValues fav   = new ContentValues();
        ContentValues ment  = new ContentValues();

        for(int pos = 0; pos < stats.size(); pos++) {
            Status stat = stats.get(pos);
            User usr = stat.getUser();

            user.put("userID",usr.getId());
            user.put("username", usr.getName());
            user.put("scrname",'@'+usr.getScreenName());
            user.put("pbLink", usr.getMiniProfileImageURL());
            user.put("banner", usr.getProfileBannerURL());
            user.put("bio",usr.getDescription());
            user.put("location",usr.getLocation());
            user.put("link",usr.getURL());

            tweet.put("userID", usr.getId());
            tweet.put("tweetID", stat.getId());
            tweet.put("time", stat.getCreatedAt().getTime());
            tweet.put("tweet", stat.getText());
            tweet.put("retweet", stat.getRetweetCount());
            tweet.put("favorite", stat.getFavoriteCount());
            tweet.put("answers", 0);

            home.put("tweetID", stat.getId());
            fav.put("tweetID", stat.getId());
            fav.put("ownerID", CurrentId);

            ment.put("mTweetID",stat.getId());

            db.insertWithOnConflict("user",null, user,SQLiteDatabase.CONFLICT_IGNORE);
            db.insertWithOnConflict("tweet",null, tweet,SQLiteDatabase.CONFLICT_REPLACE);

            if(mode!=USER_TL) {
                if(mode == HOME_TL) {
                    db.insertWithOnConflict("timeline",null,home,SQLiteDatabase.CONFLICT_REPLACE);
                } else if(mode == FAV_TL) {
                    db.insertWithOnConflict("favorit",null,fav,SQLiteDatabase.CONFLICT_REPLACE);
                } else if(mode == GET_MENT) {
                    db.insertWithOnConflict("timeline",null,ment,SQLiteDatabase.CONFLICT_IGNORE);
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
                    "WHERE favorit.ownerID = "+CurrentId+" ORDER BY tweet.time DESC";
        } else if(mode==USER_TL) {
            SQL_GET_HOME = "SELECT * FROM user INNER JOIN tweet ON user.userID = tweet.userID " +
                    "WHERE user.userID = "+CurrentId+" ORDER BY tweet.time DESC";
        } else if(mode==GET_TWEET) {
            SQL_GET_HOME = "SELECT * FROM user INNER JOIN tweet ON user.userID = tweet.userID " +
                    "WHERE tweet.tweetID = "+CurrentId+" ORDER BY tweet.time DESC";
        } else if(mode==GET_MENT){
            SQL_GET_HOME = "SELECT * FROM timeline " +
                    "INNER JOIN tweet ON timeline.mTweetID = tweet.tweetID " +
                    "INNER JOIN user ON tweet.userID=user.userID ORDER BY time DESC";
            limit = 5; //TODO 5 Mentions only!
        }

        Cursor cursor = db.rawQuery(SQL_GET_HOME,null);

        if(cursor.moveToFirst()) {
            do {
                index = cursor.getColumnIndex("time"); // time
                timeMillis.add(cursor.getLong(index));
                index = cursor.getColumnIndex("tweet"); // tweet
                tweet.add( cursor.getString(index) );
                index = cursor.getColumnIndex("retweet"); // retweet
                noRT.add( cursor.getInt(index) );
                index = cursor.getColumnIndex("favorite"); // fav
                noFav.add( cursor.getInt(index) );
                index = cursor.getColumnIndex("answers"); // answers
                noAns.add(cursor.getInt(index));
                index = cursor.getColumnIndex("username"); // user
                user.add(cursor.getString(index) );
                index = cursor.getColumnIndex("scrname"); // username
                scrname.add(cursor.getString(index) );
                index = cursor.getColumnIndex("pbLink"); // image
                pbLink.add(cursor.getString(index) );
                index = cursor.getColumnIndex("userID"); // UserID
                userId.add(cursor.getLong(index) );
                index = cursor.getColumnIndex("tweetID"); // tweetID
                tweetId.add(cursor.getLong(index) );
                size++;
            } while(cursor.moveToNext()  && size < limit);
        }
        cursor.close();
        db.close();
    }

    public int getSize() {
        return size;
    }
    public int getRetweet(int pos){return noRT.get(pos);}
    public int getFavorite(int pos){return noFav.get(pos);}
    public int getAnswer(int pos){return noAns.get(pos);}
    public long getUserID(int pos){return userId.get(pos);}
    public long getTweetId(int pos){return tweetId.get(pos);}
    public long getTime(int pos){return timeMillis.get(pos);}
    public String getUsername(int pos){return user.get(pos);}
    public String getScreenname(int pos){return scrname.get(pos);}
    public String getTweet(int pos){return tweet.get(pos);}
    public String getDate(int pos){return timeToString(getTime(pos));}
    public String getPbLink (int pos){return pbLink.get(pos);}
    public boolean loadImages(){return toggleImg;}

    /**
     * Convert Time to String
     * @param mills TweetDetail Time
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

        if(weeks > 4) {
            Date tweetDate = new Date(mills);
            return new SimpleDateFormat("dd.MM.yyyy").format(tweetDate);
        }
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

    private void initialize(Context c) {
        dataHelper = AppDatabase.getInstance(c);
        settings = c.getSharedPreferences("settings", 0);
        limit = settings.getInt("limit", 100);
        toggleImg = settings.getBoolean("image_load", false);
        initArray();
    }

    private void insert(List<Status> stats) {
        for(Status stat: stats) {
            User usr = stat.getUser();
            user.add(usr.getName());
            scrname.add('@'+usr.getScreenName());
            tweet.add(stat.getText());
            noRT.add(stat.getRetweetCount());
            noFav.add(stat.getFavoriteCount());
            noAns.add(0); // TODO
            userId.add(usr.getId());
            pbLink.add(usr.getMiniProfileImageURL());
            tweetId.add(stat.getId());
            timeMillis.add(stat.getCreatedAt().getTime());
            size++;
        }
    }

    private void insertNew(List<Status> stats) {
        for(int index = stats.size()-1 ; index >=0 ; index--) {
            Status stat = stats.get(index);
            User usr = stat.getUser();
            user.add(0,usr.getName());
            scrname.add(0,'@'+usr.getScreenName());
            tweet.add(0,stat.getText());
            noRT.add(0,stat.getRetweetCount());
            noFav.add(0,stat.getFavoriteCount());
            noAns.add(0,0); // TODO
            userId.add(0,usr.getId());
            pbLink.add(0,usr.getMiniProfileImageURL());
            tweetId.add(0,stat.getId());
            timeMillis.add(0,stat.getCreatedAt().getTime());
            size++;
        }
    }

    private void initArray() {
        user    = new ArrayList<>();
        scrname = new ArrayList<>();
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