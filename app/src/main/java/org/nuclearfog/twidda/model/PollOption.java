package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * Represents a poll vote option
 *
 * @author nuclearfog
 */
public interface PollOption extends Serializable {

	/**
	 * @return title of the option
	 */
	String getTitle();

	/**
	 * @return vote count of the option
	 */
	int getVotes();

	/**
	 * @return true if option is selected
	 */
	boolean isSelected();
}