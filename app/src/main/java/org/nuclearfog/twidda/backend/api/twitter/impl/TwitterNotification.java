package org.nuclearfog.twidda.backend.api.twitter.impl;

import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;

/**
 * Twitter implementation of a notification
 * Twitter currently only supports mentions as notification
 *
 * @author nuclearfog
 */
public class TwitterNotification implements Notification {

	private static final long serialVersionUID = -2434138376220697796L;

	private Status status;


	public TwitterNotification(Status status) {
		this.status = status;
	}


	@Override
	public long getId() {
		return status.getId();
	}


	@Override
	public int getType() {
		return TYPE_MENTION;
	}


	@Override
	public long createdAt() {
		return status.getTimestamp();
	}


	@Override
	public User getUser() {
		return status.getAuthor();
	}


	@Override
	public Status getStatus() {
		return status;
	}
}