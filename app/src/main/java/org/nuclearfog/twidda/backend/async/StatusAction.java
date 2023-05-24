package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.BuildConfig;
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
public class StatusAction extends AsyncExecutor<StatusAction.StatusParam, StatusAction.StatusResult> {

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
	protected StatusResult doInBackground(@NonNull StatusParam param) {
		try {
			switch (param.mode) {
				case StatusParam.DATABASE:
					Status status = db.getStatus(param.id);
					if (status != null) {
						return new StatusResult(StatusResult.DATABASE, status);
					}
					// fall through

				case StatusParam.ONLINE:
					status = connection.showStatus(param.id);
					if (db.containsStatus(param.id)) {
						// update status if there is a database entry
						db.saveStatus(status);
					}
					return new StatusResult(StatusResult.ONLINE, status);

				case StatusParam.DELETE:
					connection.deleteStatus(param.id);
					db.removeStatus(param.id);
					return new StatusResult(StatusResult.DELETE, null);

				case StatusParam.REPOST:
					status = connection.repostStatus(param.id);
					db.saveStatus(status);
					if (status.getEmbeddedStatus() != null)
						return new StatusResult(StatusResult.REPOST, status.getEmbeddedStatus());
					return new StatusResult(StatusResult.REPOST, status);

				case StatusParam.UNREPOST:
					status = connection.removeRepost(param.id);
					db.saveStatus(status);
					return new StatusResult(StatusResult.UNREPOST, status);

				case StatusParam.FAVORITE:
					status = connection.favoriteStatus(param.id);
					db.saveToFavorits(status);
					return new StatusResult(StatusResult.FAVORITE, status);

				case StatusParam.UNFAVORITE:
					status = connection.unfavoriteStatus(param.id);
					db.removeFromFavorite(status);
					return new StatusResult(StatusResult.UNFAVORITE, status);

				case StatusParam.BOOKMARK:
					status = connection.bookmarkStatus(param.id);
					db.saveToBookmarks(status);
					return new StatusResult(StatusResult.BOOKMARK, status);

				case StatusParam.UNBOOKMARK:
					status = connection.removeBookmark(param.id);
					db.removeFromBookmarks(status);
					return new StatusResult(StatusResult.UNBOOKMARK, status);

				case StatusParam.HIDE:
					connection.muteConversation(param.id);
					db.hideStatus(param.id, true);
					return new StatusResult(StatusResult.HIDE, null);

				case StatusParam.UNHIDE:
					connection.unmuteConversation(param.id);
					db.hideStatus(param.id, false);
					return new StatusResult(StatusResult.UNHIDE, null);
			}
		} catch (ConnectionException exception) {
			if (exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND) {
				// delete database entry if status was not found
				db.removeStatus(param.id);
			}
			return new StatusResult(StatusResult.ERROR, null, exception);
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
	public static class StatusParam {

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
		public static final int DELETE = 11;

		final int mode;
		final long id;

		public StatusParam(int mode, long id) {
			this.mode = mode;
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class StatusResult {

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
		public static final int DELETE = 22;

		public final int mode;
		@Nullable
		public final Status status;
		@Nullable
		public final ConnectionException exception;

		StatusResult(int mode, Status status) {
			this(mode, status, null);
		}

		StatusResult(int mode, @Nullable Status status, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.status = status;
			this.exception = exception;
		}
	}
}