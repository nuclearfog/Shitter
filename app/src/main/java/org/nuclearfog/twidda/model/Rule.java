package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * Represents a rule of an {@link Instance}
 */
public interface Rule extends Serializable {

	/**
	 * get ID of the rule
	 */
	long getId();

	/**
	 * get detailed description of this rule
	 */
	String getDescription();
}