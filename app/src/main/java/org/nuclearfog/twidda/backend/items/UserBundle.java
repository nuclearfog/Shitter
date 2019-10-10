package org.nuclearfog.twidda.backend.items;

import androidx.annotation.NonNull;

/**
 * Container for User information
 */
public class UserBundle {

    private final TwitterUser user;
    private final UserProperties properties;

    public UserBundle(@NonNull TwitterUser user, @NonNull UserProperties properties) {
        this.properties = properties;
        this.user = user;
    }

    public TwitterUser getUser() {
        return user;
    }

    public UserProperties getProperties() {
        return properties;
    }
}