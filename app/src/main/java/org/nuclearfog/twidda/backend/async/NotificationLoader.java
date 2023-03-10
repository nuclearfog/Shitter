package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.ui.fragments.NotificationFragment;

import java.util.List;

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
		connection = ConnectionManager.getConnection(context);
		db = new AppDatabase(context);
	}


	@NonNull
	@Override
	protected NotificationLoaderResult doInBackground(@NonNull NotificationLoaderParam params) {
		List<Notification> result = null;
		try {
			if (params.minId == 0L && params.maxId == 0L) {
				result = db.getNotifications();
				if (result.isEmpty()) {
					result = connection.getNotifications(0L, 0L);
					db.saveNotifications(result);
				}
			} else {
				result = connection.getNotifications(params.minId, params.maxId);
				if (params.maxId == 0L) {
					db.saveNotifications(result);
				}
			}
		} catch (ConnectionException exception) {
			return new NotificationLoaderResult(null, params.position, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new NotificationLoaderResult(result, params.position, null);
	}

	/**
	 *
	 */
	public static class NotificationLoaderParam {

		public final int position;
		public final long minId, maxId;

		public NotificationLoaderParam(int position, long minId, long maxId) {
			this.position = position;
			this.minId = minId;
			this.maxId = maxId;
		}
	}

	/**
	 *
	 */
	public static class NotificationLoaderResult {

		public final int position;
		@Nullable
		public final List<Notification> notifications;
		@Nullable
		public final ConnectionException exception;

		NotificationLoaderResult(@Nullable List<Notification> notifications, int position, @Nullable ConnectionException exception) {
			this.notifications = notifications;
			this.exception = exception;
			this.position = position;
		}
	}
}