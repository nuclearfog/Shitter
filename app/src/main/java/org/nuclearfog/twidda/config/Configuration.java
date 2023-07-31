package org.nuclearfog.twidda.config;

import androidx.annotation.ArrayRes;

import org.nuclearfog.twidda.R;
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

	private final String name;
	private final int accountType;
	private final boolean userlistExtended;
	private final boolean userlistDescriptionEnabled;
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
	private final boolean emojiSupported;
	private final boolean statusEditSupported;
	private final boolean webpushSupported;
	private final boolean filterSupported;
	private final int arrayResHome;

	/**
	 * @param accountType account login type, see {@link Account}
	 */
	Configuration(int accountType) {
		this.accountType = accountType;
		switch (accountType) {
			case Account.API_TWITTER_1:
			case Account.API_TWITTER_2:
				name = "Twitter";
				userlistExtended = true;
				searchFilterEnabled = true;
				profileLocationEnabled = true;
				profileUrlEnabled = true;
				idBlocklistEnabled = true;
				postLocationSupported = true;
				userlistVisibility = true;
				userlistDescriptionEnabled = true;
				notificationDismissSupported = false;
				statusSpoilerSupported = false;
				statusVisibilitySupported = false;
				directMessageSupported = true;
				emojiSupported = false;
				statusEditSupported = false;
				webpushSupported = false;
				filterSupported = false;
				arrayResHome = R.array.home_twitter_tab_icons;
				break;

			default:
			case Account.API_MASTODON:
				name = "Mastodon";
				userlistExtended = false;
				userlistDescriptionEnabled = false;
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
				emojiSupported = true;
				statusEditSupported = true;
				webpushSupported = true;
				filterSupported = true;
				arrayResHome = R.array.home_mastodon_tab_icons;
				break;
		}
	}

	/**
	 * @return network name
	 */
	public String getName() {
		return name;
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
	public boolean showUserlistsExtras() {
		return userlistExtended;
	}

	/**
	 * @return true if userlist description is supported
	 */
	public boolean userlsitDescriptionSupported() {
		return userlistDescriptionEnabled;
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
	 * @return true if text emojis are supported
	 */
	public boolean isEmojiSupported() {
		return emojiSupported;
	}

	/**
	 * @return true if status edit is supported
	 */
	public boolean isStatusEditSupported() {
		return statusEditSupported;
	}

	/**
	 * @return true if network supports push subscription
	 */
	public boolean isWebpushSupported() {
		return webpushSupported;
	}

	/**
	 * @return true if status filter is supported
	 */
	public boolean isFilterSupported() {
		return filterSupported;
	}

	/**
	 * get home tabitems drawable IDs
	 *
	 * @return Integer array resource containing drawable IDs
	 */
	@ArrayRes
	public int getHomeTabIcons() {
		return arrayResHome;
	}
}