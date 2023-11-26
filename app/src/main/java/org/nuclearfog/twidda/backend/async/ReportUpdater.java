package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.update.ReportUpdate;

/**
 * status/user report updater
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.dialogs.ReportDialog
 */
public class ReportUpdater extends AsyncExecutor<ReportUpdate, ReportUpdater.Result> {

	private Connection connection;

	/**
	 *
	 */
	public ReportUpdater(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull ReportUpdate param) {
		try {
			connection.createReport(param);
			return new Result(null);
		} catch (ConnectionException exception) {
			return new Result(exception);
		}
	}

	/**
	 *
	 */
	public static class Result {

		@Nullable
		public final ConnectionException exception;

		Result(@Nullable ConnectionException exception) {
			this.exception = exception;
		}
	}
}