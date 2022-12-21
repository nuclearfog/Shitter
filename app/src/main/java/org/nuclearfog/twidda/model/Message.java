package org.nuclearfog.twidda.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * interface class for directmessage implementations
 *
 * @author nuclearfog
 */
public interface Message extends Serializable {

	/**
	 * @return ID of the direct message
	 */
	long getId();

	/**
	 * @return author of the message
	 */
	User getSender();

	/**
	 * @return receiver of the message
	 */
	long getReceiverId();

	/**
	 * @return message text
	 */
	String getText();

	/**
	 * @return date of creation
	 */
	long getTimestamp();

	/**
	 * @return get attached media link
	 */
	@Nullable
	Media getMedia();
}