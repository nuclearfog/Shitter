package org.nuclearfog.twidda.model;

import org.nuclearfog.twidda.config.Configuration;

import java.io.Serializable;

/**
 * interface of account implementations
 * An account class collects information about a saved login.
 *
 * @author nuclearfog
 */
public interface Account extends Serializable {

	/**
	 * API ID used for Mastodon accounts
	 * used in database tables!
	 */
	int API_MASTODON = 2;

	/**
	 * @return ID of the account (user ID)
	 */
	long getId();

	/**
	 * @return date of the first login
	 */
	long getTimestamp();

	/**
	 * @return screen name of the account profile
	 */
	String getScreenname();

	/**
	 * @return profile image of the account profile
	 */
	String getProfileImageUrl();

	/**
	 * @return API key assosiated with an account
	 */
	String getConsumerToken();

	/**
	 * @return API secret key associated with an account
	 */
	String getConsumerSecret();

	/**
	 * @return oauth token
	 */
	String getOauthToken();

	/**
	 * @return oauth secret
	 */
	String getOauthSecret();

	/**
	 * @return bearer token
	 */
	String getBearerToken();

	/**
	 * @return hostname of the social network
	 */
	String getHostname();

	/**
	 * @return login configuration
	 */
	Configuration getConfiguration();
}