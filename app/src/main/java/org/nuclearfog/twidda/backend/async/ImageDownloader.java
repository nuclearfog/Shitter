package org.nuclearfog.twidda.backend.async;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.ui.activities.ImageViewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class downloads images to a local cache folder
 * and creates Uri of the images.
 *
 * @author nuclearfog
 * @see ImageViewer
 */
public class ImageDownloader extends AsyncExecutor<ImageDownloader.Param, ImageDownloader.Result> {

	private Connection connection;

	/**
	 * @param context Activity context
	 */
	public ImageDownloader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param request) {
		try {
			// get input stream
			MediaStatus mediaUpdate = connection.downloadImage(request.uri.toString());
			InputStream input = mediaUpdate.getStream();
			String mimeType = mediaUpdate.getMimeType();
			if (input == null || mimeType == null) {
				return new Result(null, null);
			}
			// create file
			String ext = '.' + mimeType.substring(mimeType.indexOf('/') + 1);
			// use deterministic filename depending on the url
			File imageFile = new File(request.cacheFolder, StringUtils.getMD5signature(request.uri.toString()) + ext);
			// if file exists with this signature, use this file
			if (imageFile.exists()) {
				return new Result(Uri.fromFile(imageFile), null);
			}
			// copy image to cache folder
			imageFile.createNewFile();
			FileOutputStream output = new FileOutputStream(imageFile);
			int length;
			byte[] buffer = new byte[4096];
			while ((length = input.read(buffer)) > 0)
				output.write(buffer, 0, length);
			input.close();
			output.close();
			// create Uri from cached image
			return new Result(Uri.fromFile(imageFile), null);
		} catch (ConnectionException exception) {
			return new Result(null, exception);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 *
	 */
	public static class Param {

		final Uri uri;
		final File cacheFolder;

		/**
		 * @param uri         online url of the image
		 * @param cacheFolder local folder to cache the image
		 */
		public Param(Uri uri, File cacheFolder) {
			this.cacheFolder = cacheFolder;
			this.uri = uri;
		}
	}

	/**
	 *
	 */
	public static class Result {

		@Nullable
		public final Uri uri;
		@Nullable
		public final ConnectionException exception;

		/**
		 * @param uri local path of the cached image or null if an error occured
		 */
		Result(@Nullable Uri uri, @Nullable ConnectionException exception) {
			this.exception = exception;
			this.uri = uri;
		}
	}
}