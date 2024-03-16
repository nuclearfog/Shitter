package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Notification;

/**
 * Async loader to load and modify notification
 *
 * @author nuclearfog
 */
public class NotificationAction extends AsyncExecutor<NotificationAction.Param, NotificationAction.Result> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public NotificationAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		db = new AppDatabase(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.action) {
				case Param.LOAD_LOCAL:
					Notification result = db.getNotification(param.id);
					if (result != null) {
						return new Result(Result.LOAD_LOCAL, param.id, result, null);
					}

				case Param.LOAD_ONLINE:
					result = connection.getNotification(param.id);
					return new Result(Result.LOAD_ONLINE, param.id, result, null);

				case Param.DISMISS:
					connection.dismissNotification(param.id);
					db.removeNotification(param.id);
					return new Result(Result.DISMISS, param.id, null, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			if (exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND) {
				db.removeNotification(param.id);
			}
			return new Result(Result.ERROR, param.id, null, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int LOAD_LOCAL = 1;
		public static final int LOAD_ONLINE = 2;
		public static final int DISMISS = 3;

		final int action;
		final long id;

		/**
		 * @param action action to perform on notification {@link #LOAD_LOCAL,#LOAD_ONLINE,#DISMISS}
		 * @param id     ID of the notification
		 */
		public Param(int action, long id) {
			this.action = action;
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int LOAD_LOCAL = 10;
		public static final int LOAD_ONLINE = 11;
		public static final int DISMISS = 12;

		@Nullable
		public final Notification notification;
		@Nullable
		public final ConnectionException exception;
		public final int action;
		public final long id;

		/**
		 * @param action       action performed on the notification
		 * @param id           ID of the notification
		 * @param notification updated notification or null if an error occured
		 */
		Result(int action, long id, @Nullable Notification notification, @Nullable ConnectionException exception) {
			this.exception = exception;
			this.notification = notification;
			this.action = action;
			this.id = id;
		}
	}
}