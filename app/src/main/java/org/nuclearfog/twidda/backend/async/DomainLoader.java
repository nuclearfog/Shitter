package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.lists.Domains;

/**
 * Async loader class to load blocked domains
 *
 * @author nuclearfog
 */
public class DomainLoader extends AsyncExecutor<DomainLoader.Param, DomainLoader.Result> {

	private Connection connection;

	/**
	 *
	 */
	public DomainLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			Domains domains = connection.getDomainBlocks(param.cursor);
			return new Result(domains, param.index, null);
		} catch (ConnectionException exception) {
			return new Result(null, param.index, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final long NO_CURSOR = 0L;

		final long cursor;
		final int index;

		public Param(long cursor, int index) {
			this.cursor = cursor;
			this.index = index;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public final int index;
		@Nullable
		public final Domains domains;
		@Nullable
		public final ConnectionException exception;

		Result(@Nullable Domains domains, int index, @Nullable ConnectionException exception) {
			this.domains = domains;
			this.index = index;
			this.exception = exception;
		}
	}
}