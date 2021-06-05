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
    private static final String TABLE_USER = "CREATE TABLE IF NOT EXISTS "
            + UserTable.TABLE + "("
            + UserTable.ID + " INTEGER PRIMARY KEY,"
            + UserTable.USERNAME + " TEXT,"
            + UserTable.SCREENNAME + " TEXT,"
            + UserTable.IMAGE + " TEXT,"
            + UserTable.BANNER + " TEXT,"
            + UserTable.DESCRIPTION + " TEXT,"
            + UserTable.LOCATION + " TEXT,"
            + UserTable.LINK + " TEXT,"
            + UserTable.REGISTER + " INTEGER,"
            + UserTable.SINCE + " INTEGER,"
            + UserTable.FRIENDS + " INTEGER,"
            + UserTable.FOLLOWER + " INTEGER,"
            + UserTable.TWEETS + " INTEGER,"
            + UserTable.FAVORS + " INTEGER);";

    /**
     * SQL query to create a table for tweet information
     */
    private static final String TABLE_TWEET = "CREATE TABLE IF NOT EXISTS "
            + TweetTable.TABLE + "("
            + TweetTable.ID + " INTEGER PRIMARY KEY,"
            + TweetTable.USER + " INTEGER,"
            + TweetTable.RETWEETID + " INTEGER,"
            + TweetTable.REPLYTWEET + " INTEGER,"
            + TweetTable.RETWEETUSER + " INTEGER,"
            + TweetTable.REPLYNAME + " TEXT,"
            + TweetTable.REPLYUSER + " INTEGER,"
            + TweetTable.SINCE + " INTEGER,"
            + TweetTable.TWEET + " TEXT,"
            + TweetTable.MEDIA + " TEXT,"
            + TweetTable.RETWEET + " INTEGER,"
            + TweetTable.FAVORITE + " INTEGER,"
            + TweetTable.REGISTER + " INTEGER,"
            + TweetTable.SOURCE + " TEXT,"
            + TweetTable.PLACE + " TEXT,"
            + TweetTable.COORDINATE + " TEXT,"
            + "FOREIGN KEY(" + TweetTable.USER + ")"
            + "REFERENCES " + UserTable.TABLE + "(" + UserTable.ID + "));";

    /**
     * SQL query to create a table for favorite tweets
     */
    private static final String TABLE_FAVORS = "CREATE TABLE IF NOT EXISTS "
            + FavoriteTable.TABLE + "("
            + FavoriteTable.FAVORITEDBY + " INTEGER,"
            + FavoriteTable.TWEETID + " INTEGER,"
            + "FOREIGN KEY(" + FavoriteTable.FAVORITEDBY + ")"
            + "REFERENCES " + UserTable.TABLE + "(" + UserTable.ID + "),"
            + "FOREIGN KEY(" + FavoriteTable.TWEETID + ")"
            + "REFERENCES " + TweetTable.TABLE + "(" + TweetTable.ID + "));";

    /**
     * SQL query to create a table for trend information
     */
    private static final String TABLE_TRENDS = "CREATE TABLE IF NOT EXISTS "
            + TrendTable.TABLE + "("
            + TrendTable.ID + " INTEGER,"
            + TrendTable.INDEX + " INTEGER,"
            + TrendTable.VOL + " INTEGER,"
            + TrendTable.TREND + " TEXT);";

    /**
     * SQL query to create a table for message information
     */
    private static final String TABLE_MESSAGES = "CREATE TABLE IF NOT EXISTS "
            + MessageTable.TABLE + "("
            + MessageTable.ID + " INTEGER PRIMARY KEY,"
            + MessageTable.SINCE + " INTEGER,"
            + MessageTable.SENDER + " INTEGER,"
            + MessageTable.RECEIVER + " INTEGER,"
            + MessageTable.MESSAGE + " TEXT);";

    /**
     * index for tweet table
     */
    private static final String INDX_TWEET = "CREATE INDEX IF NOT EXISTS idx_tweet"
            + " ON " + TweetTable.TABLE + "(" + TweetTable.USER + "," + TweetTable.REGISTER + ");";

    /**
     * index for favorite table
     */
    private static final String INDX_FAVOR = "CREATE INDEX IF NOT EXISTS idx_favor"
            + " ON " + FavoriteTable.TABLE + "(" + FavoriteTable.FAVORITEDBY + "," + FavoriteTable.TWEETID + ");";

    /**
     * index for trend table
     */
    private static final String INDX_TREND = "CREATE INDEX IF NOT EXISTS idx_trend"
            + " ON " + TrendTable.TABLE + "(" + TrendTable.ID + ");";

    /**
     * update for the tweet table
     */
    private static final String TABLE_TWEET_ADD_PLACE = "ALTER TABLE " + TweetTable.TABLE
            + " ADD COLUMN " + TweetTable.PLACE + " TEXT";

    /**
     * update for the tweet table
     */
    private static final String TABLE_TWEET_ADD_GEO = "ALTER TABLE " + TweetTable.TABLE
            + " ADD COLUMN " + TweetTable.COORDINATE + " TEXT";

    /**
     * update for the trend table
     */
    private static final String TABLE_TREND_ADD_VOL = "ALTER TABLE " + TrendTable.TABLE
            + " ADD COLUMN " + TrendTable.VOL + " INTEGER";

    /**
     *
     */
    private static final String USERTWEET_TABLE = TweetTable.TABLE
            + " INNER JOIN " + UserTable.TABLE
            + " ON " + TweetTable.TABLE + "." + TweetTable.USER + "=" + UserTable.TABLE + "." + UserTable.ID;

    /**
     * SQL query to get home timeline tweets
     */
    static final String HOME_QUERY = "SELECT * FROM " + USERTWEET_TABLE
            + " WHERE " + TweetTable.REGISTER + "&? IS NOT 0"
            + " ORDER BY " + TweetTable.ID
            + " DESC LIMIT ?";

    /**
     * SQL query to get mention timeline
     */
    static final String MENTION_QUERY = "SELECT * FROM " + USERTWEET_TABLE
            + " WHERE " + TweetTable.REGISTER + "&? IS NOT 0"
            + " AND " + UserTable.REGISTER + "&? IS 0"
            + " ORDER BY " + TweetTable.ID
            + " DESC LIMIT ?";

    /**
     * SQL query to get tweets of an user
     */
    static final String USERTWEET_QUERY = "SELECT * FROM " + USERTWEET_TABLE
            + " WHERE " + TweetTable.REGISTER + "&? IS NOT 0"
            + " AND " + TweetTable.TABLE + "." + TweetTable.USER + "=?"
            + " ORDER BY " + TweetTable.ID
            + " DESC LIMIT ?";

    /**
     * SQL query to get tweets favored by an user
     */
    static final String USERFAVORIT_QUERY = "SELECT * FROM " + USERTWEET_TABLE
            + " INNER JOIN " + FavoriteTable.TABLE
            + " ON " + TweetTable.TABLE + "." + TweetTable.ID + "=" + FavoriteTable.TABLE + "." + FavoriteTable.TWEETID
            + " WHERE " + FavoriteTable.FAVORITEDBY + "=?"
            + " ORDER BY " + TweetTable.ID
            + " DESC LIMIT ?";

    /**
     * SQL query to get a single tweet specified by an ID
     */
    static final String SINGLE_TWEET_QUERY = "SELECT * FROM " + USERTWEET_TABLE
            + " WHERE " + TweetTable.TABLE + "." + TweetTable.ID + "=? LIMIT 1";

    /**
     * SQL query to get replies of a tweet specified by a reply ID
     */
    static final String ANSWER_QUERY = "SELECT * FROM " + USERTWEET_TABLE
            + " WHERE " + TweetTable.TABLE + "." + TweetTable.REPLYTWEET + "=?"
            + " AND " + TweetTable.REGISTER + "&? IS NOT 0"
            + " AND " + UserTable.REGISTER + "&? IS 0"
            + " ORDER BY " + TweetTable.ID + " DESC LIMIT ?";

    /**
     * SQL query to get locale based trends
     */
    static final String TREND_QUERY = "SELECT * FROM " + TrendTable.TABLE
            + " WHERE " + TrendTable.ID + "=?"
            + " ORDER BY " + TrendTable.INDEX + " ASC";

    /**
     * SQL query to get direct messages
     */
    static final String MESSAGE_QUERY = "SELECT * FROM " + MessageTable.TABLE
            + " ORDER BY " + MessageTable.ID + " DESC "
            + "LIMIT ?";

    /**
     * SQL query to get user information
     */
    static final String USER_QUERY = "SELECT * FROM " + UserTable.TABLE
            + " WHERE " + UserTable.ID + "=?"
            + " LIMIT 1";

    /**
     * SQL query to get a status register for a tweet
     */
    static final String TWEETFLAG_QUERY = "SELECT " + TweetTable.REGISTER + " FROM " + TweetTable.TABLE
            + " WHERE " + TweetTable.ID + "=?"
            + " LIMIT 1;";

    /**
     * SQL query to get a status register of an user
     */
    static final String USERFLAG_QUERY = "SELECT " + UserTable.REGISTER + " FROM " + UserTable.TABLE
            + " WHERE " + UserTable.ID + "=?"
            + " LIMIT 1;";

    /**
     * SQL query to check if a status exists in database
     */
    static final String STATUS_EXIST_QUERY = "SELECT * FROM " + TweetTable.TABLE
            + " WHERE " + TweetTable.ID + "=?"
            + " LIMIT 1;";

    /**
     * singleton instance
     */
    private static DatabaseAdapter instance;

    /**
     * path to the database file
     */
    private final File databasePath;

    /**
     * database
     */
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
            instance = new DatabaseAdapter(context.getApplicationContext());
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

    /**
     * table for user information
     */
    public interface UserTable {
        // table name
        String TABLE = "user";
        // user information
        String ID = "userID";
        String USERNAME = "username";
        String SCREENNAME = "scrname";
        String DESCRIPTION = "bio";
        String LOCATION = "location";
        String LINK = "link";
        String SINCE = "createdAt";
        // image links
        String IMAGE = "pbLink";
        String BANNER = "banner";
        // connections
        String FRIENDS = "following";
        String FOLLOWER = "follower";
        // tweet count of the user
        String TWEETS = "tweetCount";
        String FAVORS = "favorCount";
        // integer register containing status bits
        String REGISTER = "userregister";
    }

    /**
     * table for all tweets
     */
    public interface TweetTable {
        // table name
        String TABLE = "tweet";
        // tweet information
        String ID = "tweetID";
        String USER = "userID";
        String TWEET = "tweet";
        String MEDIA = "media";
        String RETWEET = "retweet";
        String FAVORITE = "favorite";
        String SINCE = "time";
        String SOURCE = "source";
        // tweet location
        String PLACE = "place";
        String COORDINATE = "geo";
        // information about the replied tweet
        String REPLYTWEET = "replyID";
        String REPLYUSER = "replyUserID";
        String REPLYNAME = "replyname";
        // information about the retweeter
        String RETWEETID = "retweetID";
        String RETWEETUSER = "retweeterID";
        // register containing status bits
        String REGISTER = "statusregister";
    }

    /**
     * table for favored tweets of an user
     */
    public interface FavoriteTable {
        // table name
        String TABLE = "favorit";
        //
        String TWEETID = "tweetID";
        String FAVORITEDBY = "ownerID";
    }

    /**
     * table for twitter trends
     */
    public interface TrendTable {
        // tale name
        String TABLE = "trend";
        // trend information
        String ID = "woeID";
        String INDEX = "trendpos";
        String VOL = "vol";
        String TREND = "trendname";
    }

    /**
     * Table for direct messages
     */
    public interface MessageTable {
        // table name
        String TABLE = "message";
        // message information
        String ID = "messageID";
        String SINCE = "time";
        String SENDER = "senderID";
        String RECEIVER = "receiverID";
        String MESSAGE = "message";
    }
}