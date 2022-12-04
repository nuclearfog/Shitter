package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.NotificationTable;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;

/**
 * Database implementation of a notification
 *
 * @author nuclerfog
 */
public class NotificationImpl implements Notification {

	private static final long serialVersionUID = 436155941776152806L;

	private long id, timestamp;
	private int type;

	private User user;
	private Status status;

	/**
	 * @param cursor database cursor containing Notification table column
	 */
	public NotificationImpl(Cursor cursor) {
		id = cursor.getLong(cursor.getColumnIndexOrThrow(NotificationTable.ID));
		type = cursor.getInt(cursor.getColumnIndexOrThrow(NotificationTable.TYPE));
		timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(NotificationTable.DATE));
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
	public long getCreatedAt() {
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
	 * attach user information
	 *
	 * @param user user information
	 */
	public void addUser(User user) {
		this.user = user;
	}

	/**
	 * attach status information
	 *
	 * @param status status information
	 */
	public void addStatus(Status status) {
		this.status = status;
	}
}