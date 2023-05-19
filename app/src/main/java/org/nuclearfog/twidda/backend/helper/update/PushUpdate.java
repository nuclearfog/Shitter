package org.nuclearfog.twidda.backend.helper.update;

import java.io.Serializable;

/**
 * Webpush updater class used to create a webpush subscription
 * @see org.nuclearfog.twidda.backend.api.Connection
 *
 * @author nuclearfog
 */
public class PushUpdate implements Serializable {

	private static final long serialVersionUID = -34599486422177957L;

	/**
	 * show all notifications
	 */
	public static final int POLICY_ALL = 1;

	/**
	 * show only notifications of followed users
	 */
	public static final int POLICY_FOLLOWING = 2;

	/**
	 * show only notifications of followers
	 */
	public static final int POLICY_FOLLOWER = 3;

	/**
	 * disable push notification
	 */
	public static final int POLICY_NONE = 4;

	private String endpoint;
	private boolean notifyMention, notifyStatus, notifyFollow, notifyFollowRequest;
	private boolean notifyFavorite, notifyRepost, notifyPoll, notifyEdit;
	private int policy;

	/**
	 *
	 */
	public PushUpdate(String endpoint) {
		int idx = endpoint.indexOf('?');
		if (idx > 0) {
			this.endpoint = endpoint.substring(0, idx);
		} else {
			this.endpoint = endpoint;
		}
	}


	public String getEndpoint() {
		return endpoint;
	}


	public boolean enableMentions() {
		return notifyMention;
	}


	public boolean enableStatus() {
		return notifyStatus;
	}


	public boolean enableStatusEdit() {
		return notifyEdit;
	}


	public boolean enableRepost() {
		return notifyRepost;
	}


	public boolean enableFavorite() {
		return notifyFavorite;
	}


	public boolean enablePoll() {
		return notifyPoll;
	}


	public boolean enableFollow() {
		return notifyFollow;
	}


	public boolean enableFollowRequest() {
		return notifyFollowRequest;
	}


	public int getPolicy() {
		return policy;
	}
}