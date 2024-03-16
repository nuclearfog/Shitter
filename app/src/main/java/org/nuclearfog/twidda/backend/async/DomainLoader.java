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

		final long cursor;
		final int index;

		/**
		 * @param cursor list cursor or '0' to start at the beginning of the domain list
		 * @param index  index where to insert new items in the list/adapter
		 */
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

		/**
		 * @param domains   list of domains or null if an error occured
		 * @param index     index where to insert new items in the list/adapter
		 */
		Result(@Nullable Domains domains, int index, @Nullable ConnectionException exception) {
			this.domains = domains;
			this.index = index;
			this.exception = exception;
		}
	}
}