package org.nuclearfog.twidda.backend.api.twitter.v1.impl;

import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Media;

/**
 * media implementation for Twitrtter API v1.1
 *
 * @author nuclearfog
 */
public class MediaV1 implements Media {

	private static final long serialVersionUID = -5221331970410058908L;

	/**
	 * twitter video/gif MIME
	 */
	private static final String MIME_V_MP4 = "video/mp4";

	/**
	 * Twitter media type for animated image
	 */
	private static final String TYPE_GIF = "animated_gif";

	/**
	 * Twitter media type for image
	 */
	private static final String TYPE_IMAGE = "photo";

	/**
	 * Twitter media type for video
	 */
	private static final String TYPE_VIDEO = "video";

	private int type = NONE;
	private String url = "";
	private String previewUrl = "";
	private String key;

	/**
	 * @param json JSON containing media information (extended_entities)
	 */
	public MediaV1(JSONObject json) throws JSONException {
		String type = json.getString("type");
		key = json.getString("id_str");
		switch (type) {
			case TYPE_IMAGE:
				String url = json.getString("media_url_https");
				if (Patterns.WEB_URL.matcher(url).matches()) {
					this.url = url;
					previewUrl = url;
				} else {
					throw new JSONException("invalid url: \"" + url + "\"");
				}
				this.type = PHOTO;
				break;

			case TYPE_VIDEO:
				int maxBitrate = -1;
				JSONArray videoVariants = json.getJSONObject("video_info").getJSONArray("variants");
				previewUrl = json.optString("media_url_https", "");
				for (int i = 0; i < videoVariants.length(); i++) {
					JSONObject variant = videoVariants.getJSONObject(i);
					int bitRate = variant.optInt("bitrate", 0);
					if (bitRate > maxBitrate && MIME_V_MP4.equals(variant.getString("content_type"))) {
						url = variant.getString("url");
						if (Patterns.WEB_URL.matcher(url).matches()) {
							this.url = url;
						} else {
							throw new JSONException("invalid url: \"" + url + "\"");
						}
						maxBitrate = bitRate;
						this.type = VIDEO;
					}
				}
				break;

			case TYPE_GIF:
				JSONArray gifVariants = json.getJSONObject("video_info").getJSONArray("variants");
				previewUrl = json.optString("media_url_https", "");
				for (int i = 0; i < gifVariants.length(); i++) {
					JSONObject gifVariant = gifVariants.getJSONObject(i);
					if (MIME_V_MP4.equals(gifVariant.getString("content_type"))) {
						url = gifVariant.getString("url");
						if (Patterns.WEB_URL.matcher(url).matches()) {
							this.url = url;
						} else {
							throw new JSONException("invalid url: \"" + url + "\"");
						}
						this.type = GIF;
						break;
					}
				}
				break;
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
		return previewUrl;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		return obj instanceof Media && ((Media) obj).getKey().equals(getKey());
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