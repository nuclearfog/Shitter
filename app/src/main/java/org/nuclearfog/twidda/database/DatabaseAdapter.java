package org.nuclearfog.twidda.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.BuildConfig;

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
	private static final int DB_VERSION = 20;

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
			+ UserTable.FAVORITS + " INTEGER,"
			+ UserTable.EMOJI + " TEXT);";

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
			+ StatusTable.POLL + " INTEGER,"
			+ StatusTable.REPOST + " INTEGER,"
			+ StatusTable.FAVORITE + " INTEGER,"
			+ StatusTable.REPLY + " INTEGER,"
			+ StatusTable.SOURCE + " TEXT,"
			+ StatusTable.MENTIONS + " TEXT,"
			+ StatusTable.LOCATION + " INTEGER,"
			+ StatusTable.LANGUAGE + " TEXT);";

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
	 * SQL query to create table for notifications
	 */
	private static final String TABLE_NOTIFICATION = "CREATE TABLE IF NOT EXISTS "
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
	private static final String TABLE_MEDIA = "CREATE TABLE IF NOT EXISTS "
			+ MediaTable.NAME + "("
			+ MediaTable.KEY + " TEXT PRIMARY KEY,"
			+ MediaTable.TYPE + " INTEGER,"
			+ MediaTable.URL + " TEXT,"
			+ MediaTable.DESCRIPTION + " TEXT,"
			+ MediaTable.BLUR + " TEXT,"
			+ MediaTable.PREVIEW + " TEXT);";

	/**
	 * SQL query to create location table
	 */
	private static final String TABLE_LOCATION = "CREATE TABLE  IF NOT EXISTS "
			+ LocationTable.NAME + "("
			+ LocationTable.ID + " INTEGER PRIMARY KEY,"
			+ LocationTable.COUNTRY + " TEXT,"
			+ LocationTable.COORDINATES + " TEXT,"
			+ LocationTable.PLACE + " TEXT,"
			+ LocationTable.FULLNAME + " TEXT);";

	/**
	 * SQL query to create the emoji table
	 */
	private static final String TABLE_EMOJI = "CREATE TABLE IF NOT EXISTS "
			+ EmojiTable.NAME + "("
			+ EmojiTable.URL + " TEXT PRIMARY KEY,"
			+ EmojiTable.CATEGORY + " TEXT,"
			+ EmojiTable.CODE + " TEXT);";

	/**
	 * SQL query to create a poll table
	 */
	private static final String TABLE_POLL = "CREATE TABLE IF NOT EXISTS "
			+ PollTable.NAME + "("
			+ PollTable.ID + " INTEGER PRIMARY KEY,"
			+ PollTable.NAME + " TEXT,"
			+ PollTable.EXPIRATION + " INTEGER,"
			+ PollTable.OPTIONS + " TEXT);";

	/**
	 * table for status register
	 */
	private static final String TABLE_STATUS_REGISTER = "CREATE TABLE IF NOT EXISTS "
			+ StatusRegisterTable.NAME + "("
			+ StatusRegisterTable.OWNER + " INTEGER,"
			+ StatusRegisterTable.STATUS + " INTEGER,"
			+ StatusRegisterTable.REGISTER + " INTEGER,"
			+ StatusRegisterTable.REPOST_ID + " INTEGER);";

	/**
	 * table for user register
	 */
	private static final String TABLE_USER_REGISTER = "CREATE TABLE IF NOT EXISTS "
			+ UserRegisterTable.NAME + "("
			+ UserRegisterTable.OWNER + " INTEGER,"
			+ UserRegisterTable.USER + " INTEGER,"
			+ UserRegisterTable.REGISTER + " INTEGER);";

	/**
	 * SQL query to create a table for favorite list
	 */
	private static final String TABLE_FAVORITES = "CREATE TABLE IF NOT EXISTS "
			+ FavoriteTable.NAME + "("
			+ FavoriteTable.OWNER + " INTEGER,"
			+ FavoriteTable.STATUS + " INTEGER);";

	/**
	 * SQL query to create a table for favorite list
	 */
	private static final String TABLE_BOOKMARKS = "CREATE TABLE IF NOT EXISTS "
			+ BookmarkTable.NAME + "("
			+ BookmarkTable.OWNER + " INTEGER,"
			+ BookmarkTable.STATUS + " INTEGER);";

	/**
	 * SQL query to create user blocklist table
	 */
	private static final String TABLE_USER_BLOCKLIST = "CREATE TABLE IF NOT EXISTS "
			+ UserExcludeTable.NAME + "("
			+ UserExcludeTable.OWNER + " INTEGER,"
			+ UserExcludeTable.USER + " INTEGER);";

	/**
	 * SQL query to create instance table
	 */
	private static final String TABLE_INSTANCES = "CREATE TABLE IF NOT EXISTS "
			+ InstanceTable.NAME + "("
			+ InstanceTable.DOMAIN + " TEXT PRIMARY KEY,"
			+ InstanceTable.TIMESTAMP + " INTEGER,"
			+ InstanceTable.TITLE + " TEXT,"
			+ InstanceTable.VERSION + " TEXT,"
			+ InstanceTable.DESCRIPTION + " TEXT,"
			+ InstanceTable.MIME_TYPES + " TEXT,"
			+ InstanceTable.FLAGS + " INTEGER,"
			+ InstanceTable.IMAGE_LIMIT + " INTEGER,"
			+ InstanceTable.GIF_LIMIT + " INTEGER,"
			+ InstanceTable.VIDEO_LIMIT + " INTEGER,"
			+ InstanceTable.AUDIO_LIMIT + " INTEGER,"
			+ InstanceTable.IMAGE_SIZE + " INTEGER,"
			+ InstanceTable.GIF_SIZE + " INTEGER,"
			+ InstanceTable.VIDEO_SIZE + " INTEGER,"
			+ InstanceTable.AUDIO_SIZE + " INTEGER,"
			+ InstanceTable.STATUS_MAX_CHAR + " INTEGER,"
			+ InstanceTable.OPTION_MAX_CHAR + " INTEGER,"
			+ InstanceTable.HASHTAG_LIMIT + " INTEGER,"
			+ InstanceTable.OPTIONS_LIMIT + " INTEGER,"
			+ InstanceTable.POLL_MIN_DURATION + " INTEGER,"
			+ InstanceTable.POLL_MAX_DURATION + " INTEGER);";

	/**
	 * table index for status table
	 */
	private static final String INDX_STATUS = "CREATE INDEX IF NOT EXISTS idx_tweet"
			+ " ON " + StatusTable.NAME + "(" + StatusTable.USER + ");";

	/**
	 * table index for status register
	 */
	private static final String INDX_STATUS_REG = "CREATE INDEX IF NOT EXISTS idx_tweet_register"
			+ " ON " + StatusRegisterTable.NAME + "(" + StatusRegisterTable.OWNER + "," + StatusRegisterTable.STATUS + ");";

	/**
	 * table index for user register
	 */
	private static final String INDX_USER_REG = "CREATE INDEX IF NOT EXISTS idx_user_register"
			+ " ON " + UserRegisterTable.NAME + "(" + UserRegisterTable.OWNER + "," + UserRegisterTable.USER + ");";

	/**
	 * update account table to add social network hostname
	 */
	private static final String UPDATE_ACCOUNT_ADD_HOST = "ALTER TABLE " + AccountTable.NAME + " ADD " + AccountTable.HOSTNAME + " TEXT;";

	/**
	 * update account table to add API client ID
	 */
	private static final String UPDATE_ACCOUNT_ADD_CLIENT_ID = "ALTER TABLE " + AccountTable.NAME + " ADD " + AccountTable.CLIENT_ID + " TEXT;";

	/**
	 * update account table to add API client secret
	 */
	private static final String UPDATE_ACCOUNT_ADD_CLIENT_SEC = "ALTER TABLE " + AccountTable.NAME + " ADD " + AccountTable.CLIENT_SECRET + " TEXT;";

	/**
	 * update account table to add API client secret
	 */
	private static final String UPDATE_ACCOUNT_ADD_API = "ALTER TABLE " + AccountTable.NAME + " ADD " + AccountTable.API + " INTEGER;";

	/**
	 * update account table to add API client secret
	 */
	private static final String UPDATE_STATUS_ADD_REPLY_COUNT = "ALTER TABLE " + StatusTable.NAME + " ADD " + StatusTable.REPLY + " INTEGER;";

	/**
	 * update account table to add API client secret
	 */
	private static final String UPDATE_ACCOUNT_ADD_BEARER = "ALTER TABLE " + AccountTable.NAME + " ADD " + AccountTable.BEARER + " TEXT;";

	/**
	 * update status table add location ID
	 */
	private static final String UPDATE_STATUS_ADD_LOCATION = "ALTER TABLE " + StatusTable.NAME + " ADD " + StatusTable.LOCATION + " INTEGER;";

	/**
	 * update status table add location ID
	 */
	private static final String UPDATE_STATUS_ADD_URL = "ALTER TABLE " + StatusTable.NAME + " ADD " + StatusTable.URL + " TEXT;";

	/**
	 * update status table add emoji keys
	 */
	private static final String UPDATE_STATUS_ADD_EMOJI = "ALTER TABLE " + StatusTable.NAME + " ADD " + StatusTable.EMOJI + " TEXT;";

	/**
	 * update status table add emoji keys
	 */
	private static final String UPDATE_STATUS_ADD_STATUS_POLL = "ALTER TABLE " + StatusTable.NAME + " ADD " + StatusTable.POLL + " INTEGER;";

	/**
	 * update status table add language string
	 */
	private static final String UPDATE_STATUS_ADD_LANGUAGE = "ALTER TABLE " + StatusTable.NAME + " ADD " + StatusTable.LANGUAGE + " TEXT;";

	/**
	 * update user table add emoji key string
	 */
	private static final String UPDATE_USER_ADD_EMOJI = "ALTER TABLE " + UserTable.NAME + " ADD " + UserTable.EMOJI + " TEXT;";

	/**
	 * add mention column to status table
	 */
	private static final String UPDATE_STATUS_ADD_MENTIONS = "ALTER TABLE " + StatusTable.NAME + " ADD " + StatusTable.MENTIONS + " TEXT;";

	/**
	 * add mediatable description
	 */
	private static final String UPDATE_MEDIA_ADD_DESCRIPTION = "ALTER TABLE " + MediaTable.NAME + " ADD " + MediaTable.DESCRIPTION + " TEXT;";

	/**
	 * add mediatable description
	 */
	private static final String UPDATE_MEDIA_ADD_BLUR_HASH = "ALTER TABLE " + MediaTable.NAME + " ADD " + MediaTable.BLUR + " TEXT;";

	/**
	 * singleton instance
	 */
	private static DatabaseAdapter instance;

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
	private DatabaseAdapter(Context context) {
		// fetch database information
		databasePath = context.getDatabasePath(DB_NAME);
		db = context.openOrCreateDatabase(databasePath.toString(), Context.MODE_PRIVATE, null);
		// create tables if not exist
		db.execSQL(TABLE_USER);
		db.execSQL(TABLE_STATUS);
		db.execSQL(TABLE_FAVORITES);
		db.execSQL(TABLE_BOOKMARKS);
		db.execSQL(TABLE_TRENDS);
		db.execSQL(TABLE_ACCOUNTS);
		db.execSQL(TABLE_USER_BLOCKLIST);
		db.execSQL(TABLE_STATUS_REGISTER);
		db.execSQL(TABLE_USER_REGISTER);
		db.execSQL(TABLE_NOTIFICATION);
		db.execSQL(TABLE_MEDIA);
		db.execSQL(TABLE_LOCATION);
		db.execSQL(TABLE_EMOJI);
		db.execSQL(TABLE_POLL);
		db.execSQL(TABLE_INSTANCES);
		// create index if not exist
		db.execSQL(INDX_STATUS);
		db.execSQL(INDX_STATUS_REG);
		db.execSQL(INDX_USER_REG);
		// set initial version
		if (db.getVersion() == 0) {
			db.setVersion(DB_VERSION);
		}
		// update table
		else if (db.getVersion() != DB_VERSION) {
			if (db.getVersion() < 6) {
				db.execSQL(UPDATE_ACCOUNT_ADD_HOST);
				db.execSQL(UPDATE_ACCOUNT_ADD_CLIENT_ID);
				db.execSQL(UPDATE_ACCOUNT_ADD_CLIENT_SEC);
				db.setVersion(6);
			}
			if (db.getVersion() < 7) {
				db.execSQL(UPDATE_ACCOUNT_ADD_API);
				db.setVersion(7);
			}
			if (db.getVersion() < 8) {
				db.execSQL(UPDATE_STATUS_ADD_REPLY_COUNT);
				db.setVersion(8);
			}
			if (db.getVersion() < 9) {
				db.execSQL(UPDATE_ACCOUNT_ADD_BEARER);
				db.setVersion(9);
			}
			if (db.getVersion() < 11) {
				db.execSQL(UPDATE_STATUS_ADD_LOCATION);
				db.setVersion(11);
			}
			if (db.getVersion() < 12) {
				db.execSQL(UPDATE_STATUS_ADD_URL);
				db.setVersion(12);
			}
			if (db.getVersion() < 13) {
				db.execSQL(UPDATE_STATUS_ADD_EMOJI);
				db.setVersion(13);
			}
			if (db.getVersion() < 14) {
				db.execSQL(UPDATE_STATUS_ADD_STATUS_POLL);
				db.setVersion(14);
			}
			if (db.getVersion() < 15) {
				db.execSQL(UPDATE_STATUS_ADD_LANGUAGE);
				db.setVersion(15);
			}
			if (db.getVersion() < 16) {
				db.execSQL(UPDATE_USER_ADD_EMOJI);
				db.setVersion(16);
			}
			if (db.getVersion() < 17) {
				db.execSQL(UPDATE_STATUS_ADD_MENTIONS);
				db.setVersion(17);
			}
			if (db.getVersion() < 18) {
				db.execSQL(UPDATE_MEDIA_ADD_DESCRIPTION);
				db.setVersion(18);
			}
			if (db.getVersion() < 19) {
				db.execSQL(UPDATE_MEDIA_ADD_BLUR_HASH);
				db.setVersion(19);
			}
			if (db.getVersion() < DB_VERSION) {
				db.delete(EmojiTable.NAME, null, null);
				db.execSQL(TABLE_EMOJI);
				db.setVersion(DB_VERSION);
			}
		}
	}

	/**
	 * get database adapter instance
	 *
	 * @param context application context
	 * @return database instance
	 */
	static DatabaseAdapter getInstance(@NonNull Context context) {
		if (instance == null) {
			try {
				instance = new DatabaseAdapter(context.getApplicationContext());
			} catch (SQLiteException exception) {
				// if database is corrupted, clear and create a new one
				if (BuildConfig.DEBUG)
					exception.printStackTrace();
				SQLiteDatabase.deleteDatabase(instance.databasePath);
				instance = new DatabaseAdapter(context.getApplicationContext());
			}
		}
		return instance;
	}

	/**
	 * Get SQLite instance for reading database
	 *
	 * @return SQLite instance
	 */
	SQLiteDatabase getDbRead() {
		if (!db.isOpen())
			db = SQLiteDatabase.openOrCreateDatabase(databasePath, null);
		return db;
	}

	/**
	 * GET SQLite instance for writing database
	 *
	 * @return SQLite instance
	 */
	SQLiteDatabase getDbWrite() {
		SQLiteDatabase db = getDbRead();
		db.beginTransaction();
		return db;
	}

	/**
	 * Commit changes and close Database
	 */
	void commit() {
		db.setTransactionSuccessful();
		db.endTransaction();
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

		/**
		 * emoji keys
		 */
		String EMOJI = "userEmoji";
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
		 * mentioned usernames
		 */
		String MENTIONS = "mentions";

		/**
		 * media keys
		 */
		String MEDIA = "media";

		/**
		 * emoji keys
		 */
		String EMOJI = "emoji";

		/**
		 * ID of a {@link org.nuclearfog.twidda.model.Poll}
		 */
		String POLL = "pollID";

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

		/**
		 * language of the status
		 */
		String LANGUAGE = "lang";
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
		String STATUS = "tweetID";

		/**
		 * ID of the user of this favored status
		 */
		String OWNER = "ownerID";
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
		String STATUS = "tweetID";

		/**
		 * ID of the user of this bookmarks
		 */
		String OWNER = "ownerID";
	}

	/**
	 * table for trends and trending hashtags
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
		String STATUS = "tweetID";

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
		String USER = "userID";

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
		String USER = "userID";
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

		/**
		 * description of the media
		 */
		String DESCRIPTION = "media_description";

		/**
		 * blur hash of the preview image
		 */
		String BLUR = "blur_hash";
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
		 * location ID
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

	/**
	 * Table for status poll
	 */
	public interface PollTable {

		/**
		 * table name
		 */
		String NAME = "poll";

		/**
		 * poll ID
		 */
		String ID = "poll_id";

		/**
		 * expiration time
		 */
		String EXPIRATION = "expires_at";

		/**
		 * poll options titles separated by ';'
		 */
		String OPTIONS = "options";
	}

	/**
	 *
	 */
	public interface InstanceTable {

		/**
		 * table name
		 */
		String NAME = "instance";

		/**
		 * domain name
		 */
		String DOMAIN = "instance_domain";

		/**
		 * timestamp of the last update
		 */
		String TIMESTAMP = "instance_timestamp";

		/**
		 * title of the instance
		 */
		String TITLE = "instance_title";

		/**
		 * API verison
		 */
		String VERSION = "instance_version";

		/**
		 * instance description
		 */
		String DESCRIPTION = "instance_description";

		/**
		 * instance flags
		 */
		String FLAGS = "instance_flags";

		/**
		 * hashtag follow limit
		 */
		String HASHTAG_LIMIT = "limit_follow_tag";

		/**
		 * max allowed status length
		 */
		String STATUS_MAX_CHAR = "limit_char_status";

		/**
		 * status image attachment limit
		 */
		String IMAGE_LIMIT = "limit_image";

		/**
		 * status video attachment limit
		 */
		String VIDEO_LIMIT = "limit_video";

		/**
		 * status gif attachment limit
		 */
		String GIF_LIMIT = "limit_gif";

		/**
		 * status audio attachment limit
		 */
		String AUDIO_LIMIT = "limit_audio";

		/**
		 * status poll option limit
		 */
		String OPTIONS_LIMIT = "limit_count_options";

		/**
		 * status poll option max length
		 */
		String OPTION_MAX_CHAR = "limit_char_options";

		/**
		 * instance supproted MIME types
		 */
		String MIME_TYPES = "mime_types";

		/**
		 * max image size supported by instance
		 */
		String IMAGE_SIZE = "max_size_image";

		/**
		 * max video size supported by instance
		 */
		String VIDEO_SIZE = "max_size_video";

		/**
		 * max gif size supported by instance
		 */
		String GIF_SIZE = "max_size_gif";

		/**
		 * max audio size supported by instance
		 */
		String AUDIO_SIZE = "max_size_audio";

		/**
		 * minimum status poll duration
		 */
		String POLL_MIN_DURATION = "duration_poll_min";

		/**
		 * maximum status poll duration
		 */
		String POLL_MAX_DURATION = "duration_poll_max";
	}
}