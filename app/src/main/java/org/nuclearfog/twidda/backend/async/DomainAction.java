package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;

/**
 * background executor to block/unblock domains
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
				case Param.MODE_BLOCK:
					connection.blockDomain(param.domain);
					return new Result(Result.MODE_BLOCK, param.domain, null);

				case Param.MODE_UNBLOCK:
					connection.unblockDomain(param.domain);
					return new Result(Result.MODE_UNBLOCK, param.domain, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, param.domain, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int MODE_BLOCK = 2;
		public static final int MODE_UNBLOCK = 3;

		final String domain;
		final int mode;

		public Param(int mode, String domain) {
			this.mode = mode;
			this.domain = domain;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int MODE_BLOCK = 5;
		public static final int MODE_UNBLOCK = 6;

		public final int mode;
		@Nullable
		public final ConnectionException exception;
		@Nullable
		public final String domain;

		Result(int mode, @Nullable String domain, @Nullable ConnectionException exception) {
			this.domain = domain;
			this.exception = exception;
			this.mode = mode;
		}
	}
}