package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.database.DatabaseAdapter.AccountTable;
import org.nuclearfog.twidda.model.Account;

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
	public static final String[] COLUMNS = {ID, API, DATE, ACCESS_TOKEN, TOKEN_SECRET, CLIENT_ID, CLIENT_SECRET, BEARER, HOSTNAME, USERNAME, IMAGE};

	private long userId;
	private long loginDate;
	private int apiType;
	private String accessToken = "";
	private String tokenSecret = "";
	private String consumerToken = "";
	private String consumerSecret = "";
	private String bearerToken = "";
	private String hostname = "";
	private String screenName = "";
	private String profileImage = "";

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
		String hostname = cursor.getString(8);
		String screenName = cursor.getString(9);
		String profileImage = cursor.getString(10);

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
		if (hostname != null)
			this.hostname = hostname;
		if (screenName != null)
			this.screenName = screenName;
		if (profileImage != null)
			this.profileImage = profileImage;
	}


	@Override
	public long getId() {
		return userId;
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
		return loginDate;
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
		return hostname;
	}


	@Override
	public Configuration getConfiguration() {
		switch (apiType) {
			default:
			case API_MASTODON:
				return Configuration.MASTODON;
		}
	}


	@NonNull
	@Override
	public String toString() {
		return "hostname=\"" + getHostname() + "\" configuration=\"" + getConfiguration().getName() + "\" screen_name=\"" + getScreenname() + "\"";
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Account))
			return false;
		Account account = (Account) obj;
		return account.getId() == getId() && account.getHostname().equals(getHostname());
	}
}