package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.activities.StatusActivity;

import java.lang.ref.WeakReference;

/**
 * Background task to download a status informations and to take actions
 *
 * @author nuclearfog
 * @see StatusActivity
 */
public class StatusAction extends AsyncTask<Long, Status, Void> {

	/**
	 * Load status
	 */
	public static final int LOAD_ONLINE = 1;

	/**
	 * load status from database first
	 */
	public static final int LOAD_DATABASE = 2;

	/**
	 * repsot status
	 */
	public static final int REPOST = 3;

	/**
	 * remove repost
	 * (delete operation, "status ID" required)
	 */
	public static final int REMOVE_REPOST = 4;

	/**
	 * favorite status
	 */
	public static final int FAVORITE = 5;

	/**
	 * remove status from favorites
	 */
	public static final int UNFAVORITE = 6;

	/**
	 * hide reply
	 */
	public static final int HIDE = 7;

	/**
	 * unhide reply
	 */
	public static final int UNHIDE = 8;

	/**
	 * delete status
	 * (delete operation, "status ID" required)
	 */
	public static final int DELETE = 9;

	private Connection connection;
	private WeakReference<StatusActivity> weakRef;
	private AppDatabase db;

	@Nullable
	private ConnectionException exception;
	private int action;

	/**
	 * @param action action for a given status
	 */
	public StatusAction(StatusActivity activity, int action) {
		super();
		weakRef = new WeakReference<>(activity);
		connection = ConnectionManager.get(activity);
		db = new AppDatabase(activity);

		this.action = action;
	}

	/**
	 * @param ids first value is the status ID. The second value is the repost status ID. Required for delete operations
	 */
	@Override
	protected Void doInBackground(Long... ids) {
		org.nuclearfog.twidda.model.Status status;
		try {
			switch (action) {
				case LOAD_DATABASE:
					status = db.getStatus(ids[0]);
					if (status != null) {
						publishProgress(status);
					}
					// fall through

				case LOAD_ONLINE:
					status = connection.showStatus(ids[0]);
					publishProgress(status);
					if (db.containsStatus(ids[0])) {
						// update status if there is a database entry
						db.updateStatus(status);
					}
					break;

				case DELETE:
					connection.deleteStatus(ids[0]);
					db.removeStatus(ids[0]);
					// removing repost reference to this status
					db.removeStatus(ids[1]);
					break;

				case REPOST:
					status = connection.repostStatus(ids[0]);
					if (status.getEmbeddedStatus() != null)
						publishProgress(status.getEmbeddedStatus());
					db.updateStatus(status);
					break;

				case REMOVE_REPOST:
					status = connection.removeRepost(ids[0]);
					publishProgress(status);
					db.updateStatus(status);
					// removing repost reference to this status
					if (ids.length == 2)
						db.removeStatus(ids[1]);
					break;

				case FAVORITE:
					status = connection.favoriteStatus(ids[0]);
					publishProgress(status);
					db.saveToFavorites(status);
					break;

				case UNFAVORITE:
					status = connection.unfavoriteStatus(ids[0]);
					publishProgress(status);
					db.removeFavorite(status);
					break;

				case HIDE:
					connection.muteConversation(ids[0]);
					db.hideReply(ids[0], true);
					break;

				case UNHIDE:
					connection.unmuteConversation(ids[0]);
					db.hideReply(ids[0], false);
					break;
			}
		} catch (ConnectionException exception) {
			this.exception = exception;
			if (exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND) {
				// delete database entry if status was not found
				db.removeStatus(ids[0]);
				if (ids.length > 1) {
					// also remove reference to this status
					db.removeStatus(ids[1]);
				}
			}
		}
		return null;
	}


	@Override
	protected void onProgressUpdate(org.nuclearfog.twidda.model.Status... statuses) {
		StatusActivity activity = weakRef.get();
		if (activity != null && statuses.length > 0 && statuses[0] != null) {
			activity.setStatus(statuses[0]);
		}
	}


	@Override
	protected void onPostExecute(Void v) {
		StatusActivity activity = weakRef.get();
		if (activity != null) {
			if (exception == null) {
				activity.OnSuccess(action);
			} else {
				activity.onError(exception);
			}
		}
	}
}