package org.nuclearfog.twidda.backend.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.cardview.widget.CardView;

import com.google.android.material.tabs.TabLayout;
import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.graphics.PorterDuff.Mode.SRC_ATOP;

/**
 * Class to set up all TetView preferences
 */
public final class AppStyles {

    private static final int[][] SWITCH_STATES = {{0}};
    private GlobalSettings settings;

    private AppStyles(GlobalSettings settings) {
        this.settings = settings;
    }

    /**
     * @param settings settings instance
     * @param v        root view
     */
    public static void setTheme(GlobalSettings settings, View v) {
        AppStyles instance = new AppStyles(settings);
        if (v instanceof CardView) {
            CardView card = (CardView) v;
            card.setCardBackgroundColor(settings.getCardColor());
        } else {
            v.setBackgroundColor(settings.getBackgroundColor());
        }
        instance.setTheme(v);
    }

    /**
     * Set fonts type & color to all text elements in a view
     *
     * @param v recursive view
     */
    private void setTheme(View v) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            for (int pos = 0; pos < group.getChildCount(); pos++) {
                View child = group.getChildAt(pos);
                if (child instanceof TabLayout) {
                    TabLayout tablayout = (TabLayout) child;
                    tablayout.setSelectedTabIndicatorColor(settings.getHighlightColor());
                } else if (child instanceof SwitchButton) {
                    SwitchButton sw = (SwitchButton) child;
                    int[] color = {settings.getIconColor()};
                    sw.setTintColor(settings.getHighlightColor());
                    sw.setThumbColor(new ColorStateList(SWITCH_STATES, color));
                } else if (child instanceof TextView) {
                    TextView tv = (TextView) child;
                    tv.setTypeface(settings.getFontFace());
                    tv.setTextColor(settings.getFontColor());
                    if (!(child instanceof EditText))
                        setIconColor(tv, settings.getIconColor());
                } else if (child instanceof ImageView) {
                    ImageView img = (ImageView) child;
                    setDrawableColor(img.getDrawable(), settings.getIconColor());
                } else if (child instanceof ViewGroup) {
                    if (child instanceof CardView) {
                        CardView card = (CardView) child;
                        card.setCardBackgroundColor(settings.getCardColor());
                    }
                    setTheme(child);
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
            setDrawableColor(d, color);
        }
    }

    /**
     * sets menu icon color
     *
     * @param m     menu with icons
     * @param color new icon color
     */
    public static void setMenuIconColor(Menu m, int color) {
        for (int index = 0; index < m.size(); index++) {
            setMenuItemColor(m.getItem(index), color);
        }
    }

    /**
     * sets color of a single menu item
     *
     * @param item  menu item with a drawable
     * @param color new icon color
     */
    public static void setMenuItemColor(MenuItem item, int color) {
        Drawable d = item.getIcon();
        if (d != null) {
            setDrawableColor(d, color);
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
        seekBar.getThumb().setColorFilter(new PorterDuffColorFilter(settings.getIconColor(), SRC_ATOP));
    }

    /**
     * set Tab icons for TabLayout
     */
    public static void setTabIcons(TabLayout tabLayout, GlobalSettings settings, @ArrayRes int array) {
        Context context = tabLayout.getContext();
        TypedArray tArray = context.getResources().obtainTypedArray(array);
        for (int index = 0; index < tArray.length(); index++) {
            TabLayout.Tab mTab = tabLayout.getTabAt(index);
            if (mTab != null) {
                Drawable icon = tArray.getDrawable(index);
                setDrawableColor(icon, settings.getIconColor());
                mTab.setIcon(icon);
            }
        }
        tArray.recycle();
    }


    public static TextView[] createTabIcon(TabLayout tabLayout, GlobalSettings settings, @ArrayRes int array) {
        Context context = tabLayout.getContext();
        TypedArray tArray = context.getResources().obtainTypedArray(array);
        TextView[] tabs = new TextView[tArray.length()];
        for (int index = 0; index < tArray.length(); index++) {
            TabLayout.Tab mTab = tabLayout.getTabAt(index);
            if (mTab != null) {
                Drawable icon = tArray.getDrawable(index);
                setDrawableColor(icon, settings.getIconColor());
                View v = View.inflate(context, R.layout.icon_profile_tab, null);
                ImageView imageIcon = v.findViewById(R.id.tab_icon);
                tabs[index] = v.findViewById(R.id.tab_text);
                tabs[index].setTextColor(settings.getFontColor());
                tabs[index].setTypeface(settings.getFontFace());
                imageIcon.setImageDrawable(icon);
                mTab.setCustomView(v);
            }
        }
        tArray.recycle();
        return tabs;
    }

    /**
     * color drawable
     *
     * @param drawable drawables
     * @param color    new drawable color
     */
    private static void setDrawableColor(Drawable drawable, int color) {
        if (drawable != null) {
            drawable.mutate().setColorFilter(new PorterDuffColorFilter(color, SRC_ATOP));
        }
    }
}