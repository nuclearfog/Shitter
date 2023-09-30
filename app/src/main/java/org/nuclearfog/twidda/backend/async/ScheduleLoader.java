package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.lists.ScheduledStatuses;

/**
 * @author nuclearfog
 */
public class ScheduleLoader extends AsyncExecutor<ScheduleLoader.Param, ScheduleLoader.Result> {

	private Connection connection;

	public ScheduleLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			ScheduledStatuses statuses = connection.getScheduledStatuses(param.minId, param.maxId);
			return new Result(statuses, param.index, null);
		} catch (ConnectionException exception) {
			return new Result(null, 0, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {
		final long minId, maxId;
		final int index;

		public Param(long minId, long maxId, int index) {
			this.minId = minId;
			this.maxId = maxId;
			this.index = index;
		}
	}

	/**
	 *
	 */
	public static class Result {
		public final int index;
		@Nullable
		public final ScheduledStatuses statuses;
		@Nullable
		public final ConnectionException exception;

		Result(@Nullable ScheduledStatuses statuses, int index, @Nullable ConnectionException exception) {
			this.statuses = statuses;
			this.exception = exception;
			this.index = index;
		}
	}
}