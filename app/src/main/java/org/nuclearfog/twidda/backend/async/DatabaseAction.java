package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.database.AppDatabase;

/**
 * Async class used to modify database
 *
 * @author nuclearfog
 */
public class DatabaseAction extends AsyncExecutor<DatabaseAction.DatabaseParam, DatabaseAction.DatabaseResult> {

	private AppDatabase db;

	/**
	 *
	 */
	public DatabaseAction(Context context) {
		db = new AppDatabase(context);
	}


	@Override
	protected DatabaseResult doInBackground(@NonNull DatabaseParam param) {
		try {
			if (param.mode == DatabaseParam.DELETE) {
				db.resetDatabase();
				return new DatabaseResult(DatabaseResult.DELETE);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return new DatabaseResult(DatabaseResult.ERROR);
	}

	/**
	 *
	 */
	public static class DatabaseParam {

		public static final int DELETE = 1;

		final int mode;

		public DatabaseParam(int mode) {
			this.mode = mode;
		}
	}

	/**
	 *
	 */
	public static class DatabaseResult {

		public static final int ERROR = -1;
		public static final int DELETE = 1;

		public final int mode;

		DatabaseResult(int mode) {
			this.mode = mode;
		}
	}
}