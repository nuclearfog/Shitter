package org.nuclearfog.twidda.backend.api.twitter.v1.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

	private long id;
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

		isFollowing = source.optBoolean("following");
		isFollower = source.optBoolean("followed_by");
		isBlocked = source.optBoolean("blocking");
		isMuted = source.optBoolean("muting");
		canDm = source.optBoolean("can_dm");
		try {
			id = Long.parseLong(targetIdStr);
		} catch (NumberFormatException e) {
			throw new JSONException("bad ID: " + sourceIdStr);
		}
	}


	@Override
	public long getId() {
		return id;
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
	public boolean privateMessagingEnabled() {
		return canDm;
	}


	public void setFollowing(boolean isFollowing) {
		this.isFollowing = isFollowing;
	}


	public void setMuted(boolean isMuted) {
		this.isMuted = isMuted;
	}


	public void setBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Relation))
			return false;
		Relation relation = (Relation) obj;
		return relation.getId() == getId() && relation.isBlocked() == isBlocked() && relation.isFollower() == isFollower()
				&& relation.isFollowing() == isFollowing() && relation.isMuted() == isMuted() && relation.privateMessagingEnabled() == privateMessagingEnabled();
	}


	@NonNull
	@Override
	public String toString() {
		return "following=" + isFollowing() + " follower=" + isFollower() +
				" blocked=" + isBlocked() + " muted=" + isMuted() + " dm open=" + privateMessagingEnabled();
	}
}