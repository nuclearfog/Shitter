package org.nuclearfog.twidda.ViewAdapter;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.DataBase.TrendDatabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TrendsAdapter extends ArrayAdapter {
    private TrendDatabase trend;
    private Context context;

    public TrendsAdapter(Context context, TrendDatabase trend) {
        super(context, R.layout.trend);//test
        this.trend = trend;
        this.context = context;
    }

    @Override
    public int getCount() {
        return trend.getSize();
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if(v == null) {
            LayoutInflater inf=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.trend, parent,false);
        }
        String trendName = trend.getTrendname(position);
        ((TextView) v.findViewById(R.id.trendname)).setText(trendName);
        return v;
    }
}