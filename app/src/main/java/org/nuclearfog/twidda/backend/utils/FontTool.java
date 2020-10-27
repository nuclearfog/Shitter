package org.nuclearfog.twidda.backend.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * Class for converting all fonts in a view
 */
public final class FontTool {

    private FontTool() {
    }

    /**
     * Set fonts type & color to all text elements in a view
     *
     * @param settings current font settings
     * @param v        Root view containing views
     */
    public static void setViewFontAndColor(GlobalSettings settings, View v) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            for (int pos = 0; pos < group.getChildCount(); pos++) {
                View child = group.getChildAt(pos);
                if (child instanceof ViewGroup)
                    setViewFontAndColor(settings, child);
                else if (child instanceof TextView) {
                    TextView tv = (TextView) child;
                    tv.setTypeface(settings.getFontFace());
                    tv.setTextColor(settings.getFontColor());
                }
            }
        }
    }

    /**
     * Set fonts to all text elements in a view
     *
     * @param settings current font settings
     * @param v        Root view containing views
     */
    public static void setViewFont(GlobalSettings settings, View v) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            for (int pos = 0; pos < group.getChildCount(); pos++) {
                View child = group.getChildAt(pos);
                if (child instanceof ViewGroup)
                    setViewFont(settings, child);
                else if (child instanceof TextView) {
                    TextView tv = (TextView) child;
                    tv.setTypeface(settings.getFontFace());
                }
            }
        }
    }
}