package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.backend.helper.StatusUpdate;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.activities.StatusEditor;

import java.util.LinkedList;
import java.util.List;

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
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected StatusUpdateResult doInBackground(@NonNull StatusUpdate update) {
		try {
			// upload media first
			List<Long> mediaIds = new LinkedList<>();
			for (MediaStatus mediaStatus : update.getMediaStatuses()) {
				if (mediaStatus.isLocal()) {
					long mediaId = connection.uploadMedia(mediaStatus);
					mediaIds.add(mediaId);
				}
			}
			// upload status
			Status status = connection.uploadStatus(update, mediaIds);
			return new StatusUpdateResult(status, null);
		} catch (ConnectionException exception) {
			return new StatusUpdateResult(null, exception);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			update.close();
		}
		return new StatusUpdateResult(null, null);
	}

	/**
	 *
	 */
	public static class StatusUpdateResult {

		@Nullable
		public final Status status;
		@Nullable
		public final ConnectionException exception;

		StatusUpdateResult(@Nullable Status status, @Nullable ConnectionException exception) {
			this.status = status;
			this.exception = exception;
		}
	}
}