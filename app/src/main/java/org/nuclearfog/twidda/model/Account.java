package org.nuclearfog.twidda.model;

import androidx.annotation.Nullable;

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
	 * API ID if undefined
	 */
	int API_NONE = 0;

	/**
	 * API ID for twitter version 1.1
	 */
	int API_TWITTER_1 = 1;

	/**
	 * API ID for twitter version 2.0
	 */
	int API_TWITTER_2 = 3;

	/**
	 * API ID used for Mastodon accounts
	 */
	int API_MASTODON = 2;

	/**
	 * @return ID of the account (user ID)
	 */
	long getId();

	/**
	 * @return date of the first login
	 */
	long getLoginDate();

	/**
	 * @return user information of the account
	 */
	@Nullable
	User getUser();

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

	/**
	 * return true if the account uses the app default API tokens
	 * currently only used by Twitter logins
	 *
	 * @return true if this account uses default app integrated API tokens
	 */
	boolean usingDefaultTokens();
}