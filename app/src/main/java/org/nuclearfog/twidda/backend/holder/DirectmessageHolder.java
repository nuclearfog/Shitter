package org.nuclearfog.twidda.backend.holder;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * this class holds information about a directmessage
 *
 * @author nuclearfog
 */
public class DirectmessageHolder {

    private String name;
    private String text;
    private String mimeType = "";
    private InputStream fileStream;


    public DirectmessageHolder(String name, String text) {
        this.name = name;
        this.text = text;
    }

    /**
     * add media uri and create input stream
     *
     * @param context context used to create inputstream and mime type
     * @param uri uri of a local media file
     */
    public void addMedia(Context context, @NonNull Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        try {
            fileStream = resolver.openInputStream(uri);
            mimeType = resolver.getType(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get name of the receiver
     *
     * @return screen name
     */
    public String getReceiver() {
        return name;
    }

    /**
     * get message text
     *
     * @return message text
     */
    public String getText() {
        return text;
    }

    /**
     * get inputstream of the media file
     *
     * @return input stream
     */
    public InputStream getMediaStream() {
        return fileStream;
    }

    /**
     * get MIME type of the media file
     *
     * @return mime type string
     */
    public String getMimeType() {
        return mimeType;
    }
}