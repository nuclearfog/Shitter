package org.nuclearfog.twidda;

import android.app.Application;

import org.nuclearfog.twidda.backend.image.ImageCache;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.notification.PushSubscription;

/**
 * @author nuclearfog
 */
public class ClientApplication extends Application {


	@Override
	public void onCreate() {
		super.onCreate();
		PushSubscription.subscripe(getApplicationContext());
	}


	@Override
	public void onTerminate() {
		PushSubscription.unsubscripe(getApplicationContext());
		super.onTerminate();
	}


	@Override
	public void onLowMemory() {
		ImageCache.clear();
		PicassoBuilder.clear();
		super.onLowMemory();
	}
}