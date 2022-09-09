package org.nuclearfog.twidda.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Interface class used for all tweet implementations
 *
 * @author nuclearfog
 */
public interface Tweet extends Serializable {

	/**
	 * returned when the tweet doesn't contain any media
	 */
	int MEDIA_NONE = -1;

	/**
	 * returned when the tweet contains one or more images
	 */
	int MEDIA_PHOTO = 800;

	/**
	 * returned when the tweet contains a video
	 */
	int MEDIA_VIDEO = 801;

	/**
	 * returned when the tweet contains an animated gif
	 */
	int MEDIA_GIF = 802;

	/**
	 * @return tweet ID
	 */
	long getId();

	/**
	 * @return tweet text
	 */
	String getText();

	/**
	 * @return tweet author
	 */
	User getAuthor();

	/**
	 * @return time when the tweet was published
	 */
	long getTimestamp();

	/**
	 * @return API name from where the weet was published
	 */
	String getSource();

	/**
	 * @return embedded (quoted) tweet or null
	 */
	@Nullable
	Tweet getEmbeddedTweet();

	/**
	 * @return name of the replied tweet's author
	 */
	String getReplyName();

	/**
	 * @return ID of the replied tweet's author
	 */
	long getRepliedUserId();

	/**
	 * @return ID of the replied tweet
	 */
	long getRepliedTweetId();

	/**
	 * @return ID of the tweet retweeted by the current user
	 */
	long getRetweetId();

	/**
	 * @return number of retweets
	 */
	int getRetweetCount();

	/**
	 * @return number of the favorits
	 */
	int getFavoriteCount();

	/**
	 * @return media links (up to 4) to images and videos
	 */
	@NonNull
	Uri[] getMediaUris();

	/**
	 * @return mentioned user names in the tweet text
	 */
	String getUserMentions();

	/**
	 * @return MIME type of media attached to the tweet
	 */
	int getMediaType();

	/**
	 * @return true if tweet contains sensitive media
	 */
	boolean isSensitive();

	/**
	 * @return true if tweet is retweeted by the current user
	 */
	boolean isRetweeted();

	/**
	 * @return true if tweet is favorited by the current user
	 */
	boolean isFavorited();

	/**
	 * @return true if tweet is hidden by current user
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