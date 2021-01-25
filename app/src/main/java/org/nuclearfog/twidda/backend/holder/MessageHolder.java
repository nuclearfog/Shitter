package org.nuclearfog.twidda.backend.holder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Direct message holder class
 *
 * @author nuclearfog
 */
public class MessageHolder {

    private final String username;
    private final String message;
    private final String[] mediaPath;

    /**
     * Direct message constructor
     *
     * @param username  receiver name
     * @param message   message text
     * @param mediaPath local media path
     */
    public MessageHolder(String username, String message, @Nullable String mediaPath) {
        if (username.startsWith("@"))
            this.username = username;
        else
            this.username = '@' + username;
        this.message = message;
        this.mediaPath = new String[]{mediaPath};
    }

    /**
     * Get receiver screen name
     *
     * @return receiver name
     */
    public String getUsername() {
        return username;
    }

    /**
     * get message text
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * get local path of media
     *
     * @return media path
     */
    public String[] getMediaPath() {
        return mediaPath;
    }

    /**
     * check if media is attached
     *
     * @return if media is set
     */
    public boolean hasMedia() {
        return mediaPath[0] != null;
    }

    @NonNull
    @Override
    public String toString() {
        return "to=" + username + "media=" + hasMedia() + "\n" + message;
    }
}