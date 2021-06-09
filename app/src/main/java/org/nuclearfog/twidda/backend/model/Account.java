package org.nuclearfog.twidda.backend.model;

import androidx.annotation.Nullable;

/**
 * container class for user login information
 *
 * @author nuclearfog
 */
public class Account {

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


    public Account(long userId, long loginDate, String key1, String key2) {
        this.userId = userId;
        this.loginDate = loginDate;
        this.key1 = key1;
        this.key2 = key2;
    }

    /**
     * get ID of the user
     *
     * @return user ID
     */
    public long getId() {
        return userId;
    }

    /**
     * get date of creation
     *
     * @return date as long
     */
    public long getLoginDate() {
        return loginDate;
    }

    /**
     * get attached user information
     *
     * @return user
     */
    @Nullable
    public User getUser() {
        return user;
    }

    /**
     * attach user information
     *
     * @param user user
     */
    public void attachUser(User user) {
        this.user = user;
    }

    /**
     * get access tokens
     *
     * @return array with two access tokens
     */
    public String[] getKeys() {
        return new String[]{key1, key2};
    }
}