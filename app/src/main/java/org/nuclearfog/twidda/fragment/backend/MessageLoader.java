package org.nuclearfog.twidda.fragment.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.adapter.MessageAdapter;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.fragment.MessageListFragment;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;


public class MessageLoader extends AsyncTask<Long, Void, List<Message>> {

    public enum Mode {
        DB,
        LOAD,
        DEL
    }

    @Nullable
    private TwitterException twException;
    private Mode mode;
    private WeakReference<MessageListFragment> ui;
    private TwitterEngine mTwitter;
    private AppDatabase db;
    private MessageAdapter adapter;


    public MessageLoader(MessageListFragment fragment, Mode mode) {
        ui = new WeakReference<>(fragment);
        db = new AppDatabase(fragment.getContext());
        mTwitter = TwitterEngine.getInstance(fragment.getContext());
        adapter = fragment.getAdapter();
        this.mode = mode;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() != null)
            ui.get().setRefresh(true);
    }


    @Override
    protected List<Message> doInBackground(Long[] param) {
        List<Message> messages = null;
        long messageId = 0;
        try {
            switch (mode) {
                case DB:
                    messages = db.getMessages();
                    if (!messages.isEmpty())
                        break;

                case LOAD:
                    messages = mTwitter.getMessages();
                    db.storeMessage(messages);
                    messages = db.getMessages();
                    break;

                case DEL:
                    messageId = param[0];
                    mTwitter.deleteMessage(messageId);
                    db.deleteDm(messageId);
                    messages = db.getMessages();
                    break;
            }
        } catch (TwitterException twException) {
            this.twException = twException;
            if (twException.getErrorCode() == 34) {
                db.deleteDm(messageId);
                messages = db.getMessages();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return messages;
    }


    @Override
    protected void onPostExecute(@Nullable List<Message> messages) {
        if (ui.get() != null) {
            if (messages != null)
                adapter.replaceAll(messages);
            if (twException != null)
                ErrorHandler.printError(ui.get().getContext(), twException);
            ui.get().setRefresh(false);
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() != null)
            ui.get().setRefresh(false);
    }


    @Override
    protected void onCancelled(@Nullable List<Message> messages) {
        if (ui.get() != null) {
            if (messages != null)
                adapter.replaceAll(messages);
            ui.get().setRefresh(false);
        }
    }
}