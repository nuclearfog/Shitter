package org.nuclearfog.twidda.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Interface class for status implementations
 *
 * @author nuclearfog
 */
public interface Status extends Serializable {

	/**
	 * returned when the status doesn't contain any media
	 */
	int MEDIA_NONE = -1;

	/**
	 * returned when the status contains one or more images
	 */
	int MEDIA_PHOTO = 800;

	/**
	 * returned when the status contains a video
	 */
	int MEDIA_VIDEO = 801;

	/**
	 * returned when the status contains an animated gif
	 */
	int MEDIA_GIF = 802;

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
	 * @return media links (up to 4) to images and videos
	 */
	@NonNull
	Uri[] getMediaUris();

	/**
	 * @return mentioned user names in the status text
	 */
	String getUserMentions();

	/**
	 * @return MIME type of media attached to the status
	 */
	int getMediaType();

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
	 * @return name of the location if attached
	 */
	String getLocationName();

	/**
	 * @return GPS coordinates if attached
	 */
	String getLocationCoordinates();
}