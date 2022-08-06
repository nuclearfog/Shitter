package org.nuclearfog.twidda.backend.api.update;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used to upload and download media files
 *
 * @author nuclearfog
 */
public class MediaUpdate {

	private InputStream inputStream;
	private String mimeType;

	/**
	 * @param inputStream stream of the media (local or online)
	 * @param mimeType    MIME type e.g. image/jpeg
	 */
	public MediaUpdate(InputStream inputStream, String mimeType) {
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
	public long available() {
		try {
			return inputStream.available();
		} catch (IOException e) {
			return 0;
		}
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

	@NonNull
	@Override
	public String toString() {
		return "mime:\"" + mimeType + "\" size:" + available();
	}
}