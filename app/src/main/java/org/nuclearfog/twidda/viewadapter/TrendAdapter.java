package org.nuclearfog.twidda.viewadapter;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.window.ColorPreferences;
import org.nuclearfog.twidda.database.TrendDatabase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TrendAdapter extends ArrayAdapter {
    private TrendDatabase trend;
    private LayoutInflater inf;
    private  int background;

    public TrendAdapter(Context context, TrendDatabase trend) {
        super(context, R.layout.trend);
        inf=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ColorPreferences mcolor = ColorPreferences.getInstance(context);
        background = mcolor.getColor(ColorPreferences.BACKGROUND);
        this.trend = trend;
    }

    public TrendDatabase getDatabase() {
        return trend;
    }

    @Override
    public int getCount() {
        return trend.getSize();
    }

    @NonNull
    @Override
    public View getView(int position, View v, @NonNull ViewGroup parent) {
        if(v == null) {
            v = inf.inflate(R.layout.trend, parent,false);
            v.setBackgroundColor(background);
        }
        String trendName = trend.getTrendname(position);
        ((TextView) v.findViewById(R.id.trendpos)).setText(Integer.toString(position)+'.');
        ((TextView) v.findViewById(R.id.trendname)).setText(trendName);
        return v;
    }
}