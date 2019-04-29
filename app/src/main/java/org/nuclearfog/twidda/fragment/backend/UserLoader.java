package org.nuclearfog.twidda.fragment.backend;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.nuclearfog.twidda.fragment.UserListFragment;

import java.lang.ref.WeakReference;

public class UserLoader extends AsyncTask<Long, Void, Boolean> {

    private WeakReference<UserListFragment> ui;


    public UserLoader(@NonNull UserListFragment frag) {
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