package org.nuclearfog.twidda.backend.api;

import android.content.Context;

import org.nuclearfog.twidda.backend.api.mastodon.Mastodon;
import org.nuclearfog.twidda.backend.api.twitter.impl.v1.TwitterV1;
import org.nuclearfog.twidda.backend.api.twitter.impl.v2.TwitterV2;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.database.GlobalSettings.OnSettingsChangeListener;
import org.nuclearfog.twidda.model.Account;

/**
 * this class manages multiple API implementations depending on settings
 *
 * @author nuclearfog
 */
public class ConnectionManager {

	/**
	 * select connection to a social network automatically
	 */
	public static final int SELECT_AUTO = 0;

	/**
	 * select Twitter connection
	 */
	public static final int SELECT_TWITTER_1 = 1;

	/**
	 * select Twitter connection
	 */
	public static final int SELECT_TWITTER_2 = 2;

	/**
	 * select Mastodon connection
	 */
	public static final int SELECT_MASTODON = 3;

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
	 * @param select select instance type to create {@link #SELECT_AUTO,#SELECT_TWITTER}
	 * @return singleton instance
	 */
	public static Connection get(Context context, int select) {
		// create new singleton instance if there is none or if settings change
		if (notifySettingsChange || connection == null || select != SELECT_AUTO) {
			notifySettingsChange = false;
			GlobalSettings settings = GlobalSettings.getInstance(context);
			// select automatically
			if (select == SELECT_AUTO) {
				int apiType = settings.getLogin().getApiType();
				switch(apiType) {
					case Account.API_TWITTER_1:
						connection = new TwitterV1(context);
						break;

					case Account.API_TWITTER_2:
						connection = new TwitterV2(context);
						break;

					case Account.API_MASTODON:
						connection = new Mastodon(context);
						break;

					default:
						throw new RuntimeException("no connection selected!");
				}
			} else {
				switch(select) {
					case SELECT_TWITTER_1:
						connection = new TwitterV1(context);
						break;

					case SELECT_TWITTER_2:
						connection = new TwitterV2(context);
						break;

					case SELECT_MASTODON:
						connection = new Mastodon(context);
						break;

					default:
						throw new RuntimeException("no connection selected!");
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