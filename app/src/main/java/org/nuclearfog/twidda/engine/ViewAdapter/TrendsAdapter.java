package org.nuclearfog.twidda.engine.ViewAdapter;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.engine.TrendDatabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class TrendsAdapter extends ArrayAdapter {
    private TrendDatabase trend;
    private Context c;

    public TrendsAdapter(Context c, int layout, TrendDatabase trend) {
        super(c, layout);
        this.trend = trend;
        this.c = c;
    }

    @Override
    public int getCount() {
        return trend.getSize();
    }

    @Override
    public Object getItem(int position) {
        return trend.getTrendname(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(v == null) {
            LayoutInflater inf=(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.trend, null);
        }
        String trendName = trend.getTrendname(position);
        ((TextView) v.findViewById(R.id.trendname)).setText(trendName);
        return v;
    }
}
