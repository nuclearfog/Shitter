package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.lists.Domains;

/**
 * background executor to load/block domains
 *
 * @author nuclearfog
 */
public class DomainAction extends AsyncExecutor<DomainAction.Param, DomainAction.Result> {

	private Connection connection;

	/**
	 *
	 */
	public DomainAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.mode) {
				case Param.MODE_LOAD:
					Domains result = connection.getDomainBlocks(param.cursor);
					return new Result(Result.MODE_LOAD, param.index, result, param.domain, null);

				case Param.MODE_BLOCK:
					connection.blockDomain(param.domain);
					return new Result(Result.MODE_BLOCK, param.index, null, param.domain, null);

				case Param.MODE_UNBLOCK:
					connection.unblockDomain(param.domain);
					return new Result(Result.MODE_UNBLOCK, param.index, null, param.domain, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, param.index, null, param.domain, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int MODE_LOAD = 1;
		public static final int MODE_BLOCK = 2;
		public static final int MODE_UNBLOCK = 3;

		public static final long NO_CURSOR = 0L;

		final String domain;
		final long cursor;
		final int mode;
		final int index;

		public Param(int mode, int index, long cursor, String domain) {
			this.mode = mode;
			this.cursor = cursor;
			this.domain = domain;
			this.index = index;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int MODE_LOAD = 4;
		public static final int MODE_BLOCK = 5;
		public static final int MODE_UNBLOCK = 6;

		public final int mode;
		public final int index;
		@Nullable
		public final Domains domains;
		@Nullable
		public final ConnectionException exception;
		@Nullable
		public final String domain;

		Result(int mode, int index, @Nullable Domains domains, @Nullable String domain, @Nullable ConnectionException exception) {
			this.domains = domains;
			this.domain = domain;
			this.exception = exception;
			this.mode = mode;
			this.index = index;
		}
	}
}