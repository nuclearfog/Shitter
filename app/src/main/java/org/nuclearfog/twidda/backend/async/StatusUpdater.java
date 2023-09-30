package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.backend.helper.update.StatusUpdate;
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
public class StatusUpdater extends AsyncExecutor<StatusUpdate, StatusUpdater.Result> {

	private Connection connection;

	/**
	 *
	 */
	public StatusUpdater(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull StatusUpdate update) throws InterruptedException {
		try {
			// upload media first
			List<Long> mediaIds = new LinkedList<>();
			for (MediaStatus mediaStatus : update.getMediaStatuses()) {
				if (mediaStatus.isLocal()) {
					long mediaId = connection.updateMedia(mediaStatus);
					mediaIds.add(mediaId);
				}
			}
			// upload status
			Status status = connection.updateStatus(update, mediaIds);
			return new Result(status, null);
		} catch (ConnectionException exception) {
			return new Result(null, exception);
		} finally {
			update.close();
		}
	}

	/**
	 *
	 */
	public static class Result {

		@Nullable
		public final Status status;
		@Nullable
		public final ConnectionException exception;

		Result(@Nullable Status status, @Nullable ConnectionException exception) {
			this.status = status;
			this.exception = exception;
		}
	}
}