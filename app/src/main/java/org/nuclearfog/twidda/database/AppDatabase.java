package org.nuclearfog.twidda.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.database.DatabaseAdapter.AccountTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.BookmarkTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.EmojiTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.FavoriteTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.InstanceTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.LocationTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.MediaTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.NotificationTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.PollTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.StatusRegisterTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.StatusTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.HashtagTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.UserRegisterTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.UserTable;
import org.nuclearfog.twidda.database.impl.DatabaseAccount;
import org.nuclearfog.twidda.database.impl.DatabaseEmoji;
import org.nuclearfog.twidda.database.impl.DatabaseInstance;
import org.nuclearfog.twidda.database.impl.DatabaseLocation;
import org.nuclearfog.twidda.database.impl.DatabaseMedia;
import org.nuclearfog.twidda.database.impl.DatabaseNotification;
import org.nuclearfog.twidda.database.impl.DatabasePoll;
import org.nuclearfog.twidda.database.impl.DatabaseStatus;
import org.nuclearfog.twidda.database.impl.DatabaseHashtag;
import org.nuclearfog.twidda.database.impl.DatabaseUser;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Instance;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.Hashtag;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.lists.Accounts;
import org.nuclearfog.twidda.model.lists.Notifications;
import org.nuclearfog.twidda.model.lists.Statuses;
import org.nuclearfog.twidda.model.lists.Trends;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * SQLite database class to store and load status, messages, trends and user information
 *
 * @author nuclearfog
 */
public class AppDatabase {

	/**
	 * flag indicates that a status was favorited by the current user
	 */
	public static final int MASK_STATUS_FAVORITED = 1;

	/**
	 * flag indicates that a status was reposted by the current user
	 */
	public static final int MASK_STATUS_REPOSTED = 1 << 1;

	/**
	 * flag indicates that a status exists in the home timeline of the current user
	 */
	private static final int MASK_STATUS_HOME_TIMELINE = 1 << 2;

	/**
	 * flag indicates that a status exists in the notification of the current user
	 */
	private static final int MASK_STATUS_NOTIFICATION = 1 << 3;

	/**
	 * flag indicates that a status exists in an user timeline
	 */
	private static final int MASK_STATUS_USER_TIMELINE = 1 << 4;

	/**
	 * flag indicates that a status exists in the reply of a status
	 */
	private static final int MASK_STATUS_REPLY = 1 << 5;

	/**
	 * flag indicates that a status contains spoiler
	 */
	public static final int MASK_STATUS_SPOILER = 1 << 7;

	/**
	 * flag indicates that a status contains sensitive media
	 */
	public static final int MASK_STATUS_SENSITIVE = 1 << 8;

	/**
	 * flag indicates that a status was hidden by the current user
	 */
	public static final int MASK_STATUS_HIDDEN = 1 << 9;

	/**
	 * flag indicated that a status is bookmarked by the current user
	 */
	public static final int MASK_STATUS_BOOKMARKED = 1 << 10;

	/**
	 * status visibility flag {@link Status#VISIBLE_UNLISTED}
	 */
	public static final int MASK_STATUS_VISIBILITY_UNLISTED = 1 << 11;

	/**
	 * status visibility flag {@link Status#VISIBLE_PRIVATE}
	 */
	public static final int MASK_STATUS_VISIBILITY_PRIVATE = 2 << 11;

	/**
	 * status visibility flag {@link Status#VISIBLE_DIRECT}
	 */
	public static final int MASK_STATUS_VISIBILITY_DIRECT = 3 << 11;

	/**
	 * flag indicates that an user is verified
	 */
	public static final int MASK_USER_VERIFIED = 1;

	/**
	 * flag indicates that an user is locked/private
	 */
	public static final int MASK_USER_PRIVATE = 1 << 1;

	/**
	 * flag indicates that the current user has sent a follow request to an user
	 */
	public static final int MASK_USER_FOLLOW_REQUESTED = 1 << 2;

	/**
	 * flag indicates that the statuses of an user are excluded from timeline
	 */
	private static final int MASK_USER_FILTERED = 1 << 3;

	/**
	 * flag indicates that the user has a default profile image
	 */
	public static final int MASK_USER_DEFAULT_IMAGE = 1 << 4;

	/**
	 * used if no ID is defined
	 */
	private static final long NO_ID = -1L;

	/**
	 * query to create status table with user and register columns
	 */
	private static final String STATUS_SUBQUERY = StatusTable.NAME
			+ " INNER JOIN " + UserTable.NAME
			+ " ON " + StatusTable.NAME + "." + StatusTable.USER + "=" + UserTable.NAME + "." + UserTable.ID
			+ " INNER JOIN " + UserRegisterTable.NAME
			+ " ON " + StatusTable.NAME + "." + StatusTable.USER + "=" + UserRegisterTable.NAME + "." + UserRegisterTable.USER
			+ " INNER JOIN " + StatusRegisterTable.NAME
			+ " ON " + StatusTable.NAME + "." + StatusTable.ID + "=" + StatusRegisterTable.NAME + "." + StatusRegisterTable.STATUS;

	/**
	 * subquery to get user information
	 */
	private static final String USER_SUBQUERY = UserTable.NAME
			+ " INNER JOIN " + UserRegisterTable.NAME
			+ " ON " + UserTable.NAME + "." + UserTable.ID + "=" + UserRegisterTable.NAME + "." + UserRegisterTable.USER;

	/**
	 * subquery used to get notification
	 */
	private static final String NOTIFICATION_SUBQUERY = NotificationTable.NAME
			+ " INNER JOIN(" + USER_SUBQUERY + ")" + UserTable.NAME
			+ " ON " + NotificationTable.NAME + "." + NotificationTable.USER + "=" + UserTable.NAME + "." + UserTable.ID;

	/**
	 * SQL query to get home timeline status
	 */
	private static final String HOME_QUERY = "SELECT * FROM(" + STATUS_SUBQUERY + ")"
			+ " WHERE " + StatusRegisterTable.NAME + "." + StatusRegisterTable.REGISTER + "&" + MASK_STATUS_HOME_TIMELINE + " IS NOT 0"
			+ " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.REGISTER + "&" + MASK_USER_FILTERED + " IS 0"
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.OWNER + "=?"
			+ " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
			+ " ORDER BY " + StatusTable.TIME + " DESC"
			+ " LIMIT ?;";

	/**
	 * SQL query to get status of an user
	 */
	private static final String USER_STATUS_QUERY = "SELECT * FROM(" + STATUS_SUBQUERY + ")"
			+ " WHERE " + StatusRegisterTable.NAME + "." + StatusRegisterTable.REGISTER + "&" + MASK_STATUS_USER_TIMELINE + " IS NOT 0"
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.OWNER + "=?"
			+ " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
			+ " AND " + StatusTable.NAME + "." + StatusTable.USER + "=?"
			+ " ORDER BY " + StatusTable.TIME + " DESC"
			+ " LIMIT ?;";

	/**
	 * SQL query to get status favored by an user
	 */
	private static final String USER_FAVORIT_QUERY = "SELECT * FROM(" + STATUS_SUBQUERY + ")"
			+ " INNER JOIN " + FavoriteTable.NAME
			+ " ON " + StatusTable.NAME + "." + StatusTable.ID + "=" + FavoriteTable.NAME + "." + FavoriteTable.STATUS
			+ " WHERE " + FavoriteTable.NAME + "." + FavoriteTable.OWNER + "=?"
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.OWNER + "=?"
			+ " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
			+ " ORDER BY " + StatusTable.TIME + " DESC"
			+ " LIMIT ?;";

