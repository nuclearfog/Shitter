package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.update.PushUpdate;
import org.nuclearfog.twidda.model.WebPush;

/**
 * Async class used to update push information
 *
 * @author nuclearfog
 */
public class PushUpdater extends AsyncExecutor <PushUpdate, PushUpdater.PushUpdateResult> {

	private Connection connection;

	/**
	 *
	 */
	public PushUpdater(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected PushUpdateResult doInBackground(@NonNull PushUpdate param) {
		try {
			WebPush webpush = connection.updatePush(param);
			return new PushUpdateResult(webpush, null);
		} catch (ConnectionException e) {
			return new PushUpdateResult(null, e);
		} catch (Exception exception) {
			if (BuildConfig.DEBUG) {
				exception.printStackTrace();
			}
		}
		return null;
	}

	/**
	 *
	 */
	public static class PushUpdateResult {

		public final WebPush push;
		public final ConnectionException exception;

		PushUpdateResult(WebPush push, ConnectionException exception) {
			this.push = push;
			this.exception = exception;
		}
	}
}