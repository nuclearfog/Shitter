package org.nuclearfog.twidda.database;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static org.nuclearfog.twidda.database.DatabaseAdapter.AccountTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.BookmarkTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.EmojiTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.FavoriteTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.LocationTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.MediaTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.MessageTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.NotificationTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.StatusRegisterTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.StatusTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.TrendTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.UserExcludeTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.UserRegisterTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.UserTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.helper.Messages;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.database.impl.DatabaseAccount;
import org.nuclearfog.twidda.database.impl.DatabaseEmoji;
import org.nuclearfog.twidda.database.impl.DatabaseLocation;
import org.nuclearfog.twidda.database.impl.DatabaseMedia;
import org.nuclearfog.twidda.database.impl.DatabaseMessage;
import org.nuclearfog.twidda.database.impl.DatabaseNotification;
import org.nuclearfog.twidda.database.impl.DatabaseStatus;
import org.nuclearfog.twidda.database.impl.DatabaseTrend;
import org.nuclearfog.twidda.database.impl.DatabaseUser;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Message;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * SQLite database class to store and load status, messages, trends and user information
 *
 * @author nuclearfog
 */
public class AppDatabase {

	/**
	 * flag indicates that a status was favorited by the current user
	 */
	public static final int FAVORITE_MASK = 1;

	/**
	 * flag indicates that a status was reposted by the current user
	 */
	public static final int REPOST_MASK = 1 << 1;

	/**
	 * flag indicates that a status exists in the home timeline of the current user
	 */
	public static final int HOME_TIMELINE_MASK = 1 << 2;

	/**
	 * flag indicates that a status exists in the notification of the current user
	 */
	public static final int NOTIFICATION_MASK = 1 << 3;

	/**
	 * flag indicates that a status exists in an user timeline
	 */
	public static final int USER_TIMELINE_MASK = 1 << 4;

	/**
	 * flag indicates that a status exists in the reply of a status
	 */
	public static final int STATUS_REPLY_MASK = 1 << 5;

	/**
	 * flag indicates that a status contains sensitive media
	 */
	public static final int MEDIA_SENS_MASK = 1 << 8;

	/**
	 * flag indicates that a status was hidden by the current user
	 */
	public static final int HIDDEN_MASK = 1 << 9;

	/**
	 * flag indicated that a status is bookmarked by the current user
	 */
	public static final int BOOKMARK_MASK = 1 << 10;

	/**
	 * flag indicates that an user is verified
	 */
	public static final int VERIFIED_MASK = 1;

	/**
	 * flag indicates that an user is locked/private
	 */
	public static final int LOCKED_MASK = 1 << 1;

	/**
	 * flag indicates that the current user has sent a follow request to an user
	 */
	public static final int FOLLOW_REQUEST_MASK = 1 << 2;

	/**
	 * flag indicates that the statuses of an user are excluded from timeline
	 */
	public static final int EXCLUDE_MASK = 1 << 3;

	/**
	 * flag indicates that the user has a default profile image
	 */
	public static final int DEFAULT_IMAGE_MASK = 1 << 4;

	/**
	 * query to create status table with user and register columns
	 */
	private static final String STATUS_SUBQUERY = StatusTable.NAME
			+ " INNER JOIN " + UserTable.NAME
			+ " ON " + StatusTable.NAME + "." + StatusTable.USER + "=" + UserTable.NAME + "." + UserTable.ID
			+ " INNER JOIN " + UserRegisterTable.NAME
			+ " ON " + StatusTable.NAME + "." + StatusTable.USER + "=" + UserRegisterTable.NAME + "." + UserRegisterTable.ID
			+ " INNER JOIN " + StatusRegisterTable.NAME
			+ " ON " + StatusTable.NAME + "." + StatusTable.ID + "=" + StatusRegisterTable.NAME + "." + StatusRegisterTable.ID;

	/**
	 * query to get user information
	 */
	private static final String USER_SUBQUERY = UserTable.NAME
			+ " INNER JOIN " + UserRegisterTable.NAME
			+ " ON " + UserTable.NAME + "." + UserTable.ID + "=" + UserRegisterTable.NAME + "." + UserRegisterTable.ID;

	/**
	 * SQL query to get home timeline status
	 */
	private static final String HOME_QUERY = "SELECT * FROM(" + STATUS_SUBQUERY + ")"
			+ " WHERE " + StatusRegisterTable.NAME + "." + StatusRegisterTable.REGISTER + "&" + HOME_TIMELINE_MASK + " IS NOT 0"
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.OWNER + "=?"
			+ " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
			+ " ORDER BY " + StatusTable.TIME + " DESC"
			+ " LIMIT ?;";

	/**
	 * SQL query to get status of an user
	 */
	private static final String USER_STATUS_QUERY = "SELECT * FROM(" + STATUS_SUBQUERY + ")"
			+ " WHERE " + StatusRegisterTable.NAME + "." + StatusRegisterTable.REGISTER + "&" + USER_TIMELINE_MASK + " IS NOT 0"
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
			+ " ON " + StatusTable.NAME + "." + StatusTable.ID + "=" + FavoriteTable.NAME + "." + FavoriteTable.STATUS_ID
			+ " WHERE " + FavoriteTable.NAME + "." + FavoriteTable.OWNER_ID + "=?"
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.OWNER + "=?"
			+ " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
			+ " ORDER BY " + StatusTable.TIME + " DESC"
			+ " LIMIT ?;";

