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
public class NotificationAction extends AsyncExecutor<NotificationAction.NotificationParam, NotificationAction.NotificationResult> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public NotificationAction(Context context) {
		connection = ConnectionManager.getConnection(context);
		db = new AppDatabase(context);
	}


	@NonNull
	@Override
	protected NotificationResult doInBackground(@NonNull NotificationParam param) {
		try {
			switch (param.mode) {
				case NotificationParam.DATABASE:
					Notification result = db.getNotification(param.id);
					if (result != null) {
						return new NotificationResult(NotificationResult.DATABASE, result, null);
					}

				case NotificationParam.ONLINE:
					result = connection.getNotification(param.id);
					return new NotificationResult(NotificationResult.ONLINE, result, null);
			}
		} catch (ConnectionException exception) {
			return new NotificationResult(NotificationResult.ERROR, null, exception);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return new NotificationResult(NotificationResult.ERROR, null, null);
	}

	/**
	 *
	 */
	public static class NotificationParam {

		public static final int DATABASE = 1;
		public static final int ONLINE = 2;

		public final int mode;
		public final long id;

		public NotificationParam(int mode, long id) {
			this.mode = mode;
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class NotificationResult {

		public static final int ERROR = -1;
		public static final int DATABASE = 3;
		public static final int ONLINE = 4;
		@Nullable
		public final Notification notification;
		@Nullable
		public final ConnectionException exception;
		public final int mode;

		public NotificationResult(int mode, @Nullable Notification notification, @Nullable ConnectionException exception) {
			this.exception = exception;
			this.notification = notification;
			this.mode = mode;
		}
	}
}