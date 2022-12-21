package org.nuclearfog.twidda.backend.api.mastodon.impl;

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

	private String key;
	private String url;
	private String preview;
	private int type = NONE;


	public MastodonMedia(JSONObject json) throws JSONException {
		String typeStr = json.getString("type");
		key = json.getString("id");
		url = json.optString("url", "");
		preview = json.optString("preview_url");
		switch (typeStr) {
			case "image":
				type = PHOTO;
				break;

			case "gifv":
				type = GIF;
				break;

			case "video":
				type = VIDEO;
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
}