	/**
	 * SQL query to get status favored by an user
	 */
	private static final String USER_BOOKMARKS_QUERY = "SELECT * FROM(" + STATUS_SUBQUERY + ")"
			+ " INNER JOIN " + BookmarkTable.NAME
			+ " ON " + StatusTable.NAME + "." + StatusTable.ID + "=" + BookmarkTable.NAME + "." + BookmarkTable.STATUS_ID
			+ " WHERE " + BookmarkTable.NAME + "." + BookmarkTable.OWNER_ID + "=?"
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.OWNER + "=?"
			+ " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
			+ " ORDER BY " + StatusTable.TIME + " DESC"
			+ " LIMIT ?;";

	/**
	 * SQL query to get a single status specified by an ID
	 */
	static final String SINGLE_STATUS_QUERY = "SELECT * FROM " + STATUS_SUBQUERY
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
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.REGISTER + "&" + STATUS_REPLY_MASK + " IS NOT 0"
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.REGISTER + "&" + HIDDEN_MASK + " IS 0"
			+ " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.REGISTER + "&" + EXCLUDE_MASK + " IS 0"
			+ " ORDER BY " + StatusTable.TIME + " DESC"
			+ " LIMIT ?;";

	/**
	 * SQL query to get current user's messages
	 */
	private static final String MESSAGE_QUERY = "SELECT * FROM " + MessageTable.NAME
			+ " INNER JOIN " + UserTable.NAME
			+ " ON " + MessageTable.NAME + "." + MessageTable.FROM + "=" + UserTable.NAME + "." + UserTable.ID
			+ " INNER JOIN " + UserRegisterTable.NAME
			+ " ON " + MessageTable.NAME + "." + MessageTable.FROM + "=" + UserRegisterTable.NAME + "." + UserRegisterTable.ID
			+ " WHERE " + MessageTable.FROM + "=? OR " + MessageTable.TO + "=?"
			+ " ORDER BY " + MessageTable.TIME + " DESC"
			+ " LIMIT ?;";

	/**
	 * SQL query to get notifications
	 */
	private static final String NOTIFICATION_QUERY = "SELECT * FROM " + NotificationTable.NAME
			+ " INNER JOIN(" + USER_SUBQUERY + ")" + UserTable.NAME
			+ " ON " + NotificationTable.NAME + "." + NotificationTable.USER + "=" + UserTable.NAME + "." + UserTable.ID
			+ " WHERE " + NotificationTable.NAME + "." + NotificationTable.OWNER + "=?"
			+ " ORDER BY " + NotificationTable.TIME + " DESC"
			+ " LIMIT ?;";

	/**
	 * select status entries from favorite table matching status ID
	 * this status can be favored by multiple users
	 */
	private static final String FAVORITE_SELECT_STATUS = FavoriteTable.STATUS_ID + "=?";

	/**
	 * select all statuses from favorite table favored by given user
	 */
	private static final String FAVORITE_SELECT_OWNER = FavoriteTable.OWNER_ID + "=?";

	/**
	 * select status entries from favorite table matching status ID
	 * this status can be favored by multiple users
	 */
	private static final String BOOKMARK_SELECT_STATUS = BookmarkTable.STATUS_ID + "=?";

	/**
	 * select all statuses from favorite table favored by given user
	 */
	private static final String BOOKMARK_SELECT_OWNER = BookmarkTable.OWNER_ID + "=?";

	/**
	 * select specific status from favorite table
	 */
	private static final String FAVORITE_SELECT = FAVORITE_SELECT_STATUS + " AND " + FAVORITE_SELECT_OWNER;

	/**
	 * select specific status from favorite table
	 */
	private static final String BOOKMARK_SELECT = BOOKMARK_SELECT_STATUS + " AND " + BOOKMARK_SELECT_OWNER;

	/**
	 * select message from message table with ID
	 */
	private static final String MESSAGE_SELECT = MessageTable.ID + "=?";

	/**
	 * select trends from trend table with given world ID
	 */
	private static final String TREND_SELECT = TrendTable.ID + "=?";

	/**
	 * select status from status table matching ID
	 */
	private static final String STATUS_SELECT = StatusTable.NAME + "." + StatusTable.ID + "=?";

	/**
	 * select notification from notification table using status ID
	 */
	private static final String NOTIFICATION_SELECT = NotificationTable.NAME + "." + NotificationTable.ITEM + "=?";

	/**
	 * select user from user table matching user ID
	 */
	private static final String USER_SELECT = UserTable.NAME + "." + UserTable.ID + "=?";

	/**
	 * selection to get status flag register
	 */
	private static final String STATUS_REG_SELECT = StatusRegisterTable.ID + "=? AND " + StatusRegisterTable.OWNER + "=?";

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
	private static final String USER_REG_SELECT = UserRegisterTable.ID + "=? AND " + UserRegisterTable.OWNER + "=?";

	/**
	 * selection for account entry
	 */
	private static final String ACCOUNT_SELECTION = AccountTable.ID + "=?";

	/**
	 * column projection for user flag register
	 */
	private static final String[] USER_REG_COLUMN = {UserRegisterTable.REGISTER};

	/**
	 * column projection for status flag register
	 */
	private static final String[] STATUS_REG_COLUMN = {StatusRegisterTable.REGISTER};

	/**
	 * column to fetch from the database
	 */
	private static final String[] LIST_ID_COL = {UserExcludeTable.ID};

	/**
	 * selection to get the exclude list of the current user
	 */
	private static final String LIST_SELECT = UserExcludeTable.OWNER + "=?";

	/**
	 * selection to get a column
	 */
	private static final String FILTER_SELECT = LIST_SELECT + " AND " + UserExcludeTable.ID + "=?";

