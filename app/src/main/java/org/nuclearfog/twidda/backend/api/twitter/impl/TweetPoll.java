package org.nuclearfog.twidda.backend.api.twitter.impl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.model.Poll;

/**
 * implementation of a tweet poll
 *
 * @author nuclearfog
 */
public class TweetPoll implements Poll {

	private static final long serialVersionUID = 4587084581361253962L;

	private static final String VOTE_CLOSED = "closed";

	private long id;
	private boolean expired;
	private long expiredAt;
	private Option[] options;
	private int count = 0;


	public TweetPoll(JSONObject json) throws JSONException {
		JSONArray optionsJson = json.getJSONArray("options");
		String idStr = json.getString("id");
		expired = VOTE_CLOSED.equals(json.getString("voting_status"));
		expiredAt = StringTools.getTime(json.optString("end_datetime"), StringTools.TIME_TWITTER_V2);

		options = new Option[optionsJson.length()];
		for (int i = 0 ; i < optionsJson.length() ; i++) {
			options[i] = new TweetOption(optionsJson.getJSONObject(i));
			count += options[i].getVotes();
		}
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

	/**
	 *
	 */
	private static class TweetOption implements Option {

		private static final long serialVersionUID = -7594109890754209971L;

		private String name;
		private int voteCount;
		private boolean voted;

		TweetOption(JSONObject json) throws JSONException {
			name = json.getString("label");
			voteCount = json.getInt("votes");
			voted = false; // todo implement this
		}


		@Override
		public String getTitle() {
			return name;
		}


		@Override
		public int getVotes() {
			return voteCount;
		}


		@Override
		public boolean selected() {
			return voted;
		}
	}
}