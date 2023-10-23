package org.nuclearfog.twidda.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Represents a revision of a {@link Status} that has been edited
 *
 * @author nuclearfog
 */
public interface EditedStatus extends Serializable, Comparable<EditedStatus> {

	/**
	 * @return timestamp of this revision
	 */
	long getTimestamp();

	/**
	 * @return status text
	 */
	String getText();

	/**
	 * @return author of this status
	 */
	User getAuthor();

	/**
	 * @return true if status contains sensitive content
	 */
	boolean isSensitive();

	/**
	 * @return true if status contains spoiler content
	 */
	boolean isSpoiler();

	/**
	 * @return status poll or null
	 */
	@Nullable
	Poll getPoll();

	/**
	 * @return array of media items
	 */
	Media[] getMedia();

	/**
	 * @return array of emojis used in the text
	 */
	Emoji[] getEmojis();

	/**
	 * @return location associated with this status
	 */
	@Nullable
	Location getLocation();

	/**
	 *
	 */
	@Override
	default int compareTo(EditedStatus status) {
		return Long.compare(status.getTimestamp(), getTimestamp());
	}
}