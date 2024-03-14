package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.Field;

/**
 * @author nuclearfog
 */
public class MastodonField implements Field {

	private static final long serialVersionUID = 2278113885084330065L;

	private String key;
	private String value;
	private long timestamp = 0L;

	/**
	 * @param json fields json
	 */
	public MastodonField(JSONObject json) throws JSONException {
		key = json.getString("name");
		value = StringUtils.extractText(json.optString("value", ""));
		String timeStr = json.optString("verified_at", "");
		if (!timeStr.equals("null")) {
			timestamp = StringUtils.getIsoTime(timeStr);
		}
	}


	@Override
	public String getKey() {
		return key;
	}


	@Override
	public String getValue() {
		return value;
	}


	@Override
	public long getTimestamp() {
		return timestamp;
	}


	@NonNull
	@Override
	public String toString() {
		return "key=\"" + key + "\" value=\"" + value + "\"";
	}
}