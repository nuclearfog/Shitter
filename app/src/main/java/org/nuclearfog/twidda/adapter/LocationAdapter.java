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
 *
 * @see org.nuclearfog.twidda.activity.AppSettings
 */
public class LocationAdapter extends BaseAdapter {

    private static final int TEXT_PADDING = 20;
    private static final float TEXT_SIZE = 16.0f;

    private GlobalSettings settings;

    private List<TrendLocation> data = new ArrayList<>();


    public LocationAdapter(GlobalSettings settings) {
        this.settings = settings;
    }


    /**
     * Add a single item to top
     *
     * @param top top item to add
     */
    @MainThread
    public void addTop(TrendLocation top) {
        data.add(top);
        notifyDataSetChanged();
    }

    /**
     * replace content with new items
     *
     * @param newData item list
     */
    @MainThread
    public void setData(List<TrendLocation> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    /**
     * get position of the item or "0" if not found
     *
     * @param item item to search
     * @return index of the item
     */
    public int getPosition(TrendLocation item) {
        int pos = data.indexOf(item);
        if (pos == -1) {
            return 0;
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
        if (view instanceof TextView) {
            tv = (TextView) view;
        } else {
            tv = new TextView(parent.getContext());
            tv.setTextSize(COMPLEX_UNIT_DIP, TEXT_SIZE);
            tv.setPadding(TEXT_PADDING, 0, TEXT_PADDING, 0);
            tv.setTextColor(settings.getFontColor());
            tv.setTypeface(settings.getFontFace());
        }
        tv.setText(data.get(pos).getName());
        return tv;
    }
}