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
	 * configurations for Mastodon
	 */
	MASTODON(Account.API_MASTODON);

	private final String name;
	private final int accountType;
	private final boolean searchFilterEnabled;
	private final boolean profileLocationEnabled;
	private final boolean profileUrlEnabled;
	private final boolean postLocationSupported;
	private final boolean notificationDismissSupported;
	private final boolean statusSpoilerSupported;
	private final boolean statusVisibilitySupported;
	private final boolean emojiSupported;
	private final boolean statusEditSupported;
	private final boolean webpushSupported;
	private final boolean filterSupported;
	private final boolean publicTimelinesupported;
	private final boolean userlistSubscriberSupported;
	private final boolean userlistMembershipSupported;
	private final boolean outgoingFollowRequestSupported;
	private final int arrayResHome;

	/**
	 * @param accountType account login type, see {@link Account}
	 */
	Configuration(int accountType) {
		this.accountType = accountType;
		switch (accountType) {
			default:
			case Account.API_MASTODON:
				name = "Mastodon";
				searchFilterEnabled = false;
				profileLocationEnabled = false;
				profileUrlEnabled = false;
				postLocationSupported = false;
				notificationDismissSupported = true;
				statusSpoilerSupported = true;
				statusVisibilitySupported = true;
				emojiSupported = true;
				statusEditSupported = true;
				webpushSupported = true;
				filterSupported = true;
				publicTimelinesupported = true;
				userlistSubscriberSupported = false;
				userlistMembershipSupported = false;
				outgoingFollowRequestSupported = false;
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
	 * @return true if a public timeline is supported
	 */
	public boolean isPublicTimelinesupported() {
		return publicTimelinesupported;
	}

	/**
	 * @return true if showing subscribers of an userlist is supported
	 */
	public boolean isUserlistSubscriberSupported() {
		return userlistSubscriberSupported;
	}

	/**
	 * @return true if showing userlist members is supported
	 */
	public boolean isUserlistMembershipSupported() {
		return userlistMembershipSupported;
	}

	/**
	 * @return true if showing outgoing follow request is supported
	 */
	public boolean isOutgoingFollowRequestSupported() {
		return outgoingFollowRequestSupported;
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