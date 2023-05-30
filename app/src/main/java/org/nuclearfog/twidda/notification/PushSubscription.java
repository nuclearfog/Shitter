package org.nuclearfog.twidda.notification;

import android.content.Context;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.unifiedpush.android.connector.ConstantsKt;
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
	 */
	public static void subscripe(Context context) {
		GlobalSettings settings = GlobalSettings.getInstance(context);
		if (settings.isLoggedIn() && settings.getLogin().getConfiguration().isWebpushSupported()) {
			ArrayList<String> features = new ArrayList<>(1);
			features.add(UnifiedPush.FEATURE_BYTES_MESSAGE);
			if (!settings.getWebPush().getHost().isEmpty()) {
				UnifiedPush.registerApp(context.getApplicationContext(), settings.getWebPush().getHost(), features, "");
			} else {
				UnifiedPush.registerApp(context.getApplicationContext(), ConstantsKt.INSTANCE_DEFAULT, features, "");
			}
		}
	}

	/**
	 * remove unified push subscription
	 */
	public static void unsubscripe(Context context) {
		GlobalSettings settings = GlobalSettings.getInstance(context);
		if (settings.isLoggedIn() && settings.getLogin().getConfiguration().isWebpushSupported() && settings.pushEnabled()) {
			UnifiedPush.unregisterApp(context.getApplicationContext(), settings.getWebPush().getHost());
		}
	}
}
