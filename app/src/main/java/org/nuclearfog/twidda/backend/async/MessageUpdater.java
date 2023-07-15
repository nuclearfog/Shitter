package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.update.MessageUpdate;
import org.nuclearfog.twidda.ui.activities.MessageEditor;

/**
 * Background task to send a direct messages to a user
 *
 * @author nuclearfog
 * @see MessageEditor
 */
public class MessageUpdater extends AsyncExecutor<MessageUpdate, MessageUpdater.MessageUpdateResult> {

	private Connection connection;

	/**
	 *
	 */
	public MessageUpdater(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected MessageUpdateResult doInBackground(@NonNull MessageUpdate update) throws InterruptedException {
		try {
			// first check if user exists
			long id = connection.showUser(update.getReceiver()).getId();
			// upload media if any
			long mediaId = 0L;
			if (update.getMediaStatus() != null) {
				mediaId = connection.updateMedia(update.getMediaStatus());
			}
			// upload message and media ID
			connection.sendDirectmessage(id, update.getMessage(), mediaId);
			return new MessageUpdateResult(true, null);
		} catch (ConnectionException exception) {
			return new MessageUpdateResult(false, exception);
		} finally {
			update.close();
		}
	}

	/**
	 *
	 */
	public static class MessageUpdateResult {

		public final boolean success;
		@Nullable
		public final ConnectionException exception;

		MessageUpdateResult(boolean success, @Nullable ConnectionException exception) {
			this.exception = exception;
			this.success = success;
		}
	}
}