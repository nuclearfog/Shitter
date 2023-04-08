package org.nuclearfog.twidda.config;

import org.nuclearfog.twidda.model.Account;

/**
 * Configurations for different networks, containing static configuration
 *
 * @author nuclearfog
 */
public enum Configuration {

	/**
	 * configurations for Twitter API 1.1
	 */
	TWITTER1(Account.API_TWITTER_1),

	/**
	 * configurations for Twitter API 2.0
	 */
	TWITTER2(Account.API_TWITTER_2),

	/**
	 * configurations for Mastodon
	 */
	MASTODON(Account.API_MASTODON);

	/**
	 * fallback configuration to use when there is no network selected
	 */
	public static final Configuration FALLBACK_CONFIG = MASTODON;

	private final int accountType;
	private final boolean userlistExtended;
	private final boolean searchFilterEnabled;
	private final boolean profileLocationEnabled;
	private final boolean profileUrlEnabled;
	private final boolean idBlocklistEnabled;
	private final boolean postLocationSupported;
	private final boolean userlistVisibility;
	private final boolean notificationDismissSupported;
	private final boolean statusSpoilerSupported;
	private final boolean statusVisibilitySupported;
	private final boolean directMessageSupported;
	private final int maxImages;
	private final int maxGifs;
	private final int maxVideos;

	/**
	 * @param accountType account login type, see {@link Account}
	 */
	Configuration(int accountType) {
		this.accountType = accountType;
		switch (accountType) {
			case Account.API_TWITTER_1:
			case Account.API_TWITTER_2:
				userlistExtended = true;
				searchFilterEnabled = true;
				profileLocationEnabled = true;
				profileUrlEnabled = true;
				idBlocklistEnabled = true;
				postLocationSupported = true;
				userlistVisibility = true;
				notificationDismissSupported = false;
				statusSpoilerSupported = false;
				statusVisibilitySupported = false;
				directMessageSupported = true;
				maxImages = 4;
				maxGifs = 1;
				maxVideos = 1;
				break;

			default:
			case Account.API_MASTODON:
				userlistExtended = false;
				searchFilterEnabled = false;
				profileLocationEnabled = false;
				profileUrlEnabled = false;
				idBlocklistEnabled = false;
				postLocationSupported = false;
				userlistVisibility = false;
				notificationDismissSupported = true;
				statusSpoilerSupported = true;
				statusVisibilitySupported = true;
				directMessageSupported = false;
				maxImages = 4;
				maxGifs = 1;
				maxVideos = 1;
				break;
		}
	}

	/**
	 * @return account login type, see {@link Account}
	 */
	public int getAccountType() {
		return accountType;
	}

	/**
	 * @return true to show extra userlist information
	 */
	public boolean showListExtras() {
		return userlistExtended;
	}

	/**
	 * @return true if userlist visibility is supported
	 */
	public boolean userlistVisibilitySupported() {
		return userlistVisibility;
	}

	/**
	 * @return true if search filter option is enabled
	 */
	public boolean filterEnabled() {
		return searchFilterEnabled;
	}

	/**
	 * @return true if network supports profile location information
	 */
	public boolean profileLocationEnabled() {
		return profileLocationEnabled;
	}

	/**
	 * @return true if network supports profile url information
	 */
	public boolean profileUrlEnabled() {
		return profileUrlEnabled;
	}

	/**
	 * @return true to enable user ID filtering
	 */
	public boolean filterlistEnabled() {
		return idBlocklistEnabled;
	}

	/**
	 * @return true if posting location is supported
	 */
	public boolean locationSupported() {
		return postLocationSupported;
	}

	/**
	 * @return true if notification dismiss is supported
	 */
	public boolean notificationDismissEnabled() {
		return notificationDismissSupported;
	}

	/**
	 * @return true if login type supports warining for status spoiler
	 */
	public boolean statusSpoilerSupported() {
		return statusSpoilerSupported;
	}

	/**
	 * @return true if login type supports status visibility states
	 */
	public boolean statusVisibilitySupported() {
		return statusVisibilitySupported;
	}

	/**
	 * @return true if directmessaging is supported
	 */
	public boolean directmessageSupported() {
		return directMessageSupported;
	}

	/**
	 * @return image limit for posts
	 */
	public int getImageLimit() {
		return maxImages;
	}

	/**
	 * @return video limit for posts
	 */
	public int getVideoLimit() {
		return maxVideos;
	}

	/**
	 * @return gif limit for posts
	 */
	public int getGifLimit() {
		return maxGifs;
	}
}