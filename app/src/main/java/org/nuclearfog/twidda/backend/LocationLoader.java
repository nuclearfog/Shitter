package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.TrendLocation;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.window.AppSettings;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;

public class LocationLoader extends AsyncTask<Void, Void, List<TrendLocation>> {

    private WeakReference<AppSettings> ui;
    private GlobalSettings settings;
    private TwitterEngine mTwitter;
    private TwitterException err;


    public LocationLoader(AppSettings context) {
        ui = new WeakReference<>(context);
        settings = GlobalSettings.getInstance(context);
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
                Spinner woeId = ui.get().findViewById(R.id.woeid);
                ArrayAdapter adapter = (ArrayAdapter) woeId.getAdapter();
                adapter.clear();
                adapter.addAll(locations);
                adapter.notifyDataSetChanged();
                int position = adapter.getPosition(settings.getTrendLocation());
                woeId.setSelection(position);
            } else if (err != null) {
                ErrorHandler.printError(ui.get(), err);
            }
        }
    }
}