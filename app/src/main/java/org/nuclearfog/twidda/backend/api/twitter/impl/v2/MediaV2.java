package org.nuclearfog.twidda.backend.api.twitter.impl.v2;

import androidx.annotation.NonNull;

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

	private String key;
	private String preview;
	private String url = "";
	private int type = NONE;

	/**
	 * @param mediaItem Twitter media json format
	 */
	public MediaV2(JSONObject mediaItem) throws JSONException {
		String typeStr = mediaItem.getString("type");
		preview = mediaItem.optString("preview_image_url", "");
		key = mediaItem.getString("media_key");

		switch (typeStr) {
			case "photo":
				url = mediaItem.optString("url");
				preview = url; // fixme: currently Twitter doesn't support preview for images.
				type = PHOTO;
				break;

			case "video":
				int maxBitrate = -1;
				JSONArray variants = mediaItem.getJSONArray("variants");
				for (int i = 0; i < variants.length(); i++) {
					JSONObject variant = variants.getJSONObject(i);
					int bitRate = variant.optInt("bitrate", 0);
					if (bitRate > maxBitrate && MIME_V_MP4.equals(variant.getString("content_type"))) {
						url = variant.getString("url");
						maxBitrate = bitRate;
						type = VIDEO;
					}
				}
				break;

			case "animated_gif":
				url = mediaItem.optString("url");
				type = GIF;
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


	@NonNull
	@Override
	public String toString() {
		String tostring;
		switch (type) {
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