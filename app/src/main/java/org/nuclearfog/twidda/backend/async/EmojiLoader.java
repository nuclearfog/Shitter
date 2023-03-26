package org.nuclearfog.twidda.backend.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
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
				if (icon.getHeight() > 0 && icon.getWidth() > 0) {
					icon = Bitmap.createScaledBitmap(icon, icon.getWidth() * param.size / icon.getHeight(), param.size, false);
				} else {
					icon = Bitmap.createScaledBitmap(icon, param.size, param.size, false);
				}
				result.put(emoji.getCode(), icon);
			}
			cache.trimCache();
			return new EmojiResult(param.spannable, result);
		} catch (Exception exception) {
			return new EmojiResult(param.spannable, null);
		}
	}

	/**
	 *
	 */
	public static class EmojiParam {

		final Emoji[] emojis;
		final Spannable spannable;
		final int size;

		public EmojiParam(Emoji[] emojis, Spannable spannable, int size) {
			this.emojis = emojis;
			this.spannable = spannable;
			this.size = size;
		}
	}

	/**
	 *
	 */
	public static class EmojiResult {

		@Nullable
		public final Map<String, Bitmap> images;
		public final Spannable spannable;

		EmojiResult(Spannable spannable, @Nullable Map<String, Bitmap> images) {
			this.images = images;
			this.spannable = spannable;
		}
	}
}