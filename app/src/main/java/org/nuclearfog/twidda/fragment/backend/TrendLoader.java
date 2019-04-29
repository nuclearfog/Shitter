package org.nuclearfog.twidda.fragment.backend;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.nuclearfog.twidda.fragment.TrendListFragment;

import java.lang.ref.WeakReference;

public class TrendLoader  extends AsyncTask<Long, Void, Boolean> {

    private WeakReference<TrendListFragment> ui;


    public TrendLoader(@NonNull TrendListFragment frag) {
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