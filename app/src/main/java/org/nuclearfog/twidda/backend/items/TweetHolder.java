package org.nuclearfog.twidda.backend.items;

import androidx.annotation.NonNull;

public class TweetHolder {

    private final String text;
    private final long replyId;
    private final String[] imageLink;
    private final String videoLink;


    public TweetHolder(String text, long replyId) {
        this.text = text;
        this.replyId = replyId;
        imageLink = new String[0];
        videoLink = "";
    }

    public TweetHolder(String text, long replyId, @NonNull String[] mediaLinks) {
        this.text = text;
        this.replyId = replyId;

        String ext = "";
        String path = mediaLinks[0];
        int pos = path.lastIndexOf(".") + 1;
        if (pos > 0 && pos < path.length()) {
            ext = path.substring(pos);
            ext = ext.toLowerCase();
        }

        switch (ext) {
            case "mp4":
            case "3gp":
                imageLink = new String[0];
                videoLink = mediaLinks[0];
                break;

            case "jpg":
            case "jpeg":
            case "gif":
            case "png":
                videoLink = "";
                imageLink = mediaLinks;
                break;

            default:
                videoLink = "";
                imageLink = new String[0];
                break;
        }
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

    public boolean hasImages() {
        return imageLink.length > 0;
    }

    public boolean hasVideo() {
        return !videoLink.isEmpty();
    }

    public boolean isReply() {
        return replyId > 0;
    }
}