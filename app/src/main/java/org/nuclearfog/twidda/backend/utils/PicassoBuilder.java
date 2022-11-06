package org.nuclearfog.twidda.backend.utils;

import android.content.Context;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.database.GlobalSettings.OnSettingsChangeListener;

/**
 * Create Picasso instance with proxy connection and image cache
 *
 * @author nuclearfog
 */
public class PicassoBuilder implements OnSettingsChangeListener {

	/**
	 * image cache size in bytes
	 */
	private static final int CACHE_SIZE = 32 * 1024 * 1024;

	private static PicassoBuilder instance;
	private static boolean notifySettingsChange = false;

	private OkHttp3Downloader downloader;

	/**
	 *
	 */
	private PicassoBuilder(Context context) {
		GlobalSettings settings = GlobalSettings.getInstance(context);
		settings.addSettingsChangeListener(this);
		downloader = new OkHttp3Downloader(ConnectionBuilder.create(context, CACHE_SIZE));
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
		return new Picasso.Builder(context).downloader(instance.downloader).build();
	}


	@Override
	public void onSettingsChange() {
		notifySettingsChange = true;
	}
}