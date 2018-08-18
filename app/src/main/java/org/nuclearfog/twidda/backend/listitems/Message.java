package org.nuclearfog.twidda.backend.listitems;

public class Message {

    public final long messageId;
    public final TwitterUser sender, receiver;
    public final String message;
    public final long time;

    public Message(long messageId, TwitterUser sender, TwitterUser receiver, long time, String message) {
        this.messageId = messageId;
        this.sender = sender;
        this.receiver = receiver;
        this.time = time;
        this.message = message;
    }
}