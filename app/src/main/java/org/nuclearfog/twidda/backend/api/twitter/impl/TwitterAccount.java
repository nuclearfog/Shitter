package org.nuclearfog.twidda.backend.api.twitter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;

/**
 * Twitter implementation of an account
 *
 * @author nuclearfog
 */
public class TwitterAccount implements Account {

	private long date;

	private String oauthToken, oauthSecret;
	private String consumerToken, consumerSecret;

	private User user;

	/**
	 * @param oauthToken  oauth access token
	 * @param tokenSecret oauth token secret
	 * @param user        user information
	 */
	public TwitterAccount(String oauthToken, String tokenSecret, User user) {
		this(oauthToken, tokenSecret, "", "", user);
	}

	/**
	 * @param consumerToken  API consumer token
	 * @param consumerSecret API consumer secret
	 * @param oauthToken     oauth access token
	 * @param tokenSecret    oauth token secret
	 * @param user           user information
	 */
	public TwitterAccount(String oauthToken, String tokenSecret, String consumerToken, String consumerSecret, User user) {
		this.oauthToken = oauthToken;
		this.oauthSecret = tokenSecret;
		this.consumerToken = consumerToken;
		this.consumerSecret = consumerSecret;
		this.user = user;
		date = System.currentTimeMillis();
	}


	@Override
	public long getId() {
		return user.getId();
	}


	@Override
	public long getLoginDate() {
		return date;
	}


	@Override
	public User getUser() {
		return user;
	}


	@Override
	public String getConsumerToken() {
		return consumerToken;
	}


	@Override
	public String getConsumerSecret() {
		return consumerSecret;
	}


	@Override
	public String getOauthToken() {
		return oauthToken;
	}


	@Override
	public String getOauthSecret() {
		return oauthSecret;
	}


	@Override
	public String getBearerToken() {
		return "";
	}


	@Override
	public String getHostname() {
		return Twitter.API;
	}


	@Override
	public int getApiType() {
		return Account.API_TWITTER;
	}


	@Override
	public boolean usingDefaultTokens() {
		return consumerToken == null || consumerToken.isEmpty() || consumerSecret == null || consumerSecret.isEmpty();
	}


	@NonNull
	@Override
	public String toString() {
		return user.toString();
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Account))
			return false;
		return user.equals(((Account) obj).getUser());
	}
}