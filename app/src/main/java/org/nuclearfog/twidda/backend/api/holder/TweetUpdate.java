package org.nuclearfog.twidda.backend.api.holder;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

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

    private List<Uri> mediaUris = new ArrayList<>(5);
    private MediaStream[] mediaStreams = {};
    private boolean hasLocation = false;

    /**
     * set ID of the replied tweet
     *
     * @param replyId Tweet ID
     */
    public void setReplyId(long replyId) {
        this.replyId = replyId;
    }

    /**
     * add tweet text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Add file uri and check if file is valid
     *
     * @param mediaUri uri to a local file
     * @return number of media attached to this holder or -1 if file is invalid
     */
    public int addMedia(Context context, Uri mediaUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            DocumentFile file = DocumentFile.fromSingleUri(context, mediaUri);
            if (file != null && file.length() > 0) {
                mediaUris.add(mediaUri);
                return mediaUris.size();
            }
        } else {
            mediaUris.add(mediaUri);
            return mediaUris.size();
        }
        return -1;
    }

    /**
     * Add location to a tweet
     *
     * @param location location information
     */
    public void setLocation(@NonNull Location location) {
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
    public MediaStream[] getMediaStreams() {
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
        return mediaUris.size();
    }

    /**
     * prepare media streams if media Uri is added
     *
     * @return true if success, false if an error occurs
     */
    public boolean prepare(ContentResolver resolver) {
        if (mediaUris.isEmpty())
            return true;
        try {
            // open input streams
            mediaStreams = new MediaStream[mediaUris.size()];
            for (int i = 0 ; i < mediaStreams.length ; i++) {
                InputStream is = resolver.openInputStream(mediaUris.get(i));
                String mime = resolver.getType(mediaUris.get(i));
                // check if stream is valid
                if (is != null && mime != null && is.available() > 0) {
                    mediaStreams[i] = new MediaStream(is, mime);
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * close all open streams
     */
    public void close() {
        for (MediaStream mediaStream : mediaStreams) {
            mediaStream.close();
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "to=" + replyId + "\nTweet=" + text;
    }
}