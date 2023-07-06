package org.nuclearfog.twidda.backend.helper.update;

import android.content.ContentResolver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.model.Instance;

import java.io.Closeable;
import java.io.Serializable;

/**
 * This class is used to upload a message
 *
 * @author nuclearfog
 */
public class MessageUpdate implements Serializable, Closeable {

	private static final long serialVersionUID = 991295406939128220L;

	@Nullable
	private Instance instance;
	@Nullable
	private MediaStatus mediaStatus;
	private String name = "";
	private String text = "";

	/**
	 * close inputstream of media file
	 */
	@Override
	public void close() {
		if (mediaStatus != null) {
			mediaStatus.close();
		}
	}

	/**
	 * @param name screen name of the user
	 */
	public void setReceiver(String name) {
		this.name = name;
	}

	/**
	 * @param text message text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * get name of the receiver
	 *
	 * @return screen name
	 */
	public String getReceiver() {
		return name;
	}

	/**
	 * get message text
	 *
	 * @return message text
	 */
	public String getMessage() {
		return text;
	}


	/**
	 * get media attachment
	 *
	 * @return media attachment
	 */
	@Nullable
	public MediaStatus getMediaStatus() {
		return mediaStatus;
	}

	/**
	 * add/update media attachment
	 *
	 * @param mediaStatus media attachment
	 */
	public void setMediaUpdate(MediaStatus mediaStatus) {
		this.mediaStatus = mediaStatus;
	}

	/**
	 * initialize inputstream of the file to upload
	 *
	 * @return true if initialization succeded
	 */
	public boolean prepare(ContentResolver resolver) {
		return mediaStatus == null || mediaStatus.openStream(resolver);
	}

	/**
	 * set instance imformation such as status limitations
	 *
	 * @param instance instance imformation
	 */
	public void setInstanceInformation(Instance instance) {
		this.instance = instance;
	}

	/**
	 * get instance information
	 */
	@Nullable
	public Instance getInstance() {
		return instance;
	}


	@NonNull
	@Override
	public String toString() {
		return "to=\"" + name + "\" text=\"" + text + "\" " + mediaStatus;
	}
}