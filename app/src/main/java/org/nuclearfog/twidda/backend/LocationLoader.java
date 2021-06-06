package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activity.AppSettings;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.model.TrendLocation;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Background task to load location information used by twitter such as location names and world ID's
 *
 * @author nuclearfog
 * @see AppSettings
 */
public class LocationLoader extends AsyncTask<Void, Void, List<TrendLocation>> {

    private EngineException twException;
    private WeakReference<AppSettings> callback;
    private TwitterEngine mTwitter;


    public LocationLoader(AppSettings callback) {
        super();
        this.callback = new WeakReference<>(callback);
        mTwitter = TwitterEngine.getInstance(callback);
    }


    @Override
    protected List<TrendLocation> doInBackground(Void[] v) {
        try {
            return mTwitter.getLocations();
        } catch (EngineException twException) {
            this.twException = twException;
        }
        return null;
    }


    @Override
    protected void onPostExecute(List<TrendLocation> locations) {
        if (callback.get() != null) {
            if (locations != null) {
                callback.get().setLocationData(locations);
            } else {
                callback.get().onError(twException);
            }
        }
    }
}