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
public class FilterLoader extends AsyncExecutor<FilterLoader.FilterParam, FilterLoader.FilterResult> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public FilterLoader(Context context) {
		connection = ConnectionManager.getConnection(context);
		db = new AppDatabase(context);
	}


	@NonNull
	@Override
	protected FilterResult doInBackground(@NonNull FilterParam param) {
		try {
			switch (param.mode) {
				case FilterParam.RELOAD:
					List<Long> ids = connection.getIdBlocklist();
					db.saveFilterlist(ids);
					return new FilterResult(FilterResult.RELOAD, null);

				case FilterParam.MUTE:
					Relation relation = connection.muteUser(param.name);
					db.muteUser(relation.getId(), true);
					return new FilterResult(FilterResult.MUTE, null);

				case FilterParam.BLOCK:
					relation = connection.blockUser(param.name);
					db.muteUser(relation.getId(), true);
					return new FilterResult(FilterResult.BLOCK, null);
			}
		} catch (ConnectionException exception) {
			return new FilterResult(FilterResult.ERROR, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new FilterResult(FilterResult.ERROR, null);
	}

	/**
	 *
	 */
	public static class FilterParam {

		public static final int RELOAD = 1;
		public static final int MUTE = 2;
		public static final int BLOCK = 3;

		public final String name;
		public final int mode;

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
		public static final int RELOAD = 4;
		public static final int MUTE = 5;
		public static final int BLOCK = 6;

		public final int mode;
		@Nullable
		public final ConnectionException exception;

		FilterResult(int mode, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.exception = exception;
		}
	}
}