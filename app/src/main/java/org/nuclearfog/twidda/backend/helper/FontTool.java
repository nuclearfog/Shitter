package org.nuclearfog.twidda.backend.helper;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Class for converting all fonts in a view
 */
public abstract class FontTool {

    /**
     * Set fonts to all text elements in a view
     *
     * @param v    Root view containing views
     * @param font Font type
     */
    public static void setViewFont(View v, Typeface font) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            for (int pos = 0; pos < group.getChildCount(); pos++) {
                View child = group.getChildAt(pos);
                if (child instanceof ViewGroup)
                    setViewFont(child, font);
                else if (child instanceof TextView)
                    ((TextView) child).setTypeface(font);
            }
        }
    }
}