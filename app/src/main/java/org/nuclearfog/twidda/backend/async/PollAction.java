package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.Poll;

import java.util.Arrays;

/**
 * Asynctask to update a poll vote
 *
 * @author nuclearfog
 */
public class PollAction extends AsyncExecutor<PollAction.Param, PollAction.Result> {

	private Connection connection;

	/**
	 *
	 */
	public PollAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.mode) {
				case Param.LOAD:
					Poll poll = connection.getPoll(param.poll.getId());
					return new Result(Result.LOAD, poll, null);

				case Param.VOTE:
					poll = connection.votePoll(param.poll, param.selection);
					return new Result(Result.VOTE, poll, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, null, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int LOAD = 1;
		public static final int VOTE = 2;

		final int mode;
		final Poll poll;
		final int[] selection;

		public Param(int mode, Poll poll, int[] selection) {
			this.mode = mode;
			this.poll = poll;
			this.selection = Arrays.copyOf(selection, selection.length);
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int LOAD = 3;
		public static final int VOTE = 4;

		public final int mode;
		@Nullable
		public final Poll poll;
		@Nullable
		public final ConnectionException exception;

		Result(int mode, @Nullable Poll poll, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.poll = poll;
			this.exception = exception;
		}
	}
}