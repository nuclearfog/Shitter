package org.nuclearfog.twidda.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabase extends SQLiteOpenHelper
{
    private static final String userTable = "CREATE TABLE IF NOT EXISTS user ("+
            "userID INTEGER PRIMARY KEY, username VARCHAR(50),scrname VARCHAR(15)," +
            "pbLink TEXT,banner TEXT,bio TEXT,location TEXT,link TEXT,userregister INTEGER," +
            "createdAt INTEGER, following INTEGER, follower INTEGER);";

    private static final String tweetTable = "CREATE TABLE IF NOT EXISTS tweet ("+
            "tweetID INTEGER PRIMARY KEY, userID INTEGER, retweetID INTEGER, replyID INTEGER, retweeterID INTEGER,"+
            "replyname TEXT, replyUserID INTEGER, time INTEGER, tweet TEXT, media TEXT, retweet INTEGER, favorite INTEGER,"+
            "statusregister INTEGER, source VARCHAR(32), FOREIGN KEY (userID) REFERENCES user(userID));";

    private static final String favoriteTable = "CREATE TABLE IF NOT EXISTS favorit (" +
            "ownerID INTEGER, tweetID INTEGER PRIMARY KEY," +
            "FOREIGN KEY (ownerID) REFERENCES user(userID)," +
            "FOREIGN KEY (tweetID) REFERENCES tweet(tweetID));";

    private static final String trendTable = "CREATE TABLE IF NOT EXISTS trend (" +
            "woeID INTEGER, trendpos INTEGER, trendname TEXT, trendlink TEXT);";

    private static final String errorTable = "CREATE TABLE IF NOT EXISTS error (" +
            "time INTEGER PRIMARY KEY, message TEXT);";

    private static AppDatabase mData;

    private AppDatabase(Context context) {
        super(context, "database.db",null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(userTable);
        db.execSQL(tweetTable);

        db.execSQL(favoriteTable);

        db.execSQL(trendTable);
        db.execSQL(errorTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + "user");
        db.execSQL("DROP TABLE IF EXISTS " + "tweet");

        db.execSQL("DROP TABLE IF EXISTS " + "favorit");

        db.execSQL("DROP TABLE IF EXISTS " + "trend");
        db.execSQL("DROP TABLE IF EXISTS " + "error");
    }


    public static synchronized AppDatabase getInstance(Context context) {
        if (mData == null) {
            mData = new AppDatabase(context);
        }
        return mData;
    }
}