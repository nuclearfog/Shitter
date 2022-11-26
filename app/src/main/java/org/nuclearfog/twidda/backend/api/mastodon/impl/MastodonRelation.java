package org.nuclearfog.twidda.backend.api.mastodon.impl;

import org.json.JSONObject;
import org.nuclearfog.twidda.model.Relation;

/**
 * Mastodon implementation of an user relation
 *
 * @author nuclearfog
 */
public class MastodonRelation implements Relation {

	private boolean currentUser;
	private boolean following;
	private boolean follower;
	private boolean blocked;
	private boolean muted;


	public MastodonRelation(JSONObject json, long currentId) {
		currentUser = currentId == Long.parseLong(json.optString("id", "0"));
		following = json.optBoolean("following");
		follower = json.optBoolean("followed_by");
		blocked = json.optBoolean("blocking");
		muted = json.optBoolean("muting");
	}


	@Override
	public boolean isCurrentUser() {
		return currentUser;
	}


	@Override
	public boolean isFollowing() {
		return following;
	}


	@Override
	public boolean isFollower() {
		return follower;
	}


	@Override
	public boolean isBlocked() {
		return blocked;
	}


	@Override
	public boolean isMuted() {
		return muted;
	}


	@Override
	public boolean canDm() {
		return false;
	}
}