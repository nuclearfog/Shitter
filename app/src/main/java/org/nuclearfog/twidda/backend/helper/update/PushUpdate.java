package org.nuclearfog.twidda.backend.helper.update;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.WebPush;

import java.io.Serializable;

/**
 * Webpush updater class used to create a webpush subscription
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.backend.api.Connection
 */
public class PushUpdate implements Serializable {

	private static final long serialVersionUID = -34599486422177957L;

	private String host;
	private boolean notifyMention, notifyStatus, notifyFollow, notifyFollowRequest;
	private boolean notifyFavorite, notifyRepost, notifyPoll, notifyEdit;
	private int policy;

	/**
	 * create pushupdate using webpush subscription and custom hostname
	 *
	 * @param webPush existing webpush subscription
	 * @param host    unifiedpush host url
	 */
	public PushUpdate(WebPush webPush, String host) {
		int idxQuery = host.indexOf("?");
		if (idxQuery > 0) {
			this.host = host.substring(0, idxQuery);
		} else {
			this.host = host;
		}
		notifyMention = webPush.alertMentionEnabled();
		notifyStatus = webPush.alertNewStatusEnabled();
		notifyFollowRequest = webPush.alertFollowRequestEnabled();
		notifyFollow = webPush.alertFollowingEnabled();
		notifyFavorite = webPush.alertFavoriteEnabled();
		notifyRepost = webPush.alertRepostEnabled();
		notifyPoll = webPush.alertPollEnabled();
		notifyEdit = webPush.alertStatusChangeEnabled();
		policy = WebPush.POLICY_ALL;
	}

	/**
	 * create push update from existing push subscription
	 *
	 * @param webPush existing webpush subscription
	 */
	public PushUpdate(WebPush webPush) {
		this(webPush, webPush.getHost());
	}

	/**
	 * get hostname of the push server
	 *
	 * @return hostname url
	 */
	public String getHost() {
		return host;
	}

	/**
	 * set hostname of the push server
	 *
	 * @param host hostname url
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return true if push-notification for mentions is enabled
	 */
	public boolean mentionsEnabled() {
		return notifyMention;
	}

	/**
	 * enable/disable push notification for new mentions
	 *
	 * @param enable true to enable notification
	 */
	public void setMentionsEnabled(boolean enable) {
		notifyMention = enable;
	}

	/**
	 * @return true if push-notification for status update is enabled
	 */
	public boolean statusPostEnabled() {
		return notifyStatus;
	}

	/**
	 * enable/disable push notification for status updates
	 *
	 * @param enable true to enable notification
	 */
	public void setStatusPostEnabled(boolean enable) {
		notifyStatus = enable;
	}

	/**
	 * @return true if push-notification for status edit is enabled
	 */
	public boolean statusEditEnabled() {
		return notifyEdit;
	}

	/**
	 * enable/disable push notification for edited statuses
	 *
	 * @param enable true to enable notification
	 */
	public void setStatusEditEnabled(boolean enable) {
		notifyEdit = enable;
	}

	/**
	 * @return true if push-notification for new status reposts is enabled
	 */
	public boolean repostEnabled() {
		return notifyRepost;
	}

	/**
	 * enable/disable push notification for new status reposts
	 *
	 * @param enable true to enable notification
	 */
	public void setRepostEnabled(boolean enable) {
		notifyRepost = enable;
	}

	/**
	 * @return true if push-notification for new status favorits is enabled
	 */
	public boolean favoriteEnabled() {
		return notifyFavorite;
	}

	/**
	 * enable/disable push notification for new follow-requests
	 *
	 * @param enable true to enable notification
	 */
	public void setFavoriteEnabled(boolean enable) {
		notifyFavorite = enable;
	}

	/**
	 * @return true if push-notification for polls is enabled
	 */
	public boolean pollEnabled() {
		return notifyPoll;
	}

	/**
	 * enable/disable push notification for finnished polls
	 *
	 * @param enable true to enable notification
	 */
	public void setPollEnabled(boolean enable) {
		notifyPoll = enable;
	}

	/**
	 * @return true if push-notification for new followers is enabled
	 */
	public boolean followEnabled() {
		return notifyFollow;
	}

	/**
	 * enable/disable push notification for new followers
	 *
	 * @param enable true to enable notification
	 */
	public void setFollowEnabled(boolean enable) {
		notifyFollow = enable;
	}

	/**
	 * @return true if push-notification for new follow requests is enabled
	 */
	public boolean followRequestEnabled() {
		return notifyFollowRequest;
	}

	/**
	 * enable/disable push notification for new follow-requests
	 *
	 * @param enable true to enable notification
	 */
	public void setFollowRequestEnabled(boolean enable) {
		notifyFollowRequest = enable;
	}

	/**
	 * @return push policy (limitation of the push-notification)
	 */
	public int getPolicy() {
		return policy;
	}

	/**
	 * set push policy
	 *
	 * @param policy {@link WebPush#POLICY_ALL,WebPush#POLICY_FOLLOWING,WebPush#POLICY_FOLLOWER}
	 */
	public void setPolicy(int policy) {
		this.policy = policy;
	}


	@NonNull
	@Override
	public String toString() {
		return "host=\"" + host + "\"";
	}
}