package org.nuclearfog.twidda.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabase extends SQLiteOpenHelper {
    private static final String userTable = "CREATE TABLE IF NOT EXISTS user (" +
            "userID INTEGER PRIMARY KEY,username VARCHAR(50),scrname VARCHAR(15)," +
            "pbLink TEXT,banner TEXT,bio TEXT,location TEXT,link TEXT,userregister INTEGER," +
            "createdAt INTEGER,following INTEGER,follower INTEGER,tweetCount INTEGER,favorCount INTEGER);";

    private static final String tweetTable = "CREATE TABLE IF NOT EXISTS tweet (" +
            "tweetID INTEGER PRIMARY KEY,userID INTEGER,retweetID INTEGER,replyID INTEGER,retweeterID INTEGER," +
            "replyname TEXT,replyUserID INTEGER,time INTEGER,tweet TEXT,media TEXT,retweet INTEGER,favorite INTEGER," +
            "statusregister INTEGER,source VARCHAR(32),FOREIGN KEY (userID) REFERENCES user(userID));";

    private static final String favoriteTable = "CREATE TABLE IF NOT EXISTS favorit (" +
            "ownerID INTEGER,tweetID INTEGER," +
            "FOREIGN KEY (ownerID) REFERENCES user(userID)," +
            "FOREIGN KEY (tweetID) REFERENCES tweet(tweetID));";

    private static final String trendTable = "CREATE TABLE IF NOT EXISTS trend (" +
            "woeID INTEGER,trendpos INTEGER,trendname TEXT,trendlink TEXT);";

    private static final String messageTable = "CREATE TABLE IF NOT EXISTS message (" +
            "messageID INTEGER PRIMARY KEY,time INTEGER,senderID INTEGER,receiverID INTEGER," +
            "message TEXT);";

    private static final String INDX_TWEET = "CREATE INDEX IF NOT EXISTS idx_tweet ON tweet(userID,statusregister);";
    private static final String INDX_FAVOR = "CREATE INDEX IF NOT EXISTS idx_favor ON favorit(ownerID,tweetID);";

    private static AppDatabase mData;

    private AppDatabase(Context context) {
        super(context, "database.db", null, 3);
    }

    public static synchronized AppDatabase getInstance(Context context) {
        if (mData == null) {
            mData = new AppDatabase(context);
        }
        return mData;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(userTable);
        db.execSQL(tweetTable);

        db.execSQL(favoriteTable);

        db.execSQL(trendTable);

        db.execSQL(messageTable);

        db.execSQL(INDX_TWEET);
        db.execSQL(INDX_FAVOR);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2 && newVersion >= 2) {
            final String T_QUERY = "ALTER TABLE user ADD COLUMN tweetCount INTEGER DEFAULT 0;";
            db.execSQL(T_QUERY);
            final String F_QUERY = "ALTER TABLE user ADD COLUMN favorCount INTEGER DEFAULT 0;";
            db.execSQL(F_QUERY);
        }
        if (oldVersion < 3 && newVersion >= 3) {
            db.execSQL("DROP TABLE favorit");
            db.execSQL(favoriteTable);

            db.execSQL(INDX_TWEET);
            db.execSQL(INDX_FAVOR);
        }
    }
}