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
public class DatabaseAccount implements Account {

	private static final long serialVersionUID = -2276274593772105348L;

	/**
	 * projection of the columns with fixed order
	 */
	public static final String[] COLUMNS = {
			AccountTable.ID,
			AccountTable.DATE,
			AccountTable.ACCESS_TOKEN,
			AccountTable.TOKEN_SECRET,
			AccountTable.CLIENT_ID,
			AccountTable.CLIENT_SECRET,
			AccountTable.BEARER,
			AccountTable.HOSTNAME,
			AccountTable.API
	};

	private long userId;
	private long loginDate;
	private int apiType;
	private String accessToken, tokenSecret;
	private String consumerToken, consumerSecret;
	private String bearerToken;
	private String host;
	private User user;

	/**
	 * @param cursor database cursor using this {@link #COLUMNS}
	 */
	public DatabaseAccount(Cursor cursor) {
		userId = cursor.getLong(0);
		loginDate = cursor.getLong(1);
		accessToken = cursor.getString(2);
		tokenSecret = cursor.getString(3);
		consumerToken = cursor.getString(4);
		consumerSecret = cursor.getString(5);
		bearerToken = cursor.getString(6);
		host = cursor.getString(7);
		apiType = cursor.getInt(8);
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
				return Configuration.FALLBACK_CONFIG;
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
		return "date=" + loginDate + " host=\"" + host + "\" user=" + user;
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