package org.nuclearfog.twidda.backend.api.twitter.impl.v1;

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

	private static final long serialVersionUID = -1595992003137510951L;

	private boolean isCurrentUser;
	private boolean isFollowing;
	private boolean isFollower;
	private boolean isBlocked;
	private boolean isMuted;
	private boolean canDm;

	/**
	 * @param json JSON object containing relationship information
	 */
	public RelationV1(JSONObject json) throws JSONException {
		JSONObject relationship = json.getJSONObject("relationship");
		JSONObject source = relationship.getJSONObject("source");
		JSONObject target = relationship.getJSONObject("target");
		String sourceIdStr = source.getString("id_str");
		String targetIdStr = target.getString("id_str");

		isCurrentUser = sourceIdStr.equals(targetIdStr);
		isFollowing = source.optBoolean("following");
		isFollower = source.optBoolean("followed_by");
		isBlocked = source.optBoolean("blocking");
		isMuted = source.optBoolean("muting");
		canDm = source.optBoolean("can_dm");
	}

	@Override
	public boolean isCurrentUser() {
		return isCurrentUser;
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