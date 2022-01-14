package org.nuclearfog.twidda.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.MainThread;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for Location selection spinner
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.activities.AppSettings
 */
public class LocationAdapter extends BaseAdapter {

    private GlobalSettings settings;
    private List<Location> data = new ArrayList<>();


    public LocationAdapter(GlobalSettings settings) {
        this.settings = settings;
    }


    /**
     * Add a single item to top
     *
     * @param top top item to add
     */
    @MainThread
    public void addTop(Location top) {
        data.add(top);
        notifyDataSetChanged();
    }

    /**
     * replace content with new items
     *
     * @param newData item list
     */
    @MainThread
    public void setData(List<Location> newData) {
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
    public int getPosition(Location item) {
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
    public Location getItem(int pos) {
        return data.get(pos);
    }


    @Override
    public long getItemId(int pos) {
        return getItem(pos).getId();
    }


    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        TextView textItem;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.item_dropdown, parent, false);
        }
        textItem = view.findViewById(R.id.dropdown_textitem);
        textItem.setBackgroundColor(settings.getCardColor());
        textItem.setTextColor(settings.getFontColor());
        textItem.setTypeface(settings.getTypeFace());
        textItem.setText(data.get(pos).getName());
        view.setBackgroundColor(settings.getBackgroundColor());
        return view;
    }
}