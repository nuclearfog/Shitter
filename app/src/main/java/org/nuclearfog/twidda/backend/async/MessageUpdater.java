package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.backend.api.twitter.update.DirectmessageUpdate;
import org.nuclearfog.twidda.ui.activities.MessageEditor;

import java.lang.ref.WeakReference;

/**
 * Background task to send a direct messages to a user
 *
 * @author nuclearfog
 * @see MessageEditor
 */
public class MessageUpdater extends AsyncTask<Void, Void, Boolean> {

	private WeakReference<MessageEditor> weakRef;
	private Connection connection;

	@Nullable
	private ConnectionException exception;
	private DirectmessageUpdate message;

	/**
	 * send direct message
	 *
	 * @param activity Activity context
	 */
	public MessageUpdater(@NonNull MessageEditor activity, DirectmessageUpdate message) {
		super();
		connection = Twitter.get(activity);
		weakRef = new WeakReference<>(activity);
		this.message = message;
	}


	@Override
	protected Boolean doInBackground(Void[] v) {
		try {
			// first check if user exists
			long id = connection.showUser(message.getName()).getId();
			// upload media if any
			long mediaId = -1;
			if (message.getMediaUpdate() != null) {
				mediaId = connection.uploadMedia(message.getMediaUpdate());
			}
			// upload message and media ID
			if (!isCancelled()) {
				connection.sendDirectmessage(id, message.getText(), mediaId);
			}
			return true;
		} catch (ConnectionException exception) {
			this.exception = exception;
		} finally {
			// close all streams
			message.close();
		}
		return false;
	}


	@Override
	protected void onPostExecute(Boolean success) {
		MessageEditor activity = weakRef.get();
		if (activity != null) {
			if (success) {
				activity.onSuccess();
			} else {
				activity.onError(exception);
			}
		}
	}
}