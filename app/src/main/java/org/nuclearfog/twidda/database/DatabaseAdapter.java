package org.nuclearfog.twidda.database;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * This class creates and manages SQLite table versions
 *
 * @author nuclearfog
 */
public class DatabaseAdapter {

	/**
	 * database version
	 */
	private static final int DB_VERSION = 13;

	/**
	 * database file name
	 */
	private static final String DB_NAME = "database.db";

	/**
	 * SQL query to create a table for user information
	 */
	private static final String TABLE_USER = "CREATE TABLE IF NOT EXISTS "
			+ UserTable.NAME + "("
			+ UserTable.ID + " INTEGER PRIMARY KEY,"
			+ UserTable.USERNAME + " TEXT,"
			+ UserTable.SCREENNAME + " TEXT,"
			+ UserTable.IMAGE + " TEXT,"
			+ UserTable.BANNER + " TEXT,"
			+ UserTable.DESCRIPTION + " TEXT,"
			+ UserTable.LOCATION + " TEXT,"
			+ UserTable.LINK + " TEXT,"
			+ UserTable.SINCE + " INTEGER,"
			+ UserTable.FRIENDS + " INTEGER,"
			+ UserTable.FOLLOWER + " INTEGER,"
			+ UserTable.STATUSES + " INTEGER,"
			+ UserTable.FAVORITS + " INTEGER);";

	/**
	 * SQL query to create a table for status information
	 */
	private static final String TABLE_STATUS = "CREATE TABLE IF NOT EXISTS "
			+ StatusTable.NAME + "("
			+ StatusTable.ID + " INTEGER PRIMARY KEY,"
			+ StatusTable.USER + " INTEGER,"
			+ StatusTable.URL + " TEXT,"
			+ StatusTable.EMBEDDED + " INTEGER,"
			+ StatusTable.REPLYSTATUS + " INTEGER,"
			+ StatusTable.REPLYNAME + " TEXT,"
			+ StatusTable.REPLYUSER + " INTEGER,"
			+ StatusTable.TIME + " INTEGER,"
			+ StatusTable.TEXT + " TEXT,"
			+ StatusTable.MEDIA + " TEXT,"
			+ StatusTable.EMOJI + " TEXT,"
			+ StatusTable.REPOST + " INTEGER,"
			+ StatusTable.FAVORITE + " INTEGER,"
			+ StatusTable.REPLY + " INTEGER,"
			+ StatusTable.CONVERSATION + " INTEGER,"
			+ StatusTable.SOURCE + " TEXT,"
			+ StatusTable.LOCATION + " INTEGER,"
			+ "FOREIGN KEY(" + StatusTable.USER + ")"
			+ "REFERENCES " + UserTable.NAME + "(" + UserTable.ID + "));";

	/**
	 * SQL query to create a table for favorite list
	 */
	private static final String TABLE_FAVORITES = "CREATE TABLE IF NOT EXISTS "
			+ FavoriteTable.NAME + "("
			+ FavoriteTable.OWNER_ID + " INTEGER,"
			+ FavoriteTable.STATUS_ID + " INTEGER,"
			+ "FOREIGN KEY(" + FavoriteTable.OWNER_ID + ")"
			+ "REFERENCES " + UserTable.NAME + "(" + UserTable.ID + "),"
			+ "FOREIGN KEY(" + FavoriteTable.STATUS_ID + ")"
			+ "REFERENCES " + StatusTable.NAME + "(" + StatusTable.ID + "));";

	/**
	 * SQL query to create a table for favorite list
	 */
	private static final String TABLE_BOOKMARKS = "CREATE TABLE IF NOT EXISTS "
			+ BookmarkTable.NAME + "("
			+ BookmarkTable.OWNER_ID + " INTEGER,"
			+ BookmarkTable.STATUS_ID + " INTEGER,"
			+ "FOREIGN KEY(" + BookmarkTable.OWNER_ID + ")"
			+ "REFERENCES " + UserTable.NAME + "(" + UserTable.ID + "),"
			+ "FOREIGN KEY(" + BookmarkTable.STATUS_ID + ")"
			+ "REFERENCES " + StatusTable.NAME + "(" + StatusTable.ID + "));";

