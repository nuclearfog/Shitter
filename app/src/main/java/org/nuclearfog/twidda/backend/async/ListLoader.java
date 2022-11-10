package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.lists.UserLists;
import org.nuclearfog.twidda.ui.fragments.UserListFragment;

import java.lang.ref.WeakReference;

/**
 * Background task for downloading twitter lists created by a user
 *
 * @author nuclearfog
 * @see UserListFragment
 */
public class ListLoader extends AsyncTask<Long, Void, UserLists> {

	public static final long NO_CURSOR = -1;

	/**
	 * load userlists of an user
	 */
	public static final int LOAD_USERLISTS = 1;

	/**
	 * load userlists the specified user is on
	 */
	public static final int LOAD_MEMBERSHIPS = 2;


	@Nullable
	private ConnectionException exception;
	private WeakReference<UserListFragment> weakRef;
	private Connection connection;

	private int listType;
	private long userId;
	private String ownerName;

	/**
	 * @param fragment  callback to update information
	 * @param listType  type of list to load
	 * @param userId    ID of the userlist
	 * @param ownerName alternative if user id is not defined
	 */
	public ListLoader(UserListFragment fragment, int listType, long userId, String ownerName) {
		super();
		connection = ConnectionManager.get(fragment.getContext());
		weakRef = new WeakReference<>(fragment);

		this.listType = listType;
		this.userId = userId;
		this.ownerName = ownerName;
	}


	@Override
	protected UserLists doInBackground(Long[] param) {
		try {
			switch (listType) {
				case LOAD_USERLISTS:
					return connection.getUserlistOwnerships(userId, ownerName, 0L);

				case LOAD_MEMBERSHIPS:
					return connection.getUserlistMemberships(userId, ownerName, param[0]);
			}
		} catch (ConnectionException exception) {
			this.exception = exception;
		}
		return null;
	}


	@Override
	protected void onPostExecute(UserLists result) {
		UserListFragment fragment = weakRef.get();
		if (fragment != null) {
			if (result != null) {
				fragment.setData(result);
			} else {
				fragment.onError(exception);
			}
		}
	}
}