package org.nuclearfog.twidda.adapter;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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
     * text padding of an item
     */
    private static final int TEXT_PADDING = 20;

    /**
     * item text padding to the next text item
     */
    private static final int TEXT_PADDING_BOTTOM = 5;

    /**
     * font size of an item
     */
    private static final float FONT_SIZE = 24.0f;

    /**
     * Background color transparency mask
     */
    private static final int TRANSPARENCY_MASK = 0xbfffffff;

    /**
     * android system fonts
     */
    private static final Typeface[] fonts = GlobalSettings.FONTS;

    /**
     * font names of the system fonts
     */
    private static final String[] names = GlobalSettings.FONT_NAMES;

    private GlobalSettings settings;


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
        String name = names[pos];
        Typeface font = fonts[pos];
        if (view instanceof TextView)
            textItem = (TextView) view;
        else {
            textItem = new TextView(parent.getContext());
            textItem.setTextSize(COMPLEX_UNIT_SP, FONT_SIZE);
            textItem.setPadding(TEXT_PADDING, 0, TEXT_PADDING, TEXT_PADDING_BOTTOM);
        }
        textItem.setText(name);
        textItem.setTypeface(font);
        textItem.setTextColor(settings.getFontColor());
        textItem.setBackgroundColor(settings.getBackgroundColor() & TRANSPARENCY_MASK);
        return textItem;
    }
}