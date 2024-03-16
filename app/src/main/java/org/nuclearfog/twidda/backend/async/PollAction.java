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
			switch (param.action) {
				case Param.LOAD:
					Poll poll = connection.getPoll(param.id);
					return new Result(Result.LOAD, poll, null);

				case Param.VOTE:
					poll = connection.votePoll(param.id, param.selection);
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

		final int action;
		final long id;
		final int[] selection;

		/**
		 * @param action    action to performa on a poll {@link #LOAD,#VOTE}
		 * @param id        ID of the poll
		 * @param selection selected option
		 */
		public Param(int action, long id, int[] selection) {
			this.action = action;
			this.id = id;
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

		public final int action;
		@Nullable
		public final Poll poll;
		@Nullable
		public final ConnectionException exception;

		/**
		 * @param action action performed on the poll {@link #LOAD,#VOTE}
		 * @param poll   updated poll or null if an error occured
		 */
		Result(int action, @Nullable Poll poll, @Nullable ConnectionException exception) {
			this.action = action;
			this.poll = poll;
			this.exception = exception;
		}
	}
}