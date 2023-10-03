package org.nuclearfog.twidda.ui.adapter.viewpager;

import androidx.annotation.IntRange;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.nuclearfog.twidda.ui.fragments.ListFragment;

import java.util.Random;

/**
 * @author nuclearfog
 */
public abstract class ViewPagerAdapter extends FragmentStateAdapter {

	private static final Random RAND = new Random();

	private ListFragment.ItemViewModel viewModel;

	private int count = 0;
	private long[] ids = {};

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


	@Override
	public long getItemId(int position) {
		return ids[position];
	}


	@Override
	public boolean containsItem(long itemId) {
		for (long id : ids) {
			if (id == itemId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * set page count
	 *
	 * @param count number of pages
	 */
	public void setPageCount(@IntRange(from = 1) int count) {
		this.count = count;
		// create fragment session IDs
		ids = new long[count];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = RAND.nextLong();
		}
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