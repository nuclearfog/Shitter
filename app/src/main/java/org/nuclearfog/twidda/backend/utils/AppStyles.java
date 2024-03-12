package org.nuclearfog.twidda.backend.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.kyleduo.switchbutton.SwitchButton;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.config.GlobalSettings;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.CropTransformation;
import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal;
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical;

/**
 * Theme class provides methods to set view styles and colors
 *
 * @author nuclearfog
 */
public class AppStyles {

	/**
	 * transparency mask for hint text color
	 */
	private static final int HINT_TRANSPARENCY = 0x6fffffff;
	private static final int[][] SWITCH_STATES = {{0}};
	private GlobalSettings settings;

	/**
	 *
	 */
	private AppStyles(Context context) {
		this.settings = GlobalSettings.get(context);
	}

	/**
	 * sets view theme with default background color
	 *
	 * @param root Root view container
	 */
	public static void setTheme(ViewGroup root) {
		AppStyles instance = new AppStyles(root.getContext());
		root.setBackgroundColor(instance.settings.getBackgroundColor());
		instance.setSubViewTheme(root, instance.settings.getBackgroundColor());
	}

	/**
	 * sets view theme with custom background color
	 *
	 * @param root       Root view container
	 * @param background custom background color
	 */
	public static void setTheme(ViewGroup root, @ColorInt int background) {
		AppStyles instance = new AppStyles(root.getContext());
		root.setBackgroundColor(background);
		instance.setSubViewTheme(root, background);
	}

	/**
	 * sets view theme with background color
	 *
	 * @param root       Root view container
	 * @param background Background image view
	 */
	public static void setEditorTheme(ViewGroup root, ImageView background) {
		AppStyles instance = new AppStyles(root.getContext());
		instance.setSubViewTheme(root, instance.settings.getPopupColor());
		setDrawableColor(background, instance.settings.getPopupColor());
	}

	/**
	 * sets view font style
	 *
	 * @param root Root view container
	 */
	public static void setFontStyle(ViewGroup root) {
		AppStyles instance = new AppStyles(root.getContext());
		instance.setViewFont(root);
	}

	/**
	 * set global font scale
	 *
	 * @param context base context
	 * @return context with new configuration
	 */
	public static Context setFontScale(Context context) {
		AppStyles instance = new AppStyles(context);
		Configuration config = context.getResources().getConfiguration();
		config.fontScale = instance.settings.getTextScale();
		return context.createConfigurationContext(config);
	}

	/**
	 * update global font size
	 */
	public static void updateFontScale(Context context) {
		AppStyles instance = new AppStyles(context);
		Resources resources = context.getResources();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		if (wm != null) {
			Configuration configuration = resources.getConfiguration();
			configuration.fontScale = instance.settings.getTextScale();
			DisplayMetrics metrics = resources.getDisplayMetrics();
			wm.getDefaultDisplay().getMetrics(metrics);
			metrics.scaledDensity = configuration.fontScale * metrics.density;
			resources.updateConfiguration(configuration, metrics);
		}
	}

