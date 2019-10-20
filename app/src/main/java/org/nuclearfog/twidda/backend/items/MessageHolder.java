package org.nuclearfog.twidda.backend.items;

public class MessageHolder {

    private final String username;
    private final String message;
    private final String mediaPath;


    public MessageHolder(String username, String message, String mediaPath) {
        if (username.startsWith("@"))
            this.username = username;
        else
            this.username = '@' + username;
        this.message = message;
        this.mediaPath = mediaPath;
    }


    public String getUsername() {
        return username;
    }


    public String getMessage() {
        return message;
    }


    public String getMediaPath() {
        return mediaPath;
    }


    public boolean hasMedia() {
        return mediaPath != null && !mediaPath.isEmpty();
    }
}