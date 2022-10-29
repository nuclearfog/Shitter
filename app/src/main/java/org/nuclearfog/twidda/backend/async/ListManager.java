package org.nuclearfog.twidda.backend.async;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.twitter.Twitter;

import java.lang.ref.WeakReference;

/**
 * Backend async task to manage users on lists
 * Twitter users can be added and removed
 *
 * @author nuclearfog
 */
public class ListManager extends AsyncTask<Void, Void, Void> {

	/**
	 * add user to list
	 */
	public static final int ADD_USER = 1;

	/**
	 * remove user from list
	 */
	public static final int DEL_USER = 2;

	private Connection connection;
	private WeakReference<ListManagerCallback> weakRef;

	@Nullable
	private ConnectionException exception;
	private long listId;
	private String username;
	private int action;

	/**
	 * @param c        activity context
	 * @param listId   ID of the user list
	 * @param action   what action should be performed
	 * @param username name of the user to add or remove
	 * @param callback callback to update information
	 */
	public ListManager(Context c, long listId, int action, String username, ListManagerCallback callback) {
		super();
		weakRef = new WeakReference<>(callback);
		connection = Twitter.get(c);
		this.listId = listId;
		this.action = action;
		this.username = username;
	}


	@Override
	protected Void doInBackground(Void... v) {
		try {
			switch (action) {
				case ADD_USER:
					connection.addUserToUserlist(listId, username);
					break;

				case DEL_USER:
					connection.removeUserFromUserlist(listId, username);
					break;
			}
		} catch (ConnectionException exception) {
			this.exception = exception;
		}
		return null;
	}


	@Override
	protected void onPostExecute(Void v) {
		ListManagerCallback callback = weakRef.get();
		if (callback != null) {
			if (exception == null) {
				callback.onSuccess(username);
			} else {
				callback.onFailure(exception);
			}
		}
	}

	/**
	 * Callback interface for Activities or fragments
	 */
	public interface ListManagerCallback {

		/**
		 * Called when AsyncTask finished successfully
		 *
		 * @param names the names of the users added or removed from list
		 */
		void onSuccess(String names);

		/**
		 * called when an error occurs
		 *
		 * @param err Engine exception thrown by backend
		 */
		void onFailure(@Nullable ConnectionException err);
	}
}