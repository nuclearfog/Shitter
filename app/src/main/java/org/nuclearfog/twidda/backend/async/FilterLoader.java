package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.backend.api.twitter.TwitterException;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.database.FilterDatabase;
import org.nuclearfog.twidda.model.User;
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

	@Nullable
	private TwitterException err;
	private WeakReference<UsersActivity> weakRef;
	private FilterDatabase filterDatabase;
	private AppDatabase appDatabase;
	private Twitter twitter;

	private int mode;


	public FilterLoader(UsersActivity activity, int mode) {
		super();
		twitter = Twitter.get(activity);
		appDatabase = new AppDatabase(activity);
		filterDatabase = new FilterDatabase(activity);
		weakRef = new WeakReference<>(activity);
		this.mode = mode;
	}


	@Override
	protected Void doInBackground(String[] names) {
		try {
			switch (mode) {
				case REFRESH:
					List<Long> ids = twitter.getIdBlocklist();
					filterDatabase.setFilteredUserIds(ids);
					break;

				case MUTE_USER:
					User user = twitter.muteUser(names[0]);
					appDatabase.storeUser(user);
					break;

				case BLOCK_USER:
					user = twitter.blockUser(names[0]);
					appDatabase.storeUser(user);
					break;
			}
		} catch (TwitterException err) {
			this.err = err;
		}
		return null;
	}


	@Override
	protected void onPostExecute(Void v) {
		UsersActivity activity = weakRef.get();
		if (activity != null) {
			if (err == null) {
				activity.onSuccess(mode);
			} else {
				activity.onError(err);
			}
		}
	}
}