package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.lists.Statuses;

/**
 * Background task to download a list of statuses from different sources
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.fragments.StatusFragment
 */
public class StatusLoader extends AsyncExecutor<StatusLoader.Param, StatusLoader.Result> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public StatusLoader(Context context) {
		db = new AppDatabase(context);
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			Statuses statuses;
			switch (param.type) {
				case Param.HOME:
					if (param.minId == 0L && param.maxId == 0L) {
						statuses = db.getHomeTimeline();
						if (statuses.isEmpty()) {
							statuses = connection.getHomeTimeline(0L, 0L);
							db.saveHomeTimeline(statuses);
						}
					} else {
						statuses = connection.getHomeTimeline(param.minId, param.maxId);
						if (param.maxId == 0L) {
							db.saveHomeTimeline(statuses);
						}
					}
					return new Result(statuses, param.pos, null);

				case Param.USER:
					if (param.minId == 0L && param.maxId == 0L) {
						statuses = db.getUserTimeline(param.id);
						if (statuses.isEmpty()) {
							statuses = connection.getUserTimeline(param.id, 0L, 0L, false);
							db.saveUserTimeline(statuses);
						}
					} else {
						statuses = connection.getUserTimeline(param.id, param.minId, param.maxId, false);
						if (param.maxId == 0L) {
							db.saveUserTimeline(statuses);
						}
					}
					return new Result(statuses, param.pos, null);

				case Param.USER_REPLIES:
					if (param.minId == 0L && param.maxId == 0L) {
						statuses = db.getUserReplies(param.id);
						if (statuses.isEmpty()) {
							statuses = connection.getUserTimeline(param.id, 0L, 0L, true);
							db.saveUserReplies(statuses);
						}
					} else {
						statuses = connection.getUserTimeline(param.id, param.minId, param.maxId, true);
						if (param.maxId == 0L) {
							db.saveUserReplies(statuses);
						}
					}
					return new Result(statuses, param.pos, null);

				case Param.FAVORIT:
					if (param.minId == 0L && param.maxId == 0L) {
						statuses = db.getUserFavorites(param.id);
						if (statuses.isEmpty()) {
							statuses = connection.getUserFavorits(param.id, 0L, 0L);
							db.saveFavoriteTimeline(statuses, param.id);
						}
					} else {
						statuses = connection.getUserFavorits(param.id, 0L, param.maxId);
						if (param.maxId == 0L) {
							db.saveFavoriteTimeline(statuses, param.id);
							return new Result(statuses, Result.CLEAR, null);
						}
					}
					return new Result(statuses, param.pos, null);

				case Param.BOOKMARKS:
					if (param.minId == 0L && param.maxId == 0L) {
						statuses = db.getUserBookmarks(param.id);
						if (statuses.isEmpty()) {
							statuses = connection.getUserBookmarks(0L, 0L);
							db.saveBookmarkTimeline(statuses, param.id);
						}
					} else {
						statuses = connection.getUserBookmarks(0L, param.maxId);
						if (param.maxId == 0L) {
							db.saveBookmarkTimeline(statuses, param.id);
							return new Result(statuses, Result.CLEAR, null);
						}
					}
					return new Result(statuses, param.pos, null);

				case Param.REPLIES:
					if (param.minId == 0L && param.maxId == 0L) {
						statuses = db.getReplies(param.id);
						if (statuses.isEmpty()) {
							statuses = connection.getStatusReplies(param.id, 0L, 0L);
							if (db.containsStatus(param.id)) {
								db.saveReplies(param.id, statuses);
							}
						}
					} else {
						statuses = connection.getStatusReplies(param.id, param.minId, param.maxId);
						if (param.maxId == 0L && db.containsStatus(param.id)) {
							db.saveReplies(param.id, statuses);
						}
					}
					return new Result(statuses, param.pos, null);

				case Param.SEARCH:
					statuses = connection.searchStatuses(param.search, param.minId, param.maxId);
					return new Result(statuses, param.pos, null);

				case Param.USERLIST:
					statuses = connection.getUserlistStatuses(param.id, param.minId, param.maxId);
					return new Result(statuses, param.pos, null);

				case Param.PUBLIC:
					statuses = connection.getPublicTimeline(param.minId, param.maxId);
					return new Result(statuses, param.pos, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(null, param.pos, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int HOME = 1;
		public static final int USER = 2;
		public static final int USER_REPLIES = 3;
		public static final int FAVORIT = 4;
		public static final int REPLIES = 5;
		public static final int SEARCH = 7;
		public static final int USERLIST = 8;
		public static final int PUBLIC = 9;
		public static final int BOOKMARKS = 10;

		final String search;
		final int type, pos;
		final long id, minId, maxId;

		/**
		 * @param type   type of timeline to load {@link #HOME,#USER,#USER_REPLIES,#FAVORIT,#REPLIES,#SEARCH,#USERLIST,#PUBLIC,#BOOKMARKS}
		 * @param id     user ID if type is {@link #USER,#USER_REPLIES}, userlist ID if type is {@link #USERLIST}, status ID if type is {@link #REPLIES} or otherwise unused
		 * @param minId  minimum status ID (lower IDs will be excluded)
		 * @param maxId  maximum status ID (higher IDs will be excluded)
		 * @param pos    index where to insert the new statuses in the list/adapter
		 * @param search search string used with {@link #SEARCH}
		 */
		public Param(int type, long id, long minId, long maxId, int pos, String search) {
			this.type = type;
			this.id = id;
			this.minId = minId;
			this.maxId = maxId;
			this.pos = pos;
			this.search = search;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int CLEAR = -1;

		public final int position;
		@Nullable
		public final Statuses statuses;
		@Nullable
		public final ConnectionException exception;

		/**
		 * @param statuses  list of statuses, null if an error occured
		 * @param position  index where to insert the status in the list/adapter
		 * @param exception not null if an error occured
		 */
		Result(@Nullable Statuses statuses, int position, @Nullable ConnectionException exception) {
			this.statuses = statuses;
			this.position = position;
			this.exception = exception;
		}
	}
}