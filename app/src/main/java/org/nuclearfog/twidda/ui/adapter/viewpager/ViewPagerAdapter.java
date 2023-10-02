package org.nuclearfog.twidda.ui.adapter.viewpager;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.nuclearfog.twidda.ui.fragments.ListFragment;

import java.util.Random;

/**
 * @author nuclearfog
 */
public abstract class ViewPagerAdapter extends FragmentStateAdapter {

	private static final Random RND = new Random();

	private ListFragment.ItemViewModel viewModel;

	private int count;
	private long[] ids;

	/**
	 *
	 */
	protected ViewPagerAdapter(FragmentActivity fragmentActivity, int count) {
		super(fragmentActivity);
		viewModel = new ViewModelProvider(fragmentActivity).get(ListFragment.ItemViewModel.class);
		this.count = count;
		// create fragment session IDs
		ids = new long[count];
		for (int i = 0 ; i < ids.length ; i++) {
			ids[i] = RND.nextLong();
		}
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