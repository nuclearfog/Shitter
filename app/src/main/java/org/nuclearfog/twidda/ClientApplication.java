package org.nuclearfog.twidda;

import android.app.Application;

import org.nuclearfog.twidda.backend.image.ImageCache;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.BlurHashDecoder;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.notification.PushSubscription;

/**
 * @author nuclearfog
 */
public class ClientApplication extends Application {

	private GlobalSettings settings;


	@Override
	public void onCreate() {
		super.onCreate();
		// setup push receiver
		settings = GlobalSettings.get(getApplicationContext());
		if (settings.pushEnabled()) {
			PushSubscription.subscripe(getApplicationContext());
		}
	}


	@Override
	public void onTerminate() {
		if (settings.pushEnabled()) {
			PushSubscription.unsubscripe(getApplicationContext());
		}
		super.onTerminate();
	}


	@Override
	public void onLowMemory() {
		ImageCache.clear();
		PicassoBuilder.clear();
		BlurHashDecoder.clearCache();
		super.onLowMemory();
	}
}