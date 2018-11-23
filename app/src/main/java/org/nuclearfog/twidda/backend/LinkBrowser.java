package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.MainActivity;

import java.lang.ref.WeakReference;

public class LinkBrowser extends AsyncTask<String, Void, Void> {

    private WeakReference<MainActivity> ui;

    public LinkBrowser(MainActivity context) {
        ui = new WeakReference<>(context);

    }


    @Override
    protected Void doInBackground(String... links) {


        return null;
    }


    @Override
    protected void onPostExecute(Void v) {


    }

}
