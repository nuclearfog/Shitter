package org.nuclearfog.twidda.backend.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.model.Emoji;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * Emoji image loader class
 *
 * @author nuclearfog
 */
public class EmojiLoader extends AsyncExecutor<EmojiLoader.EmojiParam, EmojiLoader.EmojiResult> {

	private static final String FOLDER = "emojis";

	private File imageFolder;
	private Connection connection;

	/**
	 *
	 */
	public EmojiLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		imageFolder = new File(context.getExternalCacheDir(), FOLDER);
		imageFolder.mkdirs();
	}


	@NonNull
	@Override
	protected EmojiResult doInBackground(@NonNull EmojiParam param) {
		try {
			Map<String, File> files = new TreeMap<>();
			Map<String, Bitmap> result = new TreeMap<>();
			// cache all local image files first
			File[] imageFiles = imageFolder.listFiles();
			if (imageFiles != null) {
				for (File file : imageFiles) {
					files.put(file.getName(), file);
				}
			}
			for (Emoji emoji : param.emojis) {
				File file = files.get(emoji.getCode());
				if (file == null) {
					// download image to cache
					MediaStatus media = connection.downloadImage(emoji.getUrl());
					InputStream input = media.getStream();
					file = new File(imageFolder, emoji.getCode());
					file.createNewFile();
					FileOutputStream output = new FileOutputStream(file);
					Bitmap icon = BitmapFactory.decodeStream(input);
					icon.compress(Bitmap.CompressFormat.PNG, 1, output);
					// resize image
					icon = Bitmap.createScaledBitmap(icon, icon.getWidth() / icon.getHeight() * param.size, param.size, false);
					result.put(emoji.getCode(), icon);
				} else {
					// load image from cache
					FileInputStream inputStream = new FileInputStream(file);
					Bitmap icon = BitmapFactory.decodeStream(inputStream);
					// resize image
					icon = Bitmap.createScaledBitmap(icon, icon.getWidth() / icon.getHeight() * param.size, param.size, false);
					result.put(emoji.getCode(), icon);
				}
			}
			return new EmojiResult(result, null);
		} catch (ConnectionException exception) {
			return new EmojiResult(null, exception);
		} catch (Exception exception) {
			return new EmojiResult(null, null);
		}
	}

	/**
	 *
	 */
	public static class EmojiParam {
		Emoji[] emojis;
		int size;

		public EmojiParam(Emoji[] emojis, int size) {
			this.emojis = emojis;
			this.size = size;
		}
	}


	/**
	 *
	 */
	public static class EmojiResult {

		@Nullable
		public Map<String, Bitmap> images;
		@Nullable
		public ConnectionException exception;

		EmojiResult(@Nullable Map<String, Bitmap> images, @Nullable ConnectionException exception) {
			this.exception = exception;
			this.images = images;
		}
	}
}