package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.PollOption;

/**
 * Poll implementation for Mastodon
 *
 * @author nuclearfog
 */
public class MastodonPoll implements Poll {

	private static final long serialVersionUID = 1387541658586903903L;

	private long id;
	private long exTime;
	private boolean expired;
	private boolean voted;
	private boolean multipleChoice;
	private int voteCount;
	private MastodonPollOption[] options;
	private Emoji[] emojis = {};

	/**
	 * @param json Mastodon poll jswon format
	 */
	public MastodonPoll(JSONObject json) throws JSONException {
		JSONArray optionArray = json.getJSONArray("options");
		JSONArray voteArray = json.optJSONArray("own_votes");
		JSONArray emojiArray = json.optJSONArray("emojis");
		String idStr = json.optString("id", "-1");
		exTime = StringUtils.getIsoTime(json.optString("expires_at", ""));
		expired = json.optBoolean("expired", false);
		voted = json.optBoolean("voted", false);
		multipleChoice = json.optBoolean("multiple", false);
		if (!json.isNull("voters_count")) {
			voteCount = json.getInt("voters_count");
		}
		options = new MastodonPollOption[optionArray.length()];
		for (int i = 0; i < optionArray.length(); i++) {
			JSONObject option = optionArray.optJSONObject(i);
			if (option != null) {
				options[i] = new MastodonPollOption(option);
			} else {
				options[i] = new MastodonPollOption(optionArray.optString(i, "-"));
			}
		}
		if (voteArray != null) {
			for (int i = 0; i < voteArray.length(); i++) {
				int index = voteArray.getInt(i);
				if (index >= 0 && index < options.length) {
					options[index].setSelected();
				}
			}
		}
		if (emojiArray != null && emojiArray.length() > 0) {
			emojis = new Emoji[emojiArray.length()];
			for (int i = 0; i < emojis.length; i++) {
				emojis[i] = new MastodonEmoji(emojiArray.getJSONObject(i));
			}
		}
		try {
			id = Long.parseLong(idStr);
		} catch (NumberFormatException e) {
			throw new JSONException("Bad ID: " + idStr);
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public boolean voted() {
		return voted;
	}


	@Override
	public boolean closed() {
		return expired;
	}


	@Override
	public boolean multipleChoiceEnabled() {
		return multipleChoice;
	}


	@Override
	public long getEndTime() {
		return exTime;
	}


	@Override
	public int voteCount() {
		return voteCount;
	}


	@Override
	public PollOption[] getOptions() {
		return options;
	}


	@Override
	public Emoji[] getEmojis() {
		return emojis;
	}


	@Override
	public boolean equals(Object o) {
		return o instanceof Poll && ((Poll) o).getId() == getId();
	}


	@NonNull
	@Override
	public String toString() {
		StringBuilder optionsBuf = new StringBuilder();
		if (getOptions().length > 0) {
			optionsBuf.append(" options=(");
			for (PollOption option : getOptions())
				optionsBuf.append(option).append(',');
			optionsBuf.deleteCharAt(optionsBuf.length() - 1).append(')');
		}
		return "id=" + getId() + " expired=" + getEndTime() + optionsBuf;
	}
}