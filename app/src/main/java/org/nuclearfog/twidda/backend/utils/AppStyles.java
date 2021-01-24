package org.nuclearfog.twidda.backend.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.CropTransformation;

import static android.graphics.Bitmap.Config.ARGB_8888;
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
     * sets view theme
     *
     * @param settings settings instance
     * @param v        Root view
     */
    public static void setTheme(GlobalSettings settings, View v) {
        setTheme(settings, v, settings.getBackgroundColor());
    }

    /**
     * sets view theme with background color
     *
     * @param settings        settings instance
     * @param v               Root view
     * @param backgroundColor custom background color
     */
    public static void setTheme(GlobalSettings settings, View v, int backgroundColor) {
        AppStyles instance = new AppStyles(settings);
        if (v instanceof CardView) {
            CardView card = (CardView) v;
            card.setCardBackgroundColor(instance.settings.getCardColor());
        } else {
            v.setBackgroundColor(backgroundColor);
        }
        instance.setSubViewTheme(v);
    }

    /**
     * Set fonts type & color to all text elements in a view
     *
     * @param v recursive view
     */
    private void setSubViewTheme(View v) {
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
                    setIconColor(tv, settings.getIconColor());
                    if (child instanceof Button) {
                        Button btn = (Button) child;
                        setButtonColor(btn, settings.getFontColor());
                    }
                } else if (child instanceof ImageView) {
                    ImageView img = (ImageView) child;
                    setDrawableColor(img.getDrawable(), settings.getIconColor());
                    if (child instanceof ImageButton) {
                        ImageButton btn = (ImageButton) child;
                        setButtonColor(btn, settings.getFontColor());
                    }
                } else if (child instanceof ViewGroup) {
                    if (child instanceof CardView) {
                        CardView card = (CardView) child;
                        card.setCardBackgroundColor(settings.getCardColor());
                        setSubViewTheme(child);
                    } else if (!(child instanceof ViewPager)) {
                        setSubViewTheme(child);
                    }
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
     * set icon drawable color
     *
     * @param imgView ImageView with a drawable icon
     * @param color   new color for the drawable
     */
    public static void setIconColor(ImageView imgView, int color) {
        Drawable d = imgView.getDrawable();
        setDrawableColor(d, color);
    }

    /**
     * sets button background color
     *
     * @param button Button with background drawable
     * @param color  background color
     */
    public static void setButtonColor(Button button, int color) {
        Drawable d = button.getBackground();
        if (d instanceof StateListDrawable) {
            setDrawableColor(d, color);
        }
    }

    /**
     * sets button background color
     *
     * @param button Button with background drawable
     * @param color  background color
     */
    public static void setButtonColor(ImageButton button, int color) {
        Drawable d = button.getBackground();
        if (d instanceof StateListDrawable) {
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
     * set tab icons
     *
     * @param tabLayout tablayout to set tab icons
     * @param settings  settings to set color
     */
    public static void setTabIcons(TabLayout tabLayout, GlobalSettings settings, @ArrayRes int array) {
        Context context = tabLayout.getContext();
        TypedArray tArray = context.getResources().obtainTypedArray(array);
        for (int index = 0; index < tArray.length(); index++) {
            TabLayout.Tab mTab = tabLayout.getTabAt(index);
            if (mTab != null) {
                int resId = tArray.getResourceId(index, 0);
                Drawable icon = AppCompatResources.getDrawable(context, resId);
                setDrawableColor(icon, settings.getIconColor());
                mTab.setIcon(icon);
            }
        }
        tArray.recycle();
    }

    /**
     * create tab icons with TextView
     *
     * @param tabLayout TabLayout to set the icons
     * @param settings  settings instance
     * @param array     Array of drawable resources to set the icons
     * @return array of TextViews
     */
    public static TextView[] createTabIcon(TabLayout tabLayout, GlobalSettings settings, @ArrayRes int array) {
        Context context = tabLayout.getContext();
        TypedArray tArray = context.getResources().obtainTypedArray(array);
        TextView[] tabs = new TextView[tArray.length()];
        for (int index = 0; index < tArray.length(); index++) {
            TabLayout.Tab mTab = tabLayout.getTabAt(index);
            if (mTab != null) {
                int resId = tArray.getResourceId(index, 0);
                Drawable icon = AppCompatResources.getDrawable(context, resId);
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
     * setup a transparent blurry toolbar
     *
     * @param activity          activity reference to get the measures
     * @param background        background overlapped by the toolbar at the top
     * @param toolbarBackground background image of the toolbar
     */
    public static void setToolbarBackground(Activity activity, ImageView background, ImageView toolbarBackground) {
        Point displaySize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
        float toolbarRatio = displaySize.x / activity.getResources().getDimension(R.dimen.profile_toolbar_height);
        Bitmap image = ((BitmapDrawable) background.getDrawable()).getBitmap();

        BlurTransformation blur = new BlurTransformation(background.getContext(), 5);
        CropTransformation crop = new CropTransformation(image.getWidth(), (int) (image.getWidth() / toolbarRatio),
                CropTransformation.GravityHorizontal.CENTER, CropTransformation.GravityVertical.TOP);

        Bitmap result = blur.transform(crop.transform(image.copy(ARGB_8888, true)));
        toolbarBackground.setImageBitmap(result);
    }

    /**
     * color drawable
     *
     * @param drawable drawables
     * @param color    new drawable color
     */
    private static void setDrawableColor(@Nullable Drawable drawable, int color) {
        if (drawable != null) {
            drawable.mutate().setColorFilter(new PorterDuffColorFilter(color, SRC_ATOP));
        }
    }
}