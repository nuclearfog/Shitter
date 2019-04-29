package org.nuclearfog.twidda.fragment.backend;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.nuclearfog.twidda.fragment.TweetListFragment;
import java.lang.ref.WeakReference;


public class TweetLoader extends AsyncTask<Long, Void, Boolean> {

    private WeakReference<TweetListFragment> ui;


    public TweetLoader(@NonNull TweetListFragment frag) {
        ui = new WeakReference<>(frag);
    }


    @Override
    protected Boolean doInBackground(Long[] objects) {
        return true;
    }


    @Override
    protected void onProgressUpdate(Void[] v) {

    }


    @Override
    protected void onPostExecute(Boolean success) {

    }
}