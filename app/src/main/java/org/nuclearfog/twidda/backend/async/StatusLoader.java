package org.nuclearfog.twidda.backend.async;

import static org.nuclearfog.twidda.ui.fragments.StatusFragment.CLEAR_LIST;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.utils.AsyncExecutor;
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

	/**
	 * home timeline
	 */
	public static final int HOME = 2;

	/**
	 * user timeline
	 */
	public static final int USER = 3;

	/**
	 * favorite timeline
	 */
	public static final int FAVORIT = 4;

	/**
	 * reply timeline
	 */
	public static final int REPLIES = 5;

	/**
	 * reply timeline (offline database)
	 */
	public static final int REPLIES_OFFLINE = 6;

	/**
	 * search timeline
	 */
	public static final int SEARCH = 7;

	/**
	 * userlist timeline
	 */
	public static final int USERLIST = 8;

	/**
	 * public timeline
	 */
	public static final int PUBLIC = 9;

	/**
	 * bookmark timeline
	 */
	public static final int BOOKMARKS = 10;

	private Connection connection;
	private AppDatabase db;


	public StatusLoader(Context context) {
		db = new AppDatabase(context);
		connection = ConnectionManager.get(context);
	}


	@NonNull
	@Override
	protected StatusResult doInBackground(StatusParameter request) {
		List<Status> statuses = null;
		int position = request.pos;
		try {
			switch (request.type) {
				case HOME:
					if (request.minId == 0L && request.maxId == 0L) {
						statuses = db.getHomeTimeline();
						if (statuses.isEmpty()) {
							statuses = connection.getHomeTimeline(request.minId, request.maxId);
							db.saveHomeTimeline(statuses);
						}
					} else if (request.minId > 0L) {
						statuses = connection.getHomeTimeline(request.minId, request.maxId);
						db.saveHomeTimeline(statuses);
					} else if (request.maxId > 1L) {
						statuses = connection.getHomeTimeline(request.minId, request.maxId);
					}
					break;

				case USER:
					if (request.minId == 0L && request.maxId == 0L) {
						statuses = db.getUserTimeline(request.id);
						if (statuses.isEmpty()) {
							statuses = connection.getUserTimeline(request.id, 0L, request.maxId);
							db.saveUserTimeline(statuses);
						}
					} else if (request.minId > 0L) {
						statuses = connection.getUserTimeline(request.id, request.minId, request.maxId);
						db.saveUserTimeline(statuses);
					} else if (request.maxId > 1L) {
						statuses = connection.getUserTimeline(request.id, request.minId, request.maxId);
					}
					break;

				case FAVORIT:
					if (request.minId == 0L && request.maxId == 0L) {
						statuses = db.getUserFavorites(request.id);
						if (statuses.isEmpty()) {
							statuses = connection.getUserFavorits(request.id, 0L, request.maxId);
							db.saveFavoriteTimeline(statuses, request.id);
						}
					} else if (request.minId > 0L) {
						statuses = connection.getUserFavorits(request.id, 0L, request.maxId);
						db.saveFavoriteTimeline(statuses, request.id);
						position = CLEAR_LIST; // set flag to clear previous data
					} else if (request.maxId > 1L) {
						statuses = connection.getUserFavorits(request.id, request.minId, request.maxId);
					}
					break;

				case BOOKMARKS:
					if (request.id > 0L) {
						if (request.minId == 0L && request.maxId == 0L) {
							statuses = db.getUserBookmarks(request.id);
							if (statuses.isEmpty()) {
								statuses = connection.getUserBookmarks(0L, request.maxId);
								db.saveBookmarkTimeline(statuses, request.id);
							}
						} else if (request.minId > 0L) {
							statuses = connection.getUserBookmarks(request.minId, request.maxId);
							db.saveBookmarkTimeline(statuses, request.id);
						} else if (request.maxId > 1L) {
							statuses = connection.getUserBookmarks(request.minId, request.maxId);
						}
					}
					break;

				case REPLIES_OFFLINE:
					statuses = db.getReplies(request.id);
					break;

				case REPLIES:
					if (request.minId == 0L && request.maxId == 0L) {
						statuses = db.getReplies(request.id);
						if (statuses.isEmpty()) {
							statuses = connection.getStatusReplies(request.id, request.minId, request.maxId, request.search);
							if (!statuses.isEmpty() && db.containsStatus(request.id)) {
								db.saveReplyTimeline(statuses);
							}
						}
					} else if (request.minId > 0L) {
						statuses = connection.getStatusReplies(request.id, request.minId, request.maxId, request.search);
						if (!statuses.isEmpty() && db.containsStatus(request.id)) {
							db.saveReplyTimeline(statuses);
						}
					} else if (request.maxId > 1L) {
						statuses = connection.getStatusReplies(request.id, request.minId, request.maxId, request.search);
					}
					break;

				case SEARCH:
					statuses = connection.searchStatuses(request.search, request.minId, request.maxId);
					break;

				case USERLIST:
					statuses = connection.getUserlistStatuses(request.id, request.minId, request.maxId);
					break;

				case PUBLIC:
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


	public static class StatusParameter {
		public final String search;
		public final int type, pos;
		public final long id, minId, maxId;

		public StatusParameter(int type, long id, long minId, long maxId, int pos, String search) {
			this.type = type;
			this.id = id;
			this.minId = minId;
			this.maxId = maxId;
			this.pos = pos;
			this.search = search;
		}
	}


	public static class StatusResult {
		public final List<Status> statuses;
		public final int position;
		public final ConnectionException exception;

		public StatusResult(List<Status> statuses, int position, ConnectionException exception) {
			this.statuses = statuses;
			this.position = position;
			this.exception = exception;
		}
	}
}