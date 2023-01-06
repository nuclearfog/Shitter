package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.MessageTable;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Message;
import org.nuclearfog.twidda.model.User;

import java.util.regex.Pattern;

/**
 * database implementation of a directmessage
 *
 * @author nuclearfog
 */
public class MessageImpl implements Message {

	private static final long serialVersionUID = 4089879784295312386L;

	private static final Pattern MEDIA_SEPARATOR = Pattern.compile(";");

	private long id;
	private long time;
	private long receiverId;
	private String text;
	private String[] mediaKeys = {};
	private Media[] medias = {};
	private User sender;

	/**
	 * @param cursor  database cursor containing UserTable column
	 * @param account current user information
	 */
	public MessageImpl(Cursor cursor, Account account) {
		sender = new UserImpl(cursor, account);
		text = cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.MESSAGE));
		time = cursor.getLong(cursor.getColumnIndexOrThrow(MessageTable.TIME));
		id = cursor.getLong(cursor.getColumnIndexOrThrow(MessageTable.ID));
		receiverId = cursor.getLong(cursor.getColumnIndexOrThrow(MessageTable.TO));
		String mediaKeys = cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.MEDIA));
		if (mediaKeys != null && !mediaKeys.isEmpty()) {
			this.mediaKeys = MEDIA_SEPARATOR.split(mediaKeys);
		}
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


	@NonNull
	@Override
	public Media[] getMedia() {
		return medias;
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

	/**
	 * @return media key array
	 */
	public String[] getMediaKeys() {
		return mediaKeys;
	}

	/**
	 * add media
	 */
	public void addMedia(@NonNull Media[] medias) {
		this.medias = medias;
	}
}