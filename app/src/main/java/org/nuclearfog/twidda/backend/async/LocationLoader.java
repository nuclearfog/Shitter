package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.backend.api.twitter.TwitterException;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.ui.activities.SettingsActivity;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Background task to load location information used by twitter such as location names and world ID's
 *
 * @author nuclearfog
 * @see SettingsActivity
 */
public class LocationLoader extends AsyncTask<Void, Void, List<Location>> {

	private TwitterException twException;
	private WeakReference<SettingsActivity> weakRef;
	private Twitter twitter;


	public LocationLoader(SettingsActivity activity) {
		super();
		weakRef = new WeakReference<>(activity);
		twitter = Twitter.get(activity);
	}


	@Override
	protected List<Location> doInBackground(Void[] v) {
		try {
			return twitter.getLocations();
		} catch (TwitterException twException) {
			this.twException = twException;
		}
		return null;
	}


	@Override
	protected void onPostExecute(List<Location> locations) {
		SettingsActivity activity = weakRef.get();
		if (activity != null) {
			if (locations != null) {
				activity.setLocationData(locations);
			} else {
				activity.onError(twException);
			}
		}
	}
}