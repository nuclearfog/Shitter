package org.nuclearfog.twidda.backend.utils;

import android.content.Context;

import org.nuclearfog.twidda.backend.proxy.ProxyAuthenticator;
import org.nuclearfog.twidda.backend.proxy.UserProxy;
import org.nuclearfog.twidda.config.GlobalSettings;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * OkHttp client builder
 *
 * @author nuclearfog
 */
public class ConnectionBuilder {

	/**
	 * cache folder name
	 */
	private static final String CACHE_FOLDER = "cache";


	private ConnectionBuilder() {
	}

	/**
	 * create OkHttp instance
	 *
	 * @param context application context to initialize
	 * @return OkHttpClient instance
	 */
	public static OkHttpClient create(Context context) {
		return create(context, 0);
	}

	/**
	 * create OkHttpClient instance with cache enabled
	 *
	 * @param context   application context to initialize
	 * @param cacheSize cache size
	 * @return OkHttpClient instance
	 */
	public static OkHttpClient create(Context context, int cacheSize) {
		// init okhttp client builder
		GlobalSettings settings = GlobalSettings.getInstance(context);
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.writeTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).connectTimeout(60, TimeUnit.SECONDS);
		// setup cache
		if (cacheSize > 0) {
			File cacheFolder = new File(context.getExternalCacheDir(), CACHE_FOLDER);
			if (!cacheFolder.exists())
				cacheFolder.mkdir();
			builder.cache(new Cache(cacheFolder, cacheSize));
		}
		// setup proxy
		builder.proxy(UserProxy.get(settings));
		builder.proxyAuthenticator(new ProxyAuthenticator(settings));
		return builder.build();
	}
}