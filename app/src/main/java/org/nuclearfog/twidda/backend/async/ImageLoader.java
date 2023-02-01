package org.nuclearfog.twidda.backend.async;

import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.ui.activities.ImageViewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * This AsyncTask class downloads images to a local cache folder
 * and creates Uri of the images.
 *
 * @author nuclearfog
 * @see ImageViewer
 */
public class ImageLoader extends AsyncTask<Uri, Void, Uri> {

	private WeakReference<ImageViewer> weakRef;
	private Connection connection;

	@Nullable
	private ConnectionException exception;
	private File cacheFolder;

	/**
	 * @param activity    Activity context
	 * @param cacheFolder cache folder where to store image files
	 */
	public ImageLoader(ImageViewer activity, File cacheFolder) {
		super();
		weakRef = new WeakReference<>(activity);
		connection = ConnectionManager.get(activity);
		this.cacheFolder = cacheFolder;
	}


	@Override
	protected Uri doInBackground(Uri... links) {
		try {
			// get input stream
			MediaStatus mediaUpdate = connection.downloadImage(links[0].toString());
			InputStream input = mediaUpdate.getStream();
			String mimeType = mediaUpdate.getMimeType();

			// create file
			String ext = '.' + mimeType.substring(mimeType.indexOf('/') + 1);
			File imageFile = new File(cacheFolder, StringTools.getRandomString() + ext);
			imageFile.createNewFile();

			// copy image to cache folder
			FileOutputStream output = new FileOutputStream(imageFile);
			int length;
			byte[] buffer = new byte[4096];
			while ((length = input.read(buffer)) > 0)
				output.write(buffer, 0, length);
			input.close();
			output.close();

			// create Uri from cached image
			return Uri.fromFile(imageFile);
		} catch (ConnectionException exception) {
			this.exception = exception;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	protected void onPostExecute(@Nullable Uri localUri) {
		ImageViewer activity = weakRef.get();
		if (activity != null) {
			if (localUri != null) {
				activity.onSuccess(localUri);
			} else {
				activity.onError(exception);
			}
		}
	}
}