	/**
	 * SQL query to get status favored by an user
	 */
	private static final String USER_BOOKMARKS_QUERY = "SELECT * FROM(" + STATUS_SUBQUERY + ")"
			+ " INNER JOIN " + BookmarkTable.NAME
			+ " ON " + StatusTable.NAME + "." + StatusTable.ID + "=" + BookmarkTable.NAME + "." + BookmarkTable.STATUS
			+ " WHERE " + BookmarkTable.NAME + "." + BookmarkTable.OWNER + "=?"
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.OWNER + "=?"
			+ " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
			+ " ORDER BY " + StatusTable.TIME + " DESC"
			+ " LIMIT ?;";

	/**
	 * SQL query to get a single status specified by an ID
	 */
	private static final String SINGLE_STATUS_QUERY = "SELECT * FROM " + STATUS_SUBQUERY
			+ " WHERE " + StatusTable.NAME + "." + StatusTable.ID + "=?"
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.OWNER + "=?"
			+ " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
			+ " LIMIT 1;";

	/**
	 * query to get user information
	 */
	private static final String SINGLE_USER_QUERY = "SELECT * FROM " + USER_SUBQUERY
			+ " WHERE " + UserTable.NAME + "." + UserTable.ID + "=?"
			+ " LIMIT 1;";

	/**
	 * SQL query to get replies of a status specified by a status ID
	 */
	private static final String REPLY_QUERY = "SELECT * FROM(" + STATUS_SUBQUERY + ")"
			+ " WHERE " + StatusTable.NAME + "." + StatusTable.REPLYSTATUS + "=?"
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.OWNER + "=?"
			+ " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.REGISTER + "&" + MASK_STATUS_REPLY + " IS NOT 0"
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.REGISTER + "&" + MASK_STATUS_HIDDEN + " IS 0"
			+ " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.REGISTER + "&" + MASK_USER_FILTERED + " IS 0"
			+ " ORDER BY " + StatusTable.TIME + " DESC"
			+ " LIMIT ?;";

	/**
	 * SQL query to get notifications
	 */
	private static final String NOTIFICATION_QUERY = "SELECT * FROM " + NOTIFICATION_SUBQUERY
			+ " WHERE " + NotificationTable.NAME + "." + NotificationTable.OWNER + "=?"
			+ " ORDER BY " + NotificationTable.TIME + " DESC"
			+ " LIMIT ?;";

	/**
	 * SQL Query to get a single notification
	 */
	private static final String SINGLE_NOTIFICATION_QUERY = "SELECT * FROM " + NOTIFICATION_SUBQUERY
			+ " WHERE " + NotificationTable.NAME + "." + NotificationTable.ID + "=?"
			+ " LIMIT 1;";

	/**
	 * select status entries from favorite table matching status ID
	 * this status can be favored by multiple users
	 */
	private static final String FAVORITE_SELECT_STATUS = FavoriteTable.STATUS + "=?";

	/**
	 * select all statuses from favorite table favored by given user
	 */
	private static final String FAVORITE_SELECT_OWNER = FavoriteTable.OWNER + "=?";

	/**
	 * select status entries from favorite table matching status ID
	 * this status can be favored by multiple users
	 */
	private static final String BOOKMARK_SELECT_STATUS = BookmarkTable.STATUS + "=?";

	/**
	 * select all statuses from favorite table favored by given user
	 */
	private static final String BOOKMARK_SELECT_OWNER = BookmarkTable.OWNER + "=?";

	/**
	 * select specific status from favorite table
	 */
	private static final String FAVORITE_SELECT = FAVORITE_SELECT_STATUS + " AND " + FAVORITE_SELECT_OWNER;

	/**
	 * select specific status from favorite table
	 */
	private static final String BOOKMARK_SELECT = BOOKMARK_SELECT_STATUS + " AND " + BOOKMARK_SELECT_OWNER;

	/**
	 * select trends from trend table with given world ID
	 */
	private static final String TREND_SELECT = HashtagTable.ID + "=?";

	/**
	 * select status from status table matching ID
	 */
	private static final String STATUS_SELECT = StatusTable.NAME + "." + StatusTable.ID + "=?";

	/**
	 * select notification from notification table using status ID
	 */
	private static final String NOTIFICATION_SELECT = NotificationTable.NAME + "." + NotificationTable.ID + "=?";

	/**
	 * select notification from notification table using status ID
	 */
	private static final String NOTIFICATION_STATUS_SELECT = NotificationTable.NAME + "." + NotificationTable.ITEM + "=?";

	/**
	 * selection to get status flag register
	 */
	private static final String STATUS_REG_SELECT = StatusRegisterTable.STATUS + "=? AND " + StatusRegisterTable.OWNER + "=?";

	/**
	 * selection to get a single media entry
	 */
	private static final String MEDIA_SELECT = MediaTable.KEY + "=?";

	/**
	 * selection to get a single emoji entry
	 */
	private static final String EMOJI_SELECT = EmojiTable.CODE + "=?";

	/**
	 * selection to get location
	 */
	private static final String LOCATION_SELECT = LocationTable.ID + "=?";

	/**
	 * selection to get user flag register
	 */
	private static final String USER_REG_SELECT = UserRegisterTable.USER + "=? AND " + UserRegisterTable.OWNER + "=?";

	/**
	 * selection for account entry
	 */
	private static final String ACCOUNT_SELECTION = AccountTable.ID + "=?";

	/**
	 * selection for poll entry
	 */
	private static final String POLL_SELECTION = PollTable.ID + "=?";

	/**
	 * selection for instance entry
	 */
	private static final String INSTANCE_SELECTION = InstanceTable.DOMAIN + "=?";

	/**
	 * column projection for user flag register
	 */
	private static final String[] USER_REG_COLUMN = {UserRegisterTable.REGISTER};

	/**
	 * column projection for status flag register
	 */
	private static final String[] STATUS_REG_COLUMN = {StatusRegisterTable.REGISTER};

	/**
	 * default sort order for logins
	 */
	private static final String SORT_BY_CREATION = AccountTable.DATE + " DESC";

	/**
	 * limit for accessing a single row
	 */
	private static final String SINGLE_ITEM = "1";

	/**
	 * database lock
	 */
	private static final Object LOCK = new Object();

	/**
	 * limit of database entries
	 */
	private GlobalSettings settings;

	/**
	 * adapter for the database backend
	 */
	private DatabaseAdapter adapter;

	/**
	 * @param context activity context
	 */
	public AppDatabase(Context context) {
		adapter = DatabaseAdapter.getInstance(context);
		settings = GlobalSettings.get(context);
	}

	/**
	 * save home timeline
	 *
	 * @param statuses status from home timeline
	 */
	public void saveHomeTimeline(Statuses statuses) {
		synchronized (LOCK) {
			if (!statuses.isEmpty()) {
				SQLiteDatabase db = adapter.getDbWrite();
				for (Status status : statuses)
					saveStatus(status, db, MASK_STATUS_HOME_TIMELINE);
				adapter.commit();
			}
		}
	}

