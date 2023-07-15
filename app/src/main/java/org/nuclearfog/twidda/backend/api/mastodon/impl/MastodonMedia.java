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

	/**
	 * Mastodon audio type
	 */
	private static final String TYPE_AUDIO = "audio";

	private String key;
	private String url;
	private String preview = "";
	private String description = "";
	private String blur;
	private int type = UNDEFINED;
	private Meta meta;

	/**
	 * @param json Mastodon status JSON format
	 */
	public MastodonMedia(JSONObject json) throws JSONException {
		JSONObject metaJson = json.optJSONObject("meta");
		String typeStr = json.getString("type");
		String url = json.getString("url");
		String preview = json.optString("preview_url", "");
		key = json.getString("id");
		blur = json.optString("blurhash", "");

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

			case TYPE_AUDIO:
				type = AUDIO;
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
		if (json.has("description") && !json.isNull("description")) {
			description = json.getString("description");
		}
		if (metaJson != null) {
			meta = new MastodonMeta(metaJson);
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
		return description;
	}


	@Override
	public String getBlurHash() {
		return blur;
	}


	@Nullable
	@Override
	public Meta getMeta() {
		return meta;
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

	/**
	 *
	 */
	private static final class MastodonMeta implements Meta {

		private static final long serialVersionUID = 5103849502754551661L;

		private double duration;
		private int previewWidth;
		private int previewHeight;
		private int originalWidth;
		private int originalHeight;
		private int bitrate;
		private float framerate;

		/**
		 *
		 */
		public MastodonMeta(JSONObject json) {
			JSONObject original = json.optJSONObject("original");
			JSONObject small = json.optJSONObject("small");
			if (small != null) {
				previewWidth = small.optInt("width", 1);
				previewHeight = small.optInt("height", 1);
			}
			if (original != null) {
				String framerateStr = original.optString("frame_rate");
				originalWidth = original.optInt("width", 1);
				originalHeight = original.optInt("height", 1);
				bitrate = original.optInt("bitrate", 0) / 1024;
				duration = original.optDouble("duration", 0.0);
				// calculate framerate if any
				int split = framerateStr.indexOf("/");
				if (split > 0) {
					String upper = framerateStr.substring(0, split);
					String down = framerateStr.substring(split + 1);
					if (upper.matches("\\d+") && down.matches("\\d+") && !down.equals("0")) {
						framerate = (float) (Integer.parseInt(upper) / Integer.parseInt(down));
					}
				}
			}
		}


		@Override
		public double getDuration() {
			return duration;
		}


		@Override
		public int getWidthPreview() {
			return previewWidth;
		}


		@Override
		public int getHeightPreview() {
			return previewHeight;
		}


		@Override
		public int getWidth() {
			return originalWidth;
		}


		@Override
		public int getHeight() {
			return originalHeight;
		}


		@Override
		public int getBitrate() {
			return bitrate;
		}


		@Override
		public float getFrameRate() {
			return framerate;
		}
	}
}