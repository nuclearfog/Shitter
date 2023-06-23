package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.lists.Filters;

/**
 * Async loader for (server-side) status filter
 *
 * @author nuclearfog
 */
public class StatusFilterLoader extends AsyncExecutor<Void, StatusFilterLoader.FilterLoaderResult> {

	private Connection connection;

	/**
	 *
	 */
	public StatusFilterLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected FilterLoaderResult doInBackground(@NonNull Void param) {
		try {
			Filters result = connection.getFilter();
			return new FilterLoaderResult(result, null);
		} catch (ConnectionException exception) {
			return new FilterLoaderResult(null, exception);
		} catch (Exception exception) {
			return null;
		}
	}

	/**
	 *
	 */
	public static class FilterLoaderResult {

		@Nullable
		public final Filters filters;
		@Nullable
		public final ConnectionException exception;

		FilterLoaderResult(@Nullable Filters filters, @Nullable ConnectionException exception) {
			this.filters = filters;
			this.exception = exception;
		}
	}
}