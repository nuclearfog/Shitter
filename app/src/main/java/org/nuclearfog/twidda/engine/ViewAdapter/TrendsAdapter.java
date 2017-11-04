package org.nuclearfog.twidda.engine.ViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.nuclearfog.twidda.R;

import java.util.List;

import twitter4j.Location;
import twitter4j.ResponseList;
import twitter4j.Trend;
import twitter4j.Trends;

public class TrendsAdapter extends ArrayAdapter {

    private Trend[] list;
    private Context c;

    public TrendsAdapter(Context c, int layout, Trend[] list){
        super(c, layout);
        this.list = list;
        this.c = c;
    }

    @Override
    public int getCount() {
        return list.length;
    }

    @Override
    public Object getItem(int position) {
        return list[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();

        if(v == null) {
            LayoutInflater inf=(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.trend, null);
        }

        String trendName = list[position].getName();
        ((TextView) v.findViewById(R.id.trendname)).setText(trendName);
        return v;
    }

}
