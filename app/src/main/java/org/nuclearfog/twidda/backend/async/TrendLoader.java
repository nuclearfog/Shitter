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
				case TrendParameter.POPULAR_OFFLINE:
					Trends trends = db.getTrends();
					if (!trends.isEmpty()) {
						return new TrendResult(TrendResult.POPULAR, trends, param.index, null);
					}
					// fall through

				case TrendParameter.POPULAR_ONLINE:
					trends = connection.getTrends();
					db.saveTrends(trends);
					return new TrendResult(TrendResult.POPULAR, trends, param.index, null);

				case TrendParameter.SEARCH:
					trends = connection.searchHashtags(param.trend);
					return new TrendResult(TrendResult.SEARCH, trends, param.index, null);

				case TrendParameter.FOLLOWING:
					trends = connection.showHashtagFollowing(param.cursor);
					return new TrendResult(TrendResult.FOLLOWING, trends, param.index, null);
			}
		} catch (ConnectionException exception) {
			return new TrendResult(TrendResult.ERROR, null, param.index, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new TrendResult(TrendResult.ERROR, null, param.index, null);
	}

	/**
	 *
	 */
	public static class TrendParameter {

		public static final int POPULAR_OFFLINE = 1;
		public static final int POPULAR_ONLINE = 2;
		public static final int SEARCH = 3;
		public static final int FOLLOWING = 4;

		public static final long NO_CURSOR = 0L;

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

		public static final int ERROR = -1;
		public static final int POPULAR = 4;
		public static final int SEARCH = 5;
		public static final int FOLLOWING = 6;

		public final int mode;
		public final int index;
		@Nullable
		public final Trends trends;
		@Nullable
		public final ConnectionException exception;

		TrendResult(int mode, @Nullable Trends trends, int index, @Nullable ConnectionException exception) {
			this.trends = trends;
			this.exception = exception;
			this.index = index;
			this.mode = mode;
		}
	}
}