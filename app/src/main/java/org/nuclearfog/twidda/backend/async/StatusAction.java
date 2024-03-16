package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.activities.StatusActivity;

/**
 * Background task to download a status informations and to take actions
 *
 * @author nuclearfog
 * @see StatusActivity
 */
public class StatusAction extends AsyncExecutor<StatusAction.Param, StatusAction.Result> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public StatusAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		db = new AppDatabase(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.action) {
				case Param.DATABASE:
					Status status = db.getStatus(param.id);
					if (status != null) {
						return new Result(Result.DATABASE, status);
					}
					// fall through

				case Param.ONLINE:
					status = connection.showStatus(param.id);
					if (db.containsStatus(param.id)) {
						// update status if there is a database entry
						db.saveStatus(status);
					}
					return new Result(Result.ONLINE, status);

				case Param.DELETE:
					connection.deleteStatus(param.id);
					db.removeStatus(param.id);
					return new Result(Result.DELETE, null);

				case Param.REPOST:
					status = connection.repostStatus(param.id);
					db.saveStatus(status);
					if (status.getEmbeddedStatus() != null)
						return new Result(Result.REPOST, status.getEmbeddedStatus());
					return new Result(Result.REPOST, status);

				case Param.UNREPOST:
					status = connection.removeRepost(param.id);
					db.saveStatus(status);
					return new Result(Result.UNREPOST, status);

				case Param.FAVORITE:
					status = connection.favoriteStatus(param.id);
					db.saveToFavorits(status);
					return new Result(Result.FAVORITE, status);

				case Param.UNFAVORITE:
					status = connection.unfavoriteStatus(param.id);
					db.removeFromFavorite(status);
					return new Result(Result.UNFAVORITE, status);

				case Param.BOOKMARK:
					status = connection.bookmarkStatus(param.id);
					db.saveToBookmarks(status);
					return new Result(Result.BOOKMARK, status);

				case Param.UNBOOKMARK:
					status = connection.removeBookmark(param.id);
					db.removeFromBookmarks(status);
					return new Result(Result.UNBOOKMARK, status);

				case Param.HIDE:
					connection.muteConversation(param.id);
					db.hideStatus(param.id, true);
					return new Result(Result.HIDE, null);

				case Param.UNHIDE:
					connection.unmuteConversation(param.id);
					db.hideStatus(param.id, false);
					return new Result(Result.UNHIDE, null);

				case Param.PIN:
					status = connection.pinStatus(param.id);
					db.saveStatus(status);
					return new Result(Result.PIN, status);

				case Param.UNPIN:
					status = connection.unpinStatus(param.id);
					db.saveStatus(status);
					return new Result(Result.UNPIN, status);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			if (exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND) {
				// delete database entry if status was not found
				db.removeStatus(param.id);
			}
			return new Result(Result.ERROR, null, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int ONLINE = 1;
		public static final int DATABASE = 2;
		public static final int REPOST = 3;
		public static final int UNREPOST = 4;
		public static final int FAVORITE = 5;
		public static final int UNFAVORITE = 6;
		public static final int HIDE = 7;
		public static final int UNHIDE = 8;
		public static final int BOOKMARK = 9;
		public static final int UNBOOKMARK = 10;
		public static final int PIN = 11;
		public static final int UNPIN = 12;
		public static final int DELETE = 13;

		final int action;
		final long id;

		public Param(int action, long id) {
			this.action = action;
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int ONLINE = 12;
		public static final int DATABASE = 13;
		public static final int REPOST = 14;
		public static final int UNREPOST = 15;
		public static final int FAVORITE = 16;
		public static final int UNFAVORITE = 17;
		public static final int HIDE = 18;
		public static final int UNHIDE = 19;
		public static final int BOOKMARK = 20;
		public static final int UNBOOKMARK = 21;
		public static final int PIN = 22;
		public static final int UNPIN = 23;
		public static final int DELETE = 24;

		public final int action;
		@Nullable
		public final Status status;
		@Nullable
		public final ConnectionException exception;

		Result(int action, Status status) {
			this(action, status, null);
		}

		Result(int action, @Nullable Status status, @Nullable ConnectionException exception) {
			this.action = action;
			this.status = status;
			this.exception = exception;
		}
	}
}