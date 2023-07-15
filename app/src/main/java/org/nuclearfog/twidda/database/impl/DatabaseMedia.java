package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.MediaTable;
import org.nuclearfog.twidda.model.Media;

/**
 * Media database implementation
 *
 * @author nuclearfog
 */
public class DatabaseMedia implements Media, MediaTable {

	private static final long serialVersionUID = 8895107738679315263L;

	/**
	 *
	 */
	public static final String[] PROJECTION = {KEY, URL, PREVIEW, TYPE, DESCRIPTION, BLUR};

	private int mediaType;
	private String key = "";
	private String url = "";
	private String preview = "";
	private String description = "";
	private String blurHash = "";


	/**
	 * @param cursor database cursor containing media table
	 */
	public DatabaseMedia(Cursor cursor) {
		String key = cursor.getString(0);
		String url = cursor.getString(1);
		String preview = cursor.getString(2);
		String description = cursor.getString(3);
		String blurHash = cursor.getString(4);
		mediaType = cursor.getInt(3);
		if (key != null)
			this.key = key;
		if (url != null)
			this.url = url;
		if (preview != null)
			this.preview = preview;
		if (description != null)
			this.description = description;
		if (blurHash != null)
			this.blurHash = blurHash;
	}


	@Override
	public String getKey() {
		return key;
	}


	@Override
	public int getMediaType() {
		return mediaType;
	}


	@Override
	public String getUrl() {
		return url;
	}


	@Override
	public String getPreviewUrl() {
		return preview;
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public String getBlurHash() {
		return blurHash;
	}


	@Nullable
	@Override
	public Meta getMeta() {
		return null; // todo implement this
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Media))
			return false;
		Media media = (Media) obj;
		return media.getMediaType() == getMediaType() && media.getKey().equals(getKey()) && media.getPreviewUrl().equals(getPreviewUrl()) && media.getUrl().equals(getUrl());
	}


	@NonNull
	@Override
	public String toString() {
		String tostring = "type=";
		switch (getMediaType()) {
			case PHOTO:
				tostring += "photo";
				break;

			case VIDEO:
				tostring += "video";
				break;

			case GIF:
				tostring += "gif";
				break;

			default:
				tostring += "none";
				break;
		}
		return tostring + " url=\"" + getUrl() + "\"";
	}
}