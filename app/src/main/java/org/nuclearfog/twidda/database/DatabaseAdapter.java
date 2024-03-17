package org.nuclearfog.twidda.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.model.Status;

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
	private static final int DB_VERSION = 25;

	/**
	 * database file name
	 */
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
			+ StatusTable.TABLE + "("
			+ StatusTable.ID + " INTEGER PRIMARY KEY,"
			+ StatusTable.USER + " INTEGER NOT NULL,"
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
			+ StatusTable.LANGUAGE + " TEXT,"
			+ StatusTable.EDITED_AT + " INTEGER,"
			+ "FOREIGN KEY(" + StatusTable.USER + ")"
			+ "REFERENCES " + UserTable.TABLE + "(" + UserTable.ID + "),"
			+ "FOREIGN KEY(" + StatusTable.POLL + ")"
			+ "REFERENCES " + PollTable.TABLE + "(" + PollTable.ID + "),"
			+ "FOREIGN KEY(" + StatusTable.LOCATION + ")"
			+ "REFERENCES " + LocationTable.TABLE + "(" + LocationTable.ID + "));";

	/**
	 * SQL query to create a table for trend information
	 */
	private static final String TABLE_TAGS = "CREATE TABLE IF NOT EXISTS "
			+ TagTable.TABLE + "("
			+ TagTable.ID + " INTEGER,"
			+ TagTable.LOCATION + " INTEGER,"
			+ TagTable.INDEX + " INTEGER,"
			+ TagTable.VOL + " INTEGER,"
			+ TagTable.FLAGS + " INTEGER,"
			+ TagTable.TAG_NAME + " TEXT NOT NULL);";

	/**
	 * SQL query to create a table for user logins
	 */
	private static final String TABLE_ACCOUNTS = "CREATE TABLE IF NOT EXISTS "
			+ AccountTable.TABLE + "("
			+ AccountTable.ID + " INTEGER NOT NULL,"
			+ AccountTable.DATE + " INTEGER,"
			+ AccountTable.USERNAME + " TEXT,"
			+ AccountTable.IMAGE + " TEXT,"
			+ AccountTable.ACCESS_TOKEN + " TEXT,"
			+ AccountTable.TOKEN_SECRET + " TEXT,"
			+ AccountTable.HOSTNAME + " TEXT NOT NULL,"
			+ AccountTable.API + " INTEGER,"
			+ AccountTable.CLIENT_ID + " TEXT,"
			+ AccountTable.CLIENT_SECRET + " TEXT,"
			+ AccountTable.BEARER + " TEXT);";

	/**
	 * SQL query to create table for notifications
	 */
	private static final String TABLE_NOTIFICATION = "CREATE TABLE IF NOT EXISTS "
			+ NotificationTable.TABLE + "("
			+ NotificationTable.ID + " INTEGER PRIMARY KEY,"
			+ NotificationTable.SENDER + " INTEGER NOT NULL,"
			+ NotificationTable.RECEIVER + " INTEGER,"
			+ NotificationTable.TIME + " INTEGER,"
			+ NotificationTable.TYPE + " INTEGER,"
			+ NotificationTable.ITEM + " INTEGER,"
			+ "FOREIGN KEY(" + NotificationTable.SENDER + ")"
			+ "REFERENCES " + UserTable.TABLE + "(" + UserTable.ID + "));";

	/**
	 * SQL query to create media table
	 */
	private static final String TABLE_MEDIA = "CREATE TABLE IF NOT EXISTS "
			+ MediaTable.TABLE + "("
			+ MediaTable.KEY + " TEXT PRIMARY KEY,"
			+ MediaTable.TYPE + " INTEGER,"
			+ MediaTable.URL + " TEXT,"
			+ MediaTable.DESCRIPTION + " TEXT,"
			+ MediaTable.BLUR + " TEXT,"
			+ MediaTable.PREVIEW + " TEXT);";

	/**
	 * SQL query to create location table
	 */
	private static final String TABLE_LOCATION = "CREATE TABLE IF NOT EXISTS "
			+ LocationTable.TABLE + "("
			+ LocationTable.ID + " INTEGER PRIMARY KEY,"
			+ LocationTable.COUNTRY + " TEXT,"
			+ LocationTable.COORDINATES + " TEXT,"
			+ LocationTable.PLACE + " TEXT,"
			+ LocationTable.FULLNAME + " TEXT);";

	/**
	 * SQL query to create the emoji table
	 */
	private static final String TABLE_EMOJI = "CREATE TABLE IF NOT EXISTS "
			+ EmojiTable.TABLE + "("
			+ EmojiTable.URL + " TEXT PRIMARY KEY,"
			+ EmojiTable.CATEGORY + " TEXT,"
			+ EmojiTable.CODE + " TEXT);";

	/**
	 * SQL query to create a poll table
	 */
	private static final String TABLE_POLL = "CREATE TABLE IF NOT EXISTS "
			+ PollTable.TABLE + "("
			+ PollTable.ID + " INTEGER PRIMARY KEY,"
			+ PollTable.TABLE + " TEXT,"
			+ PollTable.EXPIRATION + " INTEGER,"
			+ PollTable.OPTIONS + " TEXT);";

	/**
	 * table for status register
	 */
	private static final String TABLE_STATUS_PROPERTIES = "CREATE TABLE IF NOT EXISTS "
			+ StatusPropertiesTable.TABLE + "("
			+ StatusPropertiesTable.STATUS + " INTEGER NOT NULL,"
			+ StatusPropertiesTable.OWNER + " INTEGER,"
			+ StatusPropertiesTable.FLAGS + " INTEGER,"
			+ StatusPropertiesTable.REPOST_ID + " INTEGER,"
			+ "FOREIGN KEY(" + StatusPropertiesTable.STATUS + ")"
			+ "REFERENCES " + StatusTable.TABLE + "(" + StatusTable.ID + "));";

	/**
	 * table for user register
	 */
	private static final String TABLE_USER_PROPERTIES = "CREATE TABLE IF NOT EXISTS "
			+ UserPropertiesTable.TABLE + "("
			+ UserPropertiesTable.USER + " INTEGER NOT NULL,"
			+ UserPropertiesTable.OWNER + " INTEGER,"
			+ UserPropertiesTable.REGISTER + " INTEGER,"
			+ "FOREIGN KEY(" + UserPropertiesTable.USER + ")"
			+ "REFERENCES " + UserTable.TABLE + "(" + UserTable.ID + "));";

	/**
	 * SQL query to create a table for favorite list
	 */
	private static final String TABLE_FAVORITES = "CREATE TABLE IF NOT EXISTS "
			+ FavoriteTable.TABLE + "("
			+ FavoriteTable.ID + " INTEGER NOT NULL,"
			+ FavoriteTable.OWNER + " INTEGER,"
			+ "FOREIGN KEY(" + FavoriteTable.ID + ")"
			+ "REFERENCES " + StatusTable.TABLE + "(" + StatusTable.ID + "));";

	/**
	 * SQL query to create a table for favorite list
	 */
	private static final String TABLE_BOOKMARKS = "CREATE TABLE IF NOT EXISTS "
			+ BookmarkTable.TABLE + "("
			+ BookmarkTable.ID + " INTEGER NOT NULL,"
			+ BookmarkTable.OWNER + " INTEGER,"
			+ "FOREIGN KEY(" + BookmarkTable.ID + ")"
			+ "REFERENCES " + StatusTable.TABLE + "(" + StatusTable.ID + "));";

	/**
	 * SQL query to create a status reply table
	 */
	private static final String TABLE_REPLIES = "CREATE TABLE IF NOT EXISTS "
			+ ReplyTable.TABLE + "("
			+ ReplyTable.ID + " INTEGER NOT NULL,"
			+ ReplyTable.REPLY + " INTEGER,"
			+ ReplyTable.ORDER + " INTEGER,"
			+ "FOREIGN KEY(" + ReplyTable.ID + ")"
			+ "REFERENCES " + StatusTable.TABLE + "(" + StatusTable.ID + "));";

	/**
	 * SQL query to create instance table
	 */
	private static final String TABLE_INSTANCES = "CREATE TABLE IF NOT EXISTS "
			+ InstanceTable.TABLE + "("
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
			+ InstanceTable.TAG_LIMIT + " INTEGER,"
			+ InstanceTable.OPTIONS_LIMIT + " INTEGER,"
			+ InstanceTable.POLL_MIN_DURATION + " INTEGER,"
			+ InstanceTable.POLL_MAX_DURATION + " INTEGER);";

	/**
	 * SQL query to create a push table
	 */
	private static final String TABLE_WEBPUSH = "CREATE TABLE IF NOT EXISTS "
			+ PushTable.TABLE + "("
			+ PushTable.INSTANCE + " TEXT PRIMARY KEY,"
			+ PushTable.HOST + " TEXT,"
			+ PushTable.ID + " INTEGER,"
			+ PushTable.PUB_KEY + " TEXT,"
			+ PushTable.SEC_KEY + " TEXT,"
			+ PushTable.SERVER_KEY + " TEXT,"
			+ PushTable.AUTH_SECRET + " TEXT,"
			+ PushTable.FLAGS + " INTEGER);";

	/**
	 * SQL query to create a user field table
	 *
	 * @since 3.5.6
	 */
	private static final String TALBE_FIELDS = "CREATE TABLE IF NOT EXISTS "
			+ UserFieldTable.TABLE + "("
			+ UserFieldTable.USER + " INTEGER,"
			+ UserFieldTable.KEY + " TEXT NOT NULL,"
			+ UserFieldTable.VALUE + " TEXT,"
			+ UserFieldTable.TIMESTAMP + " INTEGER,"
			+ "FOREIGN KEY(" + UserFieldTable.USER + ")"
			+ "REFERENCES " + UserTable.TABLE + "(" + UserTable.ID + "));";

	private static final String IDX_STATUS_NAME = "idx_status";
	private static final String IDX_STATUS_PROPERTIES_NAME = "idx_status_properties";
	private static final String IDX_USER_PROPERTIES_NAME = "idx_user_properties";
	private static final String IDX_FAVORITS_NAME = "idx_favorits";
	private static final String IDX_BOOKMARKS_NAME = "idx_bookmarks";
	private static final String IDX_REPLIES_NAME = "idx_replies";
	private static final String IDX_FIELDS_NAME = "idx_fields";

	/**
	 * table index for status table
	 */
	private static final String INDX_STATUS = "CREATE INDEX IF NOT EXISTS " + IDX_STATUS_NAME
			+ " ON " + StatusTable.TABLE + "(" + StatusTable.USER + ");";

	/**
	 * table index for status properties
	 */
	private static final String INDX_STATUS_PROPERTIES = "CREATE INDEX IF NOT EXISTS " + IDX_STATUS_PROPERTIES_NAME
			+ " ON " + StatusPropertiesTable.TABLE + "(" + StatusPropertiesTable.OWNER + "," + StatusPropertiesTable.STATUS + ");";

	/**
	 * table index for user properties
	 */
	private static final String INDX_USER_PROPERTIES = "CREATE INDEX IF NOT EXISTS " + IDX_USER_PROPERTIES_NAME
			+ " ON " + UserPropertiesTable.TABLE + "(" + UserPropertiesTable.OWNER + "," + UserPropertiesTable.USER + ");";

	/**
	 * table index for status favorits table
	 *
	 * @since 3.5
	 */
	private static final String INDX_FAVORITE = "CREATE INDEX IF NOT EXISTS " + IDX_FAVORITS_NAME
			+ " ON " + FavoriteTable.TABLE + "(" + FavoriteTable.OWNER + "," + FavoriteTable.ID + ");";

	/**
	 * table index for status bookmarks table
	 *
	 * @since 3.5
	 */
	private static final String INDX_BOOKMARK = "CREATE INDEX IF NOT EXISTS " + IDX_BOOKMARKS_NAME
			+ " ON " + BookmarkTable.TABLE + "(" + BookmarkTable.OWNER + "," + BookmarkTable.ID + ");";

	/**
	 * table index for status replies table
	 *
	 * @since 3.5
	 */
	private static final String INDX_REPLIES = "CREATE INDEX IF NOT EXISTS " + IDX_REPLIES_NAME
			+ " ON " + ReplyTable.TABLE + "(" + ReplyTable.REPLY + "," + ReplyTable.ID + ");";

	/**
	 * table index for status replies table
	 *
	 * @since 3.5.6
	 */
	private static final String INDX_FIELDS = "CREATE INDEX IF NOT EXISTS " + IDX_FIELDS_NAME
			+ " ON " + UserFieldTable.TABLE + "(" + UserFieldTable.USER + ");";

	/**
	 * add mediatable description
	 *
	 * @since 3.4.5
	 */
	private static final String UPDATE_MEDIA_ADD_DESCRIPTION = "ALTER TABLE " + MediaTable.TABLE + " ADD " + MediaTable.DESCRIPTION + " TEXT;";

	/**
	 * add mediatable description
	 *
	 * @since 3.4.5
	 */
	private static final String UPDATE_MEDIA_ADD_BLUR_HASH = "ALTER TABLE " + MediaTable.TABLE + " ADD " + MediaTable.BLUR + " TEXT;";

	/**
	 * add mediatable description
	 *
	 * @since 3.4.5
	 */
	private static final String UPDATE_TAG_ADD_ID = "ALTER TABLE " + TagTable.TABLE + " ADD " + TagTable.ID + " INTEGER;";

	/**
	 * add mediatable description
	 *
	 * @since 3.5.8
	 */
	private static final String UPDATE_TAG_ADD_FLAGS = "ALTER TABLE " + TagTable.TABLE + " ADD " + TagTable.FLAGS + " INTEGER;";

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
		databasePath = context.getDatabasePath(DB_NAME);
		synchronized (this) {
			// fetch database information
			db = context.openOrCreateDatabase(databasePath.toString(), Context.MODE_PRIVATE, null);
			initTables();
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
				File databasePath = context.getDatabasePath(DB_NAME);
				SQLiteDatabase.deleteDatabase(databasePath);
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
	 * delete database tables and create new
	 */
	void resetDatabase() {
		db.execSQL("DROP TABLE IF EXISTS " + UserTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + StatusTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + FavoriteTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + BookmarkTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + TagTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + AccountTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + StatusPropertiesTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + UserPropertiesTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + NotificationTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + MediaTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + LocationTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + EmojiTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + PollTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + InstanceTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + PushTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + ReplyTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + UserFieldTable.TABLE);
		db.execSQL("DROP INDEX IF EXISTS " + IDX_STATUS_NAME);
		db.execSQL("DROP INDEX IF EXISTS " + IDX_STATUS_PROPERTIES_NAME);
		db.execSQL("DROP INDEX IF EXISTS " + IDX_USER_PROPERTIES_NAME);
		db.execSQL("DROP INDEX IF EXISTS " + IDX_FAVORITS_NAME);
		db.execSQL("DROP INDEX IF EXISTS " + IDX_BOOKMARKS_NAME);
		db.execSQL("DROP INDEX IF EXISTS " + IDX_REPLIES_NAME);
		initTables();
	}

	/**
	 * initialize database tables and indexes
	 */
	private void initTables() {
		// create tables if not exist
		db.execSQL(TABLE_USER);
		db.execSQL(TABLE_STATUS);
		db.execSQL(TABLE_FAVORITES);
		db.execSQL(TABLE_BOOKMARKS);
		db.execSQL(TABLE_TAGS);
		db.execSQL(TABLE_ACCOUNTS);
		db.execSQL(TABLE_STATUS_PROPERTIES);
		db.execSQL(TABLE_USER_PROPERTIES);
		db.execSQL(TABLE_NOTIFICATION);
		db.execSQL(TABLE_MEDIA);
		db.execSQL(TABLE_LOCATION);
		db.execSQL(TABLE_EMOJI);
		db.execSQL(TABLE_POLL);
		db.execSQL(TABLE_INSTANCES);
		db.execSQL(TABLE_WEBPUSH);
		db.execSQL(TABLE_REPLIES);
		db.execSQL(TALBE_FIELDS);
		// set initial version
		if (db.getVersion() == 0) {
			db.setVersion(DB_VERSION);
		}
		// update table
		else if (db.getVersion() != DB_VERSION) {
			if (db.getVersion() < 18) {
				db.execSQL(UPDATE_MEDIA_ADD_DESCRIPTION);
				db.setVersion(18);
			}
			if (db.getVersion() < 19) {
				db.execSQL(UPDATE_MEDIA_ADD_BLUR_HASH);
				db.setVersion(19);
			}
			if (db.getVersion() < 21) {
				db.execSQL(UPDATE_TAG_ADD_ID);
				db.setVersion(21);
			}
			if (db.getVersion() < 24) {
				// recreate table
				db.delete(BookmarkTable.TABLE, null, null);
				db.execSQL(TABLE_BOOKMARKS);
				db.setVersion(24);
			}
			if (db.getVersion() < DB_VERSION) {
				db.execSQL(UPDATE_TAG_ADD_FLAGS);
				db.setVersion(DB_VERSION);
			}
		}
		// create index if not exist
		db.execSQL(INDX_STATUS);
		db.execSQL(INDX_STATUS_PROPERTIES);
		db.execSQL(INDX_USER_PROPERTIES);
		db.execSQL(INDX_FAVORITE);
		db.execSQL(INDX_BOOKMARK);
		db.execSQL(INDX_REPLIES);
		db.execSQL(INDX_FIELDS);
	}

	/**
	 * table for user information
	 */
	public interface UserTable {

		/**
		 * table name
		 */
		String TABLE = "users";

		/**
		 * ID of the user
		 */
		String ID = "user_id";

		/**
		 * user name
		 */
		String USERNAME = "username";

		/**
		 * screen name (starting with @)
		 */
		String SCREENNAME = "screen_name";

		/**
		 * description (bio) of the user
		 */
		String DESCRIPTION = "description";

		/**
		 * location attached to profile
		 */
		String LOCATION = "user_location";

		/**
		 * link attached to profile
		 */
		String LINK = "user_url";

		/**
		 * date of account creation
		 */
		String SINCE = "user_created_at";

		/**
		 * link to the original profile image
		 */
		String IMAGE = "profile_image";

		/**
		 * link to the original banner image
		 */
		String BANNER = "banner_image";

		/**
		 * following count
		 */
		String FRIENDS = "following_count";

		/**
		 * follower count
		 */
		String FOLLOWER = "follower_count";

		/**
		 * count of statuses posted by user
		 */
		String STATUSES = "user_status_count";

		/**
		 * count of the statuses favored by the user
		 */
		String FAVORITS = "user_favorite_count";

		/**
		 * emoji keys
		 */
		String EMOJI = "user_emoji_keys";
	}

	/**
	 * table for all status
	 */
	public interface StatusTable {
		/**
		 * table name
		 */
		String TABLE = "statuses";

		/**
		 * ID of the status (Primary key)
		 */
		String ID = "status_id";

		/**
		 * ID of the author
		 */
		String USER = "author_id";

		/**
		 * status text
		 */
		String TEXT = "status_text";

		/**
		 * mentioned usernames
		 */
		String MENTIONS = "mentions";

		/**
		 * media keys
		 */
		String MEDIA = "media_keys";

		/**
		 * emoji keys
		 */
		String EMOJI = "emoji_keys";

		/**
		 * ID of a {@link org.nuclearfog.twidda.model.Poll}
		 */
		String POLL = "poll_id";

		/**
		 * repost count
		 */
		String REPOST = "repost_count";

		/**
		 * favorite count
		 */
		String FAVORITE = "favorite_count";

		/**
		 * reply count
		 */
		String REPLY = "reply_count";

		/**
		 * timestamp of the status
		 */
		String TIME = "created_at";

		/**
		 * API source of the status
		 */
		String SOURCE = "status_source";

		/**
		 * URL of the status
		 */
		String URL = "status_url";

		/**
		 * ID of a location attached to a status
		 */
		String LOCATION = "location_id";

		/**
		 * ID of the replied status
		 */
		String REPLYSTATUS = "reply_status_id";

		/**
		 * ID of the replied user
		 */
		String REPLYUSER = "reply_user_id";

		/**
		 * name of the replied user
		 */
		String REPLYNAME = "reply_user_name";

		/**
		 * ID of the embedded (reposted) status
		 */
		String EMBEDDED = "embedded_status_id";

		/**
		 * language of the status
		 */
		String LANGUAGE = "status_language";

		/**
		 * timestamp of the last edit
		 */
		String EDITED_AT = "status_edited_at";
	}

	/**
	 * table for favored statuses of an user
	 */
	public interface FavoriteTable {
		/**
		 * table name
		 */
		String TABLE = "favorits";

		/**
		 * ID of the status referencing {@link StatusTable#ID}
		 */
		String ID = "status_id";

		/**
		 * ID of the user of this favored status
		 */
		String OWNER = "owner_id";
	}

	/**
	 * status bookmark table
	 */
	public interface BookmarkTable {
		/**
		 * table name
		 */
		String TABLE = "bookmarks";

		/**
		 * ID of the status referencing {@link StatusTable#ID}
		 */
		String ID = "status_id";

		/**
		 * ID of the user of this bookmarks
		 */
		String OWNER = "owner_id";
	}

	/**
	 * status reply table
	 */
	public interface ReplyTable {

		/**
		 * table name
		 */
		String TABLE = "replies";

		/**
		 * id of the replied status
		 */
		String REPLY = "reply_id";

		/**
		 * ID of the reply referencing {@link StatusTable#ID}
		 */
		String ID = "status_id";

		/**
		 * position in the reply thread
		 */
		String ORDER = "status_index";
	}

	/**
	 * table for trends and trending tags
	 */
	public interface TagTable {
		/**
		 * table name
		 */
		String TABLE = "tags";

		/**
		 * ID of the tag (may be 0)
		 */
		String ID = "tag_id";

		/**
		 * tag name
		 */
		String TAG_NAME = "tag_name";

		/**
		 * Location ID
		 */
		String LOCATION = "location_id";

		/**
		 * rank of the tag
		 */
		String INDEX = "tag_rank";

		/**
		 * popularity count
		 */
		String VOL = "activity";

		/**
		 * status flags
		 */
		String FLAGS = "tag_flags";

		/**
		 * indicates that a tag is followed by the current user
		 */
		int FLAG_FOLLOWED = 1;
	}

	/**
	 * Table for multi user login information
	 */
	public interface AccountTable {
		/**
		 * SQL table name
		 */
		String TABLE = "accounts";

		/**
		 * social network host
		 */
		String HOSTNAME = "hostname";

		/**
		 * used API
		 */
		String API = "account_api";

		/**
		 * ID of the user referencing {@link UserTable#ID}
		 */
		String ID = "user_id";

		/**
		 * name of the account profile
		 */
		String USERNAME = "screen_name";

		/**
		 * profile thumbnail url of the account profile
		 */
		String IMAGE = "profile_image_url";

		/**
		 * date of login
		 */
		String DATE = "login_created_at";

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
	public interface StatusPropertiesTable {
		/**
		 * SQL table name
		 */
		String TABLE = "status_properties";

		/**
		 * ID of the status this register referencing {@link StatusTable#ID}
		 */
		String STATUS = "status_id";

		/**
		 * ID of the owner (user) of this register
		 */
		String OWNER = "owner_id";

		/**
		 * Register with status bits
		 */
		String FLAGS = "status_flags";

		/**
		 * ID of the repost of the current user (if exists)
		 */
		String REPOST_ID = "repost_id";

		/**
		 * flag indicates that a status was favorited by the current user
		 */
		int MASK_STATUS_FAVORITED = 1;

		/**
		 * flag indicates that a status was reposted by the current user
		 */
		int MASK_STATUS_REPOSTED = 1 << 1;

		/**
		 * flag indicates that a status exists in the home timeline of the current user
		 */
		int MASK_STATUS_HOME_TIMELINE = 1 << 2;

		/**
		 * flag indicates that a status exists in the notification of the current user
		 */
		int MASK_STATUS_NOTIFICATION = 1 << 3;

		/**
		 * flag indicates that a status exists in an user timeline
		 */
		int MASK_STATUS_USER_TIMELINE = 1 << 4;

		/**
		 * flag indicates that a status exists in the reply of a status
		 */
		int MASK_STATUS_REPLY = 1 << 5;

		/**
		 * flag indicates that a status contains spoiler
		 */
		int MASK_STATUS_SPOILER = 1 << 7;

		/**
		 * flag indicates that a status contains sensitive media
		 */
		int MASK_STATUS_SENSITIVE = 1 << 8;

		/**
		 * flag indicates that a status was hidden by the current user
		 */
		int MASK_STATUS_HIDDEN = 1 << 9;

		/**
		 * flag indicated that a status is bookmarked by the current user
		 */
		int MASK_STATUS_BOOKMARKED = 1 << 10;

		/**
		 * status visibility flag {@link Status#VISIBLE_UNLISTED}
		 */
		int MASK_STATUS_VISIBILITY_UNLISTED = 1 << 11;

		/**
		 * status visibility flag {@link Status#VISIBLE_PRIVATE}
		 */
		int MASK_STATUS_VISIBILITY_PRIVATE = 2 << 11;

		/**
		 * status visibility flag {@link Status#VISIBLE_DIRECT}
		 */
		int MASK_STATUS_VISIBILITY_DIRECT = 3 << 11;

		/**
		 * status is pinned to profile
		 */
		int MASK_STATUS_PINNED = 1 << 13;

		/**
		 * status is from an user timeline (replies included)
		 */
		int MASK_STATUS_USER_REPLY = 1 << 14;
	}

	/**
	 * table for user register
	 */
	public interface UserPropertiesTable {
		/**
		 * SQL table name
		 */
		String TABLE = "user_properties";

		/**
		 * ID of the user referencing {@link UserTable#ID}
		 */
		String USER = "user_id";

		/**
		 * ID of the current user accessing the database
		 */
		String OWNER = "owner_id";

		/**
		 * Register with status bits
		 */
		String REGISTER = "user_flags";

		/**
		 * flag indicates that an user is verified
		 */
		int MASK_USER_VERIFIED = 1;

		/**
		 * flag indicates that an user is locked/private
		 */
		int MASK_USER_PRIVATE = 1 << 1;

		/**
		 * flag indicates that the statuses of an user are excluded from timeline
		 */
		int MASK_USER_FILTERED = 1 << 3;

		/**
		 * flag indicates that the user has a default profile image
		 */
		int MASK_USER_DEFAULT_IMAGE = 1 << 4;

		/**
		 * flag indicates that the user'S posts are public indexed
		 */
		int MASK_USER_INDEXABLE = 1 << 5;

		/**
		 * flag indicates that the user's information are public discoverable
		 */
		int MASK_USER_DISCOVERABLE = 1 << 6;

		/**
		 * flag indicates that the user is a bot
		 */
		int MASK_USER_BOT = 1 << 7;

		/**
		 * flag indicates that the user represents a group
		 */
		int MASK_USER_GROUP = 1 << 8;
	}

	/**
	 * table for user fields
	 */
	public interface UserFieldTable {

		/**
		 * table name
		 */
		String TABLE = "userfield";

		/**
		 * user ID of the owner
		 */
		String USER = "field_user_id";

		/**
		 * field key name
		 */
		String KEY = "field_key";

		/**
		 * field value
		 */
		String VALUE = "field_value";

		/**
		 * field timestamp of verification or '0' if not defined
		 */
		String TIMESTAMP = "field_timestamp";
	}

	/**
	 * table for notifications
	 */
	public interface NotificationTable {

		/**
		 * table name
		 */
		String TABLE = "notifications";

		/**
		 * ID of the notification (primary key)
		 */
		String ID = "notification_id";

		/**
		 * user ID of the receiver (user ID)
		 */
		String RECEIVER = "owner_id";

		/**
		 * user ID of the sender referencing {@link UserTable#ID}
		 */
		String SENDER = "user_id";

		/**
		 * creation time of the notification
		 */
		String TIME = "received_at";

		/**
		 * universal ID (status ID, list ID...)
		 */
		String ITEM = "item_id";

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
		String TABLE = "media";

		/**
		 * key to identify the media entry (primary key)
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
		String TABLE = "locations";

		/**
		 * location ID (primary key)
		 */
		String ID = "location_id";

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
		String TABLE = "emojis";

		/**
		 * emoji image url (primary key)
		 */
		String URL = "image_url";

		/**
		 * emoji code
		 */
		String CODE = "emoji_code";

		/**
		 * emoji category
		 */
		String CATEGORY = "emoji_category";
	}

	/**
	 * Table for status poll
	 */
	public interface PollTable {

		/**
		 * table name
		 */
		String TABLE = "polls";

		/**
		 * poll ID (primary key)
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
		String TABLE = "instances";

		/**
		 * domain name (primary key)
		 */
		String DOMAIN = "domain";

		/**
		 * timestamp of the last update
		 */
		String TIMESTAMP = "timestamp";

		/**
		 * title of the instance
		 */
		String TITLE = "title";

		/**
		 * API verison
		 */
		String VERSION = "version";

		/**
		 * instance description
		 */
		String DESCRIPTION = "description";

		/**
		 * instance flags
		 */
		String FLAGS = "instance_flags";

		/**
		 * tag follow limit
		 */
		String TAG_LIMIT = "limit_follow_tag";

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

	/**
	 *
	 */
	public interface PushTable {

		/**
		 * table name
		 */
		String TABLE = "web_push";

		/**
		 * web push id (hash vaule of user_id@hostname.example) (primary Key)
		 */
		String INSTANCE = "user_url";

		/**
		 * ID of the push subscription (not unique)
		 */
		String ID = "push_id";

		/**
		 *
		 */
		String HOST = "push_host";

		/**
		 *
		 */
		String SERVER_KEY = "server_key";

		/**
		 *
		 */
		String AUTH_SECRET = "auth_secret";

		/**
		 *
		 */
		String PUB_KEY = "public_key";

		/**
		 *
		 */
		String SEC_KEY = "private_key";

		/**
		 *
		 */
		String FLAGS = "push_flags";

		/**
		 *
		 */
		int MASK_POLICY_FOLLOWING = 1;

		/**
		 *
		 */
		int MASK_POLICY_FOLLOWER = 2;

		/**
		 *
		 */
		int MASK_POLICY_ALL = 3;

		/**
		 *
		 */
		int MASK_MENTION = 1 << 3;

		/**
		 *
		 */
		int MASK_STATUS = 1 << 4;

		/**
		 *
		 */
		int MASK_REPOST = 1 << 5;

		/**
		 *
		 */
		int MASK_FOLLOWING = 1 << 6;

		/**
		 *
		 */
		int MASK_REQUEST = 1 << 7;

		/**
		 *
		 */
		int MASK_FAVORITE = 1 << 8;

		/**
		 *
		 */
		int MASK_POLL = 1 << 9;

		/**
		 *
		 */
		int MASK_MODIFIED = 1 << 10;
	}
}