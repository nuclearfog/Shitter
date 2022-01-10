package org.nuclearfog.twidda.database;

import android.database.Cursor;

import org.nuclearfog.twidda.model.DirectMessage;
import org.nuclearfog.twidda.model.User;


class DirectMessageDB implements DirectMessage {

    private long id;
    private long time;
    private String text;
    private User sender;
    private User receiver;


    DirectMessageDB(Cursor cursor, long currentId) {
        text = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.MessageTable.MESSAGE));
        time = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.MessageTable.SINCE));
        id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.MessageTable.ID));
        sender = new UserDB(cursor, DatabaseAdapter.UserTable.ALIAS_1 + ".", currentId);
        receiver = new UserDB(cursor, DatabaseAdapter.UserTable.ALIAS_2 + ".", currentId);
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

    @Override
    public String getMedia() {
        return "";
    }
}