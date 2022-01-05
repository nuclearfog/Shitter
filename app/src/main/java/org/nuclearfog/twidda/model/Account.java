package org.nuclearfog.twidda.model;

import androidx.annotation.Nullable;

/**
 * interface of account implementations
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
     * @return oauth keys
     */
    String[] getKeys();
}