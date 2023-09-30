package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.update.PushUpdate;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.WebPush;

/**
 * Async class used to update push information
 *
 * @author nuclearfog
 */
public class PushUpdater extends AsyncExecutor<PushUpdate, PushUpdater.Result> {

	private Connection connection;
	private GlobalSettings settings;

	/**
	 *
	 */
	public PushUpdater(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		settings = GlobalSettings.get(context);
	}


	@Override
	protected Result doInBackground(@NonNull PushUpdate param) {
		try {
			WebPush webpush = connection.updatePush(param);
			settings.setWebPush(webpush);
			return new Result(webpush, null);
		} catch (ConnectionException e) {
			return new Result(null, e);
		}
	}

	/**
	 *
	 */
	public static class Result {

		public final WebPush push;
		public final ConnectionException exception;

		Result(WebPush push, ConnectionException exception) {
			this.push = push;
			this.exception = exception;
		}
	}
}