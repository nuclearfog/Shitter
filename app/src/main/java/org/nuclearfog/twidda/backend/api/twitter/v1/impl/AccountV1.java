package org.nuclearfog.twidda.backend.api.twitter.v1.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;

/**
 * Twitter implementation of an account
 *
 * @author nuclearfog
 */
public class AccountV1 implements Account {

	private static final long serialVersionUID = 2013001328542861179L;

	private static final String TWITTER_HOST = "https://twitter.com";

	private long date;

	private String oauthToken, oauthSecret;
	private String consumerToken, consumerSecret;

	private User user;

	/**
	 * @param oauthToken  oauth access token
	 * @param tokenSecret oauth token secret
	 * @param user        user information
	 */
	public AccountV1(String oauthToken, String tokenSecret, User user) {
		this(oauthToken, tokenSecret, "", "", user);
	}

	/**
	 * @param consumerToken  API consumer token
	 * @param consumerSecret API consumer secret
	 * @param oauthToken     oauth access token
	 * @param tokenSecret    oauth token secret
	 * @param user           user information
	 */
	public AccountV1(String oauthToken, String tokenSecret, String consumerToken, String consumerSecret, User user) {
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
	public long getTimestamp() {
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
		return TWITTER_HOST;
	}


	@Override
	public Configuration getConfiguration() {
		return Configuration.TWITTER1;
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