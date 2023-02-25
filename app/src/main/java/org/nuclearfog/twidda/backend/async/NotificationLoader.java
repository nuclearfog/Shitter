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
public class NotificationLoader extends AsyncExecutor<NotificationLoader.NotificationParam, NotificationLoader.NotificationResult> {

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
	protected NotificationResult doInBackground(@NonNull NotificationParam params) {
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
			return new NotificationResult(null, params.position, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new NotificationResult(result, params.position, null);
	}

	/**
	 *
	 */
	public static class NotificationParam {

		public final int position;
		public final long minId, maxId;

		public NotificationParam(int position, long minId, long maxId) {
			this.position = position;
			this.minId = minId;
			this.maxId = maxId;
		}
	}

	/**
	 *
	 */
	public static class NotificationResult {

		public final int position;
		@Nullable
		public final List<Notification> notifications;
		@Nullable
		public final ConnectionException exception;

		NotificationResult(@Nullable List<Notification> notifications, int position, @Nullable ConnectionException exception) {
			this.notifications = notifications;
			this.exception = exception;
			this.position = position;
		}
	}
}