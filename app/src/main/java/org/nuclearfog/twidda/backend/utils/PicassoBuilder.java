package org.nuclearfog.twidda.backend.utils;

import android.content.Context;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.backend.proxy.ProxyAuthenticator;
import org.nuclearfog.twidda.backend.proxy.UserProxy;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * Create Picasso instance with proxy connection and TLS 1.2 support for pre Lollipop devices
 *
 * @author nuclearfog
 */
public class PicassoBuilder implements GlobalSettings.SettingsListener {

	/**
	 * cache folder size in bytes
	 */
	private static final int CACHE_SIZE = 32 * 1024 * 1024;

	/**
	 * cache folder name
	 */
	private static final String CACHE_FOLDER = "picasso-cache";

	private static PicassoBuilder instance;
	private static boolean notifySettingsChange = false;

	private OkHttp3Downloader downloader;


	private PicassoBuilder(Context context) {
		GlobalSettings settings = GlobalSettings.getInstance(context);
		settings.addSettingsChangeListener(this);
		OkHttpClient.Builder builder = new OkHttpClient.Builder();

		// setup cache
		File cacheFolder = new File(context.getExternalCacheDir(), CACHE_FOLDER);
		if (!cacheFolder.exists())
			cacheFolder.mkdir();
		builder.cache(new Cache(cacheFolder, CACHE_SIZE));

		// setup proxy
		if (settings.isProxyEnabled()) {
			builder.proxy(UserProxy.get(settings));
			if (settings.isProxyAuthSet()) {
				builder.proxyAuthenticator(new ProxyAuthenticator(settings));
			}
		}
		downloader = new OkHttp3Downloader(builder.build());
		notifySettingsChange = false;
	}

	/**
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