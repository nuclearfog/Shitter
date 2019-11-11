package org.nuclearfog.twidda.backend.items;

import androidx.annotation.NonNull;

/**
 * Direct message holder class
 */
public class MessageHolder {

    private final String username;
    private final String message;
    private final String mediaPath;

    /**
     * Direct message constructor
     *
     * @param username  receiver name
     * @param message   message text
     * @param mediaPath local media path
     */
    public MessageHolder(String username, String message, String mediaPath) {
        if (username.startsWith("@"))
            this.username = username;
        else
            this.username = '@' + username;
        this.message = message;
        this.mediaPath = mediaPath;
    }

    /**
     * Get receiver screen name
     * @return receiver name
     */
    public String getUsername() {
        return username;
    }

    /**
     * get message text
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * get local path of media
     * @return media path
     */
    public String getMediaPath() {
        return mediaPath;
    }

    /**
     * check if media is attached
     * @return if media is set
     */
    public boolean hasMedia() {
        return mediaPath != null && !mediaPath.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        return "to=" + username + "media=" + hasMedia() + "\n" + message;
    }
}