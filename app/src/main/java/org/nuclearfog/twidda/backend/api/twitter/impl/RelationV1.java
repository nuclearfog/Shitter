package org.nuclearfog.twidda.backend.api.twitter.impl;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Relation;

/**
 * API v 1.1 implementation of an user relation
 *
 * @author nuclearfog
 */
public class RelationV1 implements Relation {

	private boolean isHome;
	private boolean isFollowing;
	private boolean isFollower;
	private boolean isBlocked;
	private boolean isMuted;
	private boolean canDm;

	/**
	 * @param json JSON object containing relationship information
	 * @throws JSONException if values are missing
	 */
	public RelationV1(JSONObject json) throws JSONException {
		JSONObject relationship = json.getJSONObject("relationship");
		JSONObject source = relationship.getJSONObject("source");
		JSONObject target = relationship.getJSONObject("target");

		long sourceId = Long.parseLong(source.getString("id_str"));
		long targetId = Long.parseLong(target.getString("id_str"));
		isHome = sourceId == targetId;
		isFollowing = source.optBoolean("following");
		isFollower = source.optBoolean("followed_by");
		isBlocked = source.optBoolean("blocking");
		isMuted = source.optBoolean("muting");
		canDm = source.optBoolean("can_dm");
	}

	@Override
	public boolean isHome() {
		return isHome;
	}

	@Override
	public boolean isFollowing() {
		return isFollowing;
	}

	@Override
	public boolean isFollower() {
		return isFollower;
	}

	@Override
	public boolean isBlocked() {
		return isBlocked;
	}

	@Override
	public boolean isMuted() {
		return isMuted;
	}

	@Override
	public boolean canDm() {
		return canDm;
	}

	@NonNull
	@Override
	public String toString() {
		return "following=" + isFollowing + " follower=" + isFollower +
				" blocked=" + isBlocked + " muted=" + isMuted + " dm open=" + canDm;
	}
}