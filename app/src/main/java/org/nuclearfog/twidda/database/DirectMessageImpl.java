package org.nuclearfog.twidda.database;

import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.MessageTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.UserTable;
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
    private String text;
    private User sender;
    private User receiver;
    private String media;


    DirectMessageImpl(Cursor cursor, long currentId) {
        text = cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.MESSAGE));
        time = cursor.getLong(cursor.getColumnIndexOrThrow(MessageTable.SINCE));
        id = cursor.getLong(cursor.getColumnIndexOrThrow(MessageTable.ID));
        media = cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.MEDIA));
        sender = new UserImpl(cursor, UserTable.ALIAS_1 + ".", currentId);
        receiver = new UserImpl(cursor, UserTable.ALIAS_2 + ".", currentId);
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
}