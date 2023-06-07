package org.nuclearfog.twidda.backend.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.model.Media;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * This class contains information about a media source and a inputstream used to download or upload a file.
 *
 * @author nuclearfog
 */
public class MediaStatus implements Serializable, Closeable {

	private static final long serialVersionUID = 6824278073662885637L;

	/**
	 * indicates that the media file is a photo
	 */
	public static final int IMAGE = 10;

	/**
	 * indicates that the media file is a video
	 */
	public static final int VIDEO = 11;

	/**
	 * indicates that the media file is an audio
	 */
	public static final int AUDIO = 12;

	/**
	 * indicates that the media file is an animated image
	 */
	public static final int GIF = 13;

	@Nullable
	private transient InputStream inputStream = null;

	private String mimeType;
	private String path;
	private String description;
	private String key;
	private int type;
	private boolean local;

	/**
	 * create MediaStatus from an online source
	 *
	 * @param inputStream inputstream to fetch data from internet
	 * @param mimeType    MIME type of the media
	 * @throws IllegalArgumentException when the file is invalid
	 */
	public MediaStatus(@Nullable InputStream inputStream, String mimeType, String key) throws IllegalArgumentException {
		this.inputStream = inputStream;
		this.mimeType = mimeType;
		this.key = key;
		type = getType(mimeType);
		description = "";
		local = false;
	}

	/**
	 * create MediaStatus from an offline source
	 *
	 * @param uri         path to the local file
	 * @param description description of the media source
	 * @throws IllegalArgumentException when the file is invalid
	 */
	public MediaStatus(Context context, Uri uri, String description) throws IllegalArgumentException {
		DocumentFile file = DocumentFile.fromSingleUri(context, uri);
		if (file != null && file.length() > 0) {
			this.description = description;
			mimeType = context.getContentResolver().getType(uri);
			type = getType(mimeType);
			path = uri.toString();
			local = true;
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * create MediaStatus from and online/offline source
	 *
	 * @param media Media instance containing information
	 */
	public MediaStatus(Media media) {
		path = media.getUrl();
		type = getType(media.getMediaType());
		key = media.getKey();
		local = !path.startsWith("http");
	}

	/**
	 * close stream
	 */
	@Override
	public void close() {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException e) {
			// ignore
		}
	}

	/**
	 * create a stream to upload media file
	 *
	 * @param resolver content resolver used to create stream and determine MIME type of the file
	 * @return true if stream is prepared, false if an error occured
	 */
	public boolean openStream(ContentResolver resolver) {
		if (path != null) {
			Uri uri = Uri.parse(path);
			try {
				inputStream = resolver.openInputStream(uri);
				mimeType = resolver.getType(uri);
				// check if stream is valid
				return inputStream != null && mimeType != null && inputStream.available() > 0;
			} catch (IOException exception) {
				if (BuildConfig.DEBUG) {
					exception.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * @return input stream of the media file
	 */
	@Nullable
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
	 * @return media description if any
	 */
	public String getDescription() {
		return description;
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
	 * get type of the media file
	 *
	 * @return media type {@link #VIDEO,#AUDIO,#IMAGE,#GIF}
	 */
	public int getMediaType() {
		return type;
	}

	/**
	 * get online media key
	 *
	 * @return media key or null if offline
	 */
	@Nullable
	public String getKey() {
		return key;
	}

	/**
	 * get online/local path of the file
	 *
	 * @return path or url
	 */
	public String getPath() {
		return path;
	}


	@NonNull
	@Override
	public String toString() {
		return "mime=\"" + mimeType + "\" size=" + available() + " path=\"" + path + "\"";
	}


	private int getType(String mimeType) throws IllegalArgumentException {
		if (mimeType.equals("image/gif"))
			return GIF;
		if (mimeType.startsWith("image/"))
			return IMAGE;
		if (mimeType.startsWith("video/"))
			return VIDEO;
		if (mimeType.startsWith("audio/"))
			return AUDIO;
		throw new IllegalArgumentException("wrong format!");
	}


	private int getType(int mediaType) {
		switch (mediaType) {
			case Media.AUDIO:
				return AUDIO;

			case Media.VIDEO:
				return VIDEO;

			case Media.GIF:
				return GIF;

			case Media.PHOTO:
				return IMAGE;

			default:
				return 0;
		}
	}
}