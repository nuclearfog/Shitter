package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.InstanceTable;
import org.nuclearfog.twidda.model.Instance;

import java.util.regex.Pattern;

/**
 * Database implementation of an instance
 *
 * @author nuclearfog
 */
public class DatabaseInstance implements Instance, InstanceTable {

	private static final long serialVersionUID = -6000172987158811137L;

	private static final Pattern KEY_SEPARATOR = Pattern.compile(";");

	/**
	 * bit mask for translation flag
	 */
	public static final int MASK_TRANSLATION = 1;

	/**
	 * SQL projection of the columns
	 */
	public static final String[] COLUMNS = {DOMAIN, TIMESTAMP, TITLE, VERSION, DESCRIPTION, FLAGS, HASHTAG_LIMIT, STATUS_MAX_CHAR,
			IMAGE_LIMIT, VIDEO_LIMIT, GIF_LIMIT, AUDIO_LIMIT, OPTIONS_LIMIT, OPTION_MAX_CHAR, MIME_TYPES, IMAGE_SIZE,
			VIDEO_SIZE, GIF_SIZE, AUDIO_SIZE, POLL_MIN_DURATION, POLL_MAX_DURATION};

	private String title;
	private String domain;
	private String version;
	private String description;
	private String[] mimeTypes;
	private long timestamp;
	private int hashtagLimit;
	private int statusMaxLength;
	private int imageLimit;
	private int videoLimit;
	private int gifLimit;
	private int audioLimit;
	private int imageSizeLimit;
	private int videoSizeLimit;
	private int audioSizeLimit;
	private int gifSizeLimit;
	private int pollOptionLimit;
	private int pollOptionMaxLength;
	private int pollMinDuration;
	private int pollMaxDuration;
	private boolean translationSupported;

	/**
	 * @param cursor table rows with this {@link #COLUMNS}
	 */
	public DatabaseInstance(Cursor cursor) {
		domain = cursor.getString(0);
		timestamp = cursor.getLong(1);
		title = cursor.getString(2);
		version = cursor.getString(3);
		description = cursor.getString(4);
		int flags = cursor.getInt(5);
		hashtagLimit = cursor.getInt(6);
		statusMaxLength = cursor.getInt(7);
		imageLimit = cursor.getInt(8);
		videoLimit = cursor.getInt(9);
		gifLimit = cursor.getInt(10);
		audioLimit = cursor.getInt(11);
		pollOptionLimit = cursor.getInt(12);
		pollOptionMaxLength = cursor.getInt(13);
		String mimeTypeStr = cursor.getString(14);
		imageSizeLimit = cursor.getInt(15);
		videoSizeLimit = cursor.getInt(16);
		gifSizeLimit = cursor.getInt(17);
		audioSizeLimit = cursor.getInt(18);
		pollMinDuration = cursor.getInt(19);
		pollMaxDuration = cursor.getInt(20);

		if (!mimeTypeStr.trim().isEmpty()) {
			mimeTypes = KEY_SEPARATOR.split(mimeTypeStr);
		} else {
			mimeTypes = new String[0];
		}
		translationSupported = (flags & MASK_TRANSLATION) != 0;
	}


	@Override
	public String getTitle() {
		return title;
	}


	@Override
	public String getDomain() {
		return domain;
	}


	@Override
	public String getVersion() {
		return version;
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public long getTimestamp() {
		return timestamp;
	}


	@Override
	public int getHashtagFollowLimit() {
		return hashtagLimit;
	}


	@Override
	public int getStatusCharacterLimit() {
		return statusMaxLength;
	}


	@Override
	public int getImageLimit() {
		return imageLimit;
	}


	@Override
	public int getVideoLimit() {
		return videoLimit;
	}


	@Override
	public int getGifLimit() {
		return gifLimit;
	}


	@Override
	public int getAudioLimit() {
		return audioLimit;
	}


	@Override
	public String[] getSupportedFormats() {
		return mimeTypes;
	}


	@Override
	public int getImageSizeLimit() {
		return imageSizeLimit;
	}


	@Override
	public int getGifSizeLimit() {
		return gifSizeLimit;
	}


	@Override
	public int getVideoSizeLimit() {
		return videoSizeLimit;
	}


	@Override
	public int getAudioSizeLimit() {
		return audioSizeLimit;
	}


	@Override
	public int getPollOptionsLimit() {
		return pollOptionLimit;
	}


	@Override
	public int getPollOptionCharacterLimit() {
		return pollOptionMaxLength;
	}


	@Override
	public int getMinPollDuration() {
		return pollMinDuration;
	}


	@Override
	public int getMaxPollDuration() {
		return pollMaxDuration;
	}


	@Override
	public boolean isTranslationSupported() {
		return translationSupported;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Instance))
			return false;
		Instance instance = (Instance) obj;
		return instance.getDomain().equals(getDomain()) && instance.getTimestamp() == getTimestamp();
	}


	@NonNull
	@Override
	public String toString() {
		return "domain=\"" + getDomain() + " \" version=\"" + getVersion() + "\"";
	}
}