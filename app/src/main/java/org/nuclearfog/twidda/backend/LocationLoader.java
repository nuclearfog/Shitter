package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.TrendLocation;
import org.nuclearfog.twidda.window.AppSettings;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;

/**
 * Background task to load location information used by twitter
 */
public class LocationLoader extends AsyncTask<Void, Void, List<TrendLocation>> {

    private WeakReference<AppSettings> ui;
    private TwitterEngine mTwitter;
    private TwitterException twException;


    /**
     * load location data from twitter
     *
     * @param context Activity context
     */
    public LocationLoader(AppSettings context) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
    }


    @Override
    protected List<TrendLocation> doInBackground(Void[] v) {
        try {
            return mTwitter.getLocations();
        } catch (TwitterException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
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
            } else if (twException != null) {
                ErrorHandler.printError(ui.get(), twException);
            }
        }
    }
}