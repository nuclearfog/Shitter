package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.activities.UserlistActivity;

import java.lang.ref.WeakReference;

/**
 * async task to load list information and take action to the list
 *
 * @author nuclearfog
 */
public class ListAction extends AsyncTask<Void, Void, UserList> {

	/**
	 * load userlist information
	 */
	public static final int LOAD = 1;

	/**
	 * unfollow user list
	 */
	public static final int FOLLOW = 2;

	/**
	 * unfollow user list
	 */
	public static final int UNFOLLOW = 3;

	/**
	 * delete user list
	 */
	public static final int DELETE = 4;


	private WeakReference<UserlistActivity> weakRef;
	private Connection connection;
	private ConnectionException exception;

	private long listId;
	private int action;

	/**
	 * @param activity Callback to update list information
	 * @param listId   ID of the list to process
	 * @param action   what action should be performed
	 */
	public ListAction(UserlistActivity activity, long listId, int action) {
		super();
		weakRef = new WeakReference<>(activity);
		connection = ConnectionManager.get(activity);
		this.listId = listId;
		this.action = action;
	}


	@Override
	protected UserList doInBackground(Void... v) {
		try {
			switch (action) {
				case LOAD:
					return connection.getUserlist(listId);

				case FOLLOW:
					return connection.followUserlist(listId);

				case UNFOLLOW:
					return connection.unfollowUserlist(listId);

				case DELETE:
					return connection.deleteUserlist(listId);
			}
		} catch (ConnectionException exception) {
			this.exception = exception;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	protected void onPostExecute(@Nullable UserList userList) {
		UserlistActivity callback = this.weakRef.get();
		if (callback != null) {
			if (userList != null) {
				callback.onSuccess(userList, action);
			} else {
				callback.onFailure(exception, listId);
			}
		}
	}
}