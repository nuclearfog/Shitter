package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.backend.api.twitter.TwitterException;
import org.nuclearfog.twidda.backend.api.twitter.update.UserlistUpdate;
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
	private TwitterException err;
	private Twitter twitter;

	private UserlistUpdate update;

	/**
	 * @param activity callback to {@link UserlistEditor}
	 * @param update   userlist to update
	 */
	public ListUpdater(UserlistEditor activity, UserlistUpdate update) {
		super();
		weakRef = new WeakReference<>(activity);
		twitter = Twitter.get(activity);
		this.update = update;
	}


	@Override
	protected UserList doInBackground(Void... v) {
		try {
			if (update.exists())
				return twitter.updateUserlist(update);
			return twitter.createUserlist(update);
		} catch (TwitterException err) {
			this.err = err;
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
				activity.onError(err);
			}
		}
	}
}