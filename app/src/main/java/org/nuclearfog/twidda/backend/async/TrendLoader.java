package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.lists.Trends;
import org.nuclearfog.twidda.ui.fragments.TrendFragment;

/**
 * Background task to load a list of location specific trends
 *
 * @author nuclearfog
 * @see TrendFragment
 */
public class TrendLoader extends AsyncExecutor<TrendLoader.TrendParameter, TrendLoader.TrendResult> {

	public static final int DATABASE = 1;
	public static final int ONLINE = 2;
	public static final int SEARCH = 3;

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public TrendLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		db = new AppDatabase(context);
	}


	@Override
	protected TrendResult doInBackground(@NonNull TrendParameter param) {
		try {
			switch (param.mode) {
				case DATABASE:
					Trends trends = db.getTrends();
					if (!trends.isEmpty()) {
						return new TrendResult(trends, param.index, null);
					}
					// fall through

				case ONLINE:
					trends = connection.getTrends();
					db.saveTrends(trends);
					return new TrendResult(trends, param.index, null);

				case SEARCH:
					trends = connection.searchHashtags(param.trend);
					return new TrendResult(trends, param.index, null);
			}
		} catch (ConnectionException exception) {
			return new TrendResult(null, param.index, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new TrendResult(null, param.index, null);
	}

	/**
	 *
	 */
	public static class TrendParameter {

		public static final long NO_CURSOR = -1L;

		final String trend;
		final int mode;
		final int index;
		final long cursor;

		public TrendParameter(int mode, int index, String trend, long cursor) {
			this.mode = mode;
			this.trend = trend;
			this.index = index;
			this.cursor = cursor;
		}
	}

	/**
	 *
	 */
	public static class TrendResult {

		public final int index;
		@Nullable
		public final Trends trends;
		@Nullable
		public final ConnectionException exception;

		TrendResult(@Nullable Trends trends, int index, @Nullable ConnectionException exception) {
			this.trends = trends;
			this.exception = exception;
			this.index = index;
		}
	}
}