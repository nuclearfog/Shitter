package org.nuclearfog.twidda.backend.api;

import android.content.Context;

import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.database.GlobalSettings.OnSettingsChangeListener;
import org.nuclearfog.twidda.model.Account;

/**
 * this class manages multiple API implementations depending on settings
 *
 * @author nuclearfog
 */
public class ConnectionManager {

	public static final int SELECT_AUTO = 0;

	public static final int SELECT_TWITTER = 1;

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
		return get(context, SELECT_AUTO);
	}

	/**
	 * get singleton class of a connection
	 *
	 * @return singleton instance
	 * @param select select instance type to create {@link #SELECT_AUTO,#SELECT_TWITTER}
	 */
	public static Connection get(Context context, int select) {
		// create new singleton instance if there is none or if settings change
		if (notifySettingsChange || connection == null || select != SELECT_AUTO) {
			notifySettingsChange = false;
			GlobalSettings settings = GlobalSettings.getInstance(context);
			// create Twitter instance
			if (select == SELECT_TWITTER) {
				connection = new Twitter(context);
			}
			// select automatically
			else {
				if (settings.getApiId() == Account.API_TWITTER) {
					connection = new Twitter(context);
				}
			}
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