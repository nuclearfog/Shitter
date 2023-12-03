package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;

/**
 * Async executor to update a reaction to an announcement
 *
 * @author nuclearfog
 */
public class ReactionUpdater extends AsyncExecutor<ReactionUpdater.Param, ReactionUpdater.Result> {

	private Connection connection;

	/**
	 *
	 */
	public ReactionUpdater(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.mode) {
				case Param.ADD:
					connection.addReaction(param.id, param.code);
					return new Result(Result.ADD, param.id, null);

				case Param.REMOVE:
					connection.removeReaction(param.id, param.code);
					return new Result(Result.REMOVE, param.id, null);
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, param.id, exception);
		}
		return null;
	}

	/**
	 *
	 */
	public static class Param {

		public static final int ADD = 1;
		public static final int REMOVE = 2;

		final int mode;
		final long id;
		final String code;

		public Param(int mode, long id, String code) {
			this.mode = mode;
			this.id = id;
			this.code = code;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int ADD = 10;
		public static final int REMOVE = 11;

		public final int mode;
		public final long id;
		@Nullable
		public final ConnectionException exception;

		Result(int mode, long id, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.id = id;
			this.exception = exception;
		}
	}
}