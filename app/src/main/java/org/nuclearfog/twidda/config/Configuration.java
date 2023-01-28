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
	private boolean enableVote = false;
	private boolean userlistExtended = false;
	private boolean favoritsEnabled = false;

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
				break;

			case Account.API_MASTODON:
				enableVote = true;
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
}