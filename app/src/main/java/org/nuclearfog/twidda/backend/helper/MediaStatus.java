package org.nuclearfog.twidda.backend.helper;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * This class contains information about a media source and a inputstream used to download or upload a file.
 *
 * @author nuclearfog
 */
public class MediaStatus implements Serializable {

	private static final long serialVersionUID = 6824278073662885637L;

	private transient InputStream inputStream = null;

	private String mimeType;
	private String path;
	private boolean local;

	/**
	 * create MediaStatus from an online source
	 *
	 * @param inputStream inputstream to fetch data from internet
	 * @param mimeType    MIME type of the media
	 */
	public MediaStatus(InputStream inputStream, String mimeType) {
		this.inputStream = inputStream;
		this.mimeType = mimeType;
		local = false;
	}

	/**
	 * create MediaStatus from an offline source
	 *
	 * @param path     path to the local file
	 * @param mimeType MIME type of the file
	 */
	public MediaStatus(String path, String mimeType) {
		this.path = path;
		this.mimeType = mimeType;
		local = true;
	}

	/**
	 * create a stream to upload media file
	 *
	 * @param resolver content resolver used to create stream and determine MIME type of the file
	 * @return true if stream is prepared, false if an error occured
	 */
	public boolean openStream(ContentResolver resolver) {
		if (path != null && inputStream != null) {
			Uri uri = Uri.parse(path);
			try {
				inputStream = resolver.openInputStream(uri);
				mimeType = resolver.getType(uri);
				// check if stream is valid
				return inputStream != null && mimeType != null && inputStream.available() > 0;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
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
		if (inputStream == null)
			return 0L;
		try {
			return inputStream.available();
		} catch (IOException e) {
			return 0L;
		}
	}

	/**
	 * @return true if source stream is local
	 */
	public boolean isLocal() {
		return local;
	}

	/**
	 * close stream
	 */
	public void close() {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException e) {
			// ignore
		}
	}


	@NonNull
	@Override
	public String toString() {
		return "mime=\"" + mimeType + "\" size=" + available() + " path=\"" + path + "\"";
	}
}