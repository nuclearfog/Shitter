package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.Message;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.viewadapter.MessageAdapter;
import org.nuclearfog.twidda.window.DirectMessage;

import java.lang.ref.WeakReference;
import java.util.List;

public class MessageLoader extends AsyncTask<Void, Void, Void> {

    private WeakReference<DirectMessage> ui;
    private MessageAdapter mAdapter;
    private TwitterEngine twitter;
    private DatabaseAdapter mData;

    public MessageLoader(DirectMessage context) {
        ui = new WeakReference<>(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        boolean loadImages = settings.loadImages();

        RecyclerView dm_list = context.findViewById(R.id.messagelist);
        mAdapter = (MessageAdapter) dm_list.getAdapter();
        twitter = TwitterEngine.getInstance(context);
        mData = new DatabaseAdapter(context);

        if (mAdapter == null) {
            mAdapter = new MessageAdapter(context);
            mAdapter.setImageLoad(loadImages);
            dm_list.setAdapter(mAdapter);
        }
    }

    @Override
    protected Void doInBackground(Void... param) {
        try {
            List<Message> msg;
            if (mAdapter.getItemCount() > 0) {
                msg = twitter.getMessages();
                mData.storeMessage(msg);
                msg = mData.getMessages();
            } else {
                msg = mData.getMessages();
                if (msg.size() == 0) {
                    msg = twitter.getMessages();
                    mData.storeMessage(msg);
                }
            }
            mAdapter.setData(msg);
        } catch (Exception err) {
            Log.e("Direct Message", err.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        if (ui.get() != null) {
            SwipeRefreshLayout mRefresh = ui.get().findViewById(R.id.dm_reload);
            mAdapter.notifyDataSetChanged();
            mRefresh.setRefreshing(false);
        }
    }
}