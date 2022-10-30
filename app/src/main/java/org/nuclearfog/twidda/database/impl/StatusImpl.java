package org.nuclearfog.twidda.database.impl;

import static org.nuclearfog.twidda.database.AppDatabase.FAV_MASK;
import static org.nuclearfog.twidda.database.AppDatabase.HIDDEN_MASK;
import static org.nuclearfog.twidda.database.AppDatabase.MEDIA_ANGIF_MASK;
import static org.nuclearfog.twidda.database.AppDatabase.MEDIA_IMAGE_MASK;
import static org.nuclearfog.twidda.database.AppDatabase.MEDIA_SENS_MASK;
import static org.nuclearfog.twidda.database.AppDatabase.MEDIA_VIDEO_MASK;
import static org.nuclearfog.twidda.database.AppDatabase.RTW_MASK;

import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;

import java.util.regex.Pattern;

/**
 * Implementation of a database STATUS
 *
 * @author nuclearfog
 */
public class StatusImpl implements Status {

	private static final long serialVersionUID = -5957556706939766801L;

	private static final Pattern SEPARATOR = Pattern.compile(";");

	private long id;
	private long time;
	private long embeddedId;
	private long replyID;
	private long replyUserId;
	private long myRepostId;
	@Nullable
	private Status embedded;
	private User author;
	private int repostCount;
	private int favoriteCount;
	private int mediaType;
	private String locationName;
	private String locationCoordinates;
	private String replyName;
	private String text;
	private String source;
	private String userMentions;
	private String[] mediaLinks = {};
	private boolean reposted;
	private boolean favorited;
	private boolean sensitive;
	private boolean isHidden;


	public StatusImpl(Cursor cursor, long currentUserId) {
		author = new UserImpl(cursor, currentUserId);
		time = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusTable.SINCE));
		text = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusTable.TEXT));
		repostCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusTable.REPOST));
		favoriteCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusTable.FAVORITE));
		id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusTable.ID));
		replyName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusTable.REPLYNAME));
		replyID = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusTable.REPLYSTATUS));
		source = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusTable.SOURCE));
		String linkStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusTable.MEDIA));
		locationName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusTable.PLACE));
		locationCoordinates = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusTable.COORDINATE));
		replyUserId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusTable.REPLYUSER));
		embeddedId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusTable.EMBEDDED));
		myRepostId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusRegisterTable.REPOST_ID));
		int register = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseAdapter.StatusRegisterTable.REGISTER));
		favorited = (register & FAV_MASK) != 0;
		reposted = (register & RTW_MASK) != 0;
		sensitive = (register & MEDIA_SENS_MASK) != 0;
		isHidden = (register & HIDDEN_MASK) != 0;
		if (!linkStr.isEmpty())
			mediaLinks = SEPARATOR.split(linkStr);
		userMentions = StringTools.getUserMentions(text, author.getScreenname());
		// get media type
		if ((register & MEDIA_ANGIF_MASK) == MEDIA_ANGIF_MASK) {
			mediaType = MEDIA_GIF;
		} else if ((register & MEDIA_IMAGE_MASK) == MEDIA_IMAGE_MASK) {
			mediaType = MEDIA_PHOTO;
		} else if ((register & MEDIA_VIDEO_MASK) == MEDIA_VIDEO_MASK) {
			mediaType = MEDIA_VIDEO;
		} else {
			mediaType = MEDIA_NONE;
		}
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

	@NonNull
	@Override
	public Uri[] getMediaUris() {
		Uri[] result = new Uri[mediaLinks.length];
		for (int i = 0; i < result.length; i++)
			result[i] = Uri.parse(mediaLinks[i]);
		return result;
	}

	@Override
	public String getUserMentions() {
		return userMentions;
	}

	@Override
	public int getMediaType() {
		return mediaType;
	}

	@Override
	public boolean isSensitive() {
		return sensitive;
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
	public String getLocationName() {
		return locationName;
	}

	@Override
	public String getLocationCoordinates() {
		return locationCoordinates;
	}

	@Override
	public boolean isHidden() {
		return isHidden;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Status))
			return false;
		return ((Status) obj).getId() == id;
	}

	@NonNull
	@Override
	public String toString() {
		return "from=\"" + author.getScreenname() + "\" text=\"" + text + "\"";
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
	 * attach status referenced by {@link #embeddedId}
	 *
	 * @param embedded embedded status
	 */
	public void setEmbeddedStatus(Status embedded) {
		this.embedded = embedded;
	}
}