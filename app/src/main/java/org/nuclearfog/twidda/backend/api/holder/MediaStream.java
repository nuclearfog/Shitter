package org.nuclearfog.twidda.backend.api.holder;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used to upload and download media files
 *
 * @author nuclearfog
 */
public class MediaStream  {

    private InputStream inputStream;
    private String mimeType;

    /**
     * @param inputStream stream of the media (local or online)
     * @param mimeType MIME type e.g. image/jpeg
     */
    public MediaStream(InputStream inputStream, String mimeType) {
        this.inputStream = inputStream;
        this.mimeType = mimeType;
    }

    /**
     * @return input stream of the media file
     */
    public InputStream getStream() {
        return inputStream;
    }

    /**
     * @return MIME type of the stream
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return remaining bytes of the stream
     */
    public int available() throws IOException {
        return inputStream.available();
    }

    /**
     * close stream
     */
    public void close() {
        try {
            inputStream.close();
        } catch (IOException e) {
            // ignore
        }
    }
}