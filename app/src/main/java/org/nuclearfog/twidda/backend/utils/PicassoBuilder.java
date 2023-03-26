package org.nuclearfog.twidda.backend.utils;

import android.content.Context;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.config.GlobalSettings.OnSettingsChangeListener;

/**
 * Create Picasso instance with proxy connection and image cache
 *
 * @author nuclearfog
 */
public class PicassoBuilder implements OnSettingsChangeListener {

	/**
	 * local image cache size in bytes
	 */
	private static final int STORAGE_SIZE = 32 * 1024 * 1024;
	/**
	 * picasso cache size
	 */
	private static final int CACHE_SIZE = 16 * 1024 * 1024;

	private static PicassoBuilder instance;
	private static boolean notifySettingsChange = false;

	private OkHttp3Downloader downloader;
	private LruCache imageCache;

	/**
	 *
	 */
	private PicassoBuilder(Context context) {
		GlobalSettings settings = GlobalSettings.getInstance(context);
		settings.addSettingsChangeListener(this);
		downloader = new OkHttp3Downloader(ConnectionBuilder.create(context, STORAGE_SIZE));
		imageCache = new LruCache(CACHE_SIZE);
		notifySettingsChange = false;
	}

	/**
	 * @param context Context used to initialize singleton instance
	 * @return instance of Picasso with custom downloader
	 */
	public static Picasso get(Context context) {
		if (notifySettingsChange || instance == null) {
			instance = new PicassoBuilder(context);
		}
		return new Picasso.Builder(context).downloader(instance.downloader).memoryCache(instance.imageCache).build();
	}

	/**
	 * clear image cache
	 */
	public static void clear(Context context) {
		if (notifySettingsChange || instance == null)
			instance = new PicassoBuilder(context);
		instance.imageCache.clear();
	}


	@Override
	public void onSettingsChange() {
		notifySettingsChange = true;
	}
}