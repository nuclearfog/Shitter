package org.nuclearfog.twidda.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;

/**
 * container class for user login information
 *
 * @author nuclearfog
 */
class AccountDB implements Account {

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


    AccountDB(long userId, long loginDate, String key1, String key2) {
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
    public String[] getKeys() {
        return new String[]{key1, key2};
    }

    @NonNull
    @Override
    public String toString() {
        if (user != null)
            return user + " date:" + loginDate;
        return "id:" + userId + " date:" + loginDate;
    }

    /**
     * attach user information
     *
     * @param user user associated with this account
     */
    void addUser(@Nullable User user) {
        this.user = user;
    }
}