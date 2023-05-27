package org.nuclearfog.twidda.notification;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.async.PushUpdater;
import org.nuclearfog.twidda.backend.helper.update.PushUpdate;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.unifiedpush.android.connector.MessagingReceiver;

/**
 * Push notification receiver used to trigger synchronization.
 *
 * @author nuclearfog
 */
public class PushNotificationReceiver extends MessagingReceiver {


	@Override
	public void onMessage(@NonNull Context context, @NonNull byte[] message, @NonNull String instance) {
		GlobalSettings settings = GlobalSettings.getInstance(context);
		if (settings.pushEnabled()) {
			// todo add manual synchonization
		}
	}


	@Override
	public void onNewEndpoint(@NonNull Context context, @NonNull String endpoint, @NonNull String instance) {
		PushUpdater pushUpdater = new PushUpdater(context);
		PushUpdate update = new PushUpdate(instance);
		pushUpdater.execute(update, null);
	}
}