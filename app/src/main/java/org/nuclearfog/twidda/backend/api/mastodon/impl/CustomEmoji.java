package org.nuclearfog.twidda.backend.api.mastodon.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Emoji;

/**
 * Mastododn implementation of emoji
 *
 * @author nuclearfog
 */
public class CustomEmoji implements Emoji {

	private String code;
	private String url;
	private String category;
	private boolean visibleInPicker;

	/**
	 * @param json CustomEmoji json format
	 */
	public CustomEmoji(JSONObject json) throws JSONException {
		code = json.getString("shortcode");
		url = json.getString("url");
		category = json.optString("category", "");
		visibleInPicker = json.optBoolean("visible_in_picker", true);
	}


	@Override
	public String getCode() {
		return code;
	}


	@Override
	public String getUrl() {
		return url;
	}


	@Override
	public String getCategory() {
		return category;
	}


	public boolean visible() {
		return visibleInPicker;
	}
}