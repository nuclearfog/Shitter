package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.ui.activities.UsersActivity;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Backend of {@link UsersActivity}
 * performs user mute or block actions and stores a list of IDs with blocked/muted users
 * This list is used to filter search results
 *
 * @author nuclearfog
 */
public class FilterLoader extends AsyncTask<String, Void, Void> {

	/**
	 * refresh exclude list
	 */
	public static final int REFRESH = 1;

	/**
	 * mute specified user
	 */
	public static final int MUTE_USER = 2;

	/**
	 * block specified user
	 */
	public static final int BLOCK_USER = 3;

	private WeakReference<UsersActivity> weakRef;
	private Connection connection;
	private AppDatabase db;

	@Nullable
	private ConnectionException exception;
	private int mode;

	public FilterLoader(UsersActivity activity, int mode) {
		super();
		connection = ConnectionManager.get(activity);
		weakRef = new WeakReference<>(activity);
		db = new AppDatabase(activity);
		this.mode = mode;
	}


	@Override
	protected Void doInBackground(String... names) {
		try {
			switch (mode) {
				case REFRESH:
					List<Long> ids = connection.getIdBlocklist();
					db.setFilterlistUserIds(ids);
					break;

				case MUTE_USER:
					connection.muteUser(names[0]);
					break;

				case BLOCK_USER:
					connection.blockUser(names[0]);
					break;
			}
		} catch (ConnectionException exception) {
			this.exception = exception;
		}
		return null;
	}


	@Override
	protected void onPostExecute(Void v) {
		UsersActivity activity = weakRef.get();
		if (activity != null) {
			if (exception == null) {
				activity.onSuccess(mode);
			} else {
				activity.onError(exception);
			}
		}
	}
}