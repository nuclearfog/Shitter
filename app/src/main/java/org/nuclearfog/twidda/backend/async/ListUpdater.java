package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.backend.update.UserListUpdate;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.activities.UserlistEditor;

import java.lang.ref.WeakReference;

/**
 * This class creates and updates user lists
 * Backend for {@link UserlistEditor}
 *
 * @author nuclearfog
 */
public class ListUpdater extends AsyncTask<Void, Void, UserList> {

	private WeakReference<UserlistEditor> weakRef;
	private Connection connection;

	@Nullable
	private ConnectionException exception;
	private UserListUpdate update;

	/**
	 * @param activity callback to {@link UserlistEditor}
	 * @param update   userlist to update
	 */
	public ListUpdater(UserlistEditor activity, UserListUpdate update) {
		super();
		weakRef = new WeakReference<>(activity);
		connection = Twitter.get(activity);
		this.update = update;
	}


	@Override
	protected UserList doInBackground(Void... v) {
		try {
			if (update.exists())
				return connection.updateUserlist(update);
			return connection.createUserlist(update);
		} catch (ConnectionException exception) {
			this.exception = exception;
		}
		return null;
	}


	@Override
	protected void onPostExecute(UserList result) {
		UserlistEditor activity = weakRef.get();
		if (activity != null) {
			if (result != null) {
				activity.onSuccess(result);
			} else {
				activity.onError(exception);
			}
		}
	}
}