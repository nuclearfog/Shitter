package org.nuclearfog.twidda.adapter;


import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.MainThread;

import org.nuclearfog.twidda.backend.items.TrendLocation;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.ArrayList;
import java.util.List;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

/**
 * Adapter class for Location selection spinner
 */
public class LocationAdapter extends BaseAdapter {

    private List<TrendLocation> data;
    private GlobalSettings settings;


    public LocationAdapter(GlobalSettings settings) {
        data = new ArrayList<>();
        this.settings = settings;
    }


    @MainThread
    public void addTop(TrendLocation top) {
        data.add(top);
        notifyDataSetChanged();
    }


    @MainThread
    public void setData(List<TrendLocation> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }


    public int getPosition(TrendLocation item) {
        int pos = 0;
        for (int index = 0; index < data.size(); index++) {
            if (data.get(index).equals(item))
                pos = index;
        }
        return pos;
    }


    @Override
    public int getCount() {
        return data.size();
    }


    @Override
    public TrendLocation getItem(int pos) {
        return data.get(pos);
    }


    @Override
    public long getItemId(int pos) {
        return getItem(pos).getWoeId();
    }


    @Override
    public View getView(final int pos, View view, ViewGroup parent) {
        TextView tv;
        if (view instanceof TextView)
            tv = (TextView) view;
        else {
            tv = new TextView(parent.getContext());
            tv.setTextSize(COMPLEX_UNIT_DIP, 16);
        }
        tv.setTextColor(settings.getFontColor());
        tv.setTypeface(settings.getFontFace());
        tv.setText(data.get(pos).getName());
        return tv;
    }
}