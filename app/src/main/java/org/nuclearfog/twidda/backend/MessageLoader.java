package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
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

public class MessageLoader extends AsyncTask<Long, Void, Long> {

    public static final long LOAD = 0;
    public static final long DELETE = 1;
    private static final long FAIL = -1;

    private WeakReference<DirectMessage> ui;
    private MessageAdapter mAdapter;
    private TwitterEngine twitter;
    private TwitterException err;
    private DatabaseAdapter mData;
    private List<Message> message;


    public MessageLoader(DirectMessage context) {
        ui = new WeakReference<>(context);
        RecyclerView dm_list = context.findViewById(R.id.messagelist);
        mAdapter = (MessageAdapter) dm_list.getAdapter();
        twitter = TwitterEngine.getInstance(context);
        message = new ArrayList<>();
        mData = new DatabaseAdapter(context);
    }


    @Override
    protected Long doInBackground(Long... param) {
        final long MODE = param[0];
        try {
            if (MODE == LOAD) {
                if (mAdapter.getItemCount() > 0) {
                    message = twitter.getMessages();
                    mData.storeMessage(message);
                    message = mData.getMessages();
                } else {
                    message = mData.getMessages();
                    if (message.isEmpty()) {
                        message = twitter.getMessages();
                        mData.storeMessage(message);
                    }
                }
            } else if (MODE == DELETE) {
                mData.deleteDm(param[1]);
                message = mData.getMessages();
                twitter.deleteMessage(param[1]);
            }
        } catch (TwitterException err) {
            this.err = err;
            return FAIL;
        } catch (Exception err) {
            Log.e("Direct Message", err.getMessage());
            return FAIL;
        }
        return MODE;
    }


    @Override
    protected void onPostExecute(Long mode) {
        if (ui.get() == null) return;

        SwipeRefreshLayout mRefresh = ui.get().findViewById(R.id.dm_reload);
        mRefresh.setRefreshing(false);

        if (mode != FAIL) {
            mAdapter.setData(message);
            mAdapter.notifyDataSetChanged();
        } else {
            if (err != null)
                ErrorHandling.printError(ui.get(), err);
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() == null) return;

        SwipeRefreshLayout mRefresh = ui.get().findViewById(R.id.dm_reload);
        mRefresh.setRefreshing(false);
    }
}