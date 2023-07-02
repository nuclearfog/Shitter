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
public class PollAction extends AsyncExecutor<PollAction.PollActionParam, PollAction.PollActionResult> {

	private Connection connection;

	/**
	 *
	 */
	public PollAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected PollActionResult doInBackground(@NonNull PollActionParam param) {
		try {
			switch (param.mode) {
				case PollActionParam.LOAD:
					Poll poll = connection.getPoll(param.poll.getId());
					return new PollActionResult(PollActionResult.LOAD, poll, null);

				case PollActionParam.VOTE:
					poll = connection.votePoll(param.poll, param.selection);
					return new PollActionResult(PollActionResult.VOTE, poll, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new PollActionResult(PollActionResult.ERROR, null, exception);
		}
	}

	/**
	 *
	 */
	public static class PollActionParam {

		public static final int LOAD = 1;
		public static final int VOTE = 2;

		final int mode;
		final Poll poll;
		final int[] selection;

		public PollActionParam(int mode, Poll poll, int[] selection) {
			this.mode = mode;
			this.poll = poll;
			this.selection = Arrays.copyOf(selection, selection.length);
		}
	}

	/**
	 *
	 */
	public static class PollActionResult {

		public static final int ERROR = -1;
		public static final int LOAD = 3;
		public static final int VOTE = 4;

		public final int mode;
		@Nullable
		public final Poll poll;
		@Nullable
		public final ConnectionException exception;

		PollActionResult(int mode, @Nullable Poll poll, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.poll = poll;
			this.exception = exception;
		}
	}
}