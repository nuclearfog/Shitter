package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.lists.Messages;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.ui.fragments.MessageFragment;


/**
 * task to download a direct message list from twitter and handle message actions
 *
 * @author nuclearfog
 * @see MessageFragment
 */
public class MessageLoader extends AsyncExecutor<MessageLoader.MessageLoaderParam, MessageLoader.MessageLoaderResult> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public MessageLoader(Context context) {
		db = new AppDatabase(context);
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected MessageLoaderResult doInBackground(@NonNull MessageLoaderParam param) {
		try {
			switch (param.mode) {
				case MessageLoaderParam.DATABASE:
					Messages messages = db.getMessages();
					if (!messages.isEmpty()) {
						return new MessageLoaderResult(MessageLoaderResult.DATABASE, param.index, param.id, messages, null);
					}
					// fall through

				case MessageLoaderParam.ONLINE:
					messages = connection.getDirectmessages(param.cursor);
					// merge online messages with offline messages
					db.saveMessages(messages);
					messages = db.getMessages();
					return new MessageLoaderResult(MessageLoaderResult.ONLINE, param.index, param.id, messages, null);

				case MessageLoaderParam.DELETE:
					connection.deleteDirectmessage(param.id);
					db.removeMessage(param.id);
					return new MessageLoaderResult(MessageLoaderResult.DELETE, param.index, param.id, null, null);
			}
		} catch (ConnectionException exception) {
			if (exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND)
				db.removeMessage(param.id);
			return new MessageLoaderResult(MessageLoaderResult.ERROR, param.index, param.id, null, exception);
		} catch (Exception exception) {
			if (BuildConfig.DEBUG) {
				exception.printStackTrace();
			}
		}
		return null;
	}

	/**
	 *
	 */
	public static class MessageLoaderParam {

		public static final int DATABASE = 1;
		public static final int ONLINE = 2;
		public static final int DELETE = 3;

		final int mode, index;
		final long id;
		final String cursor;

		public MessageLoaderParam(int mode, int index, long id, String cursor) {
			this.mode = mode;
			this.id = id;
			this.index = index;
			this.cursor = cursor;
		}
	}

	/**
	 *
	 */
	public static class MessageLoaderResult {

		public static final int ERROR = -1;
		public static final int DATABASE = 4;
		public static final int ONLINE = 5;
		public static final int DELETE = 6;

		public final int mode, index;
		public final long id;
		@Nullable
		public final Messages messages;
		@Nullable
		public final ConnectionException exception;

		MessageLoaderResult(int mode, int index, long id, @Nullable Messages messages, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.id = id;
			this.index = index;
			this.messages = messages;
			this.exception = exception;
		}
	}
}