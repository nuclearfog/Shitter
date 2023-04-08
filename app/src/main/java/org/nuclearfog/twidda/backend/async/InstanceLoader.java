package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Instance;

/**
 * Background loader for instance information
 *
 * @author nuclearfog
 */
public class InstanceLoader extends AsyncExecutor<InstanceLoader.InstanceLoaderParam, Instance> {

	/**
	 * time difference to update instance information
	 * if database instance is older than this time, an update will be triggered
	 */
	private static final long MAX_TIME_DIFF = 1000 * 60 * 60 * 24 * 2;

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
	protected Instance doInBackground(@NonNull InstanceLoaderParam param) {
		Instance instance = null;
		try {
			switch (param.mode) {
				case InstanceLoaderParam.LOAD_DB:
					instance = db.getInstance(param.domain);
					if (instance != null && (System.currentTimeMillis() - instance.getTimestamp()) < MAX_TIME_DIFF)
						break;
					// fall through

				case InstanceLoaderParam.LOAD_ONLINE:
					instance = connection.getInformation();
					break;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return instance;
	}

	/**
	 *
	 */
	public static class InstanceLoaderParam {

		public static final int LOAD_DB = 1;
		public static final int LOAD_ONLINE = 2;

		final int mode;
		final String domain;

		public InstanceLoaderParam(int mode, String domain) {
			this.domain = domain;
			this.mode = mode;
		}
	}
}