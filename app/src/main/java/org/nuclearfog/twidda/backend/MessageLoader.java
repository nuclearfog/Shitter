package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.lists.MessageList;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.fragment.MessageFragment;

import java.lang.ref.WeakReference;


/**
 * task to download a direct message list from twitter and handle message actions
 *
 * @author nuclearfog
 * @see MessageFragment
 */
public class MessageLoader extends AsyncTask<Long, Void, MessageList> {

    /**
     * action to perform
     */
    public enum Action {
        /**
         * load messages from database
         */
        DB,
        /**
         * load messages online
         */
        LOAD,
        /**
         * delete message
         */
        DEL
    }

    @Nullable
    private EngineException twException;
    private final WeakReference<MessageFragment> callback;
    private final TwitterEngine mTwitter;
    private final AppDatabase db;
    private final Action action;

    private String cursor;
    private long removeMsgId = -1;

    /**
     * @param callback Callback to update data
     * @param action   what action should be performed
     */
    public MessageLoader(MessageFragment callback, Action action, String cursor) {
        super();
        this.callback = new WeakReference<>(callback);
        db = new AppDatabase(callback.getContext());
        mTwitter = TwitterEngine.getInstance(callback.getContext());
        this.action = action;
        this.cursor = cursor;
    }


    @Override
    protected MessageList doInBackground(Long[] param) {
        long messageId = 0;
        try {
            switch (action) {
                case DB:
                    // TODO store cursor in the preferences
                    MessageList messages = db.getMessages();
                    if (messages.isEmpty()) {
                        messages = mTwitter.getMessages(null);
                        db.storeMessage(messages);
                    }
                    return messages;

                case LOAD:
                    messages = mTwitter.getMessages(cursor);
                    db.storeMessage(messages);
                    return messages;

                case DEL:
                    messageId = param[0];
                    mTwitter.deleteMessage(messageId);
                    db.deleteMessage(messageId);
                    removeMsgId = messageId;
                    break;
            }
        } catch (EngineException twException) {
            this.twException = twException;
            if (twException.resourceNotFound()) {
                db.deleteMessage(messageId);
                removeMsgId = messageId;
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(@Nullable MessageList messages) {
        if (callback.get() != null) {
            if (messages != null) {
                callback.get().setData(messages);
            } else {
                if (removeMsgId > 0) {
                    callback.get().removeItem(removeMsgId);
                }
                if (twException != null) {
                    callback.get().onError(twException);
                }
            }
        }
    }
}