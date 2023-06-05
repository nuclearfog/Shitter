package org.nuclearfog.twidda;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import org.nuclearfog.twidda.backend.image.ImageCache;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.notification.PushNotification;
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
		settings = GlobalSettings.getInstance(getApplicationContext());
		if (settings.pushEnabled()) {
			PushSubscription.subscripe(getApplicationContext());
		}
		// setup notification channel
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationManager manager = getSystemService(NotificationManager.class);
			NotificationChannel channel = new NotificationChannel(PushNotification.NOTIFICATION_ID_STR, PushNotification.NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH);
			manager.createNotificationChannel(channel);
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
		super.onLowMemory();
	}
}