package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Poll;

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
	private MastodonOption[] options;
	private Emoji[] emojis = {};

	/**
	 * @param json Mastodon poll jswon format
	 */
	public MastodonPoll(JSONObject json) throws JSONException {
		JSONArray optionArray = json.getJSONArray("options");
		JSONArray voteArray = json.optJSONArray("own_votes");
		JSONArray emojiArray = json.optJSONArray("emojis");
		String exTimeStr = json.getString("expires_at");
		String idStr = json.getString("id");
		exTime = StringUtils.getTime(exTimeStr, StringUtils.TIME_MASTODON);
		expired = json.getBoolean("expired");
		voted = json.optBoolean("voted", false);
		multipleChoice = json.getBoolean("multiple");
		if (!json.isNull("voters_count")) {
			voteCount = json.getInt("voters_count");
		}
		options = new MastodonOption[optionArray.length()];
		for (int i = 0; i < optionArray.length(); i++) {
			JSONObject option = optionArray.getJSONObject(i);
			options[i] = new MastodonOption(option);
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
	public Option[] getOptions() {
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
			for (Option option : getOptions())
				optionsBuf.append(option).append(',');
			optionsBuf.deleteCharAt(optionsBuf.length() - 1).append(')');
		}
		return "id=" + getId() + " expired=" + getEndTime() + optionsBuf;
	}

	/**
	 * Mastodon poll option implementation
	 */
	private static class MastodonOption implements Option {

		private static final long serialVersionUID = 4625032116285945452L;

		private String title;
		private int voteCount;
		private boolean selected = false;

		/**
		 * @param json mastodon poll json format
		 */
		private MastodonOption(JSONObject json) {
			voteCount = json.optInt("votes_count", 0);
			title = json.optString("title", "-");
		}


		@Override
		public String getTitle() {
			return title;
		}


		@Override
		public int getVotes() {
			return voteCount;
		}


		@Override
		public boolean isSelected() {
			return selected;
		}


		@NonNull
		@Override
		public String toString() {
			return "title=\"" + getTitle() + "\" votes=" + getVotes() + " selected=" + isSelected();
		}


		@Override
		public boolean equals(@Nullable Object obj) {
			return obj instanceof Option && ((Option) obj).getTitle().equals(getTitle());
		}

		/**
		 * mark this option as selected
		 */
		private void setSelected() {
			selected = true;
		}
	}
}