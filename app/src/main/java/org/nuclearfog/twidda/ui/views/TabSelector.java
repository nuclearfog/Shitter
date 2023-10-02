package org.nuclearfog.twidda.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.VisibilityDelay;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * TabLayout implementation to provide a tab selector
 *
 * @author nuclearfog
 */
public class TabSelector extends LinearLayout implements OnClickListener, OnGlobalLayoutListener, OnPreDrawListener {

	@Nullable
	private ViewPager2 viewPager;
	private LinearLayout tabContainer;
	private View indicator;

	private LayoutParams indicatorParams;

	@Nullable
	private OnTabSelectedListener listener;
	private GlobalSettings settings;

	@IdRes
	private int pagerId;

	private int oldPosition;
	private int tabCount;

	/**
	 * @inheritDoc
	 */
	public TabSelector(Context context) {
		this(context, null);
	}

	/**
	 * @inheritDoc
	 */
	public TabSelector(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		if (attrs != null) {
			TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.TabSelector);
			pagerId = attrArray.getResourceId(R.styleable.TabSelector_viewpager, NO_ID);
			attrArray.recycle();
		}
		setOrientation(VERTICAL);
		settings = GlobalSettings.get(context);
		tabContainer = new LinearLayout(context);
		tabContainer.setOrientation(LinearLayout.HORIZONTAL);
		indicator = new View(context);
		indicatorParams = new LayoutParams(LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.tabs_indicator_height));
		LayoutParams tabContainerParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		tabContainerParam.weight = 1;

		tabContainer.setLayoutParams(tabContainerParam);
		indicator.setLayoutParams(indicatorParams);
		setVisibility(INVISIBLE);
		addView(tabContainer);
		addView(indicator);

		getViewTreeObserver().addOnPreDrawListener(this);
	}


	@Override
	public void onClick(View v) {
		for (int i = 0; i < tabContainer.getChildCount(); i++) {
			if (tabContainer.getChildAt(i) == v) {
				if (viewPager != null && viewPager.getAdapter() != null && i < viewPager.getAdapter().getItemCount()) {
					if (oldPosition == i && listener != null) {
						listener.onTabSelected();
					} else {
						viewPager.setCurrentItem(i);
						oldPosition = i;
					}
				}
				break;
			}
		}
	}


	@Override
	public void onGlobalLayout() {
		getViewTreeObserver().removeOnGlobalLayoutListener(this);
		// set tab item size
		for (int i = 0; i < tabContainer.getChildCount(); i++) {
			View tabItemView = tabContainer.getChildAt(i);
			ImageView icon = tabItemView.findViewById(R.id.tab_icon);
			TextView label = tabItemView.findViewById(R.id.tab_text);
			tabItemView.getLayoutParams().width = getMeasuredWidth() / Math.max(tabCount, 1);
			tabItemView.getLayoutParams().height = getMeasuredHeight();
			AppStyles.setDrawableColor(icon, settings.getIconColor());
			label.setTextColor(settings.getTextColor());
			tabItemView.requestLayout();
		}
		// set indicator size
		if (tabCount == 1) {
			setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
		} else {
			setHorizontalGravity(Gravity.START);
		}
		indicatorParams.width = getMeasuredWidth() / Math.max(tabCount, 2);
		indicator.setLayoutParams(indicatorParams);
		indicator.setBackgroundColor(settings.getHighlightColor());
		indicator.requestLayout();
		if (viewPager != null)
			setPosition((float) viewPager.getCurrentItem());
		// make this view visible after setting sizes and distances correctly
		post(new VisibilityDelay(this, true));
	}


	@Override
	public boolean onPreDraw() {
		getViewTreeObserver().removeOnPreDrawListener(this);
		View view = getRootView().findViewById(pagerId);
		if (view instanceof ViewPager2) {
			viewPager = (ViewPager2) view;
			setPosition((float) viewPager.getCurrentItem());
			viewPager.registerOnPageChangeCallback(new ViewPagerCallback());
		}
		return true;
	}

	/**
	 * set tab icons
	 *
	 * @param arrayRes array ID containing drawable IDs
	 */
	public void addTabIcons(@ArrayRes int arrayRes) {
		TypedArray tArray = getResources().obtainTypedArray(arrayRes);
		tabContainer.removeAllViews();
		if (viewPager != null && viewPager.getAdapter() != null) {
			tabCount = Math.min(tArray.length(), viewPager.getAdapter().getItemCount());
		} else {
			tabCount = tArray.length();
		}
		for (int i = 0; i < tabCount; i++) {
			View tabItemView = inflate(getContext(), R.layout.item_tab, null);
			ImageView icon = tabItemView.findViewById(R.id.tab_icon);
			int resId = tArray.getResourceId(i, 0);
			icon.setImageResource(resId);
			tabContainer.addView(tabItemView);
			tabItemView.setOnClickListener(this);
		}
		tArray.recycle();
		setVisibility(INVISIBLE);
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	/**
	 * add tab item labels using string array resource
	 *
	 * @param stringArray resource id of the string array
	 */
	public void addTabLabels(@ArrayRes int stringArray) {
		TypedArray tArray = getResources().obtainTypedArray(stringArray);
		if (viewPager != null && viewPager.getAdapter() != null) {
			tabCount = Math.min(tArray.length(), viewPager.getAdapter().getItemCount());
		} else {
			tabCount = tArray.length();
		}
		for (int i = 0; i < tabCount; i++) {
			setLabel(i, tArray.getString(i));
		}
		tArray.recycle();
	}

	/**
	 * set tab item label
	 *
	 * @param position index of the tab item
	 * @param text     text to set
	 */
	public void setLabel(int position, String text) {
		if (position >= 0 && position < tabContainer.getChildCount()) {
			View tabItemView = tabContainer.getChildAt(position);
			TextView tabLabel = tabItemView.findViewById(R.id.tab_text);
			tabLabel.setText(text);
			tabLabel.setVisibility(VISIBLE);
		}
	}

	/**
	 * add listener to call when a tab is selected
	 */
	public void addOnTabSelectedListener(OnTabSelectedListener listener) {
		this.listener = listener;
	}

	/**
	 * update colors if theme changes
	 */
	public void updateTheme() {
		for (int i = 0; i < tabContainer.getChildCount(); i++) {
			View tabItemView = tabContainer.getChildAt(i);
			ImageView icon = tabItemView.findViewById(R.id.tab_icon);
			TextView label = tabItemView.findViewById(R.id.tab_text);
			AppStyles.setDrawableColor(icon, settings.getIconColor());
			label.setTextColor(settings.getTextColor());
		}
		indicator.setBackgroundColor(settings.getHighlightColor());
	}

	/**
	 *
	 */
	private void setPosition(float positionOffset) {
		if (viewPager != null && viewPager.getAdapter() != null && tabCount > 0) {
			indicatorParams.width = getMeasuredWidth() / Math.max(tabCount, 2);
			indicatorParams.setMarginStart((int) (getMeasuredWidth() * positionOffset / tabCount));
			indicator.setLayoutParams(indicatorParams);
		}
	}

	/**
	 *
	 */
	private void setPage(int page) {
		if (viewPager != null && viewPager.getAdapter() != null && page < viewPager.getAdapter().getItemCount() && page < tabCount) {
			if (listener != null) {
				listener.onTabSelected();
			}
			oldPosition = page;
		}
	}

	/**
	 * Listener to call when a new tab is selected
	 */
	public interface OnTabSelectedListener {

		/**
		 * called on tab item click
		 */
		void onTabSelected();
	}

	/**
	 * {@link ViewPager2} callback used to determine page & scroll position
	 */
	private class ViewPagerCallback extends OnPageChangeCallback {


		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			super.onPageScrolled(position, positionOffset, positionOffsetPixels);
			setPosition(positionOffset + position);
		}


		@Override
		public void onPageSelected(int position) {
			super.onPageSelected(position);
			setPage(position);
		}


		@Override
		public void onPageScrollStateChanged(int state) {
			super.onPageScrollStateChanged(state);
		}
	}
}