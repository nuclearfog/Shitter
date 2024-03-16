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
			switch (param.action) {
				case Param.BLOCK:
					connection.blockDomain(param.domain);
					return new Result(Result.BLOCK, param.domain, null);

				case Param.UNBLOCK:
					connection.unblockDomain(param.domain);
					return new Result(Result.UNBLOCK, param.domain, null);

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

		public static final int BLOCK = 2;
		public static final int UNBLOCK = 3;

		final String domain;
		final int action;

		/**
		 * @param action action performed on instance domain {@link #BLOCK,#UNBLOCK}
		 * @param domain instance domain name
		 */
		public Param(int action, String domain) {
			this.action = action;
			this.domain = domain;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int BLOCK = 5;
		public static final int UNBLOCK = 6;

		public final int action;
		@Nullable
		public final ConnectionException exception;
		@Nullable
		public final String domain;

		/**
		 * @param action    action performed on a domain instance {@link #BLOCK,#UNBLOCK}
		 * @param domain    instance domain name
		 */
		Result(int action, @Nullable String domain, @Nullable ConnectionException exception) {
			this.domain = domain;
			this.exception = exception;
			this.action = action;
		}
	}
}