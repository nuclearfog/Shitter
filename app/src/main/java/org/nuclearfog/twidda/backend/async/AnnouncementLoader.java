package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.lists.Announcements;

/**
 * Async loader for instance announcements
 *
 * @author nuclearfog
 */
public class AnnouncementLoader extends AsyncExecutor<Void, AnnouncementLoader.Result> {

	private Connection connection;

	/**
	 *
	 */
	public AnnouncementLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Void param) {
		try {
			Announcements announcements = connection.getAnnouncements();
			return new Result(announcements, null);
		} catch (ConnectionException exception) {
			return new Result(null, exception);
		}
	}

	/**
	 *
	 */
	public static class Result {
		@Nullable
		public final Announcements announcements;
		@Nullable
		public final ConnectionException exception;

		public Result(@Nullable Announcements announcements, @Nullable ConnectionException exception) {
			this.announcements = announcements;
			this.exception = exception;
		}
	}
}