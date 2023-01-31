package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import org.nuclearfog.twidda.ui.activities.MediaActivity;

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
	private InputStream inputStream;
	private OutputStream outputStream;

	/**
	 * @param inputStream inputstream of a cached image file
	 * @param outputStream  destiny output stream of a file
	 */
	public ImageSaver(MediaActivity activity, InputStream inputStream, OutputStream outputStream) {
		super();
		weakRef = new WeakReference<>(activity);
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}


	@Override
	protected Boolean doInBackground(Void... v) {
		try {
			int length;
			byte[] buffer = new byte[4096];
			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}
			inputStream.close();
			outputStream.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
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