	/**
	 * save user timeline
	 *
	 * @param statuses user timeline
	 */
	public void saveUserTimeline(Statuses statuses) {
		synchronized (LOCK) {
			if (!statuses.isEmpty()) {
				SQLiteDatabase db = adapter.getDbWrite();
				for (Status status : statuses)
					saveStatus(status, db, MASK_STATUS_USER_TIMELINE);
				adapter.commit();
			}
		}
	}

	/**
	 * save user favorite timeline
	 *
	 * @param statuses status favored by user
	 * @param ownerId  user ID
	 */
	public void saveFavoriteTimeline(Statuses statuses, long ownerId) {
		synchronized (LOCK) {
			SQLiteDatabase db = adapter.getDbWrite();
			// delete old favorits
			String[] delArgs = {Long.toString(ownerId)};
			db.delete(FavoriteTable.NAME, FAVORITE_SELECT_OWNER, delArgs);

			if (!statuses.isEmpty()) {
				for (Status status : statuses) {
					saveStatus(status, db, 0);
					saveFavorite(status.getId(), ownerId, db);
				}
			}
			adapter.commit();
		}
	}

	/**
	 * save user bookmark timeline
	 *
	 * @param statuses bookmarked statuses
	 * @param ownerId  id of the owner
	 */
	public void saveBookmarkTimeline(Statuses statuses, long ownerId) {
		synchronized (LOCK) {
			SQLiteDatabase db = adapter.getDbWrite();
			// delete old favorits
			String[] delArgs = {Long.toString(ownerId)};
			db.delete(BookmarkTable.NAME, BOOKMARK_SELECT_OWNER, delArgs);

			if (!statuses.isEmpty()) {
				for (Status status : statuses) {
					saveStatus(status, db, 0);
					saveBookmark(status.getId(), ownerId, db);
				}
			}
			adapter.commit();
		}
	}

	/**
	 * store replies of a status
	 *
	 * @param statuses status replies
	 */
	public void saveReplyTimeline(Statuses statuses) {
		synchronized (LOCK) {
			if (!statuses.isEmpty()) {
				SQLiteDatabase db = adapter.getDbWrite();
				for (Status status : statuses)
					saveStatus(status, db, MASK_STATUS_REPLY);
				adapter.commit();
			}
		}
	}

	/**
	 * save notifications to database
	 */
	public void saveNotifications(List<Notification> notifications) {
		synchronized (LOCK) {
			if (!notifications.isEmpty()) {
				SQLiteDatabase db = adapter.getDbWrite();
				for (Notification notification : notifications) {
					ContentValues column = new ContentValues();
					column.put(NotificationTable.ID, notification.getId());
					column.put(NotificationTable.TIME, notification.getTimestamp());
					column.put(NotificationTable.TYPE, notification.getType());
					column.put(NotificationTable.OWNER, settings.getLogin().getId());
					column.put(NotificationTable.USER, notification.getUser().getId());
					saveUser(notification.getUser(), db, SQLiteDatabase.CONFLICT_IGNORE);
					// add status
					if (notification.getStatus() != null) {
						saveStatus(notification.getStatus(), db, MASK_STATUS_NOTIFICATION);
						column.put(NotificationTable.ITEM, notification.getStatus().getId());
					}
					db.insertWithOnConflict(NotificationTable.NAME, null, column, SQLiteDatabase.CONFLICT_REPLACE);
				}
				adapter.commit();
			}
		}
	}

	/**
	 * store location specific trends
	 *
	 * @param hashtags List of Trends
	 */
	public void saveTrends(List<Hashtag> hashtags) {
		synchronized (LOCK) {
			String[] args = {Long.toString(settings.getTrendLocation().getId())};
			SQLiteDatabase db = adapter.getDbWrite();
			db.delete(HashtagTable.NAME, TREND_SELECT, args);
			for (Hashtag hashtag : hashtags) {
				ContentValues column = new ContentValues(4);
				column.put(HashtagTable.ID, hashtag.getLocationId());
				column.put(HashtagTable.VOL, hashtag.getPopularity());
				column.put(HashtagTable.TREND, hashtag.getName());
				column.put(HashtagTable.INDEX, hashtag.getRank());
				db.insert(HashtagTable.NAME, null, column);
			}
			adapter.commit();
		}
	}

	/**
	 * Store user information
	 */
	public void saveUser(User user) {
		synchronized (LOCK) {
			SQLiteDatabase db = adapter.getDbWrite();
			saveUser(user, db, SQLiteDatabase.CONFLICT_REPLACE);
			adapter.commit();
		}
	}

	/**
	 * update status
	 *
	 * @param status status to update
	 */
	public void saveStatus(Status status) {
		synchronized (LOCK) {
			SQLiteDatabase db = adapter.getDbWrite();
			saveStatus(status, db, 0);
			if (status.getEmbeddedStatus() != null)
				saveStatus(status.getEmbeddedStatus(), db, 0);
			adapter.commit();
		}
	}

	/**
	 * save instance information
	 *
	 * @param instance instance information
	 */
	public void saveInstance(Instance instance) {
		synchronized (LOCK) {
			SQLiteDatabase db = adapter.getDbWrite();
			saveInstance(instance, db);
			adapter.commit();
		}
	}

	/**
	 * save user login
	 *
	 * @param account login information
	 */
	public void saveLogin(Account account) {
		synchronized (LOCK) {
			ContentValues column = new ContentValues(9);
			column.put(AccountTable.ID, account.getId());
			column.put(AccountTable.DATE, account.getTimestamp());
			column.put(AccountTable.HOSTNAME, account.getHostname());
			column.put(AccountTable.CLIENT_ID, account.getConsumerToken());
			column.put(AccountTable.CLIENT_SECRET, account.getConsumerSecret());
			column.put(AccountTable.API, account.getConfiguration().getAccountType());
			column.put(AccountTable.ACCESS_TOKEN, account.getOauthToken());
			column.put(AccountTable.TOKEN_SECRET, account.getOauthSecret());
			column.put(AccountTable.BEARER, account.getBearerToken());
			SQLiteDatabase db = adapter.getDbWrite();
			db.insertWithOnConflict(AccountTable.NAME, "", column, SQLiteDatabase.CONFLICT_REPLACE);
			if (account.getUser() != null) {
				saveUser(account.getUser(), db, SQLiteDatabase.CONFLICT_IGNORE);
			}
			adapter.commit();
		}
	}

	/**
	 * store ID of a favorited status to the current users favorite list
	 *
	 * @param status favorited status
	 */
	public void saveToFavorits(Status status) {
		synchronized (LOCK) {
			if (status.getEmbeddedStatus() != null)
				status = status.getEmbeddedStatus();
			SQLiteDatabase db = adapter.getDbWrite();
			saveStatus(status, db, 0);
			saveFavorite(status.getId(), settings.getLogin().getId(), db);
			adapter.commit();
		}
	}

	/**
	 * store ID of a status to the current users bookmarks
	 *
	 * @param status favorited status
	 */
	public void saveToBookmarks(Status status) {
		synchronized (LOCK) {
			if (status.getEmbeddedStatus() != null)
				status = status.getEmbeddedStatus();
			SQLiteDatabase db = adapter.getDbWrite();
			saveStatus(status, db, 0);
			saveBookmark(status.getId(), settings.getLogin().getId(), db);
			adapter.commit();
		}
	}

