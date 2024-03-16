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
			switch (param.type) {
				case Param.POPULAR_OFFLINE:
					Tags tags = db.getTrends();
					if (!tags.isEmpty()) {
						return new Result(tags, param.index, null);
					}
					// fall through

				case Param.POPULAR_ONLINE:
					tags = connection.getTags();
					db.saveTrends(tags);
					return new Result(tags, param.index, null);

				case Param.SEARCH:
					tags = connection.searchTags(param.trend);
					return new Result(tags, param.index, null);

				case Param.FOLLOWING:
					tags = connection.showTagFollowing(param.cursor);
					return new Result(tags, param.index, null);

				case Param.FEATURING:
					tags = connection.showTagFeaturing();
					return new Result(tags, param.index, null);

				case Param.SUGGESTIONS:
					tags = connection.showTagSuggestions();
					return new Result(tags, param.index, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(null, param.index, exception);
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
		final int type;
		final int index;
		final long cursor;

		/**
		 * @param type   type of trend/tag list {@link #POPULAR_OFFLINE,#POPULAR_ONLINE,#SEARCH,#FOLLOWING,#FEATURING,#SUGGESTIONS}
		 * @param index  index where to isnert the new items in the list/adapter
		 * @param trend  trend list
		 * @param cursor cursor used to parse the results
		 */
		public Param(int type, int index, String trend, long cursor) {
			this.type = type;
			this.trend = trend;
			this.index = index;
			this.cursor = cursor;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public final int index;
		@Nullable
		public final Tags tags;
		@Nullable
		public final ConnectionException exception;

		/**
		 * @param tags      tag/trend items
		 * @param index     index where to isnert the new items in the list/adapter
		 * @param exception not null if an error occured
		 */
		Result(@Nullable Tags tags, int index, @Nullable ConnectionException exception) {
			this.tags = tags;
			this.exception = exception;
			this.index = index;
		}
	}
}