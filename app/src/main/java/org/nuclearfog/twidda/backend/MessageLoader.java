package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.lists.Directmessages;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.fragments.MessageFragment;

import java.lang.ref.WeakReference;

/**
 * task to download a direct message list from twitter and handle message actions
 *
 * @author nuclearfog
 * @see MessageFragment
 */
public class MessageLoader extends AsyncTask<Long, Void, Directmessages> {

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
    private TwitterException twException;
    private WeakReference<MessageFragment> weakRef;
    private Twitter twitter;
    private AppDatabase db;
    private Action action;

    private String cursor;
    private long removeMsgId = -1;

    /**
     * @param fragment Callback to update data
     * @param action   what action should be performed
     */
    public MessageLoader(MessageFragment fragment, Action action, String cursor) {
        super();
        weakRef = new WeakReference<>(fragment);
        db = new AppDatabase(fragment.getContext());
        twitter = Twitter.get(fragment.getContext());
        this.action = action;
        this.cursor = cursor;
    }


    @Override
    protected Directmessages doInBackground(Long[] param) {
        long messageId = 0;
        try {
            switch (action) {
                case DB:
                    // TODO store cursor in the preferences
                    Directmessages messages = db.getMessages();
                    if (messages.isEmpty()) {
                        messages = twitter.getDirectmessages("");
                        // merge online messages with offline messages
                        db.storeMessage(messages);
                        messages = db.getMessages();
                    }
                    return messages;

                case LOAD:
                    messages = twitter.getDirectmessages(cursor);
                    // merge online messages with offline messages
                    db.storeMessage(messages);
                    return db.getMessages();

                case DEL:
                    messageId = param[0];
                    twitter.deleteDirectmessage(messageId);
                    db.deleteMessage(messageId);
                    removeMsgId = messageId;
                    break;
            }
        } catch (TwitterException twException) {
            this.twException = twException;
            if (twException.getErrorType() == ErrorHandler.TwitterError.RESOURCE_NOT_FOUND) {
                db.deleteMessage(messageId);
                removeMsgId = messageId;
            }
        }
        return null;
    }


    @Override
    protected void onPostExecute(@Nullable Directmessages messages) {
        MessageFragment fragment = weakRef.get();
        if (fragment != null) {
            if (messages != null) {
                fragment.setData(messages);
            } else {
                if (removeMsgId > 0) {
                    fragment.removeItem(removeMsgId);
                }
                if (twException != null) {
                    fragment.onError(twException);
                }
            }
        }
    }
}