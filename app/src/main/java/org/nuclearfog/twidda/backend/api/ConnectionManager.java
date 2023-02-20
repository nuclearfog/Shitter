package org.nuclearfog.twidda.backend.api;

import android.content.Context;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.mastodon.Mastodon;
import org.nuclearfog.twidda.backend.api.twitter.impl.v1.TwitterV1;
import org.nuclearfog.twidda.backend.api.twitter.impl.v2.TwitterV2;
import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.config.GlobalSettings.OnSettingsChangeListener;

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
		return get(context, null);
	}

	/**
	 * get singleton class of a connection
	 *
	 * @param config Network selection or null to choose automatically
	 * @return singleton instance
	 */
	public static Connection get(Context context, @Nullable Configuration config) {
		// create new singleton instance if there is none or if settings change
		if (notifySettingsChange || connection == null) {
			notifySettingsChange = false;
			GlobalSettings settings = GlobalSettings.getInstance(context);
			// select automatically
			if (config == null)
				config = settings.getLogin().getConfiguration();
			switch (config) {
				case TWITTER1:
					connection = new TwitterV1(context);
					break;

				case TWITTER2:
					connection = new TwitterV2(context);
					break;

				case MASTODON:
					connection = new Mastodon(context);
					break;

				default:
					throw new RuntimeException("no connection selected!");
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