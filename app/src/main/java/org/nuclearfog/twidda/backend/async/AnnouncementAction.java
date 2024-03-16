package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;

/**
 * Async loader to remove instance announcements
 *
 * @author nuclearfog
 */
public class AnnouncementAction extends AsyncExecutor<AnnouncementAction.Param, AnnouncementAction.Result> {

	private Connection connection;

	/**
	 *
	 */
	public AnnouncementAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			if (param.action == Param.DISMISS) {
				connection.dismissAnnouncement(param.id);
				return new Result(Result.DISMISS, null);
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, exception);
		}
		return null;
	}

	/**
	 *
	 */
	public static class Param {

		public static final int DISMISS = 1;

		final int action;
		final long id;

		/**
		 * @param action action to apply on announcement {@link #DISMISS}
		 * @param id     ID of the announcement
		 */
		public Param(int action, long id) {
			this.action = action;
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int DISMISS = 10;

		public final int action;
		@Nullable
		public final ConnectionException exception;

		/**
		 * @param action    action applied on announcement
		 */
		public Result(int action, @Nullable ConnectionException exception) {
			this.action = action;
			this.exception = exception;
		}
	}
}