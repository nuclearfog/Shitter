package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.utils.AsyncExecutor;
import org.nuclearfog.twidda.database.AppDatabase;
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

	/**
	 * refresh exclude list
	 */
	public static final int MODE_RELOAD = 1;

	/**
	 * mute specified user
	 */
	public static final int MODE_MUTE = 2;

	/**
	 * block specified user
	 */
	public static final int MODE_BLOCK = 3;

	/**
	 * error occured
	 */
	public static final int MODE_ERROR = -1;


	private Connection connection;
	private AppDatabase db;

	public FilterLoader(Context context) {
		connection = ConnectionManager.get(context);
		db = new AppDatabase(context);
	}


	@NonNull
	@Override
	protected FilterResult doInBackground(FilterParam param) {
		try {
			switch (param.mode) {
				case MODE_RELOAD:
					List<Long> ids = connection.getIdBlocklist();
					db.setFilterlistUserIds(ids);
					break;

				case MODE_MUTE:
					connection.muteUser(param.name);
					break;

				case MODE_BLOCK:
					connection.blockUser(param.name);
					break;
			}
			return new FilterResult(param.mode, null);
		} catch (ConnectionException exception) {
			return new FilterResult(MODE_ERROR, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new FilterResult(MODE_ERROR, null);
	}


	public static class FilterParam {

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


	public static class FilterResult {

		public final int mode;
		@Nullable
		public final ConnectionException exception;

		FilterResult(int mode, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.exception = exception;
		}
	}
}