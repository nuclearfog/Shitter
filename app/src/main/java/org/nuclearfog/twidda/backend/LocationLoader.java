package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.TrendLocation;
import org.nuclearfog.twidda.window.AppSettings;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;

public class LocationLoader extends AsyncTask<Void, Void, List<TrendLocation>> {

    private WeakReference<AppSettings> ui;
    private TwitterEngine mTwitter;
    private TwitterException err;


    public LocationLoader(AppSettings context) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
    }


    @Override
    protected List<TrendLocation> doInBackground(Void[] v) {
        try {
            return mTwitter.getLocations();
        } catch (TwitterException err) {
            this.err = err;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(List<TrendLocation> locations) {
        if (ui.get() != null) {
            if (locations != null && !locations.isEmpty()) {
                ArrayAdapter<TrendLocation> adapter = ui.get().getAdapter();
                adapter.clear();
                adapter.addAll(locations);
                adapter.notifyDataSetChanged();
                ui.get().setWoeIdSelection();
            } else if (err != null) {
                ErrorHandler.printError(ui.get(), err);
            }
        }
    }
}