	/**
	 * set icon drawable color
	 *
	 * @param tv    TextView with a drawable icon on the left side
	 * @param color new color for the drawable
	 */
	public static void setDrawableColor(TextView tv, int color) {
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
	public static void setDrawableColor(ImageView imgView, int color) {
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
	 * set Toolbar overflow icon color
	 *
	 * @param toolbar Toolbar with overflow icon
	 * @param color   icon color
	 */
	public static void setOverflowIcon(Toolbar toolbar, int color) {
		Drawable groupIcon = ResourcesCompat.getDrawable(toolbar.getResources(), R.drawable.threedots, null);
		setDrawableColor(groupIcon, color);
		toolbar.setOverflowIcon(groupIcon);
	}

	/**
	 * sets progress circle color
	 *
	 * @param circle progress circle
	 * @param color  highlight color
	 */
	public static void setProgressColor(ProgressBar circle, int color) {
		Drawable icon = circle.getIndeterminateDrawable();
		if (icon != null) {
			icon.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
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
	 * setup a transparent blurry toolbar
	 *
	 * @param background        background overlapped by the toolbar at the top
	 * @param toolbarBackground background image of the toolbar
	 */
	public static void setToolbarBackground(ImageView background, ImageView toolbarBackground) {
		Drawable backgroundDrawable = background.getDrawable();
		if (backgroundDrawable instanceof BitmapDrawable) {
			try {
				Bitmap image = ((BitmapDrawable) backgroundDrawable).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
				// check if image is valid
				if (image.getWidth() > 0 && image.getHeight() > 0) {
					// crop image to background size
					if (background.getMeasuredHeight() > 0 && background.getMeasuredWidth() > 0) {
						CropTransformation crop;
						int width, height;
						if ((image.getWidth() / (float) image.getHeight() < (background.getWidth() / (float) background.getHeight()))) {
							height = image.getWidth() * background.getMeasuredHeight() / background.getMeasuredWidth();
							width = image.getWidth();
						} else if ((image.getWidth() / (float) image.getHeight() > (background.getWidth() / (float) background.getHeight()))) {
							height = image.getHeight();
							width = image.getHeight() * background.getMeasuredWidth() / background.getMeasuredHeight();
						} else {
							width = image.getWidth();
							height = image.getHeight();
						}
						crop = new CropTransformation(width, height, GravityHorizontal.CENTER, GravityVertical.CENTER);
						image = crop.transform(image);
					}
					int widthPixels = Resources.getSystem().getDisplayMetrics().widthPixels;
					if (Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
						widthPixels /= 2;
					int blurRadius = Math.max(Math.round((image.getWidth() * 20.0f) / widthPixels), 10);
					float toolbarRatio = background.getResources().getDimension(R.dimen.profile_toolbar_height) / widthPixels;
					// do final transformations (crop first image to toolbar background size, then blur)
					BlurTransformation blur = new BlurTransformation(background.getContext(), blurRadius);
					CropTransformation crop = new CropTransformation(image.getWidth(), (int) (image.getWidth() * toolbarRatio), GravityHorizontal.CENTER, GravityVertical.TOP);
					image = blur.transform(crop.transform(image));
					toolbarBackground.setImageBitmap(image);
				}
			} catch (Exception exception) {
				// exception may occur when there is not enough free memory
				// reset toolbar background
				if (BuildConfig.DEBUG)
					exception.printStackTrace();
				toolbarBackground.setImageResource(0);
			}
		}
	}

	/**
	 * set up seek bar color
	 *
	 * @param seekBar  seek bar to color
	 * @param settings global settings instance
	 */
	public static void setSeekBarColor(SeekBar seekBar, GlobalSettings settings) {
		seekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(settings.getHighlightColor(), PorterDuff.Mode.SRC_IN));
		if (seekBar.getThumb() != null) {
			seekBar.getThumb().setColorFilter(new PorterDuffColorFilter(settings.getIconColor(), PorterDuff.Mode.SRC_IN));
		}
	}

	/**
	 * color drawable
	 *
	 * @param drawable drawables
	 * @param color    new drawable color
	 */
	public static void setDrawableColor(@Nullable Drawable drawable, int color) {
		if (drawable != null && !(drawable instanceof BitmapDrawable)) {
			drawable.mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
		}
	}

	/**
	 * set color button drawable with corner and text
	 */
	public static void setColorButton(Button button, int color) {
		Drawable d = button.getBackground();
		GradientDrawable gradient;
		Resources resources = button.getResources();
		int width = (int) resources.getDimension(R.dimen.settings_color_button_stroke_width);
		int invColor = (color | Color.BLACK) ^ 0xffffff;
		if (d instanceof GradientDrawable) {
			gradient = (GradientDrawable) d;
		} else {
			float radius = resources.getDimension(R.dimen.settings_color_button_corner_radius);
			gradient = new GradientDrawable();
			gradient.setCornerRadius(radius);
			button.setBackground(gradient);
		}
		gradient.setColor(color);
		gradient.setStroke(width, invColor);
		button.setTextColor(invColor);
	}

	/**
	 * sets {@link SwipeRefreshLayout} theme
	 */
	public static void setSwipeRefreshColor(SwipeRefreshLayout reload, GlobalSettings settings) {
		reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
		reload.setColorSchemeColors(settings.getIconColor());
	}

	/**
	 * parsing all views from a sub ViewGroup recursively and set all colors and fonts
	 *
	 * @param group current ViewGroup to parse for sub views
	 * @param color root background color
	 */
	private void setSubViewTheme(ViewGroup group, int color) {
		for (int pos = 0; pos < group.getChildCount(); pos++) {
			View child = group.getChildAt(pos);
			if (child instanceof SwitchButton) {
				SwitchButton sw = (SwitchButton) child;
				int[] thumbColor = {settings.getIconColor()};
				sw.setTintColor(settings.getHighlightColor());
				sw.setThumbColor(new ColorStateList(SWITCH_STATES, thumbColor));
			} else if (child instanceof FloatingActionButton) {
				FloatingActionButton floatingButton = (FloatingActionButton) child;
				floatingButton.setBackgroundTintList(ColorStateList.valueOf(settings.getPopupColor()));
				setDrawableColor(floatingButton, settings.getIconColor());
			} else if (child instanceof SeekBar) {
				SeekBar seekBar = (SeekBar) child;
				setSeekBarColor(seekBar, settings);
			} else if (child instanceof Spinner) {
				Spinner dropdown = (Spinner) child;
				dropdown.setPopupBackgroundDrawable(new ColorDrawable(color));
				setDrawableColor(dropdown.getBackground(), settings.getIconColor());
			} else if (child instanceof TextView) {
				TextView tv = (TextView) child;
				tv.setTypeface(settings.getTypeFace());
				tv.setTextColor(settings.getTextColor());
				setDrawableColor(tv, settings.getIconColor());
				if (child instanceof Button) {
					Button btn = (Button) child;
					setButtonColor(btn, settings.getTextColor());
				} else if (child instanceof EditText) {
					EditText edit = (EditText) child;
					edit.setHintTextColor(settings.getTextColor() & HINT_TRANSPARENCY);
				}
			} else if (child instanceof ImageView) {
				ImageView img = (ImageView) child;
				setDrawableColor(img.getDrawable(), settings.getIconColor());
				if (child instanceof ImageButton) {
					ImageButton btn = (ImageButton) child;
					setButtonColor(btn, settings.getTextColor());
				}
			} else if (child instanceof ViewGroup) {
				if (child instanceof CardView) {
					CardView card = (CardView) child;
					card.setCardBackgroundColor(settings.getCardColor());
					setSubViewTheme(card, color);
				} else if (child instanceof NavigationView) {
					NavigationView navigationView = (NavigationView) child;
					navigationView.setBackgroundColor(settings.getBackgroundColor());
					navigationView.setItemTextColor(ColorStateList.valueOf(settings.getTextColor()));
				} else if (!(child instanceof ViewPager2)) {
					setSubViewTheme((ViewGroup) child, color);
				}
			}
		}
	}

	/**
	 * Set fonts to all text elements in a view
	 *
	 * @param group current ViewGroup to parse for sub views
	 */
	private void setViewFont(ViewGroup group) {
		for (int pos = 0; pos < group.getChildCount(); pos++) {
			View child = group.getChildAt(pos);
			if (child instanceof ViewGroup)
				setViewFont((ViewGroup) child);
			else if (child instanceof TextView) {
				TextView tv = (TextView) child;
				tv.setTypeface(settings.getTypeFace());
			}
		}
	}
}