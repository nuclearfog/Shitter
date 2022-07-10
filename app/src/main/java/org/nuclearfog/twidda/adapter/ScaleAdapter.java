package org.nuclearfog.twidda.adapter;

import static org.nuclearfog.twidda.database.GlobalSettings.SCALES;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.ui.activities.SettingsActivity;

import java.util.Locale;

/**
 * list adapter to show font scales
 *
 * @author nuclearfog
 * @see SettingsActivity
 */
public class ScaleAdapter extends BaseAdapter {

    private GlobalSettings settings;


    public ScaleAdapter(GlobalSettings settings) {
        this.settings = settings;
    }


    @Override
    public long getItemId(int pos) {
        return pos;
    }


    @Override
    public int getCount() {
        return SCALES.length;
    }


    @Override
    public Float getItem(int pos) {
        return SCALES[pos];
    }


    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        TextView textItem;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.item_dropdown, parent, false);
        }
        textItem = view.findViewById(R.id.dropdown_textitem);
        textItem.setText(String.format(Locale.getDefault(), "%.1f X", SCALES[pos]));
        textItem.setTypeface(settings.getTypeFace());
        textItem.setTextColor(settings.getFontColor());
        textItem.setBackgroundColor(settings.getCardColor());
        view.setBackgroundColor(settings.getBackgroundColor());
        return view;
    }
}