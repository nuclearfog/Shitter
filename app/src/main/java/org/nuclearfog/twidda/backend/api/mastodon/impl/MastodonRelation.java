package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

	private long id;
	private boolean isFollowing;
	private boolean isFollower;
	private boolean isBlocked;
	private boolean isMuted;

	/**
	 * @param json Relation json object
	 */
	public MastodonRelation(JSONObject json) throws JSONException {
		String idStr = json.getString("id");
		isFollowing = json.optBoolean("following");
		isFollower = json.optBoolean("followed_by");
		isBlocked = json.optBoolean("blocking");
		isMuted = json.optBoolean("muting");
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


	public void setFollowing(boolean isFollowing) {
		this.isFollowing = isFollowing;
	}


	public void setBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}


	public void setMuted(boolean isMuted) {
		this.isMuted = isMuted;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Relation))
			return false;
		Relation relation = (Relation) obj;
		return relation.getId() == getId() && relation.isBlocked() == isBlocked() && relation.isFollower() == isFollower()
				&& relation.isFollowing() == isFollowing() && relation.isMuted() == isMuted();
	}


	@NonNull
	@Override
	public String toString() {
		return "following=" + isFollowing() + " follower=" + isFollower() +
				" blocked=" + isBlocked() + " muted=" + isMuted();
	}
}