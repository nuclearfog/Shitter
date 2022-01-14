package org.nuclearfog.twidda.backend.api.holder;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * TweetHolder keeps information about a written tweet such as text, media files and location
 *
 * @author nuclearfog
 */
public class TweetHolder {

    private final String text;
    private final long replyId;
    private double longitude;
    private double latitude;
    private InputStream[] mediaStreams;

    private String[] mimeTypes = {};
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
     * @param context context to resolve Uri links
     * @param mediaUri array of media paths from storage
     */
    public void addMedia(Context context, List<Uri> mediaUri) {
        if (!mediaUri.isEmpty()) {
            List<InputStream> iss = new ArrayList<>();
            List<String> mimeTypes = new ArrayList<>();
            ContentResolver resolver = context.getContentResolver();
            try {
                for (Uri uri : mediaUri) {
                    iss.add(resolver.openInputStream(uri));
                    mimeTypes.add(resolver.getType(uri));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.mediaStreams = iss.toArray(new InputStream[0]);
            this.mimeTypes = mimeTypes.toArray(new String[0]);
        }
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
    @Nullable
    public String[] getMimeTypes() {
        return mimeTypes;
    }

    /**
     * get paths of local media files
     *
     * @return array of media paths
     */
    @Nullable
    public InputStream[] getMediaStreams() {
        return mediaStreams;
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

    @NonNull
    @Override
    public String toString() {
        return "to=" + replyId + "\nTweet=" + text;
    }
}