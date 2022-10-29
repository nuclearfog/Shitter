package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.backend.api.twitter.update.ProfileUpdate;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.activities.ProfileEditor;

import java.lang.ref.WeakReference;

/**
 * Background task for loading and editing profile information
 *
 * @author nuclearfog
 * @see ProfileEditor
 */
public class UserUpdater extends AsyncTask<Void, Void, User> {

	private WeakReference<ProfileEditor> weakRef;
	private Connection connection;
	private AppDatabase db;

	@Nullable
	private ConnectionException exception;
	private ProfileUpdate profile;

	public UserUpdater(ProfileEditor activity, ProfileUpdate profile) {
		super();
		weakRef = new WeakReference<>(activity);
		connection = Twitter.get(activity);
		db = new AppDatabase(activity);
		this.profile = profile;
	}


	@Override
	protected User doInBackground(Void[] v) {
		try {
			if (profile.getProfileImageStream() != null) {
				connection.updateProfileImage(profile.getProfileImageStream());
			}
			if (profile.getBannerImageStream() != null) {
				connection.updateBannerImage(profile.getBannerImageStream());
			}
			User user = connection.updateProfile(profile);
			// save new user information
			db.storeUser(user);
			return user;
		} catch (ConnectionException exception) {
			this.exception = exception;
		} finally {
			// close image streams
			profile.close();
		}
		return null;
	}


	@Override
	protected void onPostExecute(@Nullable User user) {
		ProfileEditor activity = weakRef.get();
		if (activity != null) {
			if (user != null) {
				activity.onSuccess(user);
			} else {
				activity.onError(exception);
			}
		}
	}
}