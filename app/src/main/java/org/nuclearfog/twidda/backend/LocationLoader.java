package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.items.TrendLocation;
import org.nuclearfog.twidda.window.AppSettings;

import java.lang.ref.WeakReference;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Background task to load location information used by twitter
 */
public class LocationLoader extends AsyncTask<Void, Void, List<TrendLocation>> {

    @Nullable
    private TwitterEngine.EngineException twException;
    private WeakReference<AppSettings> ui;
    private TwitterEngine mTwitter;


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
                ArrayAdapter<TrendLocation> adapter = ui.get().getAdapter();
                adapter.clear();
                adapter.addAll(locations);
                adapter.notifyDataSetChanged();
                ui.get().setWoeIdSelection();
            } else if (twException != null) {
                Toast.makeText(ui.get(), twException.getMessageResource(), LENGTH_SHORT).show();
            }
        }
    }
}