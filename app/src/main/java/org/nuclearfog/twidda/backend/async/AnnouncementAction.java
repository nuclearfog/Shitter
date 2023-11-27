package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;

/**
 * asyncloader to modify instance announcements
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
			switch (param.mode) {
				case Param.MODE_DISMISS:
					connection.dismissAnnouncement(param.id);
					return new Result(Result.MODE_DISMISS, param.id, null);
			}
		} catch (ConnectionException exception) {
			return new Result(Result.MODE_ERROR, param.id, exception);
		}
		return null;
	}

	/**
	 *
	 */
	public static class Param {

		public static final int MODE_DISMISS = 1;

		final int mode;
		final long id;

		public Param(int mode, long id) {
			this.mode = mode;
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int MODE_ERROR = -1;
		public static final int MODE_DISMISS = 10;

		public final int mode;
		public final long id;
		@Nullable
		public final ConnectionException exception;

		public Result(int mode, long id, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.id = id;
			this.exception = exception;
		}
	}
}