package org.nuclearfog.twidda.backend.items;

public class Message {

    private final long messageId;
    private final TwitterUser sender, receiver;
    private final String message;
    private final long time;

    public Message(long messageId, TwitterUser sender, TwitterUser receiver, long time, String message) {
        this.messageId = messageId;
        this.sender = sender;
        this.receiver = receiver;
        this.time = time;
        this.message = message;
    }

    /**
     * get message ID
     *
     * @return message ID
     */
    public long getId() {
        return messageId;
    }

    /**
     * get sender of DM
     *
     * @return user
     */
    public TwitterUser getSender() {
        return sender;
    }

    /**
     * get receiver of DM
     *
     * @return user
     */
    public TwitterUser getReceiver() {
        return receiver;
    }

    /**
     * get Message content
     *
     * @return message
     */
    public String getText() {
        return message;
    }

    /**
     * get time of DM
     *
     * @return raw time
     */
    public long getTime() {
        return time;
    }
}