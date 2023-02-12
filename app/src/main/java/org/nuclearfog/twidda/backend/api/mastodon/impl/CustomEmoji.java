package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Emoji))
			return false;
		return ((Emoji)obj).getCode().equals(code);
	}


	@NonNull
	@Override
	public String toString() {
		return "code=\"" + code + "\" category=\"" + category + "\" url=\"" + url + "\"";
	}


	public boolean visible() {
		return visibleInPicker;
	}
}