	/**
	 * save emojis to database
	 *
	 * @param emojis list of emojis
	 */
	public void saveEmojis(List<Emoji> emojis) {
		synchronized (LOCK) {
			SQLiteDatabase db = adapter.getDbWrite();
			for (Emoji emoji : emojis) {
				saveEmoji(emoji, db);
			}
			adapter.commit();
		}
	}

	/**
	 * load home timeline
	 *
	 * @return home timeline
	 */
	public Statuses getHomeTimeline() {
		synchronized (LOCK) {
			String homeStr = Long.toString(settings.getLogin().getId());
			String[] args = {homeStr, homeStr, Integer.toString(settings.getListSize())};

			SQLiteDatabase db = adapter.getDbRead();
			Cursor cursor = db.rawQuery(HOME_QUERY, args);
			return getStatuses(cursor, db);
		}
	}

	/**
	 * load user timeline
	 *
	 * @param userID user ID
	 * @return user timeline
	 */
	public Statuses getUserTimeline(long userID) {
		synchronized (LOCK) {
			String homeStr = Long.toString(settings.getLogin().getId());
			String[] args = {homeStr, homeStr, Long.toString(userID), Integer.toString(settings.getListSize())};

			SQLiteDatabase db = adapter.getDbRead();
			Cursor cursor = db.rawQuery(USER_STATUS_QUERY, args);
			return getStatuses(cursor, db);
		}
	}

	/**
	 * load favorite timeline
	 *
	 * @param ownerID user ID
	 * @return favorite timeline
	 */
	public Statuses getUserFavorites(long ownerID) {
		synchronized (LOCK) {
			String homeStr = Long.toString(settings.getLogin().getId());
			String[] args = {Long.toString(ownerID), homeStr, homeStr, Integer.toString(settings.getListSize())};

			SQLiteDatabase db = adapter.getDbRead();
			Cursor cursor = db.rawQuery(USER_FAVORIT_QUERY, args);
			Statuses statuses = getStatuses(cursor, db);
			statuses.setNextCursor(Statuses.NO_ID);
			return statuses;
		}
	}

	/**
	 * load status bookmarks
	 *
	 * @param ownerID user ID
	 * @return bookmark timeline
	 */
	public Statuses getUserBookmarks(long ownerID) {
		synchronized (LOCK) {
			String homeStr = Long.toString(settings.getLogin().getId());
			String[] args = {Long.toString(ownerID), homeStr, homeStr, Integer.toString(settings.getListSize())};

			SQLiteDatabase db = adapter.getDbRead();
			Cursor cursor = db.rawQuery(USER_BOOKMARKS_QUERY, args);
			Statuses statuses = getStatuses(cursor, db);
			statuses.setNextCursor(Statuses.NO_ID);
			return statuses;
		}
	}

	/**
	 * get reply timeline
	 *
	 * @param id status ID
	 * @return status reply timeline
	 */
	public Statuses getReplies(long id) {
		synchronized (LOCK) {
			String homeStr = Long.toString(settings.getLogin().getId());
			String[] args = {Long.toString(id), homeStr, homeStr, Integer.toString(settings.getListSize())};

			SQLiteDatabase db = adapter.getDbRead();
			Cursor cursor = db.rawQuery(REPLY_QUERY, args);
			return getStatuses(cursor, db);
		}
	}

	/**
	 * get notifiactions
	 *
	 * @return notification lsit
	 */
	public Notifications getNotifications() {
		synchronized (LOCK) {
			Account login = settings.getLogin();
			String[] args = {Long.toString(login.getId()), Integer.toString(settings.getListSize())};
			SQLiteDatabase db = adapter.getDbRead();
			Notifications result = new Notifications();
			Cursor cursor = db.rawQuery(NOTIFICATION_QUERY, args);
			if (cursor.moveToFirst()) {
				do {
					Notification notification = getNotification(cursor, login);
					result.add(notification);
				} while (cursor.moveToNext());
			}
			cursor.close();
			return result;
		}
	}

	/**
	 * Load trend List
	 *
	 * @return list of trends
	 */
	public Trends getTrends() {
		synchronized (LOCK) {
			String[] args = {Long.toString(settings.getTrendLocation().getId())};
			SQLiteDatabase db = adapter.getDbRead();
			Cursor cursor = db.query(HashtagTable.NAME, DatabaseHashtag.COLUMNS, TREND_SELECT, args, null, null, null);
			Trends trends = new Trends();
			if (cursor.moveToFirst()) {
				do {
					trends.add(new DatabaseHashtag(cursor));
				} while (cursor.moveToNext());
			}
			cursor.close();
			Collections.sort(trends);
			return trends;
		}
	}

	/**
	 * get all user logins
	 *
	 * @return list of all logins
	 */
	public Accounts getLogins() {
		synchronized (LOCK) {
			Accounts result = new Accounts();
			SQLiteDatabase db = adapter.getDbRead();
			Cursor cursor = db.query(AccountTable.NAME, DatabaseAccount.COLUMNS, null, null, null, null, SORT_BY_CREATION);
			if (cursor.moveToFirst()) {
				do {
					DatabaseAccount account = new DatabaseAccount(cursor);
					DatabaseUser user = getUser(account.getId());
					if (user != null) {
						user.setAccountInformation(account);
						account.addUser(user);
					}
					result.add(account);
				} while (cursor.moveToNext());
			}
			cursor.close();
			return result;
		}
	}

	/**
	 * get a single instance of a domain
	 *
	 * @return instance or null if not found
	 */
	@Nullable
	public Instance getInstance() {
		synchronized (LOCK) {
			SQLiteDatabase db = adapter.getDbRead();
			String[] args = {settings.getLogin().getHostname()};
			Instance result = null;
			Cursor cursor = db.query(InstanceTable.NAME, DatabaseInstance.COLUMNS, INSTANCE_SELECTION, args, null, null, null);
			if (cursor.moveToFirst()) {
				result = new DatabaseInstance(cursor);
			}
			cursor.close();
			return result;
		}
	}

	/**
	 * get a single notification by ID
	 *
	 * @param id notification ID
	 * @return notification
	 */
	@Nullable
	public Notification getNotification(long id) {
		synchronized (LOCK) {
			String[] args = {Long.toString(id)};
			SQLiteDatabase db = adapter.getDbRead();
			Cursor cursor = db.rawQuery(SINGLE_NOTIFICATION_QUERY, args);
			Notification notification = null;
			if (cursor.moveToFirst())
				notification = new DatabaseNotification(cursor, settings.getLogin());
			cursor.close();
			return notification;
		}
	}

	/**
	 * get user information
	 *
	 * @param userId ID of user
	 * @return user information or null if not found
	 */
	@Nullable
	public DatabaseUser getUser(long userId) {
		synchronized (LOCK) {
			String[] args = {Long.toString(userId)};
			SQLiteDatabase db = adapter.getDbRead();
			Cursor cursor = db.rawQuery(SINGLE_USER_QUERY, args);
			DatabaseUser user = null;
			if (cursor.moveToFirst())
				user = new DatabaseUser(cursor, settings.getLogin());
			cursor.close();
			return user;
		}
	}

