package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.lists.Statuses;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;


/**
 * Background task to download a list of statuses from different sources
 *
 * @author nuclearfog
 * @see StatusFragment
 */
public class StatusLoader extends AsyncExecutor<StatusLoader.StatusParameter, StatusLoader.StatusResult> {

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
	protected StatusResult doInBackground(@NonNull StatusParameter param) {
		try {
			Statuses statuses;
			switch (param.type) {
				case StatusParameter.HOME:
					if (param.minId == StatusParameter.NO_ID && param.maxId == StatusParameter.NO_ID) {
						statuses = db.getHomeTimeline();
						if (statuses.isEmpty()) {
							statuses = connection.getHomeTimeline(0L, 0L);
							db.saveHomeTimeline(statuses);
						}
					} else {
						statuses = connection.getHomeTimeline(param.minId, param.maxId);
						if (param.maxId == StatusParameter.NO_ID) {
							db.saveHomeTimeline(statuses);
						}
					}
					return new StatusResult(statuses, param.pos, null);

				case StatusParameter.USER:
					if (param.minId == StatusParameter.NO_ID && param.maxId == StatusParameter.NO_ID) {
						statuses = db.getUserTimeline(param.id);
						if (statuses.isEmpty()) {
							statuses = connection.getUserTimeline(param.id, 0L, 0L);
							db.saveUserTimeline(statuses);
						}
					} else {
						statuses = connection.getUserTimeline(param.id, param.minId, param.maxId);
						if (param.maxId == StatusParameter.NO_ID) {
							db.saveUserTimeline(statuses);
						}
					}
					return new StatusResult(statuses, param.pos, null);

				case StatusParameter.FAVORIT:
					if (param.minId == StatusParameter.NO_ID && param.maxId == StatusParameter.NO_ID) {
						statuses = db.getUserFavorites(param.id);
						if (statuses.isEmpty()) {
							statuses = connection.getUserFavorits(param.id, 0L, 0L);
							db.saveFavoriteTimeline(statuses, param.id);
						}
					} else {
						statuses = connection.getUserFavorits(param.id, 0L, param.maxId);
						if (param.maxId == StatusParameter.NO_ID) {
							db.saveFavoriteTimeline(statuses, param.id);
							return new StatusResult(statuses, StatusResult.CLEAR, null);
						}
					}
					return new StatusResult(statuses, param.pos, null);

				case StatusParameter.BOOKMARKS:
					if (param.minId == StatusParameter.NO_ID && param.maxId == StatusParameter.NO_ID) {
						statuses = db.getUserBookmarks(param.id);
						if (statuses.isEmpty()) {
							statuses = connection.getUserBookmarks(0L, 0L);
							db.saveBookmarkTimeline(statuses, param.id);
						}
					} else {
						statuses = connection.getUserBookmarks(0L, param.maxId);
						if (param.maxId == StatusParameter.NO_ID) {
							db.saveBookmarkTimeline(statuses, param.id);
							return new StatusResult(statuses, StatusResult.CLEAR, null);
						}
					}
					return new StatusResult(statuses, param.pos, null);

				case StatusParameter.REPLIES_LOCAL:
					statuses = db.getReplies(param.id);
					return new StatusResult(statuses, param.pos, null);

				case StatusParameter.REPLIES:
					if (param.minId == StatusParameter.NO_ID && param.maxId == StatusParameter.NO_ID) {
						statuses = db.getReplies(param.id);
						if (statuses.isEmpty()) {
							statuses = connection.getStatusReplies(param.id, 0L, 0L, param.search);
							if (db.containsStatus(param.id)) {
								db.saveReplyTimeline(statuses);
							}
						}
					} else {
						statuses = connection.getStatusReplies(param.id, param.minId, param.maxId, param.search);
						if (param.maxId == StatusParameter.NO_ID && db.containsStatus(param.id)) {
							db.saveReplyTimeline(statuses);
						}
					}
					return new StatusResult(statuses, param.pos, null);

				case StatusParameter.SEARCH:
					statuses = connection.searchStatuses(param.search, param.minId, param.maxId);
					return new StatusResult(statuses, param.pos, null);

				case StatusParameter.USERLIST:
					statuses = connection.getUserlistStatuses(param.id, param.minId, param.maxId);
					return new StatusResult(statuses, param.pos, null);

				case StatusParameter.PUBLIC:
					statuses = connection.getPublicTimeline(param.minId, param.maxId);
					return new StatusResult(statuses, param.pos, null);
			}
		} catch (ConnectionException exception) {
			return new StatusResult(null, param.pos, exception);
		} catch (Exception exception) {
			if (BuildConfig.DEBUG) {
				exception.printStackTrace();
			}
		}
		return null;
	}

	/**
	 *
	 */
	public static class StatusParameter {

		public static final long NO_ID = 0L;

		public static final int HOME = 1;
		public static final int USER = 2;
		public static final int FAVORIT = 3;
		public static final int REPLIES = 4;
		public static final int REPLIES_LOCAL = 5;
		public static final int SEARCH = 6;
		public static final int USERLIST = 7;
		public static final int PUBLIC = 8;
		public static final int BOOKMARKS = 9;

		final String search;
		final int type, pos;
		final long id, minId, maxId;

		public StatusParameter(int type, long id, long minId, long maxId, int pos, String search) {
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
	public static class StatusResult {

		public static final int CLEAR = -1;

		public final int position;
		@Nullable
		public final Statuses statuses;
		@Nullable
		public final ConnectionException exception;

		StatusResult(@Nullable Statuses statuses, int position, @Nullable ConnectionException exception) {
			this.statuses = statuses;
			this.position = position;
			this.exception = exception;
		}
	}
}