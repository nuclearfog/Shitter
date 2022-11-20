package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;

/**
 * implementation of a mastodon account
 *
 * @author nuclearfog
 */
public class MastodonAccount implements Account {

	private long id;
	private long createdAt;

	private String hostname;
	private String bearer;
	private String client_id, client_secret;

	private User user;

	public MastodonAccount() {
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public long getLoginDate() {
		return createdAt;
	}


	@Nullable
	@Override
	public User getUser() {
		return user;
	}


	@Override
	public String getConsumerToken() {
		return client_id;
	}


	@Override
	public String getConsumerSecret() {
		return client_secret;
	}


	@Override
	public String getOauthToken() {
		return "";
	}


	@Override
	public String getOauthSecret() {
		return "";
	}


	@Override
	public String getBearerToken() {
		return bearer;
	}


	@Override
	public String getHostname() {
		return hostname;
	}


	@Override
	public int getApiType() {
		return API_MASTODON;
	}
}