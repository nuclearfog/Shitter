package org.nuclearfog.twidda.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Interface class for status implementations
 *
 * @author nuclearfog
 */
public interface Status extends Serializable, Comparable<Status> {

	/**
	 * Status is visible to everyone, shown in public timelines.
	 */
	int VISIBLE_PUBLIC = 1;

	/**
	 * Status is visible to followers only, and to any mentioned users.
	 */
	int VISIBLE_PRIVATE = 2;

	/**
	 * Status is visible only to mentioned users.
	 */
	int VISIBLE_DIRECT = 3;

	/**
	 * Status is visible to public, but not included in public timelines.
	 */
	int VISIBLE_UNLISTED = 4;

	/**
	 * @return status ID
	 */
	long getId();

	/**
	 * @return status text
	 */
	String getText();

	/**
	 * @return status author
	 */
	User getAuthor();

	/**
	 * @return time when the status was published
	 */
	long getTimestamp();

	/**
	 * @return API name from where the status was published
	 */
	String getSource();

	/**
	 * @return embedded status if any
	 */
	@Nullable
	Status getEmbeddedStatus();

	/**
	 * @return name of the replied status author
	 */
	String getReplyName();

	/**
	 * @return ID of the replied status author
	 */
	long getRepliedUserId();

	/**
	 * @return ID of the replied status
	 */
	long getRepliedStatusId();

	/**
	 * @return ID of the status reposted by the current user
	 */
	long getRepostId();

	/**
	 * @return number of reposts
	 */
	int getRepostCount();

	/**
	 * @return number of the favorits
	 */
	int getFavoriteCount();

	/**
	 * @return number of replies
	 */
	int getReplyCount();

	/**
	 * get status visibility
	 *
	 * @return {@link #VISIBLE_DIRECT,#VISIBLE_PRIVATE,#VISIBLE_PUBLIC}
	 */
	int getVisibility();

	/**
	 * @return mentioned user names in the status text
	 */
	String getUserMentions();

	/**
	 * get language of the status
	 *
	 * @return ISO 639 Part 1 two-letter language code or empty
	 */
	String getLanguage();

	/**
	 * @return true if status contains sensitive media
	 */
	boolean isSensitive();

	/**
	 * @return true if status contains any spoiler
	 */
	boolean isSpoiler();

	/**
	 * @return true if status is reposted by the current user
	 */
	boolean isReposted();

	/**
	 * @return true if status is favorited by the current user
	 */
	boolean isFavorited();

	/**
	 * @return true if status is bookmarked by the current user
	 */
	boolean isBookmarked();

	/**
	 * @return true if status is hidden by current user
	 */
	boolean isHidden();

	/**
	 * @return url of the status
	 */
	String getUrl();

	/**
	 * @return cards representing link previews
	 */
	@NonNull
	Card[] getCards();

	/**
	 * @return media links (up to 4) to images and videos
	 */
	@NonNull
	Media[] getMedia();

	/**
	 * @return array of custom emojis
	 */
	@NonNull
	Emoji[] getEmojis();

	/**
	 * @return name of the location if attached
	 */
	@Nullable
	Location getLocation();

	/**
	 * @return status poll or null if not exists
	 */
	@Nullable
	Poll getPoll();

	/**
	 * @return status metrics or null if status doesn't belong to the current user
	 */
	@Nullable
	Metrics getMetrics();
}