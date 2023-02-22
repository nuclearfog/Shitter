package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.NotificationTable;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;

/**
 * Database implementation of a notification
 *
 * @author nuclerfog
 */
public class DatabaseNotification implements Notification {

	private static final long serialVersionUID = 436155941776152806L;

	private long id, timestamp, itemId;
	private int type;

	private User user;
	@Nullable
	private Status status;

	/**
	 * @param cursor  database cursor containing Notification table column
	 * @param account current user information
	 */
	public DatabaseNotification(Cursor cursor, Account account) {
		user = new DatabaseUser(cursor, account);
		id = cursor.getLong(cursor.getColumnIndexOrThrow(NotificationTable.ID));
		itemId = cursor.getLong(cursor.getColumnIndexOrThrow(NotificationTable.ITEM));
		type = cursor.getInt(cursor.getColumnIndexOrThrow(NotificationTable.TYPE));
		timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(NotificationTable.TIME));
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public int getType() {
		return type;
	}


	@Override
	public long getTimestamp() {
		return timestamp;
	}


	@Override
	public User getUser() {
		return user;
	}


	@Nullable
	@Override
	public Status getStatus() {
		return status;
	}

	/**
	 * attach status information
	 *
	 * @param status status information
	 */
	public void addStatus(Status status) {
		this.status = status;
	}

	/**
	 * get ID of the attached item (user/status ID)
	 *
	 * @return item ID
	 */
	public long getItemId() {
		return itemId;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Notification))
			return false;
		return ((Notification) obj).getId() == id;
	}


	@Override
	public int compareTo(Notification notification) {
		return Long.compare(notification.getTimestamp(), timestamp);
	}


	@NonNull
	@Override
	public String toString() {
		return "id=" + id + " " + user;
	}
}