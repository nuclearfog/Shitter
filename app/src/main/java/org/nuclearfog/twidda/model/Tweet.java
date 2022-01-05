package org.nuclearfog.twidda.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Interface class for all tweet implementations
 *
 * @author nuclearfog
 */
public interface Tweet extends Serializable {

    /**
     * tweet contains one or more images
     */
    int IMAGE = 0xe4150646;

    /**
     * tweet contains a video
     */
    int VIDEO = 0x45c010d4;

    /**
     * tweet contains an animated image
     */
    int GIF = 0xe43c15a1;

    /**
     * tweet contains no media
     */
    int NONE = 0x2bb3fc2d;

    /**
     * @return tweet ID
     */
    long getId();

    /**
     * @return tweet text
     */
    String getTweet();

    /**
     * @return tweet author
     */
    User getUser();

    /**
     * @return time when the tweet was published
     */
    long getTime();

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
    String[] getMediaLinks();

    /**
     * @return mentioned user names in the tweet text
     */
    String getMentionedUsers();

    /**
     * @return type of media attached to the tweet {@link #IMAGE}, {@link #VIDEO}, {@link #GIF} or {@link #NONE}
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
     * @return name of the location if attached
     */
    String getLocationName();

    /**
     * @return GPS coordinates if attached
     */
    String getLocationCoordinates();
}