package org.nuclearfog.twidda.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabase extends SQLiteOpenHelper
{
    private static final String uQuery = "CREATE TABLE IF NOT EXISTS user ("+
            "userID INTEGER PRIMARY KEY, username TEXT," +
            "scrname  TEXT, pbLink TEXT, banner TEXT, bio TEXT,"+
            "location TEXT, link TEXT);";

    private static final String tQuery = "CREATE TABLE IF NOT EXISTS tweet (" +
            "tweetID INTEGER PRIMARY KEY, userID INTEGER," +
            "time INTEGER, tweet TEXT, retweet INTEGER, favorite INTEGER," +
            "answers INTEGER, FOREIGN KEY (userID) REFERENCES user(userID));";

    private static final String trQuery = "CREATE TABLE IF NOT EXISTS trend (" +
            "trendpos INTEGER PRIMARY KEY, trendname TEXT, trendlink TEXT);";

    private static final String hQuery = "CREATE TABLE IF NOT EXISTS timeline (" +
            "tweetID INTEGER UNIQUE, mTweetID INTEGER UNIQUE," +
            "FOREIGN KEY (tweetID) REFERENCES tweet(tweetID));" +
            "FOREIGN KEY (mTweetID) REFERENCES tweet(tweetID));";

    private static final String fQuery = "CREATE TABLE IF NOT EXISTS favorit (" +
            "ownerID INTEGER, tweetID INTEGER UNIQUE," +
            "FOREIGN KEY (ownerID) REFERENCES user(userID)," +
            "FOREIGN KEY (tweetID) REFERENCES tweet(tweetID));";

    private static AppDatabase mData;

    private AppDatabase(Context context) {
        super(context, "database.db",null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(uQuery);
        db.execSQL(tQuery);
        db.execSQL(trQuery);
        db.execSQL(hQuery);
        db.execSQL(fQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + "user");
        db.execSQL("DROP TABLE IF EXISTS " + "tweet");
        db.execSQL("DROP TABLE IF EXISTS " + "favorit");
        db.execSQL("DROP TABLE IF EXISTS " + "timeline");
        db.execSQL("DROP TABLE IF EXISTS " + "trend");
        onCreate(db);
    }

    /**
     * Singleton Method
     * @param context Application Context
     * @return mData Object of this class
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (mData == null) {
            mData = new AppDatabase(context);
        }
        return mData;
    }
}