	/**
	 * get status from database
	 *
	 * @param id status ID
	 * @return status or null if not found
	 */
	@Nullable
	public Status getStatus(long id) {
		synchronized (LOCK) {
			String homeStr = Long.toString(settings.getLogin().getId());
			String[] args = {Long.toString(id), homeStr, homeStr};

			SQLiteDatabase db = adapter.getDbRead();
			Status result = null;
			Cursor cursor = db.rawQuery(SINGLE_STATUS_QUERY, args);
			if (cursor.moveToFirst())
				result = getStatus(cursor, db);
			cursor.close();
			return result;
		}
	}

	/**
	 * hide or unhide status
	 *
	 * @param id   ID of the reply
	 * @param hide true to hide this status
	 */
	public void hideStatus(long id, boolean hide) {
		synchronized (LOCK) {
			String[] args = {Long.toString(id), Long.toString(settings.getLogin().getId())};

			SQLiteDatabase db = adapter.getDbWrite();
			int flags = getStatusFlags(db, id);
			if (hide) {
				flags |= MASK_STATUS_HIDDEN;
			} else {
				flags &= ~MASK_STATUS_HIDDEN;
			}
			ContentValues column = new ContentValues(1);
			column.put(StatusRegisterTable.REGISTER, flags);
			db.update(StatusRegisterTable.NAME, column, STATUS_REG_SELECT, args);
			adapter.commit();
		}
	}

	/**
	 * remove status from database
	 *
	 * @param id status ID
	 */
	public void removeStatus(long id) {
		synchronized (LOCK) {
			String[] args = {Long.toString(id)};

			SQLiteDatabase db = adapter.getDbWrite();
			db.delete(StatusTable.NAME, STATUS_SELECT, args);
			db.delete(NotificationTable.NAME, NOTIFICATION_STATUS_SELECT, args);
			db.delete(FavoriteTable.NAME, FAVORITE_SELECT_STATUS, args);
			db.delete(BookmarkTable.NAME, BOOKMARK_SELECT_STATUS, args);
			adapter.commit();
		}
	}

	/**
	 * remove status from database
	 *
	 * @param id status ID
	 */
	public void removeNotification(long id) {
		synchronized (LOCK) {
			String[] args = {Long.toString(id)};

			SQLiteDatabase db = adapter.getDbWrite();
			db.delete(NotificationTable.NAME, NOTIFICATION_SELECT, args);
			adapter.commit();
		}
	}

	/**
	 * remove status from favorites
	 *
	 * @param status status to remove from the favorites
	 */
	public void removeFromFavorite(Status status) {
		synchronized (LOCK) {
			String[] delArgs = {Long.toString(status.getId()), Long.toString(settings.getLogin().getId())};

			if (status.getEmbeddedStatus() != null) {
				status = status.getEmbeddedStatus();
			}
			SQLiteDatabase db = adapter.getDbWrite();
			// get status flags
			int flags = getStatusFlags(db, status.getId());
			flags &= ~MASK_STATUS_FAVORITED; // unset favorite flag
			// update database
			saveStatusFlags(db, status, flags);
			db.delete(FavoriteTable.NAME, FAVORITE_SELECT, delArgs);
			adapter.commit();
		}
	}

	/**
	 * remove status from bookmarks
	 *
	 * @param status status to remove from the bookmarks
	 */
	public void removeFromBookmarks(Status status) {
		synchronized (LOCK) {
			String[] delArgs = {Long.toString(status.getId()), Long.toString(settings.getLogin().getId())};

			if (status.getEmbeddedStatus() != null) {
				status = status.getEmbeddedStatus();
			}
			SQLiteDatabase db = adapter.getDbWrite();
			// get status flags
			int flags = getStatusFlags(db, status.getId());
			flags &= ~MASK_STATUS_BOOKMARKED; // unset bookmark flag
			// update database
			saveStatusFlags(db, status, flags);
			db.delete(BookmarkTable.NAME, BOOKMARK_SELECT, delArgs);
			adapter.commit();
		}
	}

	/**
	 * remove login information from database
	 *
	 * @param id account ID to remove
	 */
	public void removeLogin(long id) {
		synchronized (LOCK) {
			String[] args = {Long.toString(id)};

			SQLiteDatabase db = adapter.getDbWrite();
			db.delete(AccountTable.NAME, ACCOUNT_SELECTION, args);
			adapter.commit();
		}
	}

	/**
	 * check if status exists in database
	 *
	 * @param id status ID
	 * @return true if found
	 */
	public boolean containsStatus(long id) {
		synchronized (LOCK) {
			String[] args = {Long.toString(id)};
			SQLiteDatabase db = adapter.getDbRead();
			Cursor c = db.query(StatusTable.NAME, null, STATUS_SELECT, args, null, null, SINGLE_ITEM);
			boolean result = c.moveToFirst();
			c.close();
			return result;
		}
	}

	/**
	 * check if status exists in database
	 *
	 * @param id status ID
	 * @return true if found
	 */
	public boolean containsLogin(long id) {
		synchronized (LOCK) {
			String[] args = {Long.toString(id)};
			SQLiteDatabase db = adapter.getDbRead();
			Cursor c = db.query(AccountTable.NAME, null, ACCOUNT_SELECTION, args, null, null, SINGLE_ITEM);
			boolean result = c.moveToFirst();
			c.close();
			return result;
		}
	}

	/**
	 * remove user from notification results
	 *
	 * @param id   user ID
	 * @param mute true remove user notifications
	 */
	public void muteUser(long id, boolean mute) {
		synchronized (LOCK) {
			SQLiteDatabase db = adapter.getDbWrite();
			int flags = getUserFlags(db, id);
			if (mute) {
				flags |= MASK_USER_FILTERED;
			} else {
				flags &= ~MASK_USER_FILTERED;
			}
			saveUserFlags(db, id, flags);
			adapter.commit();
		}
	}

	/**
	 * get all emojis
	 *
	 * @return list of emojis
	 */
	public List<Emoji> getEmojis() {
		synchronized (LOCK) {
			ArrayList<Emoji> result = new ArrayList<>();
			SQLiteDatabase db = adapter.getDbRead();
			Cursor c = db.query(EmojiTable.NAME, null, null, null, null, null, null);
			if (c.moveToFirst()) {
				result.ensureCapacity(c.getCount());
				do {
					result.add(new DatabaseEmoji(c));
				} while (c.moveToNext());
			}
			c.close();
			return result;
		}
	}

	/**
	 * remove database tables except account table
	 */
	public void resetDatabase() {
		synchronized (LOCK) {
			// save logins first
			List<Account> logins = getLogins();
			SQLiteDatabase db = adapter.getDbWrite();
			db.delete(UserTable.NAME, null, null);
			db.delete(StatusTable.NAME, null, null);
			db.delete(FavoriteTable.NAME, null, null);
			db.delete(BookmarkTable.NAME, null, null);
			db.delete(HashtagTable.NAME, null, null);
			db.delete(StatusRegisterTable.NAME, null, null);
			db.delete(UserRegisterTable.NAME, null, null);
			db.delete(NotificationTable.NAME, null, null);
			db.delete(MediaTable.NAME, null, null);
			db.delete(LocationTable.NAME, null, null);
			db.delete(EmojiTable.NAME, null, null);
			db.delete(PollTable.NAME, null, null);
			// save user information from logins
			for (Account login : logins) {
				if (login.getUser() != null) {
					saveUser(login.getUser(), db, SQLiteDatabase.CONFLICT_IGNORE);
				}
			}
			adapter.commit();
		}
	}

