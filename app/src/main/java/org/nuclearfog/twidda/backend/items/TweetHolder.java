package org.nuclearfog.twidda.backend.items;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.backend.helper.FilenameTools;
import org.nuclearfog.twidda.backend.helper.FilenameTools.FileType;

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
        if (BuildConfig.DEBUG && mediaLinks.length == 0)
            throw new AssertionError("media array is empty!");

        this.text = text;
        this.replyId = replyId;

        FileType type = FilenameTools.getFileType(mediaLinks[0]);

        switch (type) {
            case VIDEO:
                imageLink = new String[0];
                videoLink = mediaLinks[0];
                break;

            case ANGIF:
            case IMAGE:
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