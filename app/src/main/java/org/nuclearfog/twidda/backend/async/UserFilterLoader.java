package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.ui.activities.UsersActivity;

import java.util.List;

/**
 * Backend of {@link UsersActivity}
 * performs user mute or block actions and stores a list of IDs with blocked/muted users
 * This list is used to filter search results
 *
 * @author nuclearfog
 */
public class UserFilterLoader extends AsyncExecutor<UserFilterLoader.FilterParam, UserFilterLoader.FilterResult> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public UserFilterLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		db = new AppDatabase(context);
	}


	@Override
	protected FilterResult doInBackground(@NonNull FilterParam param) {
		try {
			switch (param.mode) {
				case FilterParam.RELOAD:
					List<Long> ids = connection.getIdBlocklist();
					db.saveFilterlist(ids);
					return new FilterResult(FilterResult.RELOAD, null);

				case FilterParam.MUTE_USER:
					Relation relation = connection.muteUser(param.name);
					db.muteUser(relation.getId(), true);
					return new FilterResult(FilterResult.MUTE_USER, null);

				case FilterParam.BLOCK_USER:
					relation = connection.blockUser(param.name);
					db.muteUser(relation.getId(), true);
					return new FilterResult(FilterResult.BLOCK_USER, null);

				case FilterParam.BLOCK_DOMAIN:
					connection.blockDomain(param.name);
					return new FilterResult(FilterResult.BLOCK_DOMAIN, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new FilterResult(FilterResult.ERROR, exception);
		}
	}

	/**
	 *
	 */
	public static class FilterParam {

		public static final int RELOAD = 1;
		public static final int MUTE_USER = 2;
		public static final int BLOCK_USER = 3;
		public static final int BLOCK_DOMAIN = 4;

		final String name;
		final int mode;

		public FilterParam(int mode) {
			this.mode = mode;
			name = "";
		}

		public FilterParam(int mode, String name) {
			this.mode = mode;
			this.name = name;
		}
	}

	/**
	 *
	 */
	public static class FilterResult {

		public static final int ERROR = -1;
		public static final int RELOAD = 5;
		public static final int MUTE_USER = 6;
		public static final int BLOCK_USER = 7;
		public static final int BLOCK_DOMAIN = 8;

		public final int mode;
		@Nullable
		public final ConnectionException exception;

		FilterResult(int mode, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.exception = exception;
		}
	}
}