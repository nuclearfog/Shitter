package org.nuclearfog.twidda.backend.api.twitter.impl.v1;

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
public class MediaV1 implements Media {

	private static final long serialVersionUID = -5221331970410058908L;

	/**
	 * twitter video/gif MIME
	 */
	private static final String MIME_V_MP4 = "video/mp4";

	private int type = NONE;
	private String url = "";
	private String preview;

	/**
	 * @param json JSON containing media information (extended_entities)
	 */
	public MediaV1(JSONObject json) throws JSONException {
		String type = json.getString("type");
		preview = json.optString("media_url_https");
		switch (type) {
			case "photo":
				url = json.getString("media_url_https");
				this.type = PHOTO;
				break;

			case "video":
				int maxBitrate = -1;
				JSONArray videoVariants = json.getJSONObject("video_info").getJSONArray("variants");
				for (int i = 0; i < videoVariants.length(); i++) {
					JSONObject variant = videoVariants.getJSONObject(i);
					int bitRate = variant.optInt("bitrate", 0);
					if (bitRate > maxBitrate && MIME_V_MP4.equals(variant.getString("content_type"))) {
						url = variant.getString("url");
						maxBitrate = bitRate;
						this.type = VIDEO;
					}
				}
				break;

			case "animated_gif":
				JSONArray gifVariants = json.getJSONArray("variants");
				for (int i = 0; i < gifVariants.length(); i++) {
					JSONObject gifVariant = gifVariants.getJSONObject(i);
					if (MIME_V_MP4.equals(gifVariant.getString("content_type"))) {
						url = gifVariant.getString("url");
						this.type = GIF;
						break;
					}
				}
				break;
		}
	}


	@Override
	public String getKey() {
		return url;
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