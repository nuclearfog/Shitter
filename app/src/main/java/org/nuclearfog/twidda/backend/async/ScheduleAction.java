package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.ScheduledStatus;

/**
 * @author nuclearfog
 */
public class ScheduleAction extends AsyncExecutor<ScheduleAction.Param, ScheduleAction.Result> {

	private Connection connection;

	/**
	 *
	 */
	public ScheduleAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			if (param.mode == Param.UPDATE) {
				ScheduledStatus status = connection.updateScheduledStatus(param.id, param.time);
				return new Result(Result.UPDATE, status.getId(), status, null);
			} else if (param.mode == Param.REMOVE) {
				connection.cancelScheduledStatus(param.id);
				return new Result(Result.REMOVE, param.id, null, null);
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, 0L, null, exception);
		}
		return null;
	}

	/**
	 *
	 */
	public static class Param {

		public static final int UPDATE = 1;
		public static final int REMOVE = 2;

		final int mode;
		final long id, time;

		public Param(int mode, long id, long time) {
			this.mode = mode;
			this.time = time;
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int UPDATE = 10;
		public static final int REMOVE = 11;
		public static final int ERROR = -1;

		public final int mode;
		public final long id;
		@Nullable
		public final ScheduledStatus status;
		@Nullable
		public final ConnectionException exception;

		Result(int mode, long id, @Nullable ScheduledStatus status, @Nullable ConnectionException exception) {
			this.id = id;
			this.mode = mode;
			this.status = status;
			this.exception = exception;
		}
	}
}