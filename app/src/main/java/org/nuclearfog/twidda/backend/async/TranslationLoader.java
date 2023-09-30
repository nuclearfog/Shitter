package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.Translation;

/**
 * Status translation loader
 *
 * @author nuclearfog
 */
public class TranslationLoader extends AsyncExecutor<Long, TranslationLoader.Result> {

	private Connection connection;

	/**
	 *
	 */
	public TranslationLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Long param) {
		try {
			return new Result(connection.getStatusTranslation(param));
		} catch (ConnectionException exception) {
			return new Result(null);
		}
	}

	/**
	 *
	 */
	public static class Result {

		@Nullable
		public Translation translation;

		Result(@Nullable Translation translation) {
			this.translation = translation;
		}
	}
}