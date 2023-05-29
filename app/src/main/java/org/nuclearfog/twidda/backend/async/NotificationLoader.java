package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.lists.Notifications;
import org.nuclearfog.twidda.ui.fragments.NotificationFragment;

/**
 * Notification loader for {@link NotificationFragment}
 *
 * @author nuclearfog
 */
public class NotificationLoader extends AsyncExecutor<NotificationLoader.NotificationLoaderParam, NotificationLoader.NotificationLoaderResult> {

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
	protected NotificationLoaderResult doInBackground(@NonNull NotificationLoaderParam param) {
		try {
			switch (param.mode) {
				case NotificationLoaderParam.LOAD_ALL:
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
					return new NotificationLoaderResult(result, param.position, null);

				case NotificationLoaderParam.LOAD_UNREAD:
					// load (known) notifications from database first
					Notifications notifications = db.getNotifications();
					// then load new notifications using the latest known notification
					long minId = 0L;
					Notification lastNotification = notifications.peekFirst();
					if (lastNotification != null)
						minId = lastNotification.getId();
					result = connection.getNotifications(minId, 0L);
					return new NotificationLoaderResult(result, 0, null);
			}
		} catch (ConnectionException exception) {
			return new NotificationLoaderResult(null, param.position, exception);
		} catch (Exception exception) {
			if (BuildConfig.DEBUG) {
				exception.printStackTrace();
			}
		}
		return null;
	}

	/**
	 *
	 */
	public static class NotificationLoaderParam {

		public static final int LOAD_ALL = 1;
		public static final int LOAD_UNREAD = 2;

		final int position;
		final long minId, maxId;
		final int mode;

		public NotificationLoaderParam(int mode, int position, long minId, long maxId) {
			this.position = position;
			this.minId = minId;
			this.maxId = maxId;
			this.mode = mode;
		}
	}

	/**
	 *
	 */
	public static class NotificationLoaderResult {

		public final int position;
		@Nullable
		public final Notifications notifications;
		@Nullable
		public final ConnectionException exception;

		NotificationLoaderResult(@Nullable Notifications notifications, int position, @Nullable ConnectionException exception) {
			this.notifications = notifications;
			this.exception = exception;
			this.position = position;
		}
	}
}