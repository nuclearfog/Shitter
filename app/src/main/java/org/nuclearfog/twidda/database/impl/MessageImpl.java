package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.MessageTable;
import org.nuclearfog.twidda.model.Message;
import org.nuclearfog.twidda.model.User;

/**
 * database implementation of a directmessage
 *
 * @author nuclearfog
 */
public class MessageImpl implements Message {

	private long id;
	private long time;
	private long senderId;
	private long receiverId;
	private String text;
	private User sender;
	private String media;


	public MessageImpl(Cursor cursor) {
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
	public String getText() {
		return text;
	}


	@Override
	public long getTimestamp() {
		return time;
	}


	@Override
	public long getReceiverId() {
		return receiverId;
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
		if (!(obj instanceof Message))
			return false;
		return ((Message) obj).getId() == id;
	}


	@NonNull
	@Override
	public String toString() {
		return "from=" + sender + " message=\"" + text + "\"";
	}


	public void setSender(User sender) {
		this.sender = sender;
	}


	public long getSenderId() {
		return senderId;
	}
}