package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Credentials;

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
	private String screenName, profileImage;

	/**
	 * @param credentials   account credentials
	 * @param hostname      hostname of the Mastodon isntance
	 * @param bearer        bearer token
	 * @param client_id     app client ID
	 * @param client_secret app client secret
	 */
	public MastodonAccount(Credentials credentials, String hostname, String bearer, String client_id, String client_secret) {
		this.hostname = hostname;
		this.bearer = bearer;
		this.client_id = client_id;
		this.client_secret = client_secret;
		id = credentials.getId();
		timestamp = System.currentTimeMillis();
		screenName = credentials.getUsername();
		profileImage = ""; // todo implement this
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public String getScreenname() {
		return screenName;
	}


	@Override
	public String getProfileImageUrl() {
		return profileImage;
	}


	@Override
	public long getTimestamp() {
		return timestamp;
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


	@NonNull
	@Override
	public String toString() {
		return "hostname=\"" + getHostname() + "\" configuration=\"" + getConfiguration().getName() + "\" id=" + id;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Account))
			return false;
		Account account = (Account) obj;
		return account.getId() == getId() && account.getHostname().equals(getHostname());
	}
}