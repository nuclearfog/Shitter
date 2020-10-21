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


/**
 * task to download a direct message list from twitter and handle message actions
 *
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
    private final WeakReference<MessageFragment> callback;
    private final TwitterEngine mTwitter;
    private final AppDatabase db;
    private final Action action;

    private long removeMsgId = -1;


    public MessageListLoader(MessageFragment callback, Action action) {
        super();
        this.callback = new WeakReference<>(callback);
        db = new AppDatabase(callback.getContext());
        mTwitter = TwitterEngine.getInstance(callback.getContext());
        this.action = action;
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
            } else if (removeMsgId > 0) {
                callback.get().removeItem(removeMsgId);
            } else {
                callback.get().onError(twException);
            }

        }
    }
}