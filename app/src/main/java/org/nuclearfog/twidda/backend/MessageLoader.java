package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.MessageAdapter;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.window.DirectMessage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import twitter4j.TwitterException;

public class MessageLoader extends AsyncTask<Long, Void, Void> {

    public enum Mode {
        GET,
        LDR,
        DEL
    }
    private final Mode mode;
    private boolean failure = false;

    private WeakReference<DirectMessage> ui;
    private MessageAdapter mAdapter;
    private TwitterEngine twitter;
    private TwitterException err;
    private DatabaseAdapter mData;
    private List<Message> message;


    public MessageLoader(@NonNull DirectMessage context, Mode mode) {
        ui = new WeakReference<>(context);
        RecyclerView dm_list = context.findViewById(R.id.messagelist);
        mAdapter = (MessageAdapter) dm_list.getAdapter();
        twitter = TwitterEngine.getInstance(context);
        mData = new DatabaseAdapter(context);
        message = new ArrayList<>();
        this.mode = mode;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() == null) return;

        SwipeRefreshLayout mRefresh = ui.get().findViewById(R.id.dm_reload);
        mRefresh.setRefreshing(true);
    }


    @Override
    protected Void doInBackground(Long... param) {
        long messageId = -1;
        try {
            switch(mode) {
                case GET:
                    message = twitter.getMessages();
                    mData.storeMessage(message);
                    break;

                case DEL:
                    messageId = param[0];
                    twitter.deleteMessage(messageId);
                    mData.deleteDm(messageId);
                    break;
            }
        } catch (TwitterException err) {
            if (err.getErrorCode() == 34)
                mData.deleteDm(messageId);
            this.err = err;
            failure = true;
        } catch (Exception err) {
            if(err.getMessage() != null)
                Log.e("Direct Message", err.getMessage());
            failure = true;
        } finally {
            message = mData.getMessages();
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void mode) {
        if (ui.get() == null) return;

        SwipeRefreshLayout mRefresh = ui.get().findViewById(R.id.dm_reload);
        mRefresh.setRefreshing(false);

        mAdapter.setData(message);
        mAdapter.notifyDataSetChanged();

        if (failure) {
            if (err != null && err.getErrorCode() != 34)
                ErrorHandler.printError(ui.get(), err);
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() == null) return;

        SwipeRefreshLayout mRefresh = ui.get().findViewById(R.id.dm_reload);
        mRefresh.setRefreshing(false);
    }
}