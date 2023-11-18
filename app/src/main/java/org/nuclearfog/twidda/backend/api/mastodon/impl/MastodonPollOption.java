package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;
import org.nuclearfog.twidda.model.PollOption;

/**
 * @author nuclearfog
 */
public class MastodonPollOption implements PollOption {

	private static final long serialVersionUID = 4625032116285945452L;

	private String title;
	private int voteCount;
	private boolean selected = false;

	/**
	 * @param json mastodon poll option json
	 */
	public MastodonPollOption(JSONObject json) {
		voteCount = json.optInt("votes_count", 0);
		title = json.optString("title", "-");
	}

	/**
	 * @param title poll option title string
	 */
	public MastodonPollOption(String title) {
		this.title = title;
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
		return obj instanceof PollOption && ((PollOption) obj).getTitle().equals(getTitle());
	}

	/**
	 * mark this option as selected
	 */
	public void setSelected() {
		selected = true;
	}
}