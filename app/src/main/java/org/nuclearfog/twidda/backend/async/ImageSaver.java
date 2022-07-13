package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import org.nuclearfog.twidda.ui.activities.MediaActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

/**
 * This AsyncTask class moves a cached image to the destiny folder
 *
 * @author nuclearfog
 * @see MediaActivity
 */
public class ImageSaver extends AsyncTask<Void, Void, Boolean> {

	private WeakReference<MediaActivity> weakRef;
	private InputStream mediaStream;
	private OutputStream fileStream;

	/**
	 * @param mediaStream inputstream of a cached image file
	 * @param fileStream  destiny output stream of a file
	 */
	public ImageSaver(MediaActivity activity, InputStream mediaStream, OutputStream fileStream) {
		super();
		weakRef = new WeakReference<>(activity);
		this.mediaStream = mediaStream;
		this.fileStream = fileStream;
	}


	@Override
	protected Boolean doInBackground(Void... v) {
		try {
			int length;
			byte[] buffer = new byte[4096];
			while ((length = mediaStream.read(buffer)) > 0) {
				fileStream.write(buffer, 0, length);
			}
			mediaStream.close();
			fileStream.close();
		} catch (IOException err) {
			err.printStackTrace();
			return false;
		}
		return true;
	}


	@Override
	protected void onPostExecute(Boolean success) {
		MediaActivity activity = weakRef.get();
		if (activity != null) {
			if (success) {
				activity.onImageSaved();
			} else {
				activity.onError();
			}
		}
	}
}