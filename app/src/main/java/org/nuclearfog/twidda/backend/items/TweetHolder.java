package org.nuclearfog.twidda.backend.items;

import android.location.Location;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.helper.StringTools;
import org.nuclearfog.twidda.backend.helper.StringTools.FileType;

public class TweetHolder {

    private final String text;
    private final long replyId;
    private String[] imageLink;
    private String videoLink;
    private double longitude, latitude;
    private boolean hasImage = false;
    private boolean hasVideo = false;
    private boolean hasLocation = false;


    public TweetHolder(String text, long replyId) {
        this.text = text;
        this.replyId = replyId;
    }


    public void addMedia(String[] mediaLinks) {
        FileType type = StringTools.getFileType(mediaLinks[0]);

        switch (type) {
            case VIDEO:
                imageLink = new String[0];
                videoLink = mediaLinks[0];
                hasVideo = true;
                break;

            case ANGIF:
            case IMAGE:
                videoLink = "";
                imageLink = mediaLinks;
                hasImage = true;
                break;
        }
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

    public String getVideoLink() {
        return videoLink;
    }

    public String[] getImageLink() {
        return imageLink;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public boolean hasImages() {
        return hasImage;
    }

    public boolean hasVideo() {
        return hasVideo;
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
        return "to=" + replyId + ", location added=" + hasLocation + ", image added=" + hasImage + ", video added=" + hasVideo
                + "\n" + text;

    }
}