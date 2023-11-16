package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.StatusPropertiesTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.StatusTable;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Card;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;

import java.util.regex.Pattern;

/**
 * Implementation of a database STATUS
 *
 * @author nuclearfog
 */
public class DatabaseStatus implements Status, StatusTable, StatusPropertiesTable {

	private static final long serialVersionUID = -5957556706939766801L;

	private static final Pattern KEY_SEPARATOR = Pattern.compile(";");

	private long id, time, embeddedId, replyID, replyUserId, myRepostId, locationId, pollId, editedAt;
	private int repostCount, favoriteCount, replyCount, visibility;
	private boolean reposted, favorited, bookmarked, sensitive, spoiler, isHidden;
	private Status embedded;
	private Poll poll;
	private User author;
	private Location location;
	private String[] mediaKeys = {};
	private String[] emojiKeys = {};
	private Media[] medias = {};
	private Emoji[] emojis = {};
	private Card[] cards = {};
	private String replyName = "";
	private String text = "";
	private String source = "";
	private String userMentions = "";
	private String statusUrl = "";
	private String language = "";

	/**
	 * @param cursor  database cursor
	 * @param account current user login information
	 */
	public DatabaseStatus(Cursor cursor, Account account) {
		author = new DatabaseUser(cursor, account);
		time = cursor.getLong(cursor.getColumnIndexOrThrow(TIME));
		repostCount = cursor.getInt(cursor.getColumnIndexOrThrow(REPOST));
		favoriteCount = cursor.getInt(cursor.getColumnIndexOrThrow(FAVORITE));
		replyCount = cursor.getInt(cursor.getColumnIndexOrThrow(REPLY));
		id = cursor.getLong(cursor.getColumnIndexOrThrow(ID));
		replyID = cursor.getLong(cursor.getColumnIndexOrThrow(REPLYSTATUS));
		locationId = cursor.getLong(cursor.getColumnIndexOrThrow(LOCATION));
		pollId = cursor.getLong(cursor.getColumnIndexOrThrow(POLL));
		replyUserId = cursor.getLong(cursor.getColumnIndexOrThrow(REPLYUSER));
		embeddedId = cursor.getLong(cursor.getColumnIndexOrThrow(EMBEDDED));
		myRepostId = cursor.getLong(cursor.getColumnIndexOrThrow(REPOST_ID));
		editedAt = cursor.getLong(cursor.getColumnIndexOrThrow(EDITED_AT));
		String statusUrl = cursor.getString(cursor.getColumnIndexOrThrow(URL));
		String language = cursor.getString(cursor.getColumnIndexOrThrow(LANGUAGE));
		String mediaKeys = cursor.getString(cursor.getColumnIndexOrThrow(MEDIA));
		String emojiKeys = cursor.getString(cursor.getColumnIndexOrThrow(EMOJI));
		String source = cursor.getString(cursor.getColumnIndexOrThrow(SOURCE));
		String text = cursor.getString(cursor.getColumnIndexOrThrow(TEXT));
		String replyName = cursor.getString(cursor.getColumnIndexOrThrow(REPLYNAME));
		String userMentions = cursor.getString(cursor.getColumnIndexOrThrow(MENTIONS));
		int register = cursor.getInt(cursor.getColumnIndexOrThrow(REGISTER));

		favorited = (register & MASK_STATUS_FAVORITED) != 0;
		reposted = (register & MASK_STATUS_REPOSTED) != 0;
		sensitive = (register & MASK_STATUS_SENSITIVE) != 0;
		isHidden = (register & MASK_STATUS_HIDDEN) != 0;
		bookmarked = (register & MASK_STATUS_BOOKMARKED) != 0;
		spoiler = (register & MASK_STATUS_SPOILER) != 0;

		if ((register & MASK_STATUS_VISIBILITY_DIRECT) != 0)
			visibility = VISIBLE_DIRECT;
		else if ((register & MASK_STATUS_VISIBILITY_PRIVATE) != 0)
			visibility = VISIBLE_PRIVATE;
		else if ((register & MASK_STATUS_VISIBILITY_UNLISTED) != 0)
			visibility = VISIBLE_UNLISTED;
		else
			visibility = VISIBLE_PUBLIC;
		if (mediaKeys != null && !mediaKeys.isEmpty())
			this.mediaKeys = KEY_SEPARATOR.split(mediaKeys);
		if (emojiKeys != null && !emojiKeys.isEmpty())
			this.emojiKeys = KEY_SEPARATOR.split(emojiKeys);
		if (statusUrl != null)
			this.statusUrl = statusUrl;
		if (language != null)
			this.language = language;
		if (replyName != null)
			this.replyName = replyName;
		if (source != null)
			this.source = source;
		if (text != null)
			this.text = text;
		if (userMentions != null)
			this.userMentions = userMentions;
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public String getText() {
		return text;
	}


	@Override
	public User getAuthor() {
		return author;
	}


	@Override
	public long getTimestamp() {
		return time;
	}


	@Override
	public String getSource() {
		return source;
	}


	@Nullable
	@Override
	public Status getEmbeddedStatus() {
		return embedded;
	}


	@Override
	public String getReplyName() {
		return replyName;
	}


	@Override
	public long getRepliedUserId() {
		return replyUserId;
	}


	@Override
	public long getRepliedStatusId() {
		return replyID;
	}


	@Override
	public long getRepostId() {
		return myRepostId;
	}


	@Override
	public int getRepostCount() {
		return repostCount;
	}


	@Override
	public int getFavoriteCount() {
		return favoriteCount;
	}


	@Override
	public int getReplyCount() {
		return replyCount;
	}


	@Override
	public int getVisibility() {
		return visibility;
	}


	@Override
	public long editedAt() {
		return editedAt;
	}


	@NonNull
	@Override
	public Media[] getMedia() {
		return medias;
	}


	@NonNull
	@Override
	public Emoji[] getEmojis() {
		return emojis;
	}


	@Override
	public String getUserMentions() {
		return userMentions;
	}


	@Override
	public String getLanguage() {
		return language;
	}


	@Override
	public boolean isSensitive() {
		return sensitive;
	}


	@Override
	public boolean isSpoiler() {
		return spoiler;
	}


	@Override
	public boolean isReposted() {
		return reposted;
	}


	@Override
	public boolean isFavorited() {
		return favorited;
	}


	@Override
	public boolean isBookmarked() {
		return bookmarked;
	}


	@Override
	@Nullable
	public Location getLocation() {
		return location;
	}


	@Override
	public boolean isHidden() {
		return isHidden;
	}


	@Override
	public String getUrl() {
		return statusUrl;
	}


	@NonNull
	@Override
	public Card[] getCards() {
		return cards;
	}


	@Nullable
	@Override
	public Poll getPoll() {
		return poll;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Status))
			return false;
		Status status = ((Status) obj);
		return status.getId() == id && status.getTimestamp() == getTimestamp() && status.getAuthor().equals(getAuthor());
	}


	@NonNull
	@Override
	public String toString() {
		return "from=\"" + getAuthor().getScreenname() + "\" text=\"" + getText() + "\"";
	}

	/**
	 * get ID of the embedded status
	 *
	 * @return ID of the
	 */
	public long getEmbeddedStatusId() {
		return embeddedId;
	}

	/**
	 * @return media keys
	 */
	public String[] getMediaKeys() {
		return mediaKeys;
	}

	/**
	 * @return emoji keys
	 */
	public String[] getEmojiKeys() {
		return emojiKeys;
	}

	/**
	 * @return location ID
	 */
	public long getLocationId() {
		return locationId;
	}

	/**
	 * @return ID of an attached poll or '0'
	 */
	public long getPollId() {
		return pollId;
	}

	/**
	 * attach status referenced by {@link #embeddedId}
	 *
	 * @param embedded embedded status
	 */
	public void setEmbeddedStatus(Status embedded) {
		this.embedded = embedded;
	}

	/**
	 * add status media
	 *
	 * @param medias media array
	 */
	public void addMedia(@NonNull Media[] medias) {
		this.medias = medias;
	}

	/**
	 * add status emojis
	 *
	 * @param emojis emoji array
	 */
	public void addEmojis(@NonNull Emoji[] emojis) {
		this.emojis = emojis;
	}

	/**
	 * add location information
	 *
	 * @param location location item
	 */
	public void addLocation(Location location) {
		this.location = location;
	}

	/**
	 * add poll
	 *
	 * @param poll poll item
	 */
	public void addPoll(@Nullable Poll poll) {
		this.poll = poll;
	}
}