	/**
	 * SQL query to create a table for trend information
	 */
	private static final String TABLE_TRENDS = "CREATE TABLE IF NOT EXISTS "
			+ TrendTable.NAME + "("
			+ TrendTable.ID + " INTEGER,"
			+ TrendTable.INDEX + " INTEGER,"
			+ TrendTable.VOL + " INTEGER,"
			+ TrendTable.TREND + " TEXT);";

	/**
	 * SQL query to create a table for message information
	 */
	private static final String TABLE_MESSAGES = "CREATE TABLE IF NOT EXISTS "
			+ MessageTable.NAME + "("
			+ MessageTable.ID + " INTEGER PRIMARY KEY,"
			+ MessageTable.TIME + " INTEGER,"
			+ MessageTable.FROM + " INTEGER,"
			+ MessageTable.TO + " INTEGER,"
			+ MessageTable.MESSAGE + " TEXT,"
			+ MessageTable.MEDIA + " TEXT);";

	/**
	 * table for status register
	 */
	private static final String TABLE_STATUS_REGISTER = "CREATE TABLE IF NOT EXISTS "
			+ StatusRegisterTable.NAME + "("
			+ StatusRegisterTable.OWNER + " INTEGER,"
			+ StatusRegisterTable.ID + " INTEGER,"
			+ StatusRegisterTable.REGISTER + " INTEGER,"
			+ StatusRegisterTable.REPOST_ID + " INTEGER,"
			+ "FOREIGN KEY(" + StatusRegisterTable.ID + ")"
			+ "REFERENCES " + StatusTable.NAME + "(" + StatusTable.ID + "));";

	/**
	 * table for user register
	 */
	private static final String TABLE_USER_REGISTER = "CREATE TABLE IF NOT EXISTS "
			+ UserRegisterTable.NAME + "("
			+ UserRegisterTable.OWNER + " INTEGER,"
			+ UserRegisterTable.ID + " INTEGER,"
			+ UserRegisterTable.REGISTER + " INTEGER,"
			+ "FOREIGN KEY(" + UserRegisterTable.ID + ")"
			+ "REFERENCES " + UserTable.NAME + "(" + UserTable.ID + "));";

	/**
	 * SQL query to create a table for user logins
	 */
	private static final String TABLE_ACCOUNTS = "CREATE TABLE IF NOT EXISTS "
			+ AccountTable.NAME + "("
			+ AccountTable.ID + " INTEGER PRIMARY KEY,"
			+ AccountTable.DATE + " INTEGER,"
			+ AccountTable.ACCESS_TOKEN + " TEXT,"
			+ AccountTable.TOKEN_SECRET + " TEXT,"
			+ AccountTable.HOSTNAME + " TEXT,"
			+ AccountTable.API + " INTEGER,"
			+ AccountTable.CLIENT_ID + " TEXT,"
			+ AccountTable.CLIENT_SECRET + " TEXT,"
			+ AccountTable.BEARER + " TEXT);";

	/**
	 * SQL query to create user exclude table
	 */
	private static final String TABLE_USER_BLOCKLIST = "CREATE TABLE IF NOT EXISTS "
			+ UserExcludeTable.NAME + "("
			+ UserExcludeTable.OWNER + " INTEGER,"
			+ UserExcludeTable.ID + " INTEGER);";

	/**
	 * SQL query to create table for notifications
	 */
	public static final String TABLE_NOTIFICATION = "CREATE TABLE IF NOT EXISTS "
			+ NotificationTable.NAME + "("
			+ NotificationTable.ID + " INTEGER PRIMARY KEY,"
			+ NotificationTable.OWNER + " INTEGER,"
			+ NotificationTable.USER + " INTEGER,"
			+ NotificationTable.TIME + " INTEGER,"
			+ NotificationTable.TYPE + " INTEGER,"
			+ NotificationTable.ITEM + " INTEGER);";

	/**
	 * SQL query to create media table
	 */
	public static final String TABLE_MEDIA = "CREATE TABLE IF NOT EXISTS "
			+ MediaTable.NAME + "("
			+ MediaTable.KEY + " TEXT PRIMARY KEY,"
			+ MediaTable.TYPE + " INTEGER,"
			+ MediaTable.URL + " TEXT,"
			+ MediaTable.PREVIEW + " TEXT);";

