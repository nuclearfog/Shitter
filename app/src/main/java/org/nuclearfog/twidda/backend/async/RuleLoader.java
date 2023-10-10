package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.lists.Rules;

/**
 * Loader class used to load instance rules
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.dialogs.ReportDialog
 */
public class RuleLoader extends AsyncExecutor<Void, Rules> {

	private Connection connection;

	/**
	 *
	 */
	public RuleLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Rules doInBackground(@NonNull Void param) {
		try {
			return connection.getRules();
		} catch (ConnectionException exception) {
			return null;
		}
	}
}