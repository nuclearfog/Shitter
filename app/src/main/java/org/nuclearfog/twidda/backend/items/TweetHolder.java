package org.nuclearfog.twidda.backend.items;

import android.location.Location;

import androidx.annotation.NonNull;


public class TweetHolder {

    public enum MediaType {
        IMAGE,
        VIDEO,
        NONE
    }

    private final String text;
    private final long replyId;
    private String[] mediaLinks;
    private double longitude, latitude;

    private MediaType mType = MediaType.NONE;
    private boolean hasLocation = false;


    public TweetHolder(String text, long replyId) {
        this.text = text;
        this.replyId = replyId;
    }


    public void addMedia(String[] mediaLinks, MediaType mType) {
        this.mediaLinks = mediaLinks;
        this.mType = mType;
    }

    public void addLocation(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        hasLocation = true;
    }

    public String getText() {
        return text;
    }

    public long getReplyId() {
        return replyId;
    }

    public MediaType getMediaType() {
        return mType;
    }

    public String[] getMediaLinks() {
        return mediaLinks;
    }

    public String getMediaLink() {
        return mediaLinks[0];
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public boolean hasLocation() {
        return hasLocation;
    }

    public boolean isReply() {
        return replyId > 0;
    }

    @NonNull
    @Override
    public String toString() {
        return "to=" + replyId + "\nTweet=" + text;
    }
}