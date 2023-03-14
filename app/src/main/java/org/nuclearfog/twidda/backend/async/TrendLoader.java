package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.ui.fragments.TrendFragment;

import java.util.List;

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


	@NonNull
	@Override
	protected TrendResult doInBackground(@NonNull TrendParameter param) {
		try {
			switch (param.mode) {
				case DATABASE:
					List<Trend> trends = db.getTrends();
					if (!trends.isEmpty()) {
						return new TrendResult(trends, null);
					}
					// fall through

				case ONLINE:
					trends = connection.getTrends();
					db.saveTrends(trends);
					return new TrendResult(trends, null);

				case SEARCH:
					trends = connection.searchHashtags(param.trend);
					return new TrendResult(trends, null);
			}
		} catch (ConnectionException exception) {
			return new TrendResult(null, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new TrendResult(null, null);
	}

	/**
	 *
	 */
	public static class TrendParameter {

		final String trend;
		final int mode;

		public TrendParameter(int mode, String trend) {
			this.mode = mode;
			this.trend = trend;
		}
	}

	/**
	 *
	 */
	public static class TrendResult {

		@Nullable
		public final List<Trend> trends;
		@Nullable
		public final ConnectionException exception;

		TrendResult(@Nullable List<Trend> trends, @Nullable ConnectionException exception) {
			this.trends = trends;
			this.exception = exception;
		}
	}
}