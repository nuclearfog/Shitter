package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
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

	@Nullable
	private ConnectionException exception;
	private int pos;


	public NotificationLoader(NotificationFragment fragment, int pos) {
		super();
		callback = new WeakReference<>(fragment);
		connection = ConnectionManager.get(fragment.getContext());
		this.pos = pos;
	}


	@Override
	protected List<Notification> doInBackground(Long... ids) {
		try {
			return connection.getNotifications(ids[0], ids[1]);
		} catch (ConnectionException exception) {
			this.exception = exception;
		}
		return null;
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