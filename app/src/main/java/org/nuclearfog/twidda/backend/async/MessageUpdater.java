package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.MessageUpdate;
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
	protected MessageUpdateResult doInBackground(@NonNull MessageUpdate update) {
		try {
			// first check if user exists
			long id = connection.showUser(update.getReceiver()).getId();
			// upload media if any
			long mediaId = 0L;
			if (update.getMediaUpdate() != null) {
				mediaId = connection.uploadMedia(update.getMediaUpdate());
			}
			// upload message and media ID
			connection.sendDirectmessage(id, update.getMessage(), mediaId);
			return new MessageUpdateResult(true, null);
		} catch (ConnectionException exception) {
			return new MessageUpdateResult(false, exception);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			update.close();
		}
		return new MessageUpdateResult(false, null);
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