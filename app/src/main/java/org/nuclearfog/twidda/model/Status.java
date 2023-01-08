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
	 * @return conversation Id (ID of the first status of a conversation)
	 */
	long getConversationId();

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
	 * @return mentioned user names in the status text
	 */
	String getUserMentions();

	/**
	 * @return true if status contains sensitive media
	 */
	boolean isSensitive();

	/**
	 * @return true if status is reposted by the current user
	 */
	boolean isReposted();

	/**
	 * @return true if status is favorited by the current user
	 */
	boolean isFavorited();

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