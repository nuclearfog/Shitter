package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activities.AppSettings;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.model.Location;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Background task to load location information used by twitter such as location names and world ID's
 *
 * @author nuclearfog
 * @see AppSettings
 */
public class LocationLoader extends AsyncTask<Void, Void, List<Location>> {

    private TwitterException twException;
    private WeakReference<AppSettings> callback;
    private Twitter twitter;


    public LocationLoader(AppSettings callback) {
        super();
        this.callback = new WeakReference<>(callback);
        twitter = Twitter.get(callback);
    }


    @Override
    protected List<Location> doInBackground(Void[] v) {
        try {
            return twitter.getLocations();
        } catch (TwitterException twException) {
            this.twException = twException;
        }
        return null;
    }


    @Override
    protected void onPostExecute(List<Location> locations) {
        if (callback.get() != null) {
            if (locations != null) {
                callback.get().setLocationData(locations);
            } else {
                callback.get().onError(twException);
            }
        }
    }
}