	/**
	 * SQL query to create location table
	 */
	public static final String TABLE_LOCATION = "CREATE TABLE  IF NOT EXISTS "
			+ LocationTable.NAME + "("
			+ LocationTable.ID + " INTEGER,"
			+ LocationTable.COUNTRY + " TEXT,"
			+ LocationTable.COORDINATES + " TEXT,"
			+ LocationTable.PLACE + " TEXT,"
			+ LocationTable.FULLNAME + " TEXT);";

	/**
	 * SQL query to create the emoji table
	 */
	public static final String TABLE_EMOJI = "CREATE TABLE IF NOT EXISTS "
			+ EmojiTable.NAME + "("
			+ EmojiTable.CODE + " TEXT PRIMARY KEY,"
			+ EmojiTable.CATEGORY + " TEXT,"
			+ EmojiTable.URL + " TEXT);";

	/**
	 * table index for status table
	 */
	private static final String INDX_STATUS = "CREATE INDEX IF NOT EXISTS idx_tweet"
			+ " ON " + StatusTable.NAME + "(" + StatusTable.USER + ");";

	/**
	 * table index for status register
	 */
	private static final String INDX_STATUS_REG = "CREATE INDEX IF NOT EXISTS idx_tweet_register"
			+ " ON " + StatusRegisterTable.NAME + "(" + StatusRegisterTable.OWNER + "," + StatusRegisterTable.ID + ");";

	/**
	 * table index for user register
	 */
	private static final String INDX_USER_REG = "CREATE INDEX IF NOT EXISTS idx_user_register"
			+ " ON " + UserRegisterTable.NAME + "(" + UserRegisterTable.OWNER + "," + UserRegisterTable.ID + ");";

	/**
	 * update account table to add social network hostname
	 */
	private static final String UPDATE_ADD_HOST = "ALTER TABLE " + AccountTable.NAME + " ADD " + AccountTable.HOSTNAME + " TEXT;";

	/**
	 * update account table to add API client ID
	 */
	private static final String UPDATE_ADD_CLIENT_ID = "ALTER TABLE " + AccountTable.NAME + " ADD " + AccountTable.CLIENT_ID + " TEXT;";

	/**
	 * update account table to add API client secret
	 */
	private static final String UPDATE_ADD_CLIENT_SEC = "ALTER TABLE " + AccountTable.NAME + " ADD " + AccountTable.CLIENT_SECRET + " TEXT;";

	/**
	 * update account table to add API client secret
	 */
	private static final String UPDATE_ADD_API_ID = "ALTER TABLE " + AccountTable.NAME + " ADD " + AccountTable.API + " INTEGER;";

	/**
	 * update account table to add API client secret
	 */
	private static final String UPDATE_ADD_REPLY_COUNT = "ALTER TABLE " + StatusTable.NAME + " ADD " + StatusTable.REPLY + " INTEGER;";

	/**
	 * update account table to add API client secret
	 */
	private static final String UPDATE_ADD_BEARER = "ALTER TABLE " + AccountTable.NAME + " ADD " + AccountTable.BEARER + " TEXT;";

	/**
	 * update account table to add API client secret
	 */
	private static final String UPDATE_ADD_CONVERSATION_ID = "ALTER TABLE " + StatusTable.NAME + " ADD " + StatusTable.CONVERSATION + " INTEGER;";

	/**
	 * update status table add location ID
	 */
	private static final String UPDATE_ADD_LOCATION_ID = "ALTER TABLE " + StatusTable.NAME + " ADD " + StatusTable.LOCATION + " INTEGER;";

	/**
	 * update status table add location ID
	 */
	private static final String UPDATE_ADD_STATUS_URL = "ALTER TABLE " + StatusTable.NAME + " ADD " + StatusTable.URL + " TEXT;";

	/**
	 * update status table add emoji keys
	 */
	private static final String UPDATE_ADD_STATUS_EMOJI = "ALTER TABLE " + StatusTable.NAME + " ADD " + StatusTable.EMOJI + " TEXT;";

