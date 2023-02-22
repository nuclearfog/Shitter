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
public class DatabaseMedia implements Media {

	private static final long serialVersionUID = 8895107738679315263L;

	/**
	 *
	 */
	public static final String[] PROJECTION = {
			MediaTable.KEY,
			MediaTable.URL,
			MediaTable.PREVIEW,
			MediaTable.TYPE
	};

	private String key;
	private String url, preview;
	private int mediaType;


	/**
	 * @param cursor database cursor containing media table
	 */
	public DatabaseMedia(Cursor cursor) {
		key = cursor.getString(0);
		url = cursor.getString(1);
		preview = cursor.getString(2);
		mediaType = cursor.getInt(3);
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