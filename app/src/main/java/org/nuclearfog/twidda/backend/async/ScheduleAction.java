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
			if (param.action == Param.UPDATE) {
				ScheduledStatus status = connection.updateScheduledStatus(param.id, param.time);
				return new Result(Result.UPDATE, status.getId(), status, null);
			} else if (param.action == Param.REMOVE) {
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

		final int action;
		final long id, time;

		/**
		 * @param action action to perform on existing scheduled status {@link #UPDATE,#REMOVE}
		 * @param id     if of the status schedule
		 * @param time   new schedule time used with {@link #UPDATE}
		 */
		public Param(int action, long id, long time) {
			this.action = action;
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

		public final int action;
		public final long id;
		@Nullable
		public final ScheduledStatus status;
		@Nullable
		public final ConnectionException exception;

		/**
		 * @param action performed action
		 * @param id     ID of the scheduled status
		 * @param status updated status schedule
		 */
		Result(int action, long id, @Nullable ScheduledStatus status, @Nullable ConnectionException exception) {
			this.id = id;
			this.action = action;
			this.status = status;
			this.exception = exception;
		}
	}
}