package org.nuclearfog.twidda.backend.api.twitter.impl;

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
	 * twitter video/gif MIME
	 */
	private static final String MIME_V_MP4 = "video/mp4";

	private String preview;
	private String url = "";
	private int type = NONE;


	public MediaV2(JSONObject json) throws JSONException {
		String typeStr = json.getString("type");
		preview = json.optString("preview_image_url", "");
		switch (typeStr) {
			case "photo":
				url = json.getString("url");
				type = PHOTO;
				break;

			case "video":
				int maxBitrate = -1;
				JSONObject video = json.getJSONObject("video_info");
				JSONArray videoVariants = video.getJSONArray("variants");
				for (int i = 0; i < videoVariants.length(); i++) {
					JSONObject variant = videoVariants.getJSONObject(i);
					int bitRate = variant.optInt("bitrate", 0);
					if (bitRate > maxBitrate && MIME_V_MP4.equals(variant.getString("content_type"))) {
						url = variant.getString("url");
						maxBitrate = bitRate;
						type = VIDEO;
					}
				}
				break;

			case "animated_gif":
				JSONObject gif = json.getJSONObject("video_info");
				JSONArray gifVariants = gif.getJSONArray("variants");
				for (int i = 0; i < gifVariants.length() ; i++) {
					JSONObject gifVariant = gifVariants.getJSONObject(i);
					if (MIME_V_MP4.equals(gifVariant.getString("content_type"))) {
						url = gifVariant.getString("url");
						type = GIF;
						break;
					}
				}
				break;
		}
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
		switch(type) {
			case PHOTO:
				tostring = "photo ";
				break;

			case VIDEO:
				tostring = "video ";
				break;

			case GIF:
				tostring = "gif ";
				break;

			default:
				tostring = "none ";
				break;
		}
		tostring += "url=\"" + url + "\"";
		return tostring;
	}
}