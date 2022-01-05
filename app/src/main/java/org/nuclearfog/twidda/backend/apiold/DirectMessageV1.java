package org.nuclearfog.twidda.backend.apiold;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.DirectMessage;
import org.nuclearfog.twidda.model.User;

import twitter4j.URLEntity;

/**
 * Container class for a Twitter direct message
 *
 * @author nuclearfog
 */
class DirectMessageV1 implements DirectMessage {

    private long messageId;
    private long time;
    private User sender;
    private User receiver;
    private String message = "";


    DirectMessageV1(twitter4j.DirectMessage dm, User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
        messageId = dm.getId();
        time = dm.getCreatedAt().getTime();
        setMessageText(dm);
    }

    @Override
    public long getId() {
        return messageId;
    }

    @Override
    public User getSender() {
        return sender;
    }

    @Override
    public User getReceiver() {
        return receiver;
    }

    @Override
    public String getText() {
        return message;
    }

    @Override
    public long getTime() {
        return time;
    }

    /**
     * unshorten t.co URLs
     *
     * @param message direct message
     */
    private void setMessageText(twitter4j.DirectMessage message) {
        String text = message.getText();
        if (text != null && !text.isEmpty()) {
            URLEntity[] entities = message.getURLEntities();
            StringBuilder messageBuilder = new StringBuilder(message.getText());
            for (int i = entities.length - 1; i >= 0; i--) {
                URLEntity entity = entities[i];
                messageBuilder.replace(entity.getStart(), entity.getEnd(), entity.getExpandedURL());
            }
            this.message = messageBuilder.toString();
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "from:" + sender.getScreenname() + " to:" + receiver.getScreenname() + " msg:" + message;
    }
}