package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.database.DatabaseAdapter.AccountTable;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;

/**
 * database implementation of account
 *
 * @author nuclearfog
 */
public class DatabaseAccount implements Account, AccountTable {

	private static final long serialVersionUID = -2276274593772105348L;

	/**
	 * projection of the columns with fixed order
	 */
	public static final String[] COLUMNS = {ID, API, DATE, ACCESS_TOKEN, TOKEN_SECRET, CLIENT_ID, CLIENT_SECRET, BEARER, HOSTNAME};

	private long userId;
	private long loginDate;
	private int apiType;
	private String accessToken = "";
	private String tokenSecret = "";
	private String consumerToken = "";
	private String consumerSecret = "";
	private String bearerToken = "";
	private String host = "";
	private User user;

	/**
	 * @param cursor database cursor using this {@link #COLUMNS}
	 */
	public DatabaseAccount(Cursor cursor) {
		userId = cursor.getLong(0);
		apiType = cursor.getInt(1);
		loginDate = cursor.getLong(2);
		String accessToken = cursor.getString(3);
		String tokenSecret = cursor.getString(4);
		String consumerToken = cursor.getString(5);
		String consumerSecret = cursor.getString(6);
		String bearerToken = cursor.getString(7);
		String host = cursor.getString(8);

		if (accessToken != null)
			this.accessToken = accessToken;
		if (tokenSecret != null)
			this.tokenSecret = tokenSecret;
		if (consumerToken != null)
			this.consumerToken = consumerToken;
		if (consumerSecret != null)
			this.consumerSecret = consumerSecret;
		if (bearerToken != null)
			this.bearerToken = bearerToken;
		if (host != null)
			this.host = host;
	}


	@Override
	public long getId() {
		return userId;
	}


	@Override
	public long getTimestamp() {
		return loginDate;
	}


	@Nullable
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
		return accessToken;
	}


	@Override
	public String getOauthSecret() {
		return tokenSecret;
	}


	@Override
	public String getBearerToken() {
		return bearerToken;
	}


	@Override
	public String getHostname() {
		return host;
	}


	@Override
	public Configuration getConfiguration() {
		switch (apiType) {
			case API_TWITTER_1:
				return Configuration.TWITTER1;

			case API_TWITTER_2:
				return Configuration.TWITTER2;

			case API_MASTODON:
				return Configuration.MASTODON;

			default:
				throw new RuntimeException("wrong API type: " + apiType);
		}
	}


	@Override
	public boolean usingDefaultTokens() {
		if (apiType != API_TWITTER_1 && apiType != Account.API_TWITTER_2)
			return false;
		return consumerToken == null || consumerToken.isEmpty() || consumerSecret == null || consumerSecret.isEmpty();
	}


	@NonNull
	@Override
	public String toString() {
		return "hostname=\"" + getHostname() + "\" configuration=\"" + getConfiguration().getName() + "\" " + user;
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

	/**
	 * attach user information
	 *
	 * @param user user associated with this account
	 */
	public void addUser(@Nullable User user) {
		this.user = user;
	}
}