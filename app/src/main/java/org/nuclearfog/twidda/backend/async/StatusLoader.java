package org.nuclearfog.twidda.backend.async;

import static org.nuclearfog.twidda.ui.fragments.StatusFragment.CLEAR_LIST;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;

import java.util.List;

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
	protected StatusResult doInBackground(@NonNull StatusParameter request) {
		List<Status> statuses = null;
		int position = request.pos;
		try {
			switch (request.type) {
				case StatusParameter.HOME:
					if (request.minId == 0L && request.maxId == 0L) {
						statuses = db.getHomeTimeline();
						if (statuses.isEmpty()) {
							statuses = connection.getHomeTimeline(0L, 0L);
							db.saveHomeTimeline(statuses);
						}
					} else {
						statuses = connection.getHomeTimeline(request.minId, request.maxId);
						if (request.maxId == 0L) {
							db.saveHomeTimeline(statuses);
						}
					}
					break;

				case StatusParameter.USER:
					if (request.minId == 0L && request.maxId == 0L) {
						statuses = db.getUserTimeline(request.id);
						if (statuses.isEmpty()) {
							statuses = connection.getUserTimeline(request.id, 0L, 0L);
							db.saveUserTimeline(statuses);
						}
					} else {
						statuses = connection.getUserTimeline(request.id, request.minId, request.maxId);
						if (request.maxId == 0L) {
							db.saveUserTimeline(statuses);
						}
					}
					break;

				case StatusParameter.FAVORIT:
					if (request.minId == 0L && request.maxId == 0L) {
						statuses = db.getUserFavorites(request.id);
						if (statuses.isEmpty()) {
							statuses = connection.getUserFavorits(request.id, 0L, 0L);
							db.saveFavoriteTimeline(statuses, request.id);
						}
					} else {
						statuses = connection.getUserFavorits(request.id, 0L, request.maxId);
						if (request.maxId == 0L) {
							db.saveFavoriteTimeline(statuses, request.id);
							position = CLEAR_LIST; // clear previous items
						}
					}
					break;

				case StatusParameter.BOOKMARKS:
					if (request.minId == 0L && request.maxId == 0L) {
						statuses = db.getUserBookmarks(request.id);
						if (statuses.isEmpty()) {
							statuses = connection.getUserBookmarks(0L, 0L);
							db.saveBookmarkTimeline(statuses, request.id);
						}
					} else {
						statuses = connection.getUserBookmarks(0L, request.maxId);
						if (request.maxId == 0L) {
							db.saveBookmarkTimeline(statuses, request.id);
							position = CLEAR_LIST; // clear previous items
						}
					}
					break;

				case StatusParameter.REPLIES_LOCAL:
					statuses = db.getReplies(request.id);
					break;

				case StatusParameter.REPLIES:
					if (request.minId == 0L && request.maxId == 0L) {
						statuses = db.getReplies(request.id);
						if (statuses.isEmpty()) {
							statuses = connection.getStatusReplies(request.id, 0L, 0L, request.search);
							if (db.containsStatus(request.id)) {
								db.saveReplyTimeline(statuses);
							}
						}
					} else {
						statuses = connection.getStatusReplies(request.id, request.minId, request.maxId, request.search);
						if (request.maxId == 0L && db.containsStatus(request.id)) {
							db.saveReplyTimeline(statuses);
						}
					}
					break;

				case StatusParameter.SEARCH:
					statuses = connection.searchStatuses(request.search, request.minId, request.maxId);
					break;

				case StatusParameter.USERLIST:
					statuses = connection.getUserlistStatuses(request.id, request.minId, request.maxId);
					break;

				case StatusParameter.PUBLIC:
					statuses = connection.getPublicTimeline(request.minId, request.maxId);
					break;
			}
		} catch (ConnectionException exception) {
			return new StatusResult(null, position, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new StatusResult(statuses, position, null);
	}

	/**
	 *
	 */
	public static class StatusParameter {

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

		public final int position;
		@Nullable
		public final List<Status> statuses;
		@Nullable
		public final ConnectionException exception;

		StatusResult(@Nullable List<Status> statuses, int position, @Nullable ConnectionException exception) {
			this.statuses = statuses;
			this.position = position;
			this.exception = exception;
		}
	}
}