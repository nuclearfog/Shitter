package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.database.AppDatabase;

/**
 * Async class used to modify database
 *
 * @author nuclearfog
 */
public class DatabaseAction extends AsyncExecutor<DatabaseAction.Param, DatabaseAction.Result> {

	private AppDatabase db;
	private GlobalSettings settings;

	/**
	 *
	 */
	public DatabaseAction(Context context) {
		settings = GlobalSettings.get(context);
		db = new AppDatabase(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.action) {
				case Param.DELETE:
					db.resetDatabase();
					return new Result(Result.DELETE);

				case Param.LOGOUT:
					db.removeLogin(settings.getLogin());
					return new Result(Result.LOGOUT);

				default:
					return null;
			}
		} catch (Exception exception) {
			return new Result(Result.ERROR);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int DELETE = 1;
		public static final int LOGOUT = 2;

		final int action;

		/**
		 * @param action action performed on the app database {@link #DELETE,#LOGOUT}
		 */
		public Param(int action) {
			this.action = action;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int DELETE = 10;
		public static final int LOGOUT = 11;

		public final int action;

		/**
		 * @param action action performed on the database {@link #DELETE,#LOGOUT} or {@link #ERROR} if an error occured
		 */
		Result(int action) {
			this.action = action;
		}
	}
}