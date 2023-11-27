package org.nuclearfog.twidda.backend.api.mastodon.impl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.Announcement;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Reaction;

/**
 * @author nuclearfog
 */
public class MastodonAnnouncement implements Announcement {

	private static final long serialVersionUID = 8328117490026664930L;

	private long id;
	private String message;
	private long time;
	private boolean dismissed;
	private Emoji[] emojis = {};
	private Reaction[] reactions = {};

	/**
	 *
	 */
	public MastodonAnnouncement(JSONObject json) throws JSONException {
		JSONArray emojiArray = json.optJSONArray("emojis");
		JSONArray reactionArray = json.optJSONArray("reactions");
		String idStr = json.getString("id");
		String timeStr = json.getString("published_at");
		dismissed = json.optBoolean("read", false);
		message = StringUtils.extractText(json.getString("content"));
		time = StringUtils.getIsoTime(timeStr);
		if (emojiArray != null && emojiArray.length() > 0) {
			emojis = new Emoji[emojiArray.length()];
			for (int i = 0; i < emojis.length; i++) {
				JSONObject emojiJson = emojiArray.getJSONObject(i);
				emojis[i] = new MastodonEmoji(emojiJson);
			}
		}
		if (reactionArray != null && reactionArray.length() > 0) {
			reactions = new Reaction[reactionArray.length()];
			for (int i = 0; i < reactions.length; i++) {
				JSONObject reactionJson = reactionArray.getJSONObject(i);
				reactions[i] = new MastodonReaction(reactionJson);
			}
		}
		try {
			id = Long.parseLong(idStr);
		} catch (NumberFormatException e) {
			throw new JSONException("bad ID: " + idStr);
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public String getMessage() {
		return message;
	}


	@Override
	public long getTimestamp() {
		return time;
	}


	@Override
	public boolean isDismissed() {
		return dismissed;
	}


	@Override
	public Emoji[] getEmojis() {
		return emojis;
	}


	@Override
	public Reaction[] getReactions() {
		return reactions;
	}
}