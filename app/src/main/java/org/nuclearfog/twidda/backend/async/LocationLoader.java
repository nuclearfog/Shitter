package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
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

	private WeakReference<SettingsActivity> weakRef;
	private Connection connection;

	@Nullable
	private ConnectionException exception;


	public LocationLoader(SettingsActivity activity) {
		super();
		weakRef = new WeakReference<>(activity);
		connection = ConnectionManager.get(activity);
	}


	@Override
	protected List<Location> doInBackground(Void[] v) {
		try {
			return connection.getLocations();
		} catch (ConnectionException exception) {
			this.exception = exception;
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
				activity.onError(exception);
			}
		}
	}
}