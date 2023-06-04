package org.nuclearfog.twidda.backend.helper.update;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.model.Instance;

import java.io.Closeable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.TreeSet;

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
	private String mediaUri;
	@Nullable
	private MediaStatus mediaUpdate;
	private String name = "";
	private String text = "";


	private TreeSet<String> supportedFormats = new TreeSet<>();

	/**
	 * close inputstream of media file
	 */
	@Override
	public void close() {
		if (mediaUpdate != null) {
			mediaUpdate.close();
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
	 * get inputstream of the media file
	 *
	 * @return input stream
	 */
	@Nullable
	public MediaStatus getMediaUpdate() {
		return mediaUpdate;
	}

	/**
	 *
	 */
	@Nullable
	public Uri getMediaUri() {
		if (mediaUri != null)
			return Uri.parse(mediaUri);
		return null;
	}

	/**
	 * add media uri and create input stream
	 *
	 * @param context context used to create inputstream and mime type
	 * @param uri     uri of a local media file
	 * @return true if file is valid
	 */
	public boolean addMedia(Context context, @NonNull Uri uri) {
		DocumentFile file = DocumentFile.fromSingleUri(context, uri);
		String mime = context.getContentResolver().getType(uri);
		// check if file is valid
		if (mime == null || file == null || file.length() == 0 || !supportedFormats.contains(mime)) {
			return false;
		}
		this.mediaUri = uri.toString();
		try {
			mediaUpdate = new MediaStatus(context, uri, "");
		} catch (IllegalArgumentException exception) {
			return false;
		}
		return true;
	}

	/**
	 * initialize inputstream of the file to upload
	 *
	 * @return true if initialization succeded
	 */
	public boolean prepare(ContentResolver resolver) {
		return mediaUpdate == null || mediaUpdate.openStream(resolver);
	}

	/**
	 * set instance imformation such as status limitations
	 *
	 * @param instance instance imformation
	 */
	public void setInstanceInformation(Instance instance) {
		supportedFormats.addAll(Arrays.asList(instance.getSupportedFormats()));
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
		return "to=\"" + name + "\" text=\"" + text + "\" " + mediaUpdate;
	}
}