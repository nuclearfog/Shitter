package org.nuclearfog.twidda.backend.api.twitter.impl;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;

public class AccountV1 implements Account {

	private long id;
	private long date;

	private String host;
	private String oauthToken, tokenSecret;
	private String apiKey, apiSec;

	private User user;

	public AccountV1(long id, String host, String oauthToken, String tokenSecret, String apiKey, String apiSec, User user) {
		this.id = id;
		this.host = host;
		this.oauthToken = oauthToken;
		this.tokenSecret = tokenSecret;
		this.apiKey = apiKey;
		this.apiSec = apiSec;
		this.user = user;
		date = System.currentTimeMillis();
	}


	@Override
	public long getId() {
		return id;
	}

	@Override
	public long getLoginDate() {
		return date;
	}

	@Nullable
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
		return host;
	}
}