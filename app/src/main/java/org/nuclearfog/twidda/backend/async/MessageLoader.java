package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.lists.Messages;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.ui.fragments.MessageFragment;

import java.lang.ref.WeakReference;

/**
 * task to download a direct message list from twitter and handle message actions
 *
 * @author nuclearfog
 * @see MessageFragment
 */
public class MessageLoader extends AsyncTask<Void, Void, Messages> {

	/**
	 * load messages from database
	 */
	public static final int DB = 1;

	/**
	 * load messages online
	 */
	public static final int LOAD = 2;

	/**
	 * delete message
	 */
	public static final int DEL = 3;

	private WeakReference<MessageFragment> weakRef;
	private Connection connection;
	private AppDatabase db;
	private int action;

	@Nullable
	private ConnectionException exception;
	private String cursor;
	private long messageId;

	/**
	 * @param fragment  Callback to update data
	 * @param action    what action should be performed
	 * @param cursor    list cursor provided by twitter
	 * @param messageId if {@link #DEL} is selected this ID is used to delete the message
	 */
	public MessageLoader(MessageFragment fragment, int action, String cursor, long messageId) {
		super();
		weakRef = new WeakReference<>(fragment);
		db = new AppDatabase(fragment.getContext());
		connection = ConnectionManager.get(fragment.getContext());
		this.action = action;
		this.cursor = cursor;
		this.messageId = messageId;
	}


	@Override
	protected Messages doInBackground(Void... v) {
		try {
			switch (action) {
				case DB:
					Messages messages = db.getMessages();
					if (messages.isEmpty()) {
						messages = connection.getDirectmessages("");
						// merge online messages with offline messages
						db.saveMessages(messages);
						messages = db.getMessages();
					}
					return messages;

				case LOAD:
					messages = connection.getDirectmessages(cursor);
					// merge online messages with offline messages
					db.saveMessages(messages);
					return db.getMessages();

				case DEL:
					connection.deleteDirectmessage(messageId);
					db.removeMessage(messageId);
					break;
			}
		} catch (ConnectionException exception) {
			this.exception = exception;
			if (exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND) {
				db.removeMessage(messageId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	protected void onPostExecute(@Nullable Messages messages) {
		MessageFragment fragment = weakRef.get();
		if (fragment != null) {
			switch (action) {
				case DB:
				case LOAD:
					if (messages != null) {
						fragment.setData(messages);
					} else {
						fragment.onError(exception, messageId);
					}
					break;

				case DEL:
					if (exception == null) {
						fragment.removeItem(messageId);
					} else {
						fragment.onError(exception, messageId);
					}
					break;
			}
		}
	}
}