package org.nuclearfog.twidda.backend.utils;

import android.content.res.ColorStateList;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.database.GlobalSettings;

import static android.graphics.PorterDuff.Mode.SRC_ATOP;

/**
 * Class to set up all TetView preferences
 */
public final class AppStyles {

    private static final int[][] SWITCH_STATES = {{0}};

    private AppStyles() {
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
                if (child instanceof ViewGroup) {
                    setViewFontAndColor(settings, child);
                } else if (child instanceof SwitchButton) {
                    SwitchButton sw = (SwitchButton) child;
                    int[] color = {settings.getFontColor()};
                    sw.setTintColor(settings.getHighlightColor());
                    sw.setThumbColor(new ColorStateList(SWITCH_STATES, color));
                } else if (child instanceof TextView) {
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

    /**
     * set icon drawable color
     *
     * @param tv    TextView with a drawable icon on the left side
     * @param color new color for the drawable
     */
    public static void setIconColor(TextView tv, int color) {
        for (Drawable d : tv.getCompoundDrawables()) {
            if (d != null) {
                d.mutate().setColorFilter(new PorterDuffColorFilter(color, SRC_ATOP));
            }
        }
    }

    /**
     * set up seek bar color
     *
     * @param settings global settings instance
     * @param seekBar  seek bar to color
     */
    public static void setSeekBarColor(GlobalSettings settings, SeekBar seekBar) {
        seekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(settings.getHighlightColor(), SRC_ATOP));
        seekBar.getThumb().setColorFilter(new PorterDuffColorFilter(settings.getFontColor(), SRC_ATOP));
    }
}