	/**
	 * singleton instance
	 */
	private static final DatabaseAdapter INSTANCE = new DatabaseAdapter();

	/**
	 * path to the database file
	 */
	private File databasePath;

	/**
	 * database
	 */
	private SQLiteDatabase db;

	/**
	 *
	 */
	private DatabaseAdapter() {
	}

	/**
	 * get database instance
	 *
	 * @return SQLite database
	 */
	public synchronized SQLiteDatabase getDatabase() {
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
		if (INSTANCE.db == null) {
			try {
				INSTANCE.init(context.getApplicationContext());
			} catch (SQLiteException e) {
				// if database is corrupted, clear and create a new one
				e.printStackTrace();
				deleteDatabase(context);
			}
		}
		return INSTANCE;
	}

	/**
	 * delete database and destroy instance
	 *
	 * @param c application context
	 */
	public static void deleteDatabase(Context c) {
		SQLiteDatabase.deleteDatabase(c.getDatabasePath(DB_NAME));
		INSTANCE.init(c.getApplicationContext());
	}

	/**
	 * initialize databases
	 *
	 * @param c application context
	 */
	private void init(Context c) {
		// fetch database information
		databasePath = c.getDatabasePath(DB_NAME);
		db = c.openOrCreateDatabase(databasePath.toString(), MODE_PRIVATE, null);
		// create tables if not exist
		db.execSQL(TABLE_USER);
		db.execSQL(TABLE_STATUS);
		db.execSQL(TABLE_FAVORITES);
		db.execSQL(TABLE_BOOKMARKS);
		db.execSQL(TABLE_TRENDS);
		db.execSQL(TABLE_MESSAGES);
		db.execSQL(TABLE_ACCOUNTS);
		db.execSQL(TABLE_USER_BLOCKLIST);
		db.execSQL(TABLE_STATUS_REGISTER);
		db.execSQL(TABLE_USER_REGISTER);
		db.execSQL(TABLE_NOTIFICATION);
		db.execSQL(TABLE_MEDIA);
		db.execSQL(TABLE_LOCATION);
		db.execSQL(TABLE_EMOJI);
		// create index if not exist
		db.execSQL(INDX_STATUS);
		db.execSQL(INDX_STATUS_REG);
		db.execSQL(INDX_USER_REG);
		// set initial version
		if (db.getVersion() == 0) {
			db.setVersion(DB_VERSION);
		} else {
			if (db.getVersion() < 6) {
				db.execSQL(UPDATE_ADD_HOST);
				db.execSQL(UPDATE_ADD_CLIENT_ID);
				db.execSQL(UPDATE_ADD_CLIENT_SEC);
				db.setVersion(6);
			}
			if (db.getVersion() < 7) {
				db.execSQL(UPDATE_ADD_API_ID);
				db.setVersion(7);
			}
			if (db.getVersion() < 8) {
				db.execSQL(UPDATE_ADD_REPLY_COUNT);
				db.setVersion(8);
			}
			if (db.getVersion() < 9) {
				db.execSQL(UPDATE_ADD_BEARER);
				db.setVersion(9);
			}
			if (db.getVersion() < 10) {
				db.execSQL(UPDATE_ADD_CONVERSATION_ID);
				db.setVersion(10);
			}
			if (db.getVersion() < 11) {
				db.execSQL(UPDATE_ADD_LOCATION_ID);
				db.setVersion(11);
			}
			if (db.getVersion() < 12) {
				db.execSQL(UPDATE_ADD_STATUS_URL);
				db.setVersion(12);
			}
			if (db.getVersion() < DB_VERSION) {
				db.execSQL(UPDATE_ADD_STATUS_EMOJI);
				db.setVersion(DB_VERSION);
			}
		}
	}

	/**
	 * table for user information
	 */
	public interface UserTable {

		/**
		 * table name
		 */
		String NAME = "user";

		/**
		 * ID of the user
		 */
		String ID = "userID";

		/**
		 * user name
		 */
		String USERNAME = "username";

		/**
		 * screen name (starting with @)
		 */
		String SCREENNAME = "scrname";

		/**
		 * description (bio) of the user
		 */
		String DESCRIPTION = "bio";

