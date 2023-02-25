package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.ui.activities.SettingsActivity;

import java.util.List;

/**
 * Background task to load location information used by twitter such as location names and world ID's
 *
 * @author nuclearfog
 * @see SettingsActivity
 */
public class LocationLoader extends AsyncExecutor<Void, LocationLoader.LocationLoaderResult> {

	private Connection connection;

	/**
	 *
	 */
	public LocationLoader(Context context) {
		connection = ConnectionManager.getConnection(context);
	}


	@NonNull
	@Override
	protected LocationLoaderResult doInBackground(@NonNull Void v) {
		try {
			List<Location> locations = connection.getLocations();
			return new LocationLoaderResult(locations, null);
		} catch (ConnectionException exception) {
			return new LocationLoaderResult(null, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new LocationLoaderResult(null, null);
	}

	/**
	 *
	 */
	public static class LocationLoaderResult {

		@Nullable
		public final List<Location> locations;
		@Nullable
		public final ConnectionException exception;

		LocationLoaderResult(@Nullable List<Location> locations, @Nullable ConnectionException exception) {
			this.locations = locations;
			this.exception = exception;
		}
	}
}