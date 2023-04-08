package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;

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
	public static final String[] PROJECTION = {KEY, URL, PREVIEW, TYPE};

	private int mediaType;
	private String key = "";
	private String url = "";
	private String preview = "";


	/**
	 * @param cursor database cursor containing media table
	 */
	public DatabaseMedia(Cursor cursor) {
		String key = cursor.getString(0);
		String url = cursor.getString(1);
		String preview = cursor.getString(2);
		mediaType = cursor.getInt(3);
		if (key != null)
			this.key = key;
		if (url != null)
			this.url = url;
		if (preview != null)
			this.preview = preview;
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
	public int compareTo(Media o) {
		return String.CASE_INSENSITIVE_ORDER.compare(key, o.getKey());
	}


	@NonNull
	@Override
	public String toString() {
		String tostring;
		switch (mediaType) {
			case PHOTO:
				tostring = "photo:";
				break;

			case VIDEO:
				tostring = "video:";
				break;

			case GIF:
				tostring = "gif:";
				break;

			default:
				tostring = "none:";
				break;
		}
		tostring += "url=\"" + url + "\"";
		return tostring;
	}
}