package org.nuclearfog.twidda.adapter;

import static org.nuclearfog.twidda.database.GlobalSettings.FONTS;
import static org.nuclearfog.twidda.database.GlobalSettings.FONT_NAMES;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * Spinner Adapter for font settings
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.activities.AppSettings
 */
public class FontAdapter extends BaseAdapter {

    private GlobalSettings settings;

    /**
     * @param settings app settings for background and font color
     */
    public FontAdapter(GlobalSettings settings) {
        this.settings = settings;
    }


    @Override
    public int getCount() {
        return FONTS.length;
    }


    @Override
    public long getItemId(int pos) {
        return pos;
    }


    @Override
    public Typeface getItem(int pos) {
        return FONTS[pos];
    }


    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        TextView textItem;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.item_dropdown, parent, false);
        }
        textItem = view.findViewById(R.id.dropdown_textitem);
        textItem.setText(FONT_NAMES[pos]);
        textItem.setTypeface(FONTS[pos]);
        textItem.setTextColor(settings.getFontColor());
        textItem.setBackgroundColor(settings.getCardColor());
        view.setBackgroundColor(settings.getBackgroundColor());
        return view;
    }
}