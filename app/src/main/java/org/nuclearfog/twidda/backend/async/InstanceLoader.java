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
public class InstanceLoader extends AsyncExecutor<Void, Instance> {

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
	protected Instance doInBackground(@NonNull Void param) {
		Instance instance = null;
		try {
			instance = db.getInstance();
			if (instance == null || (System.currentTimeMillis() - instance.getTimestamp()) >= MAX_TIME_DIFF) {
				instance = connection.getInformation();
				db.saveInstance(instance);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return instance;
	}
}