	/**
	 * default sort order for logins
	 */
	private static final String SORT_BY_CREATION = AccountTable.DATE + " DESC";

	/**
	 * limit for accessing a single row
	 */
	private static final String SINGLE_ITEM = "1";

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
		settings = GlobalSettings.getInstance(context);
	}

	/**
	 * Store user information
	 *
	 * @param user Twitter user
	 */
	public void saveUser(User user) {
		SQLiteDatabase db = getDbWrite();
		saveUser(user, db, CONFLICT_REPLACE);
		commit(db);
	}

	/**
	 * save home timeline
	 *
	 * @param home status from home timeline
	 */
	public void saveHomeTimeline(List<Status> home) {
		SQLiteDatabase db = getDbWrite();
		for (Status status : home)
			saveStatus(status, db, HOME_TIMELINE_MASK);
		commit(db);
	}

	/**
	 * save user timeline
	 *
	 * @param stats user timeline
	 */
	public void saveUserTimeline(List<Status> stats) {
		SQLiteDatabase db = getDbWrite();
		for (Status status : stats)
			saveStatus(status, db, USER_TIMELINE_MASK);
		commit(db);
	}

	/**
	 * save user favorite timeline
	 *
	 * @param statuses status favored by user
	 * @param ownerId  user ID
	 */
	public void saveFavoriteTimeline(List<Status> statuses, long ownerId) {
		SQLiteDatabase db = getDbWrite();
		// delete old favorits
		String[] delArgs = {Long.toString(ownerId)};
		db.delete(FavoriteTable.NAME, FAVORITE_SELECT_OWNER, delArgs);
		// save new favorits
		for (Status status : statuses) {
			saveStatus(status, db, 0);
			saveFavorite(status.getId(), ownerId, db);
		}
		commit(db);
	}

	/**
	 * save user bookmark timeline
	 *
	 * @param statuses bookmarked statuses
	 * @param ownerId  id of the owner
	 */
	public void saveBookmarkTimeline(List<Status> statuses, long ownerId) {
		SQLiteDatabase db = getDbWrite();
		// delete old favorits
		String[] delArgs = {Long.toString(ownerId)};
		db.delete(BookmarkTable.NAME, BOOKMARK_SELECT_OWNER, delArgs);
		// save new bookmarks
		for (Status status : statuses) {
			saveStatus(status, db, 0);
			saveBookmark(status.getId(), ownerId, db);
		}
		commit(db);
	}

	/**
	 * store replies of a status
	 *
	 * @param replies status replies
	 */
	public void saveReplyTimeline(List<Status> replies) {
		SQLiteDatabase db = getDbWrite();
		for (Status status : replies)
			saveStatus(status, db, STATUS_REPLY_MASK);
		commit(db);
	}

	/**
	 * store location specific trends
	 *
	 * @param trends List of Trends
	 */
	public void saveTrends(List<Trend> trends) {
		String[] args = {Long.toString(settings.getTrendLocation().getId())};
		SQLiteDatabase db = getDbWrite();
		db.delete(TrendTable.NAME, TREND_SELECT, args);
		for (Trend trend : trends) {
			ContentValues trendColumn = new ContentValues(4);
			trendColumn.put(TrendTable.ID, trend.getLocationId());
			trendColumn.put(TrendTable.VOL, trend.getPopularity());
			trendColumn.put(TrendTable.TREND, trend.getName());
			trendColumn.put(TrendTable.INDEX, trend.getRank());
			db.insert(TrendTable.NAME, null, trendColumn);
		}
		commit(db);
	}

	/**
	 * store ID of a favorited status to the current users favorite list
	 *
	 * @param status favorited status
	 */
	public void addToFavorits(Status status) {
		if (status.getEmbeddedStatus() != null)
			status = status.getEmbeddedStatus();
		SQLiteDatabase db = getDbWrite();
		saveStatus(status, db, 0);
		saveFavorite(status.getId(), settings.getLogin().getId(), db);
		commit(db);
	}

	/**
	 * store ID of a status to the current users bookmarks
	 *
	 * @param status favorited status
	 */
	public void addToBookmarks(Status status) {
		if (status.getEmbeddedStatus() != null)
			status = status.getEmbeddedStatus();
		SQLiteDatabase db = getDbWrite();
		saveStatus(status, db, 0);
		saveBookmark(status.getId(), settings.getLogin().getId(), db);
		commit(db);
	}

	/**
	 * save notifications to database
	 */
	public void saveNotifications(List<Notification> notifications) {
		SQLiteDatabase db = getDbWrite();
		for (Notification notification : notifications) {
			ContentValues column = new ContentValues();
			column.put(NotificationTable.ID, notification.getId());
			column.put(NotificationTable.TIME, notification.getTimestamp());
			column.put(NotificationTable.TYPE, notification.getType());
			column.put(NotificationTable.OWNER, settings.getLogin().getId());
			column.put(NotificationTable.USER, notification.getUser().getId());
			saveUser(notification.getUser(), db, CONFLICT_IGNORE);
			// add status
			if (notification.getStatus() != null) {
				saveStatus(notification.getStatus(), db, NOTIFICATION_MASK);
				column.put(NotificationTable.ITEM, notification.getStatus().getId());
			}
			db.insertWithOnConflict(NotificationTable.NAME, null, column, CONFLICT_REPLACE);
		}
		commit(db);
	}

	/**
	 * store direct messages
	 *
	 * @param messages list of direct messages
	 */
	public void saveMessages(List<Message> messages) {
		SQLiteDatabase db = getDbWrite();
		for (Message message : messages)
			saveMessages(message, db);
		commit(db);
	}

	/**
	 * save user login
	 *
	 * @param account login information
	 */
	public void saveLogin(Account account) {
		ContentValues values = new ContentValues(9);
		values.put(AccountTable.ID, account.getId());
		values.put(AccountTable.DATE, account.getTimestamp());
		values.put(AccountTable.HOSTNAME, account.getHostname());
		values.put(AccountTable.CLIENT_ID, account.getConsumerToken());
		values.put(AccountTable.CLIENT_SECRET, account.getConsumerSecret());
		values.put(AccountTable.API, account.getConfiguration().getAccountType());
		values.put(AccountTable.ACCESS_TOKEN, account.getOauthToken());
		values.put(AccountTable.TOKEN_SECRET, account.getOauthSecret());
		values.put(AccountTable.BEARER, account.getBearerToken());
		SQLiteDatabase db = getDbWrite();
		db.insertWithOnConflict(AccountTable.NAME, "", values, CONFLICT_REPLACE);
		if (account.getUser() != null) {
			saveUser(account.getUser(), db, CONFLICT_IGNORE);
		}
		commit(db);
	}

	/**
	 * create a new filterlist containing user IDs
	 *
	 * @param ids list of user IDs
	 */
	public void setFilterlistUserIds(List<Long> ids) {
		long homeId = settings.getLogin().getId();
		String[] args = {Long.toString(homeId)};
		SQLiteDatabase db = getDbWrite();

		db.delete(UserExcludeTable.NAME, LIST_SELECT, args);
		for (long id : ids) {
			ContentValues column = new ContentValues(2);
			column.put(UserExcludeTable.ID, id);
			column.put(UserExcludeTable.OWNER, homeId);
			db.insertWithOnConflict(UserExcludeTable.NAME, null, column, SQLiteDatabase.CONFLICT_IGNORE);
		}
		commit(db);
	}

	/**
	 * add user to the exclude database
	 *
	 * @param userId ID of the user
	 */
	public void addUserToFilterlist(long userId) {
		SQLiteDatabase db = getDbWrite();
		ContentValues column = new ContentValues(2);
		column.put(UserExcludeTable.ID, userId);
		column.put(UserExcludeTable.OWNER, settings.getLogin().getId());
		db.insert(UserExcludeTable.NAME, null, column);
		commit(db);
	}

	/**
	 * load home timeline
	 *
	 * @return home timeline
	 */
	public List<Status> getHomeTimeline() {
		String homeStr = Long.toString(settings.getLogin().getId());
		String[] args = {homeStr, homeStr, Integer.toString(settings.getListSize())};

		SQLiteDatabase db = getDbRead();
		Cursor cursor = db.rawQuery(HOME_QUERY, args);
		return getStatuses(cursor, db);
	}

	/**
	 * load user timeline
	 *
	 * @param userID user ID
	 * @return user timeline
	 */
	public List<Status> getUserTimeline(long userID) {
		String homeStr = Long.toString(settings.getLogin().getId());
		String[] args = {homeStr, homeStr, Long.toString(userID), Integer.toString(settings.getListSize())};

		SQLiteDatabase db = getDbRead();
		Cursor cursor = db.rawQuery(USER_STATUS_QUERY, args);
		return getStatuses(cursor, db);
	}

	/**
	 * load favorite timeline
	 *
	 * @param ownerID user ID
	 * @return favorite timeline
	 */
	public List<Status> getUserFavorites(long ownerID) {
		String homeStr = Long.toString(settings.getLogin().getId());
		String[] args = {Long.toString(ownerID), homeStr, homeStr, Integer.toString(settings.getListSize())};

		SQLiteDatabase db = getDbRead();
		Cursor cursor = db.rawQuery(USER_FAVORIT_QUERY, args);
		return getStatuses(cursor, db);
	}

	/**
	 * load status bookmarks
	 *
	 * @param ownerID user ID
	 * @return bookmark timeline
	 */
	public List<Status> getUserBookmarks(long ownerID) {
		String homeStr = Long.toString(settings.getLogin().getId());
		String[] args = {Long.toString(ownerID), homeStr, homeStr, Integer.toString(settings.getListSize())};

		SQLiteDatabase db = getDbRead();
		Cursor cursor = db.rawQuery(USER_BOOKMARKS_QUERY, args);
		return getStatuses(cursor, db);
	}

	/**
	 * get user information
	 *
	 * @param userId ID of user
	 * @return user information or null if not found
	 */
	@Nullable
	public User getUser(long userId) {
		return getUser(userId, settings.getLogin());
	}

	/**
	 * get user information
	 *
	 * @param userId  ID of user
	 * @param account current user information
	 * @return user information or null if not found
	 */
	@Nullable
	public User getUser(long userId, Account account) {
		String[] args = {Long.toString(userId)};
		SQLiteDatabase db = getDbRead();
		Cursor cursor = db.rawQuery(SINGLE_USER_QUERY, args);
		User user = null;
		if (cursor.moveToFirst())
			user = new DatabaseUser(cursor, account);
		cursor.close();
		return user;
	}

	/**
	 * get status from database
	 *
	 * @param id status ID
	 * @return status or null if not found
	 */
	@Nullable
	public Status getStatus(long id) {
		String homeStr = Long.toString(settings.getLogin().getId());
		String[] args = {Long.toString(id), homeStr, homeStr};

		SQLiteDatabase db = getDbRead();
		Status result = null;
		Cursor cursor = db.rawQuery(SINGLE_STATUS_QUERY, args);
		if (cursor.moveToFirst())
			result = getStatus(cursor, db);
		cursor.close();
		return result;
	}

	/**
	 * get reply timeline
	 *
	 * @param id status ID
	 * @return status reply timeline
	 */
	public List<Status> getReplies(long id) {
		String homeStr = Long.toString(settings.getLogin().getId());
		String[] args = {Long.toString(id), homeStr, homeStr, Integer.toString(settings.getListSize())};

		SQLiteDatabase db = getDbRead();
		Cursor cursor = db.rawQuery(REPLY_QUERY, args);
		return getStatuses(cursor, db);
	}

	/**
	 * get notifiactions
	 *
	 * @return notification lsit
	 */
	public List<Notification> getNotifications() {
		Account login = settings.getLogin();
		String[] args = {Long.toString(login.getId()), Integer.toString(settings.getListSize())};
		SQLiteDatabase db = getDbRead();
		List<Notification> result = new LinkedList<>();
		Cursor cursor = db.rawQuery(NOTIFICATION_QUERY, args);
		if (cursor.moveToFirst()) {
			do {
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
				result.add(notification);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
	}

	/**
	 * update status
	 *
	 * @param status status to update
	 */
	public void updateStatus(Status status) {
		SQLiteDatabase db = getDbWrite();
		updateStatus(status, db);
		if (status.getEmbeddedStatus() != null)
			updateStatus(status.getEmbeddedStatus(), db);
		commit(db);
	}

	/**
	 * hide or unhide status
	 *
	 * @param id   ID of the reply
	 * @param hide true to hide this status
	 */
	public void hideStatus(long id, boolean hide) {
		String[] args = {Long.toString(id), Long.toString(settings.getLogin().getId())};

		SQLiteDatabase db = getDbWrite();
		int flags = getStatusFlags(db, id);
		if (hide) {
			flags |= HIDDEN_MASK;
		} else {
			flags &= ~HIDDEN_MASK;
		}
		ContentValues values = new ContentValues(3);
		values.put(StatusRegisterTable.REGISTER, flags);
		db.update(StatusRegisterTable.NAME, values, STATUS_REG_SELECT, args);
		commit(db);
	}

	/**
	 * remove status from database
	 *
	 * @param id status ID
	 */
	public void removeStatus(long id) {
		String[] args = {Long.toString(id)};

		SQLiteDatabase db = getDbWrite();
		db.delete(StatusTable.NAME, STATUS_SELECT, args);
		db.delete(NotificationTable.NAME, NOTIFICATION_SELECT, args);
		db.delete(FavoriteTable.NAME, FAVORITE_SELECT_STATUS, args);
		db.delete(BookmarkTable.NAME, BOOKMARK_SELECT_STATUS, args);
		commit(db);
	}

	/**
	 * remove status from favorites
	 *
	 * @param status status to remove from the favorites
	 */
	public void removeFromFavorite(Status status) {
		String[] delArgs = {Long.toString(status.getId()), Long.toString(settings.getLogin().getId())};

		if (status.getEmbeddedStatus() != null) {
			status = status.getEmbeddedStatus();
		}
		SQLiteDatabase db = getDbWrite();
		// get status flags
		int flags = getStatusFlags(db, status.getId());
		flags &= ~FAVORITE_MASK; // unset favorite flag
		// update database
		saveStatusFlags(db, status, flags);
		db.delete(FavoriteTable.NAME, FAVORITE_SELECT, delArgs);
		commit(db);
	}

	/**
	 * remove status from bookmarks
	 *
	 * @param status status to remove from the bookmarks
	 */
	public void removeFromBookmarks(Status status) {
		String[] delArgs = {Long.toString(status.getId()), Long.toString(settings.getLogin().getId())};

		if (status.getEmbeddedStatus() != null) {
			status = status.getEmbeddedStatus();
		}
		SQLiteDatabase db = getDbWrite();
		// get status flags
		int flags = getStatusFlags(db, status.getId());
		flags &= ~BOOKMARK_MASK; // unset bookmark flag
		// update database
		saveStatusFlags(db, status, flags);
		db.delete(BookmarkTable.NAME, BOOKMARK_SELECT, delArgs);
		commit(db);
	}

	/**
	 * Delete Direct Message
	 *
	 * @param id Direct Message ID
	 */
	public void removeMessage(long id) {
		String[] messageId = {Long.toString(id)};

		SQLiteDatabase db = getDbWrite();
		db.delete(MessageTable.NAME, MESSAGE_SELECT, messageId);
		commit(db);
	}

	/**
	 * remove login information from database
	 *
	 * @param id account ID to remove
	 */
	public void removeLogin(long id) {
		String[] args = {Long.toString(id)};

		SQLiteDatabase db = getDbWrite();
		db.delete(AccountTable.NAME, ACCOUNT_SELECTION, args);
		commit(db);
	}

	/**
	 * Load trend List
	 *
	 * @return list of trends
	 */
	public List<Trend> getTrends() {
		String[] args = {Long.toString(settings.getTrendLocation().getId())};
		SQLiteDatabase db = getDbRead();
		Cursor cursor = db.query(TrendTable.NAME, DatabaseTrend.COLUMNS, TREND_SELECT, args, null, null, null);
		List<Trend> trends = new LinkedList<>();
		if (cursor.moveToFirst()) {
			do {
				trends.add(new DatabaseTrend(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		Collections.sort(trends);
		return trends;
	}

	/**
	 * load direct messages
	 *
	 * @return list of direct messages
	 */
	public Messages getMessages() {
		Account login = settings.getLogin();
		String homeIdStr = Long.toString(login.getId());
		String[] args = {homeIdStr, homeIdStr, Integer.toString(settings.getListSize())};
		Messages result = new Messages(null, null);
		SQLiteDatabase db = getDbRead();
		Cursor cursor = db.rawQuery(MESSAGE_QUERY, args);
		if (cursor.moveToFirst()) {
			do {
				DatabaseMessage item = new DatabaseMessage(cursor, login);
				result.add(item);
				if (item.getMediaKeys().length > 0) {
					List<Media> medias = new LinkedList<>();
					for (String key : item.getMediaKeys()) {
						Media media = getMedia(db, key);
						if (media != null) {
							medias.add(media);
						}
					}
					item.addMedia(medias.toArray(new Media[0]));
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
	}

	/**
	 * return the current filterlist containing user IDs
	 *
	 * @return a set of user IDs
	 */
	public Set<Long> getFilterlistUserIds() {
		String[] args = {Long.toString(settings.getLogin().getId())};
		SQLiteDatabase db = getDbRead();
		Cursor cursor = db.query(UserExcludeTable.NAME, LIST_ID_COL, LIST_SELECT, args, null, null, null, null);

		Set<Long> result = new TreeSet<>();
		if (cursor.moveToFirst()) {
			do {
				long id = cursor.getLong(0);
				result.add(id);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
	}

	/**
	 * remove user from the exclude database
	 *
	 * @param userId ID of the user
	 */
	public void removeUserFromFilterlist(long userId) {
		String[] args = {Long.toString(settings.getLogin().getId()), Long.toString(userId)};
		SQLiteDatabase db = getDbWrite();
		db.delete(UserExcludeTable.NAME, FILTER_SELECT, args);
		commit(db);
	}

	/**
	 * check if status exists in database
	 *
	 * @param id status ID
	 * @return true if found
	 */
	public boolean containsStatus(long id) {
		String[] args = {Long.toString(id)};
		SQLiteDatabase db = getDbRead();
		Cursor c = db.query(StatusTable.NAME, null, STATUS_SELECT, args, null, null, SINGLE_ITEM);
		boolean result = c.moveToFirst();
		c.close();
		return result;
	}

	/**
	 * check if status exists in database
	 *
	 * @param id status ID
	 * @return true if found
	 */
	public boolean containsLogin(long id) {
		String[] args = {Long.toString(id)};
		SQLiteDatabase db = getDbRead();
		Cursor c = db.query(AccountTable.NAME, null, ACCOUNT_SELECTION, args, null, null, SINGLE_ITEM);
		boolean result = c.moveToFirst();
		c.close();
		return result;
	}

	/**
	 * remove user from mention results
	 *
	 * @param id   user ID
	 * @param mute true remove user status from mention results
	 */
	public void muteUser(long id, boolean mute) {
		SQLiteDatabase db = getDbWrite();
		int flags = getUserFlags(db, id);
		if (mute) {
			flags |= EXCLUDE_MASK;
		} else {
			flags &= ~EXCLUDE_MASK;
		}
		saveUserFlags(db, id, flags);
		commit(db);
	}

	/**
	 * get all user logins
	 *
	 * @return list of all logins
	 */
	public List<Account> getLogins() {
		ArrayList<Account> result = new ArrayList<>();

		SQLiteDatabase db = getDbRead();
		Cursor cursor = db.query(AccountTable.NAME, DatabaseAccount.COLUMNS, null, null, null, null, SORT_BY_CREATION);
		if (cursor.moveToFirst()) {
			result.ensureCapacity(cursor.getCount());
			do {
				DatabaseAccount account = new DatabaseAccount(cursor);
				account.addUser(getUser(account.getId(), account));
				result.add(account);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
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
		// check if there is an embedded status
		if (result.getEmbeddedStatusId() > 1L) {
			result.setEmbeddedStatus(getStatus(result.getEmbeddedStatusId()));
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
		if (result.getLocationId() != 0L) {
			Location location = getLocation(db, result.getLocationId());
			if (location != null) {
				result.addLocation(location);
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
	private List<Status> getStatuses(Cursor cursor, SQLiteDatabase db) {
		List<Status> result = new LinkedList<>();
		if (cursor.moveToFirst()) {
			do {
				Status status = getStatus(cursor, db);
				result.add(status);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
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
	 * @param db     database instance
	 * @param userID ID of the user
	 * @return user flags
	 */
	private int getUserFlags(SQLiteDatabase db, long userID) {
		String[] args = {Long.toString(userID), Long.toString(settings.getLogin().getId())};

		Cursor c = db.query(UserRegisterTable.NAME, USER_REG_COLUMN, USER_REG_SELECT, args, null, null, null, SINGLE_ITEM);
		int result = 0;
		if (c.moveToFirst())
			result = c.getInt(0);
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
			flags |= VERIFIED_MASK;
		} else {
			flags &= ~VERIFIED_MASK;
		}
		if (user.isProtected()) {
			flags |= LOCKED_MASK;
		} else {
			flags &= ~LOCKED_MASK;
		}
		if (user.followRequested()) {
			flags |= FOLLOW_REQUEST_MASK;
		} else {
			flags &= ~FOLLOW_REQUEST_MASK;
		}
		if (user.hasDefaultProfileImage()) {
			flags |= DEFAULT_IMAGE_MASK;
		} else {
			flags &= ~DEFAULT_IMAGE_MASK;
		}
		ContentValues userColumn = new ContentValues(13);
		userColumn.put(UserTable.ID, user.getId());
		userColumn.put(UserTable.USERNAME, user.getUsername());
		userColumn.put(UserTable.SCREENNAME, user.getScreenname());
		userColumn.put(UserTable.IMAGE, user.getOriginalProfileImageUrl());
		userColumn.put(UserTable.DESCRIPTION, user.getDescription());
		userColumn.put(UserTable.LINK, user.getProfileUrl());
		userColumn.put(UserTable.LOCATION, user.getLocation());
		userColumn.put(UserTable.BANNER, user.getOriginalBannerImageUrl());
		userColumn.put(UserTable.SINCE, user.getTimestamp());
		userColumn.put(UserTable.FRIENDS, user.getFollowing());
		userColumn.put(UserTable.FOLLOWER, user.getFollower());
		userColumn.put(UserTable.STATUSES, user.getStatusCount());
		userColumn.put(UserTable.FAVORITS, user.getFavoriteCount());

		db.insertWithOnConflict(UserTable.NAME, "", userColumn, mode);
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
		long rtId = -1L;
		if (rtStat != null) {
			saveStatus(rtStat, db, 0);
			rtId = rtStat.getId();
		}
		flags |= getStatusFlags(db, status.getId());
		if (status.isFavorited()) {
			flags |= FAVORITE_MASK;
		} else {
			flags &= ~FAVORITE_MASK;
		}
		if (status.isReposted()) {
			flags |= REPOST_MASK;
		} else {
			flags &= ~REPOST_MASK;
		}
		if (status.isSensitive()) {
			flags |= MEDIA_SENS_MASK;
		} else {
			flags &= ~MEDIA_SENS_MASK;
		}
		if (status.isBookmarked()) {
			flags |= BOOKMARK_MASK;
		} else {
			flags &= ~BOOKMARK_MASK;
		}
		ContentValues statusUpdate = new ContentValues(18);
		statusUpdate.put(StatusTable.ID, status.getId());
		statusUpdate.put(StatusTable.USER, user.getId());
		statusUpdate.put(StatusTable.TIME, status.getTimestamp());
		statusUpdate.put(StatusTable.TEXT, status.getText());
		statusUpdate.put(StatusTable.EMBEDDED, rtId);
		statusUpdate.put(StatusTable.SOURCE, status.getSource());
		statusUpdate.put(StatusTable.URL, status.getUrl());
		statusUpdate.put(StatusTable.REPLYSTATUS, status.getRepliedStatusId());
		statusUpdate.put(StatusTable.REPOST, status.getRepostCount());
		statusUpdate.put(StatusTable.FAVORITE, status.getFavoriteCount());
		statusUpdate.put(StatusTable.REPLYUSER, status.getRepliedUserId());
		statusUpdate.put(StatusTable.REPLYUSER, status.getRepliedUserId());
		statusUpdate.put(StatusTable.REPLYNAME, status.getReplyName());
		statusUpdate.put(StatusTable.CONVERSATION, status.getConversationId());
		if (status.getLocation() != null && status.getLocation().getId() != 0L) {
			statusUpdate.put(StatusTable.LOCATION, status.getLocation().getId());
			saveLocation(status.getLocation(), db);
		} else {
			statusUpdate.put(StatusTable.LOCATION, 0L);
		}
		if (status.getMedia().length > 0) {
			StringBuilder buf = new StringBuilder();
			saveMedia(status.getMedia(), db);
			for (Media media : status.getMedia()) {
				buf.append(media.getKey()).append(';');
			}
			String mediaKeys = buf.deleteCharAt(buf.length() - 1).toString();
			statusUpdate.put(StatusTable.MEDIA, mediaKeys);
		}
		if (status.getEmojis().length > 0) {
			StringBuilder buf = new StringBuilder();
			saveEmojis(status.getEmojis(), db);
			for (Emoji emoji : status.getEmojis()) {
				buf.append(emoji.getCode()).append(';');
			}
			String emojiKeys = buf.deleteCharAt(buf.length() - 1).toString();
			statusUpdate.put(StatusTable.EMOJI, emojiKeys);
		}
		db.insertWithOnConflict(StatusTable.NAME, "", statusUpdate, CONFLICT_REPLACE);
		saveUser(user, db, CONFLICT_IGNORE);
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
			db.insertWithOnConflict(MediaTable.NAME, "", column, CONFLICT_IGNORE);
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
			ContentValues column = new ContentValues(3);
			column.put(EmojiTable.CODE, emoji.getCode());
			column.put(EmojiTable.URL, emoji.getUrl());
			column.put(EmojiTable.CATEGORY, emoji.getCategory());
			db.insertWithOnConflict(EmojiTable.NAME, "", column, CONFLICT_IGNORE);
		}
	}

	/**
	 * save location information
	 *
	 * @param location location information to save
	 * @param db       database write instance
	 */
	private void saveLocation(Location location, SQLiteDatabase db) {
		ContentValues locationColumn = new ContentValues(5);
		locationColumn.put(LocationTable.ID, location.getId());
		locationColumn.put(LocationTable.FULLNAME, location.getFullName());
		locationColumn.put(LocationTable.COORDINATES, location.getCoordinates());
		locationColumn.put(LocationTable.COUNTRY, location.getCountry());
		locationColumn.put(LocationTable.PLACE, location.getPlace());
		db.insertWithOnConflict(LocationTable.NAME, "", locationColumn, CONFLICT_IGNORE);
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

		ContentValues values = new ContentValues(4);
		values.put(StatusRegisterTable.REGISTER, flags);
		values.put(StatusRegisterTable.REPOST_ID, status.getRepostId());
		values.put(StatusRegisterTable.ID, status.getId());
		values.put(StatusRegisterTable.OWNER, settings.getLogin().getId());

		int count = db.update(StatusRegisterTable.NAME, values, STATUS_REG_SELECT, args);
		if (count == 0) {
			// create new entry if there isn't one
			db.insert(StatusRegisterTable.NAME, null, values);
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

		ContentValues values = new ContentValues(3);
		values.put(UserRegisterTable.ID, id);
		values.put(UserRegisterTable.OWNER, settings.getLogin().getId());
		values.put(UserRegisterTable.REGISTER, flags);

		int cnt = db.update(UserRegisterTable.NAME, values, USER_REG_SELECT, args);
		if (cnt == 0) {
			// create new entry if there isn't an entry
			db.insert(UserRegisterTable.NAME, null, values);
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
		column.put(FavoriteTable.STATUS_ID, statusId);
		column.put(FavoriteTable.OWNER_ID, ownerId);
		db.insertWithOnConflict(FavoriteTable.NAME, "", column, CONFLICT_REPLACE);
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
		column.put(BookmarkTable.STATUS_ID, statusId);
		column.put(BookmarkTable.OWNER_ID, ownerId);
		db.insertWithOnConflict(BookmarkTable.NAME, "", column, CONFLICT_REPLACE);
	}

	/**
	 * store direct message
	 *
	 * @param message direct message information
	 * @param db      database instance
	 */
	private void saveMessages(Message message, SQLiteDatabase db) {
		// store message information
		ContentValues messageColumn = new ContentValues(6);
		messageColumn.put(MessageTable.ID, message.getId());
		messageColumn.put(MessageTable.TIME, message.getTimestamp());
		messageColumn.put(MessageTable.FROM, message.getSender().getId());
		messageColumn.put(MessageTable.TO, message.getReceiverId());
		messageColumn.put(MessageTable.MESSAGE, message.getText());
		if (message.getMedia().length > 0) {
			StringBuilder keyBuf = new StringBuilder();
			for (Media media : message.getMedia())
				keyBuf.append(media.getKey()).append(';');
			keyBuf.deleteCharAt(keyBuf.length() - 1);
			messageColumn.put(MessageTable.MEDIA, keyBuf.toString());
			saveMedia(message.getMedia(), db);
		}
		db.insertWithOnConflict(MessageTable.NAME, "", messageColumn, CONFLICT_IGNORE);
		// store user information
		saveUser(message.getSender(), db, CONFLICT_IGNORE);
	}

	/**
	 * updates existing status
	 *
	 * @param status update of the status
	 * @param db     database instance
	 */
	private void updateStatus(Status status, SQLiteDatabase db) {
		String[] statusIdArg = {Long.toString(status.getId())};
		String[] userIdArg = {Long.toString(status.getAuthor().getId())};

		User user = status.getAuthor();
		int flags = getStatusFlags(db, status.getId());
		if (status.isReposted()) {
			flags |= REPOST_MASK;
		} else {
			flags &= ~REPOST_MASK;
		}
		if (status.isFavorited()) {
			flags |= FAVORITE_MASK;
		} else {
			flags &= ~FAVORITE_MASK;
		}
		if (status.isBookmarked()) {
			flags |= BOOKMARK_MASK;
		} else {
			flags &= ~BOOKMARK_MASK;
		}
		ContentValues statusUpdate = new ContentValues(7);
		statusUpdate.put(StatusTable.TEXT, status.getText());
		statusUpdate.put(StatusTable.REPOST, status.getRepostCount());
		statusUpdate.put(StatusTable.FAVORITE, status.getFavoriteCount());
		statusUpdate.put(StatusTable.REPLY, status.getReplyCount());
		statusUpdate.put(StatusTable.REPLYNAME, status.getReplyName());
		statusUpdate.put(StatusTable.SOURCE, status.getSource());
		statusUpdate.put(StatusTable.URL, status.getUrl());

		ContentValues userUpdate = new ContentValues(9);
		userUpdate.put(UserTable.USERNAME, user.getUsername());
		userUpdate.put(UserTable.SCREENNAME, user.getScreenname());
		userUpdate.put(UserTable.IMAGE, user.getOriginalProfileImageUrl());
		userUpdate.put(UserTable.DESCRIPTION, user.getDescription());
		userUpdate.put(UserTable.LINK, user.getProfileUrl());
		userUpdate.put(UserTable.LOCATION, user.getLocation());
		userUpdate.put(UserTable.BANNER, user.getOriginalBannerImageUrl());
		userUpdate.put(UserTable.FRIENDS, user.getFollowing());
		userUpdate.put(UserTable.FOLLOWER, user.getFollower());

		db.updateWithOnConflict(StatusTable.NAME, statusUpdate, STATUS_SELECT, statusIdArg, CONFLICT_REPLACE);
		db.updateWithOnConflict(UserTable.NAME, userUpdate, USER_SELECT, userIdArg, CONFLICT_IGNORE);
		saveStatusFlags(db, status, flags);
	}

	/**
	 * Get SQLite instance for reading database
	 *
	 * @return SQLite instance
	 */
	private synchronized SQLiteDatabase getDbRead() {
		return adapter.getDatabase();
	}

	/**
	 * GET SQLite instance for writing database
	 *
	 * @return SQLite instance
	 */
	private synchronized SQLiteDatabase getDbWrite() {
		SQLiteDatabase db = adapter.getDatabase();
		db.beginTransaction();
		return db;
	}

	/**
	 * Commit changes and close Database
	 *
	 * @param db database instance
	 */
	private synchronized void commit(SQLiteDatabase db) {
		db.setTransactionSuccessful();
		db.endTransaction();
	}
}