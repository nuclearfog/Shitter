package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.ui.fragments.NotificationFragment;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Notification loader for {@link NotificationFragment}
 *
 * @author nuclearfog
 */
public class NotificationLoader extends AsyncTask<Long, Void, List<Notification>> {

	private WeakReference<NotificationFragment> callback;
	private Connection connection;
	private AppDatabase db;

	@Nullable
	private ConnectionException exception;
	private int pos;

	/**
	 * @param fragment callback to fragment
	 * @param pos      index where to insert the new items in the lsit
	 */
	public NotificationLoader(NotificationFragment fragment, int pos) {
		super();
		callback = new WeakReference<>(fragment);
		connection = ConnectionManager.get(fragment.getContext());
		db = new AppDatabase(fragment.getContext());
		this.pos = pos;
	}


	@Override
	protected List<Notification> doInBackground(Long... ids) {
		long minId = ids[0];
		long maxId = ids[1];
		List<Notification> result = null;
		try {
			if (minId == 0 && maxId == 0) {
				result = db.getNotifications();
				if (result.isEmpty()) {
					result = connection.getNotifications(0, 0);
					db.saveNotifications(result);
				}
			} else {
				result = connection.getNotifications(minId, maxId);
				if (maxId == 0) {
					db.saveNotifications(result);
				}
			}
		} catch (ConnectionException exception) {
			this.exception = exception;
		}
		return result;
	}


	@Override
	protected void onPostExecute(List<Notification> notifications) {
		NotificationFragment fragment = callback.get();
		if (fragment != null) {
			if (notifications != null) {
				fragment.onSuccess(notifications, pos);
			} else {
				fragment.onError(exception);
			}
		}
	}
}