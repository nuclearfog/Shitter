package org.nuclearfog.twidda.backend.api.mastodon.impl;

import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Media;

/**
 * implementation for a Mastodon media item
 *
 * @author nuclearfog
 */
public class MastodonMedia implements Media {

	private static final long serialVersionUID = 8402701358586444094L;

	/**
	 * Mastodon image type
	 */
	private static final String TYPE_IMAGE = "image";

	/**
	 * Mastodon animated image type
	 */
	private static final String TYPE_GIF = "gifv";

	/**
	 * Mastodon video type
	 */
	private static final String TYPE_VIDEO = "video";

	private String key;
	private String url;
	private String preview = "";
	private int type = NONE;

	/**
	 * @param json Mastodon status JSON format
	 */
	public MastodonMedia(JSONObject json) throws JSONException {
		String typeStr = json.getString("type");
		String url = json.getString("url");
		String preview = json.optString("preview_url", "");
		key = json.getString("id");

		switch (typeStr) {
			case TYPE_IMAGE:
				type = PHOTO;
				break;

			case TYPE_GIF:
				type = GIF;
				break;

			case TYPE_VIDEO:
				type = VIDEO;
				break;
		}
		if (Patterns.WEB_URL.matcher(url).matches()) {
			this.url = url;
		} else {
			throw new JSONException("invalid url: \"" + url + "\"");
		}
		if (Patterns.WEB_URL.matcher(preview).matches()) {
			this.preview = preview;
		}
	}


	@Override
	public String getKey() {
		return key;
	}


	@Override
	public int getMediaType() {
		return type;
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
	public boolean equals(@Nullable Object obj) {
		return obj instanceof Media && ((Media) obj).getKey().equals(getKey());
	}


	@Override
	public int compareTo(Media o) {
		return String.CASE_INSENSITIVE_ORDER.compare(getKey(), o.getKey());
	}


	@NonNull
	@Override
	public String toString() {
		String tostring;
		switch (getMediaType()) {
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
		return tostring + "url=\"" + getUrl() + "\"";
	}
}