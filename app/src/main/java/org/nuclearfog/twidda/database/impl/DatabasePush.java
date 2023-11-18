package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.database.DatabaseAdapter.PushTable;
import org.nuclearfog.twidda.model.WebPush;

/**
 * Database implementation of Web push
 *
 * @author nuclearfog
 */
public class DatabasePush implements WebPush, PushTable {

	private static final long serialVersionUID = -3108068757732372763L;

	/**
	 * database columns
	 */
	public static final String[] COLUMNS = {
			ID,
			HOST,
			SERVER_KEY,
			PUB_KEY,
			SEC_KEY,
			AUTH_SECRET,
			FLAGS
	};

	private long id;
	private int policy;
	private String host = "";
	private String server_key = "";
	private String pub_key = "";
	private String sec_key = "";
	private String auth_sec = "";
	private boolean alertMention;
	private boolean alertPost;
	private boolean alertRepost;
	private boolean alertFollowing;
	private boolean alertRequest;
	private boolean alertFavorite;
	private boolean alertPoll;
	private boolean alertChange;

	/**
	 * @param cursor cursor containing these {@link #COLUMNS}
	 */
	public DatabasePush(Cursor cursor) {
		id = cursor.getLong(0);
		String host = cursor.getString(1);
		String server_key = cursor.getString(2);
		String pub_key = cursor.getString(3);
		String sec_key = cursor.getString(4);
		String auth_sec = cursor.getString(5);
		int flags = cursor.getInt(6);

		alertMention = (flags & MASK_MENTION) != 0;
		alertPost = (flags & MASK_STATUS) != 0;
		alertRepost = (flags & MASK_REPOST) != 0;
		alertFollowing = (flags & MASK_FOLLOWING) != 0;
		alertRequest = (flags & MASK_REQUEST) != 0;
		alertFavorite = (flags & MASK_FAVORITE) != 0;
		alertPoll = (flags & MASK_POLL) != 0;
		alertChange = (flags & MASK_MODIFIED) != 0;
		if ((flags & MASK_POLICY_ALL) != 0) {
			policy = POLICY_ALL;
		} else if ((flags & MASK_POLICY_FOLLOWING) != 0) {
			policy = POLICY_FOLLOWING;
		} else if ((flags & MASK_POLICY_FOLLOWER) != 0) {
			policy = POLICY_FOLLOWER;
		} else {
			policy = POLICY_NONE;
		}
		if (host != null)
			this.host = host;
		if (server_key != null)
			this.server_key = server_key;
		if (pub_key != null)
			this.pub_key = pub_key;
		if (sec_key != null)
			this.sec_key = sec_key;
		if (auth_sec != null)
			this.auth_sec = auth_sec;
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public String getHost() {
		return host;
	}


	@Override
	public String getServerKey() {
		return server_key;
	}


	@Override
	public String getPublicKey() {
		return pub_key;
	}


	@Override
	public String getPrivateKey() {
		return sec_key;
	}


	@Override
	public String getAuthSecret() {
		return auth_sec;
	}


	@Override
	public boolean alertMentionEnabled() {
		return alertMention;
	}


	@Override
	public boolean alertNewStatusEnabled() {
		return alertPost;
	}


	@Override
	public boolean alertRepostEnabled() {
		return alertRepost;
	}


	@Override
	public boolean alertFollowingEnabled() {
		return alertFollowing;
	}


	@Override
	public boolean alertFollowRequestEnabled() {
		return alertRequest;
	}


	@Override
	public boolean alertFavoriteEnabled() {
		return alertFavorite;
	}


	@Override
	public boolean alertPollEnabled() {
		return alertPoll;
	}


	@Override
	public boolean alertStatusChangeEnabled() {
		return alertChange;
	}


	@Override
	public int getPolicy() {
		return policy;
	}


	@NonNull
	@Override
	public String toString() {
		return "id=" + getId() + " url=\"" + getHost() + "\"";
	}
}