		/**
		 * location attached to profile
		 */
		String LOCATION = "location";

		/**
		 * link attached to profile
		 */
		String LINK = "link";

		/**
		 * date of account creation
		 */
		String SINCE = "createdAt";

		/**
		 * link to the original profile image
		 */
		String IMAGE = "pbLink";

		/**
		 * link to the original banner image
		 */
		String BANNER = "banner";

		/**
		 * following count
		 */
		String FRIENDS = "following";

		/**
		 * follower count
		 */
		String FOLLOWER = "follower";

		/**
		 * count of statuses posted by user
		 */
		String STATUSES = "tweetCount";

		/**
		 * count of the statuses favored by the user
		 */
		String FAVORITS = "favorCount";
	}

	/**
	 * table for all status
	 */
	public interface StatusTable {
		/**
		 * table name
		 */
		String NAME = "tweet";

		/**
		 * ID of the status
		 */
		String ID = "tweetID";

		/**
		 * ID of the author
		 */
		String USER = "userID";

		/**
		 * status text
		 */
		String TEXT = "tweet";

		/**
		 * media keys
		 */
		String MEDIA = "media";

		/**
		 * emoji keys
		 */
		String EMOJI = "emoji";

		/**
		 * repost count
		 */
		String REPOST = "retweet";

		/**
		 * favorite count
		 */
		String FAVORITE = "favorite";

		/**
		 * reply count
		 */
		String REPLY = "reply";

		/**
		 * timestamp of the status
		 */
		String TIME = "time";

		/**
		 * API source of the status
		 */
		String SOURCE = "source";

		/**
		 * URL of the status
		 */
		String URL = "url";

		/**
		 * place name of the status
		 */
		String LOCATION = "location_id";

		/**
		 * ID of the replied status
		 */
		String REPLYSTATUS = "replyID";

		/**
		 * first status ID of a conversation
		 */
		String CONVERSATION = "conversationID";

		/**
		 * ID of the replied user
		 */
		String REPLYUSER = "replyUserID";

		/**
		 * name of the replied user
		 */
		String REPLYNAME = "replyname";

		/**
		 * ID of the embedded (reposted) status
		 */
		String EMBEDDED = "retweetID";
	}

	/**
	 * table for favored statuses of an user
	 */
	public interface FavoriteTable {
		/**
		 * table name
		 */
		String NAME = "favorit";

		/**
		 * ID of the status
		 */
		String STATUS_ID = "tweetID";

		/**
		 * ID of the user of this favored status
		 */
		String OWNER_ID = "ownerID";
	}

	/**
	 * status bookmark table
	 */
	public interface BookmarkTable {
		/**
		 * table name
		 */
		String NAME = "bookmarks";

		/**
		 * ID of the status
		 */
		String STATUS_ID = "tweetID";

		/**
		 * ID of the user of this bookmarks
		 */
		String OWNER_ID = "ownerID";
	}

	/**
	 * table for twitter trends
	 */
	public interface TrendTable {
		/**
		 * table name
		 */
		String NAME = "trend";

		/**
		 * Location ID of the trend
		 */
		String ID = "woeID";

		/**
		 * rank of the trend
		 */
		String INDEX = "trendpos";

		/**
		 * popularity count
		 */
		String VOL = "vol";

		/**
		 * name of the trend
		 */
		String TREND = "trendname";
	}

	/**
	 * Table for direct messages
	 */
	public interface MessageTable {
		/**
		 * table name
		 */
		String NAME = "message";

		/**
		 * ID of the message
		 */
		String ID = "messageID";

		/**
		 * date of the message
		 */
		String TIME = "time";

		/**
		 * User ID of the sender
		 */
		String FROM = "senderID";

		/**
		 * User ID of the receiver
		 */
		String TO = "receiverID";

		/**
		 * message text
		 */
		String MESSAGE = "message";

		/**
		 * media keys
		 */
		String MEDIA = "media";
	}

	/**
	 * Table for multi user login information
	 */
	public interface AccountTable {
		/**
		 * SQL table name
		 */
		String NAME = "login";

		/**
		 * social network host
		 */
		String HOSTNAME = "host";

		/**
		 * used API
		 */
		String API = "api";

