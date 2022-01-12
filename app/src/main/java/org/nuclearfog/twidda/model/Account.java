package org.nuclearfog.twidda.model;

import androidx.annotation.Nullable;

/**
 * interface of account implementations
 * An account class collects information about a saved login.
 *
 * @author nuclearfog
 */
public interface Account {

    /**
     * @return ID of the account (user ID)
     */
    long getId();

    /**
     * @return date of the first login
     */
    long getLoginDate();

    /**
     * @return user information of the account
     */
    @Nullable
    User getUser();

    /**
     * @return first access token of the user
     */
    String getAccessToken();

    /**
     * @return second access token of the user
     */
    String getTokenSecret();
}