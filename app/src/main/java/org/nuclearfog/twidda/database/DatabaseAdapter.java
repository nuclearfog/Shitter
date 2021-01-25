package org.nuclearfog.twidda.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.io.File;

import static android.content.Context.MODE_PRIVATE;

/**
 * This class creates and manages SQLite table versions
 *
 * @author nuclearfog
 */
public class DatabaseAdapter {

    private static final int LATEST_VERSION = 3;
    private static final String DB_NAME = "database.db";

    /**
     * SQL query to create a table for user information
     */
    private static final String TABLE_USER = "CREATE TABLE IF NOT EXISTS user (" +
            "userID INTEGER PRIMARY KEY,username TEXT,scrname TEXT," +
            "pbLink TEXT,banner TEXT,bio TEXT,location TEXT,link TEXT,userregister INTEGER," +
            "createdAt INTEGER,following INTEGER,follower INTEGER,tweetCount INTEGER,favorCount INTEGER);";

    /**
     * SQL query to create a table for tweet information
     */
    private static final String TABLE_TWEET = "CREATE TABLE IF NOT EXISTS tweet (" +
            "tweetID INTEGER PRIMARY KEY,userID INTEGER,retweetID INTEGER,replyID INTEGER,retweeterID INTEGER," +
            "replyname TEXT,replyUserID INTEGER,time INTEGER,tweet TEXT,media TEXT,retweet INTEGER,favorite INTEGER," +
            "statusregister INTEGER,source TEXT,place TEXT,geo TEXT,FOREIGN KEY (userID) REFERENCES user(userID));";

    /**
     * SQL query to create a table for favorite tweets
     */
    private static final String TABLE_FAVORS = "CREATE TABLE IF NOT EXISTS favorit (" +
            "ownerID INTEGER,tweetID INTEGER," +
            "FOREIGN KEY (ownerID) REFERENCES user(userID)," +
            "FOREIGN KEY (tweetID) REFERENCES tweet(tweetID));";

    /**
     * SQL query to create a table for trend information
     */
    private static final String TABLE_TRENDS = "CREATE TABLE IF NOT EXISTS trend (" +
            "woeID INTEGER,trendpos INTEGER,vol INTEGER,trendname TEXT);";

    /**
     * SQL query to create a table for message information
     */
    private static final String TABLE_MESSAGES = "CREATE TABLE IF NOT EXISTS message (" +
            "messageID INTEGER PRIMARY KEY,time INTEGER,senderID INTEGER,receiverID INTEGER," +
            "message TEXT);";

    /**
     * index for tweet table
     */
    private static final String INDX_TWEET = "CREATE INDEX IF NOT EXISTS idx_tweet ON tweet(userID,statusregister);";

    /**
     * index for favorite table
     */
    private static final String INDX_FAVOR = "CREATE INDEX IF NOT EXISTS idx_favor ON favorit(ownerID,tweetID);";

    /**
     * index for trend table
     */
    private static final String INDX_TREND = "CREATE INDEX IF NOT EXISTS idx_trend ON trend(woeID);";

    //update for trend table
    private static final String TABLE_TWEET_ADD_PLACE = "ALTER TABLE tweet ADD COLUMN place TEXT";
    private static final String TABLE_TWEET_ADD_GEO = "ALTER TABLE tweet ADD COLUMN geo TEXT";
    private static final String TABLE_TREND_ADD_VOL = "ALTER TABLE trend ADD COLUMN vol INTEGER";

    /**
     * SQL query to get home timeline tweets
     */
    static final String HOMETL_QUERY = "SELECT * FROM tweet INNER JOIN user ON tweet.userID=user.userID " +
            "WHERE statusregister&? IS NOT 0 ORDER BY tweetID DESC LIMIT ?";

    /**
     * SQL query to get mention timeline
     */
    static final String MENTION_QUERY = "SELECT * FROM tweet INNER JOIN user ON tweet.userID=user.userID " +
            "WHERE statusregister&? IS NOT 0 AND userregister&? IS 0 ORDER BY tweetID DESC LIMIT ?";

    /**
     * SQL query to get tweets of an user
     */
    static final String USERTWEET_QUERY = "SELECT * FROM tweet INNER JOIN user ON tweet.userID=user.userID " +
            "WHERE statusregister&? IS NOT 0 AND user.userID=? ORDER BY tweetID DESC LIMIT ?";