		/**
		 * ID of the user
		 */
		String ID = "userID";

		/**
		 * date of login
		 */
		String DATE = "date";

		/**
		 * API ID
		 */
		String CLIENT_ID = "client_id";

		/**
		 * API secret
		 */
		String CLIENT_SECRET = "client_secret";

		/**
		 * primary oauth access token
		 */
		String ACCESS_TOKEN = "auth_key1";

		/**
		 * second oauth access token
		 */
		String TOKEN_SECRET = "auth_key2";

		/**
		 * bearer token
		 */
		String BEARER = "bearer";
	}

	/**
	 * table for status register
	 * <p>
	 * a register contains status flags (status bits) of a status
	 * every flag stands for a status like reposted or favored
	 * the idea is to save space by putting boolean rows into a single integer row
	 * <p>
	 * to avoid conflicts between multi users,
	 * every login has its own status registers
	 */
	public interface StatusRegisterTable {
		/**
		 * SQL table name
		 */
		String NAME = "tweetFlags";

		/**
		 * ID of the user this register references to
		 */
		String ID = "tweetID";

		/**
		 * ID of the current user accessing the database
		 */
		String OWNER = "ownerID";

		/**
		 * Register with status bits
		 */
		String REGISTER = "tweetRegister";

		/**
		 * ID of the repost of the current user (if exists)
		 */
		String REPOST_ID = "retweeterID";
	}

	/**
	 * table for user register
	 */
	public interface UserRegisterTable {
		/**
		 * SQL table name
		 */
		String NAME = "userFlags";

		/**
		 * ID of the user this register references to
		 */
		String ID = "userID";

		/**
		 * ID of the current user accessing the database
		 */
		String OWNER = "ownerID";

		/**
		 * Register with status bits
		 */
		String REGISTER = "userRegister";
	}

	/**
	 * table for user filter list
	 */
	public interface UserExcludeTable {
		/**
		 * table name
		 */
		String NAME = "userExclude";

		/**
		 * owner ID of the list
		 */
		String OWNER = "listOwner";

		/**
		 * user ID to filter
		 */
		String ID = "userID";
	}

	/**
	 * table for notifications
	 */
	public interface NotificationTable {

		/**
		 * table name
		 */
		String NAME = "notification";

		/**
		 * ID of the notification
		 */
		String ID = "notificationID";

		/**
		 * ID of the user owning the notification
		 */
		String OWNER = "ownerID";

		/**
		 * creation time of the notification
		 */
		String TIME = "timestamp";

		/**
		 * ID of the notification sender (user ID)
		 */
		String USER = "userID";

		/**
		 * universal ID (status ID, list ID...)
		 */
		String ITEM = "itemID";

		/**
		 * type of notification
		 * {@link org.nuclearfog.twidda.model.Notification}
		 */
		String TYPE = "type";
	}

	/**
	 * Table for media information
	 */
	public interface MediaTable {

		/**
		 * table name
		 */
		String NAME = "media";

		/**
		 * key to identify the media entry
		 */
		String KEY = "media_key";

		/**
		 * type of media {@link org.nuclearfog.twidda.model.Media}
		 */
		String TYPE = "media_type";

		/**
		 * media url of the media
		 */
		String URL = "media_url";

		/**
		 * url for the media thumbnail
		 */
		String PREVIEW = "media_preview_url";
	}

	/**
	 * Table for location information
	 */
	public interface LocationTable {

		/**
		 * Table name
		 */
		String NAME = "location";

		/**
		 * locaion ID
		 */
		String ID = "id";

		/**
		 * country name
		 */
		String COUNTRY = "country";

		/**
		 * place name
		 */
		String PLACE = "place";

		/**
		 * place coordinates
		 */
		String COORDINATES = "coordinates";

		/**
		 * full name of the location
		 */
		String FULLNAME = "full_name";
	}

	/**
	 * Table for custom empji information
	 */
	public interface EmojiTable {

		/**
		 * table name
		 */
		String NAME = "emoji";

		/**
		 * emoji code
		 */
		String CODE = "code";

		/**
		 * emoji category
		 */
		String CATEGORY = "category";

		/**
		 * emoji image url
		 */
		String URL = "url";
	}
}