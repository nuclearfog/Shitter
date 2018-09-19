package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.Message;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.viewadapter.MessageAdapter;
import org.nuclearfog.twidda.window.DirectMessage;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;

public class MessageLoader extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<DirectMessage> ui;
    private MessageAdapter mAdapter;
    private TwitterEngine twitter;
    private DatabaseAdapter mData;
    private String errorMsg = "E MessageLoader: ";
    private int returnCode = 0;


    public MessageLoader(DirectMessage context) {
        ui = new WeakReference<>(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);

        RecyclerView dm_list = context.findViewById(R.id.messagelist);
        mAdapter = (MessageAdapter) dm_list.getAdapter();
        twitter = TwitterEngine.getInstance(context);
        mData = new DatabaseAdapter(context);

        if (mAdapter == null) {
            mAdapter = new MessageAdapter(context);
            mAdapter.setColor(settings.getFontColor());
            mAdapter.setImageLoad(settings.loadImages());
            dm_list.setAdapter(mAdapter);
        }
    }


    @Override
    protected Boolean doInBackground(Void... param) {
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
        } catch (TwitterException err) {
            returnCode = err.getErrorCode();
            errorMsg += err.getMessage();
            return false;
        } catch (Exception err) {
            errorMsg += err.getMessage();
            Log.e("Direct Message", errorMsg);
            err.printStackTrace();
            return false;
        }
        return true;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() == null) return;

        SwipeRefreshLayout mRefresh = ui.get().findViewById(R.id.dm_reload);
        mAdapter.notifyDataSetChanged();
        mRefresh.setRefreshing(false);

        if (!success) {
            switch (returnCode) {
                case 420:
                    Toast.makeText(ui.get(), R.string.rate_limit_exceeded, Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    Toast.makeText(ui.get(), R.string.error_not_specified, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(ui.get(), errorMsg, Toast.LENGTH_LONG).show();
            }
        }
    }
}