package org.nuclearfog.twidda.ui.adapter.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.fragments.ListFragment;

import java.util.ArrayList;

/**
 * @author nuclearfog
 */
public class ViewPagerAdapter extends FragmentStateAdapter {

	protected ArrayList<ListFragment> fragments = new ArrayList<>();
	protected GlobalSettings settings;

	/**
	 *
	 */
	public ViewPagerAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);
		settings = GlobalSettings.get(fragmentActivity.getApplicationContext());
	}


	@Override
	public final long getItemId(int position) {
		return fragments.get(position).getSessionId();
	}


	@Override
	public final boolean containsItem(long itemId) {
		for (ListFragment fragment : fragments) {
			if (fragment.getSessionId() == itemId)
				return true;
		}
		return false;
	}


	@NonNull
	@Override
	public final Fragment createFragment(int position) {
		return fragments.get(position);
	}


	@Override
	public final int getItemCount() {
		return fragments.size();
	}

	/**
	 * called when app settings change
	 */
	public void notifySettingsChanged() {
		for (ListFragment fragment : fragments) {
			if (!fragment.isDetached()) {
				fragment.reset();
			}
		}
	}

	/**
	 * called to scroll page to top
	 *
	 * @param index tab position of page
	 */
	public void scrollToTop(int index) {
		if (!fragments.get(index).isDetached()) {
			fragments.get(index).onTabChange();
		}
	}
}