package org.nuclearfog.twidda.backend.items;

import org.nuclearfog.twidda.backend.helper.FilenameTools;
import org.nuclearfog.twidda.backend.helper.FilenameTools.FileType;

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
        FileType type = FilenameTools.getFileType(mediaLinks[0]);

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


    public void addLocation(double[] location) {
        this.latitude = location[0];
        this.longitude = location[1];
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
}