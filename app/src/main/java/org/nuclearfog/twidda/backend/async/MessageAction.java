package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;

/**
 * Async class to manage messages
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.fragments.MessageFragment
 */
public class MessageAction extends AsyncExecutor<MessageAction.MessageActionParam, MessageAction.MessageActionResult> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public MessageAction(Context context) {
		db = new AppDatabase(context);
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected MessageActionResult doInBackground(@NonNull MessageActionParam param) throws InterruptedException {
		try {
			if (param.mode == MessageActionParam.DELETE) {
				connection.deleteDirectmessage(param.id);
				db.removeMessage(param.id);
				return new MessageActionResult(MessageActionResult.DELETE, param.id, null);
			}
		} catch (ConnectionException exception) {
			if (exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND)
				db.removeMessage(param.id);
			return new MessageActionResult(MessageActionResult.ERROR, param.id, exception);
		}
		return null;
	}

	/**
	 *
	 */
	public static class MessageActionParam {

		public static final int DELETE = 1;

		final int mode;
		final long id;

		public MessageActionParam(int mode, long id) {
			this.mode = mode;
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class MessageActionResult {

		public static final int ERROR = -1;
		public static final int DELETE = 10;

		public final int mode;
		public final long id;
		public final ConnectionException exception;

		MessageActionResult(int mode, long id, ConnectionException exception) {
			this.exception = exception;
			this.mode = mode;
			this.id = id;
		}
	}
}