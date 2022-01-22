package org.nuclearfog.twidda.database;

import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.MessageTable;
import org.nuclearfog.twidda.model.DirectMessage;
import org.nuclearfog.twidda.model.User;

/**
 * database implementation of a directmessage
 *
 * @author nuclearfog
 */
class DirectMessageImpl implements DirectMessage {

    private long id;
    private long time;
    private long senderId;
    private long receiverId;
    private String text;
    private User sender;
    private User receiver;
    private String media;


    DirectMessageImpl(Cursor cursor) {
        text = cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.MESSAGE));
        time = cursor.getLong(cursor.getColumnIndexOrThrow(MessageTable.SINCE));
        id = cursor.getLong(cursor.getColumnIndexOrThrow(MessageTable.ID));
        senderId = cursor.getLong(cursor.getColumnIndexOrThrow(MessageTable.FROM));
        receiverId = cursor.getLong(cursor.getColumnIndexOrThrow(MessageTable.TO));
        media = cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.MEDIA));
    }

    @Override
    public long getId() {
        return id;
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
        return text;
    }

    @Override
    public long getTimestamp() {
        return time;
    }

    @Nullable
    @Override
    public Uri getMedia() {
        if (media != null)
            return Uri.parse(media);
        return null;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DirectMessage))
            return false;
        return ((DirectMessage) obj).getId() == id;
    }

    @NonNull
    @Override
    public String toString() {
        return "from:" + sender + " to:" + receiver + " message:" + text;
    }


    void setSender(User sender) {
        this.sender = sender;
    }


    void setReceiver(User receiver) {
        this.receiver = receiver;
    }


    long getSenderId() {
        return senderId;
    }


    long getReceiverId() {
        return receiverId;
    }
}