package org.nuclearfog.twidda.database;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static org.nuclearfog.twidda.database.DatabaseAdapter.FavoriteTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.MessageTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.NotificationTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.StatusRegisterTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.StatusTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.TrendTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.UserRegisterTable;
import static org.nuclearfog.twidda.database.DatabaseAdapter.UserTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.lists.Messages;
import org.nuclearfog.twidda.database.impl.MessageImpl;
import org.nuclearfog.twidda.database.impl.NotificationImpl;
import org.nuclearfog.twidda.database.impl.StatusImpl;
import org.nuclearfog.twidda.database.impl.TrendImpl;
import org.nuclearfog.twidda.database.impl.UserImpl;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Message;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.model.User;

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
			+ " ORDER BY " + StatusTable.ID
			+ " DESC LIMIT ?;";

	/**
	 * SQL query to get status of an user
	 */
	private static final String USER_STATUS_QUERY = "SELECT * FROM(" + STATUS_SUBQUERY + ")"
			+ " WHERE " + StatusRegisterTable.NAME + "." + StatusRegisterTable.REGISTER + "&" + USER_TIMELINE_MASK + " IS NOT 0"
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.OWNER + "=?"
			+ " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
			+ " AND " + StatusTable.NAME + "." + StatusTable.USER + "=?"
			+ " ORDER BY " + StatusTable.ID
			+ " DESC LIMIT ?;";

	/**
	 * SQL query to get status favored by an user
	 */
	private static final String USERFAVORIT_QUERY = "SELECT * FROM(" + STATUS_SUBQUERY + ")"
			+ " INNER JOIN " + FavoriteTable.NAME
			+ " ON " + StatusTable.NAME + "." + StatusTable.ID + "=" + FavoriteTable.NAME + "." + FavoriteTable.STATUS_ID
			+ " WHERE " + FavoriteTable.NAME + "." + FavoriteTable.FAVORITER_ID + "=?"
			+ " AND " + StatusRegisterTable.NAME + "." + StatusRegisterTable.OWNER + "=?"
			+ " AND " + UserRegisterTable.NAME + "." + UserRegisterTable.OWNER + "=?"
			+ " ORDER BY " + StatusTable.ID
			+ " DESC LIMIT ?;";

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
			+ " WHERE " + UserTable.NAME + "." + UserTable.ID + "=? LIMIT 1;";

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
			+ " ORDER BY " + StatusTable.ID + " DESC LIMIT ?;";

	/**
	 * SQL query to get current user's messages
	 */
	private static final String MESSAGE_QUERY = "SELECT * FROM " + MessageTable.NAME
			+ " INNER JOIN " + UserTable.NAME
			+ " ON " + MessageTable.NAME + "." + MessageTable.FROM + "=" + UserTable.NAME + "." + UserTable.ID
			+ " INNER JOIN " + UserRegisterTable.NAME
			+ " ON " + MessageTable.NAME + "." + MessageTable.FROM + "=" + UserRegisterTable.NAME + "." + UserRegisterTable.ID
			+ " WHERE " + MessageTable.FROM + "=? OR " + MessageTable.TO + "=?"
			+ " ORDER BY " + MessageTable.SINCE + " DESC LIMIT ?;";

	/**
	 * SQL query to get notifications
	 */
	private static final String NOTIFICATION_QUERY = "SELECT * FROM " + NotificationTable.NAME
			+ " INNER JOIN(" + USER_SUBQUERY + ")" + UserTable.NAME
			+ " ON " + NotificationTable.NAME + "." + NotificationTable.USER + "=" + UserTable.NAME + "." + UserTable.ID
			+ " WHERE " + NotificationTable.NAME + "." + NotificationTable.OWNER + "=?"
			+ " ORDER BY " + NotificationTable.ID + " DESC LIMIT ?;";

	/**
	 * select status entries from favorite table matching status ID
	 * this status can be favored by multiple users
	 */
	private static final String FAVORITE_SELECT_STATUS = FavoriteTable.STATUS_ID + "=?";

	/**
	 * select all statuses from favorite table favored by given user
	 */
	private static final String FAVORITE_SELECT_OWNER = FavoriteTable.FAVORITER_ID + "=?";

	/**
	 * select specific status from favorite table
	 */
	private static final String FAVORITE_SELECT = FAVORITE_SELECT_STATUS + " AND " + FAVORITE_SELECT_OWNER;

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
	 * select user from user table matching user ID
	 */
	private static final String USER_SELECT = UserTable.NAME + "." + UserTable.ID + "=?";

	/**
	 * selection to get status register
	 */
	private static final String STATUS_REG_SELECT = StatusRegisterTable.ID + "=? AND " + StatusRegisterTable.OWNER + "=?";

	/**
	 * selection to get user register
	 */
	private static final String USER_REG_SELECT = UserRegisterTable.ID + "=? AND " + UserRegisterTable.OWNER + "=?";

	/**
	 * column projection for user register
	 */
	private static final String[] USER_REG_COLUMN = {UserRegisterTable.REGISTER};

	/**
	 * column projection for status register
	 */
	private static final String[] STATUS_REG_COLUMN = {StatusRegisterTable.REGISTER};

	/**
	 * default order for trend rows
	 */
	private static final String TREND_ORDER = TrendTable.INDEX + " ASC";

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
	 * @param fav     status favored by user
	 * @param ownerId user ID
	 */
	public void saveFavoriteTimeline(List<Status> fav, long ownerId) {
		SQLiteDatabase db = getDbWrite();
		removeOldFavorites(db, ownerId);
		for (Status status : fav) {
			saveStatus(status, db, 0);
			saveFavorite(status.getId(), ownerId, db);
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
		String[] args = {Integer.toString(settings.getTrendLocation().getWorldId())};
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
	public void saveToFavorites(Status status) {
		if (status.getEmbeddedStatus() != null)
			status = status.getEmbeddedStatus();
		SQLiteDatabase db = getDbWrite();
		saveStatus(status, db, 0);
		saveFavorite(status.getId(), settings.getLogin().getId(), db);
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
			column.put(NotificationTable.DATE, notification.getCreatedAt());
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
	 * load home timeline
	 *
	 * @return home timeline
	 */
	public List<Status> getHomeTimeline() {
		String homeStr = Long.toString(settings.getLogin().getId());
		String[] args = {homeStr, homeStr, Integer.toString(settings.getListSize())};

		SQLiteDatabase db = getDbRead();
		List<Status> result = new LinkedList<>();
		Cursor cursor = db.rawQuery(HOME_QUERY, args);
		if (cursor.moveToFirst()) {
			do {
				Status status = getStatus(cursor);
				result.add(status);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
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
		List<Status> result = new LinkedList<>();
		Cursor cursor = db.rawQuery(USER_STATUS_QUERY, args);
		if (cursor.moveToFirst()) {
			do {
				Status status = getStatus(cursor);
				result.add(status);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
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
		List<Status> result = new LinkedList<>();
		Cursor cursor = db.rawQuery(USERFAVORIT_QUERY, args);
		if (cursor.moveToFirst()) {
			do {
				Status status = getStatus(cursor);
				result.add(status);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
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
	 * @param userId ID of user
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
			user = new UserImpl(cursor, account);
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
			result = getStatus(cursor);
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
		List<Status> result = new LinkedList<>();
		Cursor cursor = db.rawQuery(REPLY_QUERY, args);
		if (cursor.moveToFirst()) {
			do {
				Status status = getStatus(cursor);
				result.add(status);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
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
				NotificationImpl notification = new NotificationImpl(cursor, login);
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
	 * remove status from database
	 *
	 * @param id status ID
	 */
	public void removeStatus(long id) {
		String[] args = {Long.toString(id)};

		SQLiteDatabase db = getDbWrite();
		db.delete(StatusTable.NAME, STATUS_SELECT, args);
		db.delete(FavoriteTable.NAME, FAVORITE_SELECT_STATUS, args);
		commit(db);
	}

	/**
	 * hide or unhide status reply
	 *
	 * @param id   ID of the reply
	 * @param hide true to hide this status
	 */
	public void hideReply(long id, boolean hide) {
		String[] args = {Long.toString(id), Long.toString(settings.getLogin().getId())};

		SQLiteDatabase db = getDbWrite();
		int register = getStatusRegister(db, id);
		if (hide)
			register |= HIDDEN_MASK;
		else
			register &= ~HIDDEN_MASK;

		ContentValues values = new ContentValues(3);
		values.put(StatusRegisterTable.REGISTER, register);
		db.update(StatusRegisterTable.NAME, values, STATUS_REG_SELECT, args);
		commit(db);
	}

	/**
	 * remove status from favorites
	 *
	 * @param status status to remove from the favorites
	 */
	public void removeFavorite(Status status) {
		String[] delArgs = {Long.toString(status.getId()), Long.toString(settings.getLogin().getId())};

		if (status.getEmbeddedStatus() != null) {
			status = status.getEmbeddedStatus();
		}
		SQLiteDatabase db = getDbWrite();
		// get status register
		int register = getStatusRegister(db, status.getId());
		register &= ~FAVORITE_MASK; // unset favorite flag
		// update database
		saveStatusRegister(db, status, register);
		db.delete(FavoriteTable.NAME, FAVORITE_SELECT, delArgs);
		commit(db);
	}

	/**
	 * Delete Direct Message
	 *
	 * @param id Direct Message ID
	 */
	public void deleteMessage(long id) {
		String[] messageId = {Long.toString(id)};

		SQLiteDatabase db = getDbWrite();
		db.delete(MessageTable.NAME, MESSAGE_SELECT, messageId);
		commit(db);
	}

	/**
	 * Load trend List
	 *
	 * @return list of trends
	 */
	public List<Trend> getTrends() {
		String[] args = {Integer.toString(settings.getTrendLocation().getWorldId())};
		SQLiteDatabase db = getDbRead();
		Cursor cursor = db.query(TrendTable.NAME, TrendImpl.COLUMNS, TREND_SELECT, args, null, null, TREND_ORDER);
		List<Trend> trends = new LinkedList<>();
		if (cursor.moveToFirst()) {
			do {
				trends.add(new TrendImpl(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
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
				result.add(new MessageImpl(cursor, login));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
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
	 * remove user from mention results
	 *
	 * @param id   user ID
	 * @param mute true remove user status from mention results
	 */
	public void muteUser(long id, boolean mute) {
		SQLiteDatabase db = getDbWrite();
		int register = getUserRegister(db, id);
		if (mute) {
			register |= EXCLUDE_MASK;
		} else {
			register &= ~EXCLUDE_MASK;
		}
		saveUserRegister(db, id, register);
		commit(db);
	}

	/**
	 * set register of a status. Update if an entry exists
	 *
	 * @param db       database instance
	 * @param status   status
	 * @param register status register
	 */
	public void saveStatusRegister(SQLiteDatabase db, Status status, int register) {
		String[] args = {Long.toString(status.getId()), Long.toString(settings.getLogin().getId())};

		ContentValues values = new ContentValues(4);
		values.put(StatusRegisterTable.REGISTER, register);
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
	 * @param db       database instance
	 * @param id       User ID
	 * @param register status register
	 */
	public void saveUserRegister(SQLiteDatabase db, long id, int register) {
		String[] args = {Long.toString(id), Long.toString(settings.getLogin().getId())};

		ContentValues values = new ContentValues(3);
		values.put(UserRegisterTable.ID, id);
		values.put(UserRegisterTable.OWNER, settings.getLogin().getId());
		values.put(UserRegisterTable.REGISTER, register);

		int cnt = db.update(UserRegisterTable.NAME, values, USER_REG_SELECT, args);
		if (cnt == 0) {
			// create new entry if there isn't an entry
			db.insert(UserRegisterTable.NAME, null, values);
		}
	}

	/**
	 * get status information from database
	 *
	 * @param cursor cursor containing status informations
	 * @return status
	 */
	private Status getStatus(Cursor cursor) {
		Account login = settings.getLogin();
		StatusImpl result = new StatusImpl(cursor, login);
		// check if there is an embedded status
		if (result.getEmbeddedStatusId() > 1)
			result.setEmbeddedStatus(getStatus(result.getEmbeddedStatusId()));
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
		int register = getUserRegister(db, user.getId());
		if (user.isVerified())
			register |= VERIFIED_MASK;
		else
			register &= ~VERIFIED_MASK;
		if (user.isProtected())
			register |= LOCKED_MASK;
		else
			register &= ~LOCKED_MASK;
		if (user.followRequested())
			register |= FOLLOW_REQUEST_MASK;
		else
			register &= ~FOLLOW_REQUEST_MASK;
		if (user.hasDefaultProfileImage())
			register |= DEFAULT_IMAGE_MASK;
		else
			register &= ~DEFAULT_IMAGE_MASK;

		ContentValues userColumn = new ContentValues(13);
		userColumn.put(UserTable.ID, user.getId());
		userColumn.put(UserTable.USERNAME, user.getUsername());
		userColumn.put(UserTable.SCREENNAME, user.getScreenname());
		userColumn.put(UserTable.IMAGE, user.getOriginalProfileImageUrl());
		userColumn.put(UserTable.DESCRIPTION, user.getDescription());
		userColumn.put(UserTable.LINK, user.getProfileUrl());
		userColumn.put(UserTable.LOCATION, user.getLocation());
		userColumn.put(UserTable.BANNER, user.getOriginalBannerImageUrl());
		userColumn.put(UserTable.SINCE, user.getCreatedAt());
		userColumn.put(UserTable.FRIENDS, user.getFollowing());
		userColumn.put(UserTable.FOLLOWER, user.getFollower());
		userColumn.put(UserTable.STATUSES, user.getStatusCount());
		userColumn.put(UserTable.FAVORITS, user.getFavoriteCount());

		db.insertWithOnConflict(UserTable.NAME, "", userColumn, mode);
		saveUserRegister(db, user.getId(), register);
	}


	/**
	 * save status into database
	 *
	 * @param status      status information
	 * @param statusFlags predefined status status register or zero if there isn't one
	 * @param db          SQLite database
	 */
	private void saveStatus(Status status, SQLiteDatabase db, int statusFlags) {
		User user = status.getAuthor();
		Status rtStat = status.getEmbeddedStatus();
		long rtId = -1L;
		if (rtStat != null) {
			saveStatus(rtStat, db, 0);
			rtId = rtStat.getId();
		}
		statusFlags |= getStatusRegister(db, status.getId());
		if (status.isFavorited()) {
			statusFlags |= FAVORITE_MASK;
		} else {
			statusFlags &= ~FAVORITE_MASK;
		}
		if (status.isReposted()) {
			statusFlags |= REPOST_MASK;
		} else {
			statusFlags &= ~REPOST_MASK;
		}
		if (status.isSensitive()) {
			statusFlags |= MEDIA_SENS_MASK;
		} else {
			statusFlags &= ~MEDIA_SENS_MASK;
		}
		ContentValues statusUpdate = new ContentValues(16);
		statusUpdate.put(StatusTable.ID, status.getId());
		statusUpdate.put(StatusTable.USER, user.getId());
		statusUpdate.put(StatusTable.SINCE, status.getTimestamp());
		statusUpdate.put(StatusTable.TEXT, status.getText());
		statusUpdate.put(StatusTable.EMBEDDED, rtId);
		statusUpdate.put(StatusTable.SOURCE, status.getSource());
		statusUpdate.put(StatusTable.REPLYSTATUS, status.getRepliedStatusId());
		statusUpdate.put(StatusTable.REPOST, status.getRepostCount());
		statusUpdate.put(StatusTable.FAVORITE, status.getFavoriteCount());
		statusUpdate.put(StatusTable.REPLYUSER, status.getRepliedUserId());
		statusUpdate.put(StatusTable.REPLYUSER, status.getRepliedUserId());
		statusUpdate.put(StatusTable.REPLYNAME, status.getReplyName());
		statusUpdate.put(StatusTable.CONVERSATION, status.getConversationId());
		if (status.getLocation() != null) {
			statusUpdate.put(StatusTable.PLACE, status.getLocation().getPlace());
			statusUpdate.put(StatusTable.COORDINATE, status.getLocation().getCoordinates());
		}
		db.insertWithOnConflict(StatusTable.NAME, "", statusUpdate, CONFLICT_REPLACE);
		saveUser(user, db, CONFLICT_IGNORE);
		saveStatusRegister(db, status, statusFlags);
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
		int register = getStatusRegister(db, status.getId());
		if (status.isReposted())
			register |= REPOST_MASK;
		else
			register &= ~REPOST_MASK;
		if (status.isFavorited())
			register |= FAVORITE_MASK;
		else
			register &= ~FAVORITE_MASK;

		ContentValues statusUpdate = new ContentValues(6);
		statusUpdate.put(StatusTable.TEXT, status.getText());
		statusUpdate.put(StatusTable.REPOST, status.getRepostCount());
		statusUpdate.put(StatusTable.FAVORITE, status.getFavoriteCount());
		statusUpdate.put(StatusTable.REPLYNAME, status.getReplyName());

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

		db.update(StatusTable.NAME, statusUpdate, STATUS_SELECT, statusIdArg);
		db.update(UserTable.NAME, userUpdate, USER_SELECT, userIdArg);
		saveStatusRegister(db, status, register);
	}

	/**
	 * Store status into favorite table of a user
	 *
	 * @param statusId ID of the favored status
	 * @param ownerId  ID of the favorite list owner
	 * @param db       database instance
	 */
	private void saveFavorite(long statusId, long ownerId, SQLiteDatabase db) {
		ContentValues favTable = new ContentValues(2);
		favTable.put(FavoriteTable.STATUS_ID, statusId);
		favTable.put(FavoriteTable.FAVORITER_ID, ownerId);
		db.insertWithOnConflict(FavoriteTable.NAME, "", favTable, CONFLICT_REPLACE);
	}

	/**
	 * clear old favorites from table
	 *
	 * @param db     database instance
	 * @param userId ID of the favorite list owner
	 */
	private void removeOldFavorites(SQLiteDatabase db, long userId) {
		String[] delArgs = {Long.toString(userId)};
		db.delete(FavoriteTable.NAME, FAVORITE_SELECT_OWNER, delArgs);
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
		messageColumn.put(MessageTable.SINCE, message.getTimestamp());
		messageColumn.put(MessageTable.FROM, message.getSender().getId());
		messageColumn.put(MessageTable.TO, message.getReceiverId());
		messageColumn.put(MessageTable.MESSAGE, message.getText());
		if (message.getMedia() != null)
			messageColumn.put(MessageTable.MEDIA, message.getMedia().toString());
		db.insertWithOnConflict(MessageTable.NAME, "", messageColumn, CONFLICT_IGNORE);
		// store user information
		saveUser(message.getSender(), db, CONFLICT_IGNORE);
	}

	/**
	 * get register of a status or zero if status not found
	 *
	 * @param db database instance
	 * @param id ID of the status
	 * @return status register
	 */
	private int getStatusRegister(SQLiteDatabase db, long id) {
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
	private int getUserRegister(SQLiteDatabase db, long userID) {
		String[] args = {Long.toString(userID), Long.toString(settings.getLogin().getId())};

		Cursor c = db.query(UserRegisterTable.NAME, USER_REG_COLUMN, USER_REG_SELECT, args, null, null, null, SINGLE_ITEM);
		int result = 0;
		if (c.moveToFirst())
			result = c.getInt(0);
		c.close();
		return result;
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