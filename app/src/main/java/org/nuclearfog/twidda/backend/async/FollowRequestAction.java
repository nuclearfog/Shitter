package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;

/**
 * async loader to accept/reject a follow request
 *
 * @author nuclearfog
 */
public class FollowRequestAction extends AsyncExecutor<FollowRequestAction.Param, FollowRequestAction.Result> {


	private Connection connection;

	/**
	 *
	 */
	public FollowRequestAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.mode) {
				case Param.ACCEPT:
					connection.acceptFollowRequest(param.user_id);
					return new Result(Result.ACCEPT, param.notification_id, null);

				case Param.REJECT:
					connection.rejectFollowRequest(param.user_id);
					return new Result(Result.REJECT, param.notification_id, null);
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, param.notification_id, exception);
		}
		return null;
	}

	/**
	 *
	 */
	public static class Param {

		public static final int ACCEPT = 1;
		public static final int REJECT = 2;

		final int mode;
		final long user_id;
		final long notification_id;

		public Param(int mode, long user_id, long notification_id) {
			this.mode = mode;
			this.user_id = user_id;
			this.notification_id = notification_id;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int ACCEPT = 10;
		public static final int REJECT = 20;

		public final int mode;
		public final long notification_id;
		@Nullable
		public final ConnectionException exception;

		Result(int mode, long notification_id, @Nullable ConnectionException exception) {
			this.exception = exception;
			this.mode = mode;
			this.notification_id = notification_id;
		}
	}
}