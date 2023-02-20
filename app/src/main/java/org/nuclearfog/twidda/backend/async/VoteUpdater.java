package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.utils.AsyncExecutor;
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
		connection = ConnectionManager.get(context);
	}


	@NonNull
	@Override
	protected VoteResult doInBackground(VoteParam param) {
		try {
			Poll poll = connection.vote(param.poll, param.selection);
			return new VoteResult(poll, null);
		} catch (ConnectionException exception) {
			return new VoteResult(null, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new VoteResult(null, null);
	}

	/**
	 *
	 */
	public static class VoteParam {

		public final Poll poll;
		public final int[] selection;

		public VoteParam(Poll poll, int[] selection) {
			this.poll = poll;
			this.selection = Arrays.copyOf(selection, selection.length);
		}
	}

	/**
	 *
	 */
	public static class VoteResult {

		@Nullable
		public final Poll poll;
		@Nullable
		public final ConnectionException exception;

		VoteResult(@Nullable Poll poll, @Nullable ConnectionException exception) {
			this.poll = poll;
			this.exception = exception;
		}
	}
}