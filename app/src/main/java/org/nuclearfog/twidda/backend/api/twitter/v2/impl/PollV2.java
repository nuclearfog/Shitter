package org.nuclearfog.twidda.backend.api.twitter.v2.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.Poll;

/**
 * implementation of a tweet poll
 *
 * @author nuclearfog
 */
public class PollV2 implements Poll {

	private static final long serialVersionUID = 4587084581361253962L;

	/**
	 * fields to add twitter poll object
	 */
	public static final String FIELDS_POLL = "poll.fields=duration_minutes%2Cend_datetime%2Cid%2Coptions%2Cvoting_status";


	private static final String VOTE_CLOSED = "closed";

	private long id;
	private boolean expired;
	private long expiredAt;
	private Option[] options;
	private int count = 0;

	/**
	 * @param json tweet poll json format
	 */
	public PollV2(JSONObject json) throws JSONException {
		JSONArray optionsJson = json.getJSONArray("options");
		String idStr = json.getString("id");
		expired = VOTE_CLOSED.equals(json.getString("voting_status"));
		expiredAt = StringUtils.getTime(json.optString("end_datetime"), StringUtils.TIME_TWITTER_V2);
		// add options
		options = new Option[optionsJson.length()];
		for (int i = 0; i < optionsJson.length(); i++) {
			options[i] = new TwitterPollOption(optionsJson.getJSONObject(i));
			count += options[i].getVotes();
		}
		// add ID
		try {
			id = Long.parseLong(idStr);
		} catch (NumberFormatException e) {
			throw new JSONException("bad ID:" + idStr);
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public boolean voted() {
		// todo implement this
		return false;
	}


	@Override
	public boolean closed() {
		return expired;
	}


	@Override
	public int getLimit() {
		return 0;
	}


	@Override
	public long expirationTime() {
		return expiredAt;
	}


	@Override
	public int voteCount() {
		return count;
	}


	@Override
	public Option[] getOptions() {
		return options;
	}


	@Override
	public boolean equals(Object o) {
		return o instanceof Poll && ((Poll)o).getId() == getId();
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
		return "id=" + getId() + " expired=" + expirationTime() + optionsBuf;
	}

	/**
	 * implementation of a poll option
	 */
	private static class TwitterPollOption implements Option {

		private static final long serialVersionUID = -7594109890754209971L;

		private String title;
		private int voteCount;
		private boolean selected;

		/**
		 * @param json Twitter poll option json
		 */
		private TwitterPollOption(JSONObject json) throws JSONException {
			title = json.getString("label");
			voteCount = json.getInt("votes");
			selected = false; // todo implement this
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
	}
}