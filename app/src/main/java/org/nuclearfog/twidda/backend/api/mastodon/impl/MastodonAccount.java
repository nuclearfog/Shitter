package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;

/**
 * implementation of a mastodon account
 *
 * @author nuclearfog
 */
public class MastodonAccount implements Account {

	private static final long serialVersionUID = -3212031070966866336L;

	private long id;
	private long timestamp;

	private String hostname;
	private String bearer;
	private String client_id, client_secret;

	private User user;

	/**
	 * @param user          user information
	 * @param hostname      hostname of the Mastodon isntance
	 * @param bearer        bearer token
	 * @param client_id     app client ID
	 * @param client_secret app client secret
	 */
	public MastodonAccount(User user, String hostname, String bearer, String client_id, String client_secret) {
		this.user = user;
		this.hostname = hostname;
		this.bearer = bearer;
		this.client_id = client_id;
		this.client_secret = client_secret;
		timestamp = System.currentTimeMillis();
		id = user.getId();
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public long getTimestamp() {
		return timestamp;
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
	public Configuration getConfiguration() {
		return Configuration.MASTODON;
	}


	@Override
	public boolean usingDefaultTokens() {
		return false;
	}


	@NonNull
	@Override
	public String toString() {
		if (getUser() != null)
			return getUser().toString();
		return "";
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Account))
			return false;
		Account account = (Account) obj;
		if (account.getUser() != null && getUser() != null)
			return getUser().equals(account.getUser());
		return false;
	}
}