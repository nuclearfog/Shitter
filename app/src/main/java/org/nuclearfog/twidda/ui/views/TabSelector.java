package org.nuclearfog.twidda.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * TabLayout implementation to provide a tab selector
 *
 * @author nuclearfog
 */
public class TabSelector extends LinearLayout implements OnClickListener, OnGlobalLayoutListener {

	@Nullable
	private ViewPager2 viewPager;
	private LinearLayout tabContainer;
	private View indicator;

	@Nullable
	private OnTabSelectedListener listener;
	private GlobalSettings settings;

	private int indicator_height;
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
		setOrientation(VERTICAL);
		settings = GlobalSettings.getInstance(context);
		tabContainer = new LinearLayout(context);
		tabContainer.setOrientation(LinearLayout.HORIZONTAL);
		indicator = new View(context);

		indicator.setVisibility(INVISIBLE);
		indicator_height = (int) getResources().getDimension(R.dimen.tabs_indicator_height);
		LayoutParams tabContainerParam = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
		tabContainerParam.weight = 1;

		tabContainer.setLayoutParams(tabContainerParam);
		addView(tabContainer);
		addView(indicator);
	}


	@Override
	public void onClick(View v) {
		for (int i = 0 ; i < tabContainer.getChildCount(); i++) {
			if (tabContainer.getChildAt(i) == v && listener != null) {
				if (viewPager != null && viewPager.getAdapter() != null && i < viewPager.getAdapter().getItemCount()) {
					listener.onTabSelected(oldPosition, i);
					viewPager.setCurrentItem(i);
					oldPosition = i;
				}
				break;
			}
		}
	}


	@Override
	public void onGlobalLayout() {
		getViewTreeObserver().removeOnGlobalLayoutListener(this);
		for (int i = 0 ; i < tabContainer.getChildCount(); i++) {
			View tabItemView = tabContainer.getChildAt(i);
			ImageView icon = tabItemView.findViewById(R.id.tab_icon);
			TextView label = tabItemView.findViewById(R.id.tab_text);
			tabItemView.getLayoutParams().width = getMeasuredWidth() / Math.max(tabCount, 1);
			tabItemView.getLayoutParams().height = getMeasuredHeight();
			AppStyles.setDrawableColor(icon, settings.getIconColor());
			label.setTextColor(settings.getTextColor());
		}
		LayoutParams params = new LayoutParams(getMeasuredWidth() / Math.max(tabCount, 1), 5);
		indicator.setLayoutParams(params);
		indicator.setBackgroundColor(settings.getHighlightColor());
		indicator.setVisibility(VISIBLE);
	}

	/**
	 * attach {@link ViewPager2} to this view
	 *
	 * @param viewPager ViewPager to interact with
	 */
	public void addViewPager(ViewPager2 viewPager) {
		this.viewPager = viewPager;
		viewPager.registerOnPageChangeCallback(new ViewPagerCallback());
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
		for (int i = 0 ; i < tabCount ; i++) {
			View tabItemView = inflate(getContext(), R.layout.item_tab, null);
			ImageView icon = tabItemView.findViewById(R.id.tab_icon);
			int resId = tArray.getResourceId(i, 0);
			icon.setImageResource(resId);
			tabContainer.addView(tabItemView);
			tabItemView.setOnClickListener(this);
		}
		tArray.recycle();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
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
	 */
	private void setPosition(float positionOffset) {
		if (viewPager != null && viewPager.getAdapter() != null && tabCount > 0) {
			LayoutParams params = new LayoutParams(getMeasuredWidth() / tabCount, indicator_height);
			params.setMarginStart((int) (getMeasuredWidth() * positionOffset / tabCount));
			indicator.setLayoutParams(params);
			indicator.setVisibility(VISIBLE);
		}
	}

	/**
	 */
	private void setPage(int page) {
		if (viewPager != null && viewPager.getAdapter() != null && page < viewPager.getAdapter().getItemCount() && page < tabCount) {
			if (listener != null) {
				listener.onTabSelected(oldPosition, page);
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
		 *
		 * @param oldPosition unselected position
		 * @param position    selected position
		 */
		void onTabSelected(int oldPosition, int position);
	}

	/**
	 * {@link ViewPager2} callback used to determine page & scroll position
	 */
	private class ViewPagerCallback extends OnPageChangeCallback {


		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			super.onPageScrolled(position, positionOffset, positionOffsetPixels);
			if (positionOffsetPixels > 0) {
				setPosition(positionOffset + position);
			}
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