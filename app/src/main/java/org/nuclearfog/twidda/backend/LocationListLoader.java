package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.AppSettings;
import org.nuclearfog.twidda.backend.items.TrendLocation;

import java.lang.ref.WeakReference;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Background task to load location information used by twitter such as location names and world ID's
 * @see AppSettings
 */
public class LocationListLoader extends AsyncTask<Void, Void, List<TrendLocation>> {

    @Nullable
    private TwitterEngine.EngineException twException;
    private WeakReference<AppSettings> ui;
    private TwitterEngine mTwitter;


    public LocationListLoader(AppSettings context) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
    }


    @Override
    protected List<TrendLocation> doInBackground(Void[] v) {
        try {
            return mTwitter.getLocations();
        } catch (TwitterEngine.EngineException twException) {
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
                ui.get().setLocationData(locations);
            } else if (twException != null) {
                Toast.makeText(ui.get(), twException.getMessageResource(), LENGTH_SHORT).show();
            }
        }
    }
}