package org.nuclearfog.twidda.backend.async;

import static org.nuclearfog.twidda.ui.fragments.StatusFragment.CLEAR_LIST;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Background task to download a list of statuses from different sources
 *
 * @author nuclearfog
 * @see StatusFragment
 */
public class StatusLoader extends AsyncTask<Long, Void, List<Status>> {

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


	private WeakReference<StatusFragment> weakRef;
	private Connection connection;
	private AppDatabase db;

	@Nullable
	private ConnectionException exception;
	private int listType;
	private String search;
	private long id;
	private int pos;

	/**
	 * @param fragment callback
	 * @param listType type of timeline to load
	 * @param id       ID, depending on what list type should be loaded
	 * @param search   search string if any
	 * @param pos      index of the list where new items should be inserted
	 */
	public StatusLoader(StatusFragment fragment, int listType, long id, String search, int pos) {
		super();
		db = new AppDatabase(fragment.getContext());
		connection = ConnectionManager.get(fragment.getContext());
		weakRef = new WeakReference<>(fragment);

		this.listType = listType;
		this.search = search;
		this.id = id;
		this.pos = pos;
	}


	@Override
	protected List<org.nuclearfog.twidda.model.Status> doInBackground(Long... param) {
		List<org.nuclearfog.twidda.model.Status> statuses = null;
		long sinceId = param[0];
		long maxId = param[1];
		try {
			switch (listType) {
				case HOME:
					if (sinceId == 0 && maxId == 0) {
						statuses = db.getHomeTimeline();
						if (statuses.isEmpty()) {
							statuses = connection.getHomeTimeline(sinceId, maxId);
							db.saveHomeTimeline(statuses);
						}
					} else if (sinceId > 0) {
						statuses = connection.getHomeTimeline(sinceId, maxId);
						db.saveHomeTimeline(statuses);
					} else if (maxId > 1) {
						statuses = connection.getHomeTimeline(sinceId, maxId);
					}
					break;

				case USER:
					if (id > 0) {
						if (sinceId == 0 && maxId == 0) {
							statuses = db.getUserTimeline(id);
							if (statuses.isEmpty()) {
								statuses = connection.getUserTimeline(id, 0, maxId);
								db.saveUserTimeline(statuses);
							}
						} else if (sinceId > 0) {
							statuses = connection.getUserTimeline(id, sinceId, maxId);
							db.saveUserTimeline(statuses);
						} else if (maxId > 1) {
							statuses = connection.getUserTimeline(id, sinceId, maxId);
						}
					} else if (search != null) {
						statuses = connection.getUserTimeline(search, sinceId, maxId);
					}
					break;

				case FAVORIT:
					if (id > 0) {
						if (sinceId == 0 && maxId == 0) {
							statuses = db.getUserFavorites(id);
							if (statuses.isEmpty()) {
								statuses = connection.getUserFavorits(id, 0, maxId);
								db.saveFavoriteTimeline(statuses, id);
							}
						} else if (sinceId > 0) {
							statuses = connection.getUserFavorits(id, 0, maxId);
							db.saveFavoriteTimeline(statuses, id);
							pos = CLEAR_LIST; // set flag to clear previous data
						} else if (maxId > 1) {
							statuses = connection.getUserFavorits(id, sinceId, maxId);
						}
					} else if (search != null) {
						statuses = connection.getUserFavorits(search, sinceId, maxId);
					}
					break;

				case REPLIES_OFFLINE:
					statuses = db.getReplies(id);
					break;

				case REPLIES:
					if (sinceId == 0 && maxId == 0) {
						statuses = db.getReplies(id);
						if (statuses.isEmpty()) {
							statuses = connection.getStatusReplies(id, sinceId, maxId);
							if (!statuses.isEmpty() && db.containsStatus(id)) {
								db.saveReplyTimeline(statuses);
							}
						}
					} else if (sinceId > 0) {
						statuses = connection.getStatusReplies(id, sinceId, maxId);
						if (!statuses.isEmpty() && db.containsStatus(id)) {
							db.saveReplyTimeline(statuses);
						}
					} else if (maxId > 1) {
						statuses = connection.getStatusReplies(id, sinceId, maxId);
					}
					break;

				case SEARCH:
					statuses = connection.searchStatuses(search, sinceId, maxId);
					break;

				case USERLIST:
					statuses = connection.getUserlistStatuses(id, sinceId, maxId);
					break;

				case PUBLIC:
					statuses = connection.getPublicTimeline(sinceId, maxId);
					break;
			}
		} catch (ConnectionException exception) {
			this.exception = exception;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statuses;
	}


	@Override
	protected void onPostExecute(@Nullable List<org.nuclearfog.twidda.model.Status> statuses) {
		StatusFragment fragment = weakRef.get();
		if (fragment != null) {
			if (statuses != null) {
				fragment.setData(statuses, pos);
			}
			if (exception != null) {
				fragment.onError(exception);
			}
		}
	}
}