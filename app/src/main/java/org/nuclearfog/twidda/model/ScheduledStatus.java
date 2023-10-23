package org.nuclearfog.twidda.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Represents a scheduled status to post in the future
 *
 * @author nuclearfog
 */
public interface ScheduledStatus extends Serializable, Comparable<ScheduledStatus> {

	/**
	 * @return ID of the scheduled status
	 */
	long getId();

	/**
	 * @return time to publish status
	 */
	long getPublishTime();

	/**
	 * @return status text
	 */
	String getText();

	/**
	 * @return language of the text if any
	 */
	String getLanguage();

	/**
	 * @return attached media
	 */
	Media[] getMedia();

	/**
	 * @return attached poll
	 */
	@Nullable
	Poll getPoll();

	/**
	 * @return visibility of the status {@link Status#VISIBLE_PUBLIC,Status#VISIBLE_DIRECT,Status#VISIBLE_PRIVATE,Status#VISIBLE_UNLISTED}
	 */
	int getVisibility();

	/**
	 * @return true if status contains sensitive information
	 */
	boolean isSensitive();

	/**
	 * @return true if status contains spoiler information
	 */
	boolean isSpoiler();

	/**
	 *
	 */
	default int compareTo(ScheduledStatus status) {
		return Long.compare(status.getId(), getId());
	}
}