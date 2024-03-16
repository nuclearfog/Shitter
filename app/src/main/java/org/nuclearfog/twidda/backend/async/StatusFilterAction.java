package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.update.FilterUpdate;
import org.nuclearfog.twidda.model.Filter;

/**
 * Async class to update filter for posts
 *
 * @author nuclearfog
 */
public class StatusFilterAction extends AsyncExecutor<StatusFilterAction.Param, StatusFilterAction.Result> {

	private Connection connection;

	/**
	 *
	 */
	public StatusFilterAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.action) {
				case Param.UPDATE:
					if (param.update != null) {
						Filter filter = connection.updateFilter(param.update);
						return new Result(Result.UPDATE, param.id, filter, null);
					}
					return null;

				case Param.DELETE:
					connection.deleteFilter(param.id);
					return new Result(Result.DELETE, param.id, null, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, param.id, null, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int UPDATE = 1;
		public static final int DELETE = 2;

		@Nullable final FilterUpdate update;
		final long id;
		final int action;

		/**
		 * @param action action to perform on a filter {@link #UPDATE,#DELETE}
		 * @param id     ID of the filter. used with {@link #DELETE}
		 * @param update filter information to update. used with {@link #UPDATE}
		 */
		public Param(int action, long id, @Nullable FilterUpdate update) {
			this.action = action;
			this.id = id;
			this.update = update;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int UPDATE = 3;
		public static final int DELETE = 4;
		public static final int ERROR = -1;

		@Nullable
		public final Filter filter;
		@Nullable
		public final ConnectionException exception;
		public final long id;
		public final int action;

		/**
		 * @param action applied action {@link #DELETE,#UPDATE} or {@link #ERROR} if an error occured
		 * @param id     updated filter ID
		 * @param filter updated filter or null if deleted
		 */
		Result(int action, long id, @Nullable Filter filter, @Nullable ConnectionException exception) {
			this.action = action;
			this.id = id;
			this.filter = filter;
			this.exception = exception;
		}
	}
}