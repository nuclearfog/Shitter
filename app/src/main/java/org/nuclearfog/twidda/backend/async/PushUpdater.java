package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.update.PushUpdate;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.WebPush;

/**
 * Async class used to update push information
 *
 * @author nuclearfog
 */
public class PushUpdater extends AsyncExecutor <PushUpdate, Void> {

	private Connection connection;
	private GlobalSettings settings;

	/**
	 *
	 */
	public PushUpdater(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		settings = GlobalSettings.getInstance(context);
	}


	@Override
	protected Void doInBackground(@NonNull PushUpdate param) {
		try {
			WebPush webpush = connection.updatePush(param);
			settings.setWebPush(webpush);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}