package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.AccountTable;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;

/**
 * database implementation of account
 *
 * @author nuclearfog
 */
public class AccountImpl implements Account {

	/**
	 * projection of the columns with fixed order
	 */
	public static final String[] PROJECTION = {
			AccountTable.ID,
			AccountTable.DATE,
			AccountTable.ACCESS_TOKEN,
			AccountTable.TOKEN_SECRET,
			AccountTable.CLIENT_ID,
			AccountTable.CLIENT_SECRET,
			AccountTable.HOST
	};

	private long userId;
	private long loginDate;
	private String accessToken, tokenSecret;
	private String apiKey, apiSecret;
	private String host;

	private User user;

	/**
	 * @param cursor database cursor containing this {@link #PROJECTION}
	 */
	public AccountImpl(Cursor cursor) {
		userId = cursor.getLong(0);
		loginDate = cursor.getLong(1);
		accessToken = cursor.getString(2);
		tokenSecret = cursor.getString(3);
		apiKey = cursor.getString(4);
		apiSecret = cursor.getString(5);
		host = cursor.getString(6);
	}


	@Override
	public long getId() {
		return userId;
	}


	@Override
	public long getLoginDate() {
		return loginDate;
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
		return apiSecret;
	}


	@Override
	public String getAccessToken() {
		return accessToken;
	}


	@Override
	public String getTokenSecret() {
		return tokenSecret;
	}


	@Override
	public String getHostname() {
		return host;
	}


	@NonNull
	@Override
	public String toString() {
		return "date=" + loginDate + " host=" + host + " user=" + user;
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