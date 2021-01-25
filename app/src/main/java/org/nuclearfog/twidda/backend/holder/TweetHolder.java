package org.nuclearfog.twidda.backend.holder;

import android.location.Location;

import androidx.annotation.NonNull;

/**
 * TweetHolder keeps information about a written tweet such as text, media files and location
 *
 * @author nuclearfog
 */
public class TweetHolder {

    public enum MediaType {
        IMAGE,
        VIDEO,
        NONE
    }

    private final String text;
    private final long replyId;
    private String[] mediaPaths;
    private double longitude;
    private double latitude;

    private MediaType mType = MediaType.NONE;
    private boolean hasLocation = false;


    /**
     * create a tweet holder
     *
     * @param text    Tweet message
     * @param replyId ID of the tweet to reply or 0 if this tweet is not a reply
     */
    public TweetHolder(String text, long replyId) {
        this.text = text;
        this.replyId = replyId;
    }

    /**
     * Add media paths to the holder
     *
     * @param mediaLinks array of media paths from storage
     * @param mType      type of media
     */
    public void addMedia(String[] mediaLinks, MediaType mType) {
        this.mediaPaths = mediaLinks;
        this.mType = mType;
    }

    /**
     * Add location to a tweet
     *
     * @param location location information
     */
    public void addLocation(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        hasLocation = true;
    }

    /**
     * get tweet message
     *
     * @return tweet text
     */
    public String getText() {
        return text;
    }

    /**
     * get ID of the replied tweet
     *
     * @return Tweet ID
     */
    public long getReplyId() {
        return replyId;
    }

    /**
     * get type of attached media if any
     *
     * @return media type
     */
    public MediaType getMediaType() {
        return mType;
    }

    /**
     * get paths of local media files
     *
     * @return array of media paths
     */
    public String[] getMediaPaths() {
        return mediaPaths;
    }

    /**
     * get first media path
     *
     * @return path string
     */
    public String getMediaPath() {
        return mediaPaths[0];
    }

    /**
     * get longitude of the location
     *
     * @return longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * get latitude of the location
     *
     * @return latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * return if holder has location information attached
     *
     * @return true if location is attached
     */
    public boolean hasLocation() {
        return hasLocation;
    }

    /**
     * return if tweet is a reply
     *
     * @return true if tweet is a reply
     */
    public boolean isReply() {
        return replyId > 0;
    }

    @NonNull
    @Override
    public String toString() {
        return "to=" + replyId + "\nTweet=" + text;
    }
}