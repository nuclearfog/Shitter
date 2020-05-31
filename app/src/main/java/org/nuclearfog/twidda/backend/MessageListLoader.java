package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.fragment.MessageFragment;

import java.lang.ref.WeakReference;
import java.util.List;

import static org.nuclearfog.twidda.backend.engine.EngineException.ErrorType.RESOURCE_NOT_FOUND;


/**
 * task to download a direct message list from twitter and handle message actions
 * @see MessageFragment
 */
public class MessageListLoader extends AsyncTask<Long, Void, List<Message>> {

    public enum Action {
        DB,
        LOAD,
        DEL
    }

    @Nullable
    private EngineException twException;
    private WeakReference<MessageFragment> callback;
    private TwitterEngine mTwitter;
    private AppDatabase db;
    private Action action;
    private long id;


    public MessageListLoader(MessageFragment callback, Action action) {
        this.callback = new WeakReference<>(callback);
        db = new AppDatabase(callback.getContext());
        mTwitter = TwitterEngine.getInstance(callback.getContext());
        this.action = action;
    }


    @Override
    protected void onPreExecute() {
        if (callback.get() != null) {
            callback.get().setRefresh(true);
        }
    }


    @Override
    protected List<Message> doInBackground(Long[] param) {
        long messageId = 0;
        try {
            switch (action) {
                case DB:
                    List<Message> messages = db.getMessages();
                    if (messages.isEmpty()) {
                        messages = mTwitter.getMessages();
                        db.storeMessage(messages);
                    }
                    return messages;

                case LOAD:
                    messages = mTwitter.getMessages();
                    db.storeMessage(messages);
                    messages = db.getMessages();
                    return messages;

                case DEL:
                    messageId = param[0];
                    id = messageId;
                    mTwitter.deleteMessage(messageId);
                    db.deleteMessage(messageId);
                    break;
            }
        } catch (EngineException twException) {
            this.twException = twException;
            if (twException.getErrorType() == RESOURCE_NOT_FOUND)
                db.deleteMessage(messageId);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(@Nullable List<Message> messages) {
        if (callback.get() != null) {
            if (messages != null) {
                callback.get().setData(messages);
            } else if (twException != null) {
                callback.get().onError(twException);
                if (twException.getErrorType() == RESOURCE_NOT_FOUND) {
                    callback.get().removeItem(id);
                }
            } else if (action == Action.DEL) {
                callback.get().removeItem(id);
            }
            callback.get().setRefresh(false);
        }
    }
}