	/**
	 * get status information from database
	 *
	 * @param cursor cursor containing status informations
	 * @return status
	 */
	private Status getStatus(Cursor cursor, SQLiteDatabase db) {
		Account login = settings.getLogin();
		DatabaseStatus result = new DatabaseStatus(cursor, login);
		DatabaseUser author = (DatabaseUser) result.getAuthor();
		// check if there is an embedded status
		if (result.getEmbeddedStatusId() != NO_ID) {
			result.setEmbeddedStatus(getStatus(result.getEmbeddedStatusId()));
		}
		if (result.getPollId() != NO_ID) {
			result.addPoll(getPoll(db, result.getPollId()));
		}
		if (result.getMediaKeys().length > 0) {
			List<Media> mediaList = new LinkedList<>();
			for (String mediaKey : result.getMediaKeys()) {
				Media item = getMedia(db, mediaKey);
				if (item != null) {
					mediaList.add(item);
				}
			}
			if (!mediaList.isEmpty()) {
				result.addMedia(mediaList.toArray(new Media[0]));
			}
		}
		if (result.getEmojiKeys().length > 0) {
			List<Emoji> emojiList = new LinkedList<>();
			for (String emojiKey : result.getEmojiKeys()) {
				Emoji item = getEmoji(db, emojiKey);
				if (item != null) {
					emojiList.add(item);
				}
			}
			if (!emojiList.isEmpty()) {
				result.addEmojis(emojiList.toArray(new Emoji[0]));
			}
		}
		if (result.getLocationId() != Location.NO_ID) {
			Location location = getLocation(db, result.getLocationId());
			if (location != null) {
				result.addLocation(location);
			}
		}
		if (author.getEmojiKeys().length > 0) {
			List<Emoji> emojiList = new LinkedList<>();
			for (String emojiKey : author.getEmojiKeys()) {
				Emoji item = getEmoji(db, emojiKey);
				if (item != null) {
					emojiList.add(item);
				}
			}
			if (!emojiList.isEmpty()) {
				author.addEmojis(emojiList.toArray(new Emoji[0]));
			}
		}
		return result;
	}

	/**
	 * create a list of statuses from a cursor
	 *
	 * @param cursor cursor with statuses
	 * @return status list
	 */
	private Statuses getStatuses(Cursor cursor, SQLiteDatabase db) {
		Statuses statuses = new Statuses();
		if (cursor.moveToFirst()) {
			do {
				Status status = getStatus(cursor, db);
				statuses.add(status);
			} while (cursor.moveToNext());
		}
		cursor.close();
		Collections.sort(statuses);
		return statuses;
	}

	/**
	 * get status/message media
	 *
	 * @param key media key
	 * @param db  database read instance
	 * @return media item or null
	 */
	@Nullable
	private Media getMedia(SQLiteDatabase db, String key) {
		String[] args = {key};
		Cursor c = db.query(MediaTable.NAME, DatabaseMedia.PROJECTION, MEDIA_SELECT, args, null, null, null, SINGLE_ITEM);
		Media result = null;
		if (c.moveToFirst())
			result = new DatabaseMedia(c);
		c.close();
		return result;
	}

	/**
	 * get emoji information
	 *
	 * @param key emoji key
	 * @param db  database read instance
	 * @return emoji item or null
	 */
	@Nullable
	private Emoji getEmoji(SQLiteDatabase db, String key) {
		String[] args = {key};
		Cursor c = db.query(EmojiTable.NAME, DatabaseEmoji.PROJECTION, EMOJI_SELECT, args, null, null, null, SINGLE_ITEM);
		Emoji result = null;
		if (c.moveToFirst())
			result = new DatabaseEmoji(c);
		c.close();
		return result;
	}

	/**
	 * get status/message location
	 *
	 * @param db database read instance
	 * @param id location ID
	 * @return location item or null
	 */
	@Nullable
	private Location getLocation(SQLiteDatabase db, long id) {
		String[] args = {Long.toString(id)};
		Cursor c = db.query(LocationTable.NAME, DatabaseLocation.PROJECTION, LOCATION_SELECT, args, null, null, null, SINGLE_ITEM);
		Location result = null;
		if (c.moveToFirst())
			result = new DatabaseLocation(c);
		c.close();
		return result;
	}

	/**
	 * create database notification
	 *
	 * @param cursor database cursor containing notification columns
	 * @param login  information about the current login
	 * @return notification
	 */
	private Notification getNotification(Cursor cursor, Account login) {
		DatabaseNotification notification = new DatabaseNotification(cursor, login);
		switch (notification.getType()) {
			case Notification.TYPE_FAVORITE:
			case Notification.TYPE_REPOST:
			case Notification.TYPE_MENTION:
			case Notification.TYPE_POLL:
			case Notification.TYPE_STATUS:
			case Notification.TYPE_UPDATE:
				Status status = getStatus(notification.getItemId());
				notification.addStatus(status);
				break;
		}
		return notification;
	}

	/**
	 * get status flags or zero if status not found
	 *
	 * @param db database instance
	 * @param id ID of the status
	 * @return status flags
	 */
	private int getStatusFlags(SQLiteDatabase db, long id) {
		String[] args = {Long.toString(id), Long.toString(settings.getLogin().getId())};

		Cursor c = db.query(StatusRegisterTable.NAME, STATUS_REG_COLUMN, STATUS_REG_SELECT, args, null, null, null, SINGLE_ITEM);
		int result = 0;
		if (c.moveToFirst())
			result = c.getInt(0);
		c.close();
		return result;
	}

	/**
	 * get user register or zero if not found
	 *
	 * @param db database instance
	 * @param id ID of the user
	 * @return user flags
	 */
	private int getUserFlags(SQLiteDatabase db, long id) {
		String[] args = {Long.toString(id), Long.toString(settings.getLogin().getId())};

		Cursor c = db.query(UserRegisterTable.NAME, USER_REG_COLUMN, USER_REG_SELECT, args, null, null, null, SINGLE_ITEM);
		int result = 0;
		if (c.moveToFirst())
			result = c.getInt(0);
		c.close();
		return result;
	}

	/**
	 * get status poll
	 *
	 * @param db database instance
	 * @param id ID of the user
	 * @return poll instance
	 */
	@Nullable
	private Poll getPoll(SQLiteDatabase db, long id) {
		String[] args = {Long.toString(id)};

		Cursor c = db.query(PollTable.NAME, DatabasePoll.PROJECTION, POLL_SELECTION, args, null, null, null, SINGLE_ITEM);
		DatabasePoll result = null;
		if (c.moveToFirst())
			result = new DatabasePoll(c);
		c.close();
		return result;
	}


