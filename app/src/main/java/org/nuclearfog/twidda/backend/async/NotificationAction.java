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
public class NotificationAction extends AsyncExecutor<NotificationAction.NotificationActionParam, NotificationAction.NotificationActionResult> {

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
	protected NotificationActionResult doInBackground(@NonNull NotificationActionParam param) {
		try {
			switch (param.mode) {
				case NotificationActionParam.DATABASE:
					Notification result = db.getNotification(param.id);
					if (result != null) {
						return new NotificationActionResult(NotificationActionResult.DATABASE, param.id, result, null);
					}

				case NotificationActionParam.ONLINE:
					result = connection.getNotification(param.id);
					return new NotificationActionResult(NotificationActionResult.ONLINE, param.id, result, null);

				case NotificationActionParam.DISMISS:
					connection.dismissNotification(param.id);
					db.removeNotification(param.id);
					return new NotificationActionResult(NotificationActionResult.DISMISS, param.id, null, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			if (exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND) {
				db.removeNotification(param.id);
			}
			return new NotificationActionResult(NotificationActionResult.ERROR, param.id, null, exception);
		}
	}

	/**
	 *
	 */
	public static class NotificationActionParam {

		public static final int DATABASE = 1;
		public static final int ONLINE = 2;
		public static final int DISMISS = 3;

		final int mode;
		final long id;

		public NotificationActionParam(int mode, long id) {
			this.mode = mode;
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class NotificationActionResult {

		public static final int ERROR = -1;
		public static final int DATABASE = 3;
		public static final int ONLINE = 4;
		public static final int DISMISS = 5;

		@Nullable
		public final Notification notification;
		@Nullable
		public final ConnectionException exception;
		public final int mode;
		public final long id;

		NotificationActionResult(int mode, long id, @Nullable Notification notification, @Nullable ConnectionException exception) {
			this.exception = exception;
			this.notification = notification;
			this.mode = mode;
			this.id = id;
		}
	}
}