package org.nuclearfog.twidda.config;

import org.nuclearfog.twidda.model.Account;

/**
 * Configurations for different networks
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
	MASTODON(Account.API_MASTODON),

	/**
	 * dummy configuration
	 */
	NONE(0);

	private final int accountType;
	private final boolean enableVote;
	private final boolean userlistExtended;
	private final boolean favoritsEnabled;
	private final boolean searchFilterEnabled;
	private final boolean profileLocationEnabled;
	private final boolean profileUrlEnabled;
	private final boolean idBlocklistEnabled;
	private final boolean postLocationSupported;

	/**
	 * @param accountType account login type, see {@link Account}
	 */
	Configuration(int accountType) {
		this.accountType = accountType;
		switch (accountType) {
			case Account.API_TWITTER_1:
			case Account.API_TWITTER_2:
				userlistExtended = true;
				favoritsEnabled = true;
				enableVote = false;
				searchFilterEnabled = true;
				profileLocationEnabled = true;
				profileUrlEnabled = true;
				idBlocklistEnabled = true;
				postLocationSupported = true;
				break;

			case Account.API_MASTODON:
				enableVote = true;
				userlistExtended = false;
				favoritsEnabled = false;
				searchFilterEnabled = false;
				profileLocationEnabled = false;
				profileUrlEnabled = false;
				idBlocklistEnabled = false;
				postLocationSupported = false;
				break;

			default:
				userlistExtended = false;
				favoritsEnabled = false;
				enableVote = false;
				searchFilterEnabled = false;
				profileLocationEnabled = false;
				profileUrlEnabled = false;
				idBlocklistEnabled = false;
				postLocationSupported = false;
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
	 * @return true if network supports voting
	 */
	public boolean voteEnabled() {
		return enableVote;
	}

	/**
	 * @return true to show extra userlist information
	 */
	public boolean showListExtras() {
		return userlistExtended;
	}

	/**
	 * @return true to enable favorite timeline for users
	 */
	public boolean favoritsEnabled() {
		return favoritsEnabled;
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
}