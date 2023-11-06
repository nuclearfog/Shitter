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
public class TranslationLoader extends AsyncExecutor<TranslationLoader.Param, TranslationLoader.Result> {

	private Connection connection;

	/**
	 *
	 */
	public TranslationLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			return new Result(connection.getStatusTranslation(param.id), null);
		} catch (ConnectionException exception) {
			return new Result(null, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {
		final long id;

		public Param(long id) {
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class Result {

		@Nullable
		public final Translation translation;
		@Nullable
		public final ConnectionException exception;

		Result(@Nullable Translation translation, @Nullable ConnectionException exception) {
			this.translation = translation;
			this.exception = exception;
		}
	}
}