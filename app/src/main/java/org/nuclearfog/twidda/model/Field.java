package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * represents an user field.
 *
 * @author nuclearfog
 */
public interface Field extends Serializable, Comparable<Field> {

	/**
	 * get the key of a given field’s key-value pair.
	 *
	 * @return key string
	 */
	String getKey();

	/**
	 * get the value associated with the name key.
	 *
	 * @return value string
	 */
	String getValue();

	/**
	 * get the timestamp of the verification if any
	 *
	 * @return ISO 8601 Datetime or '0' if not defined
	 */
	long getTimestamp();


	@Override
	default int compareTo(Field field) {
		if (getTimestamp() != 0L && field.getTimestamp() != 0L) {
			// sort by date of verification, starting with the latest verification
			return Long.compare(field.getTimestamp(), getTimestamp());
		} else {
			// sort by key alphabetically
			return String.CASE_INSENSITIVE_ORDER.compare(getKey(), field.getKey());
		}
	}
}