package org.nuclearfog.twidda.backend.async;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.ui.activities.MediaActivity;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class moves a downloaded image to the destiny folder
 *
 * @author nuclearfog
 * @see MediaActivity
 */
public class ImageSaver extends AsyncExecutor<ImageSaver.ImageParam, Boolean> {


	@NonNull
	@Override
	protected Boolean doInBackground(ImageParam param) {
		try {
			int length;
			byte[] buffer = new byte[4096];
			while ((length = param.inputStream.read(buffer)) > 0) {
				param.outputStream.write(buffer, 0, length);
			}
			param.inputStream.close();
			param.outputStream.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 *
	 */
	public static class ImageParam {
		public final InputStream inputStream;
		public final OutputStream outputStream;

		public ImageParam(InputStream inputStream, OutputStream outputStream) {
			this.inputStream = inputStream;
			this.outputStream = outputStream;
		}
	}
}