    /**
     * SQL query to get tweets favorited by an user
     */
    static final String USERFAVORIT_QUERY = "SELECT * FROM tweet INNER JOIN favorit on tweet.tweetID=favorit.tweetID " +
            "INNER JOIN user ON tweet.userID=user.userID WHERE favorit.ownerID=? ORDER BY tweetID DESC LIMIT ?";

    /**
     * SQL query to get a single tweet specified by an ID
     */
    static final String SINGLE_TWEET_QUERY = "SELECT * FROM tweet INNER JOIN user ON user.userID = tweet.userID WHERE tweet.tweetID=? LIMIT 1";

    /**
     * SQL query to get replies of a tweet specified by a reply ID
     */
    static final String ANSWER_QUERY = "SELECT * FROM tweet INNER JOIN user ON tweet.userID=user.userID " +
            "WHERE tweet.replyID=? AND statusregister&? IS NOT 0 AND userregister&? IS 0 ORDER BY tweetID DESC LIMIT ?";

    /**
     * SQL query to get locale based trends
     */
    static final String TREND_QUERY = "SELECT * FROM trend WHERE woeID=? ORDER BY trendpos ASC";

    /**
     * SQL query to get direct messages
     */
    static final String MESSAGE_QUERY = "SELECT * FROM message ORDER BY messageID DESC LIMIT ?";

    /**
     * SQL query to get user information
     */
    static final String USER_QUERY = "SELECT * FROM user WHERE userID=? LIMIT 1";

    /**
     * SQL query to get a status register for a tweet
     */
    static final String TWEETFLAG_QUERY = "SELECT statusregister FROM tweet WHERE tweetID=? LIMIT 1;";

    /**
     * SQL query to get a status register of an user
     */
    static final String USERFLAG_QUERY = "SELECT userregister FROM user WHERE userID=? LIMIT 1;";

    /**
     * SQL query to check if a status exists in database
     */
    static final String STATUS_EXIST_QUERY = "SELECT tweetID FROM tweet WHERE tweetID=? LIMIT 1;";

    private static DatabaseAdapter instance;

    private final File databasePath;

    private SQLiteDatabase db;


    private DatabaseAdapter(Context context) {
        databasePath = context.getDatabasePath(DB_NAME);
        db = context.openOrCreateDatabase(databasePath.toString(), MODE_PRIVATE, null);
        initTables();
        updateTable();
    }

    /**
     * get database instance
     *
     * @return SQLite database
     */
    public synchronized SQLiteDatabase getDatabase() {
        // TODO add Multithreading safety
        if (!db.isOpen())
            db = SQLiteDatabase.openOrCreateDatabase(databasePath, null);
        return db;
    }

    /**
     * get database adapter instance
     *
     * @param context application context
     * @return database instance
     */
    public static DatabaseAdapter getInstance(@NonNull Context context) {
        if (instance == null)
            instance = new DatabaseAdapter(context);
        return instance;
    }

    /**
     * delete database and destroy instance
     *
     * @param c application context
     */
    public static void deleteDatabase(Context c) {
        SQLiteDatabase.deleteDatabase(c.getDatabasePath(DB_NAME));
        instance = null;
    }

    /**
     * update old table versions if necessary
     */
    private void updateTable() {
        if (db.getVersion() < 2) {
            db.execSQL(TABLE_TWEET_ADD_PLACE);
            db.execSQL(TABLE_TWEET_ADD_GEO);
            db.setVersion(2);
        }
        if (db.getVersion() < 3) {
            db.execSQL(TABLE_TREND_ADD_VOL);
            db.setVersion(3);
        }
    }

    /**
     * initialize tables if there aren't any
     */
    private void initTables() {
        db.execSQL(TABLE_USER);
        db.execSQL(TABLE_TWEET);
        db.execSQL(TABLE_FAVORS);
        db.execSQL(TABLE_TRENDS);
        db.execSQL(TABLE_MESSAGES);
        db.execSQL(INDX_TWEET);
        db.execSQL(INDX_FAVOR);
        db.execSQL(INDX_TREND);
        /// Database just created? set current version
        if (db.getVersion() == 0) {
            db.setVersion(LATEST_VERSION);
        }
    }
}