package org.nuclearfog.twidda.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.nuclearfog.twidda.BuildConfig;

import java.io.File;

/**
 * Memory leak save version of SQLiteOpenHelper
 */
public class Database {

    private static final String DB_NAME = "database.db";

    private static final String TABLE_USER = "CREATE TABLE IF NOT EXISTS user (" +
            "userID INTEGER PRIMARY KEY,username VARCHAR(50),scrname VARCHAR(15)," +
            "pbLink TEXT,banner TEXT,bio TEXT,location TEXT,link TEXT,userregister INTEGER," +
            "createdAt INTEGER,following INTEGER,follower INTEGER,tweetCount INTEGER,favorCount INTEGER);";

    private static final String TABLE_TWEET = "CREATE TABLE IF NOT EXISTS tweet (" +
            "tweetID INTEGER PRIMARY KEY,userID INTEGER,retweetID INTEGER,replyID INTEGER,retweeterID INTEGER," +
            "replyname TEXT,replyUserID INTEGER,time INTEGER,tweet TEXT,media TEXT,retweet INTEGER,favorite INTEGER," +
            "statusregister INTEGER,source VARCHAR(32),FOREIGN KEY (userID) REFERENCES user(userID));";

    private static final String TABLE_FAVORS = "CREATE TABLE IF NOT EXISTS favorit (" +
            "ownerID INTEGER,tweetID INTEGER," +
            "FOREIGN KEY (ownerID) REFERENCES user(userID)," +
            "FOREIGN KEY (tweetID) REFERENCES tweet(tweetID));";

    private static final String TABLE_TRENDS = "CREATE TABLE IF NOT EXISTS trend (" +
            "woeID INTEGER,trendpos INTEGER,trendname TEXT,trendlink TEXT);";

    private static final String TABLE_MESSAGES = "CREATE TABLE IF NOT EXISTS message (" +
            "messageID INTEGER PRIMARY KEY,time INTEGER,senderID INTEGER,receiverID INTEGER," +
            "message TEXT);";

    private static final String INDX_TWEET = "CREATE INDEX IF NOT EXISTS idx_tweet ON tweet(userID,statusregister);";
    private static final String INDX_FAVOR = "CREATE INDEX IF NOT EXISTS idx_favor ON favorit(ownerID,tweetID);";
    private static final String INDX_TREND = "CREATE INDEX IF NOT EXISTS idx_trend ON trend(woeID);";

    private static Database instance;

    private final File databasePath;

    private SQLiteDatabase db;


    private Database(Context context) {
        databasePath = context.getDatabasePath(DB_NAME);
        db = SQLiteDatabase.openOrCreateDatabase(databasePath, null);

        db.execSQL(TABLE_USER);
        db.execSQL(TABLE_TWEET);
        db.execSQL(TABLE_FAVORS);
        db.execSQL(TABLE_TRENDS);
        db.execSQL(TABLE_MESSAGES);
        db.execSQL(INDX_TWEET);
        db.execSQL(INDX_FAVOR);
        db.execSQL(INDX_TREND);
    }


    public synchronized SQLiteDatabase getDatabase() {
        if (BuildConfig.DEBUG && db.isDbLockedByCurrentThread())
            throw new AssertionError("DB locked!");
        if (!db.isOpen())
            db = SQLiteDatabase.openOrCreateDatabase(databasePath, null);
        return db;
    }


    public static Database getInstance(Context context) {
        if (instance == null)
            instance = new Database(context);
        return instance;
    }


    public static void deleteDatabase(Context c) {
        SQLiteDatabase.deleteDatabase(c.getDatabasePath(DB_NAME));
        instance = null;
    }
}