package org.nuclearfog.twidda.adapter;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.nuclearfog.twidda.database.GlobalSettings;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

public class FontAdapter extends BaseAdapter {

    private final Typeface[] fonts = GlobalSettings.fonts;
    private final String[] names = GlobalSettings.fontnames;


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
    public View getView(final int pos, View view, ViewGroup parent) {
        TextView tv;
        String name = names[pos];
        final Typeface font = fonts[pos];
        if (view instanceof TextView)
            tv = (TextView) view;
        else {
            tv = new TextView(parent.getContext());
            tv.setTextSize(COMPLEX_UNIT_SP, 20);
        }
        tv.setText(name);
        tv.setTypeface(font);
        return tv;
    }
}