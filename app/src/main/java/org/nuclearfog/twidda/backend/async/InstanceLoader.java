package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Instance;

/**
 * Background loader for instance information
 *
 * @author nuclearfog
 */
public class InstanceLoader extends AsyncExecutor<InstanceLoader.Param, InstanceLoader.Result> {

	/**
	 * time difference to update instance information
	 * if database instance is older than this time, an update will be triggered
	 */
	private static final long MAX_TIME_DIFF = 172800000L;

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public InstanceLoader(Context context) {
		db = new AppDatabase(context);
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			Instance instance;
			switch (param.mode) {
				case Param.OFFLINE:
					instance = db.getInstance();
					if (instance != null && instance.getTimestamp() <= MAX_TIME_DIFF)
						return new Result(instance, null);

				case Param.ONLINE:
					instance = connection.getInformation();
					db.saveInstance(instance);
					return new Result(instance, null);
			}
		} catch (ConnectionException exception) {
			return new Result(null, exception);
		}
		return null;
	}

	/**
	 *
	 */
	public static class Param {

		public static final int OFFLINE = 1;
		public static final int ONLINE = 2;

		final int mode;

		public Param(int mode) {
			this.mode = mode;
		}
	}

	/**
	 *
	 */
	public static class Result {

		@Nullable
		public final Instance instance;
		@Nullable
		public final ConnectionException exception;

		public Result(@Nullable Instance instance, @Nullable ConnectionException exception) {
			this.instance = instance;
			this.exception = exception;
		}
	}
}