	/**
	 * store user information into database
	 *
	 * @param user user information
	 * @param db   SQLITE DB
	 * @param mode SQLITE mode {@link SQLiteDatabase#CONFLICT_IGNORE,SQLiteDatabase#CONFLICT_REPLACE}
	 */
	private void saveUser(User user, SQLiteDatabase db, int mode) {
		int flags = getUserFlags(db, user.getId());
		if (user.isVerified()) {
			flags |= MASK_USER_VERIFIED;
		} else {
			flags &= ~MASK_USER_VERIFIED;
		}
		if (user.isProtected()) {
			flags |= MASK_USER_PRIVATE;
		} else {
			flags &= ~MASK_USER_PRIVATE;
		}
		if (user.followRequested()) {
			flags |= MASK_USER_FOLLOW_REQUESTED;
		} else {
			flags &= ~MASK_USER_FOLLOW_REQUESTED;
		}
		if (user.hasDefaultProfileImage()) {
			flags |= MASK_USER_DEFAULT_IMAGE;
		} else {
			flags &= ~MASK_USER_DEFAULT_IMAGE;
		}
		ContentValues column = new ContentValues(14);
		if (user.getEmojis().length > 0) {
			StringBuilder buf = new StringBuilder();
			saveEmojis(user.getEmojis(), db);
			for (Emoji emoji : user.getEmojis()) {
				buf.append(emoji.getCode()).append(';');
			}
			String emojiKeys = buf.deleteCharAt(buf.length() - 1).toString();
			column.put(UserTable.EMOJI, emojiKeys);
		}
		column.put(UserTable.ID, user.getId());
		column.put(UserTable.USERNAME, user.getUsername());
		column.put(UserTable.SCREENNAME, user.getScreenname());
		column.put(UserTable.IMAGE, user.getOriginalProfileImageUrl());
		column.put(UserTable.DESCRIPTION, user.getDescription());
		column.put(UserTable.LINK, user.getProfileUrl());
		column.put(UserTable.LOCATION, user.getLocation());
		column.put(UserTable.BANNER, user.getOriginalBannerImageUrl());
		column.put(UserTable.SINCE, user.getTimestamp());
		column.put(UserTable.FRIENDS, user.getFollowing());
		column.put(UserTable.FOLLOWER, user.getFollower());
		column.put(UserTable.STATUSES, user.getStatusCount());
		column.put(UserTable.FAVORITS, user.getFavoriteCount());

		db.insertWithOnConflict(UserTable.NAME, "", column, mode);
		saveUserFlags(db, user.getId(), flags);
	}

	/**
	 * save status into database
	 *
	 * @param status status information
	 * @param flags  predefined status status flags or zero if there isn't one
	 * @param db     SQLite database
	 */
	private void saveStatus(Status status, SQLiteDatabase db, int flags) {
		User user = status.getAuthor();
		Status rtStat = status.getEmbeddedStatus();
		long rtId = NO_ID;
		if (rtStat != null) {
			saveStatus(rtStat, db, 0);
			rtId = rtStat.getId();
		}
		flags |= getStatusFlags(db, status.getId());
		if (status.isFavorited()) {
			flags |= MASK_STATUS_FAVORITED;
		} else {
			flags &= ~MASK_STATUS_FAVORITED;
		}
		if (status.isReposted()) {
			flags |= MASK_STATUS_REPOSTED;
		} else {
			flags &= ~MASK_STATUS_REPOSTED;
		}
		if (status.isSensitive()) {
			flags |= MASK_STATUS_SENSITIVE;
		} else {
			flags &= ~MASK_STATUS_SENSITIVE;
		}
		if (status.isSpoiler()) {
			flags |= MASK_STATUS_SPOILER;
		} else {
			flags &= ~MASK_STATUS_SPOILER;
		}
		if (status.isBookmarked()) {
			flags |= MASK_STATUS_BOOKMARKED;
		} else {
			flags &= ~MASK_STATUS_BOOKMARKED;
		}
		switch (status.getVisibility()) {
			case Status.VISIBLE_DIRECT:
				flags |= MASK_STATUS_VISIBILITY_DIRECT;
				break;

			case Status.VISIBLE_UNLISTED:
				flags |= MASK_STATUS_VISIBILITY_UNLISTED;
				break;

			case Status.VISIBLE_PRIVATE:
				flags |= MASK_STATUS_VISIBILITY_PRIVATE;
				break;

			default:
				flags &= ~MASK_STATUS_VISIBILITY_DIRECT;
		}
		ContentValues column = new ContentValues(20);
		column.put(StatusTable.ID, status.getId());
		column.put(StatusTable.USER, user.getId());
		column.put(StatusTable.TIME, status.getTimestamp());
		column.put(StatusTable.TEXT, status.getText());
		column.put(StatusTable.EMBEDDED, rtId);
		column.put(StatusTable.SOURCE, status.getSource());
		column.put(StatusTable.URL, status.getUrl());
		column.put(StatusTable.REPLYSTATUS, status.getRepliedStatusId());
		column.put(StatusTable.REPOST, status.getRepostCount());
		column.put(StatusTable.FAVORITE, status.getFavoriteCount());
		column.put(StatusTable.REPLY, status.getReplyCount());
		column.put(StatusTable.REPLYUSER, status.getRepliedUserId());
		column.put(StatusTable.REPLYUSER, status.getRepliedUserId());
		column.put(StatusTable.REPLYNAME, status.getReplyName());
		column.put(StatusTable.LANGUAGE, status.getLanguage());
		column.put(StatusTable.MENTIONS, status.getUserMentions());
		if (status.getLocation() != null && status.getLocation().getId() != 0L) {
			column.put(StatusTable.LOCATION, status.getLocation().getId());
			saveLocation(status.getLocation(), db);
		} else {
			column.put(StatusTable.LOCATION, 0L);
		}
		if (status.getMedia().length > 0) {
			StringBuilder buf = new StringBuilder();
			saveMedia(status.getMedia(), db);
			for (Media media : status.getMedia()) {
				buf.append(media.getKey()).append(';');
			}
			String mediaKeys = buf.deleteCharAt(buf.length() - 1).toString();
			column.put(StatusTable.MEDIA, mediaKeys);
		}
		if (status.getEmojis().length > 0) {
			StringBuilder buf = new StringBuilder();
			saveEmojis(status.getEmojis(), db);
			for (Emoji emoji : status.getEmojis()) {
				buf.append(emoji.getCode()).append(';');
			}
			String emojiKeys = buf.deleteCharAt(buf.length() - 1).toString();
			column.put(StatusTable.EMOJI, emojiKeys);
		}
		if (status.getPoll() != null) {
			savePoll(status.getPoll(), db);
			column.put(StatusTable.POLL, status.getPoll().getId());
		}
		db.insertWithOnConflict(StatusTable.NAME, "", column, SQLiteDatabase.CONFLICT_REPLACE);
		saveUser(user, db, SQLiteDatabase.CONFLICT_IGNORE);
		saveStatusFlags(db, status, flags);
	}

	/**
	 * save media information
	 *
	 * @param medias media to save
	 * @param db     database write instance
	 */
	private void saveMedia(Media[] medias, SQLiteDatabase db) {
		for (Media media : medias) {
			ContentValues column = new ContentValues(4);
			column.put(MediaTable.KEY, media.getKey());
			column.put(MediaTable.URL, media.getUrl());
			column.put(MediaTable.PREVIEW, media.getPreviewUrl());
			column.put(MediaTable.TYPE, media.getMediaType());
			db.insertWithOnConflict(MediaTable.NAME, "", column, SQLiteDatabase.CONFLICT_IGNORE);
		}
	}

	/**
	 * save media information
	 *
	 * @param emojis emojis to save
	 * @param db     database write instance
	 */
	private void saveEmojis(Emoji[] emojis, SQLiteDatabase db) {
		for (Emoji emoji : emojis) {
			saveEmoji(emoji, db);
		}
	}

