package org.nuclearfog.twidda.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Interface for notifications
 *
 * @author nuclearfog
 */
public interface Notification extends Serializable {

	/**
	 * mention
	 */
	int TYPE_MENTION = 0x87AA;

	/**
	 * Someone (enabled notifications) for has posted a status
	 */
	int TYPE_STATUS = 0x394A;

	/**
	 * A (replied) status has been edited
	 */
	int TYPE_UPDATE = 0x2FB7;

	/**
	 * Someone reposted a status
	 */
	int TYPE_REPOST = 0xF2A8;

	/**
	 * new follower
	 */
	int TYPE_FOLLOW = 0x9BF5;

	/**
	 * Someone requested to follow
	 */
	int TYPE_REQUEST = 0xB80E;

	/**
	 * Someone favourited a status
	 */
	int TYPE_FAVORITE = 0xAA5F;

	/**
	 * a poll is finished
	 */
	int TYPE_POLL = 0x6EB7;

	/**
	 * notification ID
	 *
	 * @return ID
	 */
	long getId();

	/**
	 * type of the notification {@link #TYPE_FAVORITE,#TYPE_FOLLOW,#TYPE_MENTION,#TYPE_REPOST,#TYPE_REQUEST,#TYPE_STATUS,#TYPE_UPDATE}
	 *
	 * @return notification type
	 */
	int getType();

	/**
	 * get notification time
	 *
	 * @return time
	 */
	long getCreatedAt();

	/**
	 * get user from the notification
	 *
	 * @return user
	 */
	User getUser();

	/**
	 * get status when there was an interaction
	 *
	 * @return status
	 */
	@Nullable
	Status getStatus();
}