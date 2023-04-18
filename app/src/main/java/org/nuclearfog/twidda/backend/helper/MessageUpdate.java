package org.nuclearfog.twidda.backend.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import org.nuclearfog.twidda.model.Instance;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class is used to upload a message
 *
 * @author nuclearfog
 */
public class MessageUpdate implements Serializable {

	private static final long serialVersionUID = 991295406939128220L;

	private String uriString;
	private MediaStatus mediaUpdate;
	private String name = "";
	private String text = "";

	@Nullable
	private Instance instance;

	private Set<String> supportedFormats = new TreeSet<>();

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
		return Uri.parse(uriString);
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
		if (file == null || file.length() == 0) {
			return false;
		}
		// check if file format is supported
		if (mime == null || !supportedFormats.contains(mime)) {
			return false;
		}
		this.uriString = uri.toString();
		return true;
	}

	/**
	 * initialize inputstream of the file to upload
	 *
	 * @return true if initialization succeded
	 */
	public boolean prepare(ContentResolver resolver) {
		if (uriString == null) {
			// no need to check media files if not attached
			return true;
		}
		try {
			Uri uri = Uri.parse(uriString);
			String mimeType = resolver.getType(uri);
			InputStream fileStream = resolver.openInputStream(uri);
			if (fileStream != null && mimeType != null && fileStream.available() > 0) {
				mediaUpdate = new MediaStatus(fileStream, mimeType);
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
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

	/**
	 * close inputstream of media file
	 */
	public void close() {
		if (mediaUpdate != null) {
			mediaUpdate.close();
		}
	}

	@NonNull
	@Override
	public String toString() {
		return "to=\"" + name + "\" text=\"" + text + "\" media=" + (mediaUpdate != null);
	}
}