package org.nuclearfog.twidda.backend.api.holder;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used to upload a directmessage
 *
 * @author nuclearfog
 */
public class DirectmessageUpdate {

    private String name;
    private String text;
    private MediaStream mediaStream;


    public DirectmessageUpdate(String name, String text) {
        this.name = name;
        this.text = text;
    }

    /**
     * add media uri and create input stream
     *
     * @param context context used to create inputstream and mime type
     * @param uri     uri of a local media file
     */
    public void addMedia(Context context, @NonNull Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        try {
            String mimeType = resolver.getType(uri);
            InputStream fileStream = resolver.openInputStream(uri);
            mediaStream = new MediaStream(fileStream, mimeType);
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
    @Nullable
    public MediaStream getMediaStream() {
        return mediaStream;
    }

    /**
     * close inputstream of media file
     */
    public void closeMediaStream() {
        if (mediaStream != null) {
            mediaStream.close();
        }
    }
}