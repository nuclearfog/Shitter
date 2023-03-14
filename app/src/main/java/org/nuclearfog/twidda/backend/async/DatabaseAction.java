package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.database.AppDatabase;

/**
 * Async class used to modify database
 *
 * @author nuclearfog
 */
public class DatabaseAction extends AsyncExecutor<DatabaseAction.DatabaseParam, Void> {

	private AppDatabase db;

	/**
	 *
	 */
	public DatabaseAction(Context context) {
		db = new AppDatabase(context);
	}


	@NonNull
	@Override
	protected Void doInBackground(@NonNull DatabaseParam param) {
		try {
			if (param.mode == DatabaseParam.DELETE) {
				db.resetDatabase();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
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
}