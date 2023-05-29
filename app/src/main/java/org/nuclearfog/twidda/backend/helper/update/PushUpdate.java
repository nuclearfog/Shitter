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

	private String host;
	private boolean notifyMention, notifyStatus, notifyFollow, notifyFollowRequest;
	private boolean notifyFavorite, notifyRepost, notifyPoll, notifyEdit;
	private int policy;

	/**
	 * @param host unifiedpush host url
	 */
	public PushUpdate(String host) {
		int idxQuery = host.indexOf("?");
		if (idxQuery > 0) {
			this.host = host.substring(0, idxQuery);
		} else {
			this.host = host;
		}
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
		policy = WebPush.POLICY_ALL;
	}


	public String getHost() {
		return host;
	}


	public boolean mentionsEnabled() {
		return notifyMention;
	}


	public void setMentionsEnabled(boolean enable) {
		notifyMention = enable;
	}


	public boolean statusPostEnabled() {
		return notifyStatus;
	}


	public void setStatusPostEnabled(boolean enable) {
		notifyStatus = enable;
	}


	public boolean statusEditEnabled() {
		return notifyEdit;
	}


	public void setStatusEditEnabled(boolean enable) {
		notifyEdit = enable;
	}


	public boolean repostEnabled() {
		return notifyRepost;
	}


	public void setRepostEnabled(boolean enable) {
		notifyRepost = enable;
	}


	public boolean favoriteEnabled() {
		return notifyFavorite;
	}


	public void setFavoriteEnabled(boolean enable) {
		notifyFavorite = enable;
	}


	public boolean pollEnabled() {
		return notifyPoll;
	}


	public void setPollEnabled(boolean enable) {
		notifyPoll = enable;
	}


	public boolean followEnabled() {
		return notifyFollow;
	}


	public void setFollowEnabled(boolean enable) {
		notifyFollow = enable;
	}


	public boolean followRequestEnabled() {
		return notifyFollowRequest;
	}


	public void setFollowRequestEnabled(boolean enable) {
		notifyFollowRequest = enable;
	}


	public int getPolicy() {
		return policy;
	}


	public void setPolicy(int policy) {
		this.policy = policy;
	}
}