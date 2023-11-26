package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * Represents an announcement set by an instance administrator
 *
 * @author nuclearfog
 */
public interface Announcement extends Serializable {

	/**
	 * @return ID of the announcement
	 */
	long getId();

	/**
	 * @return message text of the announcement
	 */
	String getMessage();

	/**
	 * @return announcement publishing time
	 */
	long getTimestamp();

	/**
	 * @return emojis used in message text
	 */
	Emoji[] getEmojis();

	/**
	 * @return user reactions of the announcement
	 */
	Reaction[] getReactions();
}