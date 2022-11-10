package org.nuclearfog.twidda.backend.api.twitter.impl;

import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;

/**
 * Twitter implementation of an account
 *
 * @author nuclearfog
 */
public class AccountV1 implements Account {

	private long date;

	private String oauthToken, tokenSecret;
	private String apiKey, apiSec;

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
	 * @param apiKey      API consumer token
	 * @param apiSec      API consumer secret
	 * @param oauthToken  oauth access token
	 * @param tokenSecret oauth token secret
	 * @param user        user information
	 */
	public AccountV1(String oauthToken, String tokenSecret, String apiKey, String apiSec, User user) {
		this.oauthToken = oauthToken;
		this.tokenSecret = tokenSecret;
		this.apiKey = apiKey;
		this.apiSec = apiSec;
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
	public String getApiKey() {
		return apiKey;
	}

	@Override
	public String getApiSecret() {
		return apiSec;
	}

	@Override
	public String getAccessToken() {
		return oauthToken;
	}

	@Override
	public String getTokenSecret() {
		return tokenSecret;
	}

	@Override
	public String getHostname() {
		return Twitter.API;
	}

	@Override
	public int getApiType() {
		return Account.API_TWITTER;
	}
}