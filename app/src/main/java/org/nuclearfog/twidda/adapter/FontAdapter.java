package org.nuclearfog.twidda.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

/**
 * Spinner Adapter for font settings
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.activity.AppSettings
 */
public class FontAdapter extends BaseAdapter {

    /**
     * font size of an item
     */
    private static final float FONT_SIZE = 24.0f;

    /**
     * android system fonts
     */
    private static final Typeface[] fonts = GlobalSettings.FONTS;

    /**
     * font names of the system fonts
     */
    private static final String[] names = GlobalSettings.FONT_NAMES;

    private GlobalSettings settings;

    /**
     * @param settings app settings for background and font color
     */
    public FontAdapter(GlobalSettings settings) {
        this.settings = settings;
    }


    @Override
    public int getCount() {
        return fonts.length;
    }


    @Override
    public long getItemId(int pos) {
        return getItem(pos).hashCode();
    }


    @Override
    public Typeface getItem(int pos) {
        return fonts[pos];
    }


    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        TextView textItem;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.item_dropdown, parent, false);
        }
        textItem = view.findViewById(R.id.dropdown_textitem);
        textItem.setText(names[pos]);
        textItem.setTypeface(fonts[pos]);
        textItem.setTextSize(COMPLEX_UNIT_SP, FONT_SIZE);
        textItem.setTextColor(settings.getFontColor());
        textItem.setBackgroundColor(settings.getCardColor());
        view.setBackgroundColor(settings.getBackgroundColor());
        return view;
    }
}