	/**
	 * save location information
	 *
	 * @param location location information to save
	 * @param db       database write instance
	 */
	private void saveLocation(Location location, SQLiteDatabase db) {
		ContentValues column = new ContentValues(5);
		column.put(LocationTable.ID, location.getId());
		column.put(LocationTable.FULLNAME, location.getFullName());
		column.put(LocationTable.COORDINATES, location.getCoordinates());
		column.put(LocationTable.COUNTRY, location.getCountry());
		column.put(LocationTable.PLACE, location.getPlace());
		db.insertWithOnConflict(LocationTable.NAME, "", column, SQLiteDatabase.CONFLICT_IGNORE);
	}

	/**
	 * save status poll
	 *
	 * @param poll poll to save
	 * @param db   database instance
	 */
	private void savePoll(Poll poll, SQLiteDatabase db) {
		ContentValues column = new ContentValues(4);
		StringBuilder buf = new StringBuilder();
		for (Poll.Option option : poll.getOptions()) {
			buf.append(option.getTitle()).append(';');
		}
		if (buf.length() > 0) {
			buf.deleteCharAt(buf.length() - 1);
		}
		column.put(PollTable.ID, poll.getId());
		column.put(PollTable.EXPIRATION, poll.getEndTime());
		column.put(PollTable.OPTIONS, buf.toString());
		db.insertWithOnConflict(PollTable.NAME, "", column, SQLiteDatabase.CONFLICT_REPLACE);
	}

	/**
	 * save single emoji
	 *
	 * @param emoji emoji to save
	 * @param db    database write instance
	 */
	private void saveEmoji(Emoji emoji, SQLiteDatabase db) {
		ContentValues column = new ContentValues(3);
		column.put(EmojiTable.CODE, emoji.getCode());
		column.put(EmojiTable.URL, emoji.getUrl());
		column.put(EmojiTable.CATEGORY, emoji.getCategory());
		db.insertWithOnConflict(EmojiTable.NAME, "", column, SQLiteDatabase.CONFLICT_IGNORE);
	}

	/**
	 * set register of a status. Update if an entry exists
	 *
	 * @param db     database instance
	 * @param status status
	 * @param flags  status flags
	 */
	private void saveStatusFlags(SQLiteDatabase db, Status status, int flags) {
		String[] args = {Long.toString(status.getId()), Long.toString(settings.getLogin().getId())};

		ContentValues column = new ContentValues(4);
		column.put(StatusRegisterTable.REGISTER, flags);
		column.put(StatusRegisterTable.REPOST_ID, status.getRepostId());
		column.put(StatusRegisterTable.STATUS, status.getId());
		column.put(StatusRegisterTable.OWNER, settings.getLogin().getId());

		int count = db.update(StatusRegisterTable.NAME, column, STATUS_REG_SELECT, args);
		if (count == 0) {
			// create new entry if there isn't one
			db.insert(StatusRegisterTable.NAME, null, column);
		}
	}

	/**
	 * set user register. If entry exists, update it.
	 *
	 * @param db    database instance
	 * @param id    User ID
	 * @param flags status flags
	 */
	private void saveUserFlags(SQLiteDatabase db, long id, int flags) {
		String[] args = {Long.toString(id), Long.toString(settings.getLogin().getId())};

		ContentValues column = new ContentValues(3);
		column.put(UserRegisterTable.USER, id);
		column.put(UserRegisterTable.OWNER, settings.getLogin().getId());
		column.put(UserRegisterTable.REGISTER, flags);

		int cnt = db.update(UserRegisterTable.NAME, column, USER_REG_SELECT, args);
		if (cnt == 0) {
			// create new entry if there isn't an entry
			db.insert(UserRegisterTable.NAME, null, column);
		}
	}

	/**
	 * store status ID to the favorite table
	 *
	 * @param statusId ID of the status
	 * @param ownerId  ID of the list owner
	 * @param db       database instance
	 */
	private void saveFavorite(long statusId, long ownerId, SQLiteDatabase db) {
		ContentValues column = new ContentValues(2);
		column.put(FavoriteTable.STATUS, statusId);
		column.put(FavoriteTable.OWNER, ownerId);
		db.insertWithOnConflict(FavoriteTable.NAME, "", column, SQLiteDatabase.CONFLICT_REPLACE);
	}

	/**
	 * store status ID to the bookmark table
	 *
	 * @param statusId ID of the status
	 * @param ownerId  ID of the list owner
	 * @param db       database instance
	 */
	private void saveBookmark(long statusId, long ownerId, SQLiteDatabase db) {
		ContentValues column = new ContentValues(2);
		column.put(BookmarkTable.STATUS, statusId);
		column.put(BookmarkTable.OWNER, ownerId);
		db.insertWithOnConflict(BookmarkTable.NAME, "", column, SQLiteDatabase.CONFLICT_REPLACE);
	}

	/**
	 * save instance information
	 *
	 * @param instance instance information
	 * @param db       database instance
	 */
	private void saveInstance(Instance instance, SQLiteDatabase db) {
		ContentValues column = new ContentValues(21);
		int flags = 0;
		if (instance.isTranslationSupported())
			flags |= DatabaseInstance.MASK_TRANSLATION;
		StringBuilder mimeTypes = new StringBuilder();
		for (String mimeType : instance.getSupportedFormats()) {
			mimeTypes.append(mimeType).append(';');
		}
		if (mimeTypes.length() > 0) {
			mimeTypes.deleteCharAt(mimeTypes.length() - 1);
		}
		column.put(InstanceTable.DOMAIN, instance.getDomain());
		column.put(InstanceTable.TIMESTAMP, instance.getTimestamp());
		column.put(InstanceTable.TITLE, instance.getTitle());
		column.put(InstanceTable.VERSION, instance.getVersion());
		column.put(InstanceTable.DESCRIPTION, instance.getDescription());
		column.put(InstanceTable.FLAGS, flags);
		column.put(InstanceTable.HASHTAG_LIMIT, instance.getHashtagFollowLimit());
		column.put(InstanceTable.STATUS_MAX_CHAR, instance.getStatusCharacterLimit());
		column.put(InstanceTable.IMAGE_LIMIT, instance.getImageLimit());
		column.put(InstanceTable.VIDEO_LIMIT, instance.getVideoLimit());
		column.put(InstanceTable.GIF_LIMIT, instance.getGifLimit());
		column.put(InstanceTable.AUDIO_LIMIT, instance.getAudioLimit());
		column.put(InstanceTable.OPTIONS_LIMIT, instance.getPollOptionsLimit());
		column.put(InstanceTable.OPTION_MAX_CHAR, instance.getPollOptionCharacterLimit());
		column.put(InstanceTable.MIME_TYPES, mimeTypes.toString());
		column.put(InstanceTable.IMAGE_SIZE, instance.getImageSizeLimit());
		column.put(InstanceTable.VIDEO_SIZE, instance.getVideoSizeLimit());
		column.put(InstanceTable.GIF_SIZE, instance.getGifSizeLimit());
		column.put(InstanceTable.AUDIO_SIZE, instance.getAudioSizeLimit());
		column.put(InstanceTable.POLL_MIN_DURATION, instance.getMinPollDuration());
		column.put(InstanceTable.POLL_MAX_DURATION, instance.getMaxPollDuration());
		db.insertWithOnConflict(InstanceTable.NAME, "", column, SQLiteDatabase.CONFLICT_REPLACE);
	}
}