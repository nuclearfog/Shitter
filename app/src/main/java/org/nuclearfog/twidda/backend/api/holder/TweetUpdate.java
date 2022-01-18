package org.nuclearfog.twidda.backend.api.holder;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to upload tweet information
 *
 * @author nuclearfog
 */
public class TweetUpdate {

    private String text;
    private long replyId;
    private double longitude;
    private double latitude;
    private List<MediaStream> mediaStreams;
    private List<Uri> mediaUris;

    private boolean hasLocation = false;

    /**
     * create a tweet holder
     *
     * @param replyId ID of the tweet to reply or 0 if this tweet is not a reply
     */
    public TweetUpdate(long replyId) {
        this.replyId = replyId;
        mediaStreams = new ArrayList<>(5);
        mediaUris = new ArrayList<>(5);
    }

    /**
     * add tweet text
     */
    public void addText(String text) {
        this.text = text;
    }

    /**
     * Add media paths to the holder
     *
     * @param context  context to resolve Uri links
     * @param mediaUri array of media paths from storage
     * @return number of media added or -1 if an error occurs
     */
    public int addMedia(Context context, Uri mediaUri) {
        ContentResolver resolver = context.getContentResolver();
        try {
            InputStream is = resolver.openInputStream(mediaUri);
            String mime = resolver.getType(mediaUri);
            if (is != null && mime != null && is.available() > 0) {
                mediaStreams.add(new MediaStream(is, mime));
                mediaUris.add(mediaUri);
                return mediaStreams.size();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Add location to a tweet
     *
     * @param location location information
     */
    public void addLocation(@NonNull Location location) {
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
     * get information about media attached to the tweet
     *
     * @return list of mediastream instances
     */
    @NonNull
    public List<MediaStream> getMediaStreams() {
        return mediaStreams;
    }

    /**
     * get media links
     *
     * @return media uri array
     */
    public Uri[] getMediaUris() {
        return mediaUris.toArray(new Uri[0]);
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
     * check if location informaton is attached
     *
     * @return true if location is attached
     */
    public boolean hasLocation() {
        return hasLocation;
    }

    /**
     * check if media information is attached
     *
     * @return true if media is attached
     */
    public int mediaCount() {
        return mediaStreams.size();
    }

    @NonNull
    @Override
    public String toString() {
        return "to=" + replyId + "\nTweet=" + text;
    }
}