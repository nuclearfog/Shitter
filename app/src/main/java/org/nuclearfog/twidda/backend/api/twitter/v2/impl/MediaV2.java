package org.nuclearfog.twidda.backend.api.twitter.v2.impl;

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
public class MediaV2 implements Media {

	private static final long serialVersionUID = 7109927957743710583L;

	/**
	 * fields to add extra media information
	 */
	public static final String FIELDS_MEDIA = "media.fields=media_key%2Cpreview_image_url%2Ctype%2Curl%2Cvariants";

	/**
	 * MIME type for video/gifv format
	 */
	private static final String MIME_V_MP4 = "video/mp4";

	/**
	 * Twitter media type for image
	 */
	private static final String TYPE_IMAGE = "photo";

	/**
	 * Twitter media type for video
	 */
	private static final String TYPE_VIDEO = "video";

	/**
	 * Twitter media type for animated image
	 */
	private static final String TYPE_GIF = "animated_gif";

	private String key;
	private String preview;
	private String url = "";
	private int type = UNDEFINED;

	/**
	 * @param mediaItem Twitter media json format
	 */
	public MediaV2(JSONObject mediaItem) throws JSONException {
		String typeStr = mediaItem.getString("type");
		preview = mediaItem.optString("preview_image_url", "");
		key = mediaItem.getString("media_key");

		switch (typeStr) {
			case TYPE_IMAGE:
				String url = mediaItem.optString("url");
				if (Patterns.WEB_URL.matcher(url).matches()) {
					this.url = url;
				} else {
					throw new JSONException("invalid url: \"" + url + "\"");
				}
				if (preview.isEmpty())// fixme: currently Twitter doesn't support preview for images.
					preview = url;
				type = PHOTO;
				break;

			case TYPE_VIDEO:
				int maxBitrate = -1;
				JSONArray variants = mediaItem.getJSONArray("variants");
				for (int i = 0; i < variants.length(); i++) {
					JSONObject variant = variants.getJSONObject(i);
					int bitRate = variant.optInt("bitrate", 0);
					if (bitRate > maxBitrate && MIME_V_MP4.equals(variant.getString("content_type"))) {
						url = variant.getString("url");
						if (Patterns.WEB_URL.matcher(url).matches()) {
							this.url = url;
						} else {
							throw new JSONException("invalid url: \"" + url + "\"");
						}
						maxBitrate = bitRate;
						type = VIDEO;
					}
				}
				break;

			case TYPE_GIF:
				variants = mediaItem.getJSONArray("variants");
				for (int i = 0; i < variants.length(); i++) {
					JSONObject gifVariant = variants.getJSONObject(i);
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
		return preview;
	}


	@Override
	public String getDescription() {
		return "";
	}


	@Override
	public String getBlurHash() {
		return "";
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