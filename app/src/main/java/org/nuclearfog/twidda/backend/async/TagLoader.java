package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.lists.Tags;
import org.nuclearfog.twidda.ui.fragments.TagFragment;

/**
 * Background task to load a list of location specific trends
 *
 * @author nuclearfog
 * @see TagFragment
 */
public class TagLoader extends AsyncExecutor<TagLoader.Param, TagLoader.Result> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public TagLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		db = new AppDatabase(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.mode) {
				case Param.POPULAR_OFFLINE:
					Tags tags = db.getTrends();
					if (!tags.isEmpty()) {
						return new Result(Result.POPULAR, tags, param.index, null);
					}
					// fall through

				case Param.POPULAR_ONLINE:
					tags = connection.getTags();
					db.saveTrends(tags);
					return new Result(Result.POPULAR, tags, param.index, null);

				case Param.SEARCH:
					tags = connection.searchTags(param.trend);
					return new Result(Result.SEARCH, tags, param.index, null);

				case Param.FOLLOWING:
					tags = connection.showTagFollowing(param.cursor);
					return new Result(Result.FOLLOWING, tags, param.index, null);

				case Param.FEATURING:
					tags = connection.showTagFeaturing();
					return new Result(Result.FEATURING, tags, param.index, null);

				case Param.SUGGESTIONS:
					tags = connection.showTagSuggestions();
					return new Result(Result.SUGGESTIONS, tags, param.index, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, null, param.index, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int POPULAR_OFFLINE = 1;
		public static final int POPULAR_ONLINE = 2;
		public static final int SEARCH = 3;
		public static final int FOLLOWING = 4;
		public static final int FEATURING = 5;
		public static final int SUGGESTIONS = 6;

		public static final long NO_CURSOR = 0L;

		final String trend;
		final int mode;
		final int index;
		final long cursor;

		public Param(int mode, int index, String trend, long cursor) {
			this.mode = mode;
			this.trend = trend;
			this.index = index;
			this.cursor = cursor;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int POPULAR = 20;
		public static final int SEARCH = 21;
		public static final int FOLLOWING = 22;
		public static final int FEATURING = 23;
		public static final int SUGGESTIONS = 24;

		public final int mode;
		public final int index;
		@Nullable
		public final Tags tags;
		@Nullable
		public final ConnectionException exception;

		Result(int mode, @Nullable Tags tags, int index, @Nullable ConnectionException exception) {
			this.tags = tags;
			this.exception = exception;
			this.index = index;
			this.mode = mode;
		}
	}
}