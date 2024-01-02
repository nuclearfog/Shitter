package org.nuclearfog.twidda.ui.adapter.viewpager;

import androidx.annotation.IntRange;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.nuclearfog.twidda.ui.fragments.ListFragment;

/**
 * Adapter class for all {@link androidx.viewpager2.widget.ViewPager2} adapters
 *
 * @author nuclearfog
 */
public abstract class ViewPagerAdapter extends FragmentStateAdapter {

	/**
	 * used to communicate with fragments
	 */
	private ListFragment.ItemViewModel viewModel;

	private int count = 0;

	/**
	 *
	 */
	protected ViewPagerAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);
		viewModel = new ViewModelProvider(fragmentActivity).get(ListFragment.ItemViewModel.class);
	}


	@Override
	public int getItemCount() {
		return count;
	}

	/**
	 * set page count
	 *
	 * @param count number of pages
	 */
	public void setPageCount(@IntRange(from = 1) int count) {
		this.count = count;
	}

	/**
	 * called when app settings change
	 */
	public void notifySettingsChanged() {
		viewModel.notify(ListFragment.NOTIFY_CHANGED);
	}

	/**
	 * called to scroll page to top
	 */
	public void scrollToTop() {
		viewModel.notify(ListFragment.NOTIFY_SCROLL_TOP);
	}
}