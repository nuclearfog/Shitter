package org.nuclearfog.twidda.backend.helper.update;

import org.nuclearfog.twidda.model.WebPush;

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

	private String host;
	private boolean notifyMention, notifyStatus, notifyFollow, notifyFollowRequest;
	private boolean notifyFavorite, notifyRepost, notifyPoll, notifyEdit;
	private int policy;

	/**
	 * @param host unifiedpush host url
	 */
	public PushUpdate(String host) {
		this.host = host;
	}

	/**
	 * create push update from existing push subscription
	 */
	public PushUpdate(WebPush push) {
		host = push.getHost();
		notifyMention = push.alertMentionEnabled();
		notifyStatus = push.alertStatusPostEnabled();
		notifyFollowRequest = push.alertFollowRequestEnabled();
		notifyFollow = push.alertFollowingEnabled();
		notifyFavorite = push.alertFavoriteEnabled();
		notifyRepost = push.alertRepostEnabled();
		notifyPoll = push.alertPollEnabled();
		notifyEdit = push.alertStatusChangeEnabled();
		policy = POLICY_ALL; // todo implement this
	}


	public String getHost() {
		return host;
	}


	public boolean mentionsEnabled() {
		return notifyMention;
	}


	public boolean statusPostEnabled() {
		return notifyStatus;
	}


	public boolean statusEditEnabled() {
		return notifyEdit;
	}


	public boolean repostEnabled() {
		return notifyRepost;
	}


	public boolean favoriteEnabled() {
		return notifyFavorite;
	}


	public boolean pollEnabled() {
		return notifyPoll;
	}


	public boolean followEnabled() {
		return notifyFollow;
	}


	public boolean followRequestEnabled() {
		return notifyFollowRequest;
	}


	public int getPolicy() {
		return policy;
	}
}