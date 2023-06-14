package org.nuclearfog.twidda.notification;

import android.content.Context;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.unifiedpush.android.connector.RegistrationDialogContent;
import org.unifiedpush.android.connector.UnifiedPush;

import java.util.ArrayList;

/**
 * Unified push subscription manager class
 *
 * @author nuclearfog
 */
public class PushSubscription {

	/**
	 * subscripe to unified push service using app settings
	 * push messages will be then received by {@link PushNotificationReceiver}
	 */
	public static void subscripe(Context context) {
		GlobalSettings settings = GlobalSettings.getInstance(context);
		if (settings.isLoggedIn() && settings.getLogin().getConfiguration().isWebpushSupported()) {
			try {
				ArrayList<String> features = new ArrayList<>(1);
				features.add(UnifiedPush.FEATURE_BYTES_MESSAGE);
				UnifiedPush.registerAppWithDialog(context, settings.getPushInstance(), new RegistrationDialogContent(), features, "");
			} catch (Exception exception) {
				// thrown when ntfy-app was not found
				if (BuildConfig.DEBUG) {
					exception.printStackTrace();
				}
			}
		}
	}

	/**
	 * unregister from unified push
	 */
	public static void unsubscripe(Context context) {
		GlobalSettings settings = GlobalSettings.getInstance(context);
		if (settings.isLoggedIn() && settings.getLogin().getConfiguration().isWebpushSupported()) {
			try {
				UnifiedPush.unregisterApp(context.getApplicationContext(), settings.getWebPush().getHost());
			} catch (Exception exception) {
				// thrown when ntfy-app was not found
				if (BuildConfig.DEBUG) {
					exception.printStackTrace();
				}
			}
		}
	}
}