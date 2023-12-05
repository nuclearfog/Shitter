package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * Represents a web push subscription.
 *
 * @author nuclearfog
 */
public interface WebPush extends Serializable {

	/**
	 * show all notifications
	 */
	int POLICY_ALL = 1;

	/**
	 * show only notifications of followed users
	 */
	int POLICY_FOLLOWING = 2;

	/**
	 * show only notifications of followers
	 */
	int POLICY_FOLLOWER = 3;

	/**
	 * disable push notification
	 */
	int POLICY_NONE = 4;

	/**
	 * @return ID of the subscription
	 */
	long getId();

	/**
	 * @return webpush host url
	 */
	String getHost();

	/**
	 * @return an intern hash value to communicate with external app
	 */
	String getInstance();

	/**
	 * @return unique server key set from {@link org.nuclearfog.twidda.backend.api.Connection}
	 */
	String getServerKey();

	/**
	 * @return encryption public key
	 */
	String getPublicKey();

	/**
	 * @return encryption public key
	 */
	String getPrivateKey();

	/**
	 * @return auth secret
	 */
	String getAuthSecret();

	/**
	 * @return true if notification for mentions is enabled
	 */
	boolean alertMentionEnabled();

	/**
	 * @return true if status notification (profile subscription) is enabled
	 */
	boolean alertNewStatusEnabled();

	/**
	 * @return true if 'status reposted' notification is enabled
	 */
	boolean alertRepostEnabled();

	/**
	 * @return true if 'new follower' notification is enabled
	 */
	boolean alertFollowingEnabled();

	/**
	 * @return true if 'follow request' notification is enabled
	 */
	boolean alertFollowRequestEnabled();

	/**
	 * @return true if 'status favorited' notification is enabled
	 */
	boolean alertFavoriteEnabled();

	/**
	 * @return true if 'poll finished' notification is enabled
	 */
	boolean alertPollEnabled();

	/**
	 * @return true if 'status changed' notification is enabled
	 */
	boolean alertStatusChangeEnabled();

	/**
	 * @return push policy {@link #POLICY_ALL,#POLICY_FOLLOWER,#POLICY_FOLLOWING,#POLICY_NONE}
	 */
	int getPolicy();
}