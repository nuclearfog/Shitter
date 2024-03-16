package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.lists.StatusEditHistory;

/**
 * Async loader for {@link org.nuclearfog.twidda.ui.fragments.EditHistoryFragment}
 *
 * @author nuclearfog
 */
public class EditHistoryLoader extends AsyncExecutor<Long, EditHistoryLoader.Result> {

	private Connection connection;

	/**
	 *
	 */
	public EditHistoryLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Long param) {
		try {
			StatusEditHistory history = connection.getStatusEditHistory(param);
			return new Result(history, null);
		} catch (ConnectionException exception) {
			return new Result(null, exception);
		}
	}

	/**
	 *
	 */
	public static class Result {

		@Nullable
		public final StatusEditHistory history;
		@Nullable
		public final ConnectionException exception;

		/**
		 * @param history a list of previous versions of a status or null if an error occured
		 */
		Result(@Nullable StatusEditHistory history, @Nullable ConnectionException exception) {
			this.history = history;
			this.exception = exception;
		}
	}
}