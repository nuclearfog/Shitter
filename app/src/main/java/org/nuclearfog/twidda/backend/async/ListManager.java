package org.nuclearfog.twidda.backend.async;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.ui.activities.UserlistActivity;

import java.lang.ref.WeakReference;

/**
 * Backend async task to manage users on lists
 * Twitter users can be added and removed
 *
 * @author nuclearfog
 */
public class ListManager extends AsyncTask<Void, Void, Boolean> {

	/**
	 * add user to list
	 */
	public static final int ADD_USER = 1;

	/**
	 * remove user from list
	 */
	public static final int DEL_USER = 2;

	private Connection connection;
	private WeakReference<UserlistActivity> weakRef;

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
	public ListManager(Context c, long listId, int action, String username, UserlistActivity callback) {
		super();
		weakRef = new WeakReference<>(callback);
		connection = ConnectionManager.get(c);
		this.listId = listId;
		this.action = action;
		this.username = username;
	}


	@Override
	protected Boolean doInBackground(Void... v) {
		try {
			switch (action) {
				case ADD_USER:
					connection.addUserToList(listId, username);
					return true;

				case DEL_USER:
					connection.removeUserFromList(listId, username);
					return true;
			}
		} catch (ConnectionException exception) {
			this.exception = exception;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}


	@Override
	protected void onPostExecute(Boolean success) {
		UserlistActivity callback = weakRef.get();
		if (callback != null) {
			if (success) {
				callback.onSuccess(action, username);
			} else {
				callback.onFailure(exception);
			}
		}
	}
}