package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.backend.helper.StatusUpdate;
import org.nuclearfog.twidda.ui.activities.StatusEditor;

/**
 * Background task for posting a status
 *
 * @author nuclearfog
 * @see StatusEditor
 */
public class StatusUpdater extends AsyncExecutor<StatusUpdate, StatusUpdater.StatusUpdateResult> {

	private Connection connection;

	/**
	 *
	 */
	public StatusUpdater(Context context) {
		connection = ConnectionManager.get(context);
	}


	@NonNull
	@Override
	protected StatusUpdateResult doInBackground(StatusUpdate update) {
		try {
			// upload media first
			MediaStatus[] mediaUpdates = update.getMediaUpdates();
			long[] mediaIds = new long[mediaUpdates.length];
			for (int pos = 0; pos < mediaUpdates.length; pos++) {
				// upload media file and save media ID
				mediaIds[pos] = connection.uploadMedia(mediaUpdates[pos]);
			}
			// upload status
			connection.uploadStatus(update, mediaIds);
			return new StatusUpdateResult(true, null);
		} catch (ConnectionException exception) {
			return new StatusUpdateResult(false, exception);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			update.close();
		}
		return new StatusUpdateResult(false, null);
	}

	/**
	 *
	 */
	public static class StatusUpdateResult {

		public final boolean success;
		@Nullable
		public final ConnectionException exception;

		StatusUpdateResult(boolean success, @Nullable ConnectionException exception) {
			this.success = success;
			this.exception = exception;
		}
	}
}