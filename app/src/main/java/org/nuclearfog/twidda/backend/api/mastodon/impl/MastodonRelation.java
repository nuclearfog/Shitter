package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Relation;

/**
 * Mastodon implementation of an user relation
 *
 * @author nuclearfog
 */
public class MastodonRelation implements Relation {

	private static final long serialVersionUID = -3824807644551732407L;

	private boolean currentUser;
	private boolean following;
	private boolean follower;
	private boolean blocked;
	private boolean muted;

	/**
	 * @param json      Relation json object
	 * @param currentId ID of the current user
	 */
	public MastodonRelation(JSONObject json, long currentId) throws JSONException {
		String idStr = json.getString("id");
		following = json.optBoolean("following");
		follower = json.optBoolean("followed_by");
		blocked = json.optBoolean("blocking");
		muted = json.optBoolean("muting");
		try {
			currentUser = currentId == Long.parseLong(idStr);
		} catch (NumberFormatException e) {
			throw new JSONException("bad ID:" + idStr);
		}
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


	@NonNull
	@Override
	public String toString() {
		return "following=" + following + " follower=" + follower +
				" blocked=" + blocked + " muted=" + muted;
	}
}