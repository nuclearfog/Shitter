package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.lists.Notifications;

/**
 * Notification loader for {@link org.nuclearfog.twidda.ui.fragments.NotificationFragment}
 *
 * @author nuclearfog
 */
public class NotificationLoader extends AsyncExecutor<NotificationLoader.Param, NotificationLoader.Result> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public NotificationLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		db = new AppDatabase(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.mode) {
				case Param.LOAD_ALL:
					Notifications result;
					if (param.minId == 0L && param.maxId == 0L) {
						result = db.getNotifications();
						if (result.isEmpty()) {
							result = connection.getNotifications(0L, 0L);
							db.saveNotifications(result);
						}
					} else {
						result = connection.getNotifications(param.minId, param.maxId);
						if (param.maxId == 0L) {
							db.saveNotifications(result);
						}
					}
					return new Result(result, param.position, null);

				case Param.LOAD_UNREAD:
					// load (known) notifications from database first
					Notifications notifications = db.getNotifications();
					// then load new notifications using the latest known notification
					long minId = 0L;
					Notification lastNotification = notifications.peekFirst();
					if (lastNotification != null)
						minId = lastNotification.getId();
					result = connection.getNotifications(minId, 0L);
					return new Result(result, 0, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(null, param.position, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int LOAD_ALL = 1;
		public static final int LOAD_UNREAD = 2;

		final int position;
		final long minId, maxId;
		final int mode;

		/**
		 * @param mode     type of notification to load {@link #LOAD_ALL,#LOAD_UNREAD}
		 * @param position index where to insert the new notifications in the list/adapter
		 * @param minId    minimum ID of the notification or '0' to disable limitation
		 * @param maxId    maximum ID of the notification or '0' to disable limitation
		 */
		public Param(int mode, int position, long minId, long maxId) {
			this.position = position;
			this.minId = minId;
			this.maxId = maxId;
			this.mode = mode;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public final int position;
		@Nullable
		public final Notifications notifications;
		@Nullable
		public final ConnectionException exception;

		/**
		 * @param notifications notification list
		 * @param position  index where to insert the new notifications in the list/adapter
		 * @param exception exception if an error occurs while loading
		 */
		Result(@Nullable Notifications notifications, int position, @Nullable ConnectionException exception) {
			this.notifications = notifications;
			this.exception = exception;
			this.position = position;
		}
	}
}