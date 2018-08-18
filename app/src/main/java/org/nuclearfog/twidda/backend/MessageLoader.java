package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.Message;
import org.nuclearfog.twidda.viewadapter.MessageAdapter;
import org.nuclearfog.twidda.window.DirectMessage;

import java.lang.ref.WeakReference;
import java.util.List;

public class MessageLoader extends AsyncTask<Long, Void, Long> {

    private WeakReference<DirectMessage> ui;
    private MessageAdapter mAdapter;
    private TwitterEngine twitter;

    public MessageLoader(Context context) {
        ui = new WeakReference<>((DirectMessage) context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        boolean loadImages = settings.loadImages();

        RecyclerView dm_list = ui.get().findViewById(R.id.messagelist);
        mAdapter = (MessageAdapter) dm_list.getAdapter();
        twitter = TwitterEngine.getInstance(context);

        if (mAdapter == null) {
            mAdapter = new MessageAdapter(ui.get());
            mAdapter.setImageLoad(loadImages);
            dm_list.setAdapter(mAdapter);
        }
    }

    @Override
    protected Long doInBackground(Long... params) {
        try {
            List<Message> msg = twitter.getMessages();
            mAdapter.setData(msg);
        } catch (Exception err) {
            Log.e("Direct Message", err.getMessage());
        }
        return 1L;
    }

    @Override
    protected void onPostExecute(Long param) {
        if (ui.get() == null)
            return;

        SwipeRefreshLayout mRefresh = ui.get().findViewById(R.id.dm_reload);
        mAdapter.notifyDataSetChanged();
        mRefresh.setRefreshing(false);
    }
}