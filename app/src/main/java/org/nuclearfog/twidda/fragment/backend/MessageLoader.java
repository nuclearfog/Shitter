package org.nuclearfog.twidda.fragment.backend;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import org.nuclearfog.twidda.adapter.MessageAdapter;
import org.nuclearfog.twidda.backend.ErrorHandler;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.database.DatabaseAdapter;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;


public class MessageLoader extends AsyncTask<Long, Void, Boolean> {


    public enum Mode {
        LOAD,
        DEL
    }

    private Mode mode;
    private WeakReference<ViewGroup> ui;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private DatabaseAdapter db;
    private MessageAdapter adapter;
    private List<Message> messages;


    public MessageLoader(@NonNull ViewGroup root, Mode mode) {
        ui = new WeakReference<>(root);
        RecyclerView rv = (RecyclerView) root.getChildAt(0);
        adapter = (MessageAdapter) rv.getAdapter();
        mTwitter = TwitterEngine.getInstance(root.getContext());
        db = new DatabaseAdapter(root.getContext());
        this.mode = mode;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() == null)
            return;

        SwipeRefreshLayout reload = (SwipeRefreshLayout) ui.get();
        reload.setRefreshing(true);
    }


    @Override
    protected Boolean doInBackground(Long[] param) {
        long messageId = 0;
        try {
            switch (mode) {
                case LOAD:
                    messages = mTwitter.getMessages();
                    db.storeMessage(messages);
                    break;

                case DEL:
                    messageId = param[0];
                    mTwitter.deleteMessage(messageId);
                    db.deleteDm(messageId);
                    messages = db.getMessages();
                    break;
            }
        } catch (TwitterException err) {
            if (err.getErrorCode() == 34)
                db.deleteDm(messageId);
            else
                this.err = err;
            return false;
        } catch (Exception err) {
            if (err.getMessage() != null)
                Log.e("Status Loader", err.getMessage());
            return false;
        }
        return true;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() == null)
            return;

        if (success) {
            adapter.setData(messages);
            adapter.notifyDataSetChanged();
        } else {
            if (err != null)
                ErrorHandler.printError(ui.get().getContext(), err);
        }
        SwipeRefreshLayout reload = (SwipeRefreshLayout) ui.get();
        reload.setRefreshing(false);
    }


    @Override
    protected void onCancelled() {
        if (ui.get() == null)
            return;
        SwipeRefreshLayout reload = (SwipeRefreshLayout) ui.get();
        reload.setRefreshing(false);
    }
}