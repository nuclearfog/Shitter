package org.nuclearfog.twidda.fragment.backend;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.nuclearfog.twidda.fragment.MessageListFragment;

import java.lang.ref.WeakReference;


public class MessageLoader  extends AsyncTask<Long, Void, Boolean> {

    private WeakReference<MessageListFragment> ui;


    public MessageLoader(@NonNull MessageListFragment frag) {
        ui = new WeakReference<>(frag);
    }


    @Override
    protected java.lang.Boolean doInBackground(java.lang.Long[] objects) {
        return true;
    }


    @Override
    protected void onProgressUpdate(java.lang.Void[] v) {

    }


    @Override
    protected void onPostExecute(java.lang.Boolean success) {

    }
}