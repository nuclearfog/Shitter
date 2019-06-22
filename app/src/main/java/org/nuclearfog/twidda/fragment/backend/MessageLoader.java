package org.nuclearfog.twidda.fragment.backend;

import android.os.AsyncTask;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.MessageAdapter;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.database.DatabaseAdapter;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;

import static android.os.AsyncTask.Status.FINISHED;

public class MessageLoader extends AsyncTask<Long, Void, List<Message>> {

    public enum Mode {
        DB,
        LOAD,
        DEL
    }

    private Mode mode;
    private WeakReference<View> ui;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private DatabaseAdapter db;
    private MessageAdapter adapter;


    public MessageLoader(@NonNull View root, Mode mode) {
        ui = new WeakReference<>(root);
        RecyclerView rv = root.findViewById(R.id.fragment_list);
        adapter = (MessageAdapter) rv.getAdapter();
        mTwitter = TwitterEngine.getInstance(root.getContext());
        db = new DatabaseAdapter(root.getContext());
        this.mode = mode;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() == null)
            return;
        final SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
        reload.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getStatus() != FINISHED)
                    reload.setRefreshing(true);
            }
        }, 500);
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
                    break;

                case DEL:
                    messageId = param[0];
                    mTwitter.deleteMessage(messageId);
                    db.deleteDm(messageId);
                    messages = db.getMessages();
                    break;
            }
        } catch (TwitterException err) {
            this.err = err;
            if (err.getErrorCode() == 34) {
                db.deleteDm(messageId);
                messages = db.getMessages();
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return messages;
    }


    @Override
    protected void onPostExecute(@Nullable List<Message> messages) {
        if (ui.get() != null) {
            if (messages != null)
                adapter.setData(messages);
            if (err != null)
                ErrorHandler.printError(ui.get().getContext(), err);
            SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
            reload.setRefreshing(false);
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() != null) {
            SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
            reload.setRefreshing(false);
        }
    }


    @Override
    protected void onCancelled(@Nullable List<Message> messages) {
        if (ui.get() != null) {
            if (messages != null)
                adapter.setData(messages);
            SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
            reload.setRefreshing(false);
        }
    }
}