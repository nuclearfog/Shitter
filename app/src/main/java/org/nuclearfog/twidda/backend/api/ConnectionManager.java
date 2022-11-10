package org.nuclearfog.twidda.backend.api;

import android.content.Context;

import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.database.GlobalSettings.OnSettingsChangeListener;

/**
 * this class manages multiple API implementations depending on settings
 *
 * @author nuclearfog
 */
public class ConnectionManager {

	private static Connection connection;
	private static boolean notifySettingsChange = false;

	private ConnectionManager() {
	}

	/**
	 * get singleton class of a connection
	 *
	 * @return singleton instance
	 */
	public static Connection get(Context context) {
		// create new singleton instance if there is none or if settings change
		if (notifySettingsChange || connection == null) {
			notifySettingsChange = false;
			// todo add connection selector
			connection = new Twitter(context);
			GlobalSettings settings = GlobalSettings.getInstance(context);
			settings.addSettingsChangeListener(new OnSettingsChangeListener() {
				@Override
				public void onSettingsChange() {
					notifySettingsChange = true;
				}
			});
		}
		return connection;
	}
}