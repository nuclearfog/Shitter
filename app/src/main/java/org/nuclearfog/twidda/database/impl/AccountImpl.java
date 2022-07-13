package org.nuclearfog.twidda.database.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;

/**
 * database implementation of account
 *
 * @author nuclearfog
 */
public class AccountImpl implements Account {

	/**
	 * id of the user
	 */
	private final long userId;

	/**
	 * date of the first login
	 */
	private final long loginDate;

	/**
	 * access tokens of the login
	 */
	private final String key1, key2;

	private User user;


	public AccountImpl(long userId, long loginDate, String key1, String key2) {
		this.userId = userId;
		this.loginDate = loginDate;
		this.key1 = key1;
		this.key2 = key2;
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
	public String getAccessToken() {
		return key1;
	}

	@Override
	public String getTokenSecret() {
		return key2;
	}


	@NonNull
	@Override
	public String toString() {
		if (user != null)
			return user + " date=" + loginDate;
		return "id=" + userId + " date=" + loginDate;
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