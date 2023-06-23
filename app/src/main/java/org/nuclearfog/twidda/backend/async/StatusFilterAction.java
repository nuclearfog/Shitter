package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.update.FilterUpdate;

/**
 * Async class to update filter for posts
 *
 * @author nuclearfog
 */
public class StatusFilterAction extends AsyncExecutor<StatusFilterAction.FilterActionParam, StatusFilterAction.FilterActionResult> {

	private Connection connection;

	/**
	 *
	 */
	public StatusFilterAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected FilterActionResult doInBackground(@NonNull FilterActionParam param) {
		try {
			switch (param.mode) {
				case FilterActionParam.UPDATE:
					if (param.update != null) {
						connection.updateFilter(param.update);
						return new FilterActionResult(FilterActionResult.UPDATE, param.id, null);
					}
					break;

				case FilterActionParam.DELETE:
					connection.deleteFilter(param.id);
					return new FilterActionResult(FilterActionResult.DELETE, param.id, null);
			}
		} catch (ConnectionException exception) {
			return new FilterActionResult(FilterActionResult.ERROR, param.id, exception);
		} catch (Exception exception) {
			if (BuildConfig.DEBUG) {
				exception.printStackTrace();
			}
		}
		return null;
	}

	/**
	 *
	 */
	public static class FilterActionParam {

		public static final int UPDATE = 1;
		public static final int DELETE = 2;

		@Nullable
		final FilterUpdate update;
		final long id;
		final int mode;

		public FilterActionParam(int mode, long id, @Nullable FilterUpdate update) {
			this.mode = mode;
			this.id = id;
			this.update = update;
		}
	}

	/**
	 *
	 */
	public static class FilterActionResult {

		public static final int UPDATE = 3;
		public static final int DELETE = 4;
		public static final int ERROR = -1;

		@Nullable
		public final ConnectionException exception;
		public final long id;
		public final int mode;

		public FilterActionResult(int mode, long id, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.id = id;
			this.exception = exception;
		}
	}
}