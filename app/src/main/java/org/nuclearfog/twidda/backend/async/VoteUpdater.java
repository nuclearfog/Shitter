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
public class VoteUpdater extends AsyncExecutor<VoteUpdater.VoteParam, VoteUpdater.VoteResult> {

	private Connection connection;

	/**
	 *
	 */
	public VoteUpdater(Context context) {
		connection = ConnectionManager.getConnection(context);
	}


	@NonNull
	@Override
	protected VoteResult doInBackground(@NonNull VoteParam param) {
		try {
			switch (param.mode) {
				case VoteParam.LOAD:
					Poll poll = connection.getPoll(param.poll.getId());
					return new VoteResult(VoteResult.LOAD, poll, null);

				case VoteParam.VOTE:
					poll = connection.votePoll(param.poll, param.selection);
					return new VoteResult(VoteResult.VOTE, poll, null);
			}
		} catch (ConnectionException exception) {
			return new VoteResult(VoteResult.ERROR, null, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new VoteResult(VoteResult.ERROR, null, null);
	}

	/**
	 *
	 */
	public static class VoteParam {

		public static final int LOAD = 1;
		public static final int VOTE = 2;

		public final int mode;
		public final Poll poll;
		public final int[] selection;

		public VoteParam(int mode, Poll poll, int[] selection) {
			this.mode = mode;
			this.poll = poll;
			this.selection = Arrays.copyOf(selection, selection.length);
		}
	}

	/**
	 *
	 */
	public static class VoteResult {

		public static final int ERROR = -1;
		public static final int LOAD = 3;
		public static final int VOTE = 4;

		public final int mode;
		@Nullable
		public final Poll poll;
		@Nullable
		public final ConnectionException exception;

		VoteResult(int mode, @Nullable Poll poll, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.poll = poll;
			this.exception = exception;
		}
	}
}