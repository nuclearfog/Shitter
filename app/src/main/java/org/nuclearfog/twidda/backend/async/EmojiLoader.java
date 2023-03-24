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
import org.nuclearfog.twidda.backend.utils.ImageCache;
import org.nuclearfog.twidda.model.Emoji;

import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * Emoji image loader class
 *
 * @author nuclearfog
 */
public class EmojiLoader extends AsyncExecutor<EmojiLoader.EmojiParam, EmojiLoader.EmojiResult> {

	private Connection connection;
	private ImageCache cache;

	/**
	 *
	 */
	public EmojiLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		cache = ImageCache.getInstance(context);
	}


	@NonNull
	@Override
	protected EmojiResult doInBackground(@NonNull EmojiParam param) {
		try {
			Map<String, Bitmap> result = new TreeMap<>();
			for (Emoji emoji : param.emojis) {
				Bitmap icon = cache.getImage(emoji.getCode());
				if (icon == null) {
					MediaStatus media = connection.downloadImage(emoji.getUrl());
					InputStream input = media.getStream();
					icon = BitmapFactory.decodeStream(input);
					cache.putImage(emoji.getCode(), icon);
				}
				icon = Bitmap.createScaledBitmap(icon, icon.getWidth() / icon.getHeight() * param.size, param.size, false);
				result.put(emoji.getCode(), icon);
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