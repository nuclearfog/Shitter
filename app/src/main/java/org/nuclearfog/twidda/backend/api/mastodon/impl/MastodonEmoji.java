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
public class MastodonEmoji implements Emoji {

	private static final long serialVersionUID = 2848675481626033993L;

	private String code;
	private String url;
	private String category;
	private boolean visibleInPicker;

	/**
	 * @param json CustomEmoji json format
	 */
	public MastodonEmoji(JSONObject json) throws JSONException {
		code = json.getString("shortcode");
		url = json.getString("static_url");
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
		return ((Emoji) obj).getCode().equals(getCode());
	}


	@NonNull
	@Override
	public String toString() {
		return "code=\"" + getCode() + "\" category=\"" + getCategory() + "\" url=\"" + getUrl() + "\"";
	}

	/**
	 * @return true if emoji is visible for picker
	 */
	public boolean visible() {
		return visibleInPicker;
	}
}