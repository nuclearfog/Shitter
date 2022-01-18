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
     * returned when the tweet contains one or more images
     */
    String MEDIA_PHOTO = "photo";

    /**
     * returned when the tweet contains a video
     */
    String MEDIA_VIDEO = "video";

    /**
     * returned when the tweet contains an animated gif
     */
    String MEDIA_GIF = "animated_gif";

    /**
     * returned when the tweet doesn't contain any media
     */
    String MEDIA_NONE = "*/*";

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
    long getReplyUserId();

    /**
     * @return ID of the replied tweet
     */
    long getReplyId();

    /**
     * @return ID of the tweet retweeted by the current user
     */
    long getMyRetweetId();

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
    Uri[] getMediaLinks();

    /**
     * @return mentioned user names in the tweet text
     */
    String getUserMentions();

    /**
     * @return MIME type of media attached to the tweet
     */
    String getMediaType();

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
     * @return name of the location if attached
     */
    String getLocationName();

    /**
     * @return GPS coordinates if attached
     */
    String getLocationCoordinates();
}