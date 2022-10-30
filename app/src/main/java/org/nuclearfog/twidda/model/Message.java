package org.nuclearfog.twidda.model;

import android.net.Uri;

import androidx.annotation.Nullable;

/**
 * interface class for directmessage implementations
 *
 * @author nuclearfog
 */
public interface Message {

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
	User getReceiver();

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
	Uri getMedia();
}