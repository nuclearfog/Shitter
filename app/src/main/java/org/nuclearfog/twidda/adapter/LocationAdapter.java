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
 * @author nuclearfog
 * @see org.nuclearfog.twidda.activity.AppSettings
 */
public class LocationAdapter extends BaseAdapter {

    /**
     * item text padding
     */
    private static final int TEXT_PADDING = 20;

    /**
     * item text padding to the next text item
     */
    private static final int TEXT_PADDING_BOTTOM = 5;

    /**
     * text size of the items
     */
    private static final float TEXT_SIZE = 16.0f;

    /**
     * Background color transparency mask
     */
    private static final int TRANSPARENCY_MASK = 0xbfffffff;

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
        TextView textItem;
        if (view instanceof TextView) {
            textItem = (TextView) view;
        } else {
            textItem = new TextView(parent.getContext());
            textItem.setTextSize(COMPLEX_UNIT_DIP, TEXT_SIZE);
            textItem.setPadding(TEXT_PADDING, 0, TEXT_PADDING, TEXT_PADDING_BOTTOM);
        }
        textItem.setBackgroundColor(settings.getBackgroundColor() & TRANSPARENCY_MASK);
        textItem.setTextColor(settings.getFontColor());
        textItem.setTypeface(settings.getTypeFace());
        textItem.setText(data.get(pos).